package ch.dvbern.oss.hemed.esc.api.shared.scheduler;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Stream;

import ch.dvbern.dvbstarter.shared.i18n.translations.AppLanguages;
import ch.dvbern.dvbstarter.shared.i18n.translations.TL;
import ch.dvbern.dvbstarter.shared.i18n.translations.TLProducer;
import ch.dvbern.dvbstarter.shared.types.emailaddress.EmailAddress;
import ch.dvbern.oss.hemed.esc.api.externalimportinfojson.ImportTyp;
import ch.dvbern.oss.hemed.esc.api.features.anlass.Anlass;
import ch.dvbern.oss.hemed.esc.api.features.anlass.einladung.Einladung;
import ch.dvbern.oss.hemed.esc.api.features.anlass.impfung.UntersuchungImpfungInternService;
import ch.dvbern.oss.hemed.esc.api.features.anlass.untersuchung.Untersuchung;
import ch.dvbern.oss.hemed.esc.api.features.archivierung.Archivdokument;
import ch.dvbern.oss.hemed.esc.api.features.archivierung.ArchivdokumentRepo;
import ch.dvbern.oss.hemed.esc.api.features.archivierung.ArchivierungStatus;
import ch.dvbern.oss.hemed.esc.api.features.archivierung.ArchivierungTyp;
import ch.dvbern.oss.hemed.esc.api.features.benutzer.BenutzerRepo;
import ch.dvbern.oss.hemed.esc.api.features.impfstoff.Impfstoff;
import ch.dvbern.oss.hemed.esc.api.features.impfstoff.ImpfstoffCharge;
import ch.dvbern.oss.hemed.esc.api.features.impfstoff.ImpfstoffChargeService;
import ch.dvbern.oss.hemed.esc.api.features.kinder.Dossier;
import ch.dvbern.oss.hemed.esc.api.features.kinder.Kind;
import ch.dvbern.oss.hemed.esc.api.features.kinder.KindService;
import ch.dvbern.oss.hemed.esc.api.features.kinder.duplikate.Duplikat;
import ch.dvbern.oss.hemed.esc.api.features.kinder.duplikate.DuplikatRepo;
import ch.dvbern.oss.hemed.esc.api.features.kinder.duplikate.DuplikatSearchTerm;
import ch.dvbern.oss.hemed.esc.api.features.notizen.NotizTyp;
import ch.dvbern.oss.hemed.esc.api.features.spss.SpssReportType;
import ch.dvbern.oss.hemed.esc.api.shared.archivierung.ArchivierungRuleProducer;
import ch.dvbern.oss.hemed.esc.api.shared.archivierung.ArchivierungRuleService;
import ch.dvbern.oss.hemed.esc.api.shared.auth.TenantEnum;
import ch.dvbern.oss.hemed.esc.api.shared.auth.TenantProducer;
import ch.dvbern.oss.hemed.esc.api.shared.benutzer.Benutzer;
import ch.dvbern.oss.hemed.esc.api.shared.documentqueue.DocumentQueue;
import ch.dvbern.oss.hemed.esc.api.shared.documentqueue.DocumentQueueRepo;
import ch.dvbern.oss.hemed.esc.api.shared.documentqueue.DocumentQueueStatus;
import ch.dvbern.oss.hemed.esc.api.shared.dossierqueue.DossierQueueRunnerService;
import ch.dvbern.oss.hemed.esc.api.shared.dossierqueue.DossierQueueService;
import ch.dvbern.oss.hemed.esc.api.shared.dossierqueue.DossierQueueTyp;
import ch.dvbern.oss.hemed.esc.api.shared.fileio.FileData;
import ch.dvbern.oss.hemed.esc.api.shared.fileio.FileDataRepo;
import ch.dvbern.oss.hemed.esc.api.shared.fileio.FileTyp;
import ch.dvbern.oss.hemed.esc.api.shared.mail.service.MailDAO;
import ch.dvbern.oss.hemed.esc.api.shared.person.Geschlecht;
import ch.dvbern.oss.hemed.esc.api.shared.settings.TenantSetting;
import ch.dvbern.oss.hemed.esc.api.shared.settings.TenantSettingsService;
import ch.dvbern.oss.hemed.esc.api.shared.settings.features.FeatureEnabledSetting;
import ch.dvbern.oss.hemed.esc.api.shared.types.EntityAktivStatus;
import ch.dvbern.oss.hemed.esc.api.testutil.AbstractITest;
import ch.dvbern.oss.hemed.esc.api.testutil.FixtureService;
import ch.dvbern.oss.hemed.esc.api.testutil.builder.BenutzerBuilder;
import ch.dvbern.oss.hemed.esc.api.testutil.builder.DocumentQueueBuilder;
import ch.dvbern.oss.hemed.esc.api.testutil.builder.FileDataBuilder;
import ch.dvbern.oss.hemed.esc.api.testutil.builder.ImpfstoffChargeBuilder;
import ch.dvbern.oss.hemed.esc.api.testutil.builder.KindBuilder;
import ch.dvbern.oss.hemed.esc.api.testutil.builder.TenantSettingBuilder;
import ch.dvbern.oss.hemed.esc.api.testutil.builder.UntersuchungImpfungInternBuilder;
import ch.dvbern.oss.hemed.esc.api.testutil.security.TestAsAdminBE;
import ch.dvbern.oss.hemed.esc.api.testutil.security.TestAsAdminBS;
import io.quarkus.test.junit.QuarkusMock;
import io.quarkus.test.junit.QuarkusTest;
import io.smallrye.mutiny.Uni;
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
import org.mockito.Mockito;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

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
    private final ImpfstoffChargeService impfstoffChargeService;
    private final UntersuchungImpfungInternService untersuchungImpfungInternService;
    private final ArchivdokumentRepo archivdokumentRepo;

    private final TenantProducer tenantProducer;
    private MailDAO mailDAOMock;

    @Override
    @BeforeEach
    public void setUp() {
        super.setUp();
        TenantSettingsService tenantSettingsServiceMock = Mockito.mock(TenantSettingsService.class);
        QuarkusMock.installMockForType(tenantSettingsServiceMock, TenantSettingsService.class);
        TenantSetting tenantSettingOverride = TenantSettingBuilder.create(tenantSettingBuilder -> tenantSettingBuilder
            .withAdHocImpfstoffChargenFeature(FeatureEnabledSetting.ENABLED)
            .withSupportedLangagues(List.of(AppLanguages.DE, AppLanguages.FR))
            .withZentraleOrganisationFeature(FeatureEnabledSetting.ENABLED)
        );
        Mockito.when(tenantSettingsServiceMock.getTenantSetting()).thenReturn(tenantSettingOverride);
        Mockito.when(tenantSettingsServiceMock.isZentraleOrganisation()).thenReturn(true);

        ArchivierungRuleProducer archivierungRuleProducerMock = Mockito.mock(ArchivierungRuleProducer.class);
        QuarkusMock.installMockForType(archivierungRuleProducerMock, ArchivierungRuleProducer.class);
        ArchivierungRuleService archivierungRuleServiceMock = Mockito.mock(ArchivierungRuleService.class);
        Mockito.when(archivierungRuleProducerMock.archivierungRuleService()).thenReturn(archivierungRuleServiceMock);

        mailDAOMock = Mockito.mock(MailDAO.class);
        Mockito.when(mailDAOMock.sendEmail(any(String.class), any(String.class), any(String.class)))
            .thenReturn(Uni.createFrom().voidItem());
        QuarkusMock.installMockForType(mailDAOMock, MailDAO.class);
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
        assertThat(benutzerRecentlyLoggedInAfter.getBenutzerAktivStatus()).isEqualTo(EntityAktivStatus.AKTIV);
        assertThat(benutzer1MonthSinceLoggedInAfter.getBenutzerAktivStatus()).isEqualTo(EntityAktivStatus.AKTIV);
        assertThat(benutzer2MonthsSinceLoggedInAfter.getBenutzerAktivStatus()).isEqualTo(EntityAktivStatus.INAKTIV);
        assertThat(benutzer6MonthsSinceLoggedInButActivatedManuallyAfter.getBenutzerAktivStatus()).isEqualTo(
            EntityAktivStatus.AKTIV);
        assertThat(benutzerNeverLoggedInAfter.getBenutzerAktivStatus()).isEqualTo(EntityAktivStatus.INAKTIV);
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
        assertThat(benutzerRecentlyLoggedInAfter.getBenutzerAktivStatus()).isEqualTo(EntityAktivStatus.AKTIV);
        assertThat(benutzer1MonthSinceLoggedInAfter.getBenutzerAktivStatus()).isEqualTo(EntityAktivStatus.AKTIV);
        assertThat(benutzer2MonthsSinceLoggedInAfter.getBenutzerAktivStatus()).isEqualTo(EntityAktivStatus.AKTIV);
        assertThat(benutzer6MonthsSinceLoggedInAfter.getBenutzerAktivStatus()).isEqualTo(EntityAktivStatus.AKTIV);
        assertThat(benutzerNeverLoggedInAfter.getBenutzerAktivStatus()).isEqualTo(EntityAktivStatus.AKTIV);
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
        assertThat(benutzerAfterJob.getBenutzerAktivStatus()).isEqualTo(EntityAktivStatus.INAKTIV);
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
        assertThat(benutzerAfterJob.getBenutzerAktivStatus()).isEqualTo(EntityAktivStatus.AKTIV);
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
        fixtureService.createNotiz(dossier, Set.of(), NotizTyp.DEFAULT); // old Notiz

        // deactivated external kind
        var kindInaktivEscada = KindBuilder.create(builder -> builder
            .withExternalImportInfo(
                UUID.randomUUID().toString(),
                false,
                getAppClock().offsetDateTime().now(),
                ImportTyp.ESCADA));
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
        Benutzer benutzer = fixtureService.createBenutzerFuerZentraleOrganisation();
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

    @Test
    @TestAsAdminBS
    void runDeactivateImpfstoffChargenJob() {
        // ARRANGE
        Impfstoff impfstoff = fixtureService.createImpfstoff();

        ImpfstoffCharge chargeUsedRecently = ImpfstoffChargeBuilder.create(
            builder -> builder.forImpfstoff(impfstoff).withName("RecentCharge"));
        impfstoffChargeService.persist(chargeUsedRecently);

        ImpfstoffCharge chargeUsedLongAgo = ImpfstoffChargeBuilder.create(
            builder -> builder.forImpfstoff(impfstoff).withName("OldCharge"));
        impfstoffChargeService.persist(chargeUsedLongAgo);

        Dossier dossier = fixtureService.createDossier();
        Anlass anlass = fixtureService.createAnlass(LocalDate.now());
        Einladung einladung = fixtureService.createEinladung(anlass, dossier);
        Untersuchung untersuchung = fixtureService.createUntersuchungWithImpfungen(einladung);
        assertThat(untersuchung.getUntersuchungImpfungen()).isNotNull();

        untersuchungImpfungInternService.persist(UntersuchungImpfungInternBuilder.create(builder -> builder
            .forUntersuchungImpfungen(untersuchung.getUntersuchungImpfungen())
            .withUserErfasst(fixtureService.createBenutzerFuerZentraleOrganisation())
            .withImpfstoff(impfstoff)
            .withCharge(chargeUsedRecently)
            .withDatumImpfung(LocalDate.now().minusMonths(1))));

        untersuchungImpfungInternService.persist(UntersuchungImpfungInternBuilder.create(builder -> builder
            .forUntersuchungImpfungen(untersuchung.getUntersuchungImpfungen())
            .withUserErfasst(fixtureService.createBenutzerFuerZentraleOrganisation())
            .withImpfstoff(impfstoff)
            .withCharge(chargeUsedLongAgo)
            .withDatumImpfung(LocalDate.now().minusMonths(13))));

        // ACT
        superAdminRunnerService.runDeactivateImpfstoffChargenJob(true);

        // ASSERT
        ImpfstoffCharge chargeUsedRecentlyAfter = impfstoffChargeService.getById(chargeUsedRecently.getId());
        ImpfstoffCharge chargeUsedLongAgoAfter = impfstoffChargeService.getById(chargeUsedLongAgo.getId());
        assertThat(chargeUsedRecentlyAfter.isAktiv()).isTrue();
        assertThat(chargeUsedLongAgoAfter.isAktiv()).isFalse();
    }

    @Test
    @TestAsAdminBS
    void runDeleteImpfstoffChargenJob() {
        // ARRANGE
        Impfstoff impfstoff = fixtureService.createImpfstoff();

        setTime(OffsetDateTime.now().minusMonths(13));
        ImpfstoffCharge chargeNeverUsedOld = ImpfstoffChargeBuilder.create(
            builder -> builder.forImpfstoff(impfstoff).withName("NeverUsedOldCharge"));
        impfstoffChargeService.persist(chargeNeverUsedOld);
        resetTime();

        ImpfstoffCharge chargeNeverUsedRecent = ImpfstoffChargeBuilder.create(
            builder -> builder.forImpfstoff(impfstoff).withName("NeverUsedRecentCharge"));
        impfstoffChargeService.persist(chargeNeverUsedRecent);

        // ACT
        superAdminRunnerService.runDeleteImpfstoffChargenJob(true);

        // ASSERT
        Optional<ImpfstoffCharge> chargeNeverUsedOldAfter = impfstoffChargeService.findById(chargeNeverUsedOld.getId());
        Optional<ImpfstoffCharge> chargeNeverUsedRecentAfter =
            impfstoffChargeService.findById(chargeNeverUsedRecent.getId());
        assertThat(chargeNeverUsedOldAfter).isEmpty();
        assertThat(chargeNeverUsedRecentAfter).isPresent();
    }

    @Test
    @TestAsAdminBE
    void runArchiveDocumentDownloadEmailReminderJob() {
        // ARRANGE
        tenantProducer.setCurrentTenant(TenantEnum.BE);
        when(mailDAOMock.sendEmail(any(String.class), any(String.class), any(String.class)))
            .thenReturn(Uni.createFrom().voidItem());

        mockTranslations(Map.of(
            AppLanguages.DE, Map.of(
                "archivdokument.reminder.email.subject", "expected subject de",
                "archivdokument.reminder.email.content", "expected content de"
            ),
            AppLanguages.FR, Map.of(
                "archivdokument.reminder.email.subject", "expected subject fr",
                "archivdokument.reminder.email.content", "expected content fr"
            )
        ));

        // Create a user who owns a document to be downloaded (and should receive the email)
        Benutzer userWithDocument = BenutzerBuilder.create(builder -> builder
            .withEmail(EmailAddress.fromString("user.arzt@example.com"))
            .withVorname("Hans")
            .withNachname("Muster")
            .withKeycloakId(UUID.randomUUID().toString()));
        benutzerRepo.persistAndFlush(userWithDocument);

        Archivdokument archivdokument1 = new Archivdokument(
            OffsetDateTime.now(),
            ArchivierungTyp.KIND_DEAKTIVIERT,
            ArchivierungStatus.OFFEN,
            UUID.randomUUID(),
            "Musterkind Max",
            null,
            userWithDocument,
            null
        );
        archivdokumentRepo.persist(archivdokument1);
        Archivdokument archivdokument2 = new Archivdokument(
            OffsetDateTime.now(),
            ArchivierungTyp.KIND_DEAKTIVIERT,
            ArchivierungStatus.OFFEN,
            UUID.randomUUID(),
            "Musterkind Matin",
            null,
            userWithDocument,
            null
        );
        archivdokumentRepo.persist(archivdokument2);

        // Create a user who owns a downloaded document (and should not receive the email)
        Benutzer userWithDocumentDownloaded = BenutzerBuilder.create(builder -> builder
            .withEmail(EmailAddress.fromString("user.mpa@example.com"))
            .withVorname("Peter")
            .withNachname("Muster")
            .withKeycloakId(UUID.randomUUID().toString()));
        benutzerRepo.persistAndFlush(userWithDocumentDownloaded);

        Archivdokument archivdokumentDownloaded = new Archivdokument(
            OffsetDateTime.now(),
            ArchivierungTyp.KIND_DEAKTIVIERT,
            ArchivierungStatus.DOWNLOADED,
            UUID.randomUUID(),
            "Musterkind Maria",
            null,
            userWithDocumentDownloaded,
            null
        );
        archivdokumentRepo.persist(archivdokumentDownloaded);

        // ACT
        superAdminRunnerService.runArchiveDocumentDownloadEmailReminderJob(true);

        // ASSERT
        verify(mailDAOMock, times(1)).sendEmail(
            "user.arzt@example.com",
            "expected subject de / expected subject fr",
            "expected content de<hr>expected content fr"
        );
    }

    @Test
    @TestAsAdminBS
    void runMarkForArchivierungJob() {
        // ARRANGE
        ArchivierungRuleProducer archivierungRuleProducerMock = mock(ArchivierungRuleProducer.class);
        QuarkusMock.installMockForType(archivierungRuleProducerMock, ArchivierungRuleProducer.class);
        DossierQueueService dossierQueueServiceMock = mock(DossierQueueService.class);
        QuarkusMock.installMockForType(dossierQueueServiceMock, DossierQueueService.class);
        SchedulerConfigService schedulerConfigServiceMock = mock(SchedulerConfigService.class);
        QuarkusMock.installMockForType(schedulerConfigServiceMock, SchedulerConfigService.class);

        SchedulerConfig config = new SchedulerConfig(SchedulerTyp.MARK_FOR_ARCHIVIERUNG, false, 100);
        when(schedulerConfigServiceMock.getSchedulerConfig(SchedulerTyp.MARK_FOR_ARCHIVIERUNG)).thenReturn(config);

        Dossier dossier = fixtureService.createDossier();
        ArchivierungRuleService ruleService = mock(ArchivierungRuleService.class);
        when(archivierungRuleProducerMock.archivierungRuleService()).thenReturn(ruleService);
        when(ruleService.findDossiersToArchive(anyInt())).thenReturn(List.of(dossier.getId()));

        // ACT
        superAdminRunnerService.runMarkForArchivierungJob(true);

        // ASSERT
        verify(dossierQueueServiceMock).createDossierQueueItem(dossier.getId(), DossierQueueTyp.ARCHIVIERUNG, null);
    }

    @Test
    @TestAsAdminBS
    void runDossierQueueProcessingJob() {
        // ARRANGE
        DossierQueueRunnerService dossierQueueRunnerServiceMock = mock(DossierQueueRunnerService.class);
        QuarkusMock.installMockForType(dossierQueueRunnerServiceMock, DossierQueueRunnerService.class);
        SchedulerConfigService schedulerConfigServiceMock = mock(SchedulerConfigService.class);
        QuarkusMock.installMockForType(schedulerConfigServiceMock, SchedulerConfigService.class);

        SchedulerConfig config = new SchedulerConfig(SchedulerTyp.DOSSIER_QUEUE_PROCESSING, false, 100);
        when(schedulerConfigServiceMock.getSchedulerConfig(SchedulerTyp.DOSSIER_QUEUE_PROCESSING)).thenReturn(config);
        when(dossierQueueRunnerServiceMock.processQueueItems(config)).thenReturn(new SchedulerProcessingResult(
            100,
            1,
            1));

        // ACT
        superAdminRunnerService.runDossierQueueProcessingJob(true);

        // ASSERT
        verify(dossierQueueRunnerServiceMock).processQueueItems(config);
    }

    @Test
    @TestAsAdminBS
    void runDossierQueueCleanupJob() {
        // ARRANGE
        DossierQueueRunnerService dossierQueueRunnerServiceMock = mock(DossierQueueRunnerService.class);
        QuarkusMock.installMockForType(dossierQueueRunnerServiceMock, DossierQueueRunnerService.class);
        SchedulerConfigService schedulerConfigServiceMock = mock(SchedulerConfigService.class);
        QuarkusMock.installMockForType(schedulerConfigServiceMock, SchedulerConfigService.class);

        SchedulerConfig config = new SchedulerConfig(SchedulerTyp.DOSSIER_QUEUE_CLEAN_UP, false, 100);
        when(schedulerConfigServiceMock.getSchedulerConfig(SchedulerTyp.DOSSIER_QUEUE_CLEAN_UP)).thenReturn(config);
        when(dossierQueueRunnerServiceMock.performDossierQueueCleanup(config)).thenReturn(new SchedulerProcessingResult(
            100,
            1,
            1));

        // ACT
        superAdminRunnerService.runDossierQueueCleanupJob(true);

        // ASSERT
        verify(dossierQueueRunnerServiceMock).performDossierQueueCleanup(config);
    }

    @Test
    @TestAsAdminBS
    void runMarkForArchivierungDeleteJob() {
        // ARRANGE
        ArchivierungRuleProducer archivierungRuleProducerMock = mock(ArchivierungRuleProducer.class);
        QuarkusMock.installMockForType(archivierungRuleProducerMock, ArchivierungRuleProducer.class);
        DossierQueueService dossierQueueServiceMock = mock(DossierQueueService.class);
        QuarkusMock.installMockForType(dossierQueueServiceMock, DossierQueueService.class);
        SchedulerConfigService schedulerConfigServiceMock = mock(SchedulerConfigService.class);
        QuarkusMock.installMockForType(schedulerConfigServiceMock, SchedulerConfigService.class);

        SchedulerConfig config = new SchedulerConfig(SchedulerTyp.ARCHIVIERUNG_KIND_DELETE, false, 100);
        when(schedulerConfigServiceMock.getSchedulerConfig(SchedulerTyp.ARCHIVIERUNG_KIND_DELETE)).thenReturn(config);

        Dossier dossier = fixtureService.createDossier();
        ArchivierungRuleService ruleService = mock(ArchivierungRuleService.class);
        when(archivierungRuleProducerMock.archivierungRuleService()).thenReturn(ruleService);
        when(ruleService.findDossiersToDelete(anyInt(), anyInt())).thenReturn(List.of(dossier.getId()));

        // ACT
        superAdminRunnerService.runArchiveDocumentKindDeleteJob(true);

        // ASSERT
        verify(dossierQueueServiceMock).createDossierQueueItem(
            dossier.getId(),
            DossierQueueTyp.ARCHIVIERUNG_DELETE,
            null);
    }

    private void mockTranslations(Map<AppLanguages, Map<String, String>> translationForLanguages) {
        TLProducer tlProducerMock = mock(TLProducer.class);
        QuarkusMock.installMockForType(tlProducerMock, TLProducer.class);

        translationForLanguages.forEach((language, translations) -> {
            TL tlMock = mock(TL.class);
            when(tlProducerMock.forAppLanguage(language)).thenReturn(tlMock);

            when(tlMock.translate(anyString())).thenAnswer(invocation -> {
                String key = invocation.getArgument(0);
                return translations.getOrDefault(key, "MISSING: " + key);
            });

            when(tlMock.translate(anyString(), anyString(), any(), anyString(), any())).thenAnswer(invocation -> {
                String key = invocation.getArgument(0);
                return translations.getOrDefault(key, "MISSING: " + key);
            });
        });
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
