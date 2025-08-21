package ch.dvbern.oss.hemed.esc.api.features.pub.clientconfig;

import java.net.HttpURLConnection;
import java.net.URL;

import io.quarkus.test.common.http.TestHTTPEndpoint;
import io.quarkus.test.common.http.TestHTTPResource;
import io.quarkus.test.junit.QuarkusTest;
import io.restassured.http.ContentType;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static io.restassured.RestAssured.given;
import static net.javacrumbs.jsonunit.JsonMatchers.jsonEquals;

@QuarkusTest
class ClientConfigResourceTest {
    @TestHTTPEndpoint(ClientConfigResource.class)
    @TestHTTPResource("config")
    URL url = null;

    @ConfigProperty(name = "app.deployment.public-url")
    String publicUrl;

    @ConfigProperty(name = "quarkus.application.version")
    String appVersion;

    @ConfigProperty(name = "app.allow-timetravel")
    String allowTimetravel;

    @Nested
    class Deployment {
        @Test
        void returns_configuration_relevant_for_the_client() {
            given()
                .when()
                .contentType(ContentType.JSON)
                .get(url)
                .then()
                .assertThat()
                .statusCode(HttpURLConnection.HTTP_OK)
                .body(jsonEquals("""
{
    "clientDeploymentConfigJson": {
        "environment": "UNIT_TEST",
        "version": "%s",
    	"publicUrl": "%s"
	},
	"clientAuthConfig": {
		"clientId": "unit-test-client-id",
		"realm": "unit-test-realm",
		"authUrl": "http://dummy.keycloak",
		"logoutUrl": "http://dummy.keycloak/realms/bs/protocol/openid-connect/logout"
	},
	"featureConfigJson": {
	    "timetravelEnabled": %s
	}
}""".formatted(appVersion, publicUrl, allowTimetravel.toLowerCase())));

        }
    }

}
