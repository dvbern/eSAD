package ch.dvbern.oss.hemed.esc.api.features.anlass;

import ch.dvbern.oss.hemed.esc.api.features.kinder.Kind;
import ch.dvbern.oss.hemed.esc.api.testutil.AbstractITest;
import ch.dvbern.oss.hemed.esc.api.testutil.FixtureService;
import ch.dvbern.oss.hemed.esc.api.testutil.restassured.RequestSpecUtil;
import ch.dvbern.oss.hemed.esc.api.testutil.security.TestAsAdminBS;
import ch.dvbern.oss.hemed.esc.generated.restassured.api.EinladungenApiSpec;
import ch.dvbern.oss.hemed.esc.generated.restassured.dto.UntersuchungKindDatenJsonSpec;
import io.quarkus.test.junit.QuarkusTest;
import io.restassured.response.ResponseBody;
import jakarta.ws.rs.core.Response.Status;
import lombok.RequiredArgsConstructor;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

@QuarkusTest
@RequiredArgsConstructor
class AnlassUntersuchungKindDatenResourceITest extends AbstractITest {

    private final EinladungenApiSpec api = EinladungenApiSpec.einladungen(RequestSpecUtil.quarkusSpec());

    private final FixtureService fixtureService;

    @Override
    @BeforeEach
    public void setUp() {
        super.setUp();
    }

    @Test
    @TestAsAdminBS
    void getUntersuchungKindDaten() {
        // ARRANGE
        Untersuchung untersuchung = fixtureService.createUntersuchungWithDurchfuehrung();
        Kind kind = untersuchung.getEinladung().getDossier().getKind();
        // ACT
        UntersuchungKindDatenJsonSpec result = api
            .einladungResourceGetUntersuchungKindDaten()
            .anlassIdPath(untersuchung.getEinladung().getAnlass().getId().getId())
            .einladungIdPath(untersuchung.getEinladung().getId().getId())
            .execute(ResponseBody::prettyPeek)
            .then()
            .assertThat()
            .statusCode(Status.OK.getStatusCode())
            .extract().as(UntersuchungKindDatenJsonSpec.class);
        // ASSERT
        Assertions.assertThat(result).isNotNull();
        Assertions.assertThat(result.getData().getName()).isEqualTo(kind.getName());
        Assertions.assertThat(result.getData().getGeburtsdatum()).isEqualTo(kind.getGeburtsdatum());
    }
}
