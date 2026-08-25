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

import ch.dvbern.dvbstarter.infrastructure.stereotypes.JaxResourcePlain;
import io.quarkus.arc.profile.UnlessBuildProfile;
import io.quarkus.security.AuthenticationException;
import io.quarkus.security.ForbiddenException;
import io.quarkus.security.identity.CurrentIdentityAssociation;
import io.quarkus.security.identity.SecurityIdentity;
import io.smallrye.mutiny.Uni;
import jakarta.ws.rs.InternalServerErrorException;
import jakarta.ws.rs.Priorities;
import jakarta.ws.rs.container.ContainerRequestContext;
import jakarta.ws.rs.ext.Provider;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.eclipse.microprofile.jwt.JsonWebToken;
import org.jboss.resteasy.reactive.server.ServerRequestFilter;

@JaxResourcePlain
@Provider
@RequiredArgsConstructor
@Slf4j
@UnlessBuildProfile("test") // do not run this in test as tests get the tenant from jwt
public class TenantClaimMatchesUrlFilter {

    private final CurrentIdentityAssociation association;

    /**
     * Validates that the bearer token's tenant claim matches the tenant encoded in the request URL.
     * <p>
     * Runs against the deferred {@link SecurityIdentity} so it also works with lazy authentication
     * ({@code quarkus.http.auth.proactive=false}). A bad, expired or malformed token makes
     * {@code getDeferredIdentity()} fail with an {@link AuthenticationException}; that failure is
     * deliberately <em>not</em> caught here - letting it propagate lets Quarkus answer 401 (with a
     * challenge) instead of this filter hardcoding a status. The OIDC layer already logs the
     * verification failure, so the identity-resolution path only adds a short debug trace.
     */
    @ServerRequestFilter(priority = Priorities.AUTHENTICATION)
    public Uni<Void> validateTenantClaim(@NonNull ContainerRequestContext requestContext) {
        return association.getDeferredIdentity()
            .invoke(identity -> checkIdentityForTenant(requestContext, identity))
            .onFailure(AuthenticationException.class::isInstance)
                .invoke(t -> LOG.debug("Bearer token could not be verified for {}; Quarkus will return 401",
                    requestContext.getUriInfo().getPath()))
            .replaceWithVoid();
    }

    void checkIdentityForTenant(
        @NonNull ContainerRequestContext requestContext,
        @NonNull SecurityIdentity currentIdentity
    ) {
        // Skip validation for anonymous requests
        if (currentIdentity.isAnonymous()) {
            LOG.debug("Skipping tenant validation for anonymous request");
            return;
        }

        String path = requestContext.getUriInfo().getPath();
        if (isPublicEndpointWithoutTenantValidation(path)) {
            LOG.debug("Skipping tenant validation for public endpoint '{}'", path);
            return;
        }

        JsonWebToken jsonWebToken = (JsonWebToken) currentIdentity.getPrincipal();
        if (TenantClaimUtil.isHinUser(jsonWebToken)) {
            // HIN Benutzer werden nicht mit Tenant validiert, da sie nie einen Tenant im JWT haben
            return;
        }

        // Extract tenant claim from token
        Optional<String> tenantClaimOpt = TenantClaimUtil.extractTenantClaim(jsonWebToken);

        if (tenantClaimOpt.isEmpty()) {
            LOG.warn("No tenant claim found in JWT token for user {}", currentIdentity.getPrincipal().getName());
            throw new ForbiddenException();
        }

        String tenantClaim = tenantClaimOpt.get();

        if (DVBSupportRoleUtil.isDVBSupport(tenantClaim)) {
            LOG.debug("Skipping tenant / url matching validation for DVB-SUPPORT user '{}'", path);
            return;
        }

        // Get current tenant from url
        URI absolutePath = requestContext.getUriInfo().getAbsolutePath();
        TenantEnum currentTenantFromUrl = TenantExtractorFromUrlRequestFilter.getTenantFromUrl(absolutePath);

        if (currentTenantFromUrl == null) {
            LOG.warn("No current tenant could be determined from url");
            throw new InternalServerErrorException();
        }

        // Map tenant claim to Tenant Enum for comparison
        var tenantFromClaim = TenantEnum.fromString(tenantClaim);
        if (tenantFromClaim == null) {
            LOG.warn("Invalid tenant claim in JWT token: {}", tenantClaim);
            throw new ForbiddenException();
        }

        // Validate that token tenant matches current tenant
        if (currentTenantFromUrl != tenantFromClaim) {
            LOG.warn(
                "Tenant missmatch: token contains tenant '{}'  but current tenant is '{}' for principal {}",
                tenantClaim, currentTenantFromUrl, currentIdentity.getPrincipal().getName());

            throw new ForbiddenException();
        }

        LOG.debug(
            "Tenant validation successful: token tenant '{}' matches current tenant '{}'",
            tenantClaim, currentTenantFromUrl);
    }

    private static boolean isPublicEndpointWithoutTenantValidation(@NonNull String requestedPath) {
        return PermitAllEndpointConstants.PUBLIC_PATHS_WITHOUT_TENANT_VALIDATION.keySet()
            .stream()
            .anyMatch(requestedPath::startsWith);
    }
}
