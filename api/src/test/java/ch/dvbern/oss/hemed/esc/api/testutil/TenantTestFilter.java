package ch.dvbern.oss.hemed.esc.api.testutil;

import ch.dvbern.oss.hemed.esc.api.features.benutzer.ensure.JsonWebTokenClaims;
import ch.dvbern.oss.hemed.esc.api.shared.auth.TenantEnum;
import ch.dvbern.oss.hemed.esc.api.shared.auth.TenantContext;
import io.quarkus.arc.profile.IfBuildProfile;
import io.quarkus.security.AuthenticationException;
import io.quarkus.security.identity.CurrentIdentityAssociation;
import io.quarkus.security.identity.SecurityIdentity;
import io.smallrye.mutiny.Uni;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.ws.rs.Priorities;
import jakarta.ws.rs.container.ContainerRequestContext;
import jakarta.ws.rs.ext.Provider;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.eclipse.microprofile.jwt.JsonWebToken;
import org.jboss.resteasy.reactive.server.ServerRequestFilter;

@Slf4j
@Provider
@ApplicationScoped
@RequiredArgsConstructor
@IfBuildProfile("test") // Only active in test profile
public class TenantTestFilter {

    private final TenantContext tenantContext;
    private final CurrentIdentityAssociation association;

    /**
     * Sets the tenant from the token claim (tests do not run TenantExtractorFromUrlRequestFilter).
     * Mirrors TenantClaimMatchesUrlFilter: a bad token fails getDeferredIdentity() with an
     * AuthenticationException which is deliberately let propagate so Quarkus answers 401.
     */
    @ServerRequestFilter(priority = Priorities.AUTHENTICATION)
    public Uni<Void> setTenantFromToken(@NonNull ContainerRequestContext requestContext) {
        return association.getDeferredIdentity()
            .invoke(this::setTenantForTests)
            .onFailure(AuthenticationException.class::isInstance)
                .invoke(t -> LOG.debug("Bearer token could not be verified for {}; Quarkus will return 401",
                    requestContext.getUriInfo().getPath()))
            .replaceWithVoid();
    }

    private void setTenantForTests(@NonNull SecurityIdentity securityIdentity) {
        if (securityIdentity.isAnonymous()) {
            return;
        }
        JsonWebToken jsonWebToken = (JsonWebToken) securityIdentity.getPrincipal();
        String tenantFromToken = jsonWebToken.getClaim(JsonWebTokenClaims.TENANT_CLAIM);
        if (tenantFromToken != null) {
            tenantContext.setCurrentTenant(TenantEnum.valueOf(tenantFromToken));
        } else {
            throw new IllegalStateException("No tenant found in token");
        }
    }
}
