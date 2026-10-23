package ch.dvbern.oss.hemed.esc.api.shared.fileio;

import java.time.Duration;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

import ch.dvbern.oss.hemed.esc.api.shared.fileio.FileStoreTransactionObserver.FileStoreTxBinding;
import ch.dvbern.oss.hemed.esc.api.shared.fileio.FileStoreTransactionObserver.DeletionAfterCommit;
import ch.dvbern.oss.hemed.esc.api.shared.fileio.FileStoreTransactionObserver.UploadedInTransaction;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayNameGeneration;
import org.junit.jupiter.api.DisplayNameGenerator;
import org.junit.jupiter.api.Test;
import org.slf4j.MDC;
import software.amazon.awssdk.services.s3.S3AsyncClient;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;
import software.amazon.awssdk.services.s3.model.DeleteObjectResponse;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@DisplayNameGeneration(DisplayNameGenerator.ReplaceUnderscores.class)
class FileStoreTransactionObserverTest {

    private final FileStoreTransactionObserver observer = new FileStoreTransactionObserver();

    @AfterEach
    void tearDown() {
        MDC.clear();
        Thread.interrupted(); // clear any interrupted status
    }

    @Test
    void deleteUploadOfFailedTransaction_should_delete_file_in_s3_and_restore_mdc() {
        // ARRANGE
        MDC.put("callerKey", "callerVal");
        S3AsyncClient s3 = mock(S3AsyncClient.class);
        DeleteObjectRequest deleteRequest = DeleteObjectRequest.builder()
            .bucket("test-bucket")
            .key("test-key")
            .build();
        when(s3.deleteObject(deleteRequest))
            .thenReturn(CompletableFuture.completedFuture(DeleteObjectResponse.builder().build()));

        FileStoreTxBinding fileStoreTxBinding = new FileStoreTxBinding(
            "op123456",
            "tx123456",
            "TestService.test",
            s3,
            deleteRequest,
            Duration.ofSeconds(5),
            Map.of("txKey", "txVal")
        );

        // ACT
        observer.deleteUploadOfFailedTransaction(new UploadedInTransaction(fileStoreTxBinding));

        // ASSERT
        verify(s3)
            .deleteObject(deleteRequest);
        assertThat(MDC.get("callerKey"))
            .isEqualTo("callerVal");
        assertThat(MDC.get("txKey"))
            .isNull();
    }

    @Test
    void deleteFileOfCommittedTransaction_should_delete_file_in_s3() {
        // ARRANGE
        S3AsyncClient s3 = mock(S3AsyncClient.class);
        DeleteObjectRequest deleteRequest = DeleteObjectRequest.builder()
            .bucket("test-bucket")
            .key("test-key")
            .build();
        when(s3.deleteObject(deleteRequest))
            .thenReturn(CompletableFuture.completedFuture(DeleteObjectResponse.builder().build()));

        FileStoreTxBinding fileStoreTxBinding = new FileStoreTxBinding(
            "op123456",
            "tx123456",
            "TestService.test",
            s3,
            deleteRequest,
            Duration.ofSeconds(5),
            Map.of()
        );

        // ACT
        observer.deleteFileOfCommittedTransaction(new DeletionAfterCommit(fileStoreTxBinding));

        // ASSERT
        verify(s3)
            .deleteObject(deleteRequest);
    }

    @Test
    void delete_when_timeout_occurs_should_catch_and_not_throw_and_restore_mdc() {
        // ARRANGE
        MDC.put("callerKey", "callerVal");
        S3AsyncClient s3 = mock(S3AsyncClient.class);
        DeleteObjectRequest deleteRequest = DeleteObjectRequest.builder()
            .bucket("test-bucket")
            .key("test-key")
            .build();
        // A future that never completes, will time out after 50ms
        CompletableFuture<DeleteObjectResponse> neverCompletes = new CompletableFuture<>();
        when(s3.deleteObject(deleteRequest))
            .thenReturn(neverCompletes);

        FileStoreTxBinding fileStoreTxBinding = new FileStoreTxBinding(
            "op123456",
            "tx123456",
            "TestService.test",
            s3,
            deleteRequest,
            Duration.ofMillis(50),
            Map.of("txKey", "txVal")
        );

        // ACT & ASSERT
        assertThatCode(() -> observer.deleteUploadOfFailedTransaction(new UploadedInTransaction(fileStoreTxBinding)))
            .doesNotThrowAnyException();

        assertThat(MDC.get("callerKey"))
            .isEqualTo("callerVal");
        assertThat(MDC.get("txKey"))
            .isNull();
    }

    @Test
    void delete_when_interrupted_should_restore_interrupt_flag_and_not_throw() {
        // ARRANGE
        S3AsyncClient s3 = mock(S3AsyncClient.class);
        DeleteObjectRequest deleteRequest = DeleteObjectRequest.builder()
            .bucket("test-bucket")
            .key("test-key")
            .build();
        CompletableFuture<DeleteObjectResponse> interruptedFuture = new CompletableFuture<>() {
            @Override
            public DeleteObjectResponse get(long timeout, TimeUnit unit) throws InterruptedException {
                throw new InterruptedException("simulated interrupt");
            }
        };
        when(s3.deleteObject(deleteRequest))
            .thenReturn(interruptedFuture);

        FileStoreTxBinding fileStoreTxBinding = new FileStoreTxBinding(
            "op123456",
            "tx123456",
            "TestService.test",
            s3,
            deleteRequest,
            Duration.ofSeconds(5),
            Map.of()
        );

        // ACT
        observer.deleteUploadOfFailedTransaction(new UploadedInTransaction(fileStoreTxBinding));

        // ASSERT
        assertThat(Thread.currentThread().isInterrupted())
            .isTrue();
    }
}
