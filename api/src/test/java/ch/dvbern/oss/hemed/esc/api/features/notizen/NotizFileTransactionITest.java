package ch.dvbern.oss.hemed.esc.api.features.notizen;

import java.util.concurrent.atomic.AtomicReference;

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
 * Notiz attachments when the transaction around the change does not commit. The delete direction matters most: a file
 * deleted in S3 while its row survives the rollback is an attachment that can no longer be downloaded.
 */
@QuarkusTest
@RequiredArgsConstructor
@DisplayNameGeneration(DisplayNameGenerator.ReplaceUnderscores.class)
class NotizFileTransactionITest extends AbstractITest {

    private final FixtureService fixtureService;
    private final NotizService notizService;
    private final S3ObjectProbe s3;

    @Test
    @TestAsAdminBS
    void addUploadedFile_rolledBack_should_delete_file() {
        // ARRANGE
        Notiz notiz = fixtureService.createNotiz();
        AtomicReference<String> file = new AtomicReference<>();

        // ACT
        Throwable thrown = catchThrowable(() -> QuarkusTransaction.requiringNew().run(() -> {
            Notiz updated = notizService.addUploadedFileAndFlush(
                notiz.getId(),
                FileUploadWrapper.fromFile(TestUtil.loadTestPDF()));
            file.set(updated.getFiles().getLast().getFileInfoBucket().getFileIdInBucket());
            throw new SimulatedFailure();
        }));

        // ASSERT
        assertThat(thrown)
            .isInstanceOf(SimulatedFailure.class);
        assertThat(fixtureService.readFilesOfNotiz(notiz.getId()))
            .isEmpty();
        assertThat(s3.exists(bucketBS(), file.get()))
            .isFalse();
    }

    @Test
    @TestAsAdminBS
    void removeUploadedFile_rolledBack_should_keep_file() {
        // ARRANGE
        Notiz notiz = fixtureService.createNotiz();
        FileData fileData = fixtureService.uploadFileToNotiz(notiz.getId());

        // ACT
        Throwable thrown = catchThrowable(() -> QuarkusTransaction.requiringNew().run(() -> {
            notizService.removeUploadedFile(notizService.getById(notiz.getId()), fileData.getId());
            throw new SimulatedFailure();
        }));

        // ASSERT
        assertThat(thrown)
            .isInstanceOf(SimulatedFailure.class);
        assertThat(fixtureService.readFilesOfNotiz(notiz.getId()))
            .containsExactly(fileData);
        assertThat(s3.exists(bucketBS(), fileOf(fileData)))
            .as("the Notiz still lists the file, so it must still be downloadable")
            .isTrue();
    }

    @Test
    @TestAsAdminBS
    void removeUploadedFile_committed_should_delete_file() {
        // ARRANGE
        Notiz notiz = fixtureService.createNotiz();
        FileData fileData = fixtureService.uploadFileToNotiz(notiz.getId());

        // ACT
        QuarkusTransaction.requiringNew().run(() ->
            notizService.removeUploadedFile(notizService.getById(notiz.getId()), fileData.getId()));

        // ASSERT
        assertThat(fixtureService.readFilesOfNotiz(notiz.getId()))
            .isEmpty();
        assertThat(s3.exists(bucketBS(), fileOf(fileData)))
            .isFalse();
    }

    @Test
    @TestAsAdminBS
    void remove_rolledBack_should_keep_files() {
        // ARRANGE
        // the path of a Kind deletion: RemoveKindService removes the Notizen first and fails later
        Notiz notiz = fixtureService.createNotiz();
        FileData fileData = fixtureService.uploadFileToNotiz(notiz.getId());

        // ACT
        Throwable thrown = catchThrowable(() -> QuarkusTransaction.requiringNew().run(() -> {
            notizService.remove(notiz.getId(), true);
            throw new SimulatedFailure();
        }));

        // ASSERT
        assertThat(thrown)
            .isInstanceOf(SimulatedFailure.class);
        assertThat(fixtureService.readFilesOfNotiz(notiz.getId()))
            .containsExactly(fileData);
        assertThat(s3.exists(bucketBS(), fileOf(fileData)))
            .isTrue();
    }

    @Test
    @TestAsAdminBS
    void remove_committed_should_delete_files() {
        // ARRANGE
        Notiz notiz = fixtureService.createNotiz();
        FileData fileData = fixtureService.uploadFileToNotiz(notiz.getId());

        // ACT
        QuarkusTransaction.requiringNew().run(() -> notizService.remove(notiz.getId(), true));

        // ASSERT
        assertThat(notizService.findById(notiz.getId()))
            .isEmpty();
        assertThat(s3.exists(bucketBS(), fileOf(fileData)))
            .isFalse();
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
