package ch.dvbern.oss.hemed.esc.api.shared.fileio;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.time.Duration;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import ch.dvbern.oss.hemed.esc.api.testutil.AbstractITest;
import ch.dvbern.oss.hemed.esc.api.testutil.TestUtil;
import ch.dvbern.oss.hemed.esc.api.testutil.security.TestAsAdminBS;
import io.quarkus.test.junit.QuarkusTest;
import io.vertx.mutiny.core.buffer.Buffer;
import lombok.RequiredArgsConstructor;
import org.jboss.resteasy.reactive.multipart.FileUpload;
import org.junit.jupiter.api.DisplayNameGeneration;
import org.junit.jupiter.api.DisplayNameGenerator;
import org.junit.jupiter.api.Test;
import software.amazon.awssdk.core.ResponseBytes;
import software.amazon.awssdk.core.async.ResponsePublisher;
import software.amazon.awssdk.services.s3.model.DeleteObjectResponse;
import software.amazon.awssdk.services.s3.model.GetObjectResponse;
import software.amazon.awssdk.services.s3.model.PutObjectResponse;

import static org.assertj.core.api.Assertions.assertThat;

@QuarkusTest
@RequiredArgsConstructor
@DisplayNameGeneration(DisplayNameGenerator.ReplaceUnderscores.class)
class FileStoreServiceTest extends AbstractITest {

    private static final Duration TIMEOUT = Duration.ofSeconds(5);

    private final FileStoreService fileStoreService;

    @Test
    @TestAsAdminBS
    void testUploadAndDeleteFile() throws IOException {
        FileUpload fileUpload = TestUtil.fileToFileUpload(TestUtil.loadTestSimpleEscadaFile(), "test-file.txt");
        FileInfo fileInfo = FileInfoS3Mapper.mapFileUploadToFileInfoWithMIMEDetect(fileUpload, FileTyp.ESCADA_SYNC);
        String fileIdInBucket = fileInfo.getFileIdInBucket();
        byte[] content = Files.readAllBytes(fileUpload.filePath());
        PutObjectResponse putObjectResponse =
            fileStoreService.uploadToS3Async(fileInfo.getMediaType().toString(), content, fileInfo)
                .await()
                .atMost(TIMEOUT);

        assertThat(putObjectResponse)
            .isNotNull();
        assertThat(putObjectResponse.eTag())
            .isNotNull();

        DeleteObjectResponse deleteObjectResponse =
            fileStoreService.deleteInS3Async(fileIdInBucket)
                .await()
                .atMost(TIMEOUT);
        assertThat(deleteObjectResponse)
            .isNotNull();
    }

    @Test
    @TestAsAdminBS
    void getFileAsync() throws ExecutionException, InterruptedException, IOException {
        // ARRANGE
        FileUpload fileUpload = TestUtil.fileToFileUpload(TestUtil.loadTestSimpleEscadaFile(), "test-file.csv");
        FileInfo fileInfo = FileInfoS3Mapper.mapFileUploadToFileInfoWithMIMEDetect(fileUpload, FileTyp.ESCADA_SYNC);
        String fileIdInBucket = fileInfo.getFileIdInBucket();
        byte[] originalBytes = Files.readAllBytes(fileUpload.filePath());
        fileStoreService.uploadToS3Async(fileInfo.getMediaType().toString(), originalBytes, fileInfo)
            .await()
            .atMost(TIMEOUT);

        // ACT
        // await the completion stage
        ResponsePublisher<GetObjectResponse> responsePublisher =
            fileStoreService.getFileAsync(fileIdInBucket).toCompletableFuture().get();
        assertThat(responsePublisher)
            .isNotNull();
        assertThat(responsePublisher.response().eTag())
            .isNotNull();
        assertThat(responsePublisher.response().contentType())
            .as("Tika detects csv as text/plain since there are no magic bytes in a csv")
            .isEqualTo("text/plain");

        responsePublisher.subscribe(byteBuffer -> {
            // Use the byteBuffer
            byte[] dst = new byte[byteBuffer.remaining()];
            byteBuffer.get(dst);
            assertThat(dst)
                .isNullOrEmpty();
            assertThat(dst)
                .hasSameSizeAs(originalBytes);
            assertThat(dst)
                .isEqualTo(originalBytes);
            System.out.println("Received bytes: " + new String(dst, StandardCharsets.UTF_8));
        });
    }

    @Test
    @TestAsAdminBS
    void getFileBytesAsync() throws IOException, ExecutionException, InterruptedException, TimeoutException {
        // ARRANGE
        FileUpload fileUpload = TestUtil.fileToFileUpload(TestUtil.loadTestSimpleEscadaFile(), "test-file.csv");
        FileInfo fileInfo = FileInfoS3Mapper.mapFileUploadToFileInfoWithMIMEDetect(fileUpload, FileTyp.ESCADA_SYNC);
        String fileIdInBucket = fileInfo.getFileIdInBucket();
        byte[] originalBytes = Files.readAllBytes(fileUpload.filePath());
        fileStoreService.uploadToS3Async(fileInfo.getMediaType().toString(), originalBytes, fileInfo)
            .await()
            .atMost(TIMEOUT);

        // ACT
        ResponseBytes<GetObjectResponse> response =
            fileStoreService.getFileBytesAsync(fileIdInBucket).get(10, TimeUnit.SECONDS);
        var loadedBytes = response.asByteArray();

        // ASSERT
        assertThat(loadedBytes)
            .isNotEmpty()
            .hasSameSizeAs(originalBytes)
            .isEqualTo(originalBytes);
    }

    @Test
    @TestAsAdminBS
    void testBufferConversion() {
        // ARRANGE
        String testString = "Hello, World!";
        byte[] testBytes = testString.getBytes(StandardCharsets.UTF_8);
        // ACT
        Buffer buffer = FileStoreService.toBuffer(ByteBuffer.wrap(testBytes));
        // ASSERT
        assertThat(buffer.toString())
            .hasToString(testString);
    }
}
