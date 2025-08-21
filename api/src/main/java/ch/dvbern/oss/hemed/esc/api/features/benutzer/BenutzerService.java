package ch.dvbern.oss.hemed.esc.api.features.benutzer;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.EnumSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import ch.dvbern.dvbstarter.infrastructure.db.DbConst;
import ch.dvbern.dvbstarter.infrastructure.errorhandling.AppAuthException;
import ch.dvbern.dvbstarter.infrastructure.errorhandling.AppValidationException;
import ch.dvbern.dvbstarter.infrastructure.stereotypes.Service;
import ch.dvbern.dvbstarter.shared.clock.AppClock;
import ch.dvbern.dvbstarter.shared.types.emailaddress.EmailAddress;
import ch.dvbern.dvbstarter.shared.types.id.ID;
import ch.dvbern.dvbstarter.shared.types.phonenumber.NormalizedPhoneNumber;
import ch.dvbern.oss.hemed.esc.api.features.benutzer.ensure.JsonWebTokenDataDTO;
import ch.dvbern.oss.hemed.esc.api.features.benutzer.json.BenutzerOptionJson;
import ch.dvbern.oss.hemed.esc.api.shared.abstractentity.AbstractEntity;
import ch.dvbern.oss.hemed.esc.api.shared.auth.Berechtigung;
import ch.dvbern.oss.hemed.esc.api.shared.auth.FachRolle;
import ch.dvbern.oss.hemed.esc.api.shared.benutzer.Benutzer;
import ch.dvbern.oss.hemed.esc.api.shared.benutzer.BenutzerBerechtigung;
import io.quarkus.security.identity.SecurityIdentity;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.Validate;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.eclipse.microprofile.jwt.Claims;
import org.eclipse.microprofile.jwt.JsonWebToken;

import static ch.dvbern.dvbstarter.infrastructure.errorhandling.AppValidationMessage.noUserLoggedIn;

@Service
@RequiredArgsConstructor
@Slf4j
public class BenutzerService {

    private final JsonWebToken jsonWebToken;
    private final BenutzerRepo benutzerRepo;
    private final BenutzerberechtigungRepo benutzerberechtigungRepo;
    private final AppClock appClock;
    private final SecurityIdentity securityIdentity;

    @NonNull
    public Optional<Benutzer> findCurrentBenutzerOptional() {
        String email = jsonWebToken.getClaim(Claims.email);
        if (email == null) {
            throw new AppAuthException();
        }
        return benutzerRepo.findByEmail(EmailAddress.fromString(email));
    }

    @NonNull
    public Benutzer getCurrentBenutzerOrThrowException() {
        return findCurrentBenutzerOptional()
            .orElseThrow(() -> new AppValidationException(noUserLoggedIn()));
    }

    @NonNull
    public Benutzer createBenutzerFromJWT(@NonNull JsonWebTokenDataDTO jsonWebTokenDataDTO) {
        Benutzer newBenutzer = new Benutzer(
            jsonWebTokenDataDTO.getSubjectId().toString(),
            jsonWebTokenDataDTO.getEmail(),
            jsonWebTokenDataDTO.getTitleClaim(),
            jsonWebTokenDataDTO.getFamilyName(),
            jsonWebTokenDataDTO.getGivenName(),
            NormalizedPhoneNumber.parse(jsonWebTokenDataDTO.getPhoneNumberClaim()),
            null,
            Benutzer.mapRollenlistToString(jsonWebTokenDataDTO.getFachRollen()),
            Collections.emptySet(),
            true,  // Wenn sich der Benutzer einloggen kann, ist er aktiv,
            null
        );
        benutzerRepo.persist(newBenutzer);
        return newBenutzer;
    }

    @NonNull
    public ID<Benutzer> createBenutzer(@NonNull CreateBenutzerDTO dto) {
        var benutzer = new Benutzer(
            dto.getKeycloakId(),
            dto.getEmail(),
            dto.getTitel(),
            dto.getNachname(),
            dto.getVorname(),
            dto.getTelefon(),
            null,
            null,
            Collections.emptySet(),
            true,  // Neue benutzer sind aktiv
            null
        );
        benutzerRepo.persist(benutzer);
        return benutzer.getId();
    }

    @NonNull
    public Benutzer persist(@NonNull Benutzer benutzer) {
        benutzerRepo.persist(benutzer);
        return benutzer;
    }

    @NonNull
    public List<Benutzer> findAllBenutzer() {
        return benutzerRepo.findAll();
    }

    @NonNull
    public List<Benutzer> findAllActiveBenutzer() {
        return benutzerRepo.findAllActive();
    }

    @NonNull
    public Optional<Set<Berechtigung>> findBerechtigungenByEmail(@NonNull EmailAddress email) {
        List<Berechtigung> berechtigungenByEmail = benutzerRepo.findBerechtigungenByEmail(email);
        Set<Berechtigung> rolenSet =
            berechtigungenByEmail.isEmpty() ? Collections.emptySet() : EnumSet.copyOf(berechtigungenByEmail);
        return Optional.of(rolenSet);
    }

    @NonNull
    public List<BenutzerBerechtigung> syncBerechtigungenToBenutzer(
        @NonNull ID<Benutzer> benutzerID,
        @NonNull Set<Berechtigung> desiredBerechtigungn
    ) {
        List<BenutzerBerechtigung> currentBerechtigungen
            = benutzerberechtigungRepo.findBenutzerBerechtigungenByBenutzerId(benutzerID);

        removeLostBerechtigungen(desiredBerechtigungn, currentBerechtigungen);
        addGainedBerechtigungen(benutzerRepo.referenceById(benutzerID), desiredBerechtigungn, currentBerechtigungen);

        return benutzerberechtigungRepo.findBenutzerBerechtigungenByBenutzerId(benutzerID);
    }

    private void addGainedBerechtigungen(
        @NonNull Benutzer benutzer,
        @NonNull Set<Berechtigung> desiredBerechtigungn,
        @NonNull List<BenutzerBerechtigung> currentBerechtigungen
    ) {
        var berechtigungenToAdd = desiredBerechtigungn.stream()
            .filter(desiredBerechtigung -> currentBerechtigungen.stream()
                .noneMatch(berechtigung -> berechtigung.getBerechtigung() == desiredBerechtigung))
            .map(berechtigung -> new BenutzerBerechtigung(benutzer, berechtigung))
            .toList();
        LOG.debug(
            "Adding Berechtigungen: {}",
            berechtigungenToAdd.stream()
                .map(b -> b.getBerechtigung().name())
                .collect(Collectors.joining(", ")));
        berechtigungenToAdd.forEach(benutzerberechtigungRepo::persist);
    }

    private void removeLostBerechtigungen(
        @NonNull Set<Berechtigung> desiredBerechtigungn,
        @NonNull List<BenutzerBerechtigung> currentBerechtigungen
    ) {
        var berechtigungenToRemove = currentBerechtigungen.stream()
            .filter(benutzerBerechtigung -> !desiredBerechtigungn.contains(benutzerBerechtigung.getBerechtigung()))
            .toList();
        LOG.debug(
            "Removing Berechtigungen: {}",
            berechtigungenToRemove.stream()
                .map(b -> b.getBerechtigung().name())
                .collect(Collectors.joining(", ")));
        berechtigungenToRemove.stream().map(AbstractEntity::getId).forEach(benutzerberechtigungRepo::remove);
    }

    @NonNull
    public Benutzer getById(@NonNull ID<Benutzer> existingUserId) {
        return benutzerRepo.getById(existingUserId);
    }

    @NonNull
    public List<ID<Benutzer>> getBenutzerToDeactivate(
        int minutesInactive,
        @Nullable Integer limit
    ) {
        LocalDateTime cutoffDateTimeForOldSessions = appClock.localDateTime().now().minusMinutes(minutesInactive);
        LocalDateTime installationDate = DbConst.EARLIEST_COLLECTED_LOGIN_TIMESTAMP;
        if (cutoffDateTimeForOldSessions.isBefore(installationDate)) {
            LOG.warn(
                "ESC-DEACTIVATE-USERS: ESC has no information about login times prior to the {} and thus can "
                    + "not accurately report which users have not logged in since {}. Returning empty List",
                installationDate,
                cutoffDateTimeForOldSessions);
            return Collections.emptyList();
        }
        return benutzerRepo.getBenutzerToDeactivate(
            minutesInactive,
            limit);
    }

    public long deactivateListOfBenutzer(@NonNull List<ID<Benutzer>> benutzerToDeactivate) {
        return benutzerRepo.deactivateListOfBenutzer(benutzerToDeactivate);
    }

    @Nullable
    public Benutzer getBenutzerIfCorrectRole(@Nullable BenutzerOptionJson benutzerOptionJson, FachRolle fachRolle) {
        if (benutzerOptionJson == null) {
            return null;
        }
        Benutzer benutzer = getById(benutzerOptionJson.getId());
        boolean correctRole = benutzer.getFachRollen().contains(fachRolle);
        Validate.isTrue(correctRole);
        return benutzer;
    }

    public boolean hasCallerBerechtigung(@NonNull Berechtigung role) {
        return securityIdentity.hasRole(role.getBerechtigungName());
    }
}
