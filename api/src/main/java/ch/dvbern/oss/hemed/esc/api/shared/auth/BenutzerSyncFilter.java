package ch.dvbern.oss.hemed.esc.api.shared.auth;

import java.io.IOException;

import io.quarkus.security.identity.SecurityIdentity;
import jakarta.annotation.Priority;
import jakarta.enterprise.context.RequestScoped;
import jakarta.ws.rs.Priorities;
import jakarta.ws.rs.container.ContainerRequestContext;
import jakarta.ws.rs.container.ContainerRequestFilter;
import jakarta.ws.rs.container.PreMatching;
import jakarta.ws.rs.ext.Provider;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.eclipse.microprofile.jwt.JsonWebToken;

@Provider
@PreMatching
@Priority(Priorities.AUTHENTICATION)
@RequestScoped
@Slf4j
@RequiredArgsConstructor
public class BenutzerSyncFilter implements ContainerRequestFilter {

    private final JsonWebToken jsonWebToken;
    private final SecurityIdentity securityIdentity;


    @Override
    public void filter(@NonNull ContainerRequestContext requestContext) throws IOException {
        String rawToken = jsonWebToken.getRawToken();
        if (rawToken == null) {
            LOG.info("No Token was passed along");
        }
        if (securityIdentity.isAnonymous()) {
            LOG.info("User is anonymous");

        }
        LOG.debug("Current user is: {}", jsonWebToken.getName());
        LOG.debug("Full token {}", rawToken);
    }
}
