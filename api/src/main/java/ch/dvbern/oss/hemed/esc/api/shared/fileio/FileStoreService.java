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

import java.lang.StackWalker.StackFrame;
import java.nio.ByteBuffer;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;

import jakarta.enterprise.event.Event;
import jakarta.transaction.Status;
import jakarta.transaction.SystemException;
import jakarta.transaction.TransactionManager;
import jakarta.transaction.TransactionSynchronizationRegistry;
import jakarta.transaction.Transactional;

import ch.dvbern.dvbstarter.infrastructure.errorhandling.AppFailureException;
import ch.dvbern.dvbstarter.infrastructure.errorhandling.AppFailureMessage;
import ch.dvbern.dvbstarter.infrastructure.stereotypes.Service;
import ch.dvbern.oss.hemed.esc.api.shared.config.service.ConfigService;
import ch.dvbern.oss.hemed.esc.api.shared.fileio.FileStoreTransactionObserver.FileStoreTxBinding;
import ch.dvbern.oss.hemed.esc.api.shared.fileio.FileStoreTransactionObserver.DeletionAfterCommit;
import ch.dvbern.oss.hemed.esc.api.shared.fileio.FileStoreTransactionObserver.UploadedInTransaction;
import ch.dvbern.oss.hemed.esc.api.shared.fileio.s3.S3RequestBuilder;
import io.smallrye.mutiny.Uni;
import io.vertx.mutiny.core.buffer.Buffer;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.jboss.resteasy.reactive.multipart.FileUpload;
import org.slf4j.MDC;
import software.amazon.awssdk.core.ResponseBytes;
import software.amazon.awssdk.core.async.AsyncRequestBody;
import software.amazon.awssdk.core.async.AsyncResponseTransformer;
import software.amazon.awssdk.core.async.ResponsePublisher;
import software.amazon.awssdk.services.s3.S3AsyncClient;
import software.amazon.awssdk.services.s3.model.DeleteObjectResponse;
import software.amazon.awssdk.services.s3.model.GetObjectResponse;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.model.PutObjectResponse;

import static ch.dvbern.oss.hemed.esc.api.shared.fileio.FileStoreTransactionObserver.LOG_PREFIX;

/**
 * Access to the S3 file store.
 * <p>
 * A file that a database row references is uploaded with {@link #uploadToS3BoundToTransaction} and deleted with
 * {@link #deleteInS3AfterCommit}: S3 then follows the outcome of the transaction that writes or removes the row, see
 * {@link FileStoreTransactionObserver}. The unbound methods are for files no row points to, like the temporary files
 * of a Massenbrief.
 */
@RequiredArgsConstructor
@Service
@Slf4j
public class FileStoreService  {

    private final S3AsyncClient s3;
    private final ConfigService configService;
    private final S3RequestBuilder s3RequestBuilder;
    private final TransactionManager transactionManager;
    private final TransactionSynchronizationRegistry transactionSynchronizationRegistry;
    // events sinks for S3 actions
    private final Event<UploadedInTransaction> uploadedInTransaction;
    private final Event<DeletionAfterCommit> deletionAfterCommit;

    private static void logFileUpload(@NonNull FileInfo fileInfo, @NonNull PutObjectResponse request) {
        LOG.info(
            "File='{}' uploaded to S3, etag={}",
            fileInfo.getFileIdInBucket(),
            request.eTag());
    }

    /**
     * Uploads a file that a database row is going to reference, and deletes it again if the current transaction does
     * not commit. Without this, a rollback after the upload (an optimistic locking conflict detected at commit, for
     * instance) leaves an object in S3 that nothing points to.
     * <p>
     * MANDATORY because only the caller's transaction decides whether the row exists. With the REQUIRED of the
     * stereotype, a caller without a transaction would get one just for this method, which commits right away.
     */
    @Transactional(Transactional.TxType.MANDATORY)
    @NonNull
    public PutObjectResponse uploadToS3BoundToTransaction(
        @NonNull FileUpload fileUpload,
        @NonNull FileInfo fileInfo
    ) {
        FileStoreTxBinding fileStoreTxBinding = bindUploadToTransaction(fileInfo);
        PutObjectResponse response = Uni.createFrom()
            .completionStage(() ->
                this.createS3AsyncPutRequest(
                    fileUpload,
                    fileStoreTxBinding.deleteRequest().bucket(),
                    fileInfo.getFileIdInBucket()
                )
            )
            .await()
            .atMost(configService.getS3UploadTimeout());
        logFileUpload(fileInfo, response);
        return response;
    }

    /**
     * Same as {@link #uploadToS3BoundToTransaction(FileUpload, FileInfo)}, for content generated in memory.
     */
    @Transactional(Transactional.TxType.MANDATORY)
    @NonNull
    public PutObjectResponse uploadToS3BoundToTransaction(
        @NonNull String contentType,
        byte[] content,
        @NonNull FileInfo fileInfo
    ) {
        FileStoreTxBinding fileStoreTxBinding = bindUploadToTransaction(fileInfo);
        PutObjectResponse response = Uni.createFrom()
            .completionStage(() ->
                this.createS3AsyncPutRequest(
                    contentType,
                    content,
                    fileStoreTxBinding.deleteRequest().bucket(),
                    fileInfo.getFileIdInBucket()
                )
            )
            .await()
            .atMost(configService.getS3UploadTimeout());
        logFileUpload(fileInfo, response);
        return response;
    }

    /**
     * Deletes the file once the current transaction has committed, and never if it rolls back. Deleting right away
     * loses the file whenever the transaction rolls back later, because the row pointing to it comes back.
     */
    @Transactional(Transactional.TxType.MANDATORY)
    public void deleteInS3AfterCommit(@NonNull String fileIdInBucket) {
        FileStoreTxBinding fileStoreTxBinding = bindToTransaction(fileIdInBucket);
        deletionAfterCommit.fire(new DeletionAfterCommit(fileStoreTxBinding));
        LOG.info(
            "{} {}: delete bound to the transaction, carried out once it commits",
            LOG_PREFIX,
            fileStoreTxBinding.describe()
        );
    }


    private FileStoreTransactionObserver.FileStoreTxBinding bindUploadToTransaction(@NonNull FileInfo fileInfo) {
        FileStoreTxBinding fileStoreTxBinding = bindToTransaction(fileInfo.getFileIdInBucket());
        // fired before the upload, so that an upload which fails halfway is cleaned up by the rollback as well,
        // deleting a key that never arrived is a no-op in S3
        uploadedInTransaction.fire(new UploadedInTransaction(fileStoreTxBinding));
        LOG.info(
            "{} {}: upload bound to the transaction, deleted again if it does not commit",
            LOG_PREFIX,
            fileStoreTxBinding.describe()
        );
        return fileStoreTxBinding;
    }


    private FileStoreTransactionObserver.FileStoreTxBinding bindToTransaction(@NonNull String fileIdInBucket) {
        requireActiveTransaction(fileIdInBucket);
        String bucketName = configService.getS3BucketName();
        // resolved now, on the thread of the request: the observer may run on the transaction reaper thread, which has
        // no tenant and no logging context
        return new FileStoreTxBinding(
            UUID.randomUUID().toString().substring(0, 8),
            String.valueOf(transactionSynchronizationRegistry.getTransactionKey()),
            origin(), // remember originating method
            s3,
            s3RequestBuilder.buildDeleteObjectRequest(bucketName, fileIdInBucket),
            configService.getS3DeleteTimeout(),
            Objects.requireNonNullElse(MDC.getCopyOfContextMap(), Map.of())
        );
    }

    /**
     * ArC calls transactional observers right away when the transaction is not ACTIVE, for instance when it is already
     * marked for rollback. A deferred delete would then remove the file while the rollback keeps the row pointing to
     * it, and an upload would be deleted before it is even written.
     */
    private void requireActiveTransaction(@NonNull String fileIdInBucket) {
        int status;
        try {
            status = transactionManager.getStatus();
        } catch (SystemException e) {
            throw new AppFailureException(
                AppFailureMessage.internalError("Could not read the transaction status for S3 file " + fileIdInBucket),
                e
            );
        }
        if (status != Status.STATUS_ACTIVE) {
            throw new AppFailureException(
                AppFailureMessage.internalError(
                    "S3 file " + fileIdInBucket + " can only be bound to an active transaction, status=" + status
                )
            );
        }
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
                    fileInfo.getFileIdInBucket()
                )
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
                    fileIdInBucket
                )
            )
            .invoke(response -> LOG.info("File='{}' deleted in S3", fileIdInBucket));
    }

    @NonNull
    private CompletableFuture<DeleteObjectResponse> createDeleteFileRequest(
        @NonNull String bucketName,
        @NonNull String fileIdInBucket
    ) {
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

    /**
     * NOT_SUPPORTED is load-bearing, do not remove it. FileStoreService is a {@link Service}, so its stereotype
     * carries @Transactional(REQUIRED): without NOT_SUPPORTED this method joins the caller's transaction and the
     * AppFailureException below marks that transaction rollback-only. A caller that catches the exception to skip a
     * single unreadable file - the bulk Archivdokument download does exactly this - would then still lose every write
     * of the surrounding batch at commit time, while happily returning the zip to the user.
     * <p>
     * Suspending the transaction is safe here because this method only talks to S3, never to the database.
     */
    @Transactional(Transactional.TxType.NOT_SUPPORTED)
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

    /**
     * The application method that asked for the upload or delete, for the log: the first caller outside the file
     * store classes and the subclasses and proxies ArC generates.
     */
    @NonNull
    private static String origin() {
        Optional<StackFrame> caller = StackWalker.getInstance(StackWalker.Option.RETAIN_CLASS_REFERENCE)
            .walk(frames -> frames
                .filter(frame -> isApplicationFrame(frame.getClassName()))
                .findFirst()
            );
        return caller
            .map(frame -> frame.getDeclaringClass().getSimpleName() + '.' + frame.getMethodName())
            .orElse("unknown");
    }

    /**
     * this is just a helper to filter for the first in App class that called for the FileOperation
     */
    private static boolean isApplicationFrame(@NonNull String className) {
        return className.startsWith("ch.dvbern.oss.hemed.esc.")
            && !className.equals(FileStoreService.class.getName())
            && !className.equals(FileDataService.class.getName())
            && !className.contains("_Subclass")
            && !className.contains("_ClientProxy");
    }
}
