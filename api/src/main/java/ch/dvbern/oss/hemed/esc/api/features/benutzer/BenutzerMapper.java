package ch.dvbern.oss.hemed.esc.api.features.benutzer;

import java.util.EnumSet;
import java.util.List;
import java.util.Objects;
import java.util.function.Consumer;

import ch.dvbern.dvbstarter.infrastructure.errorhandling.AppFailureException;
import ch.dvbern.dvbstarter.infrastructure.errorhandling.AppFailureMessage;
import ch.dvbern.dvbstarter.infrastructure.json.EntityInfoJson;
import ch.dvbern.dvbstarter.infrastructure.stereotypes.JaxMapper;
import ch.dvbern.dvbstarter.shared.clock.AppClock;
import ch.dvbern.dvbstarter.shared.types.id.ID;
import ch.dvbern.oss.hemed.esc.api.features.benutzer.json.BenutzerJson;
import ch.dvbern.oss.hemed.esc.api.features.benutzer.json.BenutzerNameJson;
import ch.dvbern.oss.hemed.esc.api.features.benutzer.json.BenutzerOptionJson;
import ch.dvbern.oss.hemed.esc.api.features.benutzer.json.BenutzerSaveJson;
import ch.dvbern.oss.hemed.esc.api.features.benutzer.json.PrincipalJson;
import ch.dvbern.oss.hemed.esc.api.features.benutzer.json.TestingResponseJson;
import ch.dvbern.oss.hemed.esc.api.features.organisation.Organisation;
import ch.dvbern.oss.hemed.esc.api.features.organisation.OrganisationRollenMapper;
import ch.dvbern.oss.hemed.esc.api.features.organisation.OrganisationService;
import ch.dvbern.oss.hemed.esc.api.features.organisation.json.OrganisationJson;
import ch.dvbern.oss.hemed.esc.api.features.organisation.json.OrganisationRollenJson;
import ch.dvbern.oss.hemed.esc.api.shared.auth.FachRolle;
import ch.dvbern.oss.hemed.esc.api.shared.auth.TenantEnum;
import ch.dvbern.oss.hemed.esc.api.shared.auth.TenantContext;
import ch.dvbern.oss.hemed.esc.api.shared.benutzer.Benutzer;
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
    private final AppClock appClock;
    private final TenantSettingsService tenantSettingsService;
    private final ConfigService configService;
    private final OrganisationRollenMapper organisationRollenMapper;
    private final OrganisationService organisationService;
    private final TenantContext tenantContext;

    @Nullable
    public BenutzerOptionJson toOptionJson(@Nullable Benutzer benutzer) {
        if (benutzer == null) {
            return null;
        }
        return toOptionJson(benutzer, tenantContext.get());
    }

    @Nullable
    public static BenutzerOptionJson toOptionJson(@Nullable Benutzer benutzer, @NonNull TenantEnum tenant) {
        if (benutzer == null) {
            return null;
        }
        return new BenutzerOptionJson(
            benutzer.getId(),
            benutzer.getNachname(),
            benutzer.getVorname(),
            benutzer.getVornameName(),
            OrganisationRollenMapper.toJson(benutzer.getSingleOrganisationRollen(), tenant),
            SystemRollenMapper.toJson(benutzer.getSystemRollen(), tenant),
            benutzer.getBenutzerAktivStatus()
        );
    }

    /**
     * Minimal-Mapping (Id + voller Name) für Tabellen-/Listen-Ansichten.
     */
    @NonNull
    public static BenutzerNameJson toNameJson(@NonNull Benutzer benutzer) {
        return new BenutzerNameJson(
            benutzer.getId(),
            benutzer.getVornameName()
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
            // stored in lowercase like the email Keycloak delivers at login, whatever case the admin typed
            existingBenutzer.setEmail(saveJson.getEmail().toLowerCase());
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

        // TODO ESC-1197: Aktuell wird nur 1 Organisation pro Benutzer unterstützt
        if (isAllowedToUpdateFachRollen(saveJson)) {
            if (saveJson.getOrganisationRollen().size() > 1) {
                throw new AppFailureException(AppFailureMessage.internalError(
                    "Ein Benutzer darf im Moment nicht in mehr als 1 Organisation Mitglied sein."));
            }
            existingBenutzer.disableOptimisticLockingCheck();

            existingBenutzer.setSystemRollen(SystemRollenMapper.toEntity(existingBenutzer, saveJson.getSystemRollen()));
            existingBenutzer.setOrganisationRollen(organisationRollenMapper.toEntitySet(
                existingBenutzer,
                saveJson.getOrganisationRollen(),
                benutzerService.getCurrentBenutzerOrThrowException()));

            // TODO ESC-1197: Aktuell wird nur 1 Organisation pro Benutzer unterstützt
            if (existingBenutzer.getOrganisationRollen().size() > 1) {
                throw new AppFailureException(AppFailureMessage.internalError(
                    "Ein Benutzer darf im Moment nicht in mehr als 1 Organisation Mitglied sein."));
            }
        } else {
            if (haveSystemRollenChanged(saveJson, existingBenutzer)
                || haveOrganisationRollenChanged(saveJson, existingBenutzer)
            ) {
                throw new AppFailureException(AppFailureMessage.updateFachRollenNotAllowed());
            }
        }

        return existingBenutzer;
    }

    private boolean haveSystemRollenChanged(@NonNull BenutzerSaveJson saveJson, @NonNull Benutzer existingBenutzer) {
        EnumSet<FachRolle> systemRollenVorher = existingBenutzer.getSystemRollen() != null ?
            existingBenutzer.getSystemRollen().getFachRollen() :
            EnumSet.noneOf(FachRolle.class);
        EnumSet<FachRolle> systemRollenNachher = saveJson.getSystemRollen() != null ?
            EnumSetUtil.toEnumSet(saveJson.getSystemRollen().getFachRollen(), FachRolle.class) :
            EnumSet.noneOf(FachRolle.class);
        return !systemRollenVorher.equals(systemRollenNachher);
    }

    private boolean haveOrganisationRollenChanged(
        @NonNull BenutzerSaveJson saveJson,
        @NonNull Benutzer existingBenutzer) {
        if (saveJson.getOrganisationRollen().size() != existingBenutzer.getOrganisationRollen().size()) {
            return true;
        }
        for (OrganisationRollenJson jsonRolle : saveJson.getOrganisationRollen()) {
            if (jsonRolle.getOrganisation() == null) {
                return true;
            }
            var existingRolle = existingBenutzer.getOrganisationRollen().stream()
                .filter(r -> r.getOrganisation().getId().equals(jsonRolle.getOrganisation().getId()))
                .findFirst();
            if (existingRolle.isEmpty()) {
                return true;
            }
            EnumSet<FachRolle> jsonFachrollen = EnumSetUtil.toEnumSet(jsonRolle.getFachRollen(), FachRolle.class);
            if (!jsonFachrollen.equals(existingRolle.get().getFachRollen())) {
                return true;
            }
        }
        return false;
    }

    @Nullable
    public BenutzerJson toJson(@Nullable Benutzer benutzer) {
        if (benutzer == null) {
            return null;
        }
        TenantEnum tenant = tenantContext.get();
        return toJson(benutzer, tenant);
    }

    @Nullable
    public static BenutzerJson toJson(@Nullable Benutzer benutzer, @NonNull TenantEnum tenant) {
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
                benutzer.getVornameName(),
                benutzer.getEmail().toString(),
                benutzer.getEmail(),
                benutzer.getTelefon(),
                benutzer.getBenutzerAktivStatus(),
                OrganisationRollenMapper.toJsonList(benutzer.getOrganisationRollen(), tenant),
                benutzer.getSystemRollen() != null ?
                    SystemRollenMapper.toJson(benutzer.getSystemRollen(), tenant) :
                    null,
                benutzer.getVersion()
            ),
            benutzer.getLoginInfos() != null ? benutzer.getLoginInfos().getLastUsedSessionTimestamp() : null,
            benutzer.getTimestampAGBAccepted()
        );
    }

    @NonNull
    public PrincipalJson toPrincipalJson(@NonNull Benutzer benutzer) {
        return new PrincipalJson(
            Objects.requireNonNull(toJson(benutzer))
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
            .map(this::toOptionJson)
            .toList();
    }

    @NonNull
    public List<BenutzerJson> findAllBenutzer() {
        return this.benutzerService.findAllBenutzer().stream()
            .map(this::toJson)
            .toList();
    }

    @NonNull
    public List<BenutzerJson> findAllBenutzerForOrganisation(@NonNull ID<Organisation> organisationId) {
        return this.benutzerService.findAllBenutzerForOrganisation(organisationId).stream()
            .map(this::toJson)
            .toList();
    }

    @NonNull
    public BenutzerJson create(
        @NonNull BenutzerSaveJson benutzerJson,
        @NonNull Consumer<Benutzer> requireAuthorization
    ) {
        Benutzer entity = toEntity(null, benutzerJson);
        requireAuthorization.accept(entity);
        return Objects.requireNonNull(toJson(this.benutzerService.persist(entity)));
    }

    @NonNull
    public BenutzerJson update(
        @NonNull ID<Benutzer> benutzerID,
        @NonNull BenutzerSaveJson benutzerJson,
        @NonNull Consumer<Benutzer> requireAuthorization,
        @NonNull Consumer<Organisation> requireAuthorizationToChangeOrganisation
    ) {
        Benutzer existingBenutzer = this.benutzerService.getById(benutzerID);
        requireAuthorization.accept(existingBenutzer);
        Organisation bisherigeOrganisation = existingBenutzer.getSingleOrganisation();
        // if any new Berechtigung does not belong to bisherigeOrganisation -> requireAuthorizationToChangeOrganisation
        for (OrganisationRollenJson organisationRollenJson : benutzerJson.getOrganisationRollen()) {
            OrganisationJson neueOrganisation = organisationRollenJson.getOrganisation();
            if (neueOrganisation == null) {
                continue;
            }
            if (bisherigeOrganisation == null
                || !neueOrganisation.getId().equals(bisherigeOrganisation.getId())) {
                requireAuthorizationToChangeOrganisation.accept(organisationService.getById(neueOrganisation.getId()));
            }
        }
        Benutzer entity = toEntity(existingBenutzer, benutzerJson);
        return Objects.requireNonNull(toJson(this.benutzerService.persist(entity)));
    }

    @NonNull
    public BenutzerJson updateBenutzerAktivStatus(
        @NonNull ID<Benutzer> benutzerID,
        @NonNull EntityAktivStatus updateBenutzerAktivStatus,
        @NonNull Consumer<Benutzer> requireAuthorization
    ) {
        Benutzer existingBenutzer = this.benutzerService.getById(benutzerID);
        requireAuthorization.accept(existingBenutzer);
        if (isInaktiv(existingBenutzer.getBenutzerAktivStatus()) && isAktiv(updateBenutzerAktivStatus)) {
            existingBenutzer.setTimestampLastManuallyActivated(appClock.offsetDateTime().now());
        }
        existingBenutzer.setBenutzerAktivStatus(updateBenutzerAktivStatus);
        existingBenutzer.disableOptimisticLockingCheck();
        return Objects.requireNonNull(toJson(this.benutzerService.persist(existingBenutzer)));
    }

    @NonNull
    public BenutzerJson acceptAGB() {
        Benutzer benutzer = benutzerService.getCurrentBenutzerOrThrowException();
        if (benutzer.getTimestampAGBAccepted() == null) {
            benutzer.setTimestampAGBAccepted(appClock.offsetDateTime().now());
            benutzer.disableOptimisticLockingCheck();
            benutzer = benutzerService.persist(benutzer);
        }
        return Objects.requireNonNull(toJson(benutzer));
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
        @NonNull BenutzerSaveJson benutzerSaveJson
    ) {
        // Nur erlaubt, wenn Benutzerverwaltung intern ist
        if (!isBenutzerverwaltungIntern()) {
            return false;
        }

        // Systemrollen duerfen nicht zugeteilt werden
        if (benutzerSaveJson.getSystemRollen() != null && !benutzerSaveJson.getSystemRollen()
            .getFachRollen()
            .isEmpty()) {
            return false;
        }
        return true;
    }
}
