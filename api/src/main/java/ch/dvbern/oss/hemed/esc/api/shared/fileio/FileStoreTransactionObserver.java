package ch.dvbern.oss.hemed.esc.api.shared.fileio;

import java.time.Duration;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.event.Observes;
import jakarta.enterprise.event.TransactionPhase;

import ch.dvbern.oss.hemed.esc.api.shared.types.IgnoreForIdClassTest;
import lombok.extern.slf4j.Slf4j;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.slf4j.MDC;
import software.amazon.awssdk.services.s3.S3AsyncClient;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;

/**
 * Carries out the S3 side of a transaction once its outcome is known: an upload is deleted again when the transaction
 * that was to reference it fails, and a delete happens only when the transaction that removed the reference commits.
 * {@link FileStoreService} fires the events, the container calls these transactional observers after completion.
 * <p>
 * After completion may run on Narayana's transaction reaper thread, when the transaction timed out. There is no
 * request there, so no tenant and no MDC: the observers only use what the {@link FileStoreTxBinding} captured on
 * the thread that bound the file, and must not call other beans or the database.
 */
@ApplicationScoped
@Slf4j
public class FileStoreTransactionObserver {

    static final String LOG_PREFIX = "ESC-S3-TX";

    /**
     * Everything needed to delete the file later and to trace that delete back to the action that caused it.
     *
     * @param operationId short random id, printed on every log line of this binding
     * @param transactionId the Narayana transaction uid, the same id the ARJUNA log messages print
     * @param origin the application method that asked for the upload or delete
     * @param mdc the logging context (principal, request path, tenant) of the request that bound the file
     */
    record FileStoreTxBinding(
        @NonNull @IgnoreForIdClassTest String operationId,
        @NonNull @IgnoreForIdClassTest String transactionId,
        @NonNull String origin,
        @NonNull S3AsyncClient s3,
        @NonNull DeleteObjectRequest deleteRequest,
        @NonNull Duration timeout,
        @NonNull Map<String, String> mdc
    ) {
        @NonNull
        String describe() {
            return "op=%s tx=%s file='%s' bucket=%s origin=%s".formatted(
                operationId,
                transactionId,
                deleteRequest.key(),
                deleteRequest.bucket(),
                origin
            );
        }
    }

    /**
     * Record with infos about a file that we were newly uplaoding, should be deleted if transaction fails
     */
    record UploadedInTransaction(
        FileStoreTransactionObserver.FileStoreTxBinding fileStoreTxBinding
    ) {
    }

    /**
     * Record with infos about a file that we are in the process of removing, only delete if transaction works
     */
    record DeletionAfterCommit(
        FileStoreTransactionObserver.FileStoreTxBinding fileStoreTxBinding
    ) {
    }

    /**
     * This Observer runs when a transaction FAILED -> we have to clean up newly uploaded Files files in s3
     */
    void deleteUploadOfFailedTransaction(
        @Observes(during = TransactionPhase.AFTER_FAILURE) @NonNull UploadedInTransaction event
    ) {
        delete(event.fileStoreTxBinding(), "rolled back");
    }

    /**
     * This Observer runs when a transaction Suceeds -> On Entity Deletion in DB we have to clean up the file in s3
     * as well
     */
    void deleteFileOfCommittedTransaction(
        @Observes(during = TransactionPhase.AFTER_SUCCESS) @NonNull DeletionAfterCommit event
    ) {
        delete(event.fileStoreTxBinding(), "committed");
    }

    private void delete(
        FileStoreTransactionObserver.FileStoreTxBinding fileStoreTxBinding,
        @NonNull String outcomeMsg
    ) {
        Map<String, String> callbackMdc = MDC.getCopyOfContextMap();
        // log with the context of the request that bound the file, not the one of the reaper thread
        MDC.setContextMap(fileStoreTxBinding.mdc());
        try {
            fileStoreTxBinding.s3()
                .deleteObject(fileStoreTxBinding.deleteRequest())
                .get(fileStoreTxBinding.timeout().toMillis(), TimeUnit.MILLISECONDS);
            LOG.info(
                "{} {}: transaction {}, file deleted in S3",
                LOG_PREFIX,
                fileStoreTxBinding.describe(),
                outcomeMsg
            );
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            logFailedDelete(fileStoreTxBinding, outcomeMsg, e);
        } catch (ExecutionException | TimeoutException | RuntimeException e) {
            // ArC only logs what escapes a transactional observer, without the file: this is the one place to name it
            logFailedDelete(fileStoreTxBinding, outcomeMsg, e);
        } finally {
            if (callbackMdc == null) {
                MDC.clear();
            } else {
                MDC.setContextMap(callbackMdc);
            }
        }
    }

    private static void logFailedDelete(
        FileStoreTransactionObserver.FileStoreTxBinding fileStoreTxBinding,
        @NonNull String outocmeMsg,
        @NonNull Exception e
    ) {
        LOG.error(
            "{} {}: transaction {}, but the file could not be deleted in S3, "
                + "the file store health check will report it",
            LOG_PREFIX,
            fileStoreTxBinding.describe(),
            outocmeMsg,
            e
        );
    }
}
