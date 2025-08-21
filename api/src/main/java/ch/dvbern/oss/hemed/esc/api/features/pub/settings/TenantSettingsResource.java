package ch.dvbern.oss.hemed.esc.api.features.pub.settings;

import ch.dvbern.dvbstarter.infrastructure.ratelimit.RateLimitConst;
import ch.dvbern.dvbstarter.infrastructure.stereotypes.JaxResource;
import ch.dvbern.dvbstarter.shared.openapi.OpenApiConst.Tags;
import io.smallrye.faulttolerance.api.RateLimit;
import jakarta.annotation.security.PermitAll;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import lombok.RequiredArgsConstructor;
import org.eclipse.microprofile.metrics.MetricUnits;
import org.eclipse.microprofile.metrics.annotation.Timed;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;

@Path("/tenant-settings")
@JaxResource
@RequiredArgsConstructor
@Tag(name = Tags.TAG_SETTINGS)
@PermitAll
@RateLimit(RateLimitConst.DEFAULT_LIMIT)
@Timed(name = "TenantSettingsResourceMS", unit = MetricUnits.MILLISECONDS)
public class TenantSettingsResource {

    private final TenantSettingsMapper tenantSettingsMapper;

    @GET
    @Path("/self")
    @RateLimit(RateLimitConst.DEFAULT_LIMIT)
    @PermitAll
    public TenantSettingsJson getTenantSettings() {
        return tenantSettingsMapper.getTenantSettings();
    }

}
