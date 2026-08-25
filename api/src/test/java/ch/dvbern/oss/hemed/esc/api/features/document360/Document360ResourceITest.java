package ch.dvbern.oss.hemed.esc.api.features.document360;

import java.net.URI;

import ch.dvbern.oss.hemed.esc.api.testutil.AbstractITest;
import ch.dvbern.oss.hemed.esc.api.testutil.restassured.RequestSpecUtil;
import ch.dvbern.oss.hemed.esc.api.testutil.security.TestAsAdminBS;
import io.quarkus.test.junit.QuarkusMock;
import io.quarkus.test.junit.QuarkusTest;
import io.restassured.RestAssured;
import jakarta.ws.rs.core.Response.Status;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

@QuarkusTest
class Document360ResourceITest extends AbstractITest {

    Document360Service document360Service = Mockito.mock(Document360Service.class);

    @Override
    @BeforeEach
    public void setUp() {
        super.setUp();
        QuarkusMock.installMockForType(document360Service, Document360Service.class);
    }

    @Test
    @TestAsAdminBS
    void authenticate_shouldRedirect() {
        // ARRANGE
        URI expectedRedirectUri = URI.create("https://kb.document360.io/jwt/authorize?code=test-code");
        Mockito.when(document360Service.authenticateAndGetRedirectUri()).thenReturn(expectedRedirectUri);

        // ACT
        RestAssured.given(RequestSpecUtil.quarkusSpec().get().build())
            .when()
            .redirects().follow(false)
            .get("/api/document360/authenticate")
            .then()
            .assertThat()
            .statusCode(Status.SEE_OTHER.getStatusCode())
            .header("Location", expectedRedirectUri.toString());

        // ASSERT
        Mockito.verify(document360Service).authenticateAndGetRedirectUri();
    }
}
