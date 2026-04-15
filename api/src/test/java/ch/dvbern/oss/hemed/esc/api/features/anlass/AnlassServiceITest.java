package ch.dvbern.oss.hemed.esc.api.features.anlass;

import ch.dvbern.dvbstarter.infrastructure.errorhandling.AppValidationException;
import ch.dvbern.dvbstarter.shared.types.id.ID;
import ch.dvbern.oss.hemed.esc.api.features.anlass.anlassconfig.AnlassConfig;
import ch.dvbern.oss.hemed.esc.api.features.anlass.anlassconfig.AnlassConfigService;
import ch.dvbern.oss.hemed.esc.api.features.anlass.einladung.Einladung;
import ch.dvbern.oss.hemed.esc.api.features.anlass.types.AnlassStatus;
import ch.dvbern.oss.hemed.esc.api.features.anlass.types.AnlassTyp;
import ch.dvbern.oss.hemed.esc.api.features.anlass.einladung.types.EinladungStatus;
import ch.dvbern.oss.hemed.esc.api.features.anlass.untersuchung.Untersuchung;
import ch.dvbern.oss.hemed.esc.api.features.anlass.untersuchung.UntersuchungService;
import ch.dvbern.oss.hemed.esc.api.features.benutzer.BenutzerService;
import ch.dvbern.oss.hemed.esc.api.features.kinder.Dossier;
import ch.dvbern.oss.hemed.esc.api.features.kinder.Kind;
import ch.dvbern.oss.hemed.esc.api.features.kinder.Nachkontrolle;
import ch.dvbern.oss.hemed.esc.api.features.kinder.NachkontrolleService;
import ch.dvbern.oss.hemed.esc.api.features.labels.Label;
import ch.dvbern.oss.hemed.esc.api.features.labels.LabelService;
import ch.dvbern.oss.hemed.esc.api.shared.anlass.anlassflags.AnlassFlags;
import ch.dvbern.oss.hemed.esc.api.shared.aufgabe.Aufgabe;
import ch.dvbern.oss.hemed.esc.api.shared.aufgabe.AufgabeService;
import ch.dvbern.oss.hemed.esc.api.shared.aufgabe.types.AufgabeStatus;
import ch.dvbern.oss.hemed.esc.api.shared.benutzer.Benutzer;
import ch.dvbern.oss.hemed.esc.api.testutil.AbstractITest;
import ch.dvbern.oss.hemed.esc.api.testutil.FixtureService;
import ch.dvbern.oss.hemed.esc.api.testutil.builder.AnlassBuilder;
import ch.dvbern.oss.hemed.esc.api.testutil.builder.NachkontrolleBuilder;
import ch.dvbern.oss.hemed.esc.api.testutil.builder.UntersuchungMassnahmenBuilder;
import ch.dvbern.oss.hemed.esc.api.testutil.restassured.RequestSpecUtil;
import ch.dvbern.oss.hemed.esc.api.testutil.security.TestAsAdminBS;
import ch.dvbern.oss.hemed.esc.api.testutil.security.TestAsMpaBS;
import ch.dvbern.oss.hemed.esc.api.testutil.security.TestAsSchularztBS;
import ch.dvbern.oss.hemed.esc.generated.restassured.api.UntersuchungMassnahmenApiSpec;
import ch.dvbern.oss.hemed.esc.generated.restassured.dto.UntersuchungMassnahmenSaveJsonSpec;
import io.quarkus.test.junit.QuarkusTest;
import io.restassured.response.ResponseBody;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.core.Response.Status;
import java.time.LocalDate;
import java.time.Month;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@QuarkusTest
@RequiredArgsConstructor
class AnlassServiceITest extends AbstractITest {

    private final UntersuchungMassnahmenApiSpec apiUntersuchungMassnahmen =
        UntersuchungMassnahmenApiSpec.untersuchungMassnahmen(RequestSpecUtil.quarkusSpec());

    private final FixtureService fixtureService;
    private final AnlassService anlassService;
    private final AnlassConfigService anlassConfigService;
    private final AufgabeService aufgabeService;
    private final LabelService labelService;
    private final UntersuchungService untersuchungService;
    private final NachkontrolleService nachkontrolleService;
    private final BenutzerService benutzerService;



    @Test
    @TestAsAdminBS
    void changeAnlassStatus_beenden_should_copy_kinddaten() {
        // ARRANGE
        Anlass anlass = fixtureService.createAnlass();
        assertThat(anlass.getAnlassStatus()).isEqualTo(AnlassStatus.IN_PLANUNG);
        // Einladung 1 hat noch keine Untersuchung
        Einladung einladung1 = fixtureService.createEinladung(anlass);
        assertThat(einladung1.getUntersuchung()).isNull();
        // Einladung 2 hat bereits eine Untersuchung, aber noch keine KindDaten
        Einladung einladung2 = fixtureService.createEinladung(anlass);
        Untersuchung untersuchung2Vorher = untersuchungService.getOrCreateUntersuchung(einladung2.getId());
        assertThat(untersuchung2Vorher).isNotNull().extracting(Untersuchung::getUntersuchungKindDaten).isNull();

        // ACT
        Anlass result = anlassService.changeAnlassStatus(anlass.getId(), AnlassStatus.ABGESCHLOSSEN);

        // ASSERT
        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(anlass.getId());
        assertThat(result.getAnlassStatus()).isEqualTo(AnlassStatus.ABGESCHLOSSEN);
        Untersuchung untersuchung1Beendet = untersuchungService.getOrCreateUntersuchung(einladung1.getId());
        assertThat(untersuchung1Beendet).isNotNull().extracting(Untersuchung::getUntersuchungKindDaten).isNotNull();
        Untersuchung untersuchung2Beendet = untersuchungService.getOrCreateUntersuchung(einladung2.getId());
        assertThat(untersuchung2Beendet).isNotNull().extracting(Untersuchung::getUntersuchungKindDaten).isNotNull();
    }

    @Test
    @TestAsAdminBS
    void changeAnlassStatus_beenden_if_already_copied_should_not_override() {
        // ARRANGE
        Anlass anlass = fixtureService.createAnlass();
        assertThat(anlass.getAnlassStatus()).isEqualTo(AnlassStatus.IN_PLANUNG);
        Einladung einladung = fixtureService.createEinladung(anlass);
        String nameBeforeChange = "Alter Name";
        Kind kindBeforeBeenden = fixtureService.changeNameOfKind(einladung.getDossier().getKind().getId(), nameBeforeChange);
        assertThat(kindBeforeBeenden).isNotNull().extracting(Kind::getName).isEqualTo(nameBeforeChange);
        // Anlass das erste Mal beenden
        anlassService.changeAnlassStatus(anlass.getId(), AnlassStatus.ABGESCHLOSSEN);
        assertThat(fixtureService.getUntersuchungWithLoadedData(einladung.getId()))
            .isNotNull()
            .extracting(Untersuchung::getUntersuchungKindDaten).isNotNull()
            .extracting(untersuchungKindDaten -> untersuchungKindDaten != null ? untersuchungKindDaten.getName() : null).isEqualTo(nameBeforeChange);
        // Anlass wieder öffnen
        anlassService.changeAnlassStatus(anlass.getId(), AnlassStatus.DURCHGEFUEHRT);
        // Name des Kindes anpassen
        String nameAfterBeendung = "Neuer Name";
        Kind kindNachBeenden = fixtureService.changeNameOfKind(einladung.getDossier().getKind().getId(), nameAfterBeendung);
        assertThat(kindNachBeenden).isNotNull().extracting(Kind::getName).isEqualTo(nameAfterBeendung);

        // ACT: ein zweites Mal beenden
        anlassService.changeAnlassStatus(anlass.getId(), AnlassStatus.ABGESCHLOSSEN);

        // ASSERT: Daten wurden nicht ueberschrieben
        assertThat(fixtureService.getUntersuchungWithLoadedData(einladung.getId()))
            .isNotNull()
            .extracting(Untersuchung::getUntersuchungKindDaten).isNotNull()
            .extracting(untersuchungKindDaten -> untersuchungKindDaten != null ? untersuchungKindDaten.getName() : null).isEqualTo(nameBeforeChange);
    }

    @Test
    @TestAsAdminBS
    @Transactional
    void changeAnlassStatus_durchgefuehrt_should_create_Pendenz() {
        // ARRANGE - prepare data and expected data
        Benutzer mpa = fixtureService.createBenutzer();
        Anlass anlass = fixtureService.createAnlass(mpa);
        String expectedTitel = "Gruppen-Folgetermin erstellen - 2022/23 SU 2.SEK Test getAnlass";
        LocalDate expectedFaelligBis = getAppClock().localDateTime().now().toLocalDate();
        Label expectedLabel = labelService.getById(Label.ID_LABEL_GRUPPEN_FOLGETERMIN);
        assertThat(anlass.getAnlassStatus()).isEqualTo(AnlassStatus.IN_PLANUNG);
        assertThat(aufgabeService.findAufgabenForAnlassAndLabel(anlass, Label.ID_LABEL_GRUPPEN_FOLGETERMIN)).isEmpty();

        // ACT - change the Anlassstatus and find the generated Aufgabe
        Anlass updatedAnlass = anlassService.changeAnlassStatus(anlass.getId(), AnlassStatus.DURCHGEFUEHRT);
        List<Aufgabe> result = aufgabeService.findAufgabenForAnlassAndLabel(updatedAnlass, Label.ID_LABEL_GRUPPEN_FOLGETERMIN);

        // ASSERT - assert everything got updated the right way
        assertThat(updatedAnlass).isNotNull();
        assertThat(updatedAnlass.getMpa()).isNotNull();
        assertThat(updatedAnlass.getId()).isEqualTo(anlass.getId());
        assertThat(updatedAnlass.getAnlassStatus()).isEqualTo(AnlassStatus.DURCHGEFUEHRT);
        assertThat(result).isNotEmpty().hasSize(1);
        assertThat(result.getFirst()).isNotNull();
        assertThat(result.getFirst().getAnlass()).isNotNull();
        assertThat(result.getFirst().getAnlass().getId()).isEqualTo(updatedAnlass.getId());
        assertThat(result.getFirst().getTitel()).isEqualTo(expectedTitel);
        assertThat(result.getFirst().getFaelligBis()).isEqualTo(expectedFaelligBis);
        assertThat(result.getFirst().getEmpfaenger()).isEqualTo(mpa);
        assertThat(result.getFirst().getUserErfasst().getId().getId()).isEqualTo(TestAsAdminBS.BENUTZER_ID);
        assertThat(result.getFirst().getLabels()).contains(expectedLabel);
    }

    @Test
    @TestAsAdminBS
    @Transactional
    void changeAnlassStatus_durchgefuehrt_should_update_Pendenz_if_already_exists() {
        // ARRANGE - prepare data and expected data
        Benutzer mpa = fixtureService.createBenutzer();
        Benutzer mpa2 = fixtureService.createBenutzer();
        Anlass anlass = fixtureService.createAnlass(mpa);
        Label demoLabel = labelService.getById(Label.ID_LABEL_DISPENSATIONSGESUCH);
        String originalTitel = "Gruppen-Folgetermin erstellen - 2022/23 SU 2.SEK Test getAnlass";
        String expectedTitel = "Gruppen-Folgetermin erstellen - 2023/24 SU 2.SEK Test getAnlass";
        String expectedBemerkung = "Bemerkung zum Folgetermin";
        LocalDate expectedFaelligBis = getAppClock().localDateTime().now().toLocalDate();
        LocalDate otherFaelligBis = expectedFaelligBis.plusMonths(2);
        Integer expectedSchuljahr = 2023;
        Label expectedLabel = labelService.getById(Label.ID_LABEL_GRUPPEN_FOLGETERMIN);

        // ACT - change the anlassStatus to DURCHGEFUEHRT for the first time
        anlassService.changeAnlassStatus(anlass.getId(), AnlassStatus.DURCHGEFUEHRT);
        List<Aufgabe> aufgabe = aufgabeService.findAufgabenForAnlassAndLabel(anlass, Label.ID_LABEL_GRUPPEN_FOLGETERMIN);
        // ASSERT - assert the aufgabe is generated correctly
        assertThat(aufgabe).isNotEmpty().hasSize(1);
        assertThat(aufgabe.getFirst().getTitel()).isEqualTo(originalTitel);
        assertThat(aufgabe.getFirst().getUserErfasst().getId().getId()).isEqualTo(TestAsAdminBS.BENUTZER_ID);
        assertThat(aufgabe.getFirst().getEmpfaenger()).isEqualTo(mpa);
        assertThat(aufgabe.getFirst().getLabels()).contains(expectedLabel);

        Aufgabe changedAufgabe = aufgabe.getFirst();
        changedAufgabe.setBemerkung(expectedBemerkung);
        changedAufgabe.setAufgabeStatus(AufgabeStatus.IN_BEARBEITUNG);
        changedAufgabe.setFaelligBis(otherFaelligBis);
        Set<Label> labels = new HashSet<>(changedAufgabe.getLabels());
        labels.add(demoLabel);
        changedAufgabe.setLabels(labels);
        anlass.setMpa(mpa2);
        anlass.setSchuljahr(expectedSchuljahr);

        // ACT - Save previous changes and change anlassStatus to DURCHGEFUEHRT second time
        anlassService.persist(anlass);
        aufgabeService.persist(changedAufgabe);
        Anlass updatedAnlass = anlassService.changeAnlassStatus(anlass.getId(), AnlassStatus.DURCHGEFUEHRT);
        List<Aufgabe> result = aufgabeService.findAufgabenForAnlassAndLabel(updatedAnlass, Label.ID_LABEL_GRUPPEN_FOLGETERMIN);

        // ASSERT
        assertThat(updatedAnlass).isNotNull();
        assertThat(updatedAnlass.getMpa()).isNotNull();
        assertThat(updatedAnlass.getId()).isEqualTo(anlass.getId());
        assertThat(updatedAnlass.getAnlassStatus()).isEqualTo(AnlassStatus.DURCHGEFUEHRT);
        assertThat(result).isNotEmpty().hasSize(1);
        assertThat(result.getFirst()).isNotNull();
        assertThat(result.getFirst().getAnlass()).isNotNull();
        assertThat(result.getFirst().getAnlass().getId()).isEqualTo(updatedAnlass.getId());
        // should update Titel because Schuljahr was updated
        assertThat(result.getFirst().getTitel()).isEqualTo(expectedTitel);
        // should update faelligBis
        assertThat(result.getFirst().getFaelligBis()).isEqualTo(expectedFaelligBis);
        // should NOT update empfaenger
        assertThat(result.getFirst().getEmpfaenger()).isEqualTo(mpa);
        // should not update userErfasst
        assertThat(result.getFirst().getUserErfasst().getId().getId()).isEqualTo(TestAsAdminBS.BENUTZER_ID);
        // should not override previously added Labels
        assertThat(result.getFirst().getLabels()).contains(expectedLabel);
        assertThat(result.getFirst().getLabels()).contains(demoLabel);
        // should not update Bemerkung
        assertThat(result.getFirst().getBemerkung()).isEqualTo(expectedBemerkung);
    }


    @Test
    @TestAsAdminBS
    void changeAnlassStatus_durchgefuehrt_should_throwException_ifNoMPAOnAnlass() {
        // ARRANGE
        Anlass anlass = fixtureService.createAnlass(AnlassTyp.KLASSE_8);
        ID<Anlass> anlassId = anlass.getId();

        // ACT & ASSERT
        assertThat(anlass).isNotNull();
        assertThat(anlass.getMpa()).isNull();
        assertThat(anlass.getId()).isNotNull();
        assertThatThrownBy(() -> anlassService.changeAnlassStatus(anlassId, AnlassStatus.DURCHGEFUEHRT))
            .isInstanceOf(AppValidationException.class)
            .hasMessageContaining("AppValidation.NO_MPA_FOR_AUFGABE_EMPFAENGER");
    }

    @Test
    @TestAsAdminBS
    void nachkontrolle_einziger_anlass_should_copy_nachkontrolle_to_dossier() {
        // ARRANGE
        Untersuchung untersuchung = fixtureService.createUntersuchungWithMassnahmen();
        assertThat(untersuchung.getUntersuchungMassnahmen()).isNotNull();

        Nachkontrolle nachkontrolleVorher =
            nachkontrolleService.getOrCreateByDossier(untersuchung.getEinladung().getDossier().getId());
        AnlassFlags anlassFlagsVorher = nachkontrolleVorher.getAnlassFlags();
        assertThat(anlassFlagsVorher).extracting(AnlassFlags::isNachkontrolleSehen).isEqualTo(false);
        assertThat(anlassFlagsVorher).extracting(AnlassFlags::isNachkontrolleHoeren).isEqualTo(false);
        assertThat(anlassFlagsVorher).extracting(AnlassFlags::isNachkontrolleGroesseGewicht).isEqualTo(false);
        assertThat(anlassFlagsVorher).extracting(AnlassFlags::isNachkontrolleSprache).isEqualTo(false);
        assertThat(anlassFlagsVorher).extracting(AnlassFlags::isNachkontrolleMotorik).isEqualTo(false);
        assertThat(anlassFlagsVorher).extracting(AnlassFlags::isNachkontrolleAnderes).isEqualTo(false);

        UntersuchungMassnahmenSaveJsonSpec saveJsonSpec =
            UntersuchungMassnahmenBuilder.createJaxSpec(builder -> builder
                .forUntersuchung(untersuchung)
                .withNachkontrolle(true, true, false, false, false, false)).getData();
        apiUntersuchungMassnahmen
            .untersuchungMassnahmenResourceSaveUntersuchungMassnahmen()
            .anlassIdPath(untersuchung.getEinladung().getAnlass().getId().getId())
            .einladungIdPath(untersuchung.getEinladung().getId().getId())
            .body(saveJsonSpec)
            .execute(ResponseBody::prettyPeek)
            .then()
            .assertThat()
            .statusCode(Status.OK.getStatusCode());

        // ACT
        anlassService.changeAnlassStatus(untersuchung.getEinladung().getAnlass().getId(), AnlassStatus.ABGESCHLOSSEN);

        // ASSERT
        Nachkontrolle nachkontrolle =
            nachkontrolleService.getOrCreateByDossier(untersuchung.getEinladung().getDossier().getId());
        AnlassFlags anlassFlags = nachkontrolle.getAnlassFlags();
        assertThat(anlassFlags).extracting(AnlassFlags::isNachkontrolleSehen).isEqualTo(true);
        assertThat(anlassFlags).extracting(AnlassFlags::isNachkontrolleHoeren).isEqualTo(true);
        assertThat(anlassFlags).extracting(AnlassFlags::isNachkontrolleGroesseGewicht).isEqualTo(false);
        assertThat(anlassFlags).extracting(AnlassFlags::isNachkontrolleSprache).isEqualTo(false);
        assertThat(anlassFlags).extracting(AnlassFlags::isNachkontrolleMotorik).isEqualTo(false);
        assertThat(anlassFlags).extracting(AnlassFlags::isNachkontrolleAnderes).isEqualTo(false);
    }

    @Test
    @TestAsAdminBS
    void nachkontrolle_frueherer_anlass_should_not_copy_nachkontrolle_to_dossier() {
        // ARRANGE
        final Dossier dossier = fixtureService.createDossier();
        Untersuchung untersuchung1 = fixtureService.createUntersuchungWithMassnahmen(LocalDate.of(2025, 1, 1), dossier);
        Untersuchung untersuchung2 = fixtureService.createUntersuchungWithMassnahmen(LocalDate.of(2026, 1, 1), dossier);
        Untersuchung untersuchung3 = fixtureService.createUntersuchungWithMassnahmen(LocalDate.of(2027, 1, 1), dossier);

        UntersuchungMassnahmenSaveJsonSpec saveJsonSpec =
            UntersuchungMassnahmenBuilder.createJaxSpec(builder -> builder
                .forUntersuchung(untersuchung2)
                .withNachkontrolle(true, true, false, false, false, false)).getData();
        apiUntersuchungMassnahmen
            .untersuchungMassnahmenResourceSaveUntersuchungMassnahmen()
            .anlassIdPath(untersuchung2.getEinladung().getAnlass().getId().getId())
            .einladungIdPath(untersuchung2.getEinladung().getId().getId())
            .body(saveJsonSpec)
            .execute(ResponseBody::prettyPeek)
            .then()
            .assertThat()
            .statusCode(Status.OK.getStatusCode());

        // ACT
        anlassService.changeAnlassStatus(untersuchung2.getEinladung().getAnlass().getId(), AnlassStatus.ABGESCHLOSSEN);

        // ASSERT
        Nachkontrolle nachkontrolle =
            nachkontrolleService.getOrCreateByDossier(untersuchung2.getEinladung().getDossier().getId());
        AnlassFlags anlassFlags = nachkontrolle.getAnlassFlags();
        assertThat(anlassFlags).extracting(AnlassFlags::isNachkontrolleSehen).isEqualTo(false);
        assertThat(anlassFlags).extracting(AnlassFlags::isNachkontrolleHoeren).isEqualTo(false);
        assertThat(anlassFlags).extracting(AnlassFlags::isNachkontrolleGroesseGewicht).isEqualTo(false);
        assertThat(anlassFlags).extracting(AnlassFlags::isNachkontrolleSprache).isEqualTo(false);
        assertThat(anlassFlags).extracting(AnlassFlags::isNachkontrolleMotorik).isEqualTo(false);
        assertThat(anlassFlags).extracting(AnlassFlags::isNachkontrolleAnderes).isEqualTo(false);
    }

    @Test
    @TestAsAdminBS
    void nachkontrolle_neuester_anlass_should_copy_nachkontrolle_to_dossier() {
        // ARRANGE
        final Dossier dossier = fixtureService.createDossier();
        Untersuchung untersuchung1 = fixtureService.createUntersuchungWithMassnahmen(LocalDate.of(2025, 1, 1), dossier);
        Untersuchung untersuchung2 = fixtureService.createUntersuchungWithMassnahmen(LocalDate.of(2026, 1, 1), dossier);
        Untersuchung untersuchung3 = fixtureService.createUntersuchungWithMassnahmen(LocalDate.of(2027, 1, 1), dossier);

        UntersuchungMassnahmenSaveJsonSpec saveJsonSpec =
            UntersuchungMassnahmenBuilder.createJaxSpec(builder -> builder
                .forUntersuchung(untersuchung3)
                .withNachkontrolle(true, true, false, false, false, false)).getData();
        apiUntersuchungMassnahmen
            .untersuchungMassnahmenResourceSaveUntersuchungMassnahmen()
            .anlassIdPath(untersuchung3.getEinladung().getAnlass().getId().getId())
            .einladungIdPath(untersuchung3.getEinladung().getId().getId())
            .body(saveJsonSpec)
            .execute(ResponseBody::prettyPeek)
            .then()
            .assertThat()
            .statusCode(Status.OK.getStatusCode());

        // ACT
        anlassService.changeAnlassStatus(untersuchung3.getEinladung().getAnlass().getId(), AnlassStatus.ABGESCHLOSSEN);

        // ASSERT
        Nachkontrolle nachkontrolle =
            nachkontrolleService.getOrCreateByDossier(untersuchung3.getEinladung().getDossier().getId());
        AnlassFlags anlassFlags = nachkontrolle.getAnlassFlags();
        assertThat(anlassFlags).extracting(AnlassFlags::isNachkontrolleSehen).isEqualTo(true);
        assertThat(anlassFlags).extracting(AnlassFlags::isNachkontrolleHoeren).isEqualTo(true);
        assertThat(anlassFlags).extracting(AnlassFlags::isNachkontrolleGroesseGewicht).isEqualTo(false);
        assertThat(anlassFlags).extracting(AnlassFlags::isNachkontrolleSprache).isEqualTo(false);
        assertThat(anlassFlags).extracting(AnlassFlags::isNachkontrolleMotorik).isEqualTo(false);
        assertThat(anlassFlags).extracting(AnlassFlags::isNachkontrolleAnderes).isEqualTo(false);
    }

    @Test
    @TestAsAdminBS
    void changeAnlassStatus_beenden_without_saved_durchfuehrung_should_save_defaults() {
        // ARRANGE
        // Defaults betreffen: Arzt, MPA, Nachkontrolle, Nachzuegler, Neueintritt
        Dossier dossier = fixtureService.createDossier();
        nachkontrolleService.persist(NachkontrolleBuilder.create(builder -> builder
            .forDossier(dossier)
            .withNachkontrolleSehen(true)
            .withNachkontrolleHoeren(true)));
        AnlassConfig config =
            anlassConfigService.getByAnlassTypAndStichtag(AnlassTyp.KLASSE_4, LocalDate.of(2025, Month.JANUARY, 1));
        final Anlass anlassNichtTeilgenommen = anlassService.persist(
            AnlassBuilder.create(builder -> builder
                .withAnlassConfig(config)
                .withDatumSetSchuljahr(LocalDate.of(2025, Month.JANUARY, 1))
                .withAnlassTyp(AnlassTyp.KLASSE_4)));
        fixtureService.createEinladung(anlassNichtTeilgenommen, dossier, EinladungStatus.NACHZUEGLER);

        Benutzer mpa = benutzerService.getById(TestAsMpaBS.BENUTZER_ID_ID);
        Benutzer arzt = benutzerService.getById(TestAsSchularztBS.BENUTZER_ID_ID);
        final Anlass anlass = anlassService.persist(
            AnlassBuilder.create(builder -> builder
                .withDatumSetSchuljahr(LocalDate.of(2025, Month.FEBRUARY, 1))
                .withAnlassTyp(AnlassTyp.KLASSE_4)
                .withAnlassConfig(config)
                .withArzt(arzt)
                .withMPA(mpa)));

        Einladung einladung = fixtureService.createEinladung(anlass, dossier, EinladungStatus.NACHZUEGLER);
        Untersuchung untersuchung = untersuchungService.getOrCreateUntersuchung(einladung.getId());
        assertThat(untersuchung.getUntersuchungKindDaten()).isNull();
        assertThat(untersuchung.getUntersuchungDurchfuehrung()).isNull();

        // ACT
        anlassService.changeAnlassStatus(anlass.getId(), AnlassStatus.ABGESCHLOSSEN);

        // ASSERT
        Untersuchung untersuchungNachAbschluss = fixtureService.getUntersuchungWithLoadedData(einladung.getId());
        assertThat(untersuchungNachAbschluss.getUntersuchungKindDaten()).isNotNull();
        assertThat(untersuchungNachAbschluss.getUntersuchungDurchfuehrung()).isNotNull();
        assertThat(untersuchungNachAbschluss.getUntersuchungDurchfuehrung().getArzt()).isEqualTo(arzt);
        assertThat(untersuchungNachAbschluss.getUntersuchungDurchfuehrung().getMpa()).isEqualTo(mpa);
        assertThat(untersuchungNachAbschluss.getUntersuchungDurchfuehrung().isNachkontrolle()).isTrue();
        assertThat(untersuchungNachAbschluss.getUntersuchungDurchfuehrung().isNachzuegler()).isTrue();
        assertThat(untersuchungNachAbschluss.getUntersuchungDurchfuehrung().isNeueintritt()).isFalse();
    }
}
