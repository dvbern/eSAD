/*
 *
 * Copyright (C) 2024 DV Bern AG, Switzerland
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

package ch.dvbern.oss.hemed.esc.api.shared.fileio;

import java.nio.ByteBuffer;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;

import ch.dvbern.dvbstarter.infrastructure.errorhandling.AppFailureException;
import ch.dvbern.dvbstarter.infrastructure.errorhandling.AppFailureMessage;
import ch.dvbern.dvbstarter.infrastructure.stereotypes.Service;
import ch.dvbern.oss.hemed.esc.api.shared.config.service.ConfigService;
import ch.dvbern.oss.hemed.esc.api.shared.fileio.s3.S3RequestBuilder;
import io.smallrye.mutiny.Uni;
import io.vertx.mutiny.core.buffer.Buffer;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.jboss.resteasy.reactive.multipart.FileUpload;
import software.amazon.awssdk.core.ResponseBytes;
import software.amazon.awssdk.core.async.AsyncRequestBody;
import software.amazon.awssdk.core.async.AsyncResponseTransformer;
import software.amazon.awssdk.core.async.ResponsePublisher;
import software.amazon.awssdk.services.s3.S3AsyncClient;
import software.amazon.awssdk.services.s3.model.DeleteObjectResponse;
import software.amazon.awssdk.services.s3.model.GetObjectResponse;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.model.PutObjectResponse;

@RequiredArgsConstructor
@Service
@Slf4j
public class FileStoreService  {

    private final S3AsyncClient s3;
    private final ConfigService configService;
    private final S3RequestBuilder s3RequestBuilder;

    private static void logFileUpload(@NonNull FileInfo fileInfo, @NonNull PutObjectResponse request) {
        LOG.info(
            "File='{}' uploaded to S3, etag={}",
            fileInfo.getFileIdInBucket(),
            request.eTag());
    }

    @NonNull
    public Uni<PutObjectResponse> uploadToS3Async(
        @NonNull FileUpload fileUpload,
        @NonNull FileInfo fileInfo
    ) {
        return Uni.createFrom()
            .completionStage(() ->
                this.createS3AsyncPutRequest(
                    fileUpload,
                    configService.getS3BucketName(),
                    fileInfo.getFileIdInBucket())
            )
            .invoke(request -> logFileUpload(fileInfo, request));
    }

    @NonNull
    public Uni<PutObjectResponse> uploadToS3Async(
        @NonNull String contentType,
        byte[] content,
        @NonNull FileInfo fileInfo
    ) {
        return Uni.createFrom()
            .completionStage(() ->
                this.createS3AsyncPutRequest(
                    contentType,
                    content,
                    configService.getS3BucketName(),
                    fileInfo.getFileIdInBucket())
            )
            .invoke(request -> logFileUpload(fileInfo, request));
    }

    @NonNull
    private CompletableFuture<PutObjectResponse> createS3AsyncPutRequest(
        @NonNull FileUpload fileUpload,
        @NonNull String bucketName,
        @NonNull String objectIdentifier
    ) {
        PutObjectRequest putObjectRequest = s3RequestBuilder.buildPutRequest(
            fileUpload.contentType(), bucketName, objectIdentifier
        );

        return s3.putObject(
            putObjectRequest,
            AsyncRequestBody.fromFile(fileUpload.uploadedFile())
        );
    }

    @NonNull
    private CompletableFuture<PutObjectResponse> createS3AsyncPutRequest(
        @NonNull String contentType,
        byte[] content,
        @NonNull String bucketName,
        @NonNull String objectIdentifier
    ) {
        PutObjectRequest putObjectRequest = s3RequestBuilder.buildPutRequest(
            contentType, bucketName, objectIdentifier
        );

        return s3.putObject(
            putObjectRequest,
            AsyncRequestBody.fromByteBuffer(ByteBuffer.wrap(content))
        );
    }


    @NonNull
    public Uni<DeleteObjectResponse> deleteInS3Async(
        @NonNull String fileIdInBucket
    ) {
        return Uni.createFrom()
            .completionStage(() ->
                this.createDeleteFileRequest(
                    configService.getS3BucketName(),
                    fileIdInBucket)
            )
            .invoke(response -> LOG.info("File='{}' deleted in S3", fileIdInBucket));
    }

    @NonNull
    public <T> Uni<T> deleteFileWithIdAsyncAndReturnResponseIgnoringErrors(
        @Nullable String fileIdInBucket,
        @NonNull T response
    ) {
        if (fileIdInBucket != null) {
            return this.deleteInS3Async(fileIdInBucket)
                .onFailure().recoverWithItem(throwable -> {
                    LOG.error(
                        "WCP: Could not delete old file for fileId={}",
                        fileIdInBucket,
                        throwable);
                    return null;
                })
                .onItem().transformToUni(deleteResponse -> Uni.createFrom().item(response));
        }
        return Uni.createFrom().item(response);
    }

    @NonNull
    private CompletableFuture<DeleteObjectResponse> createDeleteFileRequest(
        @NonNull String bucketName,
        @NonNull String fileIdInBucket) {
        return s3.deleteObject(
            s3RequestBuilder.buildDeleteObjectRequest(bucketName, fileIdInBucket)
        );
    }

    @NonNull
    public CompletionStage<ResponsePublisher<GetObjectResponse>> getFileAsync(@NonNull String fileIdInBucket) {

        return s3.getObject(
            s3RequestBuilder.buildGetRequest(configService.getS3BucketName(), fileIdInBucket),
            AsyncResponseTransformer.toPublisher()
        );
    }

    @NonNull
    public CompletableFuture<ResponseBytes<GetObjectResponse>> getFileBytesAsync(@NonNull String fileIdInBucket) {
        return s3.getObject(
            s3RequestBuilder.buildGetRequest(configService.getS3BucketName(), fileIdInBucket),
            AsyncResponseTransformer.toBytes()
        );
    }

    public byte[] getFileBytesSync(@NonNull String fileIdInBucket) {
        CompletableFuture<ResponseBytes<GetObjectResponse>> futureGetBytes =
            this.getFileBytesAsync(fileIdInBucket);
        CompletableFuture<ResponseBytes<GetObjectResponse>> loadFileFromS3Future =
            futureGetBytes.exceptionally(throwable -> {
                LOG.error("Error while loading file {} ", fileIdInBucket, throwable);
                return null;
            });
        ResponseBytes<GetObjectResponse> getResponse = loadFileFromS3Future.join();

        if (getResponse != null) {
            return getResponse.asByteArray();
        }
        throw new AppFailureException(AppFailureMessage.internalError("Could not load file from S3 " + fileIdInBucket));
    }

    public boolean isFilePresent(@NonNull String fileIdInBucket) {
        CompletableFuture<ResponseBytes<GetObjectResponse>> futureGetBytes =
            this.getFileBytesAsync(fileIdInBucket);
        CompletableFuture<ResponseBytes<GetObjectResponse>> loadFileFromS3Future =
            futureGetBytes.exceptionally(throwable -> {
                LOG.error("Error while loading file {} ", fileIdInBucket, throwable);
                return null;
            });
        ResponseBytes<GetObjectResponse> getResponse = loadFileFromS3Future.join();
        return getResponse != null;
    }

    @NonNull
    public static Buffer toBuffer(@NonNull ByteBuffer bytebuffer) {
        byte[] result = new byte[bytebuffer.remaining()];
        bytebuffer.get(result);
        return Buffer.buffer(result);
    }
}
