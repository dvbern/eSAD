package ch.dvbern.oss.hemed.esc.api.features.fileingress;

import java.io.File;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.Month;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

import jakarta.persistence.OptimisticLockException;

import ch.dvbern.dvbstarter.shared.types.id.ID;
import ch.dvbern.oss.hemed.esc.api.features.anlass.einladung.Einladung;
import ch.dvbern.oss.hemed.esc.api.features.anlass.einladung.EinladungRepo;
import ch.dvbern.oss.hemed.esc.api.features.anlass.types.AnlassTyp;
import ch.dvbern.oss.hemed.esc.api.features.kinder.Dossier;
import ch.dvbern.oss.hemed.esc.api.features.notizen.Notiz;
import ch.dvbern.oss.hemed.esc.api.features.notizen.NotizTyp;
import ch.dvbern.oss.hemed.esc.api.shared.auth.TenantEnum;
import ch.dvbern.oss.hemed.esc.api.shared.fileio.FileData;
import ch.dvbern.oss.hemed.esc.api.shared.fileio.FileUploadWrapper;
import ch.dvbern.oss.hemed.esc.api.testutil.AbstractITest;
import ch.dvbern.oss.hemed.esc.api.testutil.FixtureService;
import ch.dvbern.oss.hemed.esc.api.testutil.S3ObjectProbe;
import ch.dvbern.oss.hemed.esc.api.testutil.TestUtil;
import ch.dvbern.oss.hemed.esc.api.testutil.security.TestAsFileIngressBS;
import io.quarkus.narayana.jta.QuarkusTransaction;
import io.quarkus.test.junit.QuarkusTest;
import lombok.RequiredArgsConstructor;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayNameGeneration;
import org.junit.jupiter.api.DisplayNameGenerator;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;

/**
 * File ingress when its transaction does not commit. In production the transaction boundary is the @JaxResource of
 * FileIngressResource; here QuarkusTransaction plays that part, so the test can look inside the transaction and let a
 * second push run while the first one is still open.
 */
@QuarkusTest
@RequiredArgsConstructor
@DisplayNameGeneration(DisplayNameGenerator.ReplaceUnderscores.class)
class FileIngressTransactionITest extends AbstractITest {

    private static final LocalDate GEBURTSDATUM = LocalDate.of(2025, 1, 1);
    private static final LocalDate DATE_EARLY = LocalDate.of(2025, 1, 1);

    private final FixtureService fixtureService;
    private final FileIngressService fileIngressService;
    private final EinladungRepo einladungRepo;
    private final S3ObjectProbe s3;

    @Override
    @BeforeEach
    public void setUp() {
        super.setUp();
        setTime(OffsetDateTime.of(LocalDateTime.of(2025, Month.NOVEMBER, 26, 0, 0, 0), ZoneOffset.UTC));
    }

    @TestAsFileIngressBS
    @Test
    @Timeout(value = 3, unit = TimeUnit.MINUTES)
    void processUploadNotiz_twoPushesForSameNotiz_should_leave_no_file_of_the_push_losing_the_optimistic_lock() {
        // ARRANGE
        String esadId = "A00145650";
        Einladung einladung = createEinladung(esadId);
        AtomicReference<String> fileOfLoser = new AtomicReference<>();
        AtomicReference<String> fileOfWinner = new AtomicReference<>();
        AtomicReference<Boolean> loserUploadedBeforeCommit = new AtomicReference<>();

        // ACT
        // Push A creates the Notiz and uploads its file. Before A commits, push B for the same Einladung runs start to
        // finish: it cannot see A's Notiz yet, creates its own and commits first. A then fails on the version of the
        // Einladung while committing, inside the transaction manager.
        Throwable thrown = catchThrowable(() -> QuarkusTransaction.requiringNew().run(() -> {
            push(esadId, einladung);
            fileOfLoser.set(onlyFileOfImpfeinverstaendnisNotiz(einladung.getId()));
            loserUploadedBeforeCommit.set(s3.exists(bucketBS(), fileOfLoser.get()));

            QuarkusTransaction.requiringNew().run(() -> {
                push(esadId, einladung);
                fileOfWinner.set(onlyFileOfImpfeinverstaendnisNotiz(einladung.getId()));
            });
        }));

        // ASSERT
        assertThat(thrown)
            .hasStackTraceContaining(OptimisticLockException.class.getName());
        assertThat(loserUploadedBeforeCommit.get())
            .isTrue();
        assertThat(s3.exists(bucketBS(), fileOfLoser.get()))
            .as("file of the losing push")
            .isFalse();
        assertThat(s3.exists(bucketBS(), fileOfWinner.get()))
            .as("file of the winning push")
            .isTrue();
        assertThat(filesOfImpfeinverstaendnisNotiz(einladung.getId()))
            .containsExactly(fileOfWinner.get());
    }

    @TestAsFileIngressBS
    @Test
    void processUploadNotiz_rolledBackWithoutException_should_delete_file() {
        // ARRANGE
        // When a bean inside the ingress throws an AppValidationException after the upload, its interceptor marks the
        // transaction for rollback; FileIngressService catches the exception and returns normally, and the
        // transaction is rolled back without an exception anywhere. Marking the transaction does the same.
        String esadId = "A00145651";
        Einladung einladung = createEinladung(esadId);
        AtomicReference<String> file = new AtomicReference<>();
        AtomicReference<Boolean> uploadedBeforeRollback = new AtomicReference<>();

        // ACT
        catchThrowable(() -> QuarkusTransaction.requiringNew().run(() -> {
            push(esadId, einladung);
            file.set(onlyFileOfImpfeinverstaendnisNotiz(einladung.getId()));
            uploadedBeforeRollback.set(s3.exists(bucketBS(), file.get()));
            QuarkusTransaction.setRollbackOnly();
        }));

        // ASSERT
        assertThat(uploadedBeforeRollback.get())
            .isTrue();
        assertThat(s3.exists(bucketBS(), file.get()))
            .isFalse();
        assertThat(fixtureService.readEinladungWithFilesAndLabels(einladung.getId()).getImpfeinverstaendnisNotiz())
            .isEmpty();
    }

    @NonNull
    private Einladung createEinladung(@NonNull String esadId) {
        Dossier dossier = fixtureService.createDossier(GEBURTSDATUM, esadId);
        return fixtureService.createEinladung(dossier, AnlassTyp.KLASSE_8, DATE_EARLY, "Anlass " + esadId);
    }

    private void push(@NonNull String esadId, @NonNull Einladung einladung) {
        String filename = GEBURTSDATUM.format(FileIngressService.DATE_TIME_FORMATTER) + "__" + esadId + ".pdf";
        File pdf = TestUtil.loadTestfileWithFilename("/FileUtilTest/pdfFileExample.pdf", filename);

        FileIngressResultJson result = fileIngressService.processUploadNotiz(
            esadId,
            einladung.getAnlass().getId(),
            NotizTyp.IMPFEINVERSTAENDNIS,
            FileUploadWrapper.fromFile(pdf));

        assertThat(result.getStatus())
            .as("push: %s", result.getErrors())
            .isEqualTo(FileIngressStatus.OK);
    }

    /**
     * Reads inside the running transaction. findById goes through em.find, which does not flush: a query here would
     * flush the pending update of the Einladung, lock its row, and the nested push would wait for it forever.
     */
    @NonNull
    private String onlyFileOfImpfeinverstaendnisNotiz(@NonNull ID<Einladung> einladungId) {
        Einladung einladung = einladungRepo.findById(einladungId).orElseThrow();
        List<FileData> files = einladung.getImpfeinverstaendnisNotiz().map(Notiz::getFiles).orElseThrow();
        assertThat(files)
            .hasSize(1);
        return files.getFirst().getFileInfoBucket().getFileIdInBucket();
    }

    @NonNull
    private List<String> filesOfImpfeinverstaendnisNotiz(@NonNull ID<Einladung> einladungId) {
        return fixtureService.readEinladungWithFilesAndLabels(einladungId)
            .getImpfeinverstaendnisNotiz()
            .map(notiz -> notiz.getFiles().stream()
                .map(fileData -> fileData.getFileInfoBucket().getFileIdInBucket())
                .toList())
            .orElse(List.of());
    }

    @NonNull
    private String bucketBS() {
        return s3.bucketOf(TenantEnum.BS);
    }
}
