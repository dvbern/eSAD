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
class UnrecognizedPropertyExceptionMapperTest {
    @TestHTTPEndpoint(UnrecognizedPropertyExceptionResource.class)
    @TestHTTPResource
    URL url = null;

    @Test
    void sending_json_with_invalid_attribute_triggers() {
        var response = given()
            .when()
            .contentType(ContentType.JSON)
            .header("Accept-Language", "en")
            .body("""
                {
                   "name": "Helga",
                   "vorname": "Fehlerfall"
                }
                """)
            .post(url)
            .then()
            .statusCode(HttpURLConnection.HTTP_BAD_REQUEST)
                        .log().all()
            .extract()
            .response().body()
            .asPrettyString();

        assertThatJson(response)
            .withMatcher("exceptionId", MyMatchers.matchesExceptionId())
            .isEqualTo("""
                {
                  "type": "FAILURE",
                  "id": "${json-unit.matches:exceptionId}",
                  "summary": "Unrecognized property in Json: vorname (Type: TestObject). Path: vorname. Allowed properties: name."
                }
                """);
    }
}
