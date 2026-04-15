package ch.dvbern.oss.hemed.esc.api.features.impfstoff;

import ch.dvbern.dvbstarter.infrastructure.ratelimit.RateLimitConst;
import ch.dvbern.dvbstarter.infrastructure.stereotypes.JaxResource;
import ch.dvbern.dvbstarter.shared.openapi.OpenApiConst.Tags;
import ch.dvbern.dvbstarter.shared.types.id.ID;
import ch.dvbern.oss.hemed.esc.api.features.impfstoff.json.ImpfstoffImpfstoffChargeDataJson;
import ch.dvbern.oss.hemed.esc.api.features.impfstoff.json.ImpfstoffJson;
import ch.dvbern.oss.hemed.esc.api.features.impfstoff.json.ImpfstoffSaveJson;
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
import io.micrometer.core.annotation.Counted;
import io.micrometer.core.annotation.Timed;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;

@Path("/impfstoffe")
@JaxResource
@RequiredArgsConstructor
@Tag(name = Tags.TAG_IMPFSTOFFE)
@RolesAllowed(BerechtigungName.LISTEN_SCHREIBEN)
@RateLimit(RateLimitConst.DEFAULT_LIMIT)
@Timed("ImpfstoffResourceMS")
public class ImpfstoffResource {

    private final ImpfstoffMapper impfstoffMapper;

    @Context
    private HttpServerResponse response;

    @GET
    @RateLimit(RateLimitConst.DEFAULT_LIMIT)
    @RolesAllowed(BerechtigungName.IMPFSTOFFE_LESEN)
    public ImpfstoffImpfstoffChargeDataJson findAllImpfstoffeAndChargen() {
        return impfstoffMapper.findAllWithChargen();
    }

    @POST
    @NonNull
    @Counted("createImpfstoffCount")
    @Timed("createImpfstoffMS")
    @RolesAllowed(BerechtigungName.IMPFSTOFFE_SCHREIBEN)
    public ImpfstoffJson createImpfstoff(
        @NotNull @NonNull @Valid ImpfstoffSaveJson impfstoffSaveJson
    ) {
        response.setStatusCode(Status.CREATED.getStatusCode());
        return impfstoffMapper.create(impfstoffSaveJson);
    }

    @PUT
    @Path("/{impfstoffeId}")
    @NonNull
    @RolesAllowed(BerechtigungName.IMPFSTOFFE_SCHREIBEN)
    public ImpfstoffJson updateImpfstoff(
        @NotNull @NonNull @PathParam("impfstoffeId") ID<Impfstoff> impfstoffID,
        @NotNull @NonNull @Valid ImpfstoffSaveJson impfstoffSaveJson
    ) {
        return impfstoffMapper.update(impfstoffID, impfstoffSaveJson);
    }

    @PUT
    @Path("/{impfstoffId}/impfstoffaktivstatus")
    @RateLimit(RateLimitConst.DEFAULT_LIMIT)
    @RolesAllowed(BerechtigungName.IMPFSTOFFE_SCHREIBEN)
    public ImpfstoffJson updateImpfstoffAktivStatus(
        @NotNull @NonNull @PathParam("impfstoffId") ID<Impfstoff> impfstoffId,
        @NotNull @NonNull @Valid EntityAktivStatusSaveJson saveJson
    ) {
        return impfstoffMapper.changeImpfstoffAktivStatus(impfstoffId, saveJson);
    }

    @DELETE
    @Path("/{impfstoffeId}")
    @RolesAllowed(BerechtigungName.IMPFSTOFFE_SCHREIBEN)
    public void removeImpfstoff(@NotNull @NonNull @PathParam("impfstoffeId") ID<Impfstoff> impfstoffID) {
        impfstoffMapper.remove(impfstoffID);
    }
}
