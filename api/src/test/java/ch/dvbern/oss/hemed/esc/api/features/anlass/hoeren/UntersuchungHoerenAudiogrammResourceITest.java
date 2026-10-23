package ch.dvbern.oss.hemed.esc.api.features.anlass.hoeren;

import java.util.List;
import jakarta.ws.rs.core.Response.Status;

import ch.dvbern.oss.hemed.esc.api.features.anlass.Anlass;
import ch.dvbern.oss.hemed.esc.api.features.anlass.anlassconfig.types.RequirementSetting;
import ch.dvbern.oss.hemed.esc.api.features.anlass.anonymereintrag.AnonymerEintrag;
import ch.dvbern.oss.hemed.esc.api.features.anlass.einladung.Einladung;
import ch.dvbern.oss.hemed.esc.api.features.anlass.hoeren.types.AudiogrammBeurteilung;
import ch.dvbern.oss.hemed.esc.api.features.anlass.hoeren.types.Dezibel;
import ch.dvbern.oss.hemed.esc.api.features.anlass.hoeren.types.HoerenZusaetzlicheAngaben;
import ch.dvbern.oss.hemed.esc.api.features.anlass.untersuchung.Untersuchung;
import ch.dvbern.oss.hemed.esc.api.features.anlass.untersuchung.UntersuchungService;
import ch.dvbern.oss.hemed.esc.api.shared.auth.TenantContext;
import ch.dvbern.oss.hemed.esc.api.shared.auth.TenantEnum;
import ch.dvbern.oss.hemed.esc.api.shared.benutzer.Benutzer;
import ch.dvbern.oss.hemed.esc.api.testutil.AbstractITest;
import ch.dvbern.oss.hemed.esc.api.testutil.FixtureService;
import ch.dvbern.oss.hemed.esc.api.testutil.builder.UntersuchungHoerenAudiogrammBuilder;
import ch.dvbern.oss.hemed.esc.api.testutil.restassured.RequestSpecUtil;
import ch.dvbern.oss.hemed.esc.api.testutil.security.TestAsAdminBS;
import ch.dvbern.oss.hemed.esc.api.testutil.security.TestAsSchularztBE;
import ch.dvbern.oss.hemed.esc.generated.restassured.api.UntersuchungHoerenAudiogrammApiSpec;
import ch.dvbern.oss.hemed.esc.generated.restassured.dto.AudiogrammBeurteilungSpec;
import ch.dvbern.oss.hemed.esc.generated.restassured.dto.AudiogrammSaveJsonSpec;
import ch.dvbern.oss.hemed.esc.generated.restassured.dto.DezibelSpec;
import ch.dvbern.oss.hemed.esc.generated.restassured.dto.HoerenZusaetzlicheAngabenSpec;
import ch.dvbern.oss.hemed.esc.generated.restassured.dto.UntersuchungHoerenAudiogrammJsonSpec;
import ch.dvbern.oss.hemed.esc.generated.restassured.dto.UntersuchungHoerenAudiogrammSaveJsonSpec;

import io.quarkus.test.junit.QuarkusTest;
import io.restassured.response.ResponseBody;
import lombok.RequiredArgsConstructor;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;

import static ch.dvbern.oss.hemed.esc.api.testutil.restassured.RequestSpecUtil.toHasAnonymerEintragIdWrapper;
import static ch.dvbern.oss.hemed.esc.api.testutil.restassured.RequestSpecUtil.toHasUntersuchungIdWrapper;
import static org.assertj.core.api.Assertions.assertThat;

@QuarkusTest
@RequiredArgsConstructor
class UntersuchungHoerenAudiogrammResourceITest extends AbstractITest {

    private final UntersuchungHoerenAudiogrammApiSpec api =
        UntersuchungHoerenAudiogrammApiSpec.untersuchungHoerenAudiogramm(RequestSpecUtil.quarkusSpec());

    private final UntersuchungService untersuchungService;
    private final UntersuchungHoerenAudiogrammService untersuchungHoerenAudiogrammService;
    private final FixtureService fixtureService;
    private final TenantContext tenantContext;

    @Test
    @TestAsAdminBS
    void getUntersuchungHoerenAudiogramm_non_existing_should_not_create_data() {
        // ARRANGE
        Einladung einladung = fixtureService.createEinladung();
        untersuchungService.getOrCreateUntersuchung(einladung.getId());
        // ACT / ASSERT
        api
            .untersuchungHoerenAudiogrammResourceGetUntersuchungHoerenAudiogramm()
            .anlassIdPath(einladung.getAnlass().getId().getId())
            .einladungIdPath(toHasUntersuchungIdWrapper(einladung.getId()))
            .execute(ResponseBody::prettyPeek)
            .then()
            .assertThat()
            .statusCode(Status.NO_CONTENT.getStatusCode());
    }

    @Test
    @TestAsAdminBS
    void getUntersuchungHoerenAudiogramm_existing_should_return_existing() {
        // ARRANGE
        Untersuchung untersuchung = fixtureService.createUntersuchungWithHoerenAudiogramm();
        Assertions.assertThat(untersuchung.getUntersuchungHoerenAudiogramm()).isNotNull();

        UntersuchungHoerenAudiogramm existingHoeren =
            untersuchungHoerenAudiogrammService.getById(untersuchung.getUntersuchungHoerenAudiogramm().getId());
        Assertions.assertThat(existingHoeren).isNotNull();
        Assertions.assertThat(existingHoeren.getBemerkungen()).isNotNull();
        // ACT
        UntersuchungHoerenAudiogrammJsonSpec result = api
            .untersuchungHoerenAudiogrammResourceGetUntersuchungHoerenAudiogramm()
            .anlassIdPath(untersuchung.getAnlass().getId().getId())
            .einladungIdPath(toHasUntersuchungIdWrapper(untersuchung.getEinladungRequired().getId()))
            .execute(ResponseBody::prettyPeek)
            .then()
            .assertThat()
            .statusCode(Status.OK.getStatusCode())
            .extract().as(UntersuchungHoerenAudiogrammJsonSpec.class);
        // ASSERT
        Assertions.assertThat(result).isNotNull();
        Assertions.assertThat(result.getId()).isEqualTo(existingHoeren.getId().getId());
        Assertions.assertThat(result.getAudiogrammData().getBemerkungen())
            .isEqualTo(existingHoeren.getBemerkungen());
    }

    @Test
    @TestAsAdminBS
    void saveUntersuchungHoerenAudiogramm() {
        // ARRANGE
        Einladung einladung = fixtureService.createEinladung();
        Untersuchung untersuchung = fixtureService.createUntersuchungWithHoerenAudiogramm();
        Assertions.assertThat(untersuchung.getUntersuchungHoerenAudiogramm()).isNotNull();
        UntersuchungHoerenAudiogramm existingHoeren =
            untersuchungHoerenAudiogrammService.getById(untersuchung.getUntersuchungHoerenAudiogramm().getId());
        Assertions.assertThat(existingHoeren).isNotNull();
        Assertions.assertThat(existingHoeren.getAudiogrammRechts()).isNotNull();
        Assertions.assertThat(existingHoeren.getAudiogrammLinks()).isNotNull();
        Assertions.assertThat(existingHoeren.getAudiogrammBeurteilung()).isNotNull();
        Assertions.assertThat(existingHoeren.getZusaetzlicheAngaben()).isNotNull();
        AudiogrammSaveJsonSpec expectedAudiogramm = new AudiogrammSaveJsonSpec(
            DezibelSpec.DB30,
            DezibelSpec.DB30,
            DezibelSpec.DB30,
            DezibelSpec.DB30,
            DezibelSpec.DB30,
            DezibelSpec.DB30,
            DezibelSpec.DB30,
            0L
        );

        // ACT
        UntersuchungHoerenAudiogrammSaveJsonSpec saveJsonSpec =
            UntersuchungHoerenAudiogrammBuilder.createJaxSpec(builder -> builder
                .forUntersuchungHoerenAudiogramm(existingHoeren)
                .withAudiogrammRechts(Dezibel.DB30)
                .withAudiogrammLinks(Dezibel.DB30)
                .withAudiogrammBeurteilung(List.of(AudiogrammBeurteilung.LINKS_REDUZIERT), null)
                .withZusaetzlicheAngaben(List.of(HoerenZusaetzlicheAngaben.ERKAELTUNG), null)
                .withBemerkung("Bemerkung Audiogramm")).getAudiogrammData();

        UntersuchungHoerenAudiogrammJsonSpec result = api
            .untersuchungHoerenAudiogrammResourceSaveUntersuchungHoerenAudiogramm()
            .anlassIdPath(einladung.getAnlass().getId().getId())
            .einladungIdPath(toHasUntersuchungIdWrapper(einladung.getId()))
            .body(saveJsonSpec)
            .execute(ResponseBody::prettyPeek)
            .then()
            .assertThat()
            .statusCode(Status.OK.getStatusCode())
            .extract().as(UntersuchungHoerenAudiogrammJsonSpec.class);

        // ASSERT
        Assertions.assertThat(result).isNotNull();
        Assertions.assertThat(result.getId()).isNotNull();
        Assertions.assertThat(result.getAudiogrammData()).isNotNull();
        Assertions.assertThat(result.getAudiogrammData().getAudiogrammRechts()).isNotNull();
        Assertions.assertThat(result.getAudiogrammData().getAudiogrammLinks()).isNotNull();
        Assertions.assertThat(result.getAudiogrammData().getAudiogrammRechts()).isEqualTo(expectedAudiogramm);
        Assertions.assertThat(result.getAudiogrammData().getAudiogrammLinks()).isEqualTo(expectedAudiogramm);
        Assertions.assertThat(result.getAudiogrammData().getAudiogrammBeurteilung().getValue())
            .contains(AudiogrammBeurteilungSpec.LINKS_REDUZIERT.name());
        Assertions.assertThat(result.getAudiogrammData().getZusaetzlicheAngaben().getValue())
            .contains(HoerenZusaetzlicheAngabenSpec.ERKAELTUNG.name());
        Assertions.assertThat(result.getAudiogrammData().getBemerkungen())
            .isNotNull()
            .isEqualTo("Bemerkung Audiogramm");
    }

    @Test
    @TestAsSchularztBE
    void saveUntersuchungHoerenAudiogramm_anonym() {
        // ARRANGE
        tenantContext.setCurrentTenant(TenantEnum.BE);

        // Create anlass with arzt set (to pass authorizer)
        Benutzer schularzt = fixtureService.readBenutzerWithBerechtigungen(TestAsSchularztBE.BENUTZER_ID_ID);
        AnonymerEintrag anonymerEintrag = fixtureService.createAnonymerEintragForArzt(schularzt);
        Untersuchung untersuchung = untersuchungService.getOrCreateUntersuchungAnonym(anonymerEintrag.getId(), false);
        assertThat(untersuchung.getUntersuchungHoerenAudiogramm()).isNull();

        AudiogrammSaveJsonSpec expectedAudiogramm = new AudiogrammSaveJsonSpec(
            DezibelSpec.DB30,
            DezibelSpec.DB30,
            DezibelSpec.DB30,
            DezibelSpec.DB30,
            DezibelSpec.DB30,
            DezibelSpec.DB30,
            DezibelSpec.DB30,
            0L
        );

        // ACT
        UntersuchungHoerenAudiogrammSaveJsonSpec saveJsonSpec =
            UntersuchungHoerenAudiogrammBuilder.createSaveJaxSpec(builder -> builder
                .withAudiogrammRechts(Dezibel.DB30)
                .withAudiogrammLinks(Dezibel.DB30)
                .withAudiogrammBeurteilung(List.of(AudiogrammBeurteilung.LINKS_REDUZIERT), null)
                .withZusaetzlicheAngaben(List.of(HoerenZusaetzlicheAngaben.ERKAELTUNG), null)
                .withBemerkung("Bemerkung Audiogramm"));

        UntersuchungHoerenAudiogrammJsonSpec result = api
            .untersuchungHoerenAudiogrammResourceSaveUntersuchungHoerenAudiogramm()
            .anlassIdPath(anonymerEintrag.getAnlass().getId().getId())
            .einladungIdPath(toHasAnonymerEintragIdWrapper(anonymerEintrag.getId()))
            .body(saveJsonSpec)
            .execute(ResponseBody::prettyPeek)
            .then()
            .assertThat()
            .statusCode(Status.OK.getStatusCode())
            .extract().as(UntersuchungHoerenAudiogrammJsonSpec.class);

        // ASSERT
        Assertions.assertThat(result).isNotNull();
        Assertions.assertThat(result.getId()).isNotNull();
        Assertions.assertThat(result.getAudiogrammData()).isNotNull();
        Assertions.assertThat(result.getAudiogrammData().getAudiogrammRechts()).isNotNull();
        Assertions.assertThat(result.getAudiogrammData().getAudiogrammLinks()).isNotNull();
        Assertions.assertThat(result.getAudiogrammData().getAudiogrammRechts()).isEqualTo(expectedAudiogramm);
        Assertions.assertThat(result.getAudiogrammData().getAudiogrammLinks()).isEqualTo(expectedAudiogramm);
        Assertions.assertThat(result.getAudiogrammData().getAudiogrammBeurteilung().getValue())
            .contains(AudiogrammBeurteilungSpec.LINKS_REDUZIERT.name());
        Assertions.assertThat(result.getAudiogrammData().getZusaetzlicheAngaben().getValue())
            .contains(HoerenZusaetzlicheAngabenSpec.ERKAELTUNG.name());
        Assertions.assertThat(result.getAudiogrammData().getBemerkungen())
            .isNotNull()
            .isEqualTo("Bemerkung Audiogramm");
        assertThat(untersuchungService.getOrCreateUntersuchungAnonym(anonymerEintrag.getId(), true)
            .getUntersuchungVorabklaerungen()).isNull();
    }

    @Test
    @TestAsAdminBS
    void getDefaultUntersuchungHoerenAudiogrammForAnlassConfig() {
        // ARRANGE
        Anlass anlass = fixtureService.createAnlass();
        Einladung einladung = fixtureService.createEinladung(anlass);
        AudiogrammSaveJsonSpec defaultAudiogramm = new AudiogrammSaveJsonSpec(
            DezibelSpec.DB20,
            DezibelSpec.DB20,
            DezibelSpec.DB20,
            DezibelSpec.DB20,
            DezibelSpec.DB20,
            DezibelSpec.DB20,
            DezibelSpec.DB20,
            0L
        );

        // ACT
        UntersuchungHoerenAudiogrammSaveJsonSpec result =
            api.untersuchungHoerenAudiogrammResourceGetDefaultUntersuchungHoerenAudiogrammForAnlassConfig()
                .anlassIdPath(einladung.getAnlass().getId().getId())
                .einladungIdPath(toHasUntersuchungIdWrapper(einladung.getId()))
                .execute(ResponseBody::prettyPeek)
                .then()
                .assertThat()
                .statusCode(Status.OK.getStatusCode())
                .extract()
                .as(UntersuchungHoerenAudiogrammSaveJsonSpec.class);

        // ASSERT
        assertThat(result).isNotNull();
        assertThat(result.getAudiogrammRechts()).isEqualTo(defaultAudiogramm);
        assertThat(result.getAudiogrammLinks()).isEqualTo(defaultAudiogramm);
        assertThat(result.getAudiogrammBeurteilung()
            .getValue()).isEqualTo(List.of(AudiogrammBeurteilungSpec.NORMAL.name()));
        assertThat(result.getZusaetzlicheAngaben()
            .getValue()).isEqualTo(List.of(HoerenZusaetzlicheAngabenSpec.KEINE.name()));
        assertThat(result.getBemerkungen()).isNull();
    }

    @Test
    @TestAsAdminBS
    void getDefaultUntersuchungHoerenAudiogrammForAnlassConfig_NOT_VISIBLE() {
        // ARRANGE
        Anlass anlass = fixtureService.createAnlassWithSameRequirementSettings(RequirementSetting.NOT_VISIBLE);
        Einladung einladung = fixtureService.createEinladung(anlass);

        // ACT
        UntersuchungHoerenAudiogrammSaveJsonSpec result =
            api.untersuchungHoerenAudiogrammResourceGetDefaultUntersuchungHoerenAudiogrammForAnlassConfig()
                .anlassIdPath(einladung.getAnlass().getId().getId())
                .einladungIdPath(toHasUntersuchungIdWrapper(einladung.getId()))
                .execute(ResponseBody::prettyPeek)
                .then()
                .assertThat()
                .statusCode(Status.OK.getStatusCode())
                .extract()
                .as(UntersuchungHoerenAudiogrammSaveJsonSpec.class);

        // ASSERT
        assertThat(result).isNotNull();
        assertThat(result.getAudiogrammRechts()).isNull();
        assertThat(result.getAudiogrammLinks()).isNull();
        assertThat(result.getAudiogrammBeurteilung().getValue()).isEmpty();
        assertThat(result.getZusaetzlicheAngaben().getValue()).isEmpty();
    }
}
