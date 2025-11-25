package ch.dvbern.oss.hemed.esc.api.shared.settings;

import ch.dvbern.dvbstarter.infrastructure.json.EntityInfoJson;
import ch.dvbern.dvbstarter.infrastructure.stereotypes.JaxMapper;
import ch.dvbern.oss.hemed.esc.api.shared.settings.features.TenantSettingsFeatureMapper;
import ch.dvbern.oss.hemed.esc.api.shared.auth.TenantProducer;
import ch.dvbern.oss.hemed.esc.api.shared.util.EnumSetUtil;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import org.checkerframework.checker.nullness.qual.NonNull;

@JaxMapper
@RequiredArgsConstructor
public final class TenantSettingsMapper {

    private final TenantSettingsService tenantSettingsService;

    private final TenantProducer tenantProducer;

    private final TenantSettingsFeatureMapper tenantSettingsFeatureMapper;

    @NonNull
    public TenantSettingsJson toJson(@NonNull TenantSetting settings) {
        return new TenantSettingsJson(
            settings.getId(),
            tenantProducer.produceTenant(),
            EntityInfoJson.fromEntity(settings),
            new TenantSettingSaveJson(
                settings.getDefaultLanguage(),
                settings.getSupportedLanguages(),
                tenantSettingsFeatureMapper.toJson(
                    settings.getTenantSettingFeatures()
                ),
                EnumSetUtil.toList(settings.getEinladungStatusValues())
            )
        );
    }

    public void toEntity(@NonNull TenantSetting existing, @NonNull TenantSettingSaveJson saveJson) {
        existing.setDefaultLanguage(saveJson.getDefaultLanguage());
        existing.setSupportedLanguages(saveJson.getSupportedLanguages());
        existing.setEinladungStatusValues(saveJson.getEinladungStatusValues());
        tenantSettingsFeatureMapper.toEntity(existing.getTenantSettingFeatures(), saveJson.getFeatures());
    }

    @NonNull
    public TenantSettingsJson getTenantSettings() {
        return toJson(tenantSettingsService.getTenantSetting());
    }

    @NonNull
    public TenantSettingsJson update(
        @NotNull @NonNull @Valid TenantSettingSaveJson tenantSettingSaveJson
    ) {
        var existingTenantSetting = tenantSettingsService.getTenantSetting();
        toEntity(existingTenantSetting, tenantSettingSaveJson);
        var savedSettings = tenantSettingsService.persist(existingTenantSetting);
        return toJson(savedSettings);
    }
}
