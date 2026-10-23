package ch.dvbern.oss.hemed.esc.api.shared.settings;

import java.time.LocalDate;
import java.time.Month;
import java.util.List;

import ch.dvbern.dvbstarter.shared.i18n.translations.AppLanguages;
import ch.dvbern.oss.hemed.esc.api.testutil.AbstractITest;
import ch.dvbern.oss.hemed.esc.api.testutil.assertj.AppValidationErrorJsonSpecAssert;
import ch.dvbern.oss.hemed.esc.api.testutil.restassured.RequestSpecUtil;
import ch.dvbern.oss.hemed.esc.api.testutil.security.TestAsAdminBE;
import ch.dvbern.oss.hemed.esc.api.testutil.security.TestAsAdminBS;
import ch.dvbern.oss.hemed.esc.api.testutil.security.TestAsSupportBS;
import ch.dvbern.oss.hemed.esc.generated.restassured.api.TenantSettingsApiSpec;
import ch.dvbern.oss.hemed.esc.generated.restassured.dto.AgbResponsabilitySpec;
import ch.dvbern.oss.hemed.esc.generated.restassured.dto.AppLanguagesSpec;
import ch.dvbern.oss.hemed.esc.generated.restassured.dto.AppValidationErrorJsonSpec;
import ch.dvbern.oss.hemed.esc.generated.restassured.dto.EinladungStatusSpec;
import ch.dvbern.oss.hemed.esc.generated.restassured.dto.FachRolleSpec;
import ch.dvbern.oss.hemed.esc.generated.restassured.dto.FeatureEnabledSettingSpec;
import ch.dvbern.oss.hemed.esc.generated.restassured.dto.LandingPageOptionSpec;
import ch.dvbern.oss.hemed.esc.generated.restassured.dto.LoginMethodSpec;
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
        Assertions.assertThat(result.getData().getLoginMethods())
            .containsExactlyInAnyOrder(LoginMethodSpec.PASSWORD, LoginMethodSpec.DVBERN_HEMED_ENTRA);
        // loginOptions follow LoginMethodIdpHintResolver.DISPLAY_ORDER: DVBERN_HEMED_ENTRA before PASSWORD
        Assertions.assertThat(result.getLoginOptions()).hasSize(2);
        Assertions.assertThat(result.getLoginOptions().get(0).getMethod()).isEqualTo(LoginMethodSpec.DVBERN_HEMED_ENTRA);
        Assertions.assertThat(result.getLoginOptions().get(0).getIdpHint()).isNotBlank();
        Assertions.assertThat(result.getLoginOptions().get(1).getMethod()).isEqualTo(LoginMethodSpec.PASSWORD);
        Assertions.assertThat(result.getLoginOptions().get(1).getIdpHint()).isEmpty();
        Assertions.assertThat(result.getFachrollen()).containsExactlyInAnyOrder(
            FachRolleSpec.EMPFANG,
            FachRolleSpec.MPA,
            FachRolleSpec.ORTHOPISTIN,
            FachRolleSpec.SCHULAERZTIN,
            FachRolleSpec.SUPERUSER,
            FachRolleSpec.ADMIN,
            FachRolleSpec.SUPPORT,
            FachRolleSpec.SYSTEM_ADMIN
        );
        Assertions.assertThat(result.getDelegierbareFachrollen()).containsExactlyInAnyOrder(FachRolleSpec.MPA);

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
        // All tenants start with the PASSWORD + DVBERN_HEMED_ENTRA seed. On tenants with a single effective
        // login method Keycloak is configured to redirect directly to the hemed realm (defaultProvider),
        // so the seeded default is never visible there. BeLogin/HIN for Bern are enabled per environment
        // via the Support UI only after the broker IdPs and allow-list entries exist (see plan Step 5)
        // - so no db/be changeset.
        Assertions.assertThat(result.getData().getLoginMethods())
            .containsExactlyInAnyOrder(LoginMethodSpec.PASSWORD, LoginMethodSpec.DVBERN_HEMED_ENTRA);
        // loginOptions follow LoginMethodIdpHintResolver.DISPLAY_ORDER: DVBERN_HEMED_ENTRA before PASSWORD
        Assertions.assertThat(result.getLoginOptions()).hasSize(2);
        Assertions.assertThat(result.getLoginOptions().get(0).getMethod()).isEqualTo(LoginMethodSpec.DVBERN_HEMED_ENTRA);
        Assertions.assertThat(result.getLoginOptions().get(0).getIdpHint()).contains("hemed-entra-client");
        Assertions.assertThat(result.getLoginOptions().get(1).getMethod()).isEqualTo(LoginMethodSpec.PASSWORD);
        Assertions.assertThat(result.getLoginOptions().get(1).getIdpHint()).isEmpty();
        Assertions.assertThat(result.getFachrollen()).containsExactlyInAnyOrder(
            FachRolleSpec.MPA,
            FachRolleSpec.SCHULAERZTIN,
            FachRolleSpec.SCHOOL_NURSE,
            FachRolleSpec.SUPPORT,
            FachRolleSpec.SYSTEM_ADMIN
        );
        Assertions.assertThat(result.getDelegierbareFachrollen()).containsExactlyInAnyOrder(FachRolleSpec.MPA);
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
                new TenantSettingFeatureStateJsonSpec(FeatureEnabledSettingSpec.DISABLED, irrelevant),
                new TenantSettingFeatureStateJsonSpec(FeatureEnabledSettingSpec.DISABLED, irrelevant),
                new TenantSettingFeatureStateJsonSpec(FeatureEnabledSettingSpec.DISABLED, irrelevant),
                new TenantSettingFeatureStateJsonSpec(FeatureEnabledSettingSpec.DISABLED, irrelevant),
                new TenantSettingFeatureStateJsonSpec(FeatureEnabledSettingSpec.DISABLED, irrelevant),
                new TenantSettingFeatureStateJsonSpec(FeatureEnabledSettingSpec.DISABLED, irrelevant),
                new TenantSettingFeatureStateJsonSpec(FeatureEnabledSettingSpec.DISABLED, irrelevant)
            ),
            List.of(EinladungStatusSpec.TEILNAHME_BESTAETIGT, EinladungStatusSpec.PRIVATARZT_BESTAETIGT),
            LandingPageOptionSpec.DASHBOARD,
            LocalDate.of(2026, Month.OCTOBER, 6),
            List.of(LoginMethodSpec.DVBERN_HEMED_ENTRA, LoginMethodSpec.PASSWORD));

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
        Assertions.assertThat(result.getData()
                .getFeatures()
                .getBenutzerverwaltungIgnoreFachRolleFromKeycloakFeature()
                .getFeatureEnabled())
            .isEqualTo(FeatureEnabledSettingSpec.DISABLED);
        Assertions.assertThat(result.getData().getFeatures().getZentraleOrganisationFeature().getFeatureEnabled())
            .isEqualTo(FeatureEnabledSettingSpec.DISABLED);
        Assertions.assertThat(result.getData().getFeatures().getAnonymeStatistikFeature().getFeatureEnabled())
            .isEqualTo(FeatureEnabledSettingSpec.DISABLED);
        Assertions.assertThat(result.getData().getFeatures().getNachkontrolleFeature().getFeatureEnabled())
            .isEqualTo(FeatureEnabledSettingSpec.DISABLED);
        Assertions.assertThat(result.getData().getFeatures().getAnlassSpracheSpalteFeature().getFeatureEnabled())
            .isEqualTo(FeatureEnabledSettingSpec.DISABLED);
        Assertions.assertThat(result.getData()
                .getFeatures()
                .getAnlassSpracheZusammenfassungFeature()
                .getFeatureEnabled())
            .isEqualTo(FeatureEnabledSettingSpec.DISABLED);
        Assertions.assertThat(result.getData().getFeatures().getAnlassInformationenFeature().getFeatureEnabled())
            .isEqualTo(FeatureEnabledSettingSpec.DISABLED);
        Assertions.assertThat(result.getData().getFeatures().getAnlassTitleRequiredFeature().getFeatureEnabled())
            .isEqualTo(FeatureEnabledSettingSpec.DISABLED);
        Assertions.assertThat(result.getData().getFeatures().getTenantHeaderInExportsFeature().getFeatureEnabled())
            .isEqualTo(FeatureEnabledSettingSpec.DISABLED);
        Assertions.assertThat(result.getData()
            .getEinladungStatusValues()
            .stream()
            .anyMatch(einladungStatus -> einladungStatus == EinladungStatusSpec.TEILNAHME_BESTAETIGT)).isTrue();
        Assertions.assertThat(result.getData()
            .getEinladungStatusValues()
            .stream()
            .anyMatch(einladungStatus -> einladungStatus == EinladungStatusSpec.PRIVATARZT_BESTAETIGT)).isTrue();
        Assertions.assertThat(result.getData().getLoginMethods())
            .containsExactlyInAnyOrder(LoginMethodSpec.DVBERN_HEMED_ENTRA, LoginMethodSpec.PASSWORD);
        Assertions.assertThat(result.getLoginOptions()).hasSize(2);

        // RESET value in db, not pretty but this should make sure we dont break other tests
        api.tenantSettingsResourceUpdateTenantSettings()
            .body(initial.getData())
            .execute(ResponseBody::prettyPeek)
            .then()
            .assertThat()
            .statusCode(Status.OK.getStatusCode());
    }

    @Test
    @TestAsSupportBS
    void updateTenantSettings_with_empty_loginMethods_fails() {
        // ARRANGE
        TenantSettingsJsonSpec initial = api
            .tenantSettingsResourceGetTenantSettings()
            .execute(ResponseBody::prettyPeek)
            .then()
            .assertThat()
            .statusCode(Status.OK.getStatusCode())
            .extract().as(TenantSettingsJsonSpec.class);

        TenantSettingSaveJsonSpec saveJson = initial.getData();
        saveJson.setLoginMethods(List.of());

        // ACT
        AppValidationErrorJsonSpec validationError = api.tenantSettingsResourceUpdateTenantSettings()
            .body(saveJson)
            .execute(ResponseBody::prettyPeek)
            .then()
            .assertThat()
            .statusCode(Status.BAD_REQUEST.getStatusCode())
            .extract().as(AppValidationErrorJsonSpec.class);

        // ASSERT
        AppValidationErrorJsonSpecAssert.assertThatAppValidationErrorJsonSpec(validationError)
            .extractingExactlyOneViolation()
            .hasRawMessageContaining("Größe muss zwischen 1 und 2147483647 sein");
    }
}

