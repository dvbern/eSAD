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

import ch.dvbern.oss.hemed.esc.api.features.benutzer.ensure.JsonWebTokenClaims;
import io.quarkus.security.identity.CurrentIdentityAssociation;
import io.quarkus.security.identity.SecurityIdentity;
import jakarta.ws.rs.container.ContainerRequestContext;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.UriInfo;
import org.eclipse.microprofile.jwt.JsonWebToken;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class TenantClaimMatchesUrlFilterTest {

    private CurrentIdentityAssociation currentIdentityAssociation;
    private ContainerRequestContext requestContext;
    private SecurityIdentity securityIdentity;
    private JsonWebToken jsonWebToken;
    private UriInfo uriInfo;
    private TenantClaimMatchesUrlFilter filter;

    @BeforeEach
    void setUp() {
        currentIdentityAssociation = mock(CurrentIdentityAssociation.class);
        requestContext = mock(ContainerRequestContext.class);
        securityIdentity = mock(SecurityIdentity.class);
        jsonWebToken = mock(JsonWebToken.class);
        uriInfo = mock(UriInfo.class);

        when(currentIdentityAssociation.getIdentity()).thenReturn(securityIdentity);
        when(requestContext.getUriInfo()).thenReturn(uriInfo);
        filter = new TenantClaimMatchesUrlFilter(currentIdentityAssociation);

    }

    @Test
    void should_skip_validation_for_anonymous_requests() {
        // ARRANGE
        when(securityIdentity.isAnonymous()).thenReturn(true);

        // ACT
        filter.filter(requestContext);

        // ASSERT
        verify(requestContext, never()).abortWith(any());
    }

    @Test
    void should_abort_with_forbidden_when_no_tenant_claim_found() {
        // ARRANGE
        when(securityIdentity.isAnonymous()).thenReturn(false);
        when(securityIdentity.getPrincipal()).thenReturn(jsonWebToken);
        when(jsonWebToken.getName()).thenReturn("testuser");

        when(jsonWebToken.claim(JsonWebTokenClaims.TENANT_CLAIM)).thenReturn(Optional.empty());

        // ACT
        filter.filter(requestContext);

        // ASSERT
        verify(requestContext).abortWith(argThat(response ->
            response.getStatus() == Response.Status.FORBIDDEN.getStatusCode()));
    }

    @Test
    void should_abort_with_internal_server_error_when_cannot_determine_tenant_from_url() {
        // ARRANGE
        URI absolutePath = URI.create("https://invalid-url.com/test");

        when(securityIdentity.isAnonymous()).thenReturn(false);
        when(securityIdentity.getPrincipal()).thenReturn(jsonWebToken);
        when(uriInfo.getAbsolutePath()).thenReturn(absolutePath);
        when(jsonWebToken.claim(JsonWebTokenClaims.TENANT_CLAIM)).thenReturn(Optional.of("bs"));

        // ACT
        filter.filter(requestContext);

        // ASSERT
        verify(requestContext).abortWith(argThat(response ->
            response.getStatus() == Response.Status.INTERNAL_SERVER_ERROR.getStatusCode()));
    }

    @Test
    void should_abort_with_forbidden_when_invalid_tenant_claim() {
        // ARRANGE
        URI absolutePath = URI.create("https://local-esc-bs.localhost:4443/test");

        when(securityIdentity.isAnonymous()).thenReturn(false);
        when(securityIdentity.getPrincipal()).thenReturn(jsonWebToken);
        when(uriInfo.getAbsolutePath()).thenReturn(absolutePath);
        when(jsonWebToken.claim(JsonWebTokenClaims.TENANT_CLAIM)).thenReturn(Optional.of("invalid-tenant"));

        // ACT
        filter.filter(requestContext);

        // ASSERT
        verify(requestContext).abortWith(argThat(response ->
            response.getStatus() == Response.Status.FORBIDDEN.getStatusCode()));
    }

    @Test
    void should_abort_with_forbidden_when_tenant_mismatch() {
        // ARRANGE
        URI absolutePath = URI.create("https://local-esc-bs.localhost:4443/kinder");

        when(securityIdentity.isAnonymous()).thenReturn(false);
        when(securityIdentity.getPrincipal()).thenReturn(jsonWebToken);
        when(jsonWebToken.getName()).thenReturn("testuser");
        when(uriInfo.getAbsolutePath()).thenReturn(absolutePath);
        when(jsonWebToken.claim(JsonWebTokenClaims.TENANT_CLAIM)).thenReturn(Optional.of("win"));

        // ACT
        filter.filter(requestContext);

        // ASSERT
        verify(requestContext).abortWith(argThat(response ->
            response.getStatus() == Response.Status.FORBIDDEN.getStatusCode()));
    }

    @ParameterizedTest
    @ValueSource(strings = {"bs", "win", "be"})
    void should_allow_request_when_tenant_matches(String tenanturlPart) {
        // ARRANGE
        URI absolutePath = URI.create(String.format("https://local-esc-%s.localhost:4443/kinder", tenanturlPart));

        when(securityIdentity.isAnonymous()).thenReturn(false);
        when(securityIdentity.getPrincipal()).thenReturn(jsonWebToken);
        when(uriInfo.getAbsolutePath()).thenReturn(absolutePath);
        when(jsonWebToken.claim(JsonWebTokenClaims.TENANT_CLAIM)).thenReturn(Optional.of(tenanturlPart.toUpperCase()));

        // ACT
        filter.filter(requestContext);

        // ASSERT
        verify(requestContext, never()).abortWith(any());
    }


    @Test
    void should_return_after_abort_when_tenant_mismatch() {
        // ARRANGE - setup for tenant mismatch but verify the method returns after abort
        URI absolutePath = URI.create("https://local-esc-bs.localhost:4443/test");

        when(securityIdentity.isAnonymous()).thenReturn(false);
        when(securityIdentity.getPrincipal()).thenReturn(jsonWebToken);
        when(jsonWebToken.getName()).thenReturn("testuser");
        when(uriInfo.getAbsolutePath()).thenReturn(absolutePath);
        when(jsonWebToken.claim(JsonWebTokenClaims.TENANT_CLAIM)).thenReturn(Optional.of("win"));

        // ACT & ASSERT
        assertThatCode(() -> filter.filter(requestContext))
            .doesNotThrowAnyException();

        verify(requestContext).abortWith(argThat(response ->
            response.getStatus() == Response.Status.FORBIDDEN.getStatusCode()));
    }

}
