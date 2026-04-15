package ch.dvbern.oss.hemed.esc.api.features.anlass.signatur;

import ch.dvbern.oss.hemed.esc.api.features.anlass.einladung.Einladung;
import ch.dvbern.oss.hemed.esc.api.features.anlass.untersuchung.Untersuchung;
import ch.dvbern.oss.hemed.esc.api.testutil.AbstractITest;
import ch.dvbern.oss.hemed.esc.api.testutil.FixtureService;
import ch.dvbern.oss.hemed.esc.api.testutil.restassured.RequestSpecUtil;
import ch.dvbern.oss.hemed.esc.api.testutil.security.TestAsAdminBS;
import ch.dvbern.oss.hemed.esc.generated.restassured.api.UntersuchungSignaturApiSpec;
import ch.dvbern.oss.hemed.esc.generated.restassured.dto.BenutzerOptionJsonSpec;
import ch.dvbern.oss.hemed.esc.generated.restassured.dto.UntersuchungSignaturJsonSpec;
import io.quarkus.test.junit.QuarkusTest;
import io.restassured.response.ResponseBody;
import jakarta.ws.rs.core.Response.Status;
import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@QuarkusTest
@RequiredArgsConstructor
class UntersuchungSignaturResourceITest extends AbstractITest {

    private final UntersuchungSignaturApiSpec api = UntersuchungSignaturApiSpec.untersuchungSignatur(RequestSpecUtil.quarkusSpec());

    private final UntersuchungSignaturService untersuchungSignaturService;
    private final FixtureService fixtureService;

    @Override
    @BeforeEach
    public void setUp() {
        super.setUp();
    }

    @Test
    @TestAsAdminBS
    void getUntersuchungSignatur_non_existing_should_not_create_data() {
        // ARRANGE
        Einladung einladung = fixtureService.createEinladung();
        // ACT / ASSERT
        api.untersuchungSignaturResourceGetUntersuchungSignatur()
            .anlassIdPath(einladung.getAnlass().getId().getId())
            .einladungIdPath(einladung.getId().getId())
            .execute(ResponseBody::prettyPeek)
            .then()
            .assertThat()
            .statusCode(Status.NO_CONTENT.getStatusCode());
    }

    @Test
    @TestAsAdminBS
    void getUntersuchungSignatur_existing_should_return_existing() {
        // ARRANGE
        Untersuchung untersuchung = fixtureService.createUntersuchungWithSignatur();
        assertThat(untersuchung.getUntersuchungSignatur()).isNotNull();

        UntersuchungSignatur existingSignatur = untersuchungSignaturService.getById(untersuchung.getUntersuchungSignatur().getId());
        assertThat(existingSignatur).isNotNull();
        assertThat(existingSignatur.getPerson()).isNotNull();
        assertThat(existingSignatur.getTimestampVisiert()).isNotNull();
        // ACT
        UntersuchungSignaturJsonSpec result = api.untersuchungSignaturResourceGetUntersuchungSignatur()
            .anlassIdPath(untersuchung.getEinladung().getAnlass().getId().getId())
            .einladungIdPath(untersuchung.getEinladung().getId().getId())
            .execute(ResponseBody::prettyPeek)
            .then()
            .assertThat()
            .statusCode(Status.OK.getStatusCode())
            .extract()
            .as(UntersuchungSignaturJsonSpec.class);

        // ASSERT
        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(existingSignatur.getId().getId());
        assertThat(result.getData().getPerson())
            .isNotNull()
            .extracting(BenutzerOptionJsonSpec::getId)
            .isEqualTo(TestAsAdminBS.BENUTZER_ID);
        assertThat(result.getData().getTimestampVisiert()).isNotNull();
    }

    @Test
    @TestAsAdminBS
    void visieren() {
        // ARRANGE
        Untersuchung untersuchung = fixtureService.createUntersuchung();
        assertThat(untersuchung.getUntersuchungSignatur()).isNull();
        // ACT
        UntersuchungSignaturJsonSpec result = api.untersuchungSignaturResourceSignieren()
            .anlassIdPath(untersuchung.getEinladung().getAnlass().getId().getId())
            .einladungIdPath(untersuchung.getEinladung().getId().getId())
            .execute(ResponseBody::prettyPeek)
            .then()
            .assertThat()
            .statusCode(Status.OK.getStatusCode())
            .extract()
            .as(UntersuchungSignaturJsonSpec.class);
        // ASSERT
        assertThat(result).isNotNull();
        assertThat(result.getData().getPerson())
            .isNotNull()
            .extracting(BenutzerOptionJsonSpec::getId)
            .isEqualTo(TestAsAdminBS.BENUTZER_ID);
        assertThat(result.getData().getTimestampVisiert()).isNotNull();
    }

    @Test
    @TestAsAdminBS
    void resetSignatur() {
        // ARRANGE
        Untersuchung untersuchung = fixtureService.createUntersuchungWithSignatur();
        UntersuchungSignatur existingSignatur = untersuchung.getUntersuchungSignatur();
        assertThat(existingSignatur).isNotNull();
        assertThat(existingSignatur.getPerson()).isNotNull();
        assertThat(existingSignatur.getTimestampVisiert()).isNotNull();
        // ACT
        UntersuchungSignaturJsonSpec result = api.untersuchungSignaturResourceResetSignatur()
            .anlassIdPath(untersuchung.getEinladung().getAnlass().getId().getId())
            .einladungIdPath(untersuchung.getEinladung().getId().getId())
            .execute(ResponseBody::prettyPeek)
            .then()
            .assertThat()
            .statusCode(Status.OK.getStatusCode())
            .extract()
            .as(UntersuchungSignaturJsonSpec.class);
        // ASSERT
        assertThat(result).isNotNull();
        assertThat(result.getData().getPerson()).isNull();
        assertThat(result.getData().getTimestampVisiert()).isNull();
    }
}
