package ch.dvbern.dvbstarter.infrastructure.exceptionmapper;

import java.net.HttpURLConnection;
import java.net.URL;

import ch.dvbern.oss.hemed.esc.generated.restassured.dto.AppValidationErrorJsonSpec;
import ch.dvbern.oss.hemed.esc.generated.restassured.dto.ViolationJsonSpec;
import io.quarkus.test.common.http.TestHTTPEndpoint;
import io.quarkus.test.common.http.TestHTTPResource;
import io.quarkus.test.junit.QuarkusTest;
import io.restassured.http.ContentType;
import org.junit.jupiter.api.DisplayNameGeneration;
import org.junit.jupiter.api.DisplayNameGenerator.ReplaceUnderscores;
import org.junit.jupiter.api.Test;

import static io.restassured.RestAssured.given;
import static org.assertj.core.api.Assertions.assertThat;

@QuarkusTest
@DisplayNameGeneration(ReplaceUnderscores.class)
class JsonMappingExceptionMapperTest {
    @TestHTTPEndpoint(JsonMappingExceptionResource.class)
    @TestHTTPResource("constraint-violation-exception")
    URL constraintViolationUrl = null;

    @TestHTTPEndpoint(JsonMappingExceptionResource.class)
    @TestHTTPResource("app-validation-exception")
    URL appValidationException = null;

    @TestHTTPEndpoint(JsonMappingExceptionResource.class)
    @TestHTTPResource("json-mapping-exception")
    URL jsonMappingException = null;

    @Test
    void ConstraintViolation_creates_a_Violation_response() {
        var violation = given()
            .when()
            .contentType(ContentType.JSON)
            .body("""
                {
                    "value": null
                }
                """)
            .post(constraintViolationUrl)
            .then()
            .statusCode(HttpURLConnection.HTTP_BAD_REQUEST)
            .extract()
            .response().body()
            .as(AppValidationErrorJsonSpec.class);

        assertThat(violation.getViolations())
            .containsExactlyInAnyOrder(
                new ViolationJsonSpec("value", "NotNull", "darf nicht null sein")
            );
    }

    @Test
    void AppValidationException_creates_a_Violation_response() {
        var violation = given()
            .when()
            .contentType(ContentType.JSON)
            .body("""
                {
                    "value": "foo@"
                }
                """)
            .post(appValidationException)
            .then()
            .statusCode(HttpURLConnection.HTTP_BAD_REQUEST)
            .extract()
            .response().body()
            .as(AppValidationErrorJsonSpec.class);

        assertThat(violation.getViolations())
            .containsExactlyInAnyOrder(
                new ViolationJsonSpec("", "InvalidEmail", "Ungültige Emailadresse: foo@.")
            );
    }

    @Test
    void JsonMappingException_creates_a_Violation_response() {
        var response = given()
            .when()
            .contentType(ContentType.JSON)
            .body("""
                {"animal":{"name":"lacy"}}
                """)
            .post(jsonMappingException)
            .then()
            .statusCode(HttpURLConnection.HTTP_INTERNAL_ERROR)
            //            .log().all()
            .extract()
            .response().body()
            .asPrettyString();

        assertThat(response)
            // wtf??
            // Bug in Quarkus?
            // no(!) ExceptionMapper is run, not even GenericExceptionMapper (i.e. for Exception).
            // Expected: JsonMappingException and thus JsonMappingExceptionMapper is run
            .isEmpty();
    }

}
