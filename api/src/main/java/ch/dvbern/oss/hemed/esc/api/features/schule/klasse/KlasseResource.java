package ch.dvbern.oss.hemed.esc.api.features.schule.klasse;

import ch.dvbern.dvbstarter.infrastructure.ratelimit.RateLimitConst;
import ch.dvbern.dvbstarter.infrastructure.stereotypes.JaxResource;
import ch.dvbern.dvbstarter.shared.openapi.OpenApiConst.Tags;
import ch.dvbern.dvbstarter.shared.types.id.ID;
import ch.dvbern.oss.hemed.esc.api.features.schule.klasse.json.KlasseJson;
import ch.dvbern.oss.hemed.esc.api.features.schule.klasse.json.KlasseListJson;
import ch.dvbern.oss.hemed.esc.api.features.schule.klasse.json.KlasseSaveJson;
import ch.dvbern.oss.hemed.esc.api.shared.auth.BerechtigungName;
import ch.dvbern.oss.hemed.esc.api.shared.types.EntityAktivStatusSaveJson;
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
import org.eclipse.microprofile.metrics.annotation.Counted;
import org.eclipse.microprofile.metrics.annotation.Timed;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;

@Path("/klassen")
@JaxResource
@RequiredArgsConstructor
@Tag(name = Tags.TAG_KLASSEN)
@RolesAllowed(BerechtigungName.LISTEN_SCHREIBEN)
@RateLimit(RateLimitConst.DEFAULT_LIMIT)
@Timed(name = "KlasseResourceMS", unit = MetricUnits.MILLISECONDS)
public class KlasseResource {

    private final KlasseMapper klasseMapper;

    @Context
    private HttpServerResponse response;

    @GET
    @RateLimit(RateLimitConst.DEFAULT_LIMIT)
    @RolesAllowed({ BerechtigungName.LISTEN_ADMIN_LESEN, BerechtigungName.LISTEN_LESEN })
    public KlasseListJson findAllKlassen() {
        return new KlasseListJson(klasseMapper.findAll());
    }

    @GET
    @Path("/{klasseId}")
    @RateLimit(RateLimitConst.DEFAULT_LIMIT)
    @RolesAllowed({ BerechtigungName.LISTEN_ADMIN_LESEN, BerechtigungName.LISTEN_LESEN })
    public KlasseJson findKlasse(
        @NonNull @NotNull @PathParam("klasseId") ID<Klasse> klasseId
    ) {
        return klasseMapper.getById(klasseId);
    }

    @PUT
    @Path("/{klasseId}")
    @NonNull
    @RolesAllowed(BerechtigungName.LISTEN_SCHREIBEN)
    public KlasseJson updateKlasse(
        @NotNull @NonNull @PathParam("klasseId") ID<Klasse> klasseID,
        @NotNull @NonNull @Valid KlasseSaveJson klasseJson
    ) {
        return klasseMapper.update(klasseID, klasseJson);
    }

    @PUT
    @Path("/{klasseId}/klasseaktivstatus")
    @RateLimit(RateLimitConst.DEFAULT_LIMIT)
    @RolesAllowed(BerechtigungName.LISTEN_SCHREIBEN)
    public KlasseJson updateKlasseAktivStatus(
        @NotNull @NonNull @PathParam("klasseId") ID<Klasse> klasseId,
        @NotNull @NonNull @Valid EntityAktivStatusSaveJson saveJson
    ) {
        return klasseMapper.changeKlasseAktivStatus(klasseId, saveJson);
    }

    @POST
    @NonNull
    @Counted(name = "createKlasseCount")
    @Timed(name = "createKlasseMS", unit = MetricUnits.MILLISECONDS)
    @RolesAllowed(BerechtigungName.LISTEN_SCHREIBEN)
    public KlasseJson createKlasse(
        @NotNull @NonNull @Valid KlasseSaveJson klasseJson
    ) {
        response.setStatusCode(Status.CREATED.getStatusCode());
        return klasseMapper.create(klasseJson);
    }

    @DELETE
    @Path("/{klasseId}")
    @RolesAllowed(BerechtigungName.LISTEN_SCHREIBEN)
    public void removeKlasse(@NotNull @NonNull @PathParam("klasseId") ID<Klasse> klasseId) {
        klasseMapper.remove(klasseId);
    }
}
