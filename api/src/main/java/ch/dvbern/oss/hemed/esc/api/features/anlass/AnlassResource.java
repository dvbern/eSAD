package ch.dvbern.oss.hemed.esc.api.features.anlass;

import java.util.List;

import ch.dvbern.dvbstarter.infrastructure.ratelimit.RateLimitConst;
import ch.dvbern.dvbstarter.infrastructure.stereotypes.JaxResource;
import ch.dvbern.dvbstarter.shared.openapi.OpenApiConst.Tags;
import ch.dvbern.dvbstarter.shared.types.id.ID;
import ch.dvbern.oss.hemed.esc.api.features.anlass.json.AnlassJson;
import ch.dvbern.oss.hemed.esc.api.features.anlass.json.AnlassListJson;
import ch.dvbern.oss.hemed.esc.api.features.anlass.json.AnlassSaveJson;
import ch.dvbern.oss.hemed.esc.api.features.anlass.types.AnlassStatus;
import ch.dvbern.oss.hemed.esc.api.features.schule.json.SchulInformationenListJson;
import ch.dvbern.oss.hemed.esc.api.shared.auth.AuthorizerService;
import ch.dvbern.oss.hemed.esc.api.shared.auth.BerechtigungName;
import ch.dvbern.oss.hemed.esc.api.shared.benutzer.Benutzer;
import io.smallrye.faulttolerance.api.RateLimit;
import io.vertx.core.http.HttpServerResponse;
import jakarta.annotation.security.RolesAllowed;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.ws.rs.DELETE;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.PUT;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.Response.Status;
import lombok.RequiredArgsConstructor;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.eclipse.microprofile.metrics.MetricUnits;
import org.eclipse.microprofile.metrics.annotation.Timed;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;

@Path("/anlaesse")
@JaxResource
@RequiredArgsConstructor
@Tag(name = Tags.TAG_ANLASS)
@RolesAllowed(BerechtigungName.ANLASSLISTE_LESEN)
@RateLimit(RateLimitConst.DEFAULT_LIMIT)
@Timed(name = "AnlassResourceMS", unit = MetricUnits.MILLISECONDS)
public class AnlassResource {

    private final AnlassMapper anlassMapper;
    private final AuthorizerService authorizerService;

    @Context
    private HttpServerResponse response;

    @POST
    @NonNull
    @RolesAllowed(BerechtigungName.ANLASSPLANUNG_SCHREIBEN)
    public AnlassJson createAnlass(
        @NotNull @NonNull @Valid AnlassSaveJson anlassJson
    ) {
        response.setStatusCode(Status.CREATED.getStatusCode());
        return anlassMapper.create(anlassJson);
    }

    @PUT
    @Path("/{anlassId}")
    @NonNull
    @RolesAllowed(BerechtigungName.ANLASSPLANUNG_SCHREIBEN)
    public AnlassJson updateAnlass(
        @NotNull @NonNull @PathParam("anlassId") ID<Anlass> anlassID,
        @NotNull @NonNull @Valid AnlassSaveJson anlassJson
    ) {
        authorizerService.requireWriteAuthorizationAnlass(anlassID);
        return anlassMapper.update(anlassID, anlassJson);
    }

    @PUT
    @Path("/{anlassId}/status/{status}")
    @NonNull
    @RolesAllowed(BerechtigungName.ANLASSPLANUNG_SCHREIBEN)
    public AnlassJson changeAnlassStatus(
        @NotNull @NonNull @PathParam("anlassId") ID<Anlass> anlassID,
        @NotNull @NonNull @PathParam("status") AnlassStatus status
    ) {
        authorizerService.requireWriteAuthorizationAnlass(anlassID);
        return anlassMapper.changeAnlassStatus(anlassID, status);
    }

    @DELETE
    @Path("/{anlassId}")
    @RolesAllowed(BerechtigungName.ANLASSPLANUNG_SCHREIBEN)
    public void removeAnlass(@NotNull @NonNull @PathParam("anlassId") ID<Anlass> anlassID) {
        anlassMapper.remove(anlassID);
    }

    @GET
    @RateLimit(RateLimitConst.DEFAULT_LIMIT)
    @RolesAllowed(BerechtigungName.ANLASSLISTE_LESEN)
    public AnlassListJson findAllAnlaesse() {
        List<AnlassJson> anlassList = anlassMapper.findAll();
        return new AnlassListJson(anlassList, anlassList.size());
    }

    @GET
    @Path("/{anlassId}")
    @RateLimit(RateLimitConst.DEFAULT_LIMIT)
    @RolesAllowed(BerechtigungName.ANLASSPLANUNG_LESEN)
    public AnlassJson getAnlass(
        @NotNull @NonNull @PathParam("anlassId") ID<Anlass> anlassID
    ) {
        return anlassMapper.getById(anlassID);
    }

    @GET
    @Path("/{anlassId}/klasse/lehrpersonen")
    @RateLimit(RateLimitConst.DEFAULT_LIMIT)
    @RolesAllowed(BerechtigungName.ANLASSPLANUNG_LESEN)
    public SchulInformationenListJson getSchulInformationenForAnlass(
        @NotNull @NonNull @PathParam("anlassId") ID<Anlass> anlassID
    ) {
        return new SchulInformationenListJson(anlassMapper.findSchulInformationen(anlassID));
    }

    @GET
    @Path("/benutzer/{benutzerID}")
    @RateLimit(RateLimitConst.DEFAULT_LIMIT)
    @RolesAllowed(BerechtigungName.ANLASSLISTE_LESEN)
    public AnlassListJson findAllForUser(@NonNull @NotNull @PathParam("benutzerID") ID<Benutzer> benutzerID) {
        return anlassMapper.findAllForUser(benutzerID);
    }
}
