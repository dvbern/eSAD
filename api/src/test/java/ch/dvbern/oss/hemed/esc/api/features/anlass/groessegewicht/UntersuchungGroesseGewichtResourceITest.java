package ch.dvbern.oss.hemed.esc.api.features.anlass.groessegewicht;

import java.math.BigDecimal;

import ch.dvbern.oss.hemed.esc.api.features.anlass.einladung.Einladung;
import ch.dvbern.oss.hemed.esc.api.features.anlass.untersuchung.Untersuchung;
import ch.dvbern.oss.hemed.esc.api.features.anlass.untersuchung.UntersuchungService;
import ch.dvbern.oss.hemed.esc.api.testutil.AbstractITest;
import ch.dvbern.oss.hemed.esc.api.testutil.FixtureService;
import ch.dvbern.oss.hemed.esc.api.testutil.builder.UntersuchungGroesseGewichtBuilder;
import ch.dvbern.oss.hemed.esc.api.testutil.restassured.RequestSpecUtil;
import ch.dvbern.oss.hemed.esc.api.testutil.security.TestAsAdminBS;
import ch.dvbern.oss.hemed.esc.generated.restassured.api.UntersuchungGroesseGewichtApiSpec;
import ch.dvbern.oss.hemed.esc.generated.restassured.dto.UntersuchungGroesseGewichtDataJsonSpec;
import ch.dvbern.oss.hemed.esc.generated.restassured.dto.UntersuchungGroesseGewichtSaveJsonSpec;
import io.quarkus.test.junit.QuarkusTest;
import io.restassured.response.ResponseBody;
import jakarta.ws.rs.core.Response.Status;
import lombok.RequiredArgsConstructor;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;

import static ch.dvbern.oss.hemed.esc.api.testutil.restassured.RequestSpecUtil.toHasUntersuchungIdWrapper;

@QuarkusTest
@RequiredArgsConstructor
class UntersuchungGroesseGewichtResourceITest extends AbstractITest {

    private final UntersuchungGroesseGewichtApiSpec api =
        UntersuchungGroesseGewichtApiSpec.untersuchungGroesseGewicht(RequestSpecUtil.quarkusSpec());

    private final UntersuchungService untersuchungService;
    private final UntersuchungGroesseGewichtService untersuchungGroesseGewichtService;
    private final FixtureService fixtureService;

    @Test
    @TestAsAdminBS
    void getUntersuchungGroesseGewicht_non_existing_should_not_create_data() {
        // ARRANGE
        Einladung einladung = fixtureService.createEinladung();
        untersuchungService.getOrCreateUntersuchung(einladung.getId());
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
}
