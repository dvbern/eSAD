package ch.dvbern.oss.hemed.esc.api.features.document360;

import java.net.URI;
import jakarta.annotation.security.RolesAllowed;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.core.Response;

import ch.dvbern.dvbstarter.infrastructure.ratelimit.RateLimitConst;
import ch.dvbern.dvbstarter.infrastructure.stereotypes.JaxResource;
import ch.dvbern.oss.hemed.esc.api.shared.auth.BerechtigungName;

import io.smallrye.faulttolerance.api.RateLimit;
import lombok.RequiredArgsConstructor;
import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponse;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;

/**
 * REST entry point for Document360 JWT SSO.
 *
 * <p>The browser is sent to {@code GET /api/document360/authenticate}. This resource does not return JSON:
 * it answers with {@code 303 See Other} so the user lands on the Document360 knowledge base already signed in.</p>
 *
 * @see Document360Service
 * @see <a href="https://docs.document360.com/docs/configuring-the-jwt-sso">Document360 JWT SSO</a>
 */
@Path("/document360")
@JaxResource
@RequiredArgsConstructor
@Tag(name = "Document360 SSO")
@RolesAllowed(BerechtigungName.EIGENE_BERECHTIGUNGEN_LESEN)
@RateLimit(RateLimitConst.DEFAULT_LIMIT)
public class Document360Resource {

    private final Document360Service document360Service;

    /**
     * Starts JWT SSO for the currently authenticated ESC user and redirects to Document360.
     *
     * @return {@code 303 See Other} with {@code Location} pointing at
     *     {@code {kb-login-url}?code={one-time-sso-code}}
     */
    @GET
    @Path("/authenticate")
    @Operation(
        summary = "Start Document360 JWT SSO",
        description = "Maps the current user's Fachrollen to Document360 reader groups, "
            + "requests a one-time SSO code from Document360 Identity, and redirects the browser "
            + "to the knowledge-base login URL. The code is short-lived and must not be stored."
    )
    @APIResponse(responseCode = "303", description = "Redirect to Document360 knowledge base with SSO code")
    @RateLimit(RateLimitConst.DEFAULT_LIMIT)
    @RolesAllowed("**")
    public Response authenticate() {
        URI redirectUri = document360Service.authenticateAndGetRedirectUri();
        return Response.seeOther(redirectUri).build();
    }
}
