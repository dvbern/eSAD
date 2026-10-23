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
import java.net.URL;

import ch.dvbern.oss.hemed.esc.api.Util;
import io.quarkus.arc.profile.UnlessBuildProfile;
import jakarta.enterprise.context.RequestScoped;
import jakarta.ws.rs.container.ContainerRequestContext;
import jakarta.ws.rs.container.ContainerRequestFilter;
import jakarta.ws.rs.container.PreMatching;
import jakarta.ws.rs.ext.Provider;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;

@Slf4j
@Provider
@PreMatching
@RequestScoped
@RequiredArgsConstructor
@UnlessBuildProfile("test") // run this in dev and prod, tests use TenantTestFilter
public class TenantExtractorFromUrlRequestFilter implements ContainerRequestFilter {

    private final TenantContext tenantContext;


    @Override
    public void filter(ContainerRequestContext requestContext) {
        URI absolutePath = requestContext.getUriInfo().getAbsolutePath();
        TenantEnum tenantFromUrl = TenantExtractorFromUrlRequestFilter.getTenantFromUrl(absolutePath);
        // without a tenant every access to tenant data fails loudly (no fallback to a default tenant)
        if (tenantFromUrl != null) {
            tenantContext.setCurrentTenant(tenantFromUrl);
        }
    }

    @Nullable
    public static TenantEnum getTenantFromUrl(@NonNull URI uri) {
        for (TenantEnum tenant : TenantEnum.values()) {
            if (tenant.isAktiv()) {
                URL url = Util.parseURI(uri);
                if (url.getHost().contains("esc-" + tenant.getRealmName())
                    || url.getHost().contains(tenant.getRealmName() +"-esad")) {
                    return tenant;
                }
            }
        }
        LOG.error("ESC: Could not determine tenant from URL: {}. "
                + "Please check the configuration in {}.", uri, TenantExtractorFromUrlRequestFilter.class.getSimpleName());
        return null;
    }
}
