package ch.dvbern.oss.hemed.esc.api.features.benutzer.ensure;

import ch.dvbern.dvbstarter.shared.types.phonenumber.NormalizedPhoneNumber;
import java.util.List;
import java.util.stream.Collectors;

import ch.dvbern.dvbstarter.infrastructure.stereotypes.Service;
import ch.dvbern.dvbstarter.shared.clock.AppClock;
import ch.dvbern.dvbstarter.shared.rest.HttpForwardedForHeaderFilter;
import ch.dvbern.dvbstarter.shared.runas.RunAs;
import ch.dvbern.dvbstarter.shared.types.id.ID;
import ch.dvbern.oss.hemed.esc.api.features.benutzer.BenutzerService;
import ch.dvbern.oss.hemed.esc.api.shared.auth.Berechtigung;
import ch.dvbern.oss.hemed.esc.api.shared.auth.BerechtigungName;
import ch.dvbern.oss.hemed.esc.api.shared.auth.FachRolle;
import ch.dvbern.oss.hemed.esc.api.shared.benutzer.Benutzer;
import ch.dvbern.oss.hemed.esc.api.shared.benutzer.BenutzerBerechtigung;
import ch.dvbern.oss.hemed.esc.api.shared.benutzer.BenutzerLoginInfos;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.slf4j.MDC;

@Slf4j
@Service
@RequiredArgsConstructor
public class EnsureBenutzerHelperService {

    public static final FachRolle[] EMPTY_FACHROLLEN = new FachRolle[0];
    private final BenutzerService benutzerService;

    private final AppClock appClock;


    @RunAs(username = "dev-esc@dvbern.ch", roles = { BerechtigungName.SYSTEM_ADMIN })
    @Transactional(Transactional.TxType.REQUIRES_NEW)
    public void createNewUserFromToken(JsonWebTokenDataDTO webTokenData) {

        Benutzer benutzerFromJWT = benutzerService.createBenutzerFromJWT(webTokenData);
        List<BenutzerBerechtigung> storedBerechtigungen = benutzerService.syncBerechtigungenToBenutzer(
            benutzerFromJWT.getId(),
            FachRolle.collectAllBerechtigungen(benutzerFromJWT.getFachRollen().toArray(EMPTY_FACHROLLEN))
        );

        LOG.info(
            "Created new Benutzer with ID: {} and Benutzername: {} The new Benutzer has the following Berechtigungen:"
                + " {}",
            benutzerFromJWT.getId(),
            benutzerFromJWT.getEmail(),
            storedBerechtigungen.stream()
                .map(BenutzerBerechtigung::getBerechtigung)
                .map(Berechtigung::name)
                .collect(Collectors.joining(", ")
                ));
        this.storeLastLoginInfoForBenutzer(benutzerFromJWT, webTokenData);
    }


    @RunAs(username = "dev-esc@dvbern.ch", roles = { BerechtigungName.SYSTEM_ADMIN })
    public void updateExistingUser(ID<Benutzer> existingUserId, @NonNull JsonWebTokenDataDTO webTokenData) {
        Benutzer existingUser =  benutzerService.getById(existingUserId);

        // Username ist die Email. Falls die KeycloakId sich geändert hat, loggen, aber nicht abbrechen.
        if (existingUser.getKeycloakId() == null || !existingUser.getKeycloakId().equals(webTokenData.getSubjectId().toString())) {
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
        existingUser.setFachRollen(webTokenData.getFachRollen().stream().toList());
        existingUser.setTitel(webTokenData.getTitleClaim());
        existingUser.setTelefon(NormalizedPhoneNumber.parse(webTokenData.getPhoneNumberClaim()));

        benutzerService.syncBerechtigungenToBenutzer(
            existingUser.getId(),
            FachRolle.collectAllBerechtigungen(existingUser.getFachRollen().toArray(EMPTY_FACHROLLEN)));

        // LastSessionId speichern
        this.storeLastLoginInfoForBenutzer(existingUser, webTokenData);
    }

    protected void storeLastLoginInfoForBenutzer(
        @NonNull Benutzer benutzer,
        @NonNull JsonWebTokenDataDTO webTokenData
    ) {

        BenutzerLoginInfos loginInfos = benutzer.getLoginInfos() == null ? new BenutzerLoginInfos() : benutzer.getLoginInfos();
        benutzer.setLoginInfos(loginInfos);
        String session = webTokenData.getSession();
        if (session != null) {
            if (loginInfos.getLastUsedSessionId() == null || !loginInfos.getLastUsedSessionId().equals(session)) {
                loginInfos.setLastUsedSessionId(session);
                loginInfos.setLastUsedSessionTimestamp(appClock.offsetDateTime().now());
                loginInfos.setOidcIssuer(webTokenData.getIssuer());
                benutzer.setAktiv(true);
            } else {
                LOG.debug("Session {} for user {} was already stored", benutzer.getId(), session);
            }
            String forwaredForHeaderIps = MDC.get(HttpForwardedForHeaderFilter.LOG_MDC_FORWARDED_FOR);
            if (loginInfos.getLastUsedIp() == null
                || (forwaredForHeaderIps != null && !loginInfos.getLastUsedIp().equals(forwaredForHeaderIps))) {
                loginInfos.setLastUsedIp(forwaredForHeaderIps);
            }
        } else {
            LOG.debug("Session was null");
        }
    }
}
