package ch.dvbern.oss.hemed.esc.api.features.anlass;

import java.util.List;

import ch.dvbern.dvbstarter.infrastructure.ratelimit.RateLimitConst;
import ch.dvbern.dvbstarter.infrastructure.stereotypes.JaxResource;
import ch.dvbern.dvbstarter.shared.openapi.OpenApiConst.Tags;
import ch.dvbern.dvbstarter.shared.types.id.ID;
import ch.dvbern.oss.hemed.esc.api.features.anlass.json.*;
import ch.dvbern.oss.hemed.esc.api.features.anlass.types.AnlassStatus;
import ch.dvbern.oss.hemed.esc.api.features.anlass.types.AnlassTyp;
import ch.dvbern.oss.hemed.esc.api.features.kinder.Kind;
import ch.dvbern.oss.hemed.esc.api.features.schule.json.SchulInformationenListJson;
import ch.dvbern.oss.hemed.esc.api.shared.auth.AuthorizerService;
import ch.dvbern.oss.hemed.esc.api.shared.auth.Berechtigung;
import ch.dvbern.oss.hemed.esc.api.shared.auth.BerechtigungName;
import ch.dvbern.oss.hemed.esc.api.shared.benutzer.Benutzer;
import io.smallrye.faulttolerance.api.RateLimit;
import io.vertx.core.http.HttpServerResponse;
import jakarta.annotation.security.RolesAllowed;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.Response.Status;
import lombok.RequiredArgsConstructor;
import org.checkerframework.checker.nullness.qual.NonNull;
import io.micrometer.core.annotation.Timed;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;

@Path("/anlaesse")
@JaxResource
@RequiredArgsConstructor
@Tag(name = Tags.TAG_ANLASS)
@RolesAllowed({ BerechtigungName.ANLASSLISTE_LESEN, BerechtigungName.ANLASSLISTE_LESEN_LAEUSE })
@RateLimit(RateLimitConst.DEFAULT_LIMIT)
@Timed("AnlassResourceMS")
public class AnlassResource {

    private final AnlassMapper anlassMapper;
    private final AuthorizerService authorizerService;

    @Context
    private HttpServerResponse response;

    @POST
    @NonNull
    @RolesAllowed({ BerechtigungName.ANLASSPLANUNG_SCHREIBEN, BerechtigungName.ANLASSPLANUNG_SCHREIBEN_LAEUSE })
    public AnlassJson createAnlass(
        @NotNull @NonNull @Valid AnlassSaveJson anlassJson
    ) {
        AnlassJson newAnlassJson = anlassMapper.create(anlassJson);
        authorizerService.requireWriteAuthorizationAnlass(
            newAnlassJson.getId(),
            List.of(Berechtigung.ANLASSPLANUNG_SCHREIBEN, Berechtigung.ANLASSPLANUNG_SCHREIBEN_LAEUSE));
        response.setStatusCode(Status.CREATED.getStatusCode());
        return newAnlassJson;
    }

    @PUT
    @Path("/{anlassId}")
    @NonNull
    @RolesAllowed({ BerechtigungName.ANLASSPLANUNG_SCHREIBEN, BerechtigungName.ANLASSPLANUNG_SCHREIBEN_LAEUSE })
    public AnlassJson updateAnlass(
        @NotNull @NonNull @PathParam("anlassId") ID<Anlass> anlassID,
        @NotNull @NonNull @Valid AnlassSaveJson anlassJson
    ) {
        authorizerService.requireWriteAuthorizationAnlass(
            anlassID,
            List.of(Berechtigung.ANLASSPLANUNG_SCHREIBEN, Berechtigung.ANLASSPLANUNG_SCHREIBEN_LAEUSE));
        return anlassMapper.update(anlassID, anlassJson);
    }

    @PUT
    @Path("/{anlassId}/status/{status}")
    @NonNull
    @RolesAllowed({ BerechtigungName.ANLASSPLANUNG_SCHREIBEN, BerechtigungName.ANLASSPLANUNG_SCHREIBEN_LAEUSE })
    public AnlassJson changeAnlassStatus(
        @NotNull @NonNull @PathParam("anlassId") ID<Anlass> anlassID,
        @NotNull @NonNull @PathParam("status") AnlassStatus status
    ) {
        authorizerService.requireWriteAuthorizationAnlass(
            anlassID,
            List.of(Berechtigung.ANLASSPLANUNG_SCHREIBEN, Berechtigung.ANLASSPLANUNG_SCHREIBEN_LAEUSE));
        return anlassMapper.changeAnlassStatus(anlassID, status);
    }

    @DELETE
    @Path("/{anlassId}")
    @RolesAllowed({ BerechtigungName.ANLASSPLANUNG_SCHREIBEN, BerechtigungName.ANLASSPLANUNG_SCHREIBEN_LAEUSE })
    public void removeAnlass(@NotNull @NonNull @PathParam("anlassId") ID<Anlass> anlassID) {
        authorizerService.requireWriteAuthorizationAnlass(
            anlassID,
            List.of(Berechtigung.ANLASSPLANUNG_SCHREIBEN, Berechtigung.ANLASSPLANUNG_SCHREIBEN_LAEUSE));
        anlassMapper.remove(anlassID);
    }

    @GET
    @RateLimit(RateLimitConst.DEFAULT_LIMIT)
    @RolesAllowed({ BerechtigungName.ANLASSLISTE_LESEN, BerechtigungName.ANLASSLISTE_LESEN_LAEUSE })
    public AnlassListJson findAllAnlaesseBerechtigt() {
        List<AnlassJson> anlassList = anlassMapper
            .findAllAnlaesseBerechtigt(anlass -> authorizerService.hasReadAuthorizationAnlass(
                anlass, List.of(Berechtigung.ANLASSLISTE_LESEN, Berechtigung.ANLASSLISTE_LESEN_LAEUSE)
            )).stream()
            .toList();
        return new AnlassListJson(anlassList, anlassList.size());
    }

    @GET
    @Path("/{anlassId}")
    @RateLimit(RateLimitConst.DEFAULT_LIMIT)
    @RolesAllowed({ BerechtigungName.ANLASSPLANUNG_LESEN, BerechtigungName.ANLASSPLANUNG_LESEN_LAEUSE })
    public AnlassJson getAnlass(
        @NotNull @NonNull @PathParam("anlassId") ID<Anlass> anlassID
    ) {
        authorizerService.requireReadAuthorizationAnlass(
            anlassID,
            List.of(Berechtigung.ANLASSPLANUNG_LESEN, Berechtigung.ANLASSPLANUNG_LESEN_LAEUSE));
        return anlassMapper.getById(anlassID);
    }

    @GET
    @Path("/{anlassId}/klasse/lehrpersonen")
    @RateLimit(RateLimitConst.DEFAULT_LIMIT)
    @RolesAllowed({ BerechtigungName.ANLASSPLANUNG_LESEN, BerechtigungName.ANLASSPLANUNG_LESEN_LAEUSE })
    public SchulInformationenListJson getSchulInformationenForAnlass(
        @NotNull @NonNull @PathParam("anlassId") ID<Anlass> anlassID
    ) {
        authorizerService.requireReadAuthorizationAnlass(
            anlassID,
            List.of(Berechtigung.ANLASSPLANUNG_LESEN, Berechtigung.ANLASSPLANUNG_LESEN_LAEUSE));
        return new SchulInformationenListJson(anlassMapper.findSchulInformationen(anlassID));
    }

    @GET
    @Path("/benutzer/{benutzerID}")
    @RateLimit(RateLimitConst.DEFAULT_LIMIT)
    @RolesAllowed({ BerechtigungName.ANLASSLISTE_LESEN, BerechtigungName.ANLASSLISTE_LESEN_LAEUSE })
    public AnlassListJson findAnlaesseForDashboard(@NonNull @NotNull @PathParam("benutzerID") ID<Benutzer> benutzerID) {
        return anlassMapper.findAnlaesseForDashboard(
            benutzerID,
            anlass -> authorizerService.hasReadAuthorizationAnlass(
                anlass, List.of(Berechtigung.ANLASSLISTE_LESEN, Berechtigung.ANLASSLISTE_LESEN_LAEUSE)
            ));
    }

    @GET
    @Path("/search-with-available-slots")
    @RateLimit(RateLimitConst.DEFAULT_LIMIT)
    @RolesAllowed({ BerechtigungName.ANLASSLISTE_LESEN, BerechtigungName.ANLASSLISTE_LESEN_LAEUSE })
    public @NonNull AnlassWithOpenSlotsListJson searchAnlassTermineWithAvailableSlots(
        @NonNull @NotNull @QueryParam("kindId") ID<Kind> kindId,
        @NonNull @NotNull @QueryParam("anlassTyp") List<AnlassTyp> anlassTypen
    ) {
        return anlassMapper.searchAnlassTermineWithAvailableSlots(
            anlassTypen,
            kindId,
            anlassOption -> authorizerService.hasReadAuthorizationAnlass(
                anlassOption.getAnlassId(),
                List.of(Berechtigung.ANLASSLISTE_LESEN, Berechtigung.ANLASSLISTE_LESEN_LAEUSE)));
    }

    @GET
    @Path("/search-sheduled-for-kind")
    @RateLimit(RateLimitConst.DEFAULT_LIMIT)
    @RolesAllowed({ BerechtigungName.ANLASSLISTE_LESEN, BerechtigungName.ANLASSLISTE_LESEN_LAEUSE })
    public @NonNull AnlassWithDateTimeListJson findScheduledAnlaesseForKind(
        @NonNull @NotNull @QueryParam("kindId") ID<Kind> kindId,
        @NonNull @NotNull @QueryParam("excludeAnlassIds") List<ID<Anlass>> excludeAnlassIds,
        @NonNull @NotNull @QueryParam("anlassTyp") List<AnlassTyp> anlassTypen
    ) {
        return anlassMapper.findScheduledAnlaesseOfTypeForKind(anlassTypen, kindId, excludeAnlassIds,
            anlass -> authorizerService.hasReadAuthorizationAnlass(
                anlass.getAnlassId(),
                List.of(Berechtigung.ANLASSLISTE_LESEN, Berechtigung.ANLASSLISTE_LESEN_LAEUSE)));
    }
}
