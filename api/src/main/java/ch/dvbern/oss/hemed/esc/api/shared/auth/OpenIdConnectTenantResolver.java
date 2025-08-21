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

import ch.dvbern.oss.hemed.esc.api.shared.config.service.ConfigService;
import io.quarkus.oidc.TenantResolver;
import io.vertx.ext.web.RoutingContext;
import jakarta.enterprise.context.ApplicationScoped;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;

/**
 * This filter is a default implementation of the TenantResolver interface. Currently all it does is return
 * null for the default tenant configuration.
 * <br>
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

    private final ConfigService configService;

    @Override
    @Nullable
    public String resolve(@NonNull RoutingContext context) {
        return configService.getTenantOverride()
            .map(OpenIdConnectTenantResolver::handleTenantOverrideReturn)
            .orElseGet(() -> this.resolveTenantIdFromContext(context));
    }

    @Nullable
    private String resolveTenantIdFromContext(@NonNull RoutingContext context) {
        if (isMandantBasel(context)) {
            return TenantEnum.BS.getRealmName();
        }
        if (isMandantBern(context)) {
            return TenantEnum.BE.getRealmName();
        }
        return null;
    }

    @NonNull
    private static String handleTenantOverrideReturn(@NonNull TenantEnum tenantEnum) {
        LOG.debug("Tenant override detected. Using tenant={}", tenantEnum);
        return tenantEnum.getRealmName();
    }

    /**
     * Futureproofing: if we ever have more than one tenant, we can check the url here  usually the url would be
     * something like dev-mik-be.dvbern.ch/api/v1/
     * so we can check the url and return the tenant name.
     * <pre>
     * 		String fullUrl = context.request().absoluteURI();
     * 		// convert to url and extract host
     * 		URI uri = URI.create(fullUrl);
     * 		String host = uri.getHost();
     * 		// check if host is a known mandant
     * 		boolean isBern =  Mandant.BERN.getDomains().contains(host); // check against a known list of domains
     * </pre>
     *
     * @param context the context where we can extract the uri from
     * @return true if bern is the mandant
     */
    public boolean isMandantBern(@NonNull RoutingContext context) {
        return false;
    }

    public boolean isMandantBasel(@NonNull RoutingContext context) {
        return true;
    }
}
