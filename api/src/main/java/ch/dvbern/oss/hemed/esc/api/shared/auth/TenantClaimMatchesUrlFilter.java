/*
 *
 * Copyright (C) 2025 DV Bern AG, Switzerland
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package ch.dvbern.oss.hemed.esc.api.shared.auth;

import java.net.URI;
import java.util.Optional;

import io.quarkus.arc.profile.IfBuildProfile;
import io.quarkus.security.identity.CurrentIdentityAssociation;
import jakarta.annotation.Priority;
import jakarta.enterprise.context.RequestScoped;
import jakarta.ws.rs.Priorities;
import jakarta.ws.rs.container.ContainerRequestContext;
import jakarta.ws.rs.container.ContainerRequestFilter;
import jakarta.ws.rs.container.PreMatching;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.Provider;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.microprofile.jwt.JsonWebToken;



@Provider
@PreMatching
@RequestScoped
@Priority(Priorities.AUTHENTICATION)
@RequiredArgsConstructor
@Slf4j
@IfBuildProfile("prod") // run this in real environments only
public class TenantClaimMatchesUrlFilter implements ContainerRequestFilter {

    private final CurrentIdentityAssociation association;


    @Override
    public void filter(ContainerRequestContext requestContext) {
        // Skip validation for anonymous requests
        if (association.getIdentity().isAnonymous()) {
            LOG.debug("Skipping tenant validation for anonymous request");
            return;
        }

        JsonWebToken jsonWebToken = (JsonWebToken) association.getIdentity().getPrincipal();

        // Extract tenant claim from token
        Optional<String> tenantClaimOpt = TenantClaimUtil.extractTenantClaim(jsonWebToken);

        if (tenantClaimOpt.isEmpty()) {
            LOG.warn("No tenant claim found in JWT token for user {}", association.getIdentity().getPrincipal().getName());
            requestContext.abortWith(Response.status(Response.Status.FORBIDDEN)
                .build());
            return;
        }

        String tenantClaim = tenantClaimOpt.get();

        // Get current tenant from url

        URI absolutePath = requestContext.getUriInfo().getAbsolutePath();
        TenantEnum currentTenantFromUrl = TenantExtractorFromUrlRequestFilter.getTenantFromUrl(absolutePath);

        if (currentTenantFromUrl == null) {
            LOG.warn("No current tenant could be determined from url");
            requestContext.abortWith(Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                .entity("Unable to determine current tenant")
                .build());
            return;
        }

        // Map tenant claim to Tenant Enum for comparison
        var tenantFromClaim = TenantEnum.fromString(tenantClaim);
        if(tenantFromClaim == null) {
            LOG.warn("Invalid tenant claim in JWT token: {}", tenantClaim);
            requestContext.abortWith(Response.status(Response.Status.FORBIDDEN)
                .build());
            return;
        }

        // Validate that token tenant matches current tenant
        if (currentTenantFromUrl != tenantFromClaim) {
            LOG.warn("Tenant mismatch: token contains tenant '{}'  but current tenant is '{}' for principal {}",
                tenantClaim, currentTenantFromUrl, association.getIdentity().getPrincipal().getName());

            requestContext.abortWith(Response.status(Response.Status.FORBIDDEN)
                .build());
            return;
        }

        LOG.debug("Tenant validation successful: token tenant '{}' matches current tenant '{}'",
            tenantClaim, currentTenantFromUrl);
    }
}

