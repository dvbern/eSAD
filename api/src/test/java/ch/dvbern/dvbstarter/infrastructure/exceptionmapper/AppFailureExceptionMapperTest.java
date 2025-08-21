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
class AppFailureExceptionMapperTest {
    @TestHTTPEndpoint(AppFailureExceptionResource.class)
    @TestHTTPResource
    URL url = null;

    @Test
    void makes_a_nice_response() {
        var response = given()
            .when()
            .contentType(ContentType.JSON)
            .body("""
                {
                    "value": null
                }
                """)
            .post(url)
            .then()
            .statusCode(HttpURLConnection.HTTP_INTERNAL_ERROR)
            //            .log().all()
            .extract()
            .response().body()
            .asPrettyString();

        assertThatJson(response)
            .withMatcher("exceptionId", MyMatchers.matchesExceptionId())
            .isEqualTo("""
                {
                  "type": "FAILURE",
                  "id": "${json-unit.matches:exceptionId}",
                  "summary": "Interner Server-Fehler: \\"hello exception\\"."
                }
                """);
    }
}
