package ch.dvbern.oss.hemed.esc.api.testutil;

import ch.dvbern.oss.hemed.esc.api.shared.auth.TenantEnum;
import ch.dvbern.oss.hemed.esc.api.shared.auth.TenantProducer;
import io.quarkus.arc.profile.IfBuildProfile;
import io.quarkus.security.identity.CurrentIdentityAssociation;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.ws.rs.container.ContainerRequestContext;
import jakarta.ws.rs.container.ContainerRequestFilter;
import jakarta.ws.rs.container.PreMatching;
import jakarta.ws.rs.ext.Provider;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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
    public void filter(ContainerRequestContext requestContext) {
        if (association.getIdentity().isAnonymous()) {
            return;
        }
        JsonWebToken jsonWebToken = (JsonWebToken) association.getIdentity().getPrincipal();
        String tenantFromToken = jsonWebToken.getClaim("tenant");
        if (tenantFromToken != null) {
            tenantProducer.setCurrentTenant(TenantEnum.valueOf(tenantFromToken));
        } else{
            throw new IllegalStateException("No tenant found in token");
        }
    }
}
