package ch.dvbern.oss.hemed.esc.api.features.schule.lehrperson;

import ch.dvbern.dvbstarter.infrastructure.ratelimit.RateLimitConst;
import ch.dvbern.dvbstarter.infrastructure.stereotypes.JaxResource;
import ch.dvbern.dvbstarter.shared.openapi.OpenApiConst.Tags;
import ch.dvbern.dvbstarter.shared.types.id.ID;
import ch.dvbern.oss.hemed.esc.api.features.schule.klasse.Klasse;
import ch.dvbern.oss.hemed.esc.api.features.schule.lehrperson.json.LehrpersonJson;
import ch.dvbern.oss.hemed.esc.api.features.schule.lehrperson.json.LehrpersonListJson;
import ch.dvbern.oss.hemed.esc.api.features.schule.lehrperson.json.LehrpersonOptionListJson;
import ch.dvbern.oss.hemed.esc.api.features.schule.lehrperson.json.LehrpersonSaveJson;
import ch.dvbern.oss.hemed.esc.api.shared.auth.BerechtigungName;
import io.smallrye.common.constraint.NotNull;
import io.smallrye.faulttolerance.api.RateLimit;
import io.vertx.core.http.HttpServerResponse;
import jakarta.annotation.security.RolesAllowed;
import jakarta.validation.Valid;
import jakarta.ws.rs.DELETE;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.PUT;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.Response.Status;
import lombok.RequiredArgsConstructor;
import org.checkerframework.checker.nullness.qual.NonNull;
import io.micrometer.core.annotation.Timed;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;

@Path("/klasse/{klasseID}/lehrpersonen")
@JaxResource
@RequiredArgsConstructor
@Tag(name = Tags.TAG_LEHRPERSON)
@RolesAllowed(BerechtigungName.KLASSEN_LESEN)
@RateLimit(RateLimitConst.DEFAULT_LIMIT)
@Timed("LehrpersonResourceMS")
public class LehrpersonResource {

    private final LehrpersonMapper lehrpersonMapper;

    @Context
    private HttpServerResponse respone;

    @GET
    @NonNull
    @RolesAllowed(BerechtigungName.KLASSEN_LESEN)
    public LehrpersonListJson findAllAktivByKlasse(
        @NotNull @NonNull @PathParam("klasseID") ID<Klasse> klasseId
    ) {
        return new LehrpersonListJson(lehrpersonMapper.findAllAktivByKlasse(klasseId));
    }

    @GET
    @Path("/options")
    @RateLimit(RateLimitConst.DEFAULT_LIMIT)
    @RolesAllowed(BerechtigungName.KLASSEN_LESEN)
    public LehrpersonOptionListJson findAllOptionsActive(
        @NotNull @NonNull @PathParam("klasseID") ID<Klasse> klasseId,
        @NotNull @QueryParam("excludeKlasse") boolean excludeKlasse
    ) {
        ID<Klasse> klasseToExclude = excludeKlasse ? klasseId : null;
        return new LehrpersonOptionListJson(lehrpersonMapper.findAllOptionsActive(klasseToExclude));
    }

    @GET
    @NonNull
    @Path("/{lehrpersonID}")
    @RolesAllowed(BerechtigungName.KLASSEN_LESEN)
    public LehrpersonJson getLehrperson(
        @NotNull @NonNull @PathParam("klasseID") ID<Klasse> klasseId,
        @NotNull @NonNull @PathParam("lehrpersonID") ID<Lehrperson> lehrpersonID
    ) {
        lehrpersonMapper.requireConsistentPathParams(lehrpersonID, klasseId);
        return lehrpersonMapper.getById(lehrpersonID);
    }

    @POST
    @RolesAllowed(BerechtigungName.KLASSEN_SCHREIBEN)
    @NonNull
    public LehrpersonJson createLehrperson(
        @NotNull @NonNull @PathParam("klasseID") ID<Klasse> klasseId,
        @NotNull @NonNull @Valid LehrpersonSaveJson lehrpersonSaveJson
    ) {
        respone.setStatusCode(Status.CREATED.getStatusCode());
        return lehrpersonMapper.create(klasseId, lehrpersonSaveJson);
    }

    @PUT
    @Path("/{lehrpersonID}")
    @NonNull
    @RolesAllowed(BerechtigungName.KLASSEN_SCHREIBEN)
    public LehrpersonJson updateLehrperson(
        @NotNull @NonNull @PathParam("klasseID") ID<Klasse> klasseId,
        @NotNull @NonNull @PathParam("lehrpersonID") ID<Lehrperson> lehrpersonID,
        @NotNull @NonNull @Valid LehrpersonSaveJson lehrpersonSaveJson
    ) {
        lehrpersonMapper.requireConsistentPathParams(lehrpersonID, klasseId);
        return lehrpersonMapper.update(lehrpersonID, lehrpersonSaveJson);
    }

    @DELETE
    @Path("/{lehrpersonID}")
    @RolesAllowed(BerechtigungName.KLASSEN_SCHREIBEN)
    public void deleteLehrperson(
        @NotNull @NonNull @PathParam("klasseID") ID<Klasse> klasseId,
        @NotNull @NonNull @PathParam("lehrpersonID") ID<Lehrperson> lehrpersonID
    ) {
        lehrpersonMapper.requireConsistentPathParams(lehrpersonID, klasseId);
        lehrpersonMapper.delete(lehrpersonID);
    }

    @PUT
    @Path("/schule/{lehrpersonID}")
    @RolesAllowed(BerechtigungName.KLASSEN_SCHREIBEN)
    public LehrpersonJson addLehrpersonToKlasse(
        @NotNull @NonNull @PathParam("klasseID") ID<Klasse> klasseId,
        @NotNull @NonNull @PathParam("lehrpersonID") ID<Lehrperson> lehrpersonID
    ) {
        return lehrpersonMapper.addToKlasse(klasseId, lehrpersonID);
    }

    @DELETE
    @Path("/schule/{lehrpersonID}")
    @RolesAllowed(BerechtigungName.KLASSEN_SCHREIBEN)
    public void removeLehrpersonFromKlasse(
        @NotNull @NonNull @PathParam("klasseID") ID<Klasse> klasseId,
        @NotNull @NonNull @PathParam("lehrpersonID") ID<Lehrperson> lehrpersonID
    ) {
        lehrpersonMapper.requireConsistentPathParams(lehrpersonID, klasseId);
        lehrpersonMapper.removeFromKlasse(klasseId, lehrpersonID);
    }
}
