package ch.dvbern.oss.hemed.esc.api.shared.benutzer;

import java.util.EnumSet;
import java.util.Set;

import ch.dvbern.dvbstarter.infrastructure.errorhandling.AppFailureException;
import ch.dvbern.dvbstarter.infrastructure.errorhandling.AppFailureMessage;
import ch.dvbern.dvbstarter.infrastructure.stereotypes.Service;
import ch.dvbern.oss.hemed.esc.api.features.benutzer.SystemRollen;
import ch.dvbern.oss.hemed.esc.api.features.benutzer.SystemRollenService;
import ch.dvbern.oss.hemed.esc.api.features.organisation.Organisation;
import ch.dvbern.oss.hemed.esc.api.features.organisation.OrganisationRollen;
import ch.dvbern.oss.hemed.esc.api.features.organisation.OrganisationRollenService;
import ch.dvbern.oss.hemed.esc.api.features.organisation.OrganisationService;
import ch.dvbern.oss.hemed.esc.api.shared.auth.FachRolle;
import ch.dvbern.oss.hemed.esc.api.shared.config.service.ConfigService;
import ch.dvbern.oss.hemed.esc.api.shared.settings.TenantSettingsService;
import ch.dvbern.oss.hemed.esc.api.shared.util.EnumSetUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;

@Service
@RequiredArgsConstructor
@Slf4j
public class FachrollenService {

    private final OrganisationService organisationService;
    private final OrganisationRollenService organisationRollenService;
    private final SystemRollenService systemRollenService;
    private final ConfigService configService;
    private final TenantSettingsService tenantSettingsService;

    public void setFachrollen(
        @NonNull EnumSet<FachRolle> fachrollen,
        @NonNull Benutzer benutzer,
        @Nullable Organisation organisation
    ) {
        EnumSet<FachRolle> organisationsRollen = EnumSetUtil.toEnumSet(
            fachrollen
                .stream()
                .filter(rolle -> FachRolle.getOrganisationRollen().contains(rolle))
                .toList(), FachRolle.class);

        EnumSet<FachRolle> systemRollen = EnumSetUtil.toEnumSet(
            fachrollen
                .stream()
                .filter(rolle -> FachRolle.getSystemRollen().contains(rolle))
                .toList(), FachRolle.class);

        if (!organisationsRollen.isEmpty() && !systemRollen.isEmpty()) {
            throw new AppFailureException(AppFailureMessage.notPossibleToHaveOrganisationRolesAndSystemRoles());
        }

        handleOrganisationsRollen(organisationsRollen, benutzer, organisation);
        handleSystemRollen(systemRollen, benutzer);
    }

    private void handleOrganisationsRollen(
        @NonNull EnumSet<FachRolle> organisationsRollenToSet,
        @NonNull Benutzer benutzer,
        @Nullable Organisation organisation
    ) {
        Set<OrganisationRollen> organisationsRollenBisher = benutzer.getOrganisationRollen();

        // Bisher leer, neu gesetzt
        if (organisationsRollenBisher.isEmpty() && !organisationsRollenToSet.isEmpty()) {
            if (hasZentraleOrganisation()) {
                Organisation zentraleOrganisation = organisationService.getZentraleOrganisation();
                OrganisationRollen organisationRollen = organisationRollenService.createForOrganisation(
                    zentraleOrganisation,
                    benutzer,
                    organisationsRollenToSet);
                benutzer.addOrganisation(organisationRollen);
            } else if (organisation != null) {
                OrganisationRollen organisationRollen =
                    organisationRollenService.createForOrganisation(organisation, benutzer, organisationsRollenToSet);
                benutzer.addOrganisation(organisationRollen);
            } else if (organisationsRollenToSet.contains(FachRolle.SCHULAERZTIN)) {
                Organisation newOrganisation = organisationService.createOrganisationForBenutzer(benutzer);
                OrganisationRollen organisationRollen =
                    organisationRollenService.createForOrganisation(newOrganisation, benutzer, organisationsRollenToSet);
                benutzer.addOrganisation(organisationRollen);
            } else {
                throw new AppFailureException(AppFailureMessage.organisationRolesCannotBeSetWithoutOrganisation());
            }
        // Bisher bereits vorhanden
        } else if (!organisationsRollenBisher.isEmpty()) {
            // TODO ESC-1180: Sicherstellen, dass es auch mit mehreren Organisationen funktioniert
            OrganisationRollen existingOrganisationRollen = organisationsRollenBisher.iterator().next();
            existingOrganisationRollen.setFachRollen(organisationsRollenToSet);
            existingOrganisationRollen.disableOptimisticLockingCheck();
        }
    }

    private void handleSystemRollen(@NonNull EnumSet<FachRolle> systemRollenToSet, @NonNull Benutzer benutzer) {
        @Nullable SystemRollen existingSystemRollen = benutzer.getSystemRollen();

        if (existingSystemRollen == null) {
            if (systemRollenToSet.isEmpty()) {
                return;
            }
            existingSystemRollen = systemRollenService.createNewSystemRollen(benutzer, systemRollenToSet);
            benutzer.setSystemRollen(existingSystemRollen);
            return;
        }
        // Falls die SystemRollen neu leer sein sollten wird der bisherige Eintrag gelöscht
        if (systemRollenToSet.isEmpty()) {
            benutzer.setSystemRollen(null);
            return;
        }
        // Bisher vorhanden und weiterhin vorhanden  -> überschreiben
        existingSystemRollen.setFachRollen(systemRollenToSet);
    }

    private boolean hasZentraleOrganisation() {
        return tenantSettingsService.getTenantSetting()
            .getTenantSettingFeatures()
            .getZentraleOrganisationFeature()
            .isEnabledForDeploymentEnvironment(configService.getEnvironment());
    }
}
