package ch.dvbern.oss.hemed.esc.api.testutil;

import ch.dvbern.oss.hemed.esc.api.features.benutzer.ensure.JsonWebTokenClaims;
import ch.dvbern.oss.hemed.esc.api.shared.auth.TenantEnum;
import ch.dvbern.oss.hemed.esc.api.shared.auth.TenantProducer;
import io.quarkus.arc.profile.IfBuildProfile;
import io.quarkus.security.identity.CurrentIdentityAssociation;
import io.quarkus.security.identity.SecurityIdentity;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.ws.rs.container.ContainerRequestContext;
import jakarta.ws.rs.container.ContainerRequestFilter;
import jakarta.ws.rs.container.PreMatching;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.Response.Status;
import jakarta.ws.rs.ext.Provider;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.eclipse.microprofile.jwt.JsonWebToken;

@Slf4j
@Provider
@PreMatching
@ApplicationScoped
@RequiredArgsConstructor
@IfBuildProfile("test") // Only active in test profile
public class TenantTestFilter implements ContainerRequestFilter {

    private final TenantProducer tenantProducer;
    private final CurrentIdentityAssociation association;

    @Override
    public void filter(@NonNull ContainerRequestContext requestContext) {
        var currentIdentity = association.getDeferredIdentity();
        currentIdentity.subscribe().with(
            this::setTenantForTests,
            t -> requestContext.abortWith(Response.status(Status.INTERNAL_SERVER_ERROR)
                .build())
        );
    }

    private void setTenantForTests(@NonNull SecurityIdentity securityIdentity) {
        if (securityIdentity.isAnonymous()) {
            return;
        }
        JsonWebToken jsonWebToken = (JsonWebToken) securityIdentity.getPrincipal();
        String tenantFromToken = jsonWebToken.getClaim(JsonWebTokenClaims.TENANT_CLAIM);
        if (tenantFromToken != null) {
            tenantProducer.setCurrentTenant(TenantEnum.valueOf(tenantFromToken));
        } else {
            throw new IllegalStateException("No tenant found in token");
        }
    }
}

