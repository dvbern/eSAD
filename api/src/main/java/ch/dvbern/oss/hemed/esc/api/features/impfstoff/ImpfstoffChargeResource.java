package ch.dvbern.oss.hemed.esc.api.features.impfstoff;

import ch.dvbern.dvbstarter.infrastructure.ratelimit.RateLimitConst;
import ch.dvbern.dvbstarter.infrastructure.stereotypes.JaxResource;
import ch.dvbern.dvbstarter.shared.openapi.OpenApiConst.Tags;
import ch.dvbern.dvbstarter.shared.types.id.ID;
import ch.dvbern.oss.hemed.esc.api.features.impfstoff.json.ImpfstoffChargeJson;
import ch.dvbern.oss.hemed.esc.api.features.impfstoff.json.ImpfstoffChargeListJson;
import ch.dvbern.oss.hemed.esc.api.features.impfstoff.json.ImpfstoffChargeSaveJson;
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
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.Response.Status;
import lombok.RequiredArgsConstructor;
import org.checkerframework.checker.nullness.qual.NonNull;
import io.micrometer.core.annotation.Counted;
import io.micrometer.core.annotation.Timed;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;

@Path("/impfstoffe/{impfstoffId}/chargen")
@JaxResource
@RequiredArgsConstructor
@Tag(name = Tags.TAG_IMPFSTOFF_CHARGEN)
@RolesAllowed(BerechtigungName.LISTEN_SCHREIBEN)
@RateLimit(RateLimitConst.DEFAULT_LIMIT)
@Timed("ImpfstoffChargeResourceMS")
public class ImpfstoffChargeResource {

    private final ImpfstoffChargeMapper impfstoffChargeMapper;

    @Context
    private HttpServerResponse response;

    @GET
    @NonNull
    @RolesAllowed({ BerechtigungName.LISTEN_ADMIN_LESEN, BerechtigungName.LISTEN_LESEN })
    public ImpfstoffChargeListJson findAllByImpfstoff(
        @NotNull @NonNull @PathParam("impfstoffId") ID<Impfstoff> impfstoffId
    ) {
        return new ImpfstoffChargeListJson(impfstoffChargeMapper.findAllByImpfstoff(impfstoffId));
    }

    @POST
    @NonNull
    @Counted("createImpfstoffChargeCount")
    @Timed("createImpfstoffChargeMS")
    @RolesAllowed(BerechtigungName.IMPFSTOFFE_SCHREIBEN)
    public ImpfstoffChargeJson createImpfstoffCharge(
        @NotNull @NonNull @PathParam("impfstoffId") ID<Impfstoff> impfstoffId,
        @NotNull @NonNull @Valid ImpfstoffChargeSaveJson impfstoffChargeSaveJson
    ) {
        response.setStatusCode(Status.CREATED.getStatusCode());
        return impfstoffChargeMapper.create(impfstoffId, impfstoffChargeSaveJson);
    }

    @DELETE
    @Path("/{impfstoffChargeId}")
    @RolesAllowed(BerechtigungName.IMPFSTOFFE_SCHREIBEN)
    public void removeImpfstoffCharge(
        @NotNull @NonNull @PathParam("impfstoffId") ID<Impfstoff> impfstoffId,
        @NotNull @NonNull @PathParam("impfstoffChargeId") ID<ImpfstoffCharge> impfstoffChargeId
    ) {
        impfstoffChargeMapper.requireConsistentPathParams(impfstoffChargeId, impfstoffId);
        impfstoffChargeMapper.remove(impfstoffChargeId);
    }

    @PUT
    @Path("/{impfstoffChargeId}")
    @RolesAllowed(BerechtigungName.IMPFSTOFFE_SCHREIBEN)
    public ImpfstoffChargeJson updateImpfstoffCharge(
        @NotNull @NonNull @PathParam("impfstoffId") ID<Impfstoff> impfstoffId,
        @NotNull @NonNull @PathParam("impfstoffChargeId") ID<ImpfstoffCharge> impfstoffChargeID,
        @NotNull @NonNull @Valid ImpfstoffChargeSaveJson impfstoffChargeSaveJson
    ) {
        impfstoffChargeMapper.requireConsistentPathParams(impfstoffChargeID, impfstoffId);
        return impfstoffChargeMapper.update(impfstoffChargeID, impfstoffChargeSaveJson);
    }
}
