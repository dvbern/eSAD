package ch.dvbern.oss.hemed.esc.api.features.pub.settings;

import ch.dvbern.dvbstarter.shared.i18n.translations.AppLanguages;
import ch.dvbern.oss.hemed.esc.api.testutil.AbstractITest;
import ch.dvbern.oss.hemed.esc.api.testutil.restassured.RequestSpecUtil;
import ch.dvbern.oss.hemed.esc.api.testutil.security.TestAsAdminBE;
import ch.dvbern.oss.hemed.esc.api.testutil.security.TestAsAdminBS;
import ch.dvbern.oss.hemed.esc.generated.restassured.api.TenantSettingsApiSpec;
import ch.dvbern.oss.hemed.esc.generated.restassured.dto.TenantSettingsJsonSpec;
import io.quarkus.test.junit.QuarkusTest;
import io.restassured.response.ResponseBody;
import jakarta.ws.rs.core.Response.Status;
import lombok.RequiredArgsConstructor;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

@QuarkusTest
@RequiredArgsConstructor
class TenantSettingsResourceITest extends AbstractITest {

    private final TenantSettingsApiSpec api = TenantSettingsApiSpec.tenantSettings(RequestSpecUtil.quarkusSpec());

    @Override
    @BeforeEach
    public void setUp() {
        super.setUp();
    }

    @Test
    @TestAsAdminBS
    void getTenantSettings_returnsCorrectSettings() {
        // ARRANGE
        // settings are inserted in liquibase
        // ACT
        TenantSettingsJsonSpec result = api
            .tenantSettingsResourceGetTenantSettings()
            .execute(ResponseBody::prettyPeek)
            .then()
            .assertThat()
            .statusCode(Status.OK.getStatusCode())
            .extract().as(TenantSettingsJsonSpec.class);

        // ASSERT
        Assertions.assertThat(result)
            .isNotNull();
        Assertions.assertThat(result.getDefaultLanguage().name()).isEqualTo(AppLanguages.DE.name());
        Assertions.assertThat(result.getSupportedLanguages()).hasSize(1);
    }

    @Test
    @TestAsAdminBE
    void getTenantSettings_be_returnsCorrectSettings() {
        // ARRANGE
        // settings are inserted in liquibase
        // ACT
        TenantSettingsJsonSpec result = api
            .tenantSettingsResourceGetTenantSettings()
            .execute(ResponseBody::prettyPeek)
            .then()
            .assertThat()
            .statusCode(Status.OK.getStatusCode())
            .extract().as(TenantSettingsJsonSpec.class);

        // ASSERT
        Assertions.assertThat(result)
            .isNotNull();
        Assertions.assertThat(result.getDefaultLanguage().name()).isEqualTo(AppLanguages.DE.name());
        Assertions.assertThat(result.getSupportedLanguages()).hasSize(2);
    }
}
