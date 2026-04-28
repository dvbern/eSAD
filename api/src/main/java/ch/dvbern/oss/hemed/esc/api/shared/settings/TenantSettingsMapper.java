package ch.dvbern.oss.hemed.esc.api.shared.settings;

import java.util.List;

import ch.dvbern.dvbstarter.infrastructure.json.EntityInfoJson;
import ch.dvbern.dvbstarter.infrastructure.stereotypes.JaxMapper;
import ch.dvbern.dvbstarter.shared.types.StringListConverter;
import ch.dvbern.oss.hemed.esc.api.shared.auth.FachRolle;
import ch.dvbern.oss.hemed.esc.api.shared.auth.TenantEnum;
import ch.dvbern.oss.hemed.esc.api.shared.auth.TenantProducer;
import ch.dvbern.oss.hemed.esc.api.shared.settings.features.TenantSettingsFeatureMapper;
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
        TenantEnum tenant = tenantProducer.produceTenant();
        return new TenantSettingsJson(
            settings.getId(),
            tenant,
            EntityInfoJson.fromEntity(settings),
            new TenantSettingSaveJson(
                settings.getDefaultLanguage(),
                settings.getSupportedLanguages(),
                settings.getFolgeTermineMaxSlots(),
                StringListConverter.toString(settings.getCustomerAdminEmails()),
                settings.getSupportEmail(),
                settings.getAgbResponsability(),
                tenantSettingsFeatureMapper.toJson(
                    settings.getTenantSettingFeatures()
                ),
                EnumSetUtil.toList(settings.getEinladungStatusValues()),
                settings.getDefaultLandingPage()
            ),
            FachRolle.getFachrollenWithBerechtigung(tenant)
        );
    }

    public void toEntity(@NonNull TenantSetting existing, @NonNull TenantSettingSaveJson saveJson) {
        existing.setDefaultLanguage(saveJson.getDefaultLanguage());
        existing.setSupportedLanguages(saveJson.getSupportedLanguages());
        existing.setEinladungStatusValues(saveJson.getEinladungStatusValues());
        existing.setFolgeTermineMaxSlots(saveJson.getFolgeTermineMaxSlots());
        List<String> customerAdminEmails = StringListConverter.toList(saveJson.getCustomerAdminEmails());
        existing.setCustomerAdminEmails(customerAdminEmails != null ? customerAdminEmails : List.of());
        existing.setSupportEmail(saveJson.getSupportEmail());
        existing.setAgbResponsability(saveJson.getAgbResponsability());
        existing.setDefaultLandingPage(saveJson.getDefaultLandingPage());
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
