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
import io.quarkus.security.AuthenticationFailedException;
import io.quarkus.security.ForbiddenException;
import io.quarkus.security.identity.CurrentIdentityAssociation;
import io.quarkus.security.identity.SecurityIdentity;
import io.smallrye.mutiny.Uni;
import jakarta.ws.rs.InternalServerErrorException;
import jakarta.ws.rs.container.ContainerRequestContext;
import jakarta.ws.rs.core.UriInfo;
import org.eclipse.microprofile.jwt.JsonWebToken;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class TenantClaimMatchesUrlFilterTest {

    private ContainerRequestContext requestContext;
    private SecurityIdentity securityIdentity;
    private JsonWebToken jsonWebToken;
    private UriInfo uriInfo;
    private TenantClaimMatchesUrlFilter filter;

    @BeforeEach
    void setUp() {
        requestContext = mock(ContainerRequestContext.class);
        securityIdentity = mock(SecurityIdentity.class);
        jsonWebToken = mock(JsonWebToken.class);
        uriInfo = mock(UriInfo.class);

        when(requestContext.getUriInfo()).thenReturn(uriInfo);
        when(uriInfo.getPath()).thenReturn("/some/path");
        filter = new TenantClaimMatchesUrlFilter(null);
    }

    @Test
    void should_skip_validation_for_anonymous_requests() {
        // ARRANGE
        when(securityIdentity.isAnonymous()).thenReturn(true);

        // ACT & ASSERT
        assertThatCode(() -> filter.checkIdentityForTenant(requestContext, securityIdentity))
            .doesNotThrowAnyException();
        verify(requestContext, never()).abortWith(any());
    }

    @Test
    void should_throw_forbidden_when_no_tenant_claim_found() {
        // ARRANGE
        when(securityIdentity.isAnonymous()).thenReturn(false);
        when(securityIdentity.getPrincipal()).thenReturn(jsonWebToken);
        when(jsonWebToken.getName()).thenReturn("testuser");

        when(jsonWebToken.claim(JsonWebTokenClaims.TENANT_CLAIM)).thenReturn(Optional.empty());

        // ACT & ASSERT
        assertThatThrownBy(() -> filter.checkIdentityForTenant(requestContext, securityIdentity))
            .isInstanceOf(ForbiddenException.class);
    }

    @Test
    void should_throw_internal_server_error_when_cannot_determine_tenant_from_url() {
        // ARRANGE
        URI absolutePath = URI.create("https://invalid-url.com/test");

        when(securityIdentity.isAnonymous()).thenReturn(false);
        when(securityIdentity.getPrincipal()).thenReturn(jsonWebToken);
        when(uriInfo.getAbsolutePath()).thenReturn(absolutePath);
        when(jsonWebToken.claim(JsonWebTokenClaims.TENANT_CLAIM)).thenReturn(Optional.of("bs"));

        // ACT & ASSERT
        assertThatThrownBy(() -> filter.checkIdentityForTenant(requestContext, securityIdentity))
            .isInstanceOf(InternalServerErrorException.class);
    }

    @Test
    void should_throw_forbidden_when_invalid_tenant_claim() {
        // ARRANGE
        URI absolutePath = URI.create("https://local-esc-bs.localhost:4443/test");

        when(securityIdentity.isAnonymous()).thenReturn(false);
        when(securityIdentity.getPrincipal()).thenReturn(jsonWebToken);
        when(uriInfo.getAbsolutePath()).thenReturn(absolutePath);
        when(jsonWebToken.claim(JsonWebTokenClaims.TENANT_CLAIM)).thenReturn(Optional.of("invalid-tenant"));

        // ACT & ASSERT
        assertThatThrownBy(() -> filter.checkIdentityForTenant(requestContext, securityIdentity))
            .isInstanceOf(ForbiddenException.class);
    }

    @Test
    void should_throw_forbidden_when_tenant_mismatch() {
        // ARRANGE
        URI absolutePath = URI.create("https://local-esc-bs.localhost:4443/kinder");

        when(securityIdentity.isAnonymous()).thenReturn(false);
        when(securityIdentity.getPrincipal()).thenReturn(jsonWebToken);
        when(jsonWebToken.getName()).thenReturn("testuser");
        when(uriInfo.getAbsolutePath()).thenReturn(absolutePath);
        when(jsonWebToken.claim(JsonWebTokenClaims.TENANT_CLAIM)).thenReturn(Optional.of("win"));

        // ACT & ASSERT
        assertThatThrownBy(() -> filter.checkIdentityForTenant(requestContext, securityIdentity))
            .isInstanceOf(ForbiddenException.class);
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

        // ACT & ASSERT
        assertThatCode(() -> filter.checkIdentityForTenant(requestContext, securityIdentity))
            .doesNotThrowAnyException();
        verify(requestContext, never()).abortWith(any());
    }

    @Test
    void should_stop_validation_when_tenant_mismatch() {
        // ARRANGE - mismatch: the method must stop at the denial (now via exception), not fall through
        URI absolutePath = URI.create("https://local-esc-bs.localhost:4443/test");

        when(securityIdentity.isAnonymous()).thenReturn(false);
        when(securityIdentity.getPrincipal()).thenReturn(jsonWebToken);
        when(jsonWebToken.getName()).thenReturn("testuser");
        when(uriInfo.getAbsolutePath()).thenReturn(absolutePath);
        when(jsonWebToken.claim(JsonWebTokenClaims.TENANT_CLAIM)).thenReturn(Optional.of("win"));

        // ACT & ASSERT
        assertThatThrownBy(() -> filter.checkIdentityForTenant(requestContext, securityIdentity))
            .isInstanceOf(ForbiddenException.class);
    }

    @Test
    void should_skip_validation_for_public_endpoints() {
        // ARRANGE
        when(securityIdentity.isAnonymous()).thenReturn(false);
        when(uriInfo.getPath()).thenReturn("/pub/config");

        // ACT & ASSERT
        assertThatCode(() -> filter.checkIdentityForTenant(requestContext, securityIdentity))
            .doesNotThrowAnyException();
        verify(requestContext, never()).abortWith(any());
    }

    @Test
    void should_skip_validation_for_hin_user() {
        // ARRANGE
        when(securityIdentity.isAnonymous()).thenReturn(false);
        when(securityIdentity.getPrincipal()).thenReturn(jsonWebToken);
        when(jsonWebToken.claim(JsonWebTokenClaims.HIN_PERSON_CODE)).thenReturn(Optional.of("some-hin-code"));

        // ACT & ASSERT
        assertThatCode(() -> filter.checkIdentityForTenant(requestContext, securityIdentity))
            .doesNotThrowAnyException();
        verify(requestContext, never()).abortWith(any());
    }

    @Test
    void validateTenantClaim_propagates_authentication_failure_so_framework_returns_401_not_500() {
        // A bad/expired/malformed token arrives at getDeferredIdentity() as an AuthenticationException.
        // The filter must NOT swallow it - propagating lets Quarkus answer 401 instead of 500.
        CurrentIdentityAssociation association = mock(CurrentIdentityAssociation.class);
        when(association.getDeferredIdentity())
            .thenReturn(Uni.createFrom().failure(new AuthenticationFailedException("bad token")));
        TenantClaimMatchesUrlFilter reactiveFilter = new TenantClaimMatchesUrlFilter(association);

        Throwable[] captured = new Throwable[1];
        reactiveFilter.validateTenantClaim(requestContext).subscribe().with(v -> {
        }, t -> captured[0] = t);

        assertThat(captured[0])
            .as("the auth failure must propagate so Quarkus maps it to 401 (not be aborted as 500)")
            .isInstanceOf(AuthenticationFailedException.class);
    }

    @Test
    void validateTenantClaim_lets_anonymous_identity_through_without_failure() {
        SecurityIdentity anonymous = mock(SecurityIdentity.class);
        when(anonymous.isAnonymous()).thenReturn(true);
        CurrentIdentityAssociation association = mock(CurrentIdentityAssociation.class);
        when(association.getDeferredIdentity()).thenReturn(Uni.createFrom().item(anonymous));
        TenantClaimMatchesUrlFilter reactiveFilter = new TenantClaimMatchesUrlFilter(association);

        Throwable[] captured = new Throwable[1];
        reactiveFilter.validateTenantClaim(requestContext).subscribe().with(v -> {
        }, t -> captured[0] = t);

        assertThat(captured[0])
            .as("anonymous requests must not fail tenant validation")
            .isNull();
    }
}
