package ch.dvbern.oss.hemed.esc.api.features.benutzer;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.EnumSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

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
import ch.dvbern.oss.hemed.esc.api.shared.auth.Berechtigung;
import ch.dvbern.oss.hemed.esc.api.shared.auth.FachRolle;
import ch.dvbern.oss.hemed.esc.api.shared.auth.RolesAugmentor;
import ch.dvbern.oss.hemed.esc.api.shared.auth.TenantProducer;
import ch.dvbern.oss.hemed.esc.api.shared.benutzer.Benutzer;
import ch.dvbern.oss.hemed.esc.api.shared.benutzer.FachrollenService;
import ch.dvbern.oss.hemed.esc.api.shared.settings.TenantSettingsService;
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
    private final AppClock appClock;
    private final SecurityIdentity securityIdentity;
    private final TenantProducer tenantProducer;
    private final FachrollenService fachrollenService;
    private final TenantSettingsService tenantSettingsService;

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
            EntityAktivStatus.AKTIV,
            // Wenn sich der Benutzer einloggen kann, ist er aktiv,
            null,
            null,
            Set.of(),
            null
        );
        benutzerRepo.persist(newBenutzer);

        boolean ignoreFachrollenFromKeycloak =
            tenantSettingsService.isBenutzerverwaltungIgnoreFachRolleFromKeycloakEnabled();
        if (!ignoreFachrollenFromKeycloak) {
            EnumSet<FachRolle> fachrollen = EnumSetUtil.toEnumSet(jsonWebTokenDataDTO.getFachRollen(), FachRolle.class);
            fachrollenService.setFachrollenFromJWT(fachrollen, newBenutzer, null);
        }

        newBenutzer.disableOptimisticLockingCheck();
        benutzerRepo.persist(newBenutzer);
        return newBenutzer;
    }

    @NonNull
    public Benutzer updateBenutzerFromJWt(
        @NonNull ID<Benutzer> existingUserId,
        @NonNull JsonWebTokenDataDTO webTokenData) {
        Benutzer existingUser = this.getById(existingUserId);

        // Username ist die Email. Falls die KeycloakId sich geändert hat, loggen, aber nicht abbrechen.
        if (existingUser.getKeycloakId() == null || !existingUser.getKeycloakId()
            .equals(webTokenData.getSubjectId().toString())) {
            LOG.warn(
                "KeycloakId for Benutzer {} with ID {} has changed from {} to {}. "
                    + "This should not happen, but we will continue with the update the keycloakid.",
                existingUser.getEmail(),
                existingUser.getId(),
                existingUser.getKeycloakId(),
                webTokenData.getSubjectId()
            );
        }
        existingUser.setKeycloakId(webTokenData.getSubjectId().toString());
        existingUser.setVorname(webTokenData.getGivenName());
        existingUser.setNachname(webTokenData.getFamilyName());
        existingUser.setEmail(webTokenData.getEmail());
        existingUser.setTitel(webTokenData.getTitleClaim());
        existingUser.setTelefon(NormalizedPhoneNumber.parse(webTokenData.getPhoneNumberClaim()));
        // timestampAGBAccepted wird ueber eigenen Endpoint gesetzt

        existingUser.disableOptimisticLockingCheck();

        boolean ignoreFachrollenFromKeycloak =
            tenantSettingsService.isBenutzerverwaltungIgnoreFachRolleFromKeycloakEnabled();
        if (!ignoreFachrollenFromKeycloak) {
            EnumSet<FachRolle> fachrollen = EnumSetUtil.toEnumSet(
                webTokenData.getFachRollen().stream().toList(),
                FachRolle.class);
            fachrollenService.setFachrollenFromJWT(fachrollen, existingUser, null);
        }
        return existingUser;
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
    public List<Berechtigung> getDesiredBerechtigungen(
        @NonNull EmailAddress benutzerEmail
    ) {
        Optional<Benutzer> benutzerOptional = benutzerRepo.findByEmail(benutzerEmail);
        if (benutzerOptional.isPresent()) {
            Benutzer benutzer = benutzerOptional.get();
            benutzer.disableOptimisticLockingCheck();

            // Wir lesen alle Berechtigungen, welche der Benutzer irgendwo hat (unabhängig von
            // der Organisation), damit wir diese in Quarkus setzen können
            Set<Berechtigung> desiredBerechtigungenFromFachrolle = FachRolle.collectAllBerechtigungen(
                tenantProducer.produceTenant(),
                benutzer.getFachRollenCombined().toArray(RolesAugmentor.EMPTY_FACHROLLEN));
            return desiredBerechtigungenFromFachrolle.stream().toList();
        }
        return Collections.emptyList();
    }

    @NonNull
    public Benutzer getById(@NonNull ID<Benutzer> existingUserId) {
        return benutzerRepo.getById(existingUserId);
    }

    @NonNull
    public Benutzer getByIdWithBerechtigungen(@NonNull ID<Benutzer> id) {
        return benutzerRepo.getByIdWithBerechtigungen(id);
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
        Set<FachRolle> fachRollenBenutzer = EnumSet.copyOf(benutzer.getFachRollenCombined());
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
