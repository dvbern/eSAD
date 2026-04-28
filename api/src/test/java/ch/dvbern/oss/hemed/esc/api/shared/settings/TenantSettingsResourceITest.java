package ch.dvbern.oss.hemed.esc.api.shared.settings;

import java.util.List;

import ch.dvbern.dvbstarter.shared.i18n.translations.AppLanguages;
import ch.dvbern.oss.hemed.esc.api.testutil.AbstractITest;
import ch.dvbern.oss.hemed.esc.api.testutil.restassured.RequestSpecUtil;
import ch.dvbern.oss.hemed.esc.api.testutil.security.TestAsAdminBE;
import ch.dvbern.oss.hemed.esc.api.testutil.security.TestAsAdminBS;
import ch.dvbern.oss.hemed.esc.api.testutil.security.TestAsSupportBS;
import ch.dvbern.oss.hemed.esc.generated.restassured.api.TenantSettingsApiSpec;
import ch.dvbern.oss.hemed.esc.generated.restassured.dto.AgbResponsabilitySpec;
import ch.dvbern.oss.hemed.esc.generated.restassured.dto.AppLanguagesSpec;
import ch.dvbern.oss.hemed.esc.generated.restassured.dto.EinladungStatusSpec;
import ch.dvbern.oss.hemed.esc.generated.restassured.dto.FeatureEnabledSettingSpec;
import ch.dvbern.oss.hemed.esc.generated.restassured.dto.LandingPageOptionSpec;
import ch.dvbern.oss.hemed.esc.generated.restassured.dto.TenantSettingFeatureStateJsonSpec;
import ch.dvbern.oss.hemed.esc.generated.restassured.dto.TenantSettingFeaturesJsonSpec;
import ch.dvbern.oss.hemed.esc.generated.restassured.dto.TenantSettingSaveJsonSpec;
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
        Assertions.assertThat(result.getData().getDefaultLanguage().name()).isEqualTo(AppLanguages.DE.name());
        Assertions.assertThat(result.getData().getFolgeTermineMaxSlots()).isEqualTo(16);
        Assertions.assertThat(result.getData().getSupportedLanguages()).hasSize(1);
        Assertions.assertThat(result.getData().getEinladungStatusValues()).hasSize(3);

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
        Assertions.assertThat(result.getData().getDefaultLanguage().name()).isEqualTo(AppLanguages.DE.name());
        Assertions.assertThat(result.getData().getFolgeTermineMaxSlots()).isEqualTo(16);
        Assertions.assertThat(result.getData().getSupportedLanguages()).hasSize(1);
    }

    @Test
    @TestAsSupportBS
    void updateTenantSettings_changes_data_as_requested() {
        // ARRANGE
        // settings are inserted in liquibase
        // ACT
        TenantSettingsJsonSpec initial = api
            .tenantSettingsResourceGetTenantSettings()
            .execute(ResponseBody::prettyPeek)
            .then()
            .assertThat()
            .statusCode(Status.OK.getStatusCode())
            .extract().as(TenantSettingsJsonSpec.class);

        Boolean irrelevant = false;
        TenantSettingSaveJsonSpec saveJson = new TenantSettingSaveJsonSpec(
            AppLanguagesSpec.FR,
            List.of(AppLanguagesSpec.FR, AppLanguagesSpec.DE),
            42,
            "test@mailbucket.dvbern.ch",
            "test@mailbucket.dvbern.ch",
            AgbResponsabilitySpec.AGB_MANAGED_BY_TENANT,
            new TenantSettingFeaturesJsonSpec( // Add Assertions below if new Feature is added
                new TenantSettingFeatureStateJsonSpec(FeatureEnabledSettingSpec.DISABLED, false),
                new TenantSettingFeatureStateJsonSpec(FeatureEnabledSettingSpec.DISABLED, irrelevant),
                new TenantSettingFeatureStateJsonSpec(FeatureEnabledSettingSpec.DISABLED, irrelevant),
                new TenantSettingFeatureStateJsonSpec(FeatureEnabledSettingSpec.DISABLED, irrelevant),
                new TenantSettingFeatureStateJsonSpec(FeatureEnabledSettingSpec.DISABLED, irrelevant),
                new TenantSettingFeatureStateJsonSpec(FeatureEnabledSettingSpec.DISABLED, irrelevant),
                new TenantSettingFeatureStateJsonSpec(FeatureEnabledSettingSpec.DISABLED, irrelevant),
                new TenantSettingFeatureStateJsonSpec(FeatureEnabledSettingSpec.DISABLED, irrelevant),
                new TenantSettingFeatureStateJsonSpec(FeatureEnabledSettingSpec.DISABLED, irrelevant),
                new TenantSettingFeatureStateJsonSpec(FeatureEnabledSettingSpec.DISABLED, irrelevant),
                new TenantSettingFeatureStateJsonSpec(FeatureEnabledSettingSpec.DISABLED, irrelevant),
                new TenantSettingFeatureStateJsonSpec(FeatureEnabledSettingSpec.DISABLED, irrelevant),
                new TenantSettingFeatureStateJsonSpec(FeatureEnabledSettingSpec.DISABLED, irrelevant),
                new TenantSettingFeatureStateJsonSpec(FeatureEnabledSettingSpec.DISABLED, irrelevant)
            ),
            List.of(EinladungStatusSpec.TEILNAHME_BESTAETIGT, EinladungStatusSpec.PRIVATARZT_BESTAETIGT),
            LandingPageOptionSpec.DASHBOARD);

        var result = api.tenantSettingsResourceUpdateTenantSettings()
            .body(saveJson)
            .execute(ResponseBody::prettyPeek)
            .then()
            .assertThat()
            .statusCode(Status.OK.getStatusCode())
            .extract().as(TenantSettingsJsonSpec.class);

        // ASSERT
        Assertions.assertThat(result)
            .isNotNull();
        Assertions.assertThat(result.getData().getDefaultLanguage().name()).isEqualTo(AppLanguages.FR.name());
        Assertions.assertThat(result.getData().getSupportedLanguages()).hasSize(2);
        Assertions.assertThat(result.getData().getFolgeTermineMaxSlots()).isEqualTo(42);
        Assertions.assertThat(result.getData().getDefaultLandingPage())
            .isSameAs(LandingPageOptionSpec.DASHBOARD);
        Assertions.assertThat(result.getData().getFeatures().getEsadIdFeature().getFeatureEnabled())
            .isEqualTo(FeatureEnabledSettingSpec.DISABLED);
        Assertions.assertThat(result.getData().getFeatures().getLaenderCodeFeature().getFeatureEnabled())
            .isEqualTo(FeatureEnabledSettingSpec.DISABLED);
        Assertions.assertThat(result.getData().getFeatures().getMobileScannerFeature().getFeatureEnabled())
            .isEqualTo(FeatureEnabledSettingSpec.DISABLED);
        Assertions.assertThat(result.getData().getFeatures().getArchivierungFeature().getFeatureEnabled())
            .isEqualTo(FeatureEnabledSettingSpec.DISABLED);
        Assertions.assertThat(result.getData().getFeatures().getMassenimportFeature().getFeatureEnabled())
            .isEqualTo(FeatureEnabledSettingSpec.DISABLED);
        Assertions.assertThat(result.getData().getFeatures().getMassenbriefFeature().getFeatureEnabled())
            .isEqualTo(FeatureEnabledSettingSpec.DISABLED);
        Assertions.assertThat(result.getData().getFeatures().getNichtGeprueftGrundFeature().getFeatureEnabled())
            .isEqualTo(FeatureEnabledSettingSpec.DISABLED);
        Assertions.assertThat(result.getData().getFeatures().getArztListeFeature().getFeatureEnabled())
            .isEqualTo(FeatureEnabledSettingSpec.DISABLED);
        Assertions.assertThat(result.getData().getFeatures().getAdHocImpfstoffChargenFeature().getFeatureEnabled())
            .isEqualTo(FeatureEnabledSettingSpec.DISABLED);
        Assertions.assertThat(result.getData().getFeatures().getFooterFeature().getFeatureEnabled())
            .isEqualTo(FeatureEnabledSettingSpec.DISABLED);
        Assertions.assertThat(result.getData().getFeatures().getBenutzerverwaltungInternFeature().getFeatureEnabled())
            .isEqualTo(FeatureEnabledSettingSpec.DISABLED);
        Assertions.assertThat(result.getData().getFeatures().getBenutzerverwaltungIgnoreFachRolleFromKeycloakFeature().getFeatureEnabled())
            .isEqualTo(FeatureEnabledSettingSpec.DISABLED);
        Assertions.assertThat(result.getData().getFeatures().getZentraleOrganisationFeature().getFeatureEnabled())
            .isEqualTo(FeatureEnabledSettingSpec.DISABLED);
        Assertions.assertThat(result.getData().getFeatures().getAnonymeStatistikFeature().getFeatureEnabled())
            .isEqualTo(FeatureEnabledSettingSpec.DISABLED);
        Assertions.assertThat(result.getData()
            .getEinladungStatusValues()
            .stream()
            .anyMatch(einladungStatus -> einladungStatus == EinladungStatusSpec.TEILNAHME_BESTAETIGT)).isTrue();
        Assertions.assertThat(result.getData()
            .getEinladungStatusValues()
            .stream()
            .anyMatch(einladungStatus -> einladungStatus == EinladungStatusSpec.PRIVATARZT_BESTAETIGT)).isTrue();

        // RESET value in db, not pretty but this should make sure we dont break other tests
        api.tenantSettingsResourceUpdateTenantSettings()
            .body(initial.getData())
            .execute(ResponseBody::prettyPeek)
            .then()
            .assertThat()
            .statusCode(Status.OK.getStatusCode());
    }
}

