package ch.dvbern.oss.hemed.esc.api.features.anlass.laeusekontrolle;

import java.util.List;

import ch.dvbern.oss.hemed.esc.api.features.anlass.einladung.Einladung;
import ch.dvbern.oss.hemed.esc.api.features.anlass.laeusekontrolle.types.LaeusekontrolleBeurteilung;
import ch.dvbern.oss.hemed.esc.api.features.anlass.untersuchung.Untersuchung;
import ch.dvbern.oss.hemed.esc.api.features.anlass.untersuchung.UntersuchungService;
import ch.dvbern.oss.hemed.esc.api.testutil.AbstractITest;
import ch.dvbern.oss.hemed.esc.api.testutil.FixtureService;
import ch.dvbern.oss.hemed.esc.api.testutil.builder.UntersuchungLaeusekontrolleBuilder;
import ch.dvbern.oss.hemed.esc.api.testutil.restassured.RequestSpecUtil;
import ch.dvbern.oss.hemed.esc.api.testutil.security.TestAsAdminBS;
import ch.dvbern.oss.hemed.esc.generated.restassured.api.UntersuchungLaeusekontrolleApiSpec;
import ch.dvbern.oss.hemed.esc.generated.restassured.dto.LaeusekontrolleBeurteilungSpec;
import ch.dvbern.oss.hemed.esc.generated.restassured.dto.UntersuchungLaeusekontrolleJsonSpec;
import ch.dvbern.oss.hemed.esc.generated.restassured.dto.UntersuchungLaeusekontrolleSaveJsonSpec;
import io.quarkus.test.junit.QuarkusTest;
import io.restassured.response.ResponseBody;
import jakarta.ws.rs.core.Response.Status;
import lombok.RequiredArgsConstructor;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static ch.dvbern.oss.hemed.esc.api.testutil.restassured.RequestSpecUtil.toHasUntersuchungIdWrapper;
import static org.hamcrest.Matchers.emptyOrNullString;

@QuarkusTest
@RequiredArgsConstructor
class UntersuchungLaeusekontrolleResourceITest extends AbstractITest {

    private final UntersuchungLaeusekontrolleApiSpec api =
        UntersuchungLaeusekontrolleApiSpec.untersuchungLaeusekontrolle(RequestSpecUtil.quarkusSpec());

    private final UntersuchungService untersuchungService;
    private final UntersuchungLaeusekontrolleService untersuchungLaeusekontrolleService;
    private final FixtureService fixtureService;

    @Override
    @BeforeEach
    public void setUp() {
        super.setUp();
    }

    @Test
    @TestAsAdminBS
    void getUntersuchungLaeusekontrolle_non_existing_should_return_empty_response() {
        // ARRANGE
        Einladung einladung = fixtureService.createEinladung();
        untersuchungService.getOrCreateUntersuchung(einladung.getId());
        // ACT
        api
            .untersuchungLaeusekontrolleResourceGetUntersuchungLaeusekontrolle()
            .anlassIdPath(einladung.getAnlass().getId().getId())
            .einladungIdPath(toHasUntersuchungIdWrapper(einladung.getId()))
            .execute(ResponseBody::prettyPeek)
            .then()
            .assertThat()
            .statusCode(Status.NO_CONTENT.getStatusCode())
            .body(emptyOrNullString());
    }


    @Test
    @TestAsAdminBS
    void getUntersuchungLaeusekontrolle_existing_should_return_existing() {
        // ARRANGE
        Untersuchung untersuchung = fixtureService.createUntersuchungWithLaeusekontrolle();
        Assertions.assertThat(untersuchung.getUntersuchungLaeusekontrolle()).isNotNull();
        UntersuchungLaeusekontrolle existingLaeusekontrolle =
            untersuchungLaeusekontrolleService.getById(untersuchung.getUntersuchungLaeusekontrolle().getId());
        Assertions.assertThat(existingLaeusekontrolle).isNotNull();
        Assertions.assertThat(existingLaeusekontrolle.getBeurteilung()).isNotNull().isNotEmpty();
        // ACT
        UntersuchungLaeusekontrolleJsonSpec result = api
            .untersuchungLaeusekontrolleResourceGetUntersuchungLaeusekontrolle()
            .anlassIdPath(untersuchung.getAnlass().getId().getId())
            .einladungIdPath(toHasUntersuchungIdWrapper(untersuchung.getEinladungRequired().getId()))
            .execute(ResponseBody::prettyPeek)
            .then()
            .assertThat()
            .statusCode(Status.OK.getStatusCode())
            .extract().as(UntersuchungLaeusekontrolleJsonSpec.class);
        // ASSERT
        Assertions.assertThat(result).isNotNull();
        Assertions.assertThat(result.getId()).isEqualTo(existingLaeusekontrolle.getId().getId());
        Assertions.assertThat(result.getData().getBemerkungen())
            .isNotNull()
            .isEqualTo(existingLaeusekontrolle.getBemerkungen());
    }


    @Test
    @TestAsAdminBS
    void saveUntersuchungLaeusekontrolle() {
        // ARRANGE
        Einladung einladung = fixtureService.createEinladung();
        Untersuchung untersuchung = fixtureService.createUntersuchungWithLaeusekontrolle();
        Assertions.assertThat(untersuchung.getUntersuchungLaeusekontrolle()).isNotNull();
        UntersuchungLaeusekontrolle existingLaeusekontrolle =
            untersuchungLaeusekontrolleService.getById(untersuchung.getUntersuchungLaeusekontrolle().getId());
        Assertions.assertThat(existingLaeusekontrolle).isNotNull();
        Assertions.assertThat(existingLaeusekontrolle.getBemerkungen())
            .isNotNull()
            .isEqualTo("Läuse Bemerkungen SU");
        Assertions.assertThat(existingLaeusekontrolle.getBeurteilung())
            .isNotEmpty()
            .contains(LaeusekontrolleBeurteilung.LAEUSE_VORHANDEN);

        // ACT
        UntersuchungLaeusekontrolleSaveJsonSpec saveJsonSpec =
            UntersuchungLaeusekontrolleBuilder.createJaxSpec(builder -> builder
                .forUntersuchungLaeusekontrolle(existingLaeusekontrolle)
                .withBemerkung("Test updateUntersuchungLaeusekontrolle")
                .withBeurteilung(List.of(LaeusekontrolleBeurteilung.NISSEN_VORHANDEN))).getData();
        UntersuchungLaeusekontrolleJsonSpec result = api
            .untersuchungLaeusekontrolleResourceSaveUntersuchungLaeusekontrolle()
            .anlassIdPath(einladung.getAnlass().getId().getId())
            .einladungIdPath(toHasUntersuchungIdWrapper(einladung.getId()))
            .body(saveJsonSpec)
            .execute(ResponseBody::prettyPeek)
            .then()
            .assertThat()
            .statusCode(Status.OK.getStatusCode())
            .extract().as(UntersuchungLaeusekontrolleJsonSpec.class);

        // ASSERT
        Assertions.assertThat(result).isNotNull();
        Assertions.assertThat(result.getId()).isNotNull();
        Assertions.assertThat(result.getData().getBemerkungen())
            .isNotNull()
            .isEqualTo("Test updateUntersuchungLaeusekontrolle");
        Assertions.assertThat(result.getData().getBeurteilung())
            .isNotEmpty()
            .contains(LaeusekontrolleBeurteilungSpec.NISSEN_VORHANDEN)
            .doesNotContain(LaeusekontrolleBeurteilungSpec.LAEUSE_VORHANDEN);
    }
}

