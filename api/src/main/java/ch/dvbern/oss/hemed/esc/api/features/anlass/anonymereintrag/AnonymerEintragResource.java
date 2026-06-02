/*
 * Copyright (C) 2026 DV Bern AG, Switzerland
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package ch.dvbern.oss.hemed.esc.api.features.anlass.anonymereintrag;

import java.util.List;

import ch.dvbern.dvbstarter.infrastructure.ratelimit.RateLimitConst;
import ch.dvbern.dvbstarter.infrastructure.stereotypes.JaxResource;
import ch.dvbern.dvbstarter.shared.openapi.OpenApiConst.Tags;
import ch.dvbern.dvbstarter.shared.types.id.HasUntersuchungIDWrapper;
import ch.dvbern.dvbstarter.shared.types.id.ID;
import ch.dvbern.oss.hemed.esc.api.features.anlass.Anlass;
import ch.dvbern.oss.hemed.esc.api.features.anlass.einladung.EinladungMapper;
import ch.dvbern.oss.hemed.esc.api.shared.auth.AuthorizerService;
import ch.dvbern.oss.hemed.esc.api.shared.auth.Berechtigung;
import ch.dvbern.oss.hemed.esc.api.shared.auth.BerechtigungName;
import io.micrometer.core.annotation.Timed;
import io.smallrye.faulttolerance.api.RateLimit;
import io.vertx.core.http.HttpServerResponse;
import jakarta.annotation.security.RolesAllowed;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.PUT;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.Response.Status;
import lombok.RequiredArgsConstructor;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;

@Path("/anonymer-eintrag")
@JaxResource
@RequiredArgsConstructor
@Tag(name = Tags.TAG_ANONYMEREINTRAG)
@RolesAllowed({ BerechtigungName.ANLASSLISTE_LESEN, BerechtigungName.ANLASSLISTE_LESEN_LAEUSE })
@RateLimit(RateLimitConst.DEFAULT_LIMIT)
@Timed("AnonymerEintragResourceMS")
public class AnonymerEintragResource {

    private final AnonymerEintragMapper anonymerEintragMapper;
    private final EinladungMapper einladungMapper;
    private final AuthorizerService authorizerService;

    @Context
    private HttpServerResponse response;

    @GET
    @Path("/{anonymerEintragId}")
    @RateLimit(RateLimitConst.DEFAULT_LIMIT)
    @RolesAllowed({ BerechtigungName.ANLASSPLANUNG_LESEN, BerechtigungName.ANLASSPLANUNG_LESEN_LAEUSE })
    public AnonymerEintragJson getAnonymerEintrag(
        @NotNull @NonNull @PathParam("anonymerEintragId") ID<AnonymerEintrag> anonymerEintragID
    ) {
        return anonymerEintragMapper.getById(anonymerEintragID);
    }

    @POST
    @NonNull
    @RolesAllowed({ BerechtigungName.ANLASSPLANUNG_SCHREIBEN, BerechtigungName.ANLASSPLANUNG_SCHREIBEN_LAEUSE })
    public AnonymerEintragJson createAnonymerEintrag(
        @NotNull @NonNull @Valid AnonymerEintragCreateJson anonymerEintragCreateJson
    ) {
        AnonymerEintragJson anonymerEintragJson =
            anonymerEintragMapper.createAnonymerEintrag(anonymerEintragCreateJson);
        response.setStatusCode(Status.CREATED.getStatusCode());
        return anonymerEintragJson;
    }

    @PUT
    @Path("/{anlassId}/{anonymerEintragId}/abschluss")
    @RolesAllowed({ BerechtigungName.ANLASSPLANUNG_SCHREIBEN, BerechtigungName.ANLASSPLANUNG_SCHREIBEN_LAEUSE })
    public void abschlussAnonymerEintrag(
        @NotNull @NonNull @PathParam("anlassId") ID<Anlass> anlassId,
        @NotNull @NonNull @PathParam("anonymerEintragId") HasUntersuchungIDWrapper wrappedId
    ) {
        einladungMapper.requireConsistentPathParams(wrappedId, anlassId);
        anonymerEintragMapper.anonymerEintragAbschliessen(
            wrappedId.toAnonymerEintragId(),
            anlassIdFromAnonymerEintrag -> authorizerService.requireWriteAuthorizationAnlass(
                anlassIdFromAnonymerEintrag,
                List.of(Berechtigung.ANLASSPLANUNG_SCHREIBEN, Berechtigung.ANLASSPLANUNG_SCHREIBEN_LAEUSE)));
    }
}
