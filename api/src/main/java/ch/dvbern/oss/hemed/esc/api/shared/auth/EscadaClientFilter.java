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
 * This filter allows the escada client to only access escada endpoints and vice versa
 */
@Provider
@RequiredArgsConstructor
@Slf4j
@Priority(Priorities.AUTHENTICATION)
public class EscadaClientFilter implements ContainerRequestFilter {
	private final CurrentIdentityAssociation association;
	private final ConfigService configService;

	@Override
	public void filter(ContainerRequestContext requestContext) throws IOException {
        String path = requestContext.getUriInfo().getPath();
        boolean isEscadaRequest = path.startsWith("/escada");
		boolean isEscadaClient = isEscadaClient(association, configService);

		if(isEscadaRequest != isEscadaClient){
            LOG.warn("Refused request because isEscadaRequest={} and isEscadaClient={} did not match for url={}",
                isEscadaRequest, isEscadaClient, path);
			requestContext.abortWith(Response.status(Response.Status.FORBIDDEN).build());
		}
        boolean isEscadaUser = isEscadaUser(association, configService);
        if(isEscadaRequest && !isEscadaUser) {
            LOG.warn("Refused request because isEscadaRequest={} and isEscadaUser={} did not match for url={}",
                isEscadaRequest, isEscadaUser, path);
            requestContext.abortWith(Response.status(Response.Status.FORBIDDEN).build());
        }
	}

    /**
     * Reads the AZP claim (authorized party; e.g. client name that was authorized) and checks if it  matches the
     * configured expected  client name for the escada client.
     *
     * @return true if the configured client and user  are the expected escada user
     */
	private static boolean isEscadaClient(@NonNull CurrentIdentityAssociation identity, @NonNull ConfigService config) {
		if(identity.getIdentity().isAnonymous()) {
			return false;
		}
		JsonWebToken jsonWebToken = (JsonWebToken) identity.getIdentity().getPrincipal();
		String authorizedParty = jsonWebToken.getClaim(Claims.azp);

		return config.getEscadaAZP().equals(authorizedParty);
	}

    private static boolean isEscadaUser(@NonNull CurrentIdentityAssociation identity, @NonNull ConfigService config) {
        if(identity.getIdentity().isAnonymous()) {
            return false;
        }
        JsonWebToken jsonWebToken = (JsonWebToken) identity.getIdentity().getPrincipal();
        String preferedUserName = jsonWebToken.getClaim(Claims.preferred_username);
        return config.getEscadaClientUser().equals(preferedUserName);
    }
}
