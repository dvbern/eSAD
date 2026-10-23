package ch.dvbern.oss.hemed.esc.api.shared.fileio;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.Callable;
import java.util.concurrent.CyclicBarrier;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;
import java.util.logging.LogRecord;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import jakarta.transaction.Status;
import jakarta.transaction.SystemException;
import jakarta.transaction.TransactionManager;
import jakarta.transaction.TransactionalException;

import ch.dvbern.dvbstarter.infrastructure.errorhandling.AppFailureException;
import ch.dvbern.oss.commons.logging.mdc.CommonMDCField;
import ch.dvbern.oss.hemed.esc.api.shared.auth.TenantEnum;
import ch.dvbern.oss.hemed.esc.api.shared.auth.TenantContext;
import ch.dvbern.oss.hemed.esc.api.testutil.AbstractITest;
import ch.dvbern.oss.hemed.esc.api.testutil.S3ObjectProbe;
import ch.dvbern.oss.hemed.esc.api.testutil.TestUtil;
import ch.dvbern.oss.hemed.esc.api.testutil.logging.InMemoryLogHandler;
import ch.dvbern.oss.hemed.esc.api.testutil.security.TestAsAdminBS;

import io.quarkus.arc.Arc;
import io.quarkus.arc.ManagedContext;
import io.quarkus.narayana.jta.QuarkusTransaction;
import io.quarkus.test.junit.QuarkusTest;
import lombok.RequiredArgsConstructor;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.jboss.logmanager.ExtLogRecord;
import org.jboss.resteasy.reactive.multipart.FileUpload;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayNameGeneration;
import org.junit.jupiter.api.DisplayNameGenerator;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.slf4j.MDC;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;

/**
 * S3 follows the outcome of the database transaction: a file uploaded with
 * {@link FileStoreService#uploadToS3BoundToTransaction} disappears when the transaction does not commit, a delete
 * requested with {@link FileStoreService#deleteInS3AfterCommit} only happens when it does. Runs against the real
 * transaction manager and LocalStack; QuarkusTransaction plays the part of the @JaxResource transaction boundary.
 */
@QuarkusTest
@RequiredArgsConstructor
@DisplayNameGeneration(DisplayNameGenerator.ReplaceUnderscores.class)
class FileStoreServiceTransactionFileStoreTxBindingITest extends AbstractITest {

    private static final Pattern OPERATION_AND_TRANSACTION = Pattern.compile("op=(\\w+) tx=(\\S+)");

    private final FileStoreService fileStoreService;
    private final TenantContext tenantContext;
    private final TransactionManager transactionManager;
    private final S3ObjectProbe s3;

    private InMemoryLogHandler bindingLog;
    private InMemoryLogHandler outcomeLog;

    @Override
    @BeforeEach
    public void setUp() {
        super.setUp();
        bindingLog = InMemoryLogHandler.installFor(FileStoreService.class);
        outcomeLog = InMemoryLogHandler.installFor(FileStoreTransactionObserver.class);
    }

    @AfterEach
    void tearDown() {
        bindingLog.uninstall();
        outcomeLog.uninstall();
    }

    @Test
    @TestAsAdminBS
    void uploadBoundToTransaction_committed_should_keep_file() {
        // ACT
        String file = QuarkusTransaction.requiringNew().call(this::upload);

        // ASSERT
        assertThat(s3.exists(bucketBS(), file))
            .isTrue();
    }

    @Test
    @TestAsAdminBS
    void uploadBoundToTransaction_rolledBack_should_delete_file() {
        // ARRANGE
        AtomicReference<String> file = new AtomicReference<>();
        AtomicReference<Boolean> uploadedBeforeRollback = new AtomicReference<>();

        // ACT
        Throwable thrown = catchThrowable(() -> QuarkusTransaction.requiringNew().run(() -> {
            file.set(upload());
            uploadedBeforeRollback.set(s3.exists(bucketBS(), file.get()));
            throw new SimulatedFailure();
        }));

        // ASSERT
        assertThat(thrown)
            .isInstanceOf(SimulatedFailure.class);
        assertThat(uploadedBeforeRollback.get())
            .isTrue();
        assertThat(s3.exists(bucketBS(), file.get()))
            .isFalse();
    }

    @Test
    @TestAsAdminBS
    void uploadBoundToTransaction_markedRollbackOnlyAfterUpload_should_delete_file() {
        // ARRANGE
        // FileIngressService catches the AppValidationException of a nested bean and returns normally, but the nested
        // interceptor has already marked the transaction: it is rolled back without an exception reaching anyone
        AtomicReference<String> file = new AtomicReference<>();

        // ACT
        catchThrowable(() -> QuarkusTransaction.requiringNew().run(() -> {
            file.set(upload());
            QuarkusTransaction.setRollbackOnly();
        }));

        // ASSERT
        assertThat(s3.exists(bucketBS(), file.get()))
            .isFalse();
    }

    @Test
    @TestAsAdminBS
    void uploadBoundToTransaction_withoutTransaction_should_be_rejected() {
        // ARRANGE
        FileUpload fileUpload = testFile();
        FileInfo fileInfo = fileInfoFor(fileUpload);

        // ACT
        Throwable thrown = catchThrowable(() -> fileStoreService.uploadToS3BoundToTransaction(fileUpload, fileInfo));

        // ASSERT
        assertThat(thrown)
            .isInstanceOf(TransactionalException.class);
        assertThat(s3.bucketsContaining(fileInfo.getFileIdInBucket()))
            .isEmpty();
    }

    @Test
    @TestAsAdminBS
    void uploadBoundToTransaction_inTransactionMarkedRollbackOnly_should_be_rejected() {
        // ARRANGE
        FileUpload fileUpload = testFile();
        FileInfo fileInfo = fileInfoFor(fileUpload);
        AtomicReference<Throwable> thrown = new AtomicReference<>();

        // ACT
        catchThrowable(() -> QuarkusTransaction.requiringNew().run(() -> {
            QuarkusTransaction.setRollbackOnly();
            thrown.set(catchThrowable(() -> fileStoreService.uploadToS3BoundToTransaction(fileUpload, fileInfo)));
        }));

        // ASSERT
        assertThat(thrown.get())
            .isInstanceOf(AppFailureException.class);
        assertThat(s3.bucketsContaining(fileInfo.getFileIdInBucket()))
            .isEmpty();
    }

    @Test
    @TestAsAdminBS
    void uploadBoundToTransaction_innerRequiresNewCommitted_should_keep_file() {
        // ARRANGE
        // the upload belongs to the transaction active at upload time: a REQUIRES_NEW that committed its row keeps its
        // file, whatever happens to the transaction around it
        AtomicReference<String> file = new AtomicReference<>();

        // ACT
        Throwable thrown = catchThrowable(() -> QuarkusTransaction.requiringNew().run(() -> {
            file.set(QuarkusTransaction.requiringNew().call(this::upload));
            throw new SimulatedFailure();
        }));

        // ASSERT
        assertThat(thrown)
            .isInstanceOf(SimulatedFailure.class);
        assertThat(s3.exists(bucketBS(), file.get()))
            .isTrue();
    }

    @Test
    @TestAsAdminBS
    void uploadBoundToTransaction_timedOut_should_delete_file_on_reaper_thread_logging_the_request_context()
        throws InterruptedException {
        // ARRANGE
        // BE rather than the default BS: resolving the tenant in the observer would find none on the reaper thread
        tenantContext.setCurrentTenant(TenantEnum.BE);
        // what LoggingCommonMDCFieldsRequestFilter puts into the MDC for a request
        MDC.put(CommonMDCField.RequestSource.name(), "/api/v1/test/reaper");
        AtomicReference<String> file = new AtomicReference<>();

        // ACT
        Throwable thrown;
        try {
            thrown = catchThrowable(() -> QuarkusTransaction.requiringNew().timeout(1).run(() -> {
                file.set(upload());
                awaitRollbackByReaper();
            }));
        } finally {
            MDC.remove(CommonMDCField.RequestSource.name());
        }

        // ASSERT
        assertThat(thrown)
            .as("committing a transaction the reaper rolled back must fail")
            .isNotNull();
        s3.awaitGone(s3.bucketOf(TenantEnum.BE), file.get());
        ExtLogRecord cleanupLog = (ExtLogRecord) awaitOutcomeLog(file.get());
        assertThat(cleanupLog.getThreadName())
            .as("the cleanup must have run on the reaper thread, otherwise this test proves nothing")
            .startsWith("Transaction Reaper");
        assertThat(cleanupLog.getMdc(CommonMDCField.RequestSource.name()))
            .isEqualTo("/api/v1/test/reaper");
    }

    @Test
    @TestAsAdminBS
    void uploadBoundToTransaction_rolledBack_should_log_binding_and_cleanup_with_same_operation_and_transaction() {
        // ARRANGE
        AtomicReference<String> file = new AtomicReference<>();

        // ACT
        catchThrowable(() -> QuarkusTransaction.requiringNew().run(() -> {
            file.set(upload());
            throw new SimulatedFailure();
        }));

        // ASSERT
        String binding = logLineAbout(bindingLog, file.get(), "upload bound to the transaction");
        String cleanup = logLineAbout(outcomeLog, file.get(), "transaction rolled back, file deleted in S3");
        Matcher boundTo = OPERATION_AND_TRANSACTION.matcher(binding);
        assertThat(boundTo.find())
            .isTrue();
        assertThat(cleanup)
            .contains("op=" + boundTo.group(1) + " tx=" + boundTo.group(2))
            .contains("origin=" + getClass().getSimpleName() + ".upload");
    }

    @Test
    @TestAsAdminBS
    void deleteAfterCommit_committed_should_delete_file() {
        // ARRANGE
        String file = QuarkusTransaction.requiringNew().call(this::upload);
        AtomicReference<Boolean> stillThereBeforeCommit = new AtomicReference<>();

        // ACT
        QuarkusTransaction.requiringNew().run(() -> {
            fileStoreService.deleteInS3AfterCommit(file);
            stillThereBeforeCommit.set(s3.exists(bucketBS(), file));
        });

        // ASSERT
        assertThat(stillThereBeforeCommit.get())
            .isTrue();
        assertThat(s3.exists(bucketBS(), file))
            .isFalse();
    }

    @Test
    @TestAsAdminBS
    void deleteAfterCommit_rolledBack_should_keep_file() {
        // ARRANGE
        String file = QuarkusTransaction.requiringNew().call(this::upload);

        // ACT
        Throwable thrown = catchThrowable(() -> QuarkusTransaction.requiringNew().run(() -> {
            fileStoreService.deleteInS3AfterCommit(file);
            throw new SimulatedFailure();
        }));

        // ASSERT
        assertThat(thrown)
            .isInstanceOf(SimulatedFailure.class);
        assertThat(s3.exists(bucketBS(), file))
            .isTrue();
    }

    @Test
    @TestAsAdminBS
    void deleteAfterCommit_withoutTransaction_should_be_rejected() {
        // ARRANGE
        String file = QuarkusTransaction.requiringNew().call(this::upload);

        // ACT
        Throwable thrown = catchThrowable(() -> fileStoreService.deleteInS3AfterCommit(file));

        // ASSERT
        assertThat(thrown)
            .isInstanceOf(TransactionalException.class);
        assertThat(s3.exists(bucketBS(), file))
            .isTrue();
    }

    @Test
    @TestAsAdminBS
    void deleteAfterCommit_inTransactionMarkedRollbackOnly_should_be_rejected_and_keep_file() {
        // ARRANGE
        // without the guard in FileStoreService, ArC would call the observer right away because the transaction is
        // no longer ACTIVE: the file would be gone while the rollback keeps the row pointing to it
        String file = QuarkusTransaction.requiringNew().call(this::upload);
        AtomicReference<Throwable> thrown = new AtomicReference<>();

        // ACT
        catchThrowable(() -> QuarkusTransaction.requiringNew().run(() -> {
            QuarkusTransaction.setRollbackOnly();
            thrown.set(catchThrowable(() -> fileStoreService.deleteInS3AfterCommit(file)));
        }));

        // ASSERT
        assertThat(thrown.get())
            .isInstanceOf(AppFailureException.class);
        assertThat(s3.exists(bucketBS(), file))
            .isTrue();
    }

    @ParameterizedTest
    @EnumSource(value = TenantEnum.class, names = { "BS", "WIN", "BE" })
    @TestAsAdminBS
    void uploadBoundToTransaction_committed_should_land_only_in_bucket_of_tenant(TenantEnum tenant) {
        // ARRANGE
        tenantContext.setCurrentTenant(tenant);

        // ACT
        String file = QuarkusTransaction.requiringNew().call(this::upload);

        // ASSERT
        assertThat(s3.bucketsContaining(file))
            .containsExactly(s3.bucketOf(tenant));
    }

    @ParameterizedTest
    @EnumSource(value = TenantEnum.class, names = { "BS", "WIN", "BE" })
    @TestAsAdminBS
    void uploadBoundToTransaction_rolledBack_should_delete_file_in_bucket_of_tenant(TenantEnum tenant) {
        // ARRANGE
        tenantContext.setCurrentTenant(tenant);
        AtomicReference<String> file = new AtomicReference<>();
        AtomicReference<List<String>> bucketsBeforeRollback = new AtomicReference<>();

        // ACT
        catchThrowable(() -> QuarkusTransaction.requiringNew().run(() -> {
            file.set(upload());
            bucketsBeforeRollback.set(s3.bucketsContaining(file.get()));
            throw new SimulatedFailure();
        }));

        // ASSERT
        assertThat(bucketsBeforeRollback.get())
            .containsExactly(s3.bucketOf(tenant));
        assertThat(s3.bucketsContaining(file.get()))
            .isEmpty();
    }

    @ParameterizedTest
    @EnumSource(value = TenantEnum.class, names = { "BS", "WIN", "BE" })
    @TestAsAdminBS
    void deleteAfterCommit_committed_should_delete_file_in_bucket_of_tenant(TenantEnum tenant) {
        // ARRANGE
        tenantContext.setCurrentTenant(tenant);
        String file = QuarkusTransaction.requiringNew().call(this::upload);

        // ACT
        QuarkusTransaction.requiringNew().run(() -> fileStoreService.deleteInS3AfterCommit(file));

        // ASSERT
        assertThat(s3.bucketsContaining(file))
            .isEmpty();
    }

    @Test
    @TestAsAdminBS
    void uploadBoundToTransaction_tenantSwitchedInsideTransaction_should_delete_each_file_where_it_went() {
        // ARRANGE
        // ScheduledTaskService switches the tenant in a loop: the cleanup must go to the bucket of the upload, not to
        // the one of the tenant current at rollback
        AtomicReference<String> fileBS = new AtomicReference<>();
        AtomicReference<String> fileBE = new AtomicReference<>();
        AtomicReference<List<String>> bucketsOfBS = new AtomicReference<>();
        AtomicReference<List<String>> bucketsOfBE = new AtomicReference<>();

        // ACT
        catchThrowable(() -> QuarkusTransaction.requiringNew().run(() -> {
            tenantContext.setCurrentTenant(TenantEnum.BS);
            fileBS.set(upload());
            bucketsOfBS.set(s3.bucketsContaining(fileBS.get()));
            tenantContext.setCurrentTenant(TenantEnum.BE);
            fileBE.set(upload());
            bucketsOfBE.set(s3.bucketsContaining(fileBE.get()));
            throw new SimulatedFailure();
        }));

        // ASSERT
        assertThat(bucketsOfBS.get())
            .containsExactly(s3.bucketOf(TenantEnum.BS));
        assertThat(bucketsOfBE.get())
            .containsExactly(s3.bucketOf(TenantEnum.BE));
        assertThat(s3.bucketsContaining(fileBS.get()))
            .isEmpty();
        assertThat(s3.bucketsContaining(fileBE.get()))
            .isEmpty();
    }

    @Test
    @TestAsAdminBS
    void uploadBoundToTransaction_concurrentTransactionsOfDifferentTenants_should_not_affect_each_other()
        throws Exception {
        // ARRANGE
        CyclicBarrier bothInsideTheirTransaction = new CyclicBarrier(2);
        ExecutorService executor = Executors.newFixedThreadPool(2);
        try {
            // ACT
            Future<String> committedInBS = executor.submit(() -> inOwnRequestContext(TenantEnum.BS, () ->
                QuarkusTransaction.requiringNew().call(() -> {
                    String file = upload();
                    bothInsideTheirTransaction.await(30, TimeUnit.SECONDS);
                    return file;
                })));
            Future<String> rolledBackInBE = executor.submit(() -> inOwnRequestContext(TenantEnum.BE, () -> {
                AtomicReference<String> file = new AtomicReference<>();
                catchThrowable(() -> QuarkusTransaction.requiringNew().call(() -> {
                    file.set(upload());
                    bothInsideTheirTransaction.await(30, TimeUnit.SECONDS);
                    throw new SimulatedFailure();
                }));
                return file.get();
            }));
            String fileBS = committedInBS.get(60, TimeUnit.SECONDS);
            String fileBE = rolledBackInBE.get(60, TimeUnit.SECONDS);

            // ASSERT
            assertThat(s3.bucketsContaining(fileBS))
                .containsExactly(s3.bucketOf(TenantEnum.BS));
            assertThat(s3.bucketsContaining(fileBE))
                .isEmpty();
        } finally {
            executor.shutdownNow();
        }
    }

    @NonNull
    private String upload() {
        FileUpload fileUpload = testFile();
        FileInfo fileInfo = fileInfoFor(fileUpload);
        fileStoreService.uploadToS3BoundToTransaction(fileUpload, fileInfo);
        return fileInfo.getFileIdInBucket();
    }

    @NonNull
    private static FileUpload testFile() {
        return FileUploadWrapper.fromFile(TestUtil.loadTestPDF());
    }

    @NonNull
    private static FileInfo fileInfoFor(@NonNull FileUpload fileUpload) {
        return FileInfoS3Mapper.mapFileUploadToFileInfoWithMIMEDetect(fileUpload, FileTyp.NOTIZUPLOAD);
    }

    @NonNull
    private String bucketBS() {
        return s3.bucketOf(TenantEnum.BS);
    }

    /**
     * Stays inside the transaction until the reaper has rolled it back, which it does on its own thread.
     */
    private void awaitRollbackByReaper() {
        try {
            TestUtil.waitForConditionOrAssertFails(
                () -> transactionStatus() != Status.STATUS_ACTIVE,
                200,
                100,
                "the reaper did not roll back the transaction");
        } catch (InterruptedException e) {
            // the reaper may interrupt the thread owning the timed-out transaction, which is what we wait for anyway
            // the flag is cleared so that the assertions afterwards can still wait for the cleanup
            Thread.interrupted();
        }
    }

    private int transactionStatus() {
        try {
            return transactionManager.getStatus();
        } catch (SystemException e) {
            throw new IllegalStateException(e);
        }
    }

    /**
     * The delete and its log line happen on the reaper thread, a moment apart.
     */
    @NonNull
    private LogRecord awaitOutcomeLog(@NonNull String file) throws InterruptedException {
        TestUtil.waitForConditionOrAssertFails(
            () -> findOutcomeLog(file).isPresent(),
            200,
            100,
            "no cleanup log line for " + file);
        return findOutcomeLog(file).orElseThrow();
    }

    @NonNull
    private Optional<LogRecord> findOutcomeLog(@NonNull String file) {
        return outcomeLog.entries().stream()
            .filter(entry -> InMemoryLogHandler.formattedMessage(entry).contains(file))
            .findFirst();
    }

    @NonNull
    private static String logLineAbout(
        @NonNull InMemoryLogHandler log,
        @NonNull String file,
        @NonNull String text
    ) {
        return log.flatEntries().stream()
            .filter(line -> line.contains(file) && line.contains(text))
            .findFirst()
            .orElseThrow(() -> new AssertionError("no log line '" + text + "' for " + file));
    }

    /**
     * The services are request scoped, so a call from another thread needs a request context of its own - and the
     * tenant that goes with it.
     */
    private <T> T inOwnRequestContext(@NonNull TenantEnum tenant, @NonNull Callable<T> work) throws Exception {
        ManagedContext requestContext = Arc.container().requestContext();
        requestContext.activate();
        try {
            tenantContext.setCurrentTenant(tenant);
            return work.call();
        } finally {
            requestContext.terminate();
        }
    }

    private static final class SimulatedFailure extends RuntimeException {
        private static final long serialVersionUID = 1L;

        SimulatedFailure() {
            super("simulated failure after the file operation");
        }
    }
}
