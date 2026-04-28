package ch.dvbern.oss.hemed.esc.api.features.benutzer.ensure;

import ch.dvbern.dvbstarter.infrastructure.stereotypes.Service;
import ch.dvbern.dvbstarter.shared.clock.AppClock;
import ch.dvbern.dvbstarter.shared.rest.HttpForwardedForHeaderFilter;
import ch.dvbern.dvbstarter.shared.runas.RunAs;
import ch.dvbern.dvbstarter.shared.types.id.ID;
import ch.dvbern.oss.hemed.esc.api.features.benutzer.BenutzerService;
import ch.dvbern.oss.hemed.esc.api.shared.auth.BerechtigungName;
import ch.dvbern.oss.hemed.esc.api.shared.benutzer.Benutzer;
import ch.dvbern.oss.hemed.esc.api.shared.benutzer.BenutzerLoginInfos;
import ch.dvbern.oss.hemed.esc.api.shared.types.EntityAktivStatus;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.slf4j.MDC;

@Slf4j
@Service
@RequiredArgsConstructor
public class EnsureBenutzerHelperService {

    private final BenutzerService benutzerService;

    private final AppClock appClock;

    @RunAs(username = "Irrelevant solange Rolle SYSTEM_ADMIN", roles = { BerechtigungName.SYSTEM_ADMIN })
    @Transactional(Transactional.TxType.REQUIRES_NEW)
    public void createNewUserFromToken(JsonWebTokenDataDTO webTokenData) {
        Benutzer benutzerFromJWT = benutzerService.createBenutzerFromJWT(webTokenData);
        LOG.info(
            "Created new Benutzer with ID: {} and Benutzername: {}",
            benutzerFromJWT.getId(),
            benutzerFromJWT.getEmail());
        this.storeLastLoginInfoForBenutzer(benutzerFromJWT, webTokenData);
    }

    @RunAs(username = "Irrelevant solange Rolle SYSTEM_ADMIN", roles = { BerechtigungName.SYSTEM_ADMIN })
    public void updateExistingUser(ID<Benutzer> existingUserId, @NonNull JsonWebTokenDataDTO webTokenData) {
        Benutzer existingUser = benutzerService.updateBenutzerFromJWt(existingUserId, webTokenData);
        // LastSessionId speichern
        this.storeLastLoginInfoForBenutzer(existingUser, webTokenData);
    }

    protected void storeLastLoginInfoForBenutzer(
        @NonNull Benutzer benutzer,
        @NonNull JsonWebTokenDataDTO webTokenData
    ) {
        benutzer.disableOptimisticLockingCheck();
        BenutzerLoginInfos loginInfos =
            benutzer.getLoginInfos() == null ? new BenutzerLoginInfos() : benutzer.getLoginInfos();
        benutzer.setLoginInfos(loginInfos);
        String session = webTokenData.getSession();
        if (session != null) {
            if (loginInfos.getLastUsedSessionId() == null || !loginInfos.getLastUsedSessionId().equals(session)) {
                loginInfos.setLastUsedSessionId(session);
                loginInfos.setLastUsedSessionTimestamp(appClock.offsetDateTime().now());
                loginInfos.setOidcIssuer(webTokenData.getIssuer());
                benutzer.setBenutzerAktivStatus(EntityAktivStatus.AKTIV);
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
