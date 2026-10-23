package ch.dvbern.oss.hemed.esc.api.features.anlass.groessegewicht;

import java.math.BigDecimal;

import ch.dvbern.dvbstarter.shared.types.id.HasUntersuchungIDWrapper;
import ch.dvbern.oss.hemed.esc.api.features.anlass.anonymereintrag.AnonymerEintrag;
import ch.dvbern.oss.hemed.esc.api.features.anlass.einladung.Einladung;
import ch.dvbern.oss.hemed.esc.api.features.anlass.untersuchung.Untersuchung;
import ch.dvbern.oss.hemed.esc.api.features.anlass.untersuchung.UntersuchungService;
import ch.dvbern.oss.hemed.esc.api.shared.auth.TenantEnum;
import ch.dvbern.oss.hemed.esc.api.shared.auth.TenantContext;
import ch.dvbern.oss.hemed.esc.api.shared.benutzer.Benutzer;
import ch.dvbern.oss.hemed.esc.api.testutil.AbstractITest;
import ch.dvbern.oss.hemed.esc.api.testutil.FixtureService;
import ch.dvbern.oss.hemed.esc.api.testutil.builder.UntersuchungGroesseGewichtBuilder;
import ch.dvbern.oss.hemed.esc.api.testutil.restassured.RequestSpecUtil;
import ch.dvbern.oss.hemed.esc.api.testutil.security.TestAsAdminBS;
import ch.dvbern.oss.hemed.esc.api.testutil.security.TestAsSchularztBE;
import ch.dvbern.oss.hemed.esc.generated.restassured.api.UntersuchungGroesseGewichtApiSpec;
import ch.dvbern.oss.hemed.esc.generated.restassured.dto.UntersuchungGroesseGewichtDataJsonSpec;
import ch.dvbern.oss.hemed.esc.generated.restassured.dto.UntersuchungGroesseGewichtSaveJsonSpec;
import io.quarkus.test.junit.QuarkusTest;
import io.restassured.response.ResponseBody;
import jakarta.ws.rs.core.Response.Status;
import lombok.RequiredArgsConstructor;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;

import static ch.dvbern.oss.hemed.esc.api.testutil.restassured.RequestSpecUtil.toHasAnonymerEintragIdWrapper;
import static ch.dvbern.oss.hemed.esc.api.testutil.restassured.RequestSpecUtil.toHasUntersuchungIdWrapper;

@QuarkusTest
@RequiredArgsConstructor
class UntersuchungGroesseGewichtResourceITest extends AbstractITest {

    private final UntersuchungGroesseGewichtApiSpec api =
        UntersuchungGroesseGewichtApiSpec.untersuchungGroesseGewicht(RequestSpecUtil.quarkusSpec());

    private final UntersuchungService untersuchungService;
    private final UntersuchungGroesseGewichtService untersuchungGroesseGewichtService;
    private final FixtureService fixtureService;
    private final TenantContext tenantContext;

    @Test
    @TestAsAdminBS
    void getUntersuchungGroesseGewicht_non_existing_should_not_create_data() {
        // ARRANGE
        Einladung einladung = fixtureService.createEinladung();
        untersuchungService.getOrCreateUntersuchung(HasUntersuchungIDWrapper.fromEinladungId(einladung.getId()));
        // ACT
        UntersuchungGroesseGewichtDataJsonSpec result = api
            .untersuchungGroesseGewichtResourceGetUntersuchungGroesseGewicht()
            .anlassIdPath(einladung.getAnlass().getId().getId())
            .einladungIdPath(toHasUntersuchungIdWrapper(einladung.getId()))
            .execute(ResponseBody::prettyPeek)
            .then()
            .assertThat()
            .statusCode(Status.OK.getStatusCode())
            .extract().as(UntersuchungGroesseGewichtDataJsonSpec.class);
        // ASSERT
        Assertions.assertThat(result).isNotNull();
        Assertions.assertThat(result.getUntersuchungGroesseGewichtJson()).isNull();
        Assertions.assertThat(result.getPreviousDataPoints()).isEmpty();
    }

    @Test
    @TestAsAdminBS
    void getUntersuchungGroesseGewicht_existing_should_return_existing() {
        // ARRANGE
        Untersuchung untersuchung = fixtureService.createUntersuchungWithGroesseGewicht();
        Assertions.assertThat(untersuchung.getUntersuchungGroesseGewicht()).isNotNull();

        UntersuchungGroesseGewicht existingGroesseGewicht =
            untersuchungGroesseGewichtService.getById(untersuchung.getUntersuchungGroesseGewicht().getId());
        Assertions.assertThat(existingGroesseGewicht).isNotNull();
        Assertions.assertThat(existingGroesseGewicht.getBemerkungen()).isNotNull();
        // ACT
        UntersuchungGroesseGewichtDataJsonSpec result = api
            .untersuchungGroesseGewichtResourceGetUntersuchungGroesseGewicht()
            .anlassIdPath(untersuchung.getAnlass().getId().getId())
            .einladungIdPath(toHasUntersuchungIdWrapper(untersuchung.getEinladungRequired().getId()))
            .execute(ResponseBody::prettyPeek)
            .then()
            .assertThat()
            .statusCode(Status.OK.getStatusCode())
            .extract().as(UntersuchungGroesseGewichtDataJsonSpec.class);
        // ASSERT
        Assertions.assertThat(result).isNotNull();
        Assertions.assertThat(result.getUntersuchungGroesseGewichtJson()).isNotNull();
        Assertions.assertThat(result.getUntersuchungGroesseGewichtJson().getId())
            .isEqualTo(existingGroesseGewicht.getId().getId());
        Assertions.assertThat(result.getUntersuchungGroesseGewichtJson().getData().getBemerkungen())
            .isNotNull()
            .isEqualTo(existingGroesseGewicht.getBemerkungen());
    }

    @Test
    @TestAsAdminBS
    void saveUntersuchungGroesseGewicht() {
        // ARRANGE
        Einladung einladung = fixtureService.createEinladung();
        Untersuchung untersuchung = fixtureService.createUntersuchungWithGroesseGewicht();
        Assertions.assertThat(untersuchung.getUntersuchungGroesseGewicht()).isNotNull();
        UntersuchungGroesseGewicht existingGroesseGewicht =
            untersuchungGroesseGewichtService.getById(untersuchung.getUntersuchungGroesseGewicht().getId());
        Assertions.assertThat(existingGroesseGewicht).isNotNull();
        Assertions.assertThat(existingGroesseGewicht.getBemerkungen()).isNotNull()
            .isEqualTo("Untersuchung GroesseGewicht Bemerkung");

        // ACT
        UntersuchungGroesseGewichtSaveJsonSpec saveJsonSpec =
            UntersuchungGroesseGewichtBuilder.createJaxSpec(builder -> builder
                .forUntersuchungGroesseGewicht(existingGroesseGewicht)
                .withGroesse(175)
                .withGewicht(BigDecimal.valueOf(68.8))
                .withBemerkung("Test updateUntersuchungGroesseGewicht")).getData();

        UntersuchungGroesseGewichtDataJsonSpec result = api
            .untersuchungGroesseGewichtResourceSaveUntersuchungGroesseGewicht()
            .anlassIdPath(einladung.getAnlass().getId().getId())
            .einladungIdPath(toHasUntersuchungIdWrapper(einladung.getId()))
            .body(saveJsonSpec)
            .execute(ResponseBody::prettyPeek)
            .then()
            .assertThat()
            .statusCode(Status.OK.getStatusCode())
            .extract().as(UntersuchungGroesseGewichtDataJsonSpec.class);

        // ASSERT
        Assertions.assertThat(result).isNotNull();
        Assertions.assertThat(result.getUntersuchungGroesseGewichtJson()).isNotNull();
        Assertions.assertThat(result.getUntersuchungGroesseGewichtJson().getId()).isNotNull();
        Assertions.assertThat(result.getUntersuchungGroesseGewichtJson().getData().getBemerkungen())
            .isNotNull()
            .isEqualTo("Test updateUntersuchungGroesseGewicht");
        Assertions.assertThat(result.getUntersuchungGroesseGewichtJson().getData().getGroesse()).isEqualTo(175);
        Assertions.assertThat(result.getUntersuchungGroesseGewichtJson().getData().getGewicht())
            .isEqualTo(BigDecimal.valueOf(68.8));
    }

    @Test
    @TestAsSchularztBE
    void saveUntersuchungGroesseGewicht_anonym() {
        // ARRANGE
        tenantContext.setCurrentTenant(TenantEnum.BE);

        Benutzer schularzt = fixtureService.readBenutzerWithBerechtigungen(TestAsSchularztBE.BENUTZER_ID_ID);
        AnonymerEintrag anonymerEintrag = fixtureService.createAnonymerEintragForArzt(schularzt);
        Untersuchung untersuchung = untersuchungService.getOrCreateUntersuchungAnonym(anonymerEintrag.getId(), false);

        UntersuchungGroesseGewicht untersuchungGroesseGewicht =
            UntersuchungGroesseGewicht.forUntersuchung(untersuchung);
        UntersuchungGroesseGewicht existingGroesseGewicht =
            untersuchungGroesseGewichtService.persist(untersuchungGroesseGewicht);
        Assertions.assertThat(existingGroesseGewicht).isNotNull();

        // ACT
        UntersuchungGroesseGewichtSaveJsonSpec saveJsonSpec =
            UntersuchungGroesseGewichtBuilder.createJaxSpec(builder -> builder
                .forUntersuchungGroesseGewicht(existingGroesseGewicht)
                .withGroesse(175)
                .withGewicht(BigDecimal.valueOf(68.8))
                .withBemerkung("Test saveUntersuchungGroesseGewicht anonym")).getData();

        UntersuchungGroesseGewichtDataJsonSpec result = api
            .untersuchungGroesseGewichtResourceSaveUntersuchungGroesseGewicht()
            .anlassIdPath(anonymerEintrag.getAnlass().getId().getId())
            .einladungIdPath(toHasAnonymerEintragIdWrapper(anonymerEintrag.getId()))
            .body(saveJsonSpec)
            .execute(ResponseBody::prettyPeek)
            .then()
            .assertThat()
            .statusCode(Status.OK.getStatusCode())
            .extract().as(UntersuchungGroesseGewichtDataJsonSpec.class);

        // ASSERT
        Assertions.assertThat(result).isNotNull();
        Assertions.assertThat(result.getUntersuchungGroesseGewichtJson()).isNotNull();
        Assertions.assertThat(result.getUntersuchungGroesseGewichtJson().getId()).isNotNull();
        Assertions.assertThat(result.getUntersuchungGroesseGewichtJson().getData().getBemerkungen())
            .isNotNull()
            .isEqualTo("Test saveUntersuchungGroesseGewicht anonym");
        Assertions.assertThat(result.getUntersuchungGroesseGewichtJson().getData().getGroesse()).isEqualTo(175);
        Assertions.assertThat(result.getUntersuchungGroesseGewichtJson().getData().getGewicht())
            .isEqualTo(BigDecimal.valueOf(68.8));
        // Bei AnonymerEintrag haben wir weder Alter noch previousDataPoints
        Assertions.assertThat(result.getAlter()).isNull();
        Assertions.assertThat(result.getAlterDuodezimal()).isNull();
        Assertions.assertThat(result.getPreviousDataPoints()).isEmpty();
    }
}
