/*
 * Copyright (C) 2026 DV Bern AG, Switzerland
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package ch.dvbern.oss.hemed.esc.api.shared.fileio;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.FileAttribute;
import java.nio.file.attribute.PosixFilePermission;
import java.nio.file.attribute.PosixFilePermissions;
import java.util.Set;
import jakarta.enterprise.context.ApplicationScoped;

import ch.dvbern.dvbstarter.infrastructure.errorhandling.AppFailureException;
import ch.dvbern.dvbstarter.infrastructure.errorhandling.AppFailureMessage;
import ch.dvbern.dvbstarter.infrastructure.stereotypes.NoTransactionService;
import ch.dvbern.dvbstarter.shared.types.cleanfilename.CleanFileName;
import ch.dvbern.oss.hemed.esc.api.shared.config.service.ConfigService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.checkerframework.checker.nullness.qual.NonNull;

/**
 * Creates temp files below {@code esc.tmp.dir} for payloads that are too large to keep on the heap.
 * <p>
 * Deliberately {@link NoTransactionService} rather than a {@code @Service}: this touches the filesystem only, so it must
 * not join (or start) the caller's transaction.
 */
@ApplicationScoped
@NoTransactionService
@RequiredArgsConstructor
@Slf4j
public class TempFileService {

    private static final FileAttribute<Set<PosixFilePermission>> FILE_PERMS_OWNER_ONLY =
        PosixFilePermissions.asFileAttribute(PosixFilePermissions.fromString("rw-------"));

    private final ConfigService configService;

    @NonNull
    public Path createTempFile(@NonNull CleanFileName fileName, byte[] content) {
        Path tempFileLoc = createTempFile(fileName);
        try {
            FileUtils.writeByteArrayToFile(tempFileLoc.toFile(), content);
            return tempFileLoc;
        } catch (IOException e) {
            deleteQuietly(tempFileLoc);
            throw new AppFailureException(AppFailureMessage.internalError("Error while writing to temp file"), e);
        }
    }

    @NonNull
    public Path createTempFile(@NonNull CleanFileName fileName) {
        try {
            Path tmpDir = configService.getTmpDir();
            String prefix = FilenameUtils.getBaseName(fileName.getFileName()) + '_';
            String extension = FilenameUtils.getExtension(fileName.getFileName());
            String suffix = extension.isEmpty() ? "" : '.' + extension;

            if (tmpDir.getFileSystem().supportedFileAttributeViews().contains("posix")) {
                return Files.createTempFile(tmpDir, prefix, suffix, FILE_PERMS_OWNER_ONLY);
            }
            return Files.createTempFile(tmpDir, prefix, suffix);
        } catch (IOException e) {
            throw new AppFailureException(AppFailureMessage.internalError("Error while creating temp file"), e);
        }
    }

    /**
     * Best-effort cleanup. Never throws: callers use this on paths where a leftover temp file is the lesser evil
     * compared to masking the root cause exception
     */
    public void deleteQuietly(@NonNull Path tempFile) {
        try {
            Files.deleteIfExists(tempFile);
        } catch (IOException e) {
            LOG.warn("Could not delete temp file [{}]", tempFile, e);
        }
    }
}
