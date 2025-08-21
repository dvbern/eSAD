package ch.dvbern.oss.hemed.esc.api.features.infomaterial;

import ch.dvbern.dvbstarter.infrastructure.ratelimit.RateLimitConst;
import ch.dvbern.dvbstarter.infrastructure.stereotypes.JaxResource;
import ch.dvbern.dvbstarter.shared.openapi.OpenApiConst;
import ch.dvbern.dvbstarter.shared.types.id.ID;
import ch.dvbern.oss.hemed.esc.api.features.infomaterial.json.InfomaterialJson;
import ch.dvbern.oss.hemed.esc.api.features.infomaterial.json.InfomaterialListJson;
import ch.dvbern.oss.hemed.esc.api.features.infomaterial.json.InfomaterialSaveJson;
import ch.dvbern.oss.hemed.esc.api.shared.auth.BerechtigungName;
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
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.Response.Status;
import lombok.RequiredArgsConstructor;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.eclipse.microprofile.metrics.MetricUnits;
import org.eclipse.microprofile.metrics.annotation.Timed;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;

@Path("/infomaterialien")
@JaxResource
@RequiredArgsConstructor
@Tag(name = OpenApiConst.Tags.TAG_INFOMATERIAL)
@RolesAllowed(BerechtigungName.LISTEN_SCHREIBEN)
@RateLimit(RateLimitConst.DEFAULT_LIMIT)
@Timed(name = "InfomaterialResourceMS", unit = MetricUnits.MILLISECONDS)
public class InfomaterialResource {

    private final InfomaterialMapper infomaterialMapper;

    @Context
    private HttpServerResponse response;

    @GET
    @RateLimit(RateLimitConst.DEFAULT_LIMIT)
    @RolesAllowed(BerechtigungName.LISTEN_LESEN)
    public InfomaterialListJson findAllInfomaterialien(
        @NotNull @QueryParam("onlyActive") boolean onlyActive
    ) {
        return new InfomaterialListJson(infomaterialMapper.findAll(onlyActive));
    }

    @POST
    @RolesAllowed(BerechtigungName.LISTEN_SCHREIBEN)
    @NonNull
    public InfomaterialJson createInfomaterial(
        @NotNull @NonNull @Valid InfomaterialSaveJson createInfomaterialJson
    ) {
        response.setStatusCode(Status.CREATED.getStatusCode());
        return infomaterialMapper.create(createInfomaterialJson);
    }

    @PUT
    @Path("/{infomaterialId}")
    @NonNull
    @RolesAllowed(BerechtigungName.LISTEN_SCHREIBEN)
    public InfomaterialJson updateInfomaterial(
        @NotNull @NonNull @PathParam("infomaterialId") ID<Infomaterial> infomaterialID,
        @NotNull @NonNull @Valid InfomaterialSaveJson updateInfomaterialJson
    ) {
        return infomaterialMapper.update(infomaterialID, updateInfomaterialJson);
    }

    @DELETE
    @Path("/{infomaterialId}")
    @RolesAllowed(BerechtigungName.LISTEN_SCHREIBEN)
    public void removeInfomaterial(@NotNull @NonNull @PathParam("infomaterialId") ID<Infomaterial> infomaterialID) {
        infomaterialMapper.remove(infomaterialID);
    }
}
