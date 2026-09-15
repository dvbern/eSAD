/*
 *
 * Copyright (C) 2025 DV Bern AG, Switzerland
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package ch.dvbern.oss.hemed.esc.api.shared.settings.features;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;

import ch.dvbern.dvbstarter.infrastructure.stereotypes.JaxMapper;
import ch.dvbern.oss.hemed.esc.api.shared.config.service.ConfigService;

import lombok.RequiredArgsConstructor;
import org.checkerframework.checker.nullness.qual.NonNull;

@JaxMapper
@RequiredArgsConstructor
public class TenantSettingsFeatureMapper {

    private final ConfigService configService;

    @NonNull
    public TenantSettingFeaturesJson toJson(@NonNull TenantSettingFeatures featureSettings) {
        return new TenantSettingFeaturesJson(
            toTenantSettingFeatureState(featureSettings.getEsadIdFeature()),
            toTenantSettingFeatureState(featureSettings.getLaenderCodeFeature()),
            toTenantSettingFeatureState(featureSettings.getMobileScannerFeature()),
            toTenantSettingFeatureState(featureSettings.getMassenimportFeature()),
            toTenantSettingFeatureState(featureSettings.getMassenbriefFeature()),
            toTenantSettingFeatureState(featureSettings.getNichtGeprueftGrundFeature()),
            toTenantSettingFeatureState(featureSettings.getArztListeFeature()),
            toTenantSettingFeatureState(featureSettings.getAdHocImpfstoffChargenFeature()),
            toTenantSettingFeatureState(featureSettings.getFooterFeature()),
            toTenantSettingFeatureState(featureSettings.getBenutzerverwaltungInternFeature()),
            toTenantSettingFeatureState(featureSettings.getBenutzerverwaltungIgnoreFachRolleFromKeycloakFeature()),
            toTenantSettingFeatureState(featureSettings.getArchivierungFeature()),
            toTenantSettingFeatureState(featureSettings.getZentraleOrganisationFeature()),
            toTenantSettingFeatureState(featureSettings.getAnonymeStatistikFeature()),
            toTenantSettingFeatureState(featureSettings.getNachkontrolleFeature()),
            toTenantSettingFeatureState(featureSettings.getAnlassSpracheSpalteFeature()),
            toTenantSettingFeatureState(featureSettings.getAnlassSpracheZusammenfassungFeature()),
            toTenantSettingFeatureState(featureSettings.getAnlassInformationenFeature()),
            toTenantSettingFeatureState(featureSettings.getAnlassTitleRequiredFeature()),
            toTenantSettingFeatureState(featureSettings.getTenantHeaderInExportsFeature())
        );
    }

    /**
     * this calculates the State to transfer to the client for a specific feature, to make it easier
     * for the client we already calculated if the feature should be enabled for this stage or not and store that
     * info in the TenantSettingFeatureStateJson#enabledForState field.*
     */
    private TenantSettingFeatureStateJson toTenantSettingFeatureState(@NonNull FeatureEnabledSetting featureSetting) {
        return new TenantSettingFeatureStateJson(
            featureSetting,
            featureSetting.isEnabledForDeploymentEnvironment(configService.getEnvironment())
        );
    }

    public void toEntity(
        @Valid @NotNull @NonNull TenantSettingFeatures existingFeatures,
        @NotNull @NonNull @Valid TenantSettingFeaturesJson features
    ) {
        existingFeatures.setEsadIdFeature(features.getEsadIdFeature().getFeatureEnabled());
        existingFeatures.setLaenderCodeFeature(features.getLaenderCodeFeature().getFeatureEnabled());
        existingFeatures.setMobileScannerFeature(features.getMobileScannerFeature().getFeatureEnabled());
        existingFeatures.setMassenimportFeature(features.getMassenimportFeature().getFeatureEnabled());
        existingFeatures.setMassenbriefFeature(features.getMassenbriefFeature().getFeatureEnabled());
        existingFeatures.setNichtGeprueftGrundFeature(features.getNichtGeprueftGrundFeature().getFeatureEnabled());
        existingFeatures.setArztListeFeature(features.getArztListeFeature().getFeatureEnabled());
        existingFeatures.setAdHocImpfstoffChargenFeature(features.getAdHocImpfstoffChargenFeature()
            .getFeatureEnabled());
        existingFeatures.setFooterFeature(features.getFooterFeature().getFeatureEnabled());
        existingFeatures.setBenutzerverwaltungInternFeature(features.getBenutzerverwaltungInternFeature()
            .getFeatureEnabled());
        existingFeatures.setBenutzerverwaltungIgnoreFachRolleFromKeycloakFeature(features.getBenutzerverwaltungIgnoreFachRolleFromKeycloakFeature()
            .getFeatureEnabled());
        existingFeatures.setArchivierungFeature(features.getArchivierungFeature().getFeatureEnabled());
        existingFeatures.setZentraleOrganisationFeature(features.getZentraleOrganisationFeature().getFeatureEnabled());
        existingFeatures.setAnonymeStatistikFeature(features.getAnonymeStatistikFeature().getFeatureEnabled());
        existingFeatures.setNachkontrolleFeature(features.getNachkontrolleFeature().getFeatureEnabled());
        existingFeatures.setAnlassSpracheSpalteFeature(features.getAnlassSpracheSpalteFeature().getFeatureEnabled());
        existingFeatures.setAnlassSpracheZusammenfassungFeature(features.getAnlassSpracheZusammenfassungFeature()
            .getFeatureEnabled());
        existingFeatures.setAnlassInformationenFeature(features.getAnlassInformationenFeature().getFeatureEnabled());
        existingFeatures.setAnlassTitleRequiredFeature(features.getAnlassTitleRequiredFeature().getFeatureEnabled());
        existingFeatures.setTenantHeaderInExportsFeature(features.getTenantHeaderInExportsFeature()
            .getFeatureEnabled());
    }
}
