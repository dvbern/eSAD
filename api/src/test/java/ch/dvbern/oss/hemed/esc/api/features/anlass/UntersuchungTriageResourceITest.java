package ch.dvbern.oss.hemed.esc.api.features.anlass;

import ch.dvbern.oss.hemed.esc.api.features.anlass.types.NaechsteStation;
import ch.dvbern.oss.hemed.esc.api.testutil.AbstractITest;
import ch.dvbern.oss.hemed.esc.api.testutil.FixtureService;
import ch.dvbern.oss.hemed.esc.api.testutil.builder.UntersuchungTriageBuilder;
import ch.dvbern.oss.hemed.esc.api.testutil.restassured.RequestSpecUtil;
import ch.dvbern.oss.hemed.esc.api.testutil.security.TestAsAdminBS;
import ch.dvbern.oss.hemed.esc.generated.restassured.api.UntersuchungTriageApiSpec;
import ch.dvbern.oss.hemed.esc.generated.restassured.dto.NaechsteStationSpec;
import ch.dvbern.oss.hemed.esc.generated.restassured.dto.UntersuchungTriageJsonSpec;
import ch.dvbern.oss.hemed.esc.generated.restassured.dto.UntersuchungTriageSaveJsonSpec;
import io.quarkus.test.junit.QuarkusTest;
import io.restassured.response.ResponseBody;
import jakarta.ws.rs.core.Response.Status;
import lombok.RequiredArgsConstructor;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.hamcrest.Matchers.emptyOrNullString;

@QuarkusTest
@RequiredArgsConstructor
class UntersuchungTriageResourceITest extends AbstractITest {

    private final UntersuchungTriageApiSpec api =
        UntersuchungTriageApiSpec.untersuchungTriage(RequestSpecUtil.quarkusSpec());

    private final EinladungService einladungService;
    private final UntersuchungService untersuchungService;
    private final UntersuchungTriageService untersuchungTriageService;
    private final FixtureService fixtureService;

    @Override
    @BeforeEach
    public void setUp() {
        super.setUp();
    }

    @Test
    @TestAsAdminBS
    void getUntersuchungTriage_non_existing_should_return_empty_response() {
        // ARRANGE
        Einladung einladung = fixtureService.createEinladung();
        untersuchungService.getOrCreateUntersuchung(einladung.getId());
        // ACT
        api
            .untersuchungTriageResourceGetUntersuchungTriage()
            .anlassIdPath(einladung.getAnlass().getId().getId())
            .einladungIdPath(einladung.getId().getId())
            .execute(ResponseBody::prettyPeek)
            .then()
            .assertThat()
            .statusCode(Status.NO_CONTENT.getStatusCode())
            .body(emptyOrNullString());
    }

    @Test
    @TestAsAdminBS
    void getUntersuchungTriage_existing_should_return_existing() {
        // ARRANGE
        Untersuchung untersuchung = fixtureService.createUntersuchungWithTriage();
        Assertions.assertThat(untersuchung.getUntersuchungTriage()).isNotNull();
        UntersuchungTriage existingTriage =
            untersuchungTriageService.getById(untersuchung.getUntersuchungTriage().getId());
        Assertions.assertThat(existingTriage).isNotNull();
        Assertions.assertThat(existingTriage.getTriageSuBemerkungen()).isNotNull();
        // ACT
        UntersuchungTriageJsonSpec result = api
            .untersuchungTriageResourceGetUntersuchungTriage()
            .anlassIdPath(untersuchung.getEinladung().getAnlass().getId().getId())
            .einladungIdPath(untersuchung.getEinladung().getId().getId())
            .execute(ResponseBody::prettyPeek)
            .then()
            .assertThat()
            .statusCode(Status.OK.getStatusCode())
            .extract().as(UntersuchungTriageJsonSpec.class);
        // ASSERT
        Assertions.assertThat(result).isNotNull();
        Assertions.assertThat(result.getId()).isEqualTo(existingTriage.getId().getId());
        Assertions.assertThat(result.getData().getTriageSuBemerkungen())
            .isNotNull()
            .isEqualTo(existingTriage.getTriageSuBemerkungen());
    }

    @Test
    @TestAsAdminBS
    void saveUntersuchungTriage() {
        // ARRANGE
        Einladung einladung = fixtureService.createEinladung();
        Untersuchung untersuchung = fixtureService.createUntersuchungWithTriage();
        Assertions.assertThat(untersuchung.getUntersuchungTriage()).isNotNull();
        UntersuchungTriage existingTriage =
            untersuchungTriageService.getById(untersuchung.getUntersuchungTriage().getId());
        Assertions.assertThat(existingTriage).isNotNull();
        Assertions.assertThat(existingTriage.getTriageSuBemerkungen())
            .isNotNull()
            .isEqualTo("Triage Bemerkungen SU");

        // ACT
        UntersuchungTriageSaveJsonSpec saveJsonSpec =
            UntersuchungTriageBuilder.createJaxSpec(builder -> builder
                .forUntersuchungTriage(existingTriage)
                .withTriageSu(true, "Test updateUntersuchungTriage")).getData();
        UntersuchungTriageJsonSpec result = api
            .untersuchungTriageResourceSaveUntersuchungTriage()
            .anlassIdPath(einladung.getAnlass().getId().getId())
            .einladungIdPath(einladung.getId().getId())
            .body(saveJsonSpec)
            .execute(ResponseBody::prettyPeek)
            .then()
            .assertThat()
            .statusCode(Status.OK.getStatusCode())
            .extract().as(UntersuchungTriageJsonSpec.class);

        // ASSERT
        Assertions.assertThat(result).isNotNull();
        Assertions.assertThat(result.getId()).isNotNull();
        Assertions.assertThat(result.getData().getTriageSuBemerkungen())
            .isNotNull()
            .isEqualTo("Test updateUntersuchungTriage");
        Assertions.assertThat(result.getData().getTriageSu())
            .isTrue();
    }

    @Test
    @TestAsAdminBS
    void saveUntersuchungTriage_shouldUpdate_NaechsteStationOnEinladung() {

        // ARRANGE
        Einladung einladung = fixtureService.createEinladung();
        Untersuchung untersuchung = fixtureService.createUntersuchungWithTriage();
        Assertions.assertThat(untersuchung.getUntersuchungTriage()).isNotNull();
        UntersuchungTriage existingTriage =
            untersuchungTriageService.getById(untersuchung.getUntersuchungTriage().getId());
        Assertions.assertThat(existingTriage).isNotNull();
        Assertions.assertThat(existingTriage.getTriageSuBemerkungen())
            .isNotNull()
            .isEqualTo("Triage Bemerkungen SU");

        // ACT
        UntersuchungTriageSaveJsonSpec saveJsonSpec = UntersuchungTriageBuilder.createJaxSpec(
            builder -> builder
                .forUntersuchungTriage(existingTriage)
                .withTriageSu(true, "Test updateUntersuchungTriage")).getData();
        saveJsonSpec.setNaechsteStation(NaechsteStationSpec.MPA);
        UntersuchungTriageJsonSpec result = api
            .untersuchungTriageResourceSaveUntersuchungTriage()
            .anlassIdPath(einladung.getAnlass().getId().getId())
            .einladungIdPath(einladung.getId().getId())
            .body(saveJsonSpec)
            .execute(ResponseBody::prettyPeek)
            .then()
            .assertThat()
            .statusCode(Status.OK.getStatusCode())
            .extract().as(UntersuchungTriageJsonSpec.class);

        // ASSERT
        Einladung updatedEinladung = einladungService.getById(einladung.getId());
        Assertions.assertThat(result).isNotNull();
        Assertions.assertThat(result.getId()).isNotNull();
        Assertions.assertThat(result.getData().getTriageSuBemerkungen())
            .isNotNull()
            .isEqualTo("Test updateUntersuchungTriage");
        Assertions.assertThat(result.getData().getTriageSu())
            .isTrue();
        Assertions.assertThat(updatedEinladung.getNaechsteStation()).isNotNull().isEqualTo(NaechsteStation.MPA);
        // We can't use ...getNaechsteStationSeit()).isEqualTo(expectedTime); cause we don't have the exact time
        Assertions.assertThat(updatedEinladung.getNaechsteStationSeit())
            .isAfter(getAppClock().localDateTime().now().minusSeconds(15))
            .isBefore(getAppClock().localDateTime().now().plusMinutes(15));
    }
}
