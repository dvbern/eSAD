package ch.dvbern.oss.hemed.esc.api.features.example;

import java.net.HttpURLConnection;
import java.net.URL;
import java.util.UUID;

import ch.dvbern.dvbstarter.shared.types.emailaddress.EmailAddress;
import ch.dvbern.oss.hemed.esc.api.testutil.security.TestAsSupport;
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
class ExampleResourceITest {

    @TestHTTPEndpoint(ExampleResource.class)
    @TestHTTPResource
    URL url = null;

    @Test
    @TestAsSupport
    void createExample_should_return_dto() {

        var email = EmailAddress.fromString("somebody-%s@somewhere.org".formatted(UUID.randomUUID().toString()));

        var body = """
            {
              "email": "%s",
              "phoneNumber": "034123 4567",
              "example": "test crete example json"
            }
            """.formatted(email);
        var response = given()
            .when()
            .contentType(ContentType.JSON)
            .body(body)
            .post(url)
            .then();

        response
            .log()
            .body()
            .assertThat()
            .statusCode(HttpURLConnection.HTTP_CREATED);

        String returnedPhone = response.extract().path("phoneNumber");
        assertThat(returnedPhone).isEqualTo("+41 34 123 45 67");

    }
}
