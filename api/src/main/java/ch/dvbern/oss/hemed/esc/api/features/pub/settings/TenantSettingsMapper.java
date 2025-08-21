package ch.dvbern.oss.hemed.esc.api.features.pub.settings;

import ch.dvbern.dvbstarter.infrastructure.stereotypes.JaxMapper;
import lombok.RequiredArgsConstructor;
import org.checkerframework.checker.nullness.qual.NonNull;

@JaxMapper
@RequiredArgsConstructor
public final class TenantSettingsMapper {

    private final TenantSettingsService tenantSettingsService;

    @NonNull
    public static TenantSettingsJson toJson(@NonNull TenantSetting settings) {
        return new TenantSettingsJson(
            settings.getId(),
            settings.getDefaultLanguage(),
            settings.getSupportedLanguages()
        );
    }

    @NonNull
    public TenantSettingsJson getTenantSettings() {
        return toJson(tenantSettingsService.getTenantSetting());
    }

}
