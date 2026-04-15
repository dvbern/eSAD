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
import ch.dvbern.oss.hemed.esc.api.shared.config.service.ConfigService;
import io.smallrye.faulttolerance.api.RateLimit;
import jakarta.annotation.security.PermitAll;
import jakarta.annotation.security.RolesAllowed;
import jakarta.validation.Valid;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import lombok.RequiredArgsConstructor;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.eclipse.microprofile.config.Config;
import org.eclipse.microprofile.openapi.annotations.enums.SchemaType;
import org.eclipse.microprofile.openapi.annotations.media.Content;
import org.eclipse.microprofile.openapi.annotations.media.Schema;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponse;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

@Path("/dev")
@JaxResource
@RequiredArgsConstructor
@Tag(name = OpenApiConst.Tags.TAG_DEVELOPER)
@RolesAllowed(BerechtigungName.SUPPORT)
@RateLimit(RateLimitConst.DEFAULT_LIMIT)
public class DeveloperResource {

    private final AppClock appClock;
    private final ConfigService configService;
    private final Config config;

    @POST()
    @Path("/typetests/errors/runtime-exception")
    @PermitAll
    public AppFailureErrorJson triggerRuntimeException(@NonNull @Valid ExampleMsg msg) {
        throw new IllegalStateException(msg.getMsg());
    }

    @Path("/typetests/errors/failure-message")
    @POST()
    @PermitAll
    public AppFailureErrorJson triggerFailureMessage(@NonNull @Valid ExampleMsg msg) {
        throw new AppFailureException(AppFailureMessage.internalError(msg.getMsg()));
    }

    @Path("/typetests/errors/validation-message")
    @POST()
    @PermitAll
    public AppValidationErrorJson triggerValidationMessage(@NonNull @Valid ExampleMsg msg) {
        throw new AppValidationException(AppValidationMessage.invalidEmail(msg.getMsg()));
    }

    @Path("/typetests/date")
    @GET
    @PermitAll
    public ExampleDateTimeJson getDate() {
        return new ExampleDateTimeJson(
            appClock.getToday(),
            appClock.getNow(),
            appClock.offsetDateTime().now(),
            appClock.zonedDateTime().now()
        );
    }

    @Path("/typetests/enums")
    @GET
    @PermitAll
    public UnusedEnumsToGenerateJson getEnums() {
        return new UnusedEnumsToGenerateJson();
    }

    @Path("config")
    @GET
    @Produces(MediaType.TEXT_PLAIN)
    @APIResponse(content = @Content(mediaType = MediaType.TEXT_PLAIN,
        schema = @Schema(type = SchemaType.STRING, format = "string")), description = "OK", responseCode = "200")
    @RolesAllowed(BerechtigungName.SUPPORT)
    public Response getConfig() {
        if (configService.isFeatureConfigPrintEnabled()) {
            return Response.ok(
            StreamSupport.stream(config.getPropertyNames().spliterator(), false)
                .sorted()
                .map(name -> name + '=' + config.getOptionalValue(name, String.class).orElse(""))
                .collect(Collectors.joining("\n"))).build();
        }
        return Response.ok("app.allow-configprint is disabled, to print all config values set it to true").build();

    }
}
