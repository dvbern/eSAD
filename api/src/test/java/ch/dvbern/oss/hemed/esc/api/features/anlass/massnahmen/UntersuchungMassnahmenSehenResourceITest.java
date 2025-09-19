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
import ch.dvbern.oss.hemed.esc.api.shared.auth.TenantEnum;
import ch.dvbern.oss.hemed.esc.api.shared.auth.TenantProducer;
import ch.dvbern.oss.hemed.esc.api.shared.benutzer.Benutzer;
import ch.dvbern.oss.hemed.esc.api.testutil.FixtureService;
import ch.dvbern.oss.hemed.esc.api.testutil.assertj.AppValidationErrorJsonSpecAssert;
import ch.dvbern.oss.hemed.esc.api.testutil.builder.InfomaterialBuilder;
import ch.dvbern.oss.hemed.esc.api.testutil.builder.UntersuchungMassnahmenBuilder;
import ch.dvbern.oss.hemed.esc.api.testutil.restassured.RequestSpecUtil;
import ch.dvbern.oss.hemed.esc.api.testutil.security.TestAsAdminBS;
import ch.dvbern.oss.hemed.esc.generated.restassured.api.UntersuchungMassnahmenApiSpec;
import ch.dvbern.oss.hemed.esc.generated.restassured.dto.AppValidationErrorJsonSpec;
import ch.dvbern.oss.hemed.esc.generated.restassured.dto.UntersuchungSehenMassnahmenJsonSpec;
import ch.dvbern.oss.hemed.esc.generated.restassured.dto.UntersuchungSehenMassnahmenSaveJsonSpec;
import io.quarkus.test.junit.QuarkusTest;
import io.restassured.response.ResponseBody;
import jakarta.ws.rs.core.Response.Status;
import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@QuarkusTest
@RequiredArgsConstructor
class UntersuchungMassnahmenSehenResourceITest {
    private final UntersuchungMassnahmenApiSpec api =
        UntersuchungMassnahmenApiSpec.untersuchungMassnahmen(RequestSpecUtil.quarkusSpec());

    private final UntersuchungService untersuchungService;
    private final UntersuchungMassnahmenService untersuchungMassnahmenService;
    private final InfomaterialService infomaterialService;
    private final FixtureService fixtureService;
    private final EinladungService einladungService;
    private final AufgabeService aufgabeService;
    private final TenantProducer tenantProducer;

    @BeforeEach
    void setUp() {
        tenantProducer.setCurrentTenant(TenantEnum.BS);
    }

    @Test
    @TestAsAdminBS
    void getUntersuchungMassnahmen_non_existing_should_not_create_data() {
        // ARRANGE
        Einladung einladung = fixtureService.createEinladung();
        untersuchungService.getOrCreateUntersuchung(einladung.getId());
        // ACT / ASSERT
        api
            .untersuchungMassnahmenResourceGetUntersuchungMassnahmenSehen()
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
        assertThat(untersuchung.getUntersuchungMassnahmen()).isNotNull();

        UntersuchungMassnahmen existingMassnahmen =
            untersuchungMassnahmenService.getById(untersuchung.getUntersuchungMassnahmen().getId());
        assertThat(existingMassnahmen).isNotNull();
        assertThat(existingMassnahmen.getBemerkungen()).isNotNull();
        // ACT
        UntersuchungSehenMassnahmenJsonSpec result = api
            .untersuchungMassnahmenResourceGetUntersuchungMassnahmenSehen()
            .anlassIdPath(untersuchung.getEinladung().getAnlass().getId().getId())
            .einladungIdPath(untersuchung.getEinladung().getId().getId())
            .execute(ResponseBody::prettyPeek)
            .then()
            .assertThat()
            .statusCode(Status.OK.getStatusCode())
            .extract().as(UntersuchungSehenMassnahmenJsonSpec.class);
        // ASSERT
        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(existingMassnahmen.getId().getId());
        assertThat(result.getData().getNachkontrolleSehen())
            .isNotNull()
            .isEqualTo(existingMassnahmen.getNachkontrolle().isNachkontrolleSehen());
    }

    @Test
    @TestAsAdminBS
    void saveUntersuchungMassnahmen() {
        // ARRANGE
        Einladung einladung = fixtureService.createEinladung();
        Untersuchung untersuchung = fixtureService.createUntersuchungWithMassnahmen();
        assertThat(untersuchung.getUntersuchungMassnahmen()).isNotNull();
        UntersuchungMassnahmen existingMassnahmen =
            untersuchungMassnahmenService.getById(untersuchung.getUntersuchungMassnahmen().getId());
        assertThat(existingMassnahmen).isNotNull();
        existingMassnahmen.setAbgegebenesInfomaterial(Collections.emptySet());

        // ACT
        UntersuchungSehenMassnahmenSaveJsonSpec saveJsonSpec =
            UntersuchungMassnahmenBuilder.createSehenMassnahmenSaveJaxSpec(builder -> builder
                .forUntersuchungMassnahmen(existingMassnahmen)
                .withNachkontrolle(true, false, false, false, false, false));

        UntersuchungSehenMassnahmenJsonSpec result = api
            .untersuchungMassnahmenResourceSaveUntersuchungMassnahmenSehen()
            .anlassIdPath(einladung.getAnlass().getId().getId())
            .einladungIdPath(einladung.getId().getId())
            .body(saveJsonSpec)
            .execute(ResponseBody::prettyPeek)
            .then()
            .assertThat()
            .statusCode(Status.OK.getStatusCode())
            .extract().as(UntersuchungSehenMassnahmenJsonSpec.class);

        // ASSERT
        assertThat(result).isNotNull();
        assertThat(result.getId()).isNotNull();
        assertThat(result.getData().getAbgegebenesInfomaterial()).isEmpty();
        assertThat(result.getData().getNachkontrolleSehen()).isTrue();
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
        assertThat(untersuchung).isNotNull();

        // ACT
        UntersuchungSehenMassnahmenSaveJsonSpec saveJsonSpec =
            UntersuchungMassnahmenBuilder.createSehenMassnahmenSaveJaxSpec(builder -> builder
                .forUntersuchung(untersuchung)
                .withAbgegebenesInfomaterial(Set.of(inaktivesInfomaterial))
                .withBemerkung("Untersuchung Massnahmen Bemerkung"));
        AppValidationErrorJsonSpec violation = api
            .untersuchungMassnahmenResourceSaveUntersuchungMassnahmenSehen()
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
        UntersuchungSehenMassnahmenSaveJsonSpec saveJsonSpec =
            UntersuchungMassnahmenBuilder.createSehenMassnahmenSaveJaxSpec(builder -> builder
                .forUntersuchung(untersuchung)
                .withInformationEltern(InformationEltern.BRIEF));
        api
            .untersuchungMassnahmenResourceSaveUntersuchungMassnahmenSehen()
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
            .extracting(aufgabe -> aufgabe != null ? aufgabe.getAufgabeStatus() : null).isEqualTo(AufgabeStatus.OFFEN);
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
        UntersuchungSehenMassnahmenSaveJsonSpec saveJsonSpec =
            UntersuchungMassnahmenBuilder.createSehenMassnahmenSaveJaxSpec(builder -> builder
                .forUntersuchung(untersuchung)
                .withInformationEltern(InformationEltern.KEINE));
        api
            .untersuchungMassnahmenResourceSaveUntersuchungMassnahmenSehen()
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
        UntersuchungSehenMassnahmenSaveJsonSpec saveJsonSpec =
            UntersuchungMassnahmenBuilder.createSehenMassnahmenSaveJaxSpec(builder -> builder
                .forUntersuchung(untersuchung)
                .withInformationEltern(null));
        api
            .untersuchungMassnahmenResourceSaveUntersuchungMassnahmenSehen()
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
    void ueberweisungAugenarzt_true_should_create_aufgabe() {
        // ARRANGE
        Untersuchung untersuchung = fixtureService.createUntersuchungWithMassnahmen();
        assertThat(untersuchung.getUntersuchungMassnahmen()).isNotNull();
        assertThat(untersuchung.getUntersuchungMassnahmen().getAufgabeUeberweisungAugenarzt()).isNull();

        // ACT
        UntersuchungSehenMassnahmenSaveJsonSpec saveJsonSpec =
            UntersuchungMassnahmenBuilder.createSehenMassnahmenSaveJaxSpec(builder -> builder
                .forUntersuchung(untersuchung)
                .withUeberweisungAugenarzt(true));
        api
            .untersuchungMassnahmenResourceSaveUntersuchungMassnahmenSehen()
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
            .extracting(aufgabe -> aufgabe != null ? aufgabe.getAufgabeStatus() : null).isEqualTo(AufgabeStatus.OFFEN);
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
        UntersuchungSehenMassnahmenSaveJsonSpec saveJsonSpec =
            UntersuchungMassnahmenBuilder.createSehenMassnahmenSaveJaxSpec(builder -> builder
                .forUntersuchung(untersuchung)
                .withUeberweisungAugenarzt(false));
        api
            .untersuchungMassnahmenResourceSaveUntersuchungMassnahmenSehen()
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
        UntersuchungSehenMassnahmenSaveJsonSpec saveJsonSpec =
            UntersuchungMassnahmenBuilder.createSehenMassnahmenSaveJaxSpec(builder -> builder
                .forUntersuchung(untersuchung)
                .withNachkontrolle(true, false, false, false, false, false));
        api
            .untersuchungMassnahmenResourceSaveUntersuchungMassnahmenSehen()
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
            .extracting(aufgabe -> aufgabe != null ? aufgabe.getAufgabeStatus() : null).isEqualTo(AufgabeStatus.OFFEN);
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
        UntersuchungSehenMassnahmenSaveJsonSpec saveJsonSpec =
            UntersuchungMassnahmenBuilder.createSehenMassnahmenSaveJaxSpec(builder -> builder
                .forUntersuchung(untersuchung)
                .withNachkontrolle(false, false, false, false, false, false));
        api
            .untersuchungMassnahmenResourceSaveUntersuchungMassnahmenSehen()
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

        UntersuchungSehenMassnahmenSaveJsonSpec saveJsonSpec =
            UntersuchungMassnahmenBuilder.createSehenMassnahmenSaveJaxSpec(builder -> builder
                .forUntersuchung(untersuchung)
                .withInformationEltern(InformationEltern.BRIEF)
                .withUeberweisungAugenarzt(true)
                .withNachkontrolle(true, false, false, false, false, false));
        api
            .untersuchungMassnahmenResourceSaveUntersuchungMassnahmenSehen()
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
            .extracting(aufgabe -> aufgabe != null ? aufgabe.getAufgabeStatus() : null).isEqualTo(AufgabeStatus.OFFEN);
        assertThat(untersuchungSaved.getUntersuchungMassnahmen().getAufgabeUeberweisungAugenarzt())
            .isNotNull()
            .extracting(aufgabe -> aufgabe != null ? aufgabe.getAufgabeStatus() : null).isEqualTo(AufgabeStatus.OFFEN);
        assertThat(untersuchungSaved.getUntersuchungMassnahmen().getAufgabeNachkontrolle())
            .isNotNull()
            .extracting(aufgabe -> aufgabe != null ? aufgabe.getAufgabeStatus() : null).isEqualTo(AufgabeStatus.OFFEN);

        // ACT
        Untersuchung untersuchungWithLoadedData =
            fixtureService.getUntersuchungWithLoadedData(untersuchung.getEinladung().getId());
        assertThat(untersuchungWithLoadedData.getUntersuchungMassnahmen()).isNotNull();
        UntersuchungSehenMassnahmenSaveJsonSpec updateJsonSpec =
            UntersuchungMassnahmenBuilder.createSehenMassnahmenSaveJaxSpec(builder -> builder
                .forUntersuchung(untersuchungWithLoadedData)
                .forUntersuchungMassnahmen(untersuchungWithLoadedData.getUntersuchungMassnahmen())
                .withInformationEltern(InformationEltern.KEINE)
                .withUeberweisungAugenarzt(false)
                .withNachkontrolle(false, false, false, false, false, false));

        api
            .untersuchungMassnahmenResourceSaveUntersuchungMassnahmenSehen()
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
            .extracting(aufgabe -> aufgabe != null ? aufgabe.getAufgabeStatus() : null).isEqualTo(AufgabeStatus.ABGESCHLOSSEN);
        assertThat(untersuchungUpdated.getUntersuchungMassnahmen().getAufgabeUeberweisungAugenarzt())
            .isNotNull()
            .extracting(aufgabe -> aufgabe != null ? aufgabe.getAufgabeStatus() : null).isEqualTo(AufgabeStatus.ABGESCHLOSSEN);
        assertThat(untersuchungUpdated.getUntersuchungMassnahmen().getAufgabeNachkontrolle())
            .isNotNull()
            .extracting(aufgabe -> aufgabe != null ? aufgabe.getAufgabeStatus() : null).isEqualTo(AufgabeStatus.ABGESCHLOSSEN);
    }

    @Test
    @TestAsAdminBS
    void einladung_entfernen_true_should_delete_aufgaben() {
        // ARRANGE
        Untersuchung untersuchung = fixtureService.createUntersuchungWithMassnahmen();
        assertThat(untersuchung.getUntersuchungMassnahmen()).isNotNull();
        assertThat(untersuchung.getUntersuchungMassnahmen().getAufgabeNachkontrolle()).isNull();

        UntersuchungSehenMassnahmenSaveJsonSpec saveJsonSpec =
            UntersuchungMassnahmenBuilder.createSehenMassnahmenSaveJaxSpec(builder -> builder
                .forUntersuchung(untersuchung)
                .withInformationEltern(InformationEltern.BRIEF)
                .withUeberweisungAugenarzt(true)
                .withNachkontrolle(true, false, false, false, false, false));
        api
            .untersuchungMassnahmenResourceSaveUntersuchungMassnahmenSehen()
            .anlassIdPath(untersuchung.getEinladung().getAnlass().getId().getId())
            .einladungIdPath(untersuchung.getEinladung().getId().getId())
            .body(saveJsonSpec)
            .execute(ResponseBody::prettyPeek)
            .then()
            .assertThat()
            .statusCode(Status.OK.getStatusCode());

        assertThat(aufgabeService.findAllForKind(untersuchung.getEinladung().getDossier().getKind().getId())).hasSize(3);
        Untersuchung untersuchungSaved = fixtureService.getUntersuchungWithLoadedData(untersuchung.getEinladung().getId());
        assertThat(untersuchungSaved.getUntersuchungMassnahmen()).isNotNull();
        Aufgabe aufgabeInformationEltern = untersuchungSaved.getUntersuchungMassnahmen().getAufgabeInformationEltern();
        Aufgabe aufgabeUeberweisungAugenarzt = untersuchungSaved.getUntersuchungMassnahmen().getAufgabeUeberweisungAugenarzt();
        Aufgabe aufgabeNachkontrolle = untersuchungSaved.getUntersuchungMassnahmen().getAufgabeNachkontrolle();

        assertThat(aufgabeInformationEltern).isNotNull();
        assertThat(aufgabeUeberweisungAugenarzt).isNotNull();
        assertThat(aufgabeNachkontrolle).isNotNull();

        // ACT
        List<ID<Einladung>> einladungIdList = List.of(untersuchung.getEinladung().getId());
        einladungService.removeEinladungen(einladungIdList);

        // ASSERT
        assertThat(aufgabeService.findAllForKind(untersuchung.getEinladung().getDossier().getKind().getId())).isEmpty();
    }

    @Test
    @TestAsAdminBS
    void nachkontrolle_true_should_not_overwrite_empfaenger() {
        // ARRANGE
        Untersuchung untersuchung = fixtureService.createUntersuchungWithMassnahmen();
        assertThat(untersuchung.getUntersuchungMassnahmen()).isNotNull();
        assertThat(untersuchung.getUntersuchungMassnahmen().getAufgabeNachkontrolle()).isNull();

        // saving for the first time will create the Aufgaben and assign the current user as empfaenger
        UntersuchungSehenMassnahmenSaveJsonSpec saveJsonSpec =
            UntersuchungMassnahmenBuilder.createSehenMassnahmenSaveJaxSpec(builder -> builder.forUntersuchung(untersuchung)
                .withNachkontrolle(true, false, false, false, false, false));

        api.untersuchungMassnahmenResourceSaveUntersuchungMassnahmenSehen()
            .anlassIdPath(untersuchung.getEinladung().getAnlass().getId().getId())
            .einladungIdPath(untersuchung.getEinladung().getId().getId())
            .body(saveJsonSpec)
            .execute(ResponseBody::prettyPeek)
            .then()
            .assertThat()
            .statusCode(Status.OK.getStatusCode());

        Untersuchung untersuchungSaved =
            fixtureService.getUntersuchungWithLoadedData(untersuchung.getEinladung().getId());
        assertThat(untersuchungSaved.getUntersuchungMassnahmen()).isNotNull();
        assertThat(untersuchungSaved.getUntersuchungMassnahmen().getAufgabeNachkontrolle()).isNotNull();

        Aufgabe aufgabe = untersuchungSaved.getUntersuchungMassnahmen().getAufgabeNachkontrolle();
        Benutzer existingBenuzter = fixtureService.createBenutzer();

        fixtureService.changeAufgabeEmpfaenger(aufgabe.getId(), existingBenuzter);

        Untersuchung untersuchungSaved2 =
            fixtureService.getUntersuchungWithLoadedData(untersuchung.getEinladung().getId());
        assertThat(untersuchungSaved2.getUntersuchungMassnahmen()).isNotNull();

        // ACT
        UntersuchungSehenMassnahmenSaveJsonSpec saveJsonSpecUpdated =
            UntersuchungMassnahmenBuilder.createSehenMassnahmenSaveJaxSpec(builder -> builder
                .forUntersuchungMassnahmen(untersuchungSaved2.getUntersuchungMassnahmen())
                .withNachkontrolle(true, false, false, false, false, false)
            );

        api.untersuchungMassnahmenResourceSaveUntersuchungMassnahmenSehen()
            .anlassIdPath(untersuchung.getEinladung().getAnlass().getId().getId())
            .einladungIdPath(untersuchung.getEinladung().getId().getId())
            .body(saveJsonSpecUpdated)
            .execute(ResponseBody::prettyPeek)
            .then()
            .assertThat()
            .statusCode(Status.OK.getStatusCode())
            .extract()
            .as(UntersuchungSehenMassnahmenJsonSpec.class);

        Untersuchung actualUntersuchungen =  fixtureService.getUntersuchungWithLoadedData(untersuchung.getEinladung().getId());

        assertThat(actualUntersuchungen.getUntersuchungMassnahmen()).isNotNull();
        Aufgabe aufgabeNachkontrolle = actualUntersuchungen.getUntersuchungMassnahmen().getAufgabeNachkontrolle();
        assertThat(aufgabeNachkontrolle)
            .isNotNull()
            .extracting(actualAufgabe -> actualAufgabe.getEmpfaenger().getId()).isEqualTo(existingBenuzter.getId());
    }
}
