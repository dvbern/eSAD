/*
 *
 * Copyright (C) 2025 DV Bern AG, Switzerland
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package ch.dvbern.dvbstarter.infrastructure.fileio;

import java.io.IOException;
import java.io.Serial;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;

import ch.dvbern.dvbstarter.infrastructure.errorhandling.AppFailureException;
import ch.dvbern.dvbstarter.infrastructure.errorhandling.AppFailureMessage;
import ch.dvbern.dvbstarter.shared.types.cleanfilename.CleanFileName;
import ch.dvbern.oss.hemed.esc.api.shared.fileio.FileInfo;
import ch.dvbern.oss.hemed.esc.api.shared.fileio.FileStoreService;
import com.google.common.net.UrlEscapers;
import io.smallrye.mutiny.Multi;
import io.smallrye.mutiny.Uni;
import io.vertx.mutiny.core.buffer.Buffer;
import jakarta.ws.rs.core.HttpHeaders;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.StreamingOutput;
import lombok.experimental.UtilityClass;
import lombok.extern.slf4j.Slf4j;
import mutiny.zero.flow.adapters.AdaptersToFlow;
import org.apache.tika.mime.MediaType;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.jboss.resteasy.reactive.RestMulti;

/**
 * Utility class to construct file download responses, either Multi<Buffer> or Response
 */
@UtilityClass
@Slf4j
public class FileDownloadUtil {

    public static final String CONTENT_DISPOSITION = "Content-Disposition";
    public static final String FILENAME = ";filename=";
    public static final String MIME_TYPE_DOCX = "vnd.openxmlformats-officedocument.wordprocessingml.document";
    public static final String MIME_TYPE_XLSX = "vnd.openxmlformats-officedocument.spreadsheetml.sheet";

    @NonNull
    public static Multi<Buffer> getBufferRestMultiWithInfo(
        @NonNull FileInfo fileInfoBucket,
        @NonNull FileStoreService fileStoreService,
        @NonNull String disposition
    ) {
        String fileId = fileInfoBucket.getFileIdInBucket();
        final String filename = urlEscapeAndQuoteFilename(fileInfoBucket.getFileName().getFileName());
        final String contentType = fileInfoBucket.getMediaType().toString();

        return RestMulti.fromUniResponse(
            Uni.createFrom().completionStage(() -> fileStoreService.getFileAsync(fileId)),
            response -> Multi.createFrom()
                .safePublisher(AdaptersToFlow.publisher(response))
                .map(FileStoreService::toBuffer),
            response -> Map.of(
                CONTENT_DISPOSITION,
                List.of(disposition + FILENAME + filename),
                "Content-Type",
                List.of(contentType)));
    }

    /**
     * Create the response with the file from the storage but keep the original filename and content type
     */
    public Multi<Buffer> getBufferMultiWithInfo(
        @NonNull FileInfo fileInfoBucket,
        @NonNull FileStoreService fileStoreService,
        @NonNull String disposition
    ) {
        String fileId = fileInfoBucket.getFileIdInBucket();
        String filename = urlEscapeAndQuoteFilename(fileInfoBucket.getFileName().getFileName());
        final String contentType = fileInfoBucket.getMediaType().toString();

        return Uni.createFrom().completionStage(() -> fileStoreService.getFileAsync(fileId))
            .onItem()
            .transformToMulti(responsePublisher -> RestMulti
                .fromMultiData(Multi.createFrom().safePublisher(AdaptersToFlow.publisher(responsePublisher))
                    .map(FileStoreService::toBuffer))
                .header(CONTENT_DISPOSITION, disposition + FILENAME + filename)
                .header("Content-Type", contentType)
                .build());
    }

    @NonNull
    public static Multi<Buffer> getBufferRestMultiWithInfo(
        @NonNull FileInfo fileInfoBucket,
        @NonNull FileStoreService fileStoreService
    ) {
        return getBufferRestMultiWithInfo(fileInfoBucket, fileStoreService, "attachment");
    }

    @NonNull
    public static Response buildFileResponse(
        @NonNull String disposition,
        byte[] content,
        @NonNull FileInfo fileInfoBucket
    ) {

        String mimeType = fileInfoBucket.getMediaType().toString();

        String filename = urlEscapeAndQuoteFilename(fileInfoBucket.getFileName().getFileName());
        String contentDispositionHeaderValue = disposition + FILENAME + filename;

        return Response
            .ok(content, mimeType)
            .header(CONTENT_DISPOSITION, contentDispositionHeaderValue)
            // note: content-length is not allowed if transfer is chunked
            // ... which is used for most downloads by Jackson
            //.header(HttpHeaders.CONTENT_LENGTH, content.length)
            .build();
    }

    @NonNull
    private String urlEscapeAndQuoteFilename(@NonNull String filename) {
        return '"' + UrlEscapers.urlFragmentEscaper().escape(filename) + '"';
    }

    /**
     * Streams an already-written temp file to the client and deletes it afterwards, so that large downloads never have
     * to be held on the heap.
     * <p>
     * The delete covers the transfer completing and the client aborting mid-transfer, which is what actually happens
     * in normal operation. It does not cover the response body never being written at all: the caller sits behind
     * {@code @JaxResource}, which is {@code @Transactional}, so the transaction commits only after the resource method
     * returns and the entity is written after that again. If that commit rolls back - the plausible failure for a
     * large batch, see esc.archive.bulk-download.max-files - the StreamingOutput below never runs and the file is left
     * behind in esc.tmp.dir. Nothing sweeps those today; if bulk downloads start timing out, add a scheduled cleanup.
     * <p>
     * Unlike {@link #buildFileResponse} this sets an explicit Content-Length, which is known here and lets the browser
     * show real download progress.
     *
     * @param content temp file to stream; ownership passes to the response, do not read or delete it afterwards.
     */
    @NonNull
    public static Response buildStreamingFileResponse(
        @NonNull String disposition,
        @NonNull Path content,
        @NonNull CleanFileName fileName,
        @NonNull String mimeType
    ) {
        long contentLength;
        try {
            contentLength = Files.size(content);
        } catch (IOException e) {
            deleteQuietly(content);
            throw new AppFailureException(
                AppFailureMessage.internalError("Could not determine size of download " + content),
                e);
        }

        String contentDispositionHeaderValue = disposition + FILENAME + urlEscapeAndQuoteFilename(fileName.getFileName());

        StreamingOutput stream = output -> {
            try {
                Files.copy(content, output);
                output.flush();
            } finally {
                deleteQuietly(content);
            }
        };

        return Response
            .ok(stream, mimeType)
            .header(CONTENT_DISPOSITION, contentDispositionHeaderValue)
            .header(HttpHeaders.CONTENT_LENGTH, contentLength)
            .build();
    }

    private void deleteQuietly(@NonNull Path file) {
        try {
            Files.deleteIfExists(file);
        } catch (IOException e) {
            LOG.warn("Could not delete temp file [{}] after download", file, e);
        }
    }

    @NonNull
    public static FileInfo buildFileInfo(@NonNull String fileName, @NonNull String mimeType, long fileSize) {
        return new FileInfo() {
            @Serial
            private static final long serialVersionUID = -2829730724238936664L;

            @Override
            public CleanFileName getFileName() {
                return CleanFileName.parse(fileName);
            }

            @Override
            public MediaType getMediaType() {
                return MediaType.application(mimeType);
            }

            @Override
            public long getFileSize() {
                return fileSize;
            }
        };
    }
}
