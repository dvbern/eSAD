package ch.dvbern.oss.hemed.esc.api.features.schule.kontaktpersonschule;

import ch.dvbern.dvbstarter.infrastructure.ratelimit.RateLimitConst;
import ch.dvbern.dvbstarter.infrastructure.stereotypes.JaxResource;
import ch.dvbern.dvbstarter.shared.openapi.OpenApiConst.Tags;
import ch.dvbern.dvbstarter.shared.types.id.ID;
import ch.dvbern.oss.hemed.esc.api.features.schule.Schule;
import ch.dvbern.oss.hemed.esc.api.features.schule.kontaktpersonschule.json.KontaktPersonSchuleJson;
import ch.dvbern.oss.hemed.esc.api.features.schule.kontaktpersonschule.json.KontaktPersonSchuleListJson;
import ch.dvbern.oss.hemed.esc.api.features.schule.kontaktpersonschule.json.KontaktPersonSchuleOptionListJson;
import ch.dvbern.oss.hemed.esc.api.features.schule.kontaktpersonschule.json.KontaktPersonSchuleSaveJson;
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
import org.eclipse.microprofile.metrics.MetricUnits;
import org.eclipse.microprofile.metrics.annotation.Timed;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;

@Path("/schulen/{schuleID}/kontaktpersonen")
@JaxResource
@RequiredArgsConstructor
@Tag(name = Tags.TAG_KONTAKTPERSON_SCHULE)
@RolesAllowed(BerechtigungName.LISTEN_SCHREIBEN)
@RateLimit(RateLimitConst.DEFAULT_LIMIT)
@Timed(name = "KontaktPersonSchuleResourceMS", unit = MetricUnits.MILLISECONDS)
public class KontaktPersonSchuleResource {

    private final KontaktPersonSchuleMapper kontaktPersonSchuleMapper;

    @Context
    private HttpServerResponse respone;

    @GET
    @NonNull
    @RolesAllowed(BerechtigungName.LISTEN_LESEN)
    public KontaktPersonSchuleListJson findAllAktivBySchule(
        @NotNull @NonNull @PathParam("schuleID") ID<Schule> schuleId) {
        return new KontaktPersonSchuleListJson(kontaktPersonSchuleMapper.findAllAktivBySchule(schuleId));
    }

    @GET
    @Path("/options")
    @RateLimit(RateLimitConst.DEFAULT_LIMIT)
    @RolesAllowed(BerechtigungName.LISTEN_LESEN)
    public KontaktPersonSchuleOptionListJson findAllOptionsActive(
        @NotNull @NonNull @PathParam("schuleID") ID<Schule> schuleId,
        @NotNull @QueryParam("excludeSchule") boolean excludeSchule
    ) {
        ID<Schule> schuleToExclude = excludeSchule ? schuleId : null;
        return new KontaktPersonSchuleOptionListJson(kontaktPersonSchuleMapper.findAllOptionsActive(schuleToExclude));
    }

    @GET
    @NonNull
    @Path("/{kontaktPersonSchuleID}")
    @RolesAllowed(BerechtigungName.LISTEN_LESEN)
    public KontaktPersonSchuleJson getKontaktPersonSchule(
        @NotNull @NonNull @PathParam("schuleID") ID<Schule> schuleId,
        @NotNull @NonNull @PathParam("kontaktPersonSchuleID") ID<KontaktPersonSchule> kontaktPersonSchuleID
    ) {
        return kontaktPersonSchuleMapper.getById(kontaktPersonSchuleID);
    }

    @POST
    @RolesAllowed(BerechtigungName.LISTEN_SCHREIBEN)
    @NonNull
    public KontaktPersonSchuleJson createKontaktPersonSchule(
        @NotNull @NonNull @PathParam("schuleID") ID<Schule> schuleId,
        @NotNull @NonNull @Valid KontaktPersonSchuleSaveJson kontaktPersonSchuleSaveJson
    ) {
        respone.setStatusCode(Status.CREATED.getStatusCode());
        return kontaktPersonSchuleMapper.create(schuleId, kontaktPersonSchuleSaveJson);
    }

    @PUT
    @Path("/{kontaktPersonSchuleID}")
    @NonNull
    @RolesAllowed(BerechtigungName.LISTEN_SCHREIBEN)
    public KontaktPersonSchuleJson updateKontaktPersonSchule(
        @NotNull @NonNull @PathParam("schuleID") ID<Schule> schuleId,
        @NotNull @NonNull @PathParam("kontaktPersonSchuleID") ID<KontaktPersonSchule> kontaktPersonSchuleID,
        @NotNull @NonNull @Valid KontaktPersonSchuleSaveJson kontaktPersonSchuleSaveJson
    ) {
        return kontaktPersonSchuleMapper.update(kontaktPersonSchuleID, kontaktPersonSchuleSaveJson);
    }

    @DELETE
    @Path("/{kontaktPersonSchuleID}")
    @RolesAllowed(BerechtigungName.LISTEN_SCHREIBEN)
    public void deleteKontaktPersonSchule(
        @NotNull @NonNull @PathParam("schuleID") ID<Schule> schuleId,
        @NotNull @NonNull @PathParam("kontaktPersonSchuleID") ID<KontaktPersonSchule> kontaktPersonSchuleID
    ) {
        kontaktPersonSchuleMapper.delete(kontaktPersonSchuleID);
    }

    @PUT
    @Path("/schule/{kontaktPersonSchuleID}")
    @RolesAllowed(BerechtigungName.LISTEN_SCHREIBEN)
    public KontaktPersonSchuleJson addKontaktPersonSchuleToSchule(
        @NotNull @NonNull @PathParam("schuleID") ID<Schule> schuleId,
        @NotNull @NonNull @PathParam("kontaktPersonSchuleID") ID<KontaktPersonSchule> kontaktPersonSchuleID
    ) {
        return kontaktPersonSchuleMapper.addToSchule(schuleId, kontaktPersonSchuleID);
    }

    @DELETE
    @Path("/schule/{kontaktPersonSchuleID}")
    @RolesAllowed(BerechtigungName.LISTEN_SCHREIBEN)
    public void removeKontaktPersonSchuleFromSchule(
        @NotNull @NonNull @PathParam("schuleID") ID<Schule> schuleId,
        @NotNull @NonNull @PathParam("kontaktPersonSchuleID") ID<KontaktPersonSchule> kontaktPersonSchuleID
    ) {
        kontaktPersonSchuleMapper.removeFromSchule(schuleId, kontaktPersonSchuleID);
    }
}
