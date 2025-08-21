package ch.dvbern.dvbstarter.infrastructure.json;

import java.net.HttpURLConnection;
import java.net.URL;

import ch.dvbern.dvbstarter.shared.types.id.ID;
import ch.dvbern.oss.hemed.esc.api.shared.benutzer.Benutzer;
import io.quarkus.test.common.http.TestHTTPEndpoint;
import io.quarkus.test.common.http.TestHTTPResource;
import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.security.TestSecurity;
import jakarta.ws.rs.core.HttpHeaders;
import org.junit.jupiter.api.Test;

import static io.restassured.RestAssured.given;

@QuarkusTest
@TestSecurity(authorizationEnabled = false)
class CreatedResponseBuilderTest {

    static final ID<Benutzer> FIXTURE_ID = ID.parse("11111111-1111-1111-1111-111111111111", Benutzer.class);

    @TestHTTPEndpoint(CreatedResponseBuilderTestResource.class)
    @TestHTTPResource
    private final URL url = null;

    @Test
    @SuppressWarnings("TestMethodWithoutAssertion")
    void creates_a_location_response_header_with_redirect_to_the_getter_URL() {
        given()
            .when()
            .post(url)
            .then()
            .statusCode(HttpURLConnection.HTTP_CREATED)
            .header(HttpHeaders.LOCATION, url + "/" + FIXTURE_ID.getUUID());
    }

}
