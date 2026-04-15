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
import ch.dvbern.oss.hemed.esc.api.features.organisation.Organisation;
import ch.dvbern.oss.hemed.esc.api.features.organisation.OrganisationRollen;
import ch.dvbern.oss.hemed.esc.api.features.organisation.OrganisationRollenService;
import ch.dvbern.oss.hemed.esc.api.features.organisation.OrganisationService;
import ch.dvbern.oss.hemed.esc.api.shared.abstractentity.AbstractEntity;
import ch.dvbern.oss.hemed.esc.api.shared.auth.Berechtigung;
import ch.dvbern.oss.hemed.esc.api.shared.auth.FachRolle;
import ch.dvbern.oss.hemed.esc.api.shared.auth.RolesAugmentor;
import ch.dvbern.oss.hemed.esc.api.shared.auth.TenantProducer;
import ch.dvbern.oss.hemed.esc.api.shared.benutzer.Benutzer;
import ch.dvbern.oss.hemed.esc.api.shared.benutzer.BenutzerBerechtigung;
import ch.dvbern.oss.hemed.esc.api.shared.benutzer.FachrollenService;
import ch.dvbern.oss.hemed.esc.api.shared.types.EntityAktivStatus;
import ch.dvbern.oss.hemed.esc.api.shared.util.EnumSetUtil;
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
    private final TenantProducer tenantProducer;
    private final OrganisationService organisationService;
    private final OrganisationRollenService organisationRollenService;
    private final FachrollenService fachrollenService;

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
            Collections.emptySet(),
            EntityAktivStatus.AKTIV,
            // Wenn sich der Benutzer einloggen kann, ist er aktiv,
            null,
            null,
            Set.of(),
            null
        );
        benutzerRepo.persist(newBenutzer);

        EnumSet<FachRolle> fachrollen = EnumSetUtil.toEnumSet(jsonWebTokenDataDTO.getFachRollen(), FachRolle.class);

        fachrollenService.setFachrollen(fachrollen, newBenutzer, null);

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
            Collections.emptySet(),
            EntityAktivStatus.AKTIV,  // Neue benutzer sind aktiv
            null,
            null,
            Set.of(),
            null
        );
        benutzerRepo.persist(benutzer);

        Organisation organisation = organisationService.getZentraleOrganisation();
        OrganisationRollen rollen = new OrganisationRollen();
        rollen.setOrganisation(organisation);
        rollen.setBenutzer(benutzer);
        rollen.setFachRollen(EnumSet.noneOf(FachRolle.class));
        organisationRollenService.persist(rollen);
        organisation.addMitglied(rollen);
        benutzer.addOrganisation(rollen);

        return benutzer.getId();
    }

    @NonNull
    public Benutzer persist(@NonNull Benutzer benutzer) {
        benutzerRepo.persist(benutzer);
        return benutzer;
    }

    @NonNull
    public Benutzer persistWithOptimisticLocking(@NonNull Benutzer entity, @NonNull Long versionFromFrontend) {
        return benutzerRepo.persistWithOptimisticLocking(entity, versionFromFrontend);
    }

    @NonNull
    public List<Benutzer> findAllBenutzer() {
        return benutzerRepo.findAll();
    }

    @NonNull
    public List<Benutzer> findAllBenutzerForOrganisation(@NonNull ID<Organisation> organisationId) {
        return benutzerRepo.findAllForOrganisation(organisationId);
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
        Benutzer benutzer = benutzerRepo.referenceById(benutzerID);
        benutzer.disableOptimisticLockingCheck();
        benutzer.getOrganisationRollen().forEach(AbstractEntity::disableOptimisticLockingCheck);

        List<BenutzerBerechtigung> currentBerechtigungen
            = benutzerberechtigungRepo.findBenutzerBerechtigungenByBenutzerId(benutzerID);
        removeLostBerechtigungen(desiredBerechtigungn, currentBerechtigungen);
        addGainedBerechtigungen(benutzer, desiredBerechtigungn, currentBerechtigungen);

        return benutzerberechtigungRepo.findBenutzerBerechtigungenByBenutzerId(benutzerID);
    }

    @NonNull
    public List<Berechtigung> getDesiredBerechtigungen(
        @NonNull EmailAddress benutzerEmail
    ) {
        Optional<Benutzer> benutzerOptional = benutzerRepo.findByEmail(benutzerEmail);
        if (benutzerOptional.isPresent()) {
            Benutzer benutzer = benutzerOptional.get();
            benutzer.disableOptimisticLockingCheck();

            Set<Berechtigung> desiredBerechtigungenFromFachrolle = FachRolle.collectAllBerechtigungen(
                tenantProducer.produceTenant(),
                benutzer.getFachRollen().toArray(RolesAugmentor.EMPTY_FACHROLLEN));
            return desiredBerechtigungenFromFachrolle.stream().toList();
        }
        return Collections.emptyList();
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
    public Optional<Benutzer> findByEmail(@NonNull EmailAddress email) {
        return benutzerRepo.findByEmail(email);
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
        return getBenutzerIfOneOfRolesIsCorrect(benutzerOptionJson, List.of(fachRolle));
    }

    @Nullable
    public Benutzer getBenutzerIfOneOfRolesIsCorrect(
        @Nullable BenutzerOptionJson benutzerOptionJson,
        @NonNull List<FachRolle> fachRollen
    ) {
        if (benutzerOptionJson == null) {
            return null;
        }
        return getBenutzerIfOneOfRolesIsCorrect(benutzerOptionJson.getId(), fachRollen);
    }

    @Nullable
    public Benutzer getBenutzerIfCorrectRole(@Nullable ID<Benutzer> benutzerID, FachRolle fachRolle) {
        if (benutzerID == null) {
            return null;
        }
        return getBenutzerIfOneOfRolesIsCorrect(benutzerID, List.of(fachRolle));
    }

    @Nullable
    public Benutzer getBenutzerIfOneOfRolesIsCorrect(@Nullable ID<Benutzer> benutzerID, List<FachRolle> fachRollen) {
        if (benutzerID == null) {
            return null;
        }
        Benutzer benutzer = getById(benutzerID);
        Set<FachRolle> fachRollenBenutzer = EnumSet.copyOf(benutzer.getFachRollen());
        boolean benutzerHasCorrectRole = fachRollen.stream().anyMatch(fachRollenBenutzer::contains);

        Validate.isTrue(benutzerHasCorrectRole);
        return benutzer;
    }

    public boolean hasCallerBerechtigung(@NonNull Berechtigung role) {
        return securityIdentity.hasRole(role.getBerechtigungName());
    }

    public boolean hasCallerBerechtigungen(@NonNull List<Berechtigung> roles) {
        return roles.stream().allMatch(role -> securityIdentity.hasRole(role.getBerechtigungName()));
    }

    public boolean hasAnyOfCallerBerechtigungen(@NonNull List<Berechtigung> anyOfRoles) {
        return anyOfRoles.stream().anyMatch(role -> securityIdentity.hasRole(role.getBerechtigungName()));
    }
}
