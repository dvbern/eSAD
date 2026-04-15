package ch.dvbern.oss.hemed.esc.api.features.example.developer;

import ch.dvbern.oss.hemed.esc.api.testutil.AbstractITest;
import ch.dvbern.oss.hemed.esc.api.testutil.restassured.RequestSpecUtil;
import ch.dvbern.oss.hemed.esc.api.testutil.security.TestAsSupportBS;
import ch.dvbern.oss.hemed.esc.generated.restassured.api.DeveloperApiSpec;
import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.junit.TestProfile;
import io.restassured.response.ResponseBody;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;

@QuarkusTest
@TestProfile(ConfigPrintDisabledTestProfile.class)
class DeveloperResourceConfigDisabledITest extends AbstractITest {

    DeveloperApiSpec developerApiSpec = DeveloperApiSpec.developer(RequestSpecUtil.quarkusSpec());

    @Test
    @TestAsSupportBS
    void getConfigDisabled() {
        // ACT
        var result = developerApiSpec
            .developerResourceGetConfig()
            .execute(ResponseBody::prettyPeek)
            .then()
            .assertThat()
            .statusCode(200)
            .extract().asString();

        // ASSERT
        Assertions.assertThat(result).isNotNull()
            .isEqualTo("app.allow-configprint is disabled, to print all config values set it to true");
    }
}
