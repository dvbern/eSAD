package ch.dvbern.dvbstarter.infrastructure.exceptionmapper;

import java.net.HttpURLConnection;
import java.net.URL;

import ch.dvbern.oss.hemed.esc.api.testutil.hamcrest.MyMatchers;
import io.quarkus.test.common.http.TestHTTPEndpoint;
import io.quarkus.test.common.http.TestHTTPResource;
import io.quarkus.test.junit.QuarkusTest;
import io.restassured.http.ContentType;
import org.junit.jupiter.api.Test;

import static io.restassured.RestAssured.given;
import static net.javacrumbs.jsonunit.assertj.JsonAssertions.assertThatJson;

@QuarkusTest
class WebApplicationExceptionMapperTest {
    @TestHTTPEndpoint(WebApplicationExceptionResource.class)
    @TestHTTPResource
    URL url = null;

    @Test
    void exception_creates_a_nice_response() {
        var response = given()
            .when()
            .contentType(ContentType.JSON)
            .post("/path-does-not-exist")
            .then()
            .statusCode(HttpURLConnection.HTTP_BAD_REQUEST)
            .extract()
            .response().body()
            .asPrettyString();

        assertThatJson(response)
            .withMatcher("exceptionId", MyMatchers.matchesExceptionId())
            .isEqualTo("""
                {
                  "type": "FAILURE",
                  "id": "${json-unit.matches:exceptionId}",
                  "summary": "HTTP 404 Not Found (URL: POST %s://%s:%s/api/path-does-not-exist)"
                }
                """.formatted(url.getProtocol(), url.getHost(), url.getPort()));
    }
}
