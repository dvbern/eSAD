package ch.dvbern.oss.hemed.esc.api.shared.auth;

import io.quarkus.security.identity.CurrentIdentityAssociation;
import io.quarkus.security.identity.SecurityIdentity;
import io.smallrye.mutiny.Uni;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.ws.rs.Priorities;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.eclipse.microprofile.jwt.JsonWebToken;
import org.jboss.resteasy.reactive.server.ServerRequestFilter;

/**
 * Resolves the {@link SecurityIdentity} before the resource method runs.
 * <p>
 * The filter returns the {@link Uni} instead of subscribing to it itself. That hands the
 * subscription to RESTEasy Reactive, which has two effects: the request is suspended until the
 * identity is available (so downstream code never races against the sync), and an
 * {@code AuthenticationFailedException} propagates to the security layer, which answers with 401
 * plus a challenge header instead of the bare 500 you get when the failure is only logged.
 */
@ApplicationScoped
@Slf4j
@RequiredArgsConstructor
public class BenutzerSyncFilter {

    private final CurrentIdentityAssociation association;

    @ServerRequestFilter(priority = Priorities.AUTHENTICATION)
    @NonNull
    public Uni<Void> syncBenutzer() {
        return association.getDeferredIdentity()
            .onItem().invoke(this::processIdentity)
            .onFailure().invoke(t -> LOG.error("Fehler beim Abrufen der SecurityIdentity", t))
            .replaceWithVoid();
    }

    private void processIdentity(@NonNull SecurityIdentity identity) {
        if (identity.isAnonymous()) {
            LOG.debug("User is anonymous");
            return;
        }

        if (!(identity.getPrincipal() instanceof JsonWebToken jsonWebToken)) {
            LOG.debug("Principal is no JsonWebToken: {}", identity.getPrincipal().getClass().getName());
            return;
        }

        LOG.debug("Current user is: {} (azp: {})", jsonWebToken.getName(), jsonWebToken.getClaim("azp"));
    }
}
