package ch.dvbern.oss.hemed.esc.api.features.impfstoff;

import java.util.Optional;

import ch.dvbern.dvbstarter.infrastructure.deployment.DeploymentEnvironment;
import ch.dvbern.dvbstarter.infrastructure.errorhandling.AppValidationException;
import ch.dvbern.oss.hemed.esc.api.shared.config.service.ConfigService;
import ch.dvbern.oss.hemed.esc.api.shared.settings.TenantSetting;
import ch.dvbern.oss.hemed.esc.api.shared.settings.TenantSettingsService;
import ch.dvbern.oss.hemed.esc.api.shared.settings.features.FeatureEnabledSetting;
import ch.dvbern.oss.hemed.esc.api.testutil.builder.ImpfstoffBuilder;
import ch.dvbern.oss.hemed.esc.api.testutil.builder.ImpfstoffChargeBuilder;
import ch.dvbern.oss.hemed.esc.api.testutil.builder.TenantSettingBuilder;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayNameGeneration;
import org.junit.jupiter.api.DisplayNameGenerator.ReplaceUnderscores;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@DisplayNameGeneration(ReplaceUnderscores.class)
class ImpfstoffChargeServiceTest {

    private final ImpfstoffChargeRepo impfstoffChargeRepo = Mockito.mock(ImpfstoffChargeRepo.class);
    private final TenantSettingsService tenantSettingsService = Mockito.mock(TenantSettingsService.class);
    private final ConfigService configService = Mockito.mock(ConfigService.class);

    private final ImpfstoffChargeService impfstoffChargeService = new ImpfstoffChargeService(
        impfstoffChargeRepo,
        tenantSettingsService,
        configService
    );

    private final Impfstoff impfstoff = ImpfstoffBuilder.defaultImpfstoff();

    @BeforeEach
    void setUp() {
        when(configService.getEnvironment())
            .thenReturn(DeploymentEnvironment.LOCAL);
    }

    private void mockFeatureSetting(FeatureEnabledSetting setting) {
        TenantSetting tenantSetting = TenantSettingBuilder.create(builder -> builder
            .withAdHocImpfstoffChargenFeature(setting)
        );
        when(tenantSettingsService.getTenantSetting())
            .thenReturn(tenantSetting);
    }

    @Test
    void getChargeOrCreateNewIfFeatureEnabled_returns_null_when_chargeName_is_null() {
        // ARRANGE
        mockFeatureSetting(FeatureEnabledSetting.ENABLED);

        // ACT
        ImpfstoffCharge result = impfstoffChargeService.getChargeOrCreateNewIfFeatureEnabled(null, impfstoff);

        // ASSERT
        assertThat(result)
            .isNull();
    }

    @Test
    void getChargeOrCreateNewIfFeatureEnabled_reactivates_inactive_charge_when_feature_enabled() {
        // ARRANGE
        mockFeatureSetting(FeatureEnabledSetting.ENABLED);
        ImpfstoffCharge inactiveCharge = ImpfstoffChargeBuilder.create(builder -> builder
            .forImpfstoff(impfstoff)
            .withName("CH-100")
            .withAktiv(false)
        );
        when(impfstoffChargeRepo.findByImpfstoffAndName(impfstoff.getId(), "CH-100"))
            .thenReturn(Optional.of(inactiveCharge));

        // ACT
        ImpfstoffCharge result = impfstoffChargeService.getChargeOrCreateNewIfFeatureEnabled("CH-100", impfstoff);

        // ASSERT
        assertThat(result)
            .isNotNull();
        assertThat(result.isAktiv())
            .isTrue();
    }

    @Test
    void getChargeOrCreateNewIfFeatureEnabled_does_not_reactivate_inactive_charge_when_feature_disabled() {
        // ARRANGE
        mockFeatureSetting(FeatureEnabledSetting.DISABLED);
        ImpfstoffCharge inactiveCharge = ImpfstoffChargeBuilder.create(builder -> builder
            .forImpfstoff(impfstoff)
            .withName("CH-100")
            .withAktiv(false)
        );
        when(impfstoffChargeRepo.findByImpfstoffAndName(impfstoff.getId(), "CH-100"))
            .thenReturn(Optional.of(inactiveCharge));

        // ACT
        ImpfstoffCharge result = impfstoffChargeService.getChargeOrCreateNewIfFeatureEnabled("CH-100", impfstoff);

        // ASSERT
        assertThat(result)
            .isNotNull();
        assertThat(result.isAktiv())
            .isFalse();
    }

    @Test
    void getChargeOrCreateNewIfFeatureEnabled_keeps_active_charge_active_when_feature_disabled() {
        // ARRANGE
        mockFeatureSetting(FeatureEnabledSetting.DISABLED);
        ImpfstoffCharge activeCharge = ImpfstoffChargeBuilder.create(builder -> builder
            .forImpfstoff(impfstoff)
            .withName("CH-100")
            .withAktiv(true)
        );
        when(impfstoffChargeRepo.findByImpfstoffAndName(impfstoff.getId(), "CH-100"))
            .thenReturn(Optional.of(activeCharge));

        // ACT
        ImpfstoffCharge result = impfstoffChargeService.getChargeOrCreateNewIfFeatureEnabled("CH-100", impfstoff);

        // ASSERT
        assertThat(result)
            .isNotNull();
        assertThat(result.isAktiv())
            .isTrue();
    }

    @Test
    void getChargeOrCreateNewIfFeatureEnabled_creates_new_charge_when_not_found_and_feature_enabled() {
        // ARRANGE
        mockFeatureSetting(FeatureEnabledSetting.ENABLED);
        when(impfstoffChargeRepo.findByImpfstoffAndName(impfstoff.getId(), "NEW-CH"))
            .thenReturn(Optional.empty());

        // ACT
        ImpfstoffCharge result = impfstoffChargeService.getChargeOrCreateNewIfFeatureEnabled("NEW-CH", impfstoff);

        // ASSERT
        assertThat(result)
            .isNotNull();
        assertThat(result.getName())
            .isEqualTo("NEW-CH");
        assertThat(result.isAktiv())
            .isTrue();
        verify(impfstoffChargeRepo)
            .persist(any(ImpfstoffCharge.class));
    }

    @Test
    void getChargeOrCreateNewIfFeatureEnabled_throws_validation_exception_when_not_found_and_feature_disabled() {
        // ARRANGE
        mockFeatureSetting(FeatureEnabledSetting.DISABLED);
        when(impfstoffChargeRepo.findByImpfstoffAndName(impfstoff.getId(), "NON-EXISTENT"))
            .thenReturn(Optional.empty());

        // ACT & ASSERT
        assertThatThrownBy(() -> impfstoffChargeService.getChargeOrCreateNewIfFeatureEnabled("NON-EXISTENT", impfstoff))
            .isInstanceOf(AppValidationException.class);
        verify(impfstoffChargeRepo, never())
            .persist(any(ImpfstoffCharge.class));
    }
}
