package ch.dvbern.dvbstarter.infrastructure.exceptionmapper;

import java.net.URL;
import java.util.List;

import ch.dvbern.dvbstarter.infrastructure.errorhandling.AppValidationException;
import ch.dvbern.dvbstarter.infrastructure.errorhandling.AppValidationMessage;
import ch.dvbern.dvbstarter.infrastructure.exceptionmapper.AppValidationTestingResource.NestedList;
import ch.dvbern.dvbstarter.infrastructure.exceptionmapper.AppValidationTestingResource.NestedString;
import ch.dvbern.dvbstarter.infrastructure.exceptionmapper.AppValidationTestingResource.ParamObject;
import ch.dvbern.oss.hemed.esc.generated.restassured.dto.AppValidationErrorJsonSpec;
import ch.dvbern.oss.hemed.esc.generated.restassured.dto.ViolationJsonSpec;
import io.quarkus.test.common.http.TestHTTPEndpoint;
import io.quarkus.test.common.http.TestHTTPResource;
import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.security.TestSecurity;
import io.restassured.http.ContentType;
import jakarta.ws.rs.core.Response.Status;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.DisplayNameGeneration;
import org.junit.jupiter.api.DisplayNameGenerator;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static ch.dvbern.oss.commons.i18nl10n.I18nMessageAssertions.assertThatI18nMessage;
import static io.restassured.RestAssured.given;
import static org.assertj.core.api.Assertions.assertThat;

@QuarkusTest
@TestSecurity(authorizationEnabled = false)
@Slf4j
@DisplayNameGeneration(DisplayNameGenerator.ReplaceUnderscores.class)
@SuppressWarnings("ResultOfMethodCallIgnored")
class AppValidationExceptionMapperTest {

    @TestHTTPEndpoint(AppValidationTestingResource.class)
    @TestHTTPResource("validate")
    URL validateUrl = null;

    @Test
    @SuppressWarnings("TestMethodWithoutAssertion")
    void sanity_check_with_valid_object() {

        var param = ParamObject.empty().withRootValue("ab");

        given()
            .body(param)
            .contentType(ContentType.JSON)
            .accept(ContentType.JSON)
            .when()
            .post(validateUrl)
            .then()
            .statusCode(Status.NO_CONTENT.getStatusCode())
        ;

    }

    @Test
    void an_invalid_object_in_root_produces_the_correct_path_and_message() {

        var param = ParamObject.empty().withRootValue("too-long");

        var response = given()
            .body(param)
            .contentType(ContentType.JSON)
            .accept(ContentType.JSON)
            .when()
            .post(validateUrl)
            .then()
            .statusCode(Status.BAD_REQUEST.getStatusCode())
            .extract().body()
            .as(AppValidationErrorJsonSpec.class);

        assertThat(response.getViolations())
            .containsExactlyInAnyOrder(
                new ViolationJsonSpec("rootValue", "Length", "Länge muss zwischen 0 und 2 sein")
            );
    }

    @Test
    void an_invalid_nested_string_produces_the_correct_path_and_message() {

        var param = ParamObject.empty().withNestedString(new NestedString("too-long"));

        var response = given()
            .body(param)
            .contentType(ContentType.JSON)
            .accept(ContentType.JSON)
            .when()
            .post(validateUrl)
            .then()
            .statusCode(Status.BAD_REQUEST.getStatusCode())
            .extract().body()
            .as(AppValidationErrorJsonSpec.class);

        assertThat(response.getViolations())
            .containsExactlyInAnyOrder(
                new ViolationJsonSpec("nestedString.value", "Length", "Länge muss zwischen 0 und 2 sein")
            );
    }

    @Test
    void an_invalid_nested_list_with_invalid_size_results_in_the_correct_path_and_message() {

        var param = ParamObject.empty().withNestedList(new NestedList(List.of(
            new NestedString("ok")))
        );

        var response = given()
            .body(param)
            .contentType(ContentType.JSON)
            .accept(ContentType.JSON)
            .when()
            .post(validateUrl)
            .then()
            .statusCode(Status.BAD_REQUEST.getStatusCode())
            .extract().body()
            .as(AppValidationErrorJsonSpec.class);

        assertThat(response.getViolations())
            .containsExactlyInAnyOrder(
                new ViolationJsonSpec("nestedList.list", "Size", "Größe muss zwischen 2 und 2147483647 sein")
            );
    }

    @Test
    void an_invalid_nested_list_with_an_invalid_entry_results_in_the_correct_path_and_message() {

        var param = ParamObject.empty().withNestedList(new NestedList(List.of(
                new NestedString("ok"),
                new NestedString("too-long")
            ))
        );

        var response = given()
            .body(param)
            .contentType(ContentType.JSON)
            .accept(ContentType.JSON)
            .when()
            .post(validateUrl)
            .then()
            .statusCode(Status.BAD_REQUEST.getStatusCode())
            .extract().body()
            .as(AppValidationErrorJsonSpec.class);

        assertThat(response.getViolations())
            .containsExactlyInAnyOrder(
                new ViolationJsonSpec("nestedList.list[1].value", "Length", "Länge muss zwischen 0 und 2 sein")
            );
    }

    @Nested
    class MessageFactoryMethods {
        @Test
        void invalidEmail_gets_generated_with_correct_key_and_args() {
            var ex = new AppValidationException(AppValidationMessage.invalidEmail("foo@bar"));

            assertThatI18nMessage(ex.getI18nMessage())
                .hasKey("AppValidation.INVALID_EMAIL")
                .hasArgs("email", "foo@bar");
        }
    }
}
