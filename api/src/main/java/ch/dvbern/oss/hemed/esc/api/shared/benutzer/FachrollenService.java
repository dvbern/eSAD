package ch.dvbern.oss.hemed.esc.api.shared.benutzer;

import java.util.EnumSet;
import java.util.Optional;

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
    private final FachrollenMailService fachrollenMailService;

    public void setFachrollenFromJWT(
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

    public void handleOrganisationsRollen(
        @NonNull EnumSet<FachRolle> organisationsRollenToSet,
        @NonNull Benutzer benutzer,
        @Nullable Organisation organisation
    ) {
        // Nur Systemrollen ODER neuer Benutzer ohne Rollen -> abbrechen
        if (organisationsRollenToSet.isEmpty() && organisation == null) {
            return;
        }
        // Die zu verwendende Organisation ermitteln
        Organisation resolvedOrganisation = resolveOrganisation(organisationsRollenToSet, benutzer, organisation);
        // Eventuell existierende OrganisationRollen fuer diese Organisation suchen
        Optional<OrganisationRollen> existingRollen = benutzer.getOrganisationRollen().stream()
            .filter(or -> or.getOrganisation().equals(resolvedOrganisation))
            .findFirst();
        // Die zu setzenden Fachrollen dort anhängen bzw. ein neues OrganisationRollen erstellen
        if (existingRollen.isPresent()) {
            existingRollen.get().setFachRollen(organisationsRollenToSet);
            existingRollen.get().disableOptimisticLockingCheck();
        } else {
            OrganisationRollen organisationRollen =
                organisationRollenService.createForOrganisation(resolvedOrganisation, benutzer, organisationsRollenToSet);
            benutzer.addOrganisation(organisationRollen);
            // Mail senden
            fachrollenMailService.sendEinladungOrganisationMail(benutzer, resolvedOrganisation);
        }
    }

    @NonNull
    private Organisation resolveOrganisation(
        @NonNull EnumSet<FachRolle> organisationsRollenToSet,
        @NonNull Benutzer benutzer,
        @Nullable Organisation organisation
    ) {
        if (hasZentraleOrganisation()) {
            return organisationService.getZentraleOrganisation();
        }
        if (organisation != null) {
            return organisation;
        }
        if (organisationsRollenToSet.contains(FachRolle.SCHULAERZTIN)) {
            return organisationService.createOrganisationForBenutzer(benutzer);
        }
        throw new AppFailureException(AppFailureMessage.organisationRolesCannotBeSetWithoutOrganisation());
    }

    public void handleSystemRollen(
        @NonNull EnumSet<FachRolle> systemRollenToSet,
        @NonNull Benutzer benutzer
    ) {
        SystemRollen existingSystemRollen = benutzer.getSystemRollen();
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
