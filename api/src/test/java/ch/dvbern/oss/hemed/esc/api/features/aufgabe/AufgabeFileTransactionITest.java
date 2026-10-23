package ch.dvbern.oss.hemed.esc.api.features.aufgabe;

import java.util.concurrent.atomic.AtomicReference;

import ch.dvbern.oss.hemed.esc.api.shared.aufgabe.Aufgabe;
import ch.dvbern.oss.hemed.esc.api.shared.aufgabe.AufgabeService;
import ch.dvbern.oss.hemed.esc.api.shared.auth.TenantEnum;
import ch.dvbern.oss.hemed.esc.api.shared.fileio.FileData;
import ch.dvbern.oss.hemed.esc.api.shared.fileio.FileUploadWrapper;
import ch.dvbern.oss.hemed.esc.api.testutil.AbstractITest;
import ch.dvbern.oss.hemed.esc.api.testutil.FixtureService;
import ch.dvbern.oss.hemed.esc.api.testutil.S3ObjectProbe;
import ch.dvbern.oss.hemed.esc.api.testutil.TestUtil;
import ch.dvbern.oss.hemed.esc.api.testutil.security.TestAsAdminBS;
import io.quarkus.narayana.jta.QuarkusTransaction;
import io.quarkus.test.junit.QuarkusTest;
import lombok.RequiredArgsConstructor;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.junit.jupiter.api.DisplayNameGeneration;
import org.junit.jupiter.api.DisplayNameGenerator;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;

/**
 * Aufgabe attachments when the transaction around the change does not commit.
 */
@QuarkusTest
@RequiredArgsConstructor
@DisplayNameGeneration(DisplayNameGenerator.ReplaceUnderscores.class)
class AufgabeFileTransactionITest extends AbstractITest {

    private final FixtureService fixtureService;
    private final AufgabeService aufgabeService;
    private final S3ObjectProbe s3;

    @Test
    @TestAsAdminBS
    void addUploadedFile_rolledBack_should_delete_file() {
        // ARRANGE
        Aufgabe aufgabe = fixtureService.createAufgabe();
        AtomicReference<String> file = new AtomicReference<>();

        // ACT
        Throwable thrown = catchThrowable(() -> QuarkusTransaction.requiringNew().run(() -> {
            Aufgabe updated = aufgabeService.addUploadedFile(
                aufgabe.getId(),
                FileUploadWrapper.fromFile(TestUtil.loadTestPDF()));
            file.set(updated.getFiles().getLast().getFileInfoBucket().getFileIdInBucket());
            throw new SimulatedFailure();
        }));

        // ASSERT
        assertThat(thrown)
            .isInstanceOf(SimulatedFailure.class);
        assertThat(fixtureService.readFilesOfAufgabe(aufgabe.getId()))
            .isEmpty();
        assertThat(s3.exists(bucketBS(), file.get()))
            .isFalse();
    }

    @Test
    @TestAsAdminBS
    void removeUploadedFile_rolledBack_should_keep_file() {
        // ARRANGE
        Aufgabe aufgabe = fixtureService.createAufgabe();
        FileData fileData = fixtureService.uploadFileToAufgabe(aufgabe.getId());

        // ACT
        Throwable thrown = catchThrowable(() -> QuarkusTransaction.requiringNew().run(() -> {
            aufgabeService.removeUploadedFile(aufgabeService.getById(aufgabe.getId()), fileData.getId(), noCheck -> {
            });
            throw new SimulatedFailure();
        }));

        // ASSERT
        assertThat(thrown)
            .isInstanceOf(SimulatedFailure.class);
        assertThat(fixtureService.readFilesOfAufgabe(aufgabe.getId()))
            .containsExactly(fileData);
        assertThat(s3.exists(bucketBS(), fileOf(fileData)))
            .isTrue();
    }

    @Test
    @TestAsAdminBS
    void removeUploadedFile_committed_should_delete_file() {
        // ARRANGE
        Aufgabe aufgabe = fixtureService.createAufgabe();
        FileData fileData = fixtureService.uploadFileToAufgabe(aufgabe.getId());

        // ACT
        QuarkusTransaction.requiringNew().run(() ->
            aufgabeService.removeUploadedFile(aufgabeService.getById(aufgabe.getId()), fileData.getId(), noCheck -> {
            }));

        // ASSERT
        assertThat(fixtureService.readFilesOfAufgabe(aufgabe.getId()))
            .isEmpty();
        assertThat(s3.exists(bucketBS(), fileOf(fileData)))
            .isFalse();
    }

    @Test
    @TestAsAdminBS
    void remove_rolledBack_should_keep_files() {
        // ARRANGE
        Aufgabe aufgabe = fixtureService.createAufgabe();
        FileData fileData = fixtureService.uploadFileToAufgabe(aufgabe.getId());

        // ACT
        Throwable thrown = catchThrowable(() -> QuarkusTransaction.requiringNew().run(() -> {
            aufgabeService.remove(aufgabe.getId());
            throw new SimulatedFailure();
        }));

        // ASSERT
        assertThat(thrown)
            .isInstanceOf(SimulatedFailure.class);
        assertThat(fixtureService.readFilesOfAufgabe(aufgabe.getId()))
            .containsExactly(fileData);
        assertThat(s3.exists(bucketBS(), fileOf(fileData)))
            .isTrue();
    }

    @NonNull
    private static String fileOf(@NonNull FileData fileData) {
        return fileData.getFileInfoBucket().getFileIdInBucket();
    }

    @NonNull
    private String bucketBS() {
        return s3.bucketOf(TenantEnum.BS);
    }

    private static final class SimulatedFailure extends RuntimeException {
        private static final long serialVersionUID = 1L;

        SimulatedFailure() {
            super("simulated failure after the file change");
        }
    }
}
