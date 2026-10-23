package ch.dvbern.oss.hemed.esc.api.features.anlass.hoeren;

import java.util.List;

import ch.dvbern.oss.hemed.esc.api.features.anlass.Anlass;
import ch.dvbern.oss.hemed.esc.api.features.anlass.anlassconfig.types.RequirementSetting;
import ch.dvbern.oss.hemed.esc.api.features.anlass.anonymereintrag.AnonymerEintrag;
import ch.dvbern.oss.hemed.esc.api.features.anlass.einladung.Einladung;
import ch.dvbern.oss.hemed.esc.api.features.anlass.hoeren.types.OtoskopieResultat;
import ch.dvbern.oss.hemed.esc.api.features.anlass.types.AnlassTyp;
import ch.dvbern.oss.hemed.esc.api.features.anlass.untersuchung.Untersuchung;
import ch.dvbern.oss.hemed.esc.api.features.anlass.untersuchung.UntersuchungService;
import ch.dvbern.oss.hemed.esc.api.shared.auth.TenantEnum;
import ch.dvbern.oss.hemed.esc.api.shared.auth.TenantContext;
import ch.dvbern.oss.hemed.esc.api.shared.benutzer.Benutzer;
import ch.dvbern.oss.hemed.esc.api.testutil.FixtureService;
import ch.dvbern.oss.hemed.esc.api.testutil.builder.UntersuchungHoerenOtoskopieBuilder;
import ch.dvbern.oss.hemed.esc.api.testutil.restassured.RequestSpecUtil;
import ch.dvbern.oss.hemed.esc.api.testutil.security.TestAsAdminBS;
import ch.dvbern.oss.hemed.esc.api.testutil.security.TestAsSchularztBE;
import ch.dvbern.oss.hemed.esc.generated.restassured.api.UntersuchungHoerenOtoskopieApiSpec;
import ch.dvbern.oss.hemed.esc.generated.restassured.dto.OtoskopieResultatSpec;
import ch.dvbern.oss.hemed.esc.generated.restassured.dto.UntersuchungHoerenOtoskopieJsonSpec;
import ch.dvbern.oss.hemed.esc.generated.restassured.dto.UntersuchungHoerenOtoskopieSaveJsonSpec;
import io.quarkus.test.junit.QuarkusTest;
import io.restassured.response.ResponseBody;
import jakarta.ws.rs.core.Response.Status;
import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.Test;

import static ch.dvbern.oss.hemed.esc.api.testutil.restassured.RequestSpecUtil.toHasAnonymerEintragIdWrapper;
import static ch.dvbern.oss.hemed.esc.api.testutil.restassured.RequestSpecUtil.toHasUntersuchungIdWrapper;
import static org.assertj.core.api.Assertions.assertThat;

@QuarkusTest
@RequiredArgsConstructor
class UntersuchungHoerenOtoskopieResourceITest {

    private final UntersuchungHoerenOtoskopieApiSpec api =
        UntersuchungHoerenOtoskopieApiSpec.untersuchungHoerenOtoskopie(RequestSpecUtil.quarkusSpec());

    private final UntersuchungService untersuchungService;
    private final UntersuchungHoerenOtoskopieService untersuchungHoerenOtoskopieService;
    private final FixtureService fixtureService;
    private final TenantContext tenantContext;

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
            .einladungIdPath(toHasUntersuchungIdWrapper(einladung.getId()))
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
            .anlassIdPath(untersuchung.getAnlass().getId().getId())
            .einladungIdPath(toHasUntersuchungIdWrapper(untersuchung.getEinladungRequired().getId()))
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
            .einladungIdPath(toHasUntersuchungIdWrapper(einladung.getId()))
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
    @TestAsSchularztBE
    void saveUntersuchungHoerenOtoskopie_anonym() {
        // ARRANGE
        tenantContext.setCurrentTenant(TenantEnum.BE);

        Benutzer schularzt = fixtureService.readBenutzerWithBerechtigungen(TestAsSchularztBE.BENUTZER_ID_ID);
        AnonymerEintrag anonymerEintrag = fixtureService.createAnonymerEintragForArzt(schularzt);
        Untersuchung untersuchung = untersuchungService.getOrCreateUntersuchungAnonym(anonymerEintrag.getId(), false);
        assertThat(untersuchung.getUntersuchungHoerenOtoskopie()).isNull();

        // ACT
        UntersuchungHoerenOtoskopieSaveJsonSpec saveJsonSpec =
            UntersuchungHoerenOtoskopieBuilder.createSaveJaxSpec(builder -> builder
                .withOtoskopieLinks(List.of(OtoskopieResultat.TROMMELFELL_CERUMEN), null)
                .withOtoskopieRechts(List.of(OtoskopieResultat.TROMMELFELL_CERUMEN), null)
            );

        UntersuchungHoerenOtoskopieJsonSpec result = api
            .untersuchungHoerenOtoskopieResourceSaveUntersuchungHoerenOtoskopie()
            .anlassIdPath(anonymerEintrag.getAnlass().getId().getId())
            .einladungIdPath(toHasAnonymerEintragIdWrapper(anonymerEintrag.getId()))
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
        assertThat(untersuchungService.getOrCreateUntersuchungAnonym(anonymerEintrag.getId(), true)
            .getUntersuchungHoerenOtoskopie()).isNotNull();
    }

    @Test
    @TestAsAdminBS
    void saveUntersuchungHoerenOtoskopie_should_respect_independence_between_links_and_rechts() {
        // ARRANGE
        // createAnlassWithSameRequirementSettings already persists the config
        Anlass anlass = fixtureService.createAnlassWithSameRequirementSettings(RequirementSetting.REQUIRED);
        Einladung einladung = fixtureService.createEinladung(anlass);
        untersuchungService.getOrCreateUntersuchung(einladung.getId());

        // ACT
        UntersuchungHoerenOtoskopieSaveJsonSpec saveJsonSpec =
            UntersuchungHoerenOtoskopieBuilder.createJaxSpec(builder -> builder
                .withOtoskopieRechts(List.of(OtoskopieResultat.NORMAL), null, null)
                .withOtoskopieLinks(List.of(OtoskopieResultat.ANDERES), null, "Bemerkung Links")
                .withBemerkung("Bemerkung Allgemein")
            ).getOtoskopieData();

        UntersuchungHoerenOtoskopieJsonSpec result = api
            .untersuchungHoerenOtoskopieResourceSaveUntersuchungHoerenOtoskopie()
            .anlassIdPath(einladung.getAnlass().getId().getId())
            .einladungIdPath(toHasUntersuchungIdWrapper(einladung.getId()))
            .body(saveJsonSpec)
            .execute(ResponseBody::prettyPeek)
            .then()
            .assertThat()
            .statusCode(200)
            .extract().as(UntersuchungHoerenOtoskopieJsonSpec.class);

        // ASSERT
        assertThat(result.getOtoskopieData().getOtoskopieRechtsResultatAndere()).isNull();
        assertThat(result.getOtoskopieData().getOtoskopieLinksResultatAndere()).isEqualTo("Bemerkung Links");
        assertThat(result.getOtoskopieData().getBemerkungen()).isEqualTo("Bemerkung Allgemein");
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
                .einladungIdPath(toHasUntersuchungIdWrapper(einladung.getId()))
                .execute(ResponseBody::prettyPeek)
                .then()
                .assertThat()
                .statusCode(Status.OK.getStatusCode())
                .extract()
                .as(UntersuchungHoerenOtoskopieSaveJsonSpec.class);

        // ASSERT
        assertThat(result).isNotNull();
        assertThat(result.getOtoskopieRechts()
            .getValue()).isEqualTo(List.of(OtoskopieResultatSpec.NICHT_GEPRUEFT.name()));
        assertThat(result.getOtoskopieRechtsResultatAndere()).isNull();
        assertThat(result.getOtoskopieLinks()
            .getValue()).isEqualTo(List.of(OtoskopieResultatSpec.NICHT_GEPRUEFT.name()));
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
                .einladungIdPath(toHasUntersuchungIdWrapper(einladung.getId()))
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
                .einladungIdPath(toHasUntersuchungIdWrapper(einladung.getId()))
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
