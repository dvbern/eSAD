 package ch.dvbern.oss.hemed.esc.api.shared.settings;

 import ch.dvbern.dvbstarter.infrastructure.stereotypes.Service;
 import ch.dvbern.oss.hemed.esc.api.shared.config.service.ConfigService;
 import lombok.RequiredArgsConstructor;
 import org.checkerframework.checker.nullness.qual.NonNull;

 @Service
 @RequiredArgsConstructor
 public class TenantSettingsService {

     private final TenantSettingsRepo tenantSettingsRepo;
     private final ConfigService configService;

     @NonNull
     public TenantSetting persist(@NonNull TenantSetting tenantSettings) {
         tenantSettingsRepo.persist(tenantSettings);
         return tenantSettings;
     }

     @NonNull
     public TenantSetting getTenantSetting() {
         return tenantSettingsRepo.getTenantSetting();
     }

     public boolean isBenutzerverwaltungIgnoreFachRolleFromKeycloakEnabled() {
         return getTenantSetting()
             .getTenantSettingFeatures()
             .getBenutzerverwaltungIgnoreFachRolleFromKeycloakFeature()
             .isEnabledForDeploymentEnvironment(configService.getEnvironment());
     }
 }
