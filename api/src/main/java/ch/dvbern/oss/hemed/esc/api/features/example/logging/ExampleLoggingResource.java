package ch.dvbern.oss.hemed.esc.api.features.example.logging;

import ch.dvbern.dvbstarter.infrastructure.ratelimit.RateLimitConst;
import ch.dvbern.dvbstarter.infrastructure.stereotypes.JaxResource;
import ch.dvbern.dvbstarter.shared.openapi.OpenApiConst;
import io.smallrye.faulttolerance.api.RateLimit;
import jakarta.annotation.Nullable;
import jakarta.annotation.security.PermitAll;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.Response;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.microprofile.metrics.annotation.Counted;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;

@Path("/example/logging")
@JaxResource
@RequiredArgsConstructor
@Tag(name = OpenApiConst.Tags.EXAMPLE_LOGGING)
@PermitAll
@Slf4j
@RateLimit(RateLimitConst.DEFAULT_LIMIT)
@Counted(name = "ExampleLoggingResourceCount")
public class ExampleLoggingResource {

    @GET
    @RateLimit(RateLimitConst.DEFAULT_LIMIT)
    @PermitAll
    public Response log(
        @QueryParam("message") @Nullable String message
    ) {
        LOG.info("Logging message: {}", message);

        return Response.ok("See logfile")
            .build();
    }
}
