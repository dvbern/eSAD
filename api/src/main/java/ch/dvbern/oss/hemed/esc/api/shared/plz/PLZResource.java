package ch.dvbern.oss.hemed.esc.api.shared.plz;

import ch.dvbern.dvbstarter.infrastructure.ratelimit.RateLimitConst;
import ch.dvbern.dvbstarter.infrastructure.stereotypes.JaxResource;
import ch.dvbern.dvbstarter.shared.openapi.OpenApiConst;
import ch.dvbern.oss.hemed.esc.api.shared.auth.BerechtigungName;
import ch.dvbern.oss.hemed.esc.api.shared.plz.json.PLZDataListJson;
import io.smallrye.faulttolerance.api.RateLimit;
import jakarta.annotation.security.PermitAll;
import jakarta.annotation.security.RolesAllowed;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.Response;
import lombok.RequiredArgsConstructor;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.eclipse.microprofile.metrics.MetricUnits;
import org.eclipse.microprofile.metrics.annotation.Timed;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;

@Path("/plz")
@JaxResource
@RequiredArgsConstructor
@Tag(name = OpenApiConst.Tags.TAG_PLZ)
@RolesAllowed(BerechtigungName.SUPPORT)
@RateLimit(RateLimitConst.DEFAULT_LIMIT)
@Timed(name = "PLZResourceMS", unit = MetricUnits.MILLISECONDS)
public class PLZResource {

    private final PLZDataMapper plzDataMapper;

    @POST
    @Path("/importplz")
    @RolesAllowed(BerechtigungName.SUPPORT)
    public Response importPlzFromCsv() {
        plzDataMapper.importPlzKantonIntoDatabase();
        return Response.ok().build();
    }

    @GET
    @PermitAll
    public PLZDataListJson findPLZData(
        @QueryParam("plzdata") @Nullable String plzData
    ) {
        return plzDataMapper.findOrteForPLZ(plzData);
    }
}
