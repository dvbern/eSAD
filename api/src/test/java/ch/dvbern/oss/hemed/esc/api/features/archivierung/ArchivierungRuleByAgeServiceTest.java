package ch.dvbern.oss.hemed.esc.api.features.archivierung;

import java.time.OffsetDateTime;
import java.util.List;

import ch.dvbern.dvbstarter.shared.types.id.ID;
import ch.dvbern.oss.hemed.esc.api.features.kinder.Dossier;
import ch.dvbern.oss.hemed.esc.api.features.kinder.Kind;
import ch.dvbern.oss.hemed.esc.api.shared.archivierung.ArchivierungRuleByAgeService;
import ch.dvbern.oss.hemed.esc.api.shared.benutzer.Benutzer;
import ch.dvbern.oss.hemed.esc.api.shared.types.EntityAktivStatus;
import ch.dvbern.oss.hemed.esc.api.testutil.AbstractITest;
import ch.dvbern.oss.hemed.esc.api.testutil.FixtureService;
import ch.dvbern.oss.hemed.esc.api.testutil.builder.ArchivdokumentBuilder;
import ch.dvbern.oss.hemed.esc.api.testutil.security.TestAsSupportBS;
import io.quarkus.test.junit.QuarkusTest;
import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@QuarkusTest
@RequiredArgsConstructor
class ArchivierungRuleByAgeServiceTest extends AbstractITest {
    private static final int MONTHS_ARCHIVED_BEFORE_DELETION = 6;
    public static final int DEFAULT_BATCHSIZE_FOR_TESTING = 100;

    private final ArchivierungRuleByAgeService archivierungRuleByAgeService;
    private final FixtureService fixtureService;
    private final ArchivdokumentService archivdokumentService;

    @Test
    @TestAsSupportBS
    void should_find_dossiers_that_are_marked_for_delete() {
        // ARRANGE
        OffsetDateTime olderDate =
            getAppClock().offsetDateTime().now().minusMonths(MONTHS_ARCHIVED_BEFORE_DELETION).minusDays(1);
        OffsetDateTime newerDate =
            getAppClock().offsetDateTime().now().minusMonths(MONTHS_ARCHIVED_BEFORE_DELETION).plusDays(1);
        Benutzer benutzer = fixtureService.createBenutzerFuerZentraleOrganisation();

        // Kind_1 -> Aktiv und keine Archivierungsdokumente -> sollte nicht gefunden werden.
        Kind kind_1 = fixtureService.createKind(EntityAktivStatus.AKTIV);
        Dossier dossier_1 = fixtureService.createDossier(kind_1);
        Archivdokument archivdokument_1 = ArchivdokumentBuilder.create(builder -> builder
            .withArchivierungTyp(ArchivierungTyp.KIND_DEAKTIVIERT)
            .withTimestampDownloaded(olderDate)
            .withOwner(benutzer)
            .withStatus(ArchivierungStatus.DOWNLOADED_READY_FOR_DELETION)
            .withDossierID(dossier_1.getId().getUUID()));
        archivdokumentService.persist(archivdokument_1);

        // Kind_2 -> Inaktiv und Archivierungsdokumente (offen) -> sollte nicht gefunden werden;
        Kind kind_2 = fixtureService.createKind(EntityAktivStatus.INAKTIV);
        Dossier dossier_2 = fixtureService.createDossier(kind_2);
        Archivdokument archivdokument_2 = ArchivdokumentBuilder.create(builder -> builder
            .withArchivierungTyp(ArchivierungTyp.KIND_DEAKTIVIERT)
            .withTimestampDownloaded(olderDate)
            .withOwner(benutzer)
            .withStatus(ArchivierungStatus.OFFEN)
            .withDossierID(dossier_2.getId().getUUID()));
        archivdokumentService.persist(archivdokument_2);

        // Kind_3 -> Inaktiv und Archivierungsdokumente (abgeschlossen & Typ Arztwechsel) -> sollte nicht gefunden
        // werden;
        Kind kind_3 = fixtureService.createKind(EntityAktivStatus.INAKTIV);
        Dossier dossier_3 = fixtureService.createDossier(kind_3);
        Archivdokument archivdokument_3 = ArchivdokumentBuilder.create(builder -> builder
            .withArchivierungTyp(ArchivierungTyp.ZUSTAENDIGER_ARZT_CHANGED)
            .withTimestampDownloaded(olderDate)
            .withOwner(benutzer)
            .withStatus(ArchivierungStatus.DOWNLOADED_READY_FOR_DELETION)
            .withDossierID(dossier_3.getId().getUUID()));
        archivdokumentService.persist(archivdokument_3);

        // Kind_4 -> Inaktiv und Archivierungsdokumente (abgeschlossen & Typ Kind Deaktiviert & Datum 1 Monat alt) ->
        // sollte nicht gefunden werden;
        Kind kind_4 = fixtureService.createKind(EntityAktivStatus.INAKTIV);
        Dossier dossier_4 = fixtureService.createDossier(kind_4);
        Archivdokument archivdokument_4 = ArchivdokumentBuilder.create(builder -> builder
            .withArchivierungTyp(ArchivierungTyp.KIND_DEAKTIVIERT)
            .withTimestampDownloaded(newerDate)
            .withOwner(benutzer)
            .withStatus(ArchivierungStatus.DOWNLOADED_READY_FOR_DELETION)
            .withDossierID(dossier_4.getId().getUUID()));
        archivdokumentService.persist(archivdokument_4);

        // Kind_5 -> Inaktiv und Archivierungsdokumente (abgeschlossen & Typ Kind Deaktiviert & Datum 9 Monat alt) ->
        // sollte gefunden werden;
        Kind kind_5 = fixtureService.createKind(EntityAktivStatus.INAKTIV);
        Dossier dossier_5 = fixtureService.createDossier(kind_5);
        Archivdokument archivdokument_5 = ArchivdokumentBuilder.create(builder -> builder
            .withArchivierungTyp(ArchivierungTyp.KIND_DEAKTIVIERT)
            .withTimestampDownloaded(olderDate)
            .withOwner(benutzer)
            .withStatus(ArchivierungStatus.DOWNLOADED_READY_FOR_DELETION)
            .withDossierID(dossier_5.getId().getUUID()));
        archivdokumentService.persist(archivdokument_5);

        // ACT
        List<ID<Dossier>> dossierToDelete = archivierungRuleByAgeService.findDossiersToDelete(
            MONTHS_ARCHIVED_BEFORE_DELETION,
            DEFAULT_BATCHSIZE_FOR_TESTING);

        // ASSERT
        assertThat(dossierToDelete)
            .isNotEmpty()
            .hasSize(1)
            .anyMatch(dossier -> dossier.getUUID().equals(dossier_5.getId().getUUID()));
    }
}
