package ch.dvbern.oss.hemed.esc.api.shared.archivierung;

import ch.dvbern.oss.hemed.esc.api.shared.auth.TenantEnum;
import ch.dvbern.oss.hemed.esc.api.shared.auth.TenantProducer;
import ch.dvbern.oss.hemed.esc.api.shared.config.service.ConfigService;
import ch.dvbern.oss.hemed.esc.api.shared.settings.TenantSettingsService;
import jakarta.enterprise.context.RequestScoped;
import jakarta.enterprise.inject.Produces;
import lombok.RequiredArgsConstructor;
import org.checkerframework.checker.nullness.qual.NonNull;


@RequestScoped
@RequiredArgsConstructor
public class ArchivierungRuleProducer {

    private final TenantProducer tenantProducer;
    private final ConfigService configService;
    private final TenantSettingsService tenantSettingsService;
    private final ArchivierungRuleByAgeService archivierungRuleByAgeService;
    private final ArchivierungRuleNoopService archivierungRuleNoopService;


    @Produces
    @NonNull
    public ArchivierungRuleService archivierungRuleService() {
        boolean archivierungsFeatureEnabled = tenantSettingsService.getTenantSetting()
            .getTenantSettingFeatures()
            .getArchivierungFeature()
            .isEnabledForDeploymentEnvironment(configService.getEnvironment());
        if (archivierungsFeatureEnabled) {
            TenantEnum tenantEnum = tenantProducer.produceTenant();
            return switch (tenantEnum) {
                case BS, WIN -> throw new IllegalStateException("ArchivierungRuleService not defined for tenant: " + tenantEnum + ", but TenantSetting is enabled");
                case BE -> archivierungRuleByAgeService;
                default -> throw new IllegalStateException("Unexpected value: " + tenantEnum);
            };
        }
        return archivierungRuleNoopService;
    }
}
