package ch.dvbern.oss.hemed.esc.api.shared.scheduler;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Stream;

import ch.dvbern.oss.hemed.esc.api.features.benutzer.BenutzerRepo;
import ch.dvbern.oss.hemed.esc.api.features.kinder.Kind;
import ch.dvbern.oss.hemed.esc.api.features.kinder.KindService;
import ch.dvbern.oss.hemed.esc.api.features.kinder.duplikate.Duplikat;
import ch.dvbern.oss.hemed.esc.api.features.kinder.duplikate.DuplikatRepo;
import ch.dvbern.oss.hemed.esc.api.features.kinder.duplikate.DuplikatSearchTerm;
import ch.dvbern.oss.hemed.esc.api.shared.types.EntityAktivStatus;
import ch.dvbern.oss.hemed.esc.api.features.spss.SpssReportType;
import ch.dvbern.oss.hemed.esc.api.shared.benutzer.Benutzer;
import ch.dvbern.oss.hemed.esc.api.shared.documentqueue.DocumentQueue;
import ch.dvbern.oss.hemed.esc.api.shared.documentqueue.DocumentQueueRepo;
import ch.dvbern.oss.hemed.esc.api.shared.documentqueue.DocumentQueueStatus;
import ch.dvbern.oss.hemed.esc.api.shared.fileio.FileData;
import ch.dvbern.oss.hemed.esc.api.shared.fileio.FileDataRepo;
import ch.dvbern.oss.hemed.esc.api.shared.fileio.FileTyp;
import ch.dvbern.oss.hemed.esc.api.shared.person.Geschlecht;
import ch.dvbern.oss.hemed.esc.api.testutil.AbstractITest;
import ch.dvbern.oss.hemed.esc.api.testutil.FixtureService;
import ch.dvbern.oss.hemed.esc.api.testutil.builder.BenutzerBuilder;
import ch.dvbern.oss.hemed.esc.api.testutil.builder.DocumentQueueBuilder;
import ch.dvbern.oss.hemed.esc.api.testutil.builder.FileDataBuilder;
import ch.dvbern.oss.hemed.esc.api.testutil.builder.KindBuilder;
import ch.dvbern.oss.hemed.esc.api.testutil.security.TestAsAdminBS;
import io.quarkus.test.junit.QuarkusTest;
import lombok.RequiredArgsConstructor;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayNameGeneration;
import org.junit.jupiter.api.DisplayNameGenerator.ReplaceUnderscores;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import static org.assertj.core.api.Assertions.assertThat;

@QuarkusTest
@RequiredArgsConstructor
@DisplayNameGeneration(ReplaceUnderscores.class)
class SuperAdminRunnerServiceTest extends AbstractITest {

    private final SuperAdminRunnerService superAdminRunnerService;
    private final BenutzerRepo benutzerRepo;
    private final FixtureService fixtureService;
    private final KindService kindService;
    private final FileDataRepo fileDataRepo;
    private final DocumentQueueRepo documentQueueRepo;
    private final DuplikatRepo duplikatRepo;

    @Override
    @BeforeEach
    public void setUp() {
        super.setUp();
    }

    @Test
    @TestAsAdminBS
    void runBenutzerInaktivierenJob_afterProduction() {
        // ARRANGE
        setTime(OffsetDateTime.parse("2026-01-01T00:00:00.999999999+00:00"));
        Benutzer benutzerRecentlyLoggedIn = BenutzerBuilder.create(
            builder -> builder
                .withLastUsedSessionTimestamp(getAppClock().offsetDateTime().now())
        );
        Benutzer benutzer1MonthSinceLoggedIn = BenutzerBuilder.create(
            builder -> builder
                .withLastUsedSessionTimestamp(getAppClock().offsetDateTime().now().minusMonths(1))
        );
        Benutzer benutzer2MonthsSinceLoggedIn = BenutzerBuilder.create(
            builder -> builder
                .withLastUsedSessionTimestamp(getAppClock().offsetDateTime().now().minusMonths(2))
        );
        Benutzer benutzer6MonthsSinceLoggedInButManuallyActivated = BenutzerBuilder.create(
            builder -> builder
                .withLastUsedSessionTimestamp(getAppClock().offsetDateTime().now().minusMonths(6))
                .withLastManuallyActivatedTimestamp(getAppClock().offsetDateTime().now().minusDays(2))
        );
        Benutzer benutzerNeverLoggedIn = BenutzerBuilder.create(
            builder -> builder
                .withLastUsedSessionTimestamp(null)
        );
        benutzerRepo.persist(benutzerRecentlyLoggedIn);
        benutzerRepo.persist(benutzer1MonthSinceLoggedIn);
        benutzerRepo.persist(benutzer2MonthsSinceLoggedIn);
        benutzerRepo.persist(benutzer6MonthsSinceLoggedInButManuallyActivated);
        benutzerRepo.persist(benutzerNeverLoggedIn);
        // ACT
        superAdminRunnerService.runBenutzerInaktivierenJob(true);
        // ASSERT
        Benutzer benutzerRecentlyLoggedInAfter = benutzerRepo.findById(benutzerRecentlyLoggedIn.getId()).orElseThrow();
        Benutzer benutzer1MonthSinceLoggedInAfter =
            benutzerRepo.findById(benutzer1MonthSinceLoggedIn.getId()).orElseThrow();
        Benutzer benutzer2MonthsSinceLoggedInAfter =
            benutzerRepo.findById(benutzer2MonthsSinceLoggedIn.getId()).orElseThrow();
        Benutzer benutzer6MonthsSinceLoggedInButActivatedManuallyAfter =
            benutzerRepo.findById(benutzer6MonthsSinceLoggedInButManuallyActivated.getId()).orElseThrow();
        Benutzer benutzerNeverLoggedInAfter = benutzerRepo.findById(benutzerNeverLoggedIn.getId()).orElseThrow();
        assertThat(benutzerRecentlyLoggedInAfter.isAktiv()).isTrue();
        assertThat(benutzer1MonthSinceLoggedInAfter.isAktiv()).isTrue();
        assertThat(benutzer2MonthsSinceLoggedInAfter.isAktiv()).isFalse();
        assertThat(benutzer6MonthsSinceLoggedInButActivatedManuallyAfter.isAktiv()).isTrue();
        assertThat(benutzerNeverLoggedInAfter.isAktiv()).isFalse();
    }

    @Test
    @TestAsAdminBS
    void runBenutzerInaktivierenJob_beforeProduction() {
        // ARRANGE
        setTime(OffsetDateTime.parse("2024-01-01T00:00:00.999999999+00:00")); // Vor Produktivsetzung
        Benutzer benutzerRecentlyLoggedIn = BenutzerBuilder.create(
            builder -> builder
                .withLastUsedSessionTimestamp(getAppClock().offsetDateTime().now())
        );
        Benutzer benutzer1MonthSinceLoggedIn = BenutzerBuilder.create(
            builder -> builder
                .withLastUsedSessionTimestamp(getAppClock().offsetDateTime().now().minusMonths(1))
        );
        Benutzer benutzer2MonthsSinceLoggedIn = BenutzerBuilder.create(
            builder -> builder
                .withLastUsedSessionTimestamp(getAppClock().offsetDateTime().now().minusMonths(2))
        );
        Benutzer benutzer6MonthsSinceLoggedInButManuallyActivated = BenutzerBuilder.create(
            builder -> builder
                .withLastUsedSessionTimestamp(getAppClock().offsetDateTime().now().minusMonths(6))
                .withLastManuallyActivatedTimestamp(getAppClock().offsetDateTime().now().minusDays(2))
        );
        Benutzer benutzerNeverLoggedIn = BenutzerBuilder.create(
            builder -> builder
                .withLastUsedSessionTimestamp(null)
        );
        benutzerRepo.persist(benutzerRecentlyLoggedIn);
        benutzerRepo.persist(benutzer1MonthSinceLoggedIn);
        benutzerRepo.persist(benutzer2MonthsSinceLoggedIn);
        benutzerRepo.persist(benutzer6MonthsSinceLoggedInButManuallyActivated);
        benutzerRepo.persist(benutzerNeverLoggedIn);
        // ACT
        superAdminRunnerService.runBenutzerInaktivierenJob(true);
        // ASSERT
        Benutzer benutzerRecentlyLoggedInAfter = benutzerRepo.findById(benutzerRecentlyLoggedIn.getId()).orElseThrow();
        Benutzer benutzer1MonthSinceLoggedInAfter =
            benutzerRepo.findById(benutzer1MonthSinceLoggedIn.getId()).orElseThrow();
        Benutzer benutzer2MonthsSinceLoggedInAfter =
            benutzerRepo.findById(benutzer2MonthsSinceLoggedIn.getId()).orElseThrow();
        Benutzer benutzer6MonthsSinceLoggedInAfter =
            benutzerRepo.findById(benutzer6MonthsSinceLoggedInButManuallyActivated.getId()).orElseThrow();
        Benutzer benutzerNeverLoggedInAfter = benutzerRepo.findById(benutzerNeverLoggedIn.getId()).orElseThrow();
        // Alle bleiben aktiv, da vor Produktivsetzung
        assertThat(benutzerRecentlyLoggedInAfter.isAktiv()).isTrue();
        assertThat(benutzer1MonthSinceLoggedInAfter.isAktiv()).isTrue();
        assertThat(benutzer2MonthsSinceLoggedInAfter.isAktiv()).isTrue();
        assertThat(benutzer6MonthsSinceLoggedInAfter.isAktiv()).isTrue();
        assertThat(benutzerNeverLoggedInAfter.isAktiv()).isTrue();
    }

    @Test
    @TestAsAdminBS
    void runBenutzerInaktivierenJob_shouldDeactivateBenutzerWithNullLastUsedSessionTimestamp() {
        // ARRANGE
        setTime(OffsetDateTime.parse("2026-01-01T00:00:00.999999999+00:00")); // After Production
        Benutzer benutzerWithNull = BenutzerBuilder.create(
            builder -> builder
                .withLastUsedSessionTimestamp(null)
        );
        benutzerRepo.persist(benutzerWithNull);

        // ACT
        superAdminRunnerService.runBenutzerInaktivierenJob(true);

        // ASSERT
        Benutzer benutzerAfterJob = benutzerRepo.findById(benutzerWithNull.getId()).orElseThrow();
        assertThat(benutzerAfterJob.isAktiv()).isFalse();
    }


    @Test
    @TestAsAdminBS
    void runBenutzerInaktivierenJob_shouldNotDeactivateManuallyActivatedWithNullLastUsedSessionTimestamp() {
        // ARRANGE
        setTime(OffsetDateTime.parse("2026-01-01T00:00:00.999999999+00:00")); // After Production
        Benutzer benutzerWithNullButManuallyActivated = BenutzerBuilder.create(
            builder -> builder
                .withLastUsedSessionTimestamp(null)
                .withLastManuallyActivatedTimestamp(getAppClock().offsetDateTime().now().minusDays(2))
        );
        benutzerRepo.persist(benutzerWithNullButManuallyActivated);

        // ACT
        superAdminRunnerService.runBenutzerInaktivierenJob(true);

        // ASSERT
        Benutzer benutzerAfterJob = benutzerRepo.findById(benutzerWithNullButManuallyActivated.getId()).orElseThrow();
        assertThat(benutzerAfterJob.isAktiv()).isTrue();
    }

    @Test
    @TestAsAdminBS
    void runKinderDeaktivierenJob() {
        // ARRANGE
        // mehr als 9 Monate her
        setTime(OffsetDateTime.parse("2024-01-01T00:00:00.999999999+00:00"));

        var kind = KindBuilder.create(builder -> builder
            .withGeburtsdatum(LocalDate.of(1990, 1, 1))
        );
        kindService.persistWithoutDuplicateCheck(kind);
        var dossier = fixtureService.createDossier(kind);
        fixtureService.createNotiz(dossier, Set.of()); // old Notiz

        // deactivated external kind
        var kindInaktivEscada = KindBuilder.create(builder -> builder
            .withExternalImportInfo(UUID.randomUUID().toString(), false, getAppClock().offsetDateTime().now()));
        kindService.persistWithoutDuplicateCheck(kindInaktivEscada);

        // ACT
        resetTime();
        superAdminRunnerService.runKinderInaktivierenJob(true);
        // ASSERT
        Kind kindAfterInaktivierung = kindService.getById(kind.getId());
        assertThat(kindAfterInaktivierung.getKindAktivStatus()).isEqualTo(EntityAktivStatus.INAKTIV);
        Kind escadaKindAfterRun = kindService.getById(kindInaktivEscada.getId());
        assertThat(escadaKindAfterRun.getKindAktivStatus()).isEqualTo(EntityAktivStatus.INAKTIV);
    }

    @ParameterizedTest
    @MethodSource("documentQueueRuns")
    @TestAsAdminBS
    void runDocumentQueueJob(@NonNull FileTyp fileTyp, boolean expectedSuccess, @Nullable String parameter) {
        // ARRANGE
        Benutzer benutzer = BenutzerBuilder.defaultBenutzer();

        DocumentQueue documentQueue = DocumentQueueBuilder.create(
            builder -> builder.withBenutzer(benutzer)
                .withType(fileTyp)
        );

        if (parameter != null) {
            documentQueue.setParameters(parameter);
        }

        benutzerRepo.persist(benutzer);
        documentQueueRepo.persist(documentQueue);

        // ACT
        superAdminRunnerService.runDocumentQueueJob(true);

        // ASSERT
        DocumentQueue documentQueueAfter = documentQueueRepo.getById(documentQueue.getId());
        if (expectedSuccess) {
            assertThat(documentQueueAfter.getResult()).isNotNull();
            assertThat(documentQueueAfter.getErrorCount()).isZero();
            assertThat(documentQueueAfter.getLastError()).isNull();
            assertThat(documentQueueAfter.getStatus()).isEqualTo(DocumentQueueStatus.SUCCESS);
            assertThat(documentQueueAfter.getTimestampResult()).isNotNull();
        } else {
            assertThat(documentQueueAfter.getResult()).isNull();
            assertThat(documentQueueAfter.getErrorCount()).isPositive();
            assertThat(documentQueueAfter.getLastError()).isNotNull();
            assertThat(documentQueueAfter.getStatus()).isEqualTo(DocumentQueueStatus.FAILED_RETRY);
            assertThat(documentQueueAfter.getTimestampResult()).isNotNull();
        }
    }

    @Test
    @TestAsAdminBS
    void runDocumentQueueCleanUpJob() {
        // ARRANGE
        Benutzer benutzer = fixtureService.createBenutzer();
        FileData fileData = FileDataBuilder.defaultImgFileData();
        fileDataRepo.persist(fileData);

        OffsetDateTime now = getAppClock().offsetDateTime().now();
        DocumentQueue documentQueueSucess = DocumentQueueBuilder.create(
            builder -> builder.withBenutzer(benutzer)
                .withTimestampResult(now.minusDays(2))
                .withStatus(DocumentQueueStatus.SUCCESS));
        documentQueueRepo.persist(documentQueueSucess);

        DocumentQueue successQueueOld = DocumentQueueBuilder.create(
            builder -> builder.withBenutzer(benutzer)
                .withTimestampResult(now.minusMonths(1))
                .withResult(fileData)
                .withStatus(DocumentQueueStatus.SUCCESS));
        documentQueueRepo.persist(successQueueOld);

        DocumentQueue failureQueue = DocumentQueueBuilder.create(
            builder -> builder.withType(FileTyp.PLAYGROUND_ERROR)
                .withBenutzer(benutzer)
                .withTimestampResult(now.minusHours(3))
                .withStatus(DocumentQueueStatus.FAILED));
        documentQueueRepo.persist(failureQueue);

        DocumentQueue notYetExecutedQueue = DocumentQueueBuilder.create(
            builder -> builder.withType(FileTyp.PLAYGROUND_ERROR)
                .withBenutzer(benutzer)
                .withTimestampResult(now.minusDays(6))
                .withStatus(DocumentQueueStatus.NEW));
        documentQueueRepo.persist(notYetExecutedQueue);

        // ACT
        superAdminRunnerService.runDocumentQueueCleanupJob(true);

        // ASSERT
        Optional<DocumentQueue> documentQueueSucessAfter = documentQueueRepo.findById(documentQueueSucess.getId());
        assertThat(documentQueueSucessAfter).isPresent();

        // Removed because old!
        Optional<DocumentQueue> successQueueOldAfter = documentQueueRepo.findById(successQueueOld.getId());
        assertThat(successQueueOldAfter).isNotPresent();

        Optional<DocumentQueue> failureQueueAfter = documentQueueRepo.findById(failureQueue.getId());
        assertThat(failureQueueAfter).isPresent();

        Optional<DocumentQueue> notYetExecutedQueueAfter = documentQueueRepo.findById(failureQueue.getId());
        assertThat(notYetExecutedQueueAfter).isPresent();
    }

    @Test
    @TestAsAdminBS
    void runReloadDuplikateJob() {
        // ARRANGE
        Kind kind1 = fixtureService.createKind("Otto", "Bähler", LocalDate.of(2000, 1, 1), Geschlecht.MAENNLICH);
        Kind kind2 = fixtureService.createKind("Otto", "Baehler", LocalDate.of(2000, 1, 1), Geschlecht.MAENNLICH);
        Kind kind3 = fixtureService.createKind("Someone", "Someone", LocalDate.of(2000, 1, 1), Geschlecht.WEIBLICH);
        // ACT
        superAdminRunnerService.runReloadDuplikateJob(true);
        // ASSERT
        assertThat(duplikatRepo.findByKindIds(kind1.getId(), kind2.getId())).isPresent();
        assertThat(duplikatRepo.findByKindIds(kind1.getId(), kind3.getId())).isEmpty();
        assertThat(duplikatRepo.findByKindIds(kind2.getId(), kind3.getId())).isEmpty();
    }

    @Test
    @TestAsAdminBS
    void rerunReloadDuplikateJob_should_not_create_another_duplikat() {
        // ARRANGE
        Kind kind1 = fixtureService.createKind("Markus", "Höfer", LocalDate.of(2000, 1, 1), Geschlecht.MAENNLICH);
        Kind kind2 = fixtureService.createKind("Markus", "Hoefer", LocalDate.of(2000, 1, 1), Geschlecht.MAENNLICH);
        superAdminRunnerService.runReloadDuplikateJob(true);
        DuplikatSearchTerm searchTerm = new DuplikatSearchTerm(
            kind1.getId(),
            kind1.getName(),
            kind1.getVorname(),
            kind1.getGeburtsdatum(),
            kind1.getGeschlecht()
        );
        List<Kind> duplikateBefore = duplikatRepo.searchDuplikate(searchTerm);
        assertThat(duplikateBefore).isNotNull().hasSize(1).containsExactly(kind2);
        // ACT
        superAdminRunnerService.runReloadDuplikateJob(true);
        // ASSERT
        List<Kind> duplikateAfter = duplikatRepo.searchDuplikate(searchTerm);
        assertThat(duplikateAfter).isNotNull().hasSize(1).containsExactly(kind2);
    }

    @Test
    @TestAsAdminBS
    void rerunReloadDuplikateJob_should_remove_duplikat_if_not_same_anymore() {
        // ARRANGE
        Kind kind1 = fixtureService.createKind("Otto", "Bähler", LocalDate.of(2000, 1, 1), Geschlecht.MAENNLICH);
        Kind kind2 = fixtureService.createKind("Otto", "Baehler", LocalDate.of(2000, 1, 1), Geschlecht.MAENNLICH);
        superAdminRunnerService.runReloadDuplikateJob(true);
        Optional<Duplikat> duplikatBefore = duplikatRepo.findByKindIds(kind1.getId(), kind2.getId());
        assertThat(duplikatBefore).isPresent();
        // Das eine Kind ändert seinen Namen
        fixtureService.changeNameOfKind(kind1.getId(), "Herger");
        // ACT
        superAdminRunnerService.runReloadDuplikateJob(true);
        // ASSERT
        Optional<Duplikat> duplikatAfter = duplikatRepo.findByKindIds(kind1.getId(), kind2.getId());
        assertThat(duplikatAfter).isEmpty();
    }

    @NonNull
    private static Stream<Arguments> documentQueueRuns() {
        return Stream.of(
            Arguments.of(FileTyp.PLAYGROUND, true, null),
            Arguments.of(FileTyp.PLAYGROUND_ERROR, false, null),
            Arguments.of(FileTyp.SPSS_REPORT, true, "{\"spssReportType\": \"" + SpssReportType.KINDERGARTEN + "\"}"),
            Arguments.of(FileTyp.NOTIZUPLOAD, false, null)
        );
    }
}
