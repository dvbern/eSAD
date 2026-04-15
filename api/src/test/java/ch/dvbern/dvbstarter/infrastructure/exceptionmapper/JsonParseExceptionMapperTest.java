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
class JsonParseExceptionMapperTest {
    @TestHTTPEndpoint(JsonParseExceptionResource.class)
    @TestHTTPResource
    URL url = null;

    @Test
    void sending_invalid_json_triggers() {
        @SuppressWarnings("JsonStandardCompliance")
        var response = given()
            .when()
            .contentType(ContentType.JSON)
            .body("""
                asdf
                """)
            .post(url)
            .then()
            .statusCode(HttpURLConnection.HTTP_BAD_REQUEST)
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
                  "summary": "Interner Server-Fehler: JSON_PARSING error at location line: 1, column: 1. Message: Unrecognized token 'asdf': was expecting (JSON String, Number, Array, Object or token 'null', 'true' or 'false')."
                }
                """);
    }
}
