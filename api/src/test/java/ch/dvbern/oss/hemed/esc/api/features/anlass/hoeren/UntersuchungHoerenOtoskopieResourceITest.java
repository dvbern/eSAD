package ch.dvbern.oss.hemed.esc.api.features.anlass.hoeren;

import java.util.List;

import ch.dvbern.oss.hemed.esc.api.features.anlass.Anlass;
import ch.dvbern.oss.hemed.esc.api.features.anlass.anlassconfig.types.RequirementSetting;
import ch.dvbern.oss.hemed.esc.api.features.anlass.einladung.Einladung;
import ch.dvbern.oss.hemed.esc.api.features.anlass.hoeren.types.OtoskopieResultat;
import ch.dvbern.oss.hemed.esc.api.features.anlass.types.AnlassTyp;
import ch.dvbern.oss.hemed.esc.api.features.anlass.untersuchung.Untersuchung;
import ch.dvbern.oss.hemed.esc.api.features.anlass.untersuchung.UntersuchungService;
import ch.dvbern.oss.hemed.esc.api.testutil.FixtureService;
import ch.dvbern.oss.hemed.esc.api.testutil.builder.UntersuchungHoerenOtoskopieBuilder;
import ch.dvbern.oss.hemed.esc.api.testutil.restassured.RequestSpecUtil;
import ch.dvbern.oss.hemed.esc.api.testutil.security.TestAsAdminBS;
import ch.dvbern.oss.hemed.esc.generated.restassured.api.UntersuchungHoerenOtoskopieApiSpec;
import ch.dvbern.oss.hemed.esc.generated.restassured.dto.OtoskopieResultatSpec;
import ch.dvbern.oss.hemed.esc.generated.restassured.dto.UntersuchungHoerenOtoskopieJsonSpec;
import ch.dvbern.oss.hemed.esc.generated.restassured.dto.UntersuchungHoerenOtoskopieSaveJsonSpec;
import io.quarkus.test.junit.QuarkusTest;
import io.restassured.response.ResponseBody;
import jakarta.ws.rs.core.Response.Status;
import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@QuarkusTest
@RequiredArgsConstructor
class UntersuchungHoerenOtoskopieResourceITest {

    private final UntersuchungHoerenOtoskopieApiSpec api =
        UntersuchungHoerenOtoskopieApiSpec.untersuchungHoerenOtoskopie(RequestSpecUtil.quarkusSpec());

    private final UntersuchungService untersuchungService;
    private final UntersuchungHoerenOtoskopieService untersuchungHoerenOtoskopieService;
    private final FixtureService fixtureService;

    @Test
    @TestAsAdminBS
    void getUntersuchungHoerenOtoskopie_non_existing_should_not_create_data() {
        // ARRANGE
        Einladung einladung = fixtureService.createEinladung();
        untersuchungService.getOrCreateUntersuchung(einladung.getId());
        // ACT / ASSERT
        api
            .untersuchungHoerenOtoskopieResourceGetUntersuchungHoerenOtoskopie()
            .anlassIdPath(einladung.getAnlass().getId().getId())
            .einladungIdPath(einladung.getId().getId())
            .execute(ResponseBody::prettyPeek)
            .then()
            .assertThat()
            .statusCode(Status.NO_CONTENT.getStatusCode());
    }

    @Test
    @TestAsAdminBS
    void getUntersuchungHoerenOtoskopie_existing_should_return_existing() {
        // ARRANGE
        Untersuchung untersuchung = fixtureService.createUntersuchungWithHoerenOtoskopie();
        assertThat(untersuchung.getUntersuchungHoerenOtoskopie()).isNotNull();

        UntersuchungHoerenOtoskopie existingHoeren =
            untersuchungHoerenOtoskopieService.getById(untersuchung.getUntersuchungHoerenOtoskopie().getId());
        assertThat(existingHoeren).isNotNull();
        assertThat(existingHoeren.getBemerkungen()).isNotNull();
        // ACT
        UntersuchungHoerenOtoskopieJsonSpec result = api
            .untersuchungHoerenOtoskopieResourceGetUntersuchungHoerenOtoskopie()
            .anlassIdPath(untersuchung.getEinladung().getAnlass().getId().getId())
            .einladungIdPath(untersuchung.getEinladung().getId().getId())
            .execute(ResponseBody::prettyPeek)
            .then()
            .assertThat()
            .statusCode(Status.OK.getStatusCode())
            .extract().as(UntersuchungHoerenOtoskopieJsonSpec.class);
        // ASSERT
        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(existingHoeren.getId().getId());
        assertThat(result.getOtoskopieData().getBemerkungen())
            .isEqualTo(existingHoeren.getBemerkungen());
    }

    @Test
    @TestAsAdminBS
    void saveUntersuchungHoerenOtoskopie() {
        // ARRANGE
        Einladung einladung = fixtureService.createEinladung();
        Untersuchung untersuchung = fixtureService.createUntersuchungWithHoerenOtoskopie();
        assertThat(untersuchung.getUntersuchungHoerenOtoskopie()).isNotNull();
        UntersuchungHoerenOtoskopie existingHoeren =
            untersuchungHoerenOtoskopieService.getById(untersuchung.getUntersuchungHoerenOtoskopie().getId());
        assertThat(existingHoeren).isNotNull();
        assertThat(existingHoeren.getOtoskopieRechts()).isNotNull();
        assertThat(existingHoeren.getOtoskopieRechtsResultatAndere()).isNull();
        assertThat(existingHoeren.getOtoskopieLinks()).isNotNull();
        assertThat(existingHoeren.getOtoskopieLinksResultatAndere()).isNull();

        // ACT
        UntersuchungHoerenOtoskopieSaveJsonSpec saveJsonSpec =
            UntersuchungHoerenOtoskopieBuilder.createJaxSpec(builder -> builder
                .forUntersuchungHoerenOtoskopie(existingHoeren)
                .withOtoskopieLinks(List.of(OtoskopieResultat.TROMMELFELL_CERUMEN), null)
                .withOtoskopieRechts(List.of(OtoskopieResultat.TROMMELFELL_CERUMEN), null)
            ).getOtoskopieData();

        UntersuchungHoerenOtoskopieJsonSpec result = api
            .untersuchungHoerenOtoskopieResourceSaveUntersuchungHoerenOtoskopie()
            .anlassIdPath(einladung.getAnlass().getId().getId())
            .einladungIdPath(einladung.getId().getId())
            .body(saveJsonSpec)
            .execute(ResponseBody::prettyPeek)
            .then()
            .assertThat()
            .statusCode(Status.OK.getStatusCode())
            .extract().as(UntersuchungHoerenOtoskopieJsonSpec.class);

        // ASSERT
        assertThat(result).isNotNull();
        assertThat(result.getId()).isNotNull();
        assertThat(result.getOtoskopieData()).isNotNull();
        assertThat(result.getOtoskopieData().getOtoskopieRechts()).isNotNull();
        assertThat(result.getOtoskopieData().getOtoskopieLinks()).isNotNull();
        assertThat(result.getOtoskopieData().getOtoskopieRechts().getValue())
            .contains(OtoskopieResultatSpec.TROMMELFELL_CERUMEN.name());
        assertThat(result.getOtoskopieData().getOtoskopieLinks().getValue())
            .contains(OtoskopieResultatSpec.TROMMELFELL_CERUMEN.name());
    }

    @Test
    @TestAsAdminBS
    void getDefaultUntersuchungHoerenOtoskopieForAnlassConfig() {
        // ARRANGE
        Anlass anlass = fixtureService.createAnlass();
        Einladung einladung = fixtureService.createEinladung(anlass);

        // ACT
        UntersuchungHoerenOtoskopieSaveJsonSpec result =
            api.untersuchungHoerenOtoskopieResourceGetDefaultUntersuchungHoerenOtoskopieForAnlassConfig()
                .anlassIdPath(einladung.getAnlass().getId().getId())
                .einladungIdPath(einladung.getId().getId())
                .execute(ResponseBody::prettyPeek)
                .then()
                .assertThat()
                .statusCode(Status.OK.getStatusCode())
                .extract()
                .as(UntersuchungHoerenOtoskopieSaveJsonSpec.class);

        // ASSERT
        assertThat(result).isNotNull();
        assertThat(result.getOtoskopieRechts().getValue()).isEqualTo(List.of(OtoskopieResultatSpec.NICHT_GEPRUEFT.name()));
        assertThat(result.getOtoskopieRechtsResultatAndere()).isNull();
        assertThat(result.getOtoskopieLinks().getValue()).isEqualTo(List.of(OtoskopieResultatSpec.NICHT_GEPRUEFT.name()));
        assertThat(result.getOtoskopieLinksResultatAndere()).isNull();
    }

    @Test
    @TestAsAdminBS
    void getDefaultUntersuchungHoerenOtoskopieForAnlassConfigForKindergarten() {
        // ARRANGE
        Anlass anlass = fixtureService.createAnlass(AnlassTyp.KINDERGARTEN);
        Einladung einladung = fixtureService.createEinladung(anlass);

        // ACT
        UntersuchungHoerenOtoskopieSaveJsonSpec result =
            api.untersuchungHoerenOtoskopieResourceGetDefaultUntersuchungHoerenOtoskopieForAnlassConfig()
                .anlassIdPath(einladung.getAnlass().getId().getId())
                .einladungIdPath(einladung.getId().getId())
                .execute(ResponseBody::prettyPeek)
                .then()
                .assertThat()
                .statusCode(Status.OK.getStatusCode())
                .extract()
                .as(UntersuchungHoerenOtoskopieSaveJsonSpec.class);

        // ASSERT
        assertThat(result).isNotNull();
        assertThat(result.getOtoskopieRechts().getValue()).isEmpty();
        assertThat(result.getOtoskopieRechtsResultatAndere()).isNull();
        assertThat(result.getOtoskopieLinks().getValue()).isEmpty();
        assertThat(result.getOtoskopieLinksResultatAndere()).isNull();
    }

    @Test
    @TestAsAdminBS
    void getDefaultUntersuchungOtoskopieForAnlassConfig_NOT_VISIBLE() {
        // ARRANGE
        Anlass anlass = fixtureService.createAnlassWithSameRequirementSettings(RequirementSetting.NOT_VISIBLE);
        Einladung einladung = fixtureService.createEinladung(anlass);

        // ACT
        UntersuchungHoerenOtoskopieSaveJsonSpec result =
            api.untersuchungHoerenOtoskopieResourceGetDefaultUntersuchungHoerenOtoskopieForAnlassConfig()
                .anlassIdPath(einladung.getAnlass().getId().getId())
                .einladungIdPath(einladung.getId().getId())
                .execute(ResponseBody::prettyPeek)
                .then()
                .assertThat()
                .statusCode(Status.OK.getStatusCode())
                .extract()
                .as(UntersuchungHoerenOtoskopieSaveJsonSpec.class);

        // ASSERT
        assertThat(result).isNotNull();
        assertThat(result.getOtoskopieRechts().getValue()).isEmpty();
        assertThat(result.getOtoskopieRechtsResultatAndere()).isNull();
        assertThat(result.getOtoskopieLinks().getValue()).isEmpty();
        assertThat(result.getOtoskopieLinksResultatAndere()).isNull();
    }
}
