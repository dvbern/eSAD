package ch.dvbern.oss.hemed.esc.api.shared.fileio;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.concurrent.atomic.AtomicReference;

import ch.dvbern.oss.hemed.esc.api.shared.auth.TenantEnum;
import ch.dvbern.oss.hemed.esc.api.shared.printer.PrintResultDTO;
import ch.dvbern.oss.hemed.esc.api.testutil.AbstractITest;
import ch.dvbern.oss.hemed.esc.api.testutil.FixtureService;
import ch.dvbern.oss.hemed.esc.api.testutil.S3ObjectProbe;
import ch.dvbern.oss.hemed.esc.api.testutil.TestUtil;
import ch.dvbern.oss.hemed.esc.api.testutil.security.TestAsAdminBS;
import io.quarkus.narayana.jta.QuarkusTransaction;
import io.quarkus.test.junit.QuarkusTest;
import lombok.RequiredArgsConstructor;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.jboss.resteasy.reactive.multipart.FileUpload;
import org.junit.jupiter.api.DisplayNameGeneration;
import org.junit.jupiter.api.DisplayNameGenerator;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;

/**
 * Generated files (Kinderdossier exports of the Archivierung and the Duplikat merge) are uploaded from memory; they
 * follow the transaction like user uploads do.
 */
@QuarkusTest
@RequiredArgsConstructor
@DisplayNameGeneration(DisplayNameGenerator.ReplaceUnderscores.class)
class FileDataServiceITest extends AbstractITest {

    private final FixtureService fixtureService;
    private final FileDataService fileDataService;
    private final S3ObjectProbe s3;

    @Test
    @TestAsAdminBS
    void createFileData_committed_should_keep_file() {
        // ACT
        FileData fileData = fixtureService.createFileData(TestUtil.loadTestPDF(), "Export");

        // ASSERT
        assertThat(s3.exists(bucketBS(), fileData.getFileInfoBucket().getFileIdInBucket()))
            .isTrue();
    }

    @Test
    @TestAsAdminBS
    void createFileData_rolledBack_should_delete_file() throws IOException {
        // ARRANGE
        PrintResultDTO export = printResultOf(TestUtil.loadTestPDF());
        AtomicReference<String> file = new AtomicReference<>();

        // ACT
        Throwable thrown = catchThrowable(() -> QuarkusTransaction.requiringNew().run(() -> {
            FileData fileData = fileDataService.createFileData(export, FileTyp.KINDERDOSSIER, "Export");
            file.set(fileData.getFileInfoBucket().getFileIdInBucket());
            throw new SimulatedFailure();
        }));

        // ASSERT
        assertThat(thrown)
            .isInstanceOf(SimulatedFailure.class);
        assertThat(s3.exists(bucketBS(), file.get()))
            .isFalse();
    }

    @NonNull
    private static PrintResultDTO printResultOf(@NonNull File file) throws IOException {
        FileUpload fileUpload = FileUploadWrapper.fromFile(file);
        return new PrintResultDTO(
            Files.readAllBytes(fileUpload.uploadedFile()),
            FileInfoS3Mapper.mapFileUploadToFileInfoWithMIMEDetect(fileUpload, FileTyp.KINDERDOSSIER));
    }

    @NonNull
    private String bucketBS() {
        return s3.bucketOf(TenantEnum.BS);
    }

    private static final class SimulatedFailure extends RuntimeException {
        private static final long serialVersionUID = 1L;

        SimulatedFailure() {
            super("simulated failure after the upload");
        }
    }
}
