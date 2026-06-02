package ch.dvbern.oss.hemed.esc.api.features.anlass.massnahmen;

import java.util.Collections;
import java.util.List;
import java.util.Set;

import ch.dvbern.dvbstarter.infrastructure.errorhandling.AppValidationMessage;
import ch.dvbern.dvbstarter.shared.types.id.ID;
import ch.dvbern.oss.hemed.esc.api.features.anlass.anonymereintrag.AnonymerEintrag;
import ch.dvbern.oss.hemed.esc.api.features.anlass.einladung.Einladung;
import ch.dvbern.oss.hemed.esc.api.features.anlass.einladung.EinladungService;
import ch.dvbern.oss.hemed.esc.api.features.anlass.massnahmen.types.InformationEltern;
import ch.dvbern.oss.hemed.esc.api.features.anlass.untersuchung.Untersuchung;
import ch.dvbern.oss.hemed.esc.api.features.anlass.untersuchung.UntersuchungService;
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
import ch.dvbern.oss.hemed.esc.api.testutil.security.TestAsSchularztBE;
import ch.dvbern.oss.hemed.esc.generated.restassured.api.UntersuchungMassnahmenApiSpec;
import ch.dvbern.oss.hemed.esc.generated.restassured.dto.AppValidationErrorJsonSpec;
import ch.dvbern.oss.hemed.esc.generated.restassured.dto.UntersuchungMassnahmenJsonSpec;
import ch.dvbern.oss.hemed.esc.generated.restassured.dto.UntersuchungMassnahmenSaveJsonSpec;
import ch.dvbern.oss.hemed.esc.generated.restassured.dto.UntersuchungSehenMassnahmenJsonSpec;
import ch.dvbern.oss.hemed.esc.generated.restassured.dto.UntersuchungSehenMassnahmenSaveJsonSpec;
import io.quarkus.test.junit.QuarkusTest;
import io.restassured.response.ResponseBody;
import jakarta.ws.rs.core.Response.Status;
import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static ch.dvbern.oss.hemed.esc.api.testutil.restassured.RequestSpecUtil.toHasAnonymerEintragIdWrapper;
import static ch.dvbern.oss.hemed.esc.api.testutil.restassured.RequestSpecUtil.toHasUntersuchungIdWrapper;
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
            .untersuchungMassnahmenResourceGetUntersuchungMassnahmen()
            .anlassIdPath(einladung.getAnlass().getId().getId())
            .einladungIdPath(toHasUntersuchungIdWrapper(einladung.getId()))
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
        UntersuchungMassnahmenJsonSpec result = api
            .untersuchungMassnahmenResourceGetUntersuchungMassnahmen()
            .anlassIdPath(untersuchung.getAnlass().getId().getId())
            .einladungIdPath(toHasUntersuchungIdWrapper(untersuchung.getEinladungRequired().getId()))
            .execute(ResponseBody::prettyPeek)
            .then()
            .assertThat()
            .statusCode(Status.OK.getStatusCode())
            .extract().as(UntersuchungMassnahmenJsonSpec.class);
        // ASSERT
        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(existingMassnahmen.getId().getId());
        assertThat(result.getData().getBemerkungen())
            .isNotNull()
            .isEqualTo(existingMassnahmen.getBemerkungen());
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
        assertThat(existingMassnahmen.getBemerkungen()).isNotNull()
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
            .einladungIdPath(toHasUntersuchungIdWrapper(einladung.getId()))
            .body(saveJsonSpec)
            .execute(ResponseBody::prettyPeek)
            .then()
            .assertThat()
            .statusCode(Status.OK.getStatusCode())
            .extract().as(UntersuchungMassnahmenJsonSpec.class);

        // ASSERT
        assertThat(result).isNotNull();
        assertThat(result.getId()).isNotNull();
        assertThat(result.getData().getBemerkungen())
            .isNotNull()
            .isEqualTo("Test updateUntersuchungMassnahmen");
        assertThat(result.getData().getKeineMassnahmen()).isTrue();
        assertThat(result.getData().getBeratungMPA()).isTrue();
        assertThat(result.getData().getBeratungSchulAerztin()).isTrue();
    }

    @Test
    @TestAsAdminBS
    void getUntersuchungSehenMassnahmen_existing_should_return_existing() {
        // ARRANGE
        Untersuchung untersuchung = fixtureService.createUntersuchungWithSehenAndMassnahmen();
        UntersuchungMassnahmen existingMassnahmen = untersuchung.getUntersuchungMassnahmen();
        assertThat(existingMassnahmen).isNotNull();
        // ACT
        UntersuchungSehenMassnahmenJsonSpec result = api
            .untersuchungMassnahmenResourceGetUntersuchungMassnahmenSehen()
            .anlassIdPath(untersuchung.getAnlass().getId().getId())
            .einladungIdPath(toHasUntersuchungIdWrapper(untersuchung.getEinladungRequired().getId()))
            .execute(ResponseBody::prettyPeek)
            .then()
            .assertThat()
            .statusCode(Status.OK.getStatusCode())
            .extract().as(UntersuchungSehenMassnahmenJsonSpec.class);
        // ASSERT
        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(existingMassnahmen.getId().getId());
        assertThat(result.getData().getBereitsBehandeltSehen()).isEqualTo(existingMassnahmen.isBereitsBehandeltSehen());
        assertThat(result.getData().getUeberweisungAugenarzt()).isEqualTo(existingMassnahmen.isUeberweisungAugenarzt());
        assertThat(result.getData().getNachkontrolleSehen())
            .isEqualTo(existingMassnahmen.getNachkontrolle().isNachkontrolleSehen());
    }

    @Test
    @TestAsAdminBS
    void saveUntersuchungSehenMassnahmen() {
        // ARRANGE
        Untersuchung untersuchung = fixtureService.createUntersuchungWithSehenAndMassnahmen();
        Einladung einladung = untersuchung.getEinladungRequired();
        UntersuchungMassnahmen existingMassnahmen = untersuchung.getUntersuchungMassnahmen();
        assertThat(existingMassnahmen).isNotNull();

        // ACT
        UntersuchungSehenMassnahmenSaveJsonSpec saveJsonSpec =
            UntersuchungMassnahmenBuilder.createSehenMassnahmenSaveJaxSpec(
                builder -> builder
                    .forUntersuchung(untersuchung)
                    .forUntersuchungMassnahmen(existingMassnahmen)
                    .withBereitsBehandeltSehen(true)
                    .withUeberweisungAugenarzt(true)
                    .withInformationEltern(InformationEltern.KEINE)
                    .withNachkontrolle(true, false, false, false, false, false)
                    .withAbgegebenesInfomaterial(Set.of())
            );

        UntersuchungSehenMassnahmenJsonSpec result = api
            .untersuchungMassnahmenResourceSaveUntersuchungMassnahmenSehen()
            .anlassIdPath(einladung.getAnlass().getId().getId())
            .einladungIdPath(toHasUntersuchungIdWrapper(einladung.getId()))
            .body(saveJsonSpec)
            .execute(ResponseBody::prettyPeek)
            .then()
            .assertThat()
            .statusCode(Status.OK.getStatusCode())
            .extract().as(UntersuchungSehenMassnahmenJsonSpec.class);

        // ASSERT
        assertThat(result).isNotNull();
        assertThat(result.getId()).isNotNull();
        assertThat(result.getData().getBereitsBehandeltSehen()).isTrue();
        assertThat(result.getData().getUeberweisungAugenarzt()).isTrue();
        assertThat(result.getData().getNachkontrolleSehen()).isTrue();
    }

    @Test
    @TestAsSchularztBE
    void saveUntersuchungSehenMassnahmen_anonym() {
        // ARRANGE
        tenantProducer.setCurrentTenant(TenantEnum.BE);
        Benutzer arzt = fixtureService.readBenutzerWithBerechtigungen(TestAsSchularztBE.BENUTZER_ID_ID);
        AnonymerEintrag anonymerEintrag = fixtureService.createAnonymerEintragForArzt(arzt);
        Untersuchung untersuchung = untersuchungService.getOrCreateUntersuchungAnonym(anonymerEintrag.getId(), false);
        assertThat(untersuchung.getUntersuchungMassnahmen()).isNull();

        // ACT
        UntersuchungSehenMassnahmenSaveJsonSpec saveJsonSpec =
            UntersuchungMassnahmenBuilder.createSehenMassnahmenSaveJaxSpec(
                builder -> builder
                    .withBereitsBehandeltSehen(true)
                    .withInformationEltern(InformationEltern.KEINE)
                    .withUeberweisungAugenarzt(true)
                    .withNachkontrolle(true, false, false, false, false, false)
                    .withAbgegebenesInfomaterial(Set.of())
            );
        UntersuchungSehenMassnahmenJsonSpec result = api
            .untersuchungMassnahmenResourceSaveUntersuchungMassnahmenSehen()
            .anlassIdPath(anonymerEintrag.getAnlass().getId().getId())
            .einladungIdPath(toHasAnonymerEintragIdWrapper(anonymerEintrag.getId()))
            .body(saveJsonSpec)
            .execute(ResponseBody::prettyPeek)
            .then()
            .assertThat()
            .statusCode(Status.OK.getStatusCode())
            .extract().as(UntersuchungSehenMassnahmenJsonSpec.class);

        // ASSERT
        assertThat(result).isNotNull();
        assertThat(result.getId()).isNotNull();
        assertThat(result.getData().getBereitsBehandeltSehen()).isTrue();
        assertThat(result.getData().getAbgegebenesInfomaterial()).isEmpty();

        assertThat(result.getData().getUeberweisungAugenarzt()).isTrue();
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
        UntersuchungMassnahmenSaveJsonSpec saveJsonSpec =
            UntersuchungMassnahmenBuilder.createJaxSpec(builder -> builder
                .forUntersuchung(untersuchung)
                .withAbgegebenesInfomaterial(Set.of(inaktivesInfomaterial))
                .withBemerkung("Untersuchung Massnahmen Bemerkung")).getData();
        AppValidationErrorJsonSpec violation = api
            .untersuchungMassnahmenResourceSaveUntersuchungMassnahmen()
            .anlassIdPath(einladung.getAnlass().getId().getId())
            .einladungIdPath(toHasUntersuchungIdWrapper(einladung.getId()))
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
            .anlassIdPath(untersuchung.getAnlass().getId().getId())
            .einladungIdPath(toHasUntersuchungIdWrapper(untersuchung.getEinladungRequired().getId()))
            .body(saveJsonSpec)
            .execute(ResponseBody::prettyPeek)
            .then()
            .assertThat()
            .statusCode(Status.OK.getStatusCode());

        // ASSERT
        Untersuchung untersuchungSaved =
            fixtureService.getUntersuchungWithLoadedData(untersuchung.getEinladungRequired().getId());
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
        UntersuchungMassnahmenSaveJsonSpec saveJsonSpec =
            UntersuchungMassnahmenBuilder.createJaxSpec(builder -> builder
                .forUntersuchung(untersuchung)
                .withInformationEltern(InformationEltern.KEINE)).getData();
        api
            .untersuchungMassnahmenResourceSaveUntersuchungMassnahmen()
            .anlassIdPath(untersuchung.getAnlass().getId().getId())
            .einladungIdPath(toHasUntersuchungIdWrapper(untersuchung.getEinladungRequired().getId()))
            .body(saveJsonSpec)
            .execute(ResponseBody::prettyPeek)
            .then()
            .assertThat()
            .statusCode(Status.OK.getStatusCode());

        // ASSERT
        Untersuchung untersuchungSaved =
            fixtureService.getUntersuchungWithLoadedData(untersuchung.getEinladungRequired().getId());
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
            .anlassIdPath(untersuchung.getAnlass().getId().getId())
            .einladungIdPath(toHasUntersuchungIdWrapper(untersuchung.getEinladungRequired().getId()))
            .body(saveJsonSpec)
            .execute(ResponseBody::prettyPeek)
            .then()
            .assertThat()
            .statusCode(Status.OK.getStatusCode());

        // ASSERT
        Untersuchung untersuchungSaved =
            fixtureService.getUntersuchungWithLoadedData(untersuchung.getEinladungRequired().getId());
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
            .anlassIdPath(untersuchung.getAnlass().getId().getId())
            .einladungIdPath(toHasUntersuchungIdWrapper(untersuchung.getEinladungRequired().getId()))
            .body(saveJsonSpec)
            .execute(ResponseBody::prettyPeek)
            .then()
            .assertThat()
            .statusCode(Status.OK.getStatusCode());

        // ASSERT
        Untersuchung untersuchungSaved =
            fixtureService.getUntersuchungWithLoadedData(untersuchung.getEinladungRequired().getId());
        assertThat(untersuchungSaved.getUntersuchungMassnahmen()).isNotNull();
        assertThat(untersuchungSaved.getUntersuchungMassnahmen().getAufgabeUeberweisungHausarzt())
            .isNotNull()
            .extracting(aufgabe -> aufgabe != null ? aufgabe.getAufgabeStatus() : null).isEqualTo(AufgabeStatus.OFFEN);
        assertThat(untersuchungSaved.getUntersuchungMassnahmen()
            .getAufgabeUeberweisungHausarzt()
            .getBemerkung()).isNull();
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
            .anlassIdPath(untersuchung.getAnlass().getId().getId())
            .einladungIdPath(toHasUntersuchungIdWrapper(untersuchung.getEinladungRequired().getId()))
            .body(saveJsonSpec)
            .execute(ResponseBody::prettyPeek)
            .then()
            .assertThat()
            .statusCode(Status.OK.getStatusCode());

        // ASSERT
        Untersuchung untersuchungSaved =
            fixtureService.getUntersuchungWithLoadedData(untersuchung.getEinladungRequired().getId());
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
            .anlassIdPath(untersuchung.getAnlass().getId().getId())
            .einladungIdPath(toHasUntersuchungIdWrapper(untersuchung.getEinladungRequired().getId()))
            .body(saveJsonSpec)
            .execute(ResponseBody::prettyPeek)
            .then()
            .assertThat()
            .statusCode(Status.OK.getStatusCode());

        // ASSERT
        Untersuchung untersuchungSaved =
            fixtureService.getUntersuchungWithLoadedData(untersuchung.getEinladungRequired().getId());
        assertThat(untersuchungSaved.getUntersuchungMassnahmen()).isNotNull();
        assertThat(untersuchungSaved.getUntersuchungMassnahmen().getAufgabeUeberweisungAugenarzt())
            .isNotNull()
            .extracting(aufgabe -> aufgabe != null ? aufgabe.getAufgabeStatus() : null).isEqualTo(AufgabeStatus.OFFEN);
        assertThat(untersuchungSaved.getUntersuchungMassnahmen()
            .getAufgabeUeberweisungAugenarzt()
            .getBemerkung()).isNull();
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
            .anlassIdPath(untersuchung.getAnlass().getId().getId())
            .einladungIdPath(toHasUntersuchungIdWrapper(untersuchung.getEinladungRequired().getId()))
            .body(saveJsonSpec)
            .execute(ResponseBody::prettyPeek)
            .then()
            .assertThat()
            .statusCode(Status.OK.getStatusCode());

        // ASSERT
        Untersuchung untersuchungSaved =
            fixtureService.getUntersuchungWithLoadedData(untersuchung.getEinladungRequired().getId());
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
            .anlassIdPath(untersuchung.getAnlass().getId().getId())
            .einladungIdPath(toHasUntersuchungIdWrapper(untersuchung.getEinladungRequired().getId()))
            .body(saveJsonSpec)
            .execute(ResponseBody::prettyPeek)
            .then()
            .assertThat()
            .statusCode(Status.OK.getStatusCode());

        // ASSERT
        Untersuchung untersuchungSaved =
            fixtureService.getUntersuchungWithLoadedData(untersuchung.getEinladungRequired().getId());
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
        UntersuchungMassnahmenSaveJsonSpec saveJsonSpec =
            UntersuchungMassnahmenBuilder.createJaxSpec(builder -> builder
                .forUntersuchung(untersuchung)
                .withNachkontrolle(false, false, false, false, false, false)).getData();
        api
            .untersuchungMassnahmenResourceSaveUntersuchungMassnahmen()
            .anlassIdPath(untersuchung.getAnlass().getId().getId())
            .einladungIdPath(toHasUntersuchungIdWrapper(untersuchung.getEinladungRequired().getId()))
            .body(saveJsonSpec)
            .execute(ResponseBody::prettyPeek)
            .then()
            .assertThat()
            .statusCode(Status.OK.getStatusCode());

        // ASSERT
        Untersuchung untersuchungSaved =
            fixtureService.getUntersuchungWithLoadedData(untersuchung.getEinladungRequired().getId());
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
            .anlassIdPath(untersuchung.getAnlass().getId().getId())
            .einladungIdPath(toHasUntersuchungIdWrapper(untersuchung.getEinladungRequired().getId()))
            .body(saveJsonSpec)
            .execute(ResponseBody::prettyPeek)
            .then()
            .assertThat()
            .statusCode(Status.OK.getStatusCode());

        Untersuchung untersuchungSaved =
            fixtureService.getUntersuchungWithLoadedData(untersuchung.getEinladungRequired().getId());
        assertThat(untersuchungSaved.getUntersuchungMassnahmen()).isNotNull();
        assertThat(untersuchungSaved.getUntersuchungMassnahmen().getAufgabeInformationEltern())
            .isNotNull()
            .extracting(aufgabe -> aufgabe != null ? aufgabe.getAufgabeStatus() : null).isEqualTo(AufgabeStatus.OFFEN);
        assertThat(untersuchungSaved.getUntersuchungMassnahmen().getAufgabeUeberweisungHausarzt())
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
            fixtureService.getUntersuchungWithLoadedData(untersuchung.getEinladungRequired().getId());
        assertThat(untersuchungWithLoadedData.getUntersuchungMassnahmen()).isNotNull();
        UntersuchungMassnahmenSaveJsonSpec updateJsonSpec =
            UntersuchungMassnahmenBuilder.createJaxSpec(builder -> builder
                .forUntersuchung(untersuchungWithLoadedData)
                .forUntersuchungMassnahmen(untersuchungWithLoadedData.getUntersuchungMassnahmen())
                .withInformationEltern(InformationEltern.KEINE)
                .withUeberweisungHausarzt(false)
                .withUeberweisungAugenarzt(false)
                .withNachkontrolle(false, false, false, false, false, false)).getData();

        api
            .untersuchungMassnahmenResourceSaveUntersuchungMassnahmen()
            .anlassIdPath(untersuchung.getAnlass().getId().getId())
            .einladungIdPath(toHasUntersuchungIdWrapper(untersuchung.getEinladungRequired().getId()))
            .body(updateJsonSpec)
            .execute(ResponseBody::prettyPeek)
            .then()
            .assertThat()
            .statusCode(Status.OK.getStatusCode());

        // ASSERT
        Untersuchung untersuchungUpdated =
            fixtureService.getUntersuchungWithLoadedData(untersuchung.getEinladungRequired().getId());
        assertThat(untersuchungUpdated.getUntersuchungMassnahmen()).isNotNull();
        assertThat(untersuchungUpdated.getUntersuchungMassnahmen().getAufgabeInformationEltern())
            .isNotNull()
            .extracting(aufgabe -> aufgabe != null ? aufgabe.getAufgabeStatus() : null)
            .isEqualTo(AufgabeStatus.ABGESCHLOSSEN);
        assertThat(untersuchungUpdated.getUntersuchungMassnahmen().getAufgabeUeberweisungHausarzt())
            .isNotNull()
            .extracting(aufgabe -> aufgabe != null ? aufgabe.getAufgabeStatus() : null)
            .isEqualTo(AufgabeStatus.ABGESCHLOSSEN);
        assertThat(untersuchungUpdated.getUntersuchungMassnahmen().getAufgabeUeberweisungAugenarzt())
            .isNotNull()
            .extracting(aufgabe -> aufgabe != null ? aufgabe.getAufgabeStatus() : null)
            .isEqualTo(AufgabeStatus.ABGESCHLOSSEN);
        assertThat(untersuchungUpdated.getUntersuchungMassnahmen().getAufgabeNachkontrolle())
            .isNotNull()
            .extracting(aufgabe -> aufgabe != null ? aufgabe.getAufgabeStatus() : null)
            .isEqualTo(AufgabeStatus.ABGESCHLOSSEN);
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
            .anlassIdPath(untersuchung.getAnlass().getId().getId())
            .einladungIdPath(toHasUntersuchungIdWrapper(untersuchung.getEinladungRequired().getId()))
            .body(saveJsonSpec)
            .execute(ResponseBody::prettyPeek)
            .then()
            .assertThat()
            .statusCode(Status.OK.getStatusCode());

        assertThat(aufgabeService.findAllForKind(untersuchung.getEinladungRequired()
            .getDossier()
            .getKind()
            .getId())).hasSize(4);
        Untersuchung untersuchungSaved =
            fixtureService.getUntersuchungWithLoadedData(untersuchung.getEinladungRequired().getId());
        assertThat(untersuchungSaved.getUntersuchungMassnahmen()).isNotNull();
        Aufgabe aufgabeInformationEltern = untersuchungSaved.getUntersuchungMassnahmen().getAufgabeInformationEltern();
        Aufgabe aufgabeUeberweisungHausarzt =
            untersuchungSaved.getUntersuchungMassnahmen().getAufgabeUeberweisungHausarzt();
        Aufgabe aufgabeUeberweisungAugenarzt =
            untersuchungSaved.getUntersuchungMassnahmen().getAufgabeUeberweisungAugenarzt();
        Aufgabe aufgabeNachkontrolle = untersuchungSaved.getUntersuchungMassnahmen().getAufgabeNachkontrolle();

        assertThat(aufgabeInformationEltern).isNotNull();
        assertThat(aufgabeUeberweisungHausarzt).isNotNull();
        assertThat(aufgabeUeberweisungAugenarzt).isNotNull();
        assertThat(aufgabeNachkontrolle).isNotNull();

        // ACT
        List<ID<Einladung>> einladungIdList = List.of(untersuchung.getEinladungRequired().getId());
        einladungService.removeEinladungen(einladungIdList);

        // ASSERT
        assertThat(aufgabeService.findAllForKind(untersuchung.getEinladungRequired()
            .getDossier()
            .getKind()
            .getId())).isEmpty();
    }

    @Test
    @TestAsAdminBS
    void nachkontrolle_true_should_not_overwrite_empfaenger() {
        // ARRANGE
        Untersuchung untersuchung = fixtureService.createUntersuchungWithMassnahmen();
        assertThat(untersuchung.getUntersuchungMassnahmen()).isNotNull();
        assertThat(untersuchung.getUntersuchungMassnahmen().getAufgabeNachkontrolle()).isNull();

        // saving for the first time will create the Aufgaben and assign the current user as empfaenger
        UntersuchungMassnahmenSaveJsonSpec saveJsonSpec =
            UntersuchungMassnahmenBuilder.createJaxSpec(builder -> builder.forUntersuchung(untersuchung)
                .withNachkontrolle(true, false, false, false, false, false)).getData();

        api.untersuchungMassnahmenResourceSaveUntersuchungMassnahmen()
            .anlassIdPath(untersuchung.getAnlass().getId().getId())
            .einladungIdPath(toHasUntersuchungIdWrapper(untersuchung.getEinladungRequired().getId()))
            .body(saveJsonSpec)
            .execute(ResponseBody::prettyPeek)
            .then()
            .assertThat()
            .statusCode(Status.OK.getStatusCode());

        Untersuchung untersuchungSaved =
            fixtureService.getUntersuchungWithLoadedData(untersuchung.getEinladungRequired().getId());
        assertThat(untersuchungSaved.getUntersuchungMassnahmen()).isNotNull();
        assertThat(untersuchungSaved.getUntersuchungMassnahmen().getAufgabeNachkontrolle()).isNotNull();

        Aufgabe aufgabe = untersuchungSaved.getUntersuchungMassnahmen().getAufgabeNachkontrolle();
        Benutzer existingBenuzter = fixtureService.createBenutzerFuerZentraleOrganisation();

        fixtureService.changeAufgabeEmpfaenger(aufgabe.getId(), existingBenuzter);

        Untersuchung untersuchungSaved2 =
            fixtureService.getUntersuchungWithLoadedData(untersuchung.getEinladungRequired().getId());
        assertThat(untersuchungSaved2.getUntersuchungMassnahmen()).isNotNull();

        // ACT
        UntersuchungMassnahmenSaveJsonSpec saveJsonSpecUpdated =
            UntersuchungMassnahmenBuilder.createJaxSpec(builder -> builder
                .forUntersuchungMassnahmen(untersuchungSaved2.getUntersuchungMassnahmen())
                .withNachkontrolle(true, false, false, false, false, false)
            ).getData();

        api.untersuchungMassnahmenResourceSaveUntersuchungMassnahmen()
            .anlassIdPath(untersuchung.getAnlass().getId().getId())
            .einladungIdPath(toHasUntersuchungIdWrapper(untersuchung.getEinladungRequired().getId()))
            .body(saveJsonSpecUpdated)
            .execute(ResponseBody::prettyPeek)
            .then()
            .assertThat()
            .statusCode(Status.OK.getStatusCode())
            .extract()
            .as(UntersuchungMassnahmenJsonSpec.class);

        Untersuchung actualUntersuchungen =
            fixtureService.getUntersuchungWithLoadedData(untersuchung.getEinladungRequired().getId());

        assertThat(actualUntersuchungen.getUntersuchungMassnahmen()).isNotNull();
        Aufgabe aufgabeNachkontrolle = actualUntersuchungen.getUntersuchungMassnahmen().getAufgabeNachkontrolle();
        assertThat(aufgabeNachkontrolle)
            .isNotNull()
            .extracting(actualAufgabe -> actualAufgabe.getEmpfaenger().getId()).isEqualTo(existingBenuzter.getId());
    }
}
