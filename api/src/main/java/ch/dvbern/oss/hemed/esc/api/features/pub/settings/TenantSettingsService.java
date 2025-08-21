 package ch.dvbern.oss.hemed.esc.api.features.pub.settings;

 import ch.dvbern.dvbstarter.infrastructure.stereotypes.Service;
 import lombok.RequiredArgsConstructor;
 import org.checkerframework.checker.nullness.qual.NonNull;

@Service
@RequiredArgsConstructor
public class TenantSettingsService {

    private final TenantSettingsRepo tenantSettingsRepo;

    @NonNull
    public TenantSetting persist(@NonNull TenantSetting tenantSettings) {
        tenantSettingsRepo.persist(tenantSettings);
        return tenantSettings;
    }

    @NonNull
    public TenantSetting getTenantSetting() {
        return tenantSettingsRepo.getTenantSetting();
    }

}
