package ch.dvbern.oss.hemed.esc.api.features.kinder;

import ch.dvbern.dvbstarter.infrastructure.ratelimit.RateLimitConst;
import ch.dvbern.dvbstarter.infrastructure.stereotypes.JaxResource;
import ch.dvbern.dvbstarter.shared.openapi.OpenApiConst.Tags;
import ch.dvbern.dvbstarter.shared.types.id.ID;
import ch.dvbern.oss.hemed.esc.api.features.kinder.json.*;
import ch.dvbern.oss.hemed.esc.api.features.kinder.kontakt_person_kind.KontaktPersonKindMapper;
import ch.dvbern.oss.hemed.esc.api.features.kinder.kontakt_person_kind.json.KontaktPersonKindListJson;
import ch.dvbern.oss.hemed.esc.api.features.kinder.types.KindSaveModeDuplikat;
import ch.dvbern.oss.hemed.esc.api.features.schule.Schule;
import ch.dvbern.oss.hemed.esc.api.features.schule.klasse.Klasse;
import ch.dvbern.oss.hemed.esc.api.shared.auth.BerechtigungName;
import ch.dvbern.oss.hemed.esc.api.shared.types.EntityAktivStatusSaveJson;
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
import org.checkerframework.checker.nullness.qual.Nullable;
import org.eclipse.microprofile.metrics.MetricUnits;
import org.eclipse.microprofile.metrics.annotation.Timed;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;

@Path("/kinder")
@JaxResource
@RequiredArgsConstructor
@Tag(name = Tags.TAG_KINDER)
@RolesAllowed(BerechtigungName.KINDER_SCHREIBEN)
@RateLimit(RateLimitConst.DEFAULT_LIMIT)
@Timed(name = "KindResourceMS", unit = MetricUnits.MILLISECONDS)
public class KindResource {

    private final KindMapper kindMapper;
    private final KontaktPersonKindMapper kontaktPersonKindMapper;

    @Context
    private HttpServerResponse response;

    @POST
    @NonNull
    @RolesAllowed(BerechtigungName.KINDER_SCHREIBEN)
    public KindSaveResultJson createKind(
        @NotNull @NonNull @Valid KindSaveJson kindJson,
        @NotNull @QueryParam("kindSaveMode") KindSaveModeDuplikat kindSaveMode
    ) {
        response.setStatusCode(Status.CREATED.getStatusCode());
        return kindMapper.create(kindJson, kindSaveMode);
    }

    @PUT
    @Path("/{kindId}")
    @NonNull
    @RolesAllowed(BerechtigungName.KINDER_SCHREIBEN)
    public KindSaveResultJson updateKind(
        @NotNull @NonNull @PathParam("kindId") ID<Kind> kindID,
        @NotNull @NonNull @Valid KindSaveJson kindJson,
        @NotNull @QueryParam("kindSaveMode") KindSaveModeDuplikat kindSaveMode
    ) {
        return kindMapper.update(kindID, kindJson, kindSaveMode);
    }

    @PUT
    @Path("/{kindId}/kindaktivstatus")
    @RolesAllowed(BerechtigungName.KINDER_SCHREIBEN)
    public void updateKindAktivStatus(
        @NonNull @NotNull @PathParam("kindId") ID<Kind> kindId,
        @NotNull @NonNull @Valid EntityAktivStatusSaveJson saveJson
    ) {
        kindMapper.changeKindAktivStatus(kindId, saveJson);
    }

    @DELETE
    @Path("/{kindId}")
    @RolesAllowed(BerechtigungName.KINDER_SCHREIBEN)
    public void removeKind(@NotNull @NonNull @PathParam("kindId") ID<Kind> kindID) {
        kindMapper.remove(kindID);
    }

    @GET
    @RateLimit(RateLimitConst.DEFAULT_LIMIT)
    @RolesAllowed(BerechtigungName.KINDER_LESEN)
    public KindListJson findAllKinder() {
        return new KindListJson(kindMapper.findAll());
    }

    @GET
    @Path("/options")
    @RateLimit(RateLimitConst.DEFAULT_LIMIT)
    @RolesAllowed(BerechtigungName.KINDER_LESEN)
    public KindListJson findKinderBySchuleOrKlasse(
        @Nullable @QueryParam("schuleId") ID<Schule> schuleId,
        @Nullable @QueryParam("klasseId") ID<Klasse> klasseId
    ) {
        KindSucheOptionsJson options = new KindSucheOptionsJson(schuleId, klasseId);
        return new KindListJson(kindMapper.findByOptions(options));
    }

    @GET
    @Path("/{kindId}")
    @RateLimit(RateLimitConst.DEFAULT_LIMIT)
    @RolesAllowed(BerechtigungName.KINDER_LESEN)
    public KindJson getKind(
        @NotNull @NonNull @PathParam("kindId") ID<Kind> kindId
    ) {
        return kindMapper.getById(kindId);
    }

    @GET
    @Path("/{kindId}/kontaktpersonen")
    @RateLimit(RateLimitConst.DEFAULT_LIMIT)
    @RolesAllowed(BerechtigungName.KINDER_LESEN)
    public KontaktPersonKindListJson findAktivKindKontaktPersonen(
        @NotNull @NonNull @PathParam("kindId") ID<Kind> kindId
    ) {
        return new KontaktPersonKindListJson(kontaktPersonKindMapper.findAktivByKind(kindId));
    }

    @PUT
    @Path("/{kindId}/esad-id-generated")
    @RateLimit(RateLimitConst.DEFAULT_LIMIT)
    @RolesAllowed(BerechtigungName.KINDER_SCHREIBEN)
    public KindJson generateEsadIdForKind(
        @NotNull @NonNull @PathParam("kindId") ID<Kind> kindId
    ) {
        return kindMapper.generateEsadIdForKind(kindId);
    }

    @PUT
    @Path("/{kindId}/esad-id-manual")
    @RateLimit(RateLimitConst.DEFAULT_LIMIT)
    @RolesAllowed(BerechtigungName.KINDER_SCHREIBEN)
    public KindJson setEsadIdForKind(
        @NotNull @NonNull @PathParam("kindId") ID<Kind> kindId,
        @NotNull @NonNull @Valid EsadIdJson esadIdJson
    ) {
        return kindMapper.setEsadIdForKind(kindId, esadIdJson);
    }
}
