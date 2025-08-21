package ch.dvbern.oss.hemed.esc.api.features.anlass.massnahmen;

import java.util.Collections;
import java.util.List;
import java.util.Set;

import ch.dvbern.dvbstarter.infrastructure.errorhandling.AppValidationMessage;
import ch.dvbern.dvbstarter.shared.types.id.ID;
import ch.dvbern.oss.hemed.esc.api.features.anlass.Einladung;
import ch.dvbern.oss.hemed.esc.api.features.anlass.EinladungService;
import ch.dvbern.oss.hemed.esc.api.features.anlass.Untersuchung;
import ch.dvbern.oss.hemed.esc.api.features.anlass.UntersuchungService;
import ch.dvbern.oss.hemed.esc.api.features.infomaterial.Infomaterial;
import ch.dvbern.oss.hemed.esc.api.features.infomaterial.InfomaterialService;
import ch.dvbern.oss.hemed.esc.api.shared.aufgabe.Aufgabe;
import ch.dvbern.oss.hemed.esc.api.shared.aufgabe.AufgabeService;
import ch.dvbern.oss.hemed.esc.api.shared.aufgabe.types.AufgabeStatus;
import ch.dvbern.oss.hemed.esc.api.testutil.FixtureService;
import ch.dvbern.oss.hemed.esc.api.testutil.assertj.AppValidationErrorJsonSpecAssert;
import ch.dvbern.oss.hemed.esc.api.testutil.builder.InfomaterialBuilder;
import ch.dvbern.oss.hemed.esc.api.testutil.builder.UntersuchungMassnahmenBuilder;
import ch.dvbern.oss.hemed.esc.api.testutil.restassured.RequestSpecUtil;
import ch.dvbern.oss.hemed.esc.api.testutil.security.TestAsAdminBS;
import ch.dvbern.oss.hemed.esc.generated.restassured.api.UntersuchungMassnahmenApiSpec;
import ch.dvbern.oss.hemed.esc.generated.restassured.dto.AppValidationErrorJsonSpec;
import ch.dvbern.oss.hemed.esc.generated.restassured.dto.UntersuchungMassnahmenJsonSpec;
import ch.dvbern.oss.hemed.esc.generated.restassured.dto.UntersuchungMassnahmenSaveJsonSpec;
import io.quarkus.test.junit.QuarkusTest;
import io.restassured.response.ResponseBody;
import jakarta.ws.rs.core.Response.Status;
import lombok.RequiredArgsConstructor;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@QuarkusTest
@RequiredArgsConstructor
class UntersuchungMassnahmenResourceITest {
    private final UntersuchungMassnahmenApiSpec api =
        UntersuchungMassnahmenApiSpec.untersuchungMassnahmen(RequestSpecUtil.quarkusSpec());

    private final UntersuchungService untersuchungService;
    private final UntersuchungMassnahmenService untersuchungMassnahmenService;
    private final InfomaterialService infomaterialService;
    private final FixtureService fixtureService;
    private final EinladungService einladungService;
    private final AufgabeService aufgabeService;

    @Test
    @TestAsAdminBS
    void getUntersuchungMassnahmen_non_existing_should_not_create_data() {
        // ARRANGE
        Einladung einladung = fixtureService.createEinladung();
        untersuchungService.getOrCreateUntersuchung(einladung.getId());
        // ACT / ASSERT
        api
            .untersuchungMassnahmenResourceGetUntersuchungMassnahmen()
            .anlassIdPath(einladung.getAnlass().getId().getId())
            .einladungIdPath(einladung.getId().getId())
            .execute(ResponseBody::prettyPeek)
            .then()
            .assertThat()
            .statusCode(Status.NO_CONTENT.getStatusCode());
    }

    @Test
    @TestAsAdminBS
    void getUntersuchungMassnahmen_existing_should_return_existing() {
        // ARRANGE
        Untersuchung untersuchung = fixtureService.createUntersuchungWithMassnahmen();
        Assertions.assertThat(untersuchung.getUntersuchungMassnahmen()).isNotNull();

        UntersuchungMassnahmen existingMassnahmen =
            untersuchungMassnahmenService.getById(untersuchung.getUntersuchungMassnahmen().getId());
        Assertions.assertThat(existingMassnahmen).isNotNull();
        Assertions.assertThat(existingMassnahmen.getBemerkungen()).isNotNull();
        // ACT
        UntersuchungMassnahmenJsonSpec result = api
            .untersuchungMassnahmenResourceGetUntersuchungMassnahmen()
            .anlassIdPath(untersuchung.getEinladung().getAnlass().getId().getId())
            .einladungIdPath(untersuchung.getEinladung().getId().getId())
            .execute(ResponseBody::prettyPeek)
            .then()
            .assertThat()
            .statusCode(Status.OK.getStatusCode())
            .extract().as(UntersuchungMassnahmenJsonSpec.class);
        // ASSERT
        Assertions.assertThat(result).isNotNull();
        Assertions.assertThat(result.getId()).isEqualTo(existingMassnahmen.getId().getId());
        Assertions.assertThat(result.getData().getBemerkungen())
            .isNotNull()
            .isEqualTo(existingMassnahmen.getBemerkungen());
    }

    @Test
    @TestAsAdminBS
    void saveUntersuchungMassnahmen() {
        // ARRANGE
        Einladung einladung = fixtureService.createEinladung();
        Untersuchung untersuchung = fixtureService.createUntersuchungWithMassnahmen();
        Assertions.assertThat(untersuchung.getUntersuchungMassnahmen()).isNotNull();
        UntersuchungMassnahmen existingMassnahmen =
            untersuchungMassnahmenService.getById(untersuchung.getUntersuchungMassnahmen().getId());
        Assertions.assertThat(existingMassnahmen).isNotNull();
        Assertions.assertThat(existingMassnahmen.getBemerkungen()).isNotNull()
            .isEqualTo("Untersuchung Massnahmen Bemerkung");

        existingMassnahmen.setAbgegebenesInfomaterial(Collections.emptySet());

        // ACT
        UntersuchungMassnahmenSaveJsonSpec saveJsonSpec =
            UntersuchungMassnahmenBuilder.createJaxSpec(builder -> builder
                .forUntersuchungMassnahmen(existingMassnahmen)
                .withKeineMassnahmen(true)
                .withBeratungMPA(true)
                .withBeratungSchulAerztin(true)
                .withBemerkung("Test updateUntersuchungMassnahmen")).getData();

        UntersuchungMassnahmenJsonSpec result = api
            .untersuchungMassnahmenResourceSaveUntersuchungMassnahmen()
            .anlassIdPath(einladung.getAnlass().getId().getId())
            .einladungIdPath(einladung.getId().getId())
            .body(saveJsonSpec)
            .execute(ResponseBody::prettyPeek)
            .then()
            .assertThat()
            .statusCode(Status.OK.getStatusCode())
            .extract().as(UntersuchungMassnahmenJsonSpec.class);

        // ASSERT
        Assertions.assertThat(result).isNotNull();
        Assertions.assertThat(result.getId()).isNotNull();
        Assertions.assertThat(result.getData().getBemerkungen())
            .isNotNull()
            .isEqualTo("Test updateUntersuchungMassnahmen");
        Assertions.assertThat(result.getData().getKeineMassnahmen()).isTrue();
        Assertions.assertThat(result.getData().getBeratungMPA()).isTrue();
        Assertions.assertThat(result.getData().getBeratungSchulAerztin()).isTrue();
    }

    @Test
    @TestAsAdminBS
    void remove_inactive_infomaterial_throws_exception() {
        // ARRANGE
        Infomaterial inaktivesInfomaterial = infomaterialService.persist(InfomaterialBuilder.create(
            builder -> builder
                .isActive(false)
                .withRandomName("Inaktives Infomaterial")));
        Einladung einladung = fixtureService.createEinladung();
        Untersuchung untersuchung = untersuchungService.getOrCreateUntersuchung(einladung.getId());
        Assertions.assertThat(untersuchung).isNotNull();

        // ACT
        UntersuchungMassnahmenSaveJsonSpec saveJsonSpec =
            UntersuchungMassnahmenBuilder.createJaxSpec(builder -> builder
                .forUntersuchung(untersuchung)
                .withAbgegebenesInfomaterial(Set.of(inaktivesInfomaterial))
                .withBemerkung("Untersuchung Massnahmen Bemerkung")).getData();
        AppValidationErrorJsonSpec violation = api
            .untersuchungMassnahmenResourceSaveUntersuchungMassnahmen()
            .anlassIdPath(einladung.getAnlass().getId().getId())
            .einladungIdPath(einladung.getId().getId())
            .body(saveJsonSpec)
            .execute(ResponseBody::prettyPeek)
            .then()
            .assertThat()
            .statusCode(Status.BAD_REQUEST.getStatusCode())
            .extract()
            .as(AppValidationErrorJsonSpec.class);

        // ASSERT
        AppValidationErrorJsonSpecAssert.assertThatAppValidationErrorJsonSpec(violation)
            .extractingExactlyOneViolation()
            .hasClientKey(AppValidationMessage.inactiveInfomaterialCannotBeAddedOrRemoved().getClientKey())
            .hasRawMessageContaining("Deaktivierte Infomaterialien können nicht entfernt werden.");
    }

    @Test
    @TestAsAdminBS
    void infoElternBrief_should_create_aufgabe_with_bemerkung() {
        // ARRANGE
        Untersuchung untersuchung = fixtureService.createUntersuchungWithMassnahmen();
        assertThat(untersuchung.getUntersuchungMassnahmen()).isNotNull();
        assertThat(untersuchung.getUntersuchungMassnahmen().getAufgabeInformationEltern()).isNull();

        // ACT
        UntersuchungMassnahmenSaveJsonSpec saveJsonSpec =
            UntersuchungMassnahmenBuilder.createJaxSpec(builder -> builder
                .forUntersuchung(untersuchung)
                .withInformationEltern(InformationEltern.BRIEF)).getData();
        api
            .untersuchungMassnahmenResourceSaveUntersuchungMassnahmen()
            .anlassIdPath(untersuchung.getEinladung().getAnlass().getId().getId())
            .einladungIdPath(untersuchung.getEinladung().getId().getId())
            .body(saveJsonSpec)
            .execute(ResponseBody::prettyPeek)
            .then()
            .assertThat()
            .statusCode(Status.OK.getStatusCode());

        // ASSERT
        Untersuchung untersuchungSaved = fixtureService.getUntersuchungWithLoadedData(untersuchung.getEinladung().getId());
        assertThat(untersuchungSaved.getUntersuchungMassnahmen()).isNotNull();
        assertThat(untersuchungSaved.getUntersuchungMassnahmen().getAufgabeInformationEltern())
            .isNotNull()
            .extracting(Aufgabe::getAufgabeStatus).isEqualTo(AufgabeStatus.OFFEN);
        assertThat(untersuchungSaved.getUntersuchungMassnahmen().getAufgabeInformationEltern().getBemerkung())
            .isNotNull();
    }

    @Test
    @TestAsAdminBS
    void infoElternKeine_should_not_create_aufgabe() {
        // ARRANGE
        Untersuchung untersuchung = fixtureService.createUntersuchungWithMassnahmen();
        assertThat(untersuchung.getUntersuchungMassnahmen()).isNotNull();
        assertThat(untersuchung.getUntersuchungMassnahmen().getAufgabeInformationEltern()).isNull();

        // ACT
        UntersuchungMassnahmenSaveJsonSpec saveJsonSpec =
            UntersuchungMassnahmenBuilder.createJaxSpec(builder -> builder
                .forUntersuchung(untersuchung)
                .withInformationEltern(InformationEltern.KEINE)).getData();
        api
            .untersuchungMassnahmenResourceSaveUntersuchungMassnahmen()
            .anlassIdPath(untersuchung.getEinladung().getAnlass().getId().getId())
            .einladungIdPath(untersuchung.getEinladung().getId().getId())
            .body(saveJsonSpec)
            .execute(ResponseBody::prettyPeek)
            .then()
            .assertThat()
            .statusCode(Status.OK.getStatusCode());

        // ASSERT
        Untersuchung untersuchungSaved = fixtureService.getUntersuchungWithLoadedData(untersuchung.getEinladung().getId());
        assertThat(untersuchungSaved.getUntersuchungMassnahmen()).isNotNull();
        assertThat(untersuchungSaved.getUntersuchungMassnahmen().getAufgabeInformationEltern()).isNull();
    }

    @Test
    @TestAsAdminBS
    void infoElternNull_should_not_create_aufgabe() {
        // ARRANGE
        Untersuchung untersuchung = fixtureService.createUntersuchungWithMassnahmen();
        assertThat(untersuchung.getUntersuchungMassnahmen()).isNotNull();
        assertThat(untersuchung.getUntersuchungMassnahmen().getAufgabeInformationEltern()).isNull();

        // ACT
        UntersuchungMassnahmenSaveJsonSpec saveJsonSpec =
            UntersuchungMassnahmenBuilder.createJaxSpec(builder -> builder
                .forUntersuchung(untersuchung)
                .withInformationEltern(null)).getData();
        api
            .untersuchungMassnahmenResourceSaveUntersuchungMassnahmen()
            .anlassIdPath(untersuchung.getEinladung().getAnlass().getId().getId())
            .einladungIdPath(untersuchung.getEinladung().getId().getId())
            .body(saveJsonSpec)
            .execute(ResponseBody::prettyPeek)
            .then()
            .assertThat()
            .statusCode(Status.OK.getStatusCode());

        // ASSERT
        Untersuchung untersuchungSaved = fixtureService.getUntersuchungWithLoadedData(untersuchung.getEinladung().getId());
        assertThat(untersuchungSaved.getUntersuchungMassnahmen()).isNotNull();
        assertThat(untersuchungSaved.getUntersuchungMassnahmen().getAufgabeInformationEltern()).isNull();
    }

    @Test
    @TestAsAdminBS
    void ueberweisungHausarzt_true_should_create_aufgabe() {
        // ARRANGE
        Untersuchung untersuchung = fixtureService.createUntersuchungWithMassnahmen();
        assertThat(untersuchung.getUntersuchungMassnahmen()).isNotNull();
        assertThat(untersuchung.getUntersuchungMassnahmen().getAufgabeUeberweisungHausarzt()).isNull();

        // ACT
        UntersuchungMassnahmenSaveJsonSpec saveJsonSpec =
            UntersuchungMassnahmenBuilder.createJaxSpec(builder -> builder
                .forUntersuchung(untersuchung)
                .withUeberweisungHausarzt(true)).getData();
        api
            .untersuchungMassnahmenResourceSaveUntersuchungMassnahmen()
            .anlassIdPath(untersuchung.getEinladung().getAnlass().getId().getId())
            .einladungIdPath(untersuchung.getEinladung().getId().getId())
            .body(saveJsonSpec)
            .execute(ResponseBody::prettyPeek)
            .then()
            .assertThat()
            .statusCode(Status.OK.getStatusCode());

        // ASSERT
        Untersuchung untersuchungSaved = fixtureService.getUntersuchungWithLoadedData(untersuchung.getEinladung().getId());
        assertThat(untersuchungSaved.getUntersuchungMassnahmen()).isNotNull();
        assertThat(untersuchungSaved.getUntersuchungMassnahmen().getAufgabeUeberweisungHausarzt())
            .isNotNull()
            .extracting(Aufgabe::getAufgabeStatus).isEqualTo(AufgabeStatus.OFFEN);
        assertThat(untersuchungSaved.getUntersuchungMassnahmen().getAufgabeUeberweisungHausarzt().getBemerkung()).isNull();
    }

    @Test
    @TestAsAdminBS
    void ueberweisungHausarzt_false_should_not_create_aufgabe() {
        // ARRANGE
        Untersuchung untersuchung = fixtureService.createUntersuchungWithMassnahmen();
        assertThat(untersuchung.getUntersuchungMassnahmen()).isNotNull();
        assertThat(untersuchung.getUntersuchungMassnahmen().getAufgabeUeberweisungHausarzt()).isNull();

        // ACT
        UntersuchungMassnahmenSaveJsonSpec saveJsonSpec =
            UntersuchungMassnahmenBuilder.createJaxSpec(builder -> builder
                .forUntersuchung(untersuchung)
                .withUeberweisungHausarzt(false)).getData();
        api
            .untersuchungMassnahmenResourceSaveUntersuchungMassnahmen()
            .anlassIdPath(untersuchung.getEinladung().getAnlass().getId().getId())
            .einladungIdPath(untersuchung.getEinladung().getId().getId())
            .body(saveJsonSpec)
            .execute(ResponseBody::prettyPeek)
            .then()
            .assertThat()
            .statusCode(Status.OK.getStatusCode());

        // ASSERT
        Untersuchung untersuchungSaved = fixtureService.getUntersuchungWithLoadedData(untersuchung.getEinladung().getId());
        assertThat(untersuchungSaved.getUntersuchungMassnahmen()).isNotNull();
        assertThat(untersuchungSaved.getUntersuchungMassnahmen().getAufgabeUeberweisungHausarzt()).isNull();
    }

    @Test
    @TestAsAdminBS
    void ueberweisungAugenarzt_true_should_create_aufgabe() {
        // ARRANGE
        Untersuchung untersuchung = fixtureService.createUntersuchungWithMassnahmen();
        assertThat(untersuchung.getUntersuchungMassnahmen()).isNotNull();
        assertThat(untersuchung.getUntersuchungMassnahmen().getAufgabeUeberweisungAugenarzt()).isNull();

        // ACT
        UntersuchungMassnahmenSaveJsonSpec saveJsonSpec =
            UntersuchungMassnahmenBuilder.createJaxSpec(builder -> builder
                .forUntersuchung(untersuchung)
                .withUeberweisungAugenarzt(true)).getData();
        api
            .untersuchungMassnahmenResourceSaveUntersuchungMassnahmen()
            .anlassIdPath(untersuchung.getEinladung().getAnlass().getId().getId())
            .einladungIdPath(untersuchung.getEinladung().getId().getId())
            .body(saveJsonSpec)
            .execute(ResponseBody::prettyPeek)
            .then()
            .assertThat()
            .statusCode(Status.OK.getStatusCode());

        // ASSERT
        Untersuchung untersuchungSaved = fixtureService.getUntersuchungWithLoadedData(untersuchung.getEinladung().getId());
        assertThat(untersuchungSaved.getUntersuchungMassnahmen()).isNotNull();
        assertThat(untersuchungSaved.getUntersuchungMassnahmen().getAufgabeUeberweisungAugenarzt())
            .isNotNull()
            .extracting(Aufgabe::getAufgabeStatus).isEqualTo(AufgabeStatus.OFFEN);
        assertThat(untersuchungSaved.getUntersuchungMassnahmen().getAufgabeUeberweisungAugenarzt().getBemerkung()).isNull();
    }

    @Test
    @TestAsAdminBS
    void ueberweisungAugenarzt_false_should_not_create_aufgabe() {
        // ARRANGE
        Untersuchung untersuchung = fixtureService.createUntersuchungWithMassnahmen();
        assertThat(untersuchung.getUntersuchungMassnahmen()).isNotNull();
        assertThat(untersuchung.getUntersuchungMassnahmen().getAufgabeUeberweisungAugenarzt()).isNull();

        // ACT
        UntersuchungMassnahmenSaveJsonSpec saveJsonSpec =
            UntersuchungMassnahmenBuilder.createJaxSpec(builder -> builder
                .forUntersuchung(untersuchung)
                .withUeberweisungAugenarzt(false)).getData();
        api
            .untersuchungMassnahmenResourceSaveUntersuchungMassnahmen()
            .anlassIdPath(untersuchung.getEinladung().getAnlass().getId().getId())
            .einladungIdPath(untersuchung.getEinladung().getId().getId())
            .body(saveJsonSpec)
            .execute(ResponseBody::prettyPeek)
            .then()
            .assertThat()
            .statusCode(Status.OK.getStatusCode());

        // ASSERT
        Untersuchung untersuchungSaved = fixtureService.getUntersuchungWithLoadedData(untersuchung.getEinladung().getId());
        assertThat(untersuchungSaved.getUntersuchungMassnahmen()).isNotNull();
        assertThat(untersuchungSaved.getUntersuchungMassnahmen().getAufgabeUeberweisungAugenarzt()).isNull();
    }

    @Test
    @TestAsAdminBS
    void nachkontrolle_true_should_create_aufgabe() {
        // ARRANGE
        Untersuchung untersuchung = fixtureService.createUntersuchungWithMassnahmen();
        assertThat(untersuchung.getUntersuchungMassnahmen()).isNotNull();
        assertThat(untersuchung.getUntersuchungMassnahmen().getAufgabeNachkontrolle()).isNull();

        // ACT
        UntersuchungMassnahmenSaveJsonSpec saveJsonSpec =
            UntersuchungMassnahmenBuilder.createJaxSpec(builder -> builder
                .forUntersuchung(untersuchung)
                .withNachkontrolle(true, false, false, false, false, false)).getData();
        api
            .untersuchungMassnahmenResourceSaveUntersuchungMassnahmen()
            .anlassIdPath(untersuchung.getEinladung().getAnlass().getId().getId())
            .einladungIdPath(untersuchung.getEinladung().getId().getId())
            .body(saveJsonSpec)
            .execute(ResponseBody::prettyPeek)
            .then()
            .assertThat()
            .statusCode(Status.OK.getStatusCode());

        // ASSERT
        Untersuchung untersuchungSaved = fixtureService.getUntersuchungWithLoadedData(untersuchung.getEinladung().getId());
        assertThat(untersuchungSaved.getUntersuchungMassnahmen()).isNotNull();
        assertThat(untersuchungSaved.getUntersuchungMassnahmen().getAufgabeNachkontrolle())
            .isNotNull()
            .extracting(Aufgabe::getAufgabeStatus).isEqualTo(AufgabeStatus.OFFEN);
        assertThat(untersuchungSaved.getUntersuchungMassnahmen().getAufgabeNachkontrolle().getBemerkung()).isNull();
    }

    @Test
    @TestAsAdminBS
    void nachkontrolle_false_should_not_create_aufgabe() {
        // ARRANGE
        Untersuchung untersuchung = fixtureService.createUntersuchungWithMassnahmen();
        assertThat(untersuchung.getUntersuchungMassnahmen()).isNotNull();
        assertThat(untersuchung.getUntersuchungMassnahmen().getAufgabeNachkontrolle()).isNull();

        // ACT
        UntersuchungMassnahmenSaveJsonSpec saveJsonSpec =
            UntersuchungMassnahmenBuilder.createJaxSpec(builder -> builder
                .forUntersuchung(untersuchung)
                .withNachkontrolle(false, false, false, false, false, false)).getData();
        api
            .untersuchungMassnahmenResourceSaveUntersuchungMassnahmen()
            .anlassIdPath(untersuchung.getEinladung().getAnlass().getId().getId())
            .einladungIdPath(untersuchung.getEinladung().getId().getId())
            .body(saveJsonSpec)
            .execute(ResponseBody::prettyPeek)
            .then()
            .assertThat()
            .statusCode(Status.OK.getStatusCode());

        // ASSERT
        Untersuchung untersuchungSaved = fixtureService.getUntersuchungWithLoadedData(untersuchung.getEinladung().getId());
        assertThat(untersuchungSaved.getUntersuchungMassnahmen()).isNotNull();
        assertThat(untersuchungSaved.getUntersuchungMassnahmen().getAufgabeNachkontrolle()).isNull();
    }

    @Test
    @TestAsAdminBS
    void massnahmen_entfernen_true_should_close_aufgaben() {
        // ARRANGE
        Untersuchung untersuchung = fixtureService.createUntersuchungWithMassnahmen();
        assertThat(untersuchung.getUntersuchungMassnahmen()).isNotNull();
        assertThat(untersuchung.getUntersuchungMassnahmen().getAufgabeNachkontrolle()).isNull();

        UntersuchungMassnahmenSaveJsonSpec saveJsonSpec =
            UntersuchungMassnahmenBuilder.createJaxSpec(builder -> builder
                .forUntersuchung(untersuchung)
                .withInformationEltern(InformationEltern.BRIEF)
                .withUeberweisungHausarzt(true)
                .withUeberweisungAugenarzt(true)
                .withNachkontrolle(true, false, false, false, false, false)).getData();
        api
            .untersuchungMassnahmenResourceSaveUntersuchungMassnahmen()
            .anlassIdPath(untersuchung.getEinladung().getAnlass().getId().getId())
            .einladungIdPath(untersuchung.getEinladung().getId().getId())
            .body(saveJsonSpec)
            .execute(ResponseBody::prettyPeek)
            .then()
            .assertThat()
            .statusCode(Status.OK.getStatusCode());

        Untersuchung untersuchungSaved = fixtureService.getUntersuchungWithLoadedData(untersuchung.getEinladung().getId());
        assertThat(untersuchungSaved.getUntersuchungMassnahmen()).isNotNull();
        assertThat(untersuchungSaved.getUntersuchungMassnahmen().getAufgabeInformationEltern())
            .isNotNull()
            .extracting(Aufgabe::getAufgabeStatus).isEqualTo(AufgabeStatus.OFFEN);
        assertThat(untersuchungSaved.getUntersuchungMassnahmen().getAufgabeUeberweisungHausarzt())
            .isNotNull()
            .extracting(Aufgabe::getAufgabeStatus).isEqualTo(AufgabeStatus.OFFEN);
        assertThat(untersuchungSaved.getUntersuchungMassnahmen().getAufgabeUeberweisungAugenarzt())
            .isNotNull()
            .extracting(Aufgabe::getAufgabeStatus).isEqualTo(AufgabeStatus.OFFEN);
        assertThat(untersuchungSaved.getUntersuchungMassnahmen().getAufgabeNachkontrolle())
            .isNotNull()
            .extracting(Aufgabe::getAufgabeStatus).isEqualTo(AufgabeStatus.OFFEN);

        // ACT
        UntersuchungMassnahmenSaveJsonSpec updateJsonSpec =
            UntersuchungMassnahmenBuilder.createJaxSpec(builder -> builder
                .forUntersuchung(untersuchung)
                .withInformationEltern(InformationEltern.KEINE)
                .withUeberweisungHausarzt(false)
                .withUeberweisungAugenarzt(false)
                .withNachkontrolle(false, false, false, false, false, false)).getData();
        api
            .untersuchungMassnahmenResourceSaveUntersuchungMassnahmen()
            .anlassIdPath(untersuchung.getEinladung().getAnlass().getId().getId())
            .einladungIdPath(untersuchung.getEinladung().getId().getId())
            .body(updateJsonSpec)
            .execute(ResponseBody::prettyPeek)
            .then()
            .assertThat()
            .statusCode(Status.OK.getStatusCode());

        // ASSERT
        Untersuchung untersuchungUpdated = fixtureService.getUntersuchungWithLoadedData(untersuchung.getEinladung().getId());
        assertThat(untersuchungUpdated.getUntersuchungMassnahmen()).isNotNull();
        assertThat(untersuchungUpdated.getUntersuchungMassnahmen().getAufgabeInformationEltern())
            .isNotNull()
            .extracting(Aufgabe::getAufgabeStatus).isEqualTo(AufgabeStatus.ABGESCHLOSSEN);
        assertThat(untersuchungUpdated.getUntersuchungMassnahmen().getAufgabeUeberweisungHausarzt())
            .isNotNull()
            .extracting(Aufgabe::getAufgabeStatus).isEqualTo(AufgabeStatus.ABGESCHLOSSEN);
        assertThat(untersuchungUpdated.getUntersuchungMassnahmen().getAufgabeUeberweisungAugenarzt())
            .isNotNull()
            .extracting(Aufgabe::getAufgabeStatus).isEqualTo(AufgabeStatus.ABGESCHLOSSEN);
        assertThat(untersuchungUpdated.getUntersuchungMassnahmen().getAufgabeNachkontrolle())
            .isNotNull()
            .extracting(Aufgabe::getAufgabeStatus).isEqualTo(AufgabeStatus.ABGESCHLOSSEN);
    }

    @Test
    @TestAsAdminBS
    void einladung_entfernen_true_should_delete_aufgaben() {
        // ARRANGE
        Untersuchung untersuchung = fixtureService.createUntersuchungWithMassnahmen();
        assertThat(untersuchung.getUntersuchungMassnahmen()).isNotNull();
        assertThat(untersuchung.getUntersuchungMassnahmen().getAufgabeNachkontrolle()).isNull();

        UntersuchungMassnahmenSaveJsonSpec saveJsonSpec =
            UntersuchungMassnahmenBuilder.createJaxSpec(builder -> builder
                .forUntersuchung(untersuchung)
                .withInformationEltern(InformationEltern.BRIEF)
                .withUeberweisungHausarzt(true)
                .withUeberweisungAugenarzt(true)
                .withNachkontrolle(true, false, false, false, false, false)).getData();
        api
            .untersuchungMassnahmenResourceSaveUntersuchungMassnahmen()
            .anlassIdPath(untersuchung.getEinladung().getAnlass().getId().getId())
            .einladungIdPath(untersuchung.getEinladung().getId().getId())
            .body(saveJsonSpec)
            .execute(ResponseBody::prettyPeek)
            .then()
            .assertThat()
            .statusCode(Status.OK.getStatusCode());

        assertThat(aufgabeService.findAllForKind(untersuchung.getEinladung().getDossier().getKind().getId())).hasSize(4);
        Untersuchung untersuchungSaved = fixtureService.getUntersuchungWithLoadedData(untersuchung.getEinladung().getId());
        assertThat(untersuchungSaved.getUntersuchungMassnahmen()).isNotNull();
        Aufgabe aufgabeInformationEltern = untersuchungSaved.getUntersuchungMassnahmen().getAufgabeInformationEltern();
        Aufgabe aufgabeUeberweisungHausarzt = untersuchungSaved.getUntersuchungMassnahmen().getAufgabeUeberweisungHausarzt();
        Aufgabe aufgabeUeberweisungAugenarzt = untersuchungSaved.getUntersuchungMassnahmen().getAufgabeUeberweisungAugenarzt();
        Aufgabe aufgabeNachkontrolle = untersuchungSaved.getUntersuchungMassnahmen().getAufgabeNachkontrolle();

        assertThat(aufgabeInformationEltern).isNotNull();
        assertThat(aufgabeUeberweisungHausarzt).isNotNull();
        assertThat(aufgabeUeberweisungAugenarzt).isNotNull();
        assertThat(aufgabeNachkontrolle).isNotNull();

        // ACT
        List<ID<Einladung>> einladungIdList = List.of(untersuchung.getEinladung().getId());
        einladungService.removeEinladungen(einladungIdList);

        // ASSERT
        assertThat(aufgabeService.findAllForKind(untersuchung.getEinladung().getDossier().getKind().getId())).isEmpty();
    }
}
