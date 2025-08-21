package ch.dvbern.oss.hemed.esc.api.features.example.developer;

import ch.dvbern.dvbstarter.infrastructure.errorhandling.AppFailureException;
import ch.dvbern.dvbstarter.infrastructure.errorhandling.AppFailureMessage;
import ch.dvbern.dvbstarter.infrastructure.errorhandling.AppValidationException;
import ch.dvbern.dvbstarter.infrastructure.errorhandling.AppValidationMessage;
import ch.dvbern.dvbstarter.infrastructure.errorhandling.json.AppFailureErrorJson;
import ch.dvbern.dvbstarter.infrastructure.errorhandling.json.AppValidationErrorJson;
import ch.dvbern.dvbstarter.infrastructure.ratelimit.RateLimitConst;
import ch.dvbern.dvbstarter.infrastructure.stereotypes.JaxResource;
import ch.dvbern.dvbstarter.shared.clock.AppClock;
import ch.dvbern.dvbstarter.shared.openapi.OpenApiConst;
import ch.dvbern.oss.hemed.esc.api.shared.auth.BerechtigungName;
import io.smallrye.faulttolerance.api.RateLimit;
import jakarta.annotation.security.PermitAll;
import jakarta.annotation.security.RolesAllowed;
import jakarta.validation.Valid;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import lombok.RequiredArgsConstructor;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;

@Path("/typetests")
@JaxResource
@RequiredArgsConstructor
@Tag(name = OpenApiConst.Tags.TAG_DEVELOPER)
@RolesAllowed(BerechtigungName.SUPPORT)
@RateLimit(RateLimitConst.DEFAULT_LIMIT)
public class DeveloperResource {

    private final AppClock appClock;

    @POST()
    @Path("errors/runtime-exception")
    @PermitAll
    public AppFailureErrorJson triggerRuntimeException(@NonNull @Valid ExampleMsg msg) {
        throw new IllegalStateException(msg.getMsg());
    }

    @Path("errors/failure-message")
    @POST()
    @PermitAll
    public AppFailureErrorJson triggerFailureMessage(@NonNull @Valid ExampleMsg msg) {
        throw new AppFailureException(AppFailureMessage.internalError(msg.getMsg()));
    }

    @Path("errors/validation-message")
    @POST()
    @PermitAll
    public AppValidationErrorJson triggerValidationMessage(@NonNull @Valid ExampleMsg msg) {
        throw new AppValidationException(AppValidationMessage.invalidEmail(msg.getMsg()));
    }

    @Path("date")
    @GET
    @PermitAll
    public ExampleDateTimeJson getDate() {
        return new ExampleDateTimeJson(
            appClock.localDateTime().now().toLocalDate(),
            appClock.localDateTime().now(),
            appClock.offsetDateTime().now(),
            appClock.zonedDateTime().now()
        );
    }
}
