package ch.dvbern.oss.hemed.esc.api.features.anlass.schulrelevantebeeintraechtigung;

import ch.dvbern.oss.hemed.esc.api.features.anlass.einladung.Einladung;
import ch.dvbern.oss.hemed.esc.api.features.anlass.schulrelevantebeeintraechtigung.types.FreiesGehen;
import ch.dvbern.oss.hemed.esc.api.features.anlass.sprache.types.Nachsprechen;
import ch.dvbern.oss.hemed.esc.api.features.anlass.untersuchung.Untersuchung;
import ch.dvbern.oss.hemed.esc.api.features.anlass.untersuchung.UntersuchungService;
import ch.dvbern.oss.hemed.esc.api.testutil.AbstractITest;
import ch.dvbern.oss.hemed.esc.api.testutil.FixtureService;
import ch.dvbern.oss.hemed.esc.api.testutil.builder.UntersuchungSchulrelevanteBeeintraechtigungBuilder;
import ch.dvbern.oss.hemed.esc.api.testutil.restassured.RequestSpecUtil;
import ch.dvbern.oss.hemed.esc.api.testutil.security.TestAsAdminBS;
import ch.dvbern.oss.hemed.esc.generated.restassured.api.UntersuchungSchulrelevanteBeeintraechtigungApiSpec;
import ch.dvbern.oss.hemed.esc.generated.restassured.dto.FreiesGehenSpec;
import ch.dvbern.oss.hemed.esc.generated.restassured.dto.UntersuchungSchulrelevanteBeeintraechtigungJsonSpec;
import ch.dvbern.oss.hemed.esc.generated.restassured.dto.UntersuchungSchulrelevanteBeeintraechtigungSaveJsonSpec;
import io.quarkus.test.junit.QuarkusTest;
import io.restassured.response.ResponseBody;
import jakarta.ws.rs.core.Response.Status;
import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

import static ch.dvbern.oss.hemed.esc.api.testutil.restassured.RequestSpecUtil.toHasUntersuchungIdWrapper;

@QuarkusTest
@RequiredArgsConstructor
class UntersuchungSchulrelevanteBeeintraechtigungResourceITest extends AbstractITest {

    private final UntersuchungSchulrelevanteBeeintraechtigungApiSpec api =
        UntersuchungSchulrelevanteBeeintraechtigungApiSpec.untersuchungSchulrelevanteBeeintraechtigung(
            RequestSpecUtil.quarkusSpec());

    private final UntersuchungService untersuchungService;
    private final UntersuchungSchulrelevanteBeeintraechtigungService untersuchungBeeintraechtigungService;
    private final FixtureService fixtureService;

    @Override
    @BeforeEach
    public void setUp() {
        super.setUp();
    }

    @Test
    @TestAsAdminBS
    void getUntersuchungSchulrelevanteBeeintraechtigung_non_existing_should_not_create_data() {
        // ARRANGE
        Einladung einladung = fixtureService.createEinladung();
        untersuchungService.getOrCreateUntersuchung(einladung.getId());
        // ACT / ASSERT
        api.untersuchungSchulrelevanteBeeintraechtigungResourceGetUntersuchungSchulrelevanteBeeintraechtigung()
            .anlassIdPath(einladung.getAnlass().getId().getId())
            .einladungIdPath(toHasUntersuchungIdWrapper(einladung.getId()))
            .execute(ResponseBody::prettyPeek)
            .then()
            .assertThat()
            .statusCode(Status.NO_CONTENT.getStatusCode());
    }

    @Test
    @TestAsAdminBS
    void getUntersuchungSchulrelevanteBeeintraechtigung_existing_should_return_existing() {
        // ARRANGE
        Untersuchung untersuchung = fixtureService.createUntersuchungWithSchulrelevanteBeeintraechtigung();
        assertThat(untersuchung.getUntersuchungSchulrelevanteBeeintraechtigung()).isNotNull();

        UntersuchungSchulrelevanteBeeintraechtigung
            existingBeeintraechtigung =
            untersuchungBeeintraechtigungService.getById(untersuchung.getUntersuchungSchulrelevanteBeeintraechtigung()
                .getId());
        assertThat(existingBeeintraechtigung).isNotNull();
        assertThat(existingBeeintraechtigung.getFreiesGehen().getEnumValue(FreiesGehen.class)).isEqualTo(FreiesGehen.NORMAL);
        assertThat(existingBeeintraechtigung.getBemerkungen()).isNotNull();
        // ACT
        UntersuchungSchulrelevanteBeeintraechtigungJsonSpec result =
            api.untersuchungSchulrelevanteBeeintraechtigungResourceGetUntersuchungSchulrelevanteBeeintraechtigung()
                .anlassIdPath(untersuchung.getAnlass().getId().getId())
                .einladungIdPath(toHasUntersuchungIdWrapper(untersuchung.getEinladungRequired().getId()))
                .execute(ResponseBody::prettyPeek)
                .then()
                .assertThat()
                .statusCode(Status.OK.getStatusCode())
                .extract()
                .as(UntersuchungSchulrelevanteBeeintraechtigungJsonSpec.class);
        // ASSERT
        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(existingBeeintraechtigung.getId().getId());
        assertThat(existingBeeintraechtigung.getFreiesGehen()).isNotNull();
        assertThat(result.getData()
            .getFreiesGehen().getValue()).contains(existingBeeintraechtigung.getFreiesGehen().toEnumValueName(
            FreiesGehen.class));
        assertThat(result.getData().getBemerkungen()).isNotNull().isEqualTo(existingBeeintraechtigung.getBemerkungen());
    }

    @Test
    @TestAsAdminBS
    void saveUntersuchungSchulrelevanteBeeintraechtigung() {
        // ARRANGE
        Einladung einladung = fixtureService.createEinladung();
        Untersuchung untersuchung = fixtureService.createUntersuchungWithSchulrelevanteBeeintraechtigung();
        assertThat(untersuchung.getUntersuchungSchulrelevanteBeeintraechtigung()).isNotNull();
        UntersuchungSchulrelevanteBeeintraechtigung
            existingBeeintraechtigung =
            untersuchungBeeintraechtigungService.getById(untersuchung.getUntersuchungSchulrelevanteBeeintraechtigung()
                .getId());
        assertThat(existingBeeintraechtigung).isNotNull();
        assertThat(existingBeeintraechtigung.getFreiesGehen().getEnumValue(FreiesGehen.class)).isEqualTo(FreiesGehen.NORMAL);
        assertThat(existingBeeintraechtigung.getBemerkungen()).isNotNull()
            .isEqualTo("Untersuchung Schulrelevante Beeinträchtigung Bemerkung");

        // ACT
        UntersuchungSchulrelevanteBeeintraechtigungSaveJsonSpec saveJsonSpec =
            UntersuchungSchulrelevanteBeeintraechtigungBuilder.createJaxSpec(builder -> builder
                .forUntersuchungSchulrelevanteBeeintraechtigung(existingBeeintraechtigung)
                .withFreiesGehen(FreiesGehen.PATHOLOGISCH, null)
                .withBemerkungen("Test updateUntersuchungSchulrelevanteBeeintraechtigung")).getData();

        UntersuchungSchulrelevanteBeeintraechtigungJsonSpec result = api.untersuchungSchulrelevanteBeeintraechtigungResourceSaveUntersuchungSchulrelevanteBeeintraechtigung()
            .anlassIdPath(einladung.getAnlass().getId().getId())
            .einladungIdPath(toHasUntersuchungIdWrapper(einladung.getId()))
            .body(saveJsonSpec)
            .execute(ResponseBody::prettyPeek)
            .then()
            .assertThat()
            .statusCode(Status.OK.getStatusCode())
            .extract()
            .as(UntersuchungSchulrelevanteBeeintraechtigungJsonSpec.class);

        // ASSERT
        assertThat(result).isNotNull();
        assertThat(result.getId()).isNotNull();
        assertThat(result.getData().getBemerkungen()).isNotNull().isEqualTo("Test updateUntersuchungSchulrelevanteBeeintraechtigung");
        assertThat(result.getData().getFreiesGehen().getValue()).contains(FreiesGehenSpec.PATHOLOGISCH.name());
    }
}
