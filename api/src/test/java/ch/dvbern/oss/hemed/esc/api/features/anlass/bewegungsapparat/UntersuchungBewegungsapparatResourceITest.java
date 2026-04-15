package ch.dvbern.oss.hemed.esc.api.features.anlass.bewegungsapparat;

import ch.dvbern.oss.hemed.esc.api.features.anlass.bewegungsapparat.types.Skoliose;
import ch.dvbern.oss.hemed.esc.api.features.anlass.einladung.Einladung;
import ch.dvbern.oss.hemed.esc.api.features.anlass.untersuchung.Untersuchung;
import ch.dvbern.oss.hemed.esc.api.features.anlass.untersuchung.UntersuchungService;
import ch.dvbern.oss.hemed.esc.api.testutil.AbstractITest;
import ch.dvbern.oss.hemed.esc.api.testutil.FixtureService;
import ch.dvbern.oss.hemed.esc.api.testutil.builder.UntersuchungBewegungsapparatBuilder;
import ch.dvbern.oss.hemed.esc.api.testutil.restassured.RequestSpecUtil;
import ch.dvbern.oss.hemed.esc.api.testutil.security.TestAsAdminBS;
import ch.dvbern.oss.hemed.esc.generated.restassured.api.UntersuchungBewegungsapparatApiSpec;
import ch.dvbern.oss.hemed.esc.generated.restassured.dto.SkolioseSpec;
import ch.dvbern.oss.hemed.esc.generated.restassured.dto.UntersuchungBewegungsapparatJsonSpec;
import ch.dvbern.oss.hemed.esc.generated.restassured.dto.UntersuchungBewegungsapparatSaveJsonSpec;
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
class UntersuchungBewegungsapparatResourceITest extends AbstractITest {

    private final UntersuchungBewegungsapparatApiSpec api =
        UntersuchungBewegungsapparatApiSpec.untersuchungBewegungsapparat(RequestSpecUtil.quarkusSpec());

    private final UntersuchungService untersuchungService;
    private final UntersuchungBewegungsapparatService untersuchungBewegungsapparatService;
    private final FixtureService fixtureService;

    @Override
    @BeforeEach
    public void setUp() {
        super.setUp();
    }

    @Test
    @TestAsAdminBS
    void getUntersuchungBewegungsapparat_non_existing_should_return_empty_response() {
        // ARRANGE
        Einladung einladung = fixtureService.createEinladung();
        untersuchungService.getOrCreateUntersuchung(einladung.getId());
        // ACT
        api
            .untersuchungBewegungsapparatResourceGetUntersuchungBewegungsapparat()
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
    void getUntersuchungBewegungsapparat_existing_should_return_existing() {
        // ARRANGE
        Untersuchung untersuchung = fixtureService.createUntersuchungWithBewegungsapparat();
        Assertions.assertThat(untersuchung.getUntersuchungBewegungsapparat()).isNotNull();

        UntersuchungBewegungsapparat existingBewegungsapparat =
            untersuchungBewegungsapparatService.getById(untersuchung.getUntersuchungBewegungsapparat().getId());

        Assertions.assertThat(existingBewegungsapparat).isNotNull();
        Assertions.assertThat(existingBewegungsapparat.getSkoliose()).isNotNull();
        Assertions.assertThat(existingBewegungsapparat.getHaltungsschwaeche()).isNotNull();

        // ACT
        UntersuchungBewegungsapparatJsonSpec result = api
            .untersuchungBewegungsapparatResourceGetUntersuchungBewegungsapparat()
            .anlassIdPath(untersuchung.getEinladung().getAnlass().getId().getId())
            .einladungIdPath(untersuchung.getEinladung().getId().getId())
            .execute(ResponseBody::prettyPeek)
            .then()
            .assertThat()
            .statusCode(Status.OK.getStatusCode())
            .extract().as(UntersuchungBewegungsapparatJsonSpec.class);

        // ASSERT
        Assertions.assertThat(result).isNotNull();
        Assertions.assertThat(result.getId()).isEqualTo(existingBewegungsapparat.getId().getId());
        Assertions.assertThat(result.getData().getBemerkungen())
            .isNotNull()
            .isEqualTo(existingBewegungsapparat.getBemerkungen());
    }

    @Test
    @TestAsAdminBS
    void saveUntersuchungBewegungsapparat() {
        // ARRANGE
        Einladung einladung = fixtureService.createEinladung();
        Untersuchung untersuchung = fixtureService.createUntersuchungWithBewegungsapparat();
        Assertions.assertThat(untersuchung.getUntersuchungBewegungsapparat()).isNotNull();

        UntersuchungBewegungsapparat existingBewegungsapparat =
            untersuchungBewegungsapparatService.getById(untersuchung.getUntersuchungBewegungsapparat().getId());

        Assertions.assertThat(existingBewegungsapparat).isNotNull();
        Assertions.assertThat(existingBewegungsapparat.getBemerkungen())
            .isNotNull()
            .isEqualTo("Bewegungsapparat Bemerkungen SU");
        Assertions.assertThat(existingBewegungsapparat.getSkoliose().getEnumValue(Skoliose.class)).isEqualTo(Skoliose.SKOLIOSE_JA);

        // ACT
        UntersuchungBewegungsapparatSaveJsonSpec saveJsonSpec =
            UntersuchungBewegungsapparatBuilder.createJaxSpec(builder -> builder
                .forUntersuchungBewegungsapparat(existingBewegungsapparat)
                .withBemerkung("Test updateUntersuchungBewegungsapparat")
                .withSkoliose(Skoliose.SKOLIOSE_NEIN, null)).getData();
        UntersuchungBewegungsapparatJsonSpec result = api
            .untersuchungBewegungsapparatResourceSaveUntersuchungBewegungsapparat()
            .anlassIdPath(einladung.getAnlass().getId().getId())
            .einladungIdPath(einladung.getId().getId())
            .body(saveJsonSpec)
            .execute(ResponseBody::prettyPeek)
            .then()
            .assertThat()
            .statusCode(Status.OK.getStatusCode())
            .extract().as(UntersuchungBewegungsapparatJsonSpec.class);

        // ASSERT
        Assertions.assertThat(result).isNotNull();
        Assertions.assertThat(result.getId()).isNotNull();
        Assertions.assertThat(result.getData().getBemerkungen())
            .isNotNull()
            .isEqualTo("Test updateUntersuchungBewegungsapparat");
        Assertions.assertThat(result.getData().getSkoliose().getValue()).contains(SkolioseSpec.SKOLIOSE_NEIN.name());
    }
}
