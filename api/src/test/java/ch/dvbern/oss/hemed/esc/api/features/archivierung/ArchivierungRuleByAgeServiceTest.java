package ch.dvbern.oss.hemed.esc.api.features.archivierung;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.stream.Stream;

import ch.dvbern.dvbstarter.shared.i18n.datetime.DateConst;
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
import org.checkerframework.checker.nullness.qual.NonNull;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

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
        Kind kind1 = fixtureService.createKind(EntityAktivStatus.AKTIV);
        Dossier dossier1 = fixtureService.createDossier(kind1);
        Archivdokument archivdokument1 = ArchivdokumentBuilder.create(builder -> builder
            .withArchivierungTyp(ArchivierungTyp.KIND_DEAKTIVIERT)
            .withTimestampDownloaded(olderDate)
            .withOwner(benutzer)
            .withStatus(ArchivierungStatus.DOWNLOADED_READY_FOR_DELETION)
            .withDossierID(dossier1.getId().getUUID()));
        archivdokumentService.persist(archivdokument1);

        // Kind_2 -> Inaktiv und Archivierungsdokumente (offen) -> sollte nicht gefunden werden;
        Kind kind2 = fixtureService.createKind(EntityAktivStatus.INAKTIV);
        Dossier dossier2 = fixtureService.createDossier(kind2);
        Archivdokument archivdokument2 = ArchivdokumentBuilder.create(builder -> builder
            .withArchivierungTyp(ArchivierungTyp.KIND_DEAKTIVIERT)
            .withTimestampDownloaded(olderDate)
            .withOwner(benutzer)
            .withStatus(ArchivierungStatus.OFFEN)
            .withDossierID(dossier2.getId().getUUID()));
        archivdokumentService.persist(archivdokument2);

        // Kind_3 -> Inaktiv und Archivierungsdokumente (abgeschlossen & Typ Arztwechsel) -> sollte nicht gefunden
        // werden;
        Kind kind3 = fixtureService.createKind(EntityAktivStatus.INAKTIV);
        Dossier dossier3 = fixtureService.createDossier(kind3);
        Archivdokument archivdokument3 = ArchivdokumentBuilder.create(builder -> builder
            .withArchivierungTyp(ArchivierungTyp.ZUSTAENDIGER_ARZT_CHANGED)
            .withTimestampDownloaded(olderDate)
            .withOwner(benutzer)
            .withStatus(ArchivierungStatus.DOWNLOADED_READY_FOR_DELETION)
            .withDossierID(dossier3.getId().getUUID()));
        archivdokumentService.persist(archivdokument3);

        // Kind_4 -> Inaktiv und Archivierungsdokumente (abgeschlossen & Typ Kind Deaktiviert & Datum 1 Monat alt) ->
        // sollte nicht gefunden werden;
        Kind kind4 = fixtureService.createKind(EntityAktivStatus.INAKTIV);
        Dossier dossier4 = fixtureService.createDossier(kind4);
        Archivdokument archivdokument4 = ArchivdokumentBuilder.create(builder -> builder
            .withArchivierungTyp(ArchivierungTyp.KIND_DEAKTIVIERT)
            .withTimestampDownloaded(newerDate)
            .withOwner(benutzer)
            .withStatus(ArchivierungStatus.DOWNLOADED_READY_FOR_DELETION)
            .withDossierID(dossier4.getId().getUUID()));
        archivdokumentService.persist(archivdokument4);

        // Kind_5 -> Inaktiv und Archivierungsdokumente (abgeschlossen & Typ Kind Deaktiviert & Datum 9 Monat alt) ->
        // sollte gefunden werden;
        Kind kind5 = fixtureService.createKind(EntityAktivStatus.INAKTIV);
        Dossier dossier5 = fixtureService.createDossier(kind5);
        Archivdokument archivdokument5 = ArchivdokumentBuilder.create(builder -> builder
            .withArchivierungTyp(ArchivierungTyp.KIND_DEAKTIVIERT)
            .withTimestampDownloaded(olderDate)
            .withOwner(benutzer)
            .withStatus(ArchivierungStatus.DOWNLOADED_READY_FOR_DELETION)
            .withDossierID(dossier5.getId().getUUID()));
        archivdokumentService.persist(archivdokument5);

        // ACT
        List<ID<Dossier>> dossierToDelete = archivierungRuleByAgeService.findDossiersToDelete(
            MONTHS_ARCHIVED_BEFORE_DELETION,
            DEFAULT_BATCHSIZE_FOR_TESTING);

        // ASSERT
        assertThat(dossierToDelete)
            .contains(dossier5.getId())
            .doesNotContain(dossier1.getId(), dossier2.getId(), dossier3.getId(), dossier4.getId());
    }

    /**
     * Prueft den Stichtag ({@value #MONTHS_ARCHIVED_BEFORE_DELETION} Monate zurueck) minutengenau ueber
     * Sommer-/Winterzeitgrenzen hinweg. Der korrekte, zonenbewusst berechnete Stichtag hat dieselbe lokale
     * Uhrzeit wie "jetzt"; das Archivdokument wird jeweils eine Minute davor bzw. danach heruntergeladen.
     * <p>
     * Der Vergleich in der DB ist {@code timestampDownloaded < cutoff}: nur ein Download vor dem Stichtag
     * fuehrt zur Loeschung.
     * <p>
     * Die fruehere, nicht zonenbewusste Berechnung ({@code OffsetDateTime.minusMonths()} behaelt den heutigen
     * Offset bei) verschob den Stichtag um eine Stunde und kippte damit das Resultat - je nach Richtung des
     * Zeitzonenwechsels in beide Richtungen.
     */
    @ParameterizedTest(name = "[{index}] {0}")
    @MethodSource("dstScenarios")
    @TestAsSupportBS
    void findDossiersToDelete_should_not_depend_on_dst(
        @SuppressWarnings("unused") String beschreibung,
        LocalDate heute,
        int downloadMinutenRelativZumStichtag,
        boolean sollGeloeschtWerden
    ) {
        // ARRANGE
        OffsetDateTime jetzt = atTestTime(heute);
        OffsetDateTime stichtag = atTestTime(heute.minusMonths(MONTHS_ARCHIVED_BEFORE_DELETION));
        OffsetDateTime timestampDownloaded = stichtag.plusMinutes(downloadMinutenRelativZumStichtag);

        Dossier dossier = createDossierReadyForDeletion(timestampDownloaded);
        setTime(jetzt);

        // ACT
        List<ID<Dossier>> dossierToDelete = archivierungRuleByAgeService.findDossiersToDelete(
            MONTHS_ARCHIVED_BEFORE_DELETION,
            DEFAULT_BATCHSIZE_FOR_TESTING);

        // ASSERT
        if (sollGeloeschtWerden) {
            assertThat(dossierToDelete)
                .contains(dossier.getId());
        } else {
            assertThat(dossierToDelete)
                .doesNotContain(dossier.getId());
        }
    }

    static Stream<Arguments> dstScenarios() {
        // 2026-06-21 ist Sommerzeit (UTC+2), der Stichtag 2025-12-21 Winterzeit (UTC+1).
        LocalDate sommerzeitMitStichtagInWinterzeit = LocalDate.of(2026, 6, 21);
        // 2026-01-21 ist Winterzeit (UTC+1), der Stichtag 2025-07-21 Sommerzeit (UTC+2).
        LocalDate winterzeitMitStichtagInSommerzeit = LocalDate.of(2026, 1, 21);
        // 2026-10-20 und der Stichtag 2026-04-20 liegen beide in der Sommerzeit (UTC+2). Die Sommerzeit dauert
        // laenger als sechs Monate (29.03. - 25.10.2026), darum gibt es auch beim Stichtag von sechs Monaten
        // Daten ohne Zeitzonenwechsel.
        LocalDate ohneZeitzonenwechsel = LocalDate.of(2026, 10, 20);

        return Stream.of(
            // Die alte Berechnung schob den Stichtag hier eine Stunde zurueck: der Download lag dadurch
            // faelschlicherweise nach dem Stichtag und das Dossier wurde nicht geloescht.
            Arguments.of("Sommer->Winter, Download eine Minute vor Stichtag",
                sommerzeitMitStichtagInWinterzeit, -1, true),
            Arguments.of("Sommer->Winter, Download eine Minute nach Stichtag",
                sommerzeitMitStichtagInWinterzeit, 1, false),
            Arguments.of("Winter->Sommer, Download eine Minute vor Stichtag",
                winterzeitMitStichtagInSommerzeit, -1, true),
            // Die alte Berechnung schob den Stichtag hier eine Stunde vor: der Download lag dadurch
            // faelschlicherweise vor dem Stichtag und das Dossier wurde faelschlicherweise geloescht.
            Arguments.of("Winter->Sommer, Download eine Minute nach Stichtag",
                winterzeitMitStichtagInSommerzeit, 1, false),
            Arguments.of("Ohne Zeitzonenwechsel, Download eine Minute vor Stichtag",
                ohneZeitzonenwechsel, -1, true),
            // Genau auf dem Stichtag: der Vergleich ist <, das Dossier wird nicht geloescht.
            Arguments.of("Ohne Zeitzonenwechsel, Download exakt auf Stichtag",
                ohneZeitzonenwechsel, 0, false),
            Arguments.of("Ohne Zeitzonenwechsel, Download eine Minute nach Stichtag",
                ohneZeitzonenwechsel, 1, false)
        );
    }

    /**
     * Fixe Testuhrzeit kurz nach Mitternacht: damit liegen Stichtag und Download am selben Tag und eine
     * Verschiebung um eine Stunde ist am Resultat ablesbar.
     */
    @NonNull
    private static OffsetDateTime atTestTime(@NonNull LocalDate datum) {
        return datum
            .atTime(LocalTime.of(0, 30))
            .atZone(DateConst.ZONE_ID)
            .toOffsetDateTime();
    }

    /**
     * Erstellt ein inaktives Kind mit Dossier und einem zum uebergebenen Zeitpunkt heruntergeladenen
     * Archivdokument, sodass einzig der Download-Zeitpunkt ueber das Resultat entscheidet.
     */
    @NonNull
    private Dossier createDossierReadyForDeletion(@NonNull OffsetDateTime timestampDownloaded) {
        Benutzer benutzer = fixtureService.createBenutzerFuerZentraleOrganisation();
        Kind kind = fixtureService.createKind(EntityAktivStatus.INAKTIV);
        Dossier dossier = fixtureService.createDossier(kind);

        Archivdokument archivdokument = ArchivdokumentBuilder.create(builder -> builder
            .withArchivierungTyp(ArchivierungTyp.KIND_DEAKTIVIERT)
            .withTimestampDownloaded(timestampDownloaded)
            .withOwner(benutzer)
            .withStatus(ArchivierungStatus.DOWNLOADED_READY_FOR_DELETION)
            .withDossierID(dossier.getId().getUUID()));
        archivdokumentService.persist(archivdokument);

        return dossier;
    }
}
