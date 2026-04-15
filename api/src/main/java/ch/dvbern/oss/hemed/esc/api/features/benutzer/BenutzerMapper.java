package ch.dvbern.oss.hemed.esc.api.features.benutzer;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import java.util.stream.Stream;

import ch.dvbern.dvbstarter.infrastructure.errorhandling.AppFailureException;
import ch.dvbern.dvbstarter.infrastructure.errorhandling.AppFailureMessage;
import ch.dvbern.dvbstarter.infrastructure.json.EntityInfoJson;
import ch.dvbern.dvbstarter.infrastructure.stereotypes.JaxMapper;
import ch.dvbern.dvbstarter.shared.clock.AppClock;
import ch.dvbern.dvbstarter.shared.types.id.ID;
import ch.dvbern.oss.hemed.esc.api.features.benutzer.json.BenutzerJson;
import ch.dvbern.oss.hemed.esc.api.features.benutzer.json.BenutzerOptionJson;
import ch.dvbern.oss.hemed.esc.api.features.benutzer.json.BenutzerSaveJson;
import ch.dvbern.oss.hemed.esc.api.features.benutzer.json.PrincipalJson;
import ch.dvbern.oss.hemed.esc.api.features.benutzer.json.TestingResponseJson;
import ch.dvbern.oss.hemed.esc.api.features.organisation.OrganisationRollenMapper;
import ch.dvbern.oss.hemed.esc.api.features.organisation.Organisation;
import ch.dvbern.oss.hemed.esc.api.features.organisation.OrganisationService;
import ch.dvbern.oss.hemed.esc.api.features.organisation.json.OrganisationRollenJson;
import ch.dvbern.oss.hemed.esc.api.shared.auth.Berechtigung;
import ch.dvbern.oss.hemed.esc.api.shared.auth.FachRolle;
import ch.dvbern.oss.hemed.esc.api.shared.auth.TenantProducer;
import ch.dvbern.oss.hemed.esc.api.shared.benutzer.Benutzer;
import ch.dvbern.oss.hemed.esc.api.shared.benutzer.BenutzerBerechtigung;
import ch.dvbern.oss.hemed.esc.api.shared.benutzer.CurrentUserService;
import ch.dvbern.oss.hemed.esc.api.shared.benutzer.FachrollenService;
import ch.dvbern.oss.hemed.esc.api.shared.config.service.ConfigService;
import ch.dvbern.oss.hemed.esc.api.shared.settings.TenantSettingsService;
import ch.dvbern.oss.hemed.esc.api.shared.types.EntityAktivStatus;
import ch.dvbern.oss.hemed.esc.api.shared.util.EnumSetUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;

import static ch.dvbern.oss.hemed.esc.api.shared.types.EntityAktivStatus.isAktiv;
import static ch.dvbern.oss.hemed.esc.api.shared.types.EntityAktivStatus.isInaktiv;

@Slf4j
@JaxMapper
@RequiredArgsConstructor
public class BenutzerMapper {

    private final BenutzerService benutzerService;
    private final TenantProducer tenantProducer;
    private final AppClock appClock;
    private final TenantSettingsService tenantSettingsService;
    private final ConfigService configService;
    private final CurrentUserService currentUserService;
    private final OrganisationService organisationService;
    private final FachrollenService fachrollenService;

    @Nullable
    public static BenutzerOptionJson toOptionJson(@Nullable Benutzer benutzer) {
        if (benutzer == null) {
            return null;
        }
        return new BenutzerOptionJson(
            benutzer.getId(),
            benutzer.getNachname(),
            benutzer.getVorname(),
            benutzer.getVorname() + ' ' + benutzer.getNachname(),
            OrganisationRollenMapper.toJsonList(benutzer.getOrganisationRollen()),
            SystemRollenMapper.toJson(benutzer.getSystemRollen()),
            benutzer.getBenutzerAktivStatus()
        );
    }

    @Nullable
    public Benutzer toEntity(@Nullable BenutzerOptionJson benutzerOptionJson) {
        if (benutzerOptionJson == null) {
            return null;
        }

        return this.benutzerService.getById(benutzerOptionJson.getId());
    }

    @NonNull
    @SuppressWarnings("java:S2583" /* isAllowedToUpdateFachRollen does not always return true*/)
    private Benutzer toEntity(
        @Nullable Benutzer existingBenutzer,
        @NonNull BenutzerSaveJson saveJson
    ) {
        if (!isBenutzerverwaltungIntern()) {
            throw new AppFailureException(AppFailureMessage.benutzerverwaltungIsNotIntern());
        }

        if (existingBenutzer == null) {
            existingBenutzer = new Benutzer();
            existingBenutzer.setEmail(saveJson.getEmail());
        }
        existingBenutzer.setNachname(saveJson.getName());
        existingBenutzer.setVorname(saveJson.getVorname());
        existingBenutzer.setTitel(saveJson.getTitel());
        existingBenutzer.setTelefon(saveJson.getTelefon());
        // ensure we only set the timestampManuallyActivated if it's being activated
        if (isInaktiv(existingBenutzer.getBenutzerAktivStatus()) && isAktiv(saveJson.getBenutzerAktivStatus())) {
            existingBenutzer.setTimestampLastManuallyActivated(appClock.offsetDateTime().now());
        }
        existingBenutzer.setBenutzerAktivStatus(saveJson.getBenutzerAktivStatus());

        this.benutzerService.persistWithOptimisticLocking(existingBenutzer, saveJson.getVersion());
        existingBenutzer.disableOptimisticLockingCheck();

        if (isAllowedToUpdateFachRollen(existingBenutzer, saveJson)) {
            if (saveJson.getOrganisationRollen().size() > 1) {
                throw new AppFailureException(AppFailureMessage.internalError(
                    "Ein Benutzer darf im Moment nicht in mehr als 1 Organisation Mitglied sein."));
            }

            List<FachRolle> combinedFachrollen = getCombinedFachrollen(saveJson);

            @Nullable Organisation organisation = null;
            if (!saveJson.getOrganisationRollen().isEmpty()
                && saveJson.getOrganisationRollen().getFirst().getOrganisation() != null) {
                organisation = organisationService.getById(saveJson.getOrganisationRollen()
                    .getFirst()
                    .getOrganisation()
                    .getId());
            }

            fachrollenService.setFachrollen(
                EnumSetUtil.toEnumSet(combinedFachrollen, FachRolle.class),
                existingBenutzer,
                organisation);
        } else {
            // if the fachrollen have changed, throw exception
            if (!existingBenutzer.getFachRollen()
                .equals(EnumSetUtil.toEnumSet(getCombinedFachrollen(saveJson), FachRolle.class))) {
                throw new AppFailureException(AppFailureMessage.updateFachRollenNotAllowed());
            }
        }

        return existingBenutzer;
    }

    private List<FachRolle> getCombinedFachrollen(@NonNull BenutzerSaveJson saveJson) {
        List<FachRolle> combinedFachrollen = new ArrayList<>();
        if (saveJson.getSystemRollen() != null) {
            combinedFachrollen.addAll(saveJson.getSystemRollen().getFachRollen());
        }
        saveJson.getOrganisationRollen()
            .stream()
            .map(OrganisationRollenJson::getFachRollen)
            .forEach(combinedFachrollen::addAll);
        return combinedFachrollen;
    }

    @Nullable
    public static BenutzerJson toJson(@Nullable Benutzer benutzer) {
        if (benutzer == null) {
            return null;
        }
        return new BenutzerJson(
            benutzer.getId(),
            EntityInfoJson.fromEntity(benutzer),
            new BenutzerSaveJson(
                benutzer.getTitel(),
                benutzer.getNachname(),
                benutzer.getVorname(),
                benutzer.getVorname() + ' ' + benutzer.getNachname(),
                benutzer.getEmail().toString(),
                benutzer.getEmail(),
                benutzer.getTelefon(),
                benutzer.getBenutzerAktivStatus(),
                OrganisationRollenMapper.toJsonList(benutzer.getOrganisationRollen()),
                benutzer.getSystemRollen() != null ? SystemRollenMapper.toJson(benutzer.getSystemRollen()) : null,
                benutzer.getVersion()
            ),
            benutzer.getLoginInfos() != null ? benutzer.getLoginInfos().getLastUsedSessionTimestamp() : null,
            benutzer.getTimestampAGBAccepted()
        );
    }

    @NonNull
    public static PrincipalJson toPrincipalJson(@NonNull Benutzer benutzer) {
        return new PrincipalJson(
            toJson(benutzer),
            benutzer.getBerechtigungen().stream().map(BenutzerBerechtigung::getBerechtigung).toList()
        );
    }

    @NonNull
    public TestingResponseJson mappingErrorExample() {
        var id = ID.random(Benutzer.class);
        throw new AppFailureException(AppFailureMessage.entityNotFound(id));
    }

    @NonNull
    public List<BenutzerOptionJson> findAllActiveBenutzerOptions() {
        return this.benutzerService.findAllActiveBenutzer().stream()
            .map(BenutzerMapper::toOptionJson)
            .toList();
    }

    @NonNull
    public List<BenutzerJson> findAllBenutzer() {
        return this.benutzerService.findAllBenutzer().stream()
            .map(BenutzerMapper::toJson)
            .toList();
    }

    @NonNull
    public List<BenutzerJson> findAllBenutzerForOrganisation(@NonNull ID<Organisation> organisationId) {
        return this.benutzerService.findAllBenutzerForOrganisation(organisationId).stream()
            .map(BenutzerMapper::toJson)
            .toList();
    }

    @NonNull
    public BenutzerJson create(
        @NonNull BenutzerSaveJson benutzerJson
    ) {
        Benutzer entity = toEntity(null, benutzerJson);
        return toJson(this.benutzerService.persist(entity));
    }

    @NonNull
    public BenutzerJson update(
        @NonNull ID<Benutzer> benutzerID,
        @NonNull BenutzerSaveJson benutzerJson
    ) {
        Benutzer existingBenutzer = this.benutzerService.getById(benutzerID);
        Benutzer entity = toEntity(existingBenutzer, benutzerJson);
        return toJson(this.benutzerService.persist(entity));
    }

    @NonNull
    public BenutzerJson updateBenutzerAktivStatus(
        @NonNull ID<Benutzer> benutzerID,
        EntityAktivStatus updateBenutzerAktivStatus
    ) {
        Benutzer existingBenutzer = this.benutzerService.getById(benutzerID);
        if (isInaktiv(existingBenutzer.getBenutzerAktivStatus()) && isAktiv(updateBenutzerAktivStatus)) {
            existingBenutzer.setTimestampLastManuallyActivated(appClock.offsetDateTime().now());
        }
        existingBenutzer.setBenutzerAktivStatus(updateBenutzerAktivStatus);
        existingBenutzer.disableOptimisticLockingCheck();
        return toJson(this.benutzerService.persist(existingBenutzer));
    }

    @NonNull
    public BenutzerJson acceptAGB() {
        Benutzer benutzer = benutzerService.getCurrentBenutzerOrThrowException();
        if (benutzer.getTimestampAGBAccepted() == null) {
            benutzer.setTimestampAGBAccepted(appClock.offsetDateTime().now());
            benutzer.disableOptimisticLockingCheck();
            benutzer = benutzerService.persist(benutzer);
        }
        return toJson(benutzer);
    }

    private boolean isBenutzerverwaltungIntern() {
        return tenantSettingsService.getTenantSetting()
            .getTenantSettingFeatures()
            .getBenutzerverwaltungInternFeature()
            .isEnabledForDeploymentEnvironment(configService.getEnvironment());
    }

    @SuppressWarnings({
        "java:S1126", /*Allows better readability*/
        "java:S3516" /*Does not always return the same value*/
    })
    private boolean isAllowedToUpdateFachRollen(
        @NonNull Benutzer benutzerToUpdate,
        @NonNull BenutzerSaveJson benutzerSaveJson
    ) {
        Benutzer currentBenutzer = currentUserService.currentUser();
        benutzerToUpdate.disableOptimisticLockingCheck();
        if (!isBenutzerverwaltungIntern()) {
            return false;
        }

        // Roles of currently logged-in user
        EnumSet<FachRolle> userFachRollen = currentBenutzer.getFachRollen();
        // Roles of the user to update
        EnumSet<FachRolle> existingFachRollen = benutzerToUpdate.getFachRollen();

        // Roles of the user after the update
        List<FachRolle> updatedFachRollen = getCombinedFachrollen(benutzerSaveJson);

        List<FachRolle> newRollen = updatedFachRollen.stream()
            .filter(r -> !existingFachRollen.contains(r))
            .toList();

        // Roles to remove from the user
        List<FachRolle> removeRollen = existingFachRollen.stream()
            .filter(r -> !updatedFachRollen.contains(r))
            .toList();
        // Added and removed for easier checks
        List<FachRolle> rollenDiff = Stream.concat(newRollen.stream(), removeRollen.stream()).toList();

        // The User needs the Berechtigung BENUTZER_BERECHTIGUNGEN_SCHREIBEN;
        // TODO ESC-1180: Eigene Berechtigung für Organisationsbenutzer
        boolean hasListenSchreibenBerechtigung = userFachRollen.stream()
            .anyMatch(f -> f.hasBerechtigung(
                Berechtigung.BENUTZER_BERECHTIGUNGEN_SCHREIBEN,
                tenantProducer.produceTenant()));
        boolean hasOrganisationSchreibenBerechtigung = userFachRollen.stream()
            .anyMatch(f -> f.hasBerechtigung(
                Berechtigung.ORGANISATION_VERWALTEN_SCHREIBEN,
                tenantProducer.produceTenant()));

        // System Admins are not Managed through this Endpoint, so we don't allow to update them
        if (!(hasListenSchreibenBerechtigung || hasOrganisationSchreibenBerechtigung)
            || rollenDiff.contains(FachRolle.SYSTEM_ADMIN)
            || rollenDiff.contains(FachRolle.SUPPORT)
            || rollenDiff.contains(FachRolle.ADMIN)
        ) {
            logEditRollenNotAllowed(currentBenutzer, benutzerToUpdate, newRollen, removeRollen);
            return false;
        }
        return true;
    }

    private void logEditRollenNotAllowed(
        @NonNull Benutzer currentBenutzer,
        @NonNull Benutzer benutzerToUpdate,
        @NonNull List<FachRolle> newRollen,
        @NonNull List<FachRolle> removeRollen
    ) {
        LOG.warn(
            "The logged in User {} with roles {} tried to assigning roles {} and removing roles {} to user {} with "
                + "current roles {}. This is not allowed",
            currentBenutzer.getId(),
            currentBenutzer.getFachRollen(),
            newRollen,
            removeRollen,
            benutzerToUpdate.getId(),
            benutzerToUpdate.getFachRollen());
    }
}
