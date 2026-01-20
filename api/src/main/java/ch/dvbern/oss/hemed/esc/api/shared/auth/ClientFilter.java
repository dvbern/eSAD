/*
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

import ch.dvbern.oss.hemed.esc.api.shared.config.service.ConfigService;
import io.quarkus.security.identity.CurrentIdentityAssociation;
import jakarta.annotation.Priority;
import jakarta.ws.rs.Priorities;
import jakarta.ws.rs.container.ContainerRequestContext;
import jakarta.ws.rs.container.ContainerRequestFilter;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.Provider;
import java.io.IOException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.eclipse.microprofile.jwt.Claims;
import org.eclipse.microprofile.jwt.JsonWebToken;

/**
 * This filter allows
 * the escada client to only access escada endpoints and vice versa
 * the pupil client to only access pupil endpoints and vice versa
 */
@Provider
@RequiredArgsConstructor
@Slf4j
@Priority(Priorities.AUTHENTICATION)
public class ClientFilter implements ContainerRequestFilter {
	private final CurrentIdentityAssociation association;
	private final ConfigService configService;

    @Override
    public void filter(ContainerRequestContext requestContext) throws IOException {
        checkRessouceAccessForClient(FilteredClientEnum.PUPIL, association, requestContext);
        checkRessouceAccessForClient(FilteredClientEnum.ESCADA, association, requestContext);
    }

    void checkRessouceAccessForClient(@NonNull FilteredClientEnum filteredClient, @NonNull CurrentIdentityAssociation association, @NonNull ContainerRequestContext requestContext) {
        String path = requestContext.getUriInfo().getPath();
        boolean isProtectedRequest = path.startsWith(getResourcePathForClient(filteredClient));
        boolean isProtectedClient = isProtectedClient(association, configService, filteredClient);
        if(isProtectedRequest != isProtectedClient){
            LOG.warn("Refused request because isProtectedRequest={} and isProtectedClient={} did not match for url={} and client={}",
                filteredClient, isProtectedRequest, isProtectedClient, path);
            requestContext.abortWith(Response.status(Response.Status.FORBIDDEN).build());
        }
        boolean isProtectedUser = isProtectedUser(association, configService, filteredClient);

        if(isProtectedRequest && !isProtectedUser) {
            LOG.warn("Refused request because isProtectedRequest={} and isProtectedUser={} did not match for url={} and client={}",
                filteredClient, isProtectedRequest, isProtectedUser, path);
            requestContext.abortWith(Response.status(Response.Status.FORBIDDEN).build());
        }
    }

    private boolean isProtectedClient(@NonNull CurrentIdentityAssociation identity, @NonNull ConfigService config, @NonNull FilteredClientEnum filteredClient) {
        if(identity.getIdentity().isAnonymous()) {
            return false;
        }
        JsonWebToken jsonWebToken = (JsonWebToken) identity.getIdentity().getPrincipal();
        String authorizedParty = jsonWebToken.getClaim(Claims.azp);

        return getConfiguredAZP(filteredClient,config).equals(authorizedParty);
    }

    private boolean isProtectedUser(@NonNull CurrentIdentityAssociation identity, @NonNull ConfigService config, @NonNull FilteredClientEnum filteredClient) {
        if(identity.getIdentity().isAnonymous()) {
            return false;
        }
        JsonWebToken jsonWebToken = (JsonWebToken) identity.getIdentity().getPrincipal();
        String preferedUserName = jsonWebToken.getClaim(Claims.preferred_username);

        return getConfiguredUser(filteredClient, config).equals(preferedUserName);
    }

    private String getResourcePathForClient(@NonNull FilteredClientEnum clientName){
        return switch (clientName) {
            case FilteredClientEnum.PUPIL -> "/pupil";
            case FilteredClientEnum.ESCADA -> "/escada";
        };
    }

    private String getConfiguredUser(@NonNull FilteredClientEnum clientName, @NonNull ConfigService config){
        return switch (clientName) {
            case FilteredClientEnum.PUPIL -> config.getPupilClientUser();
            case FilteredClientEnum.ESCADA -> config.getEscadaClientUser();
        };
    }

    private String getConfiguredAZP(@NonNull FilteredClientEnum clientName, @NonNull ConfigService config){
        return switch (clientName) {
            case FilteredClientEnum.PUPIL-> config.getPupilAZP();
            case FilteredClientEnum.ESCADA -> config.getEscadaAZP();
        };
    }
}
