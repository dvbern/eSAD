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
import io.quarkus.security.ForbiddenException;
import io.quarkus.security.identity.CurrentIdentityAssociation;
import io.quarkus.security.identity.SecurityIdentity;
import io.smallrye.mutiny.Uni;
import jakarta.ws.rs.Priorities;
import jakarta.ws.rs.container.ContainerRequestContext;
import jakarta.ws.rs.ext.Provider;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.eclipse.microprofile.jwt.Claims;
import org.eclipse.microprofile.jwt.JsonWebToken;
import org.jboss.resteasy.reactive.server.ServerRequestFilter;

/**
 * This filter allows
 * the escada client to only access escada endpoints and vice versa
 * the pupil client to only access pupil endpoints and vice versa
 * the hellodata client to only access hellodata endpoints and vice versa
 * <p>
 * The check runs against the deferred {@link SecurityIdentity} so that it also works when
 * proactive authentication is disabled ({@code quarkus.http.auth.proactive=false}). Returning
 * the {@link Uni} hands the subscription to RESTEasy Reactive, which suspends the request until
 * the identity has been resolved.
 */
@Provider
@RequiredArgsConstructor
@Slf4j
public class ClientFilter {
	private final CurrentIdentityAssociation association;
	private final ConfigService configService;

	@ServerRequestFilter(priority = Priorities.AUTHENTICATION)
	@NonNull
	public Uni<Void> enforce(@NonNull ContainerRequestContext requestContext) {
		return association.getDeferredIdentity()
			.invoke(identity -> checkClientAccess(identity, requestContext))
			.replaceWithVoid();
	}

	private void checkClientAccess(@NonNull SecurityIdentity identity, @NonNull ContainerRequestContext requestContext) {
		checkRessouceAccessForClient(FilteredClientEnum.PUPIL, identity, requestContext);
		checkRessouceAccessForClient(FilteredClientEnum.ESCADA, identity, requestContext);
		checkRessouceAccessForClient(FilteredClientEnum.HELLODATA, identity, requestContext);
	}

	void checkRessouceAccessForClient(@NonNull FilteredClientEnum filteredClient, @NonNull SecurityIdentity identity,
		@NonNull ContainerRequestContext requestContext) {
		String path = requestContext.getUriInfo().getPath();
		boolean isProtectedRequest = path.startsWith(getResourcePathForClient(filteredClient));
		boolean isProtectedClient = isProtectedClient(identity, filteredClient);
		if (isProtectedRequest != isProtectedClient) {
			LOG.warn("Refused request because isProtectedRequest={} and isProtectedClient={} did not match for url={} and client={}",
				filteredClient, isProtectedRequest, isProtectedClient, path);
			throw new ForbiddenException();
		}
		boolean isProtectedUser = isProtectedUser(identity, filteredClient);

		if (isProtectedRequest && !isProtectedUser) {
			LOG.warn("Refused request because isProtectedRequest={} and isProtectedUser={} did not match for url={} and client={}",
				filteredClient, isProtectedRequest, isProtectedUser, path);
			throw new ForbiddenException();
		}
	}

	private boolean isProtectedClient(@NonNull SecurityIdentity identity, @NonNull FilteredClientEnum filteredClient) {
		if (identity.isAnonymous()) {
			return false;
		}
		JsonWebToken jsonWebToken = (JsonWebToken) identity.getPrincipal();
		String authorizedParty = jsonWebToken.getClaim(Claims.azp);

		return getConfiguredAZP(filteredClient, configService).equals(authorizedParty);
	}

	private boolean isProtectedUser(@NonNull SecurityIdentity identity, @NonNull FilteredClientEnum filteredClient) {
		if (identity.isAnonymous()) {
			return false;
		}
		JsonWebToken jsonWebToken = (JsonWebToken) identity.getPrincipal();
		String preferedUserName = jsonWebToken.getClaim(Claims.preferred_username);

		return getConfiguredUser(filteredClient, configService).equals(preferedUserName);
	}

	private String getResourcePathForClient(@NonNull FilteredClientEnum clientName) {
		return switch (clientName) {
			case FilteredClientEnum.PUPIL -> "/pupil";
			case FilteredClientEnum.ESCADA -> "/escada";
			case FilteredClientEnum.HELLODATA -> "/hellodata";
		};
	}

	private String getConfiguredUser(@NonNull FilteredClientEnum clientName, @NonNull ConfigService config) {
		return switch (clientName) {
			case FilteredClientEnum.PUPIL -> config.getPupilClientUser();
			case FilteredClientEnum.ESCADA -> config.getEscadaClientUser();
			case FilteredClientEnum.HELLODATA -> config.getHelloDataClientUser();
		};
	}

	private String getConfiguredAZP(@NonNull FilteredClientEnum clientName, @NonNull ConfigService config) {
		return switch (clientName) {
			case FilteredClientEnum.PUPIL -> config.getPupilAZP();
			case FilteredClientEnum.ESCADA -> config.getEscadaAZP();
			case FilteredClientEnum.HELLODATA -> config.getHelloDataAZP();
		};
	}
}
