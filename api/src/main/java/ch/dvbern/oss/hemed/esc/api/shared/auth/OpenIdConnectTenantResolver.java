/*
 * Copyright (C) 2022 DV Bern AG, Switzerland
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

import io.quarkus.oidc.TenantResolver;
import io.vertx.ext.web.RoutingContext;
import jakarta.enterprise.context.ApplicationScoped;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;

/**
 * This filter is a default implementation of the TenantResolver interface.
 * The Idea is that this class could check the context to extract the requested url and decide which
 * oidc tenant to use (this is needed to validate the passed jwt token)
 * <br>
 * The relevant application.properties are:
 * quarkus.oidc.{tenant}.auth-server-url, quarkus.oidc.{tenant}.client-id etc.
 */
@ApplicationScoped
@RequiredArgsConstructor
@Slf4j
public class OpenIdConnectTenantResolver implements TenantResolver {

    @Override
    @Nullable
    public String resolve(@NonNull RoutingContext context) {
        String fullUrl = context.request().absoluteURI();
        TenantEnum tenantFromUrl = TenantExtractorFromUrlRequestFilter.getTenantFromUrl(URI.create(fullUrl));
        if (tenantFromUrl != null) {
            return tenantFromUrl.getRealmName();
        }
        return null;
    }
}
