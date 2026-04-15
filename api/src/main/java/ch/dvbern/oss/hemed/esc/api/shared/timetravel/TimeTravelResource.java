package ch.dvbern.oss.hemed.esc.api.shared.timetravel;

import java.time.LocalDate;

import ch.dvbern.dvbstarter.infrastructure.ratelimit.RateLimitConst;
import ch.dvbern.dvbstarter.infrastructure.stereotypes.JaxResource;
import ch.dvbern.dvbstarter.shared.openapi.OpenApiConst.Tags;
import ch.dvbern.oss.hemed.esc.api.shared.auth.BerechtigungName;
import io.micrometer.core.annotation.Timed;
import io.smallrye.faulttolerance.api.RateLimit;
import jakarta.annotation.security.PermitAll;
import jakarta.annotation.security.RolesAllowed;
import jakarta.validation.Valid;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.PUT;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import lombok.RequiredArgsConstructor;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;

@Path("/timetravel")
@JaxResource
@RequiredArgsConstructor
@Tag(name = Tags.TAG_TIMETRAVEL)
@RateLimit(RateLimitConst.DEFAULT_LIMIT)
@Timed("TimeTravelResourceMS")
@PermitAll
public class TimeTravelResource {

    private final TimeTravelService timeTravelService;

    @GET
    @Path("/now")
    @Produces("application/json")
    @NonNull
    @PermitAll
    public TimeTravelJson getNow() {
        LocalDate now = timeTravelService.getNow();
        boolean isTimeTravelling = timeTravelService.isTimeTravelling();
        return new TimeTravelJson(now, isTimeTravelling);
    }

    @PUT
    @Path("/now")
    @Produces("application/json")
    @NonNull
    @RolesAllowed(BerechtigungName.SUPPORT)
    public TimeTravelJson setNow(@NonNull @Valid TimeTravelJson dto) {
        LocalDate now = timeTravelService.setNow(dto.getNow());
        boolean isTimeTravelling = timeTravelService.isTimeTravelling();
        return new TimeTravelJson(now, isTimeTravelling);
    }

    @PUT
    @Path("/now/reset")
    @Produces("application/json")
    @NonNull
    @RolesAllowed(BerechtigungName.SUPPORT)
    public TimeTravelJson resetNow() {
        LocalDate now = timeTravelService.resetNow();
        boolean isTimeTravelling = timeTravelService.isTimeTravelling();
        return new TimeTravelJson(now, isTimeTravelling);
    }
}
