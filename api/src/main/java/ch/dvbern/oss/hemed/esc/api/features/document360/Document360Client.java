package ch.dvbern.oss.hemed.esc.api.features.document360;

import ch.dvbern.oss.hemed.esc.api.features.document360.json.Document360CodeRequestJson;
import ch.dvbern.oss.hemed.esc.api.features.document360.json.Document360CodeResponseJson;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.HeaderParam;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import org.eclipse.microprofile.rest.client.inject.RegisterRestClient;

/**
 * MicroProfile REST client for Document360 Identity.
 *
 * <p>The configured URL ({@code quarkus.rest-client.document360-api.url}) is the full
 * {@code /jwt/generateCode} endpoint, so this method has no extra {@code @Path}.</p>
 */
@RegisterRestClient(configKey = "document360-api")
public interface Document360Client {

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    Document360CodeResponseJson generateCode(
        @HeaderParam("Authorization") String authHeader,
        Document360CodeRequestJson request
    );
}
