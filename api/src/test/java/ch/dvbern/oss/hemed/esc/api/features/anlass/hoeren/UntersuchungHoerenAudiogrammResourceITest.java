package ch.dvbern.oss.hemed.esc.api.features.anlass.hoeren;

import java.util.List;

import ch.dvbern.oss.hemed.esc.api.features.anlass.Anlass;
import ch.dvbern.oss.hemed.esc.api.features.anlass.einladung.Einladung;
import ch.dvbern.oss.hemed.esc.api.features.anlass.hoeren.types.AudiogrammBeurteilung;
import ch.dvbern.oss.hemed.esc.api.features.anlass.hoeren.types.Dezibel;
import ch.dvbern.oss.hemed.esc.api.features.anlass.hoeren.types.HoerenZusaetzlicheAngaben;
import ch.dvbern.oss.hemed.esc.api.features.anlass.untersuchung.Untersuchung;
import ch.dvbern.oss.hemed.esc.api.features.anlass.untersuchung.UntersuchungService;
import ch.dvbern.oss.hemed.esc.api.features.anlass.anlassconfig.types.RequirementSetting;
import ch.dvbern.oss.hemed.esc.api.testutil.AbstractITest;
import ch.dvbern.oss.hemed.esc.api.testutil.FixtureService;
import ch.dvbern.oss.hemed.esc.api.testutil.builder.UntersuchungHoerenAudiogrammBuilder;
import ch.dvbern.oss.hemed.esc.api.testutil.restassured.RequestSpecUtil;
import ch.dvbern.oss.hemed.esc.api.testutil.security.TestAsAdminBS;
import ch.dvbern.oss.hemed.esc.generated.restassured.api.UntersuchungHoerenAudiogrammApiSpec;
import ch.dvbern.oss.hemed.esc.generated.restassured.dto.AudiogrammBeurteilungSpec;
import ch.dvbern.oss.hemed.esc.generated.restassured.dto.AudiogrammSaveJsonSpec;
import ch.dvbern.oss.hemed.esc.generated.restassured.dto.DezibelSpec;
import ch.dvbern.oss.hemed.esc.generated.restassured.dto.HoerenZusaetzlicheAngabenSpec;
import ch.dvbern.oss.hemed.esc.generated.restassured.dto.UntersuchungHoerenAudiogrammJsonSpec;
import ch.dvbern.oss.hemed.esc.generated.restassured.dto.UntersuchungHoerenAudiogrammSaveJsonSpec;
import io.quarkus.test.junit.QuarkusTest;
import io.restassured.response.ResponseBody;
import jakarta.ws.rs.core.Response.Status;
import lombok.RequiredArgsConstructor;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@QuarkusTest
@RequiredArgsConstructor
class UntersuchungHoerenAudiogrammResourceITest extends AbstractITest {

    private final UntersuchungHoerenAudiogrammApiSpec api =
        UntersuchungHoerenAudiogrammApiSpec.untersuchungHoerenAudiogramm(RequestSpecUtil.quarkusSpec());

    private final UntersuchungService untersuchungService;
    private final UntersuchungHoerenAudiogrammService untersuchungHoerenAudiogrammService;
    private final FixtureService fixtureService;

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
            .einladungIdPath(einladung.getId().getId())
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
            .anlassIdPath(untersuchung.getEinladung().getAnlass().getId().getId())
            .einladungIdPath(untersuchung.getEinladung().getId().getId())
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
                .withAudiogrammBeurteilung(List.of(AudiogrammBeurteilung.LINKS_REDUZIERT))
                .withZusaetzlicheAngaben(HoerenZusaetzlicheAngaben.ERKAELTUNG)
                .withBemerkung("Bemerkung Audiogramm")).getAudiogrammData();

        UntersuchungHoerenAudiogrammJsonSpec result = api
            .untersuchungHoerenAudiogrammResourceSaveUntersuchungHoerenAudiogramm()
            .anlassIdPath(einladung.getAnlass().getId().getId())
            .einladungIdPath(einladung.getId().getId())
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
        Assertions.assertThat(result.getAudiogrammData().getAudiogrammBeurteilung())
            .contains(AudiogrammBeurteilungSpec.LINKS_REDUZIERT);
        Assertions.assertThat(result.getAudiogrammData().getZusaetzlicheAngaben())
            .contains(HoerenZusaetzlicheAngabenSpec.ERKAELTUNG);
        Assertions.assertThat(result.getAudiogrammData().getBemerkungen())
            .isNotNull()
            .isEqualTo("Bemerkung Audiogramm");
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
                .einladungIdPath(einladung.getId().getId())
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
        assertThat(result.getAudiogrammBeurteilung()).isEqualTo(List.of(AudiogrammBeurteilungSpec.NORMAL));
        assertThat(result.getZusaetzlicheAngaben()).isEqualTo(List.of(HoerenZusaetzlicheAngabenSpec.KEINE));
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
                .einladungIdPath(einladung.getId().getId())
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
        assertThat(result.getAudiogrammBeurteilung()).isEmpty();
        assertThat(result.getZusaetzlicheAngaben()).isEmpty();
    }
}
