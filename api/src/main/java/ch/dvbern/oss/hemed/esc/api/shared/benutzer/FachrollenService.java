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
import ch.dvbern.oss.hemed.esc.api.shared.auth.Berechtigung;
import ch.dvbern.oss.hemed.esc.api.shared.auth.FachRolle;
import ch.dvbern.oss.hemed.esc.api.shared.auth.TenantEnum;
import ch.dvbern.oss.hemed.esc.api.shared.auth.TenantProducer;
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
    private final TenantProducer tenantProducer;

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

        // Beim Login-Flow wird das Sync direkt aus dem JWT durchgeführt, ohne expliziten "current user".
        // Daher hier die Berechtigungspruefung der Organisations-Rollen ueberspringen.
        handleOrganisationsRollenAndSendInviteMailIfNewUser(organisationsRollen, benutzer, organisation, null, true);
        handleSystemRollen(systemRollen, benutzer);
    }

    public void handleOrganisationsRollenAndSendInviteMailIfNewUser(
        @NonNull EnumSet<FachRolle> organisationsRollenToSet,
        @NonNull Benutzer benutzer,
        @Nullable Organisation organisation,
        @NonNull Benutzer currentBenutzer
    ) {
        handleOrganisationsRollenAndSendInviteMailIfNewUser(
            organisationsRollenToSet,
            benutzer,
            organisation,
            currentBenutzer,
            false);
    }

    private void handleOrganisationsRollenAndSendInviteMailIfNewUser(
        @NonNull EnumSet<FachRolle> organisationsRollenToSet,
        @NonNull Benutzer benutzer,
        @Nullable Organisation organisation,
        @Nullable Benutzer currentBenutzer,
        boolean skipValidation
    ) {
        // Nur Systemrollen ODER neuer Benutzer ohne Rollen -> abbrechen
        if (organisationsRollenToSet.isEmpty() && organisation == null) {
            return;
        }
        // Die zu verwendende Organisation ermitteln
        Organisation resolvedOrganisation = resolveOrganisation(organisationsRollenToSet, benutzer, organisation);
        // Berechtigung des aktuellen Benutzers pruefen, bevor irgendwelche Rollen veraendert werden
        if (!skipValidation && currentBenutzer != null) {
            validateUpdateRollen(currentBenutzer, benutzer, resolvedOrganisation, organisationsRollenToSet);
        }
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
                organisationRollenService.createForOrganisation(
                    resolvedOrganisation,
                    benutzer,
                    organisationsRollenToSet);
            benutzer.addOrganisation(organisationRollen);

            // Mail senden wenn der Benutzer nicht durch den Support berechtigt wurde
            if (!hasSystemRollen(currentBenutzer)) {
                fachrollenMailService.sendEinladungOrganisationMail(benutzer, resolvedOrganisation);
            }
        }
    }

    void validateUpdateRollen(
        @NonNull Benutzer currentBenutzer,
        @NonNull Benutzer targetBenutzer,
        @NonNull Organisation resolvedOrganisation,
        @NonNull EnumSet<FachRolle> organisationsRollenToSet
    ) {
        // Benutzer darf seine eigenen Organisations-Rollen nicht aendern
        if (currentBenutzer.equals(targetBenutzer)) {
            throw new AppFailureException(AppFailureMessage.updateFachRollenNotAllowed());
        }

        // System-Benutzer duerfen alles aendern
        if (hasSystemRollen(currentBenutzer)) {
            return;
        }

        // Aktueller Benutzer muss in derselben Organisation die Berechtigung BENUTZER_BERECHTIGUNGEN_SCHREIBEN haben
        TenantEnum currentTenant = tenantProducer.produceTenant();
        boolean hasRequiredBerechtigung = getFachrollenInOrg(currentBenutzer, resolvedOrganisation)
            .stream()
            .anyMatch(fr -> fr.hasBerechtigung(Berechtigung.BENUTZER_BERECHTIGUNGEN_SCHREIBEN, currentTenant));

        if (!hasRequiredBerechtigung) {
            throw new AppFailureException(AppFailureMessage.updateFachRollenNotAllowed());
        }

        // Nur delegierbare Rollen duerfen hinzugefuegt/entfernt werden
        EnumSet<FachRolle> existingTargetRollen = getFachrollenInOrg(targetBenutzer, resolvedOrganisation);

        // Symmetrische Differenz: Rollen die hinzugefuegt oder entfernt werden
        EnumSet<FachRolle> added = EnumSet.copyOf(organisationsRollenToSet);
        added.removeAll(existingTargetRollen);
        EnumSet<FachRolle> removed = EnumSet.copyOf(existingTargetRollen);
        removed.removeAll(organisationsRollenToSet);

        EnumSet<FachRolle> changedRollen = EnumSet.noneOf(FachRolle.class);
        changedRollen.addAll(added);
        changedRollen.addAll(removed);

        if (changedRollen.stream().anyMatch(fr -> !fr.isDelegierbar())) {
            throw new AppFailureException(AppFailureMessage.updateFachRollenNotAllowed());
        }
    }

    private static EnumSet<FachRolle> getFachrollenInOrg(
        @NonNull Benutzer benutzer,
        @NonNull Organisation organisation
    ) {
        return benutzer.getOrganisationRollen().stream()
            .filter(or -> or.getOrganisation().equals(organisation))
            .findFirst()
            .map(OrganisationRollen::getFachRollen)
            .orElseGet(() -> EnumSet.noneOf(FachRolle.class));
    }

    private static boolean hasSystemRollen(@Nullable Benutzer benutzer) {
        return Optional.ofNullable(benutzer)
            .map(Benutzer::getSystemRollen)
            .map(SystemRollen::getFachRollen)
            .map(fachRollen -> !fachRollen.isEmpty())
            .orElse(false);
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
            LOG.info("Setting system roles for benutzer {} to {}", benutzer.getId(), systemRollenToSet);
            return;
        }
        // Falls die SystemRollen neu leer sein sollten wird der bisherige Eintrag gelöscht
        if (systemRollenToSet.isEmpty()) {
            LOG.info("Setting system roles to null for benutzer {}", benutzer.getId());
            benutzer.setSystemRollen(null);
            return;
        }
        // Bisher vorhanden und weiterhin vorhanden  -> überschreiben
        if (!systemRollenToSet.equals(existingSystemRollen.getFachRollen())) {
            LOG.info(
                "Changing system roles for benutzer {} from {} to {}",
                benutzer.getId(),
                existingSystemRollen.getFachRollen(),
                systemRollenToSet);
            existingSystemRollen.setFachRollen(systemRollenToSet);
        }
    }

    private boolean hasZentraleOrganisation() {
        return tenantSettingsService.getTenantSetting()
            .getTenantSettingFeatures()
            .getZentraleOrganisationFeature()
            .isEnabledForDeploymentEnvironment(configService.getEnvironment());
    }
}
