package ch.dvbern.oss.hemed.esc.api.shared.archivierung;

import ch.dvbern.dvbstarter.infrastructure.deployment.DeploymentEnvironment;
import ch.dvbern.oss.hemed.esc.api.shared.auth.TenantEnum;
import ch.dvbern.oss.hemed.esc.api.shared.auth.TenantProducer;
import ch.dvbern.oss.hemed.esc.api.shared.config.service.ConfigService;
import ch.dvbern.oss.hemed.esc.api.shared.settings.TenantSetting;
import ch.dvbern.oss.hemed.esc.api.shared.settings.TenantSettingsService;
import ch.dvbern.oss.hemed.esc.api.shared.settings.features.FeatureEnabledSetting;
import ch.dvbern.oss.hemed.esc.api.shared.settings.features.TenantSettingFeatures;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class ArchivierungRuleProducerTest {

    private TenantProducer tenantProducer;
    private ConfigService configService;
    private TenantSettingsService tenantSettingsService;
    private ArchivierungRuleByAgeService archivierungRuleByAgeService;
    private ArchivierungRuleNoopService archivierungRuleNoopService;
    private ArchivierungRuleProducer service;

    private TenantSetting tenantSetting;
    private TenantSettingFeatures features;

    @BeforeEach
    void setUp() {
        tenantProducer = mock(TenantProducer.class);
        configService = mock(ConfigService.class);
        tenantSettingsService = mock(TenantSettingsService.class);
        archivierungRuleByAgeService = mock(ArchivierungRuleByAgeService.class);
        archivierungRuleNoopService = mock(ArchivierungRuleNoopService.class);

        service = new ArchivierungRuleProducer(
            tenantProducer,
            configService,
            tenantSettingsService,
            archivierungRuleByAgeService,
            archivierungRuleNoopService
        );

        tenantSetting = mock(TenantSetting.class);
        features = mock(TenantSettingFeatures.class);
        when(tenantSettingsService.getTenantSetting()).thenReturn(tenantSetting);
        when(tenantSetting.getTenantSettingFeatures()).thenReturn(features);
        when(configService.getEnvironment()).thenReturn(DeploymentEnvironment.LOCAL);
    }

    @Test
    void testArchivierungRuleService_FeatureDisabled() {
        // ARRANGE
        when(features.getArchivierungFeature()).thenReturn(FeatureEnabledSetting.DISABLED);

        // ACT
        ArchivierungRuleService result = service.archivierungRuleService();

        // ASSERT
        assertThat(result).isSameAs(archivierungRuleNoopService);
    }

    @Test
    void testArchivierungRuleService_FeatureEnabled_TenantBE() {
        // ARRANGE
        when(features.getArchivierungFeature()).thenReturn(FeatureEnabledSetting.ENABLED);
        when(tenantProducer.produceTenant()).thenReturn(TenantEnum.BE);

        // ACT
        ArchivierungRuleService result = service.archivierungRuleService();

        // ASSERT
        assertThat(result).isSameAs(archivierungRuleByAgeService);
    }

    @ParameterizedTest
    @EnumSource(value = TenantEnum.class, names = {"BS", "WIN"})
    void testArchivierungRuleService_FeatureEnabled_TenantBS_WIN_ThrowsException(TenantEnum tenant) {
        // ARRANGE
        when(features.getArchivierungFeature()).thenReturn(FeatureEnabledSetting.ENABLED);
        when(tenantProducer.produceTenant()).thenReturn(tenant);

        // ACT & ASSERT
        assertThatThrownBy(() -> service.archivierungRuleService())
            .isInstanceOf(IllegalStateException.class)
            .hasMessageContaining("ArchivierungRuleService not defined for tenant: " + tenant);
    }

    @Test
    void testArchivierungRuleService_FeatureEnabled_DefaultTenant_ThrowsException() {
        // ARRANGE
        when(features.getArchivierungFeature()).thenReturn(FeatureEnabledSetting.ENABLED);
        when(tenantProducer.produceTenant()).thenReturn(TenantEnum.DEFAULT);

        // ACT & ASSERT
        assertThatThrownBy(() -> service.archivierungRuleService())
            .isInstanceOf(IllegalStateException.class)
            .hasMessageContaining("Unexpected value: " + TenantEnum.DEFAULT);
    }
}
