package ch.dvbern.oss.hemed.esc.api.features.benutzer;

import ch.dvbern.dvbstarter.infrastructure.errorhandling.AppFailureException;
import ch.dvbern.dvbstarter.infrastructure.errorhandling.AppFailureMessage;
import ch.dvbern.dvbstarter.infrastructure.json.EntityInfoJson;
import ch.dvbern.dvbstarter.infrastructure.stereotypes.JaxMapper;
import ch.dvbern.dvbstarter.shared.clock.AppClock;
import ch.dvbern.dvbstarter.shared.types.id.ID;
import ch.dvbern.oss.hemed.esc.api.features.benutzer.json.*;
import ch.dvbern.oss.hemed.esc.api.shared.auth.Berechtigung;
import ch.dvbern.oss.hemed.esc.api.shared.auth.FachRolle;
import ch.dvbern.oss.hemed.esc.api.shared.benutzer.Benutzer;
import ch.dvbern.oss.hemed.esc.api.shared.benutzer.BenutzerBerechtigung;
import ch.dvbern.oss.hemed.esc.api.shared.util.EnumSetUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;

import java.util.EnumSet;
import java.util.List;
import java.util.stream.Stream;

@Slf4j
@JaxMapper
@RequiredArgsConstructor
public class BenutzerMapper {

    private final BenutzerService benutzerService;
    private final AppClock appClock;

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
            EnumSetUtil.toList(benutzer.getFachRollen()),
            benutzer.isAktiv()
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
        @NonNull Benutzer currentBenutzer,
        @NonNull Benutzer existingBenutzer,
        @NonNull BenutzerSaveJson saveJson
    ) {
        existingBenutzer.setNachname(saveJson.getName());
        existingBenutzer.setVorname(saveJson.getVorname());
        existingBenutzer.setTitel(saveJson.getTitel());
        existingBenutzer.setTelefon(saveJson.getTelefon());
        // ensure we only set the timestampManuallyActivated if it's being activated
        if (!existingBenutzer.isAktiv() && saveJson.isAktiv()) {
            existingBenutzer.setTimestampLastManuallyActivated(appClock.offsetDateTime().now());
        }
        existingBenutzer.setAktiv(saveJson.isAktiv());

        // Only System-Admin, Support and Admin roles can update roles
        if (isAllowedToUpdateFachRollen(currentBenutzer, existingBenutzer, saveJson)) {
            existingBenutzer.setFachRollen(EnumSetUtil.toEnumSet(saveJson.getFachRollen(), FachRolle.class));
        } else {
            throw new AppFailureException(AppFailureMessage.updateFachRollenNotAllowed());
        }

        return existingBenutzer;
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
                benutzer.isAktiv(),
                EnumSetUtil.toList(benutzer.getFachRollen()),
                benutzer.getVersion()
            )
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
    public BenutzerJson update(
        @NonNull ID<Benutzer> currentBenutzerID,
        @NonNull ID<Benutzer> benutzerID,
        @NonNull BenutzerSaveJson benutzerJson
    ) {
        Benutzer currentBenutzer = this.benutzerService.getById(currentBenutzerID);
        Benutzer existingBenutzer = this.benutzerService.getById(benutzerID);
        Benutzer entity = toEntity(currentBenutzer, existingBenutzer, benutzerJson);
        return toJson(this.benutzerService.persistWithOptimisticLocking(entity, benutzerJson.getVersion()));
    }

    @NonNull
    public BenutzerJson updateBenutzerAktivStatus(
        @NonNull ID<Benutzer> benutzerID,
        boolean updateIsAktiv
    ) {
        Benutzer existingBenutzer = this.benutzerService.getById(benutzerID);
        if (!existingBenutzer.isAktiv() && updateIsAktiv) {
            existingBenutzer.setTimestampLastManuallyActivated(appClock.offsetDateTime().now());
        }
        existingBenutzer.setAktiv(updateIsAktiv);
        existingBenutzer.disableOptimisticLockingCheck();
        return toJson(this.benutzerService.persist(existingBenutzer));
    }

    @SuppressWarnings({
        "java:S1126", /*Allows better readability*/
        "java:S3516" /*Does not always return the same value*/
    })
    private boolean isAllowedToUpdateFachRollen(
        @NonNull Benutzer currentBenutzer,
        @NonNull Benutzer benutzerToUpdate,
        @NonNull BenutzerSaveJson benutzerSaveJson
    ) {
        // Roles of currently logged-in user
        EnumSet<FachRolle> userFachRollen = currentBenutzer.getFachRollen();
        // Roles of the user to update
        EnumSet<FachRolle> existingFachRollen = benutzerToUpdate.getFachRollen();
        // Roles of the user after the update
        List<FachRolle> updatedFachRollen = benutzerSaveJson.getFachRollen();
        // Roles to add to the user
        List<FachRolle> newRollen = updatedFachRollen.stream()
            .filter(r -> !existingFachRollen.contains(r))
            .toList();
        // Roles to remove from the user
        List<FachRolle> removeRollen = existingFachRollen.stream()
            .filter(r -> !updatedFachRollen.contains(r))
            .toList();
        // Added and removed for easier checks
        List<FachRolle> rollenDiff = Stream.concat(newRollen.stream(), removeRollen.stream()).toList();
        // Required roles for update of role
        List<RequiredEditRollen> requiredRollenToEditRolle = List.of(
            new RequiredEditRollen(FachRolle.SUPPORT, List.of(FachRolle.SYSTEM_ADMIN, FachRolle.SUPPORT)),
            new RequiredEditRollen(FachRolle.ADMIN, List.of(FachRolle.SYSTEM_ADMIN, FachRolle.SUPPORT, FachRolle.ADMIN))
        );

        // The System Admin, Support and Admin Role have the LISTEN_SCHREIBEN Berechtigung, without this it's not allowed to update
        boolean hasListenSchreibenBerechtigung = userFachRollen.stream().anyMatch(f -> f.hasBerechtigung(Berechtigung.LISTEN_SCHREIBEN));

        // System Admins are not Managed through this Endpoint, so we don't allow to update them
        if (!hasListenSchreibenBerechtigung || rollenDiff.contains(FachRolle.SYSTEM_ADMIN)) {
            logEditRollenNotAllowed(currentBenutzer, benutzerToUpdate, newRollen, removeRollen);
            return false;
        }
        // Allow if nothing changed
        if (rollenDiff.isEmpty()) {
            return true;
        }
        // Allow to update roles if no SysAdmin/Support/Admin roles are added or removed
        if (rollenDiff.stream().noneMatch(List.of(FachRolle.SYSTEM_ADMIN, FachRolle.ADMIN, FachRolle.SUPPORT)::contains)) {
            return true;
        }

        // First filter out roles, which are not relevant to be checked
        // Then check if the user has the required role for the update
        boolean isAllowed = requiredRollenToEditRolle
            .stream()
            .filter(requiredEditRollen ->
                rollenDiff.contains(requiredEditRollen.roleToUpdate()))
            .allMatch(requiredEditRollen ->
                userFachRollen.stream().anyMatch(requiredEditRollen.requiredRollen()::contains));

        if (isAllowed) {
            return true;
        }

        logEditRollenNotAllowed(currentBenutzer, benutzerToUpdate, newRollen, removeRollen);
        return false;
    }

    private void logEditRollenNotAllowed(
        @NonNull Benutzer currentBenutzer,
        @NonNull Benutzer benutzerToUpdate,
        @NonNull List<FachRolle> newRollen,
        @NonNull List<FachRolle> removeRollen
    ) {
        LOG.warn("The logged in User {} with roles {} tried to assigning roles {} and removing roles {} to user {} with current roles {}. This is not allowed",
            currentBenutzer.getId(),
            currentBenutzer.getFachRollen(),
            newRollen,
            removeRollen,
            benutzerToUpdate.getId(),
            benutzerToUpdate.getFachRollen());
    }
}
