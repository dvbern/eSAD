package ch.dvbern.oss.hemed.esc.api.features.example;

import ch.dvbern.dvbstarter.infrastructure.ratelimit.RateLimitConst;
import ch.dvbern.dvbstarter.infrastructure.stereotypes.JaxResource;
import ch.dvbern.dvbstarter.shared.openapi.OpenApiConst;
import ch.dvbern.oss.hemed.esc.api.shared.auth.BerechtigungName;
import io.smallrye.faulttolerance.api.RateLimit;
import io.vertx.core.http.HttpServerResponse;
import jakarta.annotation.security.RolesAllowed;
import jakarta.validation.Valid;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.core.Context;
import lombok.RequiredArgsConstructor;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;

@Path("/example")
@JaxResource
@RequiredArgsConstructor
@Tag(name = OpenApiConst.Tags.TAG_BENUTZER)
@RolesAllowed(BerechtigungName.SUPPORT)
@RateLimit(RateLimitConst.DEFAULT_LIMIT)
public class ExampleResource {

    @Context
    private HttpServerResponse response;

    @POST
    @RolesAllowed(BerechtigungName.SUPPORT)
    public ExampleJson createExample(@Valid CreateExampleJson createExampleJson) {
        response.setStatusCode(201);
        return new ExampleJson(
            createExampleJson.getExample(),
            createExampleJson.getEmail(),
            createExampleJson.getPhoneNumber()
        );
    }
}
