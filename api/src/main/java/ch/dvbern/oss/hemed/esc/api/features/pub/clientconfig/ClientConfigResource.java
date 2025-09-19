package ch.dvbern.oss.hemed.esc.api.features.pub.clientconfig;

import ch.dvbern.dvbstarter.infrastructure.ratelimit.RateLimitConst;
import ch.dvbern.dvbstarter.infrastructure.stereotypes.JaxResource;
import ch.dvbern.dvbstarter.shared.openapi.OpenApiConst;
import ch.dvbern.oss.hemed.esc.api.features.pub.clientconfig.json.ClientAuthConfigJson;
import ch.dvbern.oss.hemed.esc.api.features.pub.clientconfig.json.ClientDeploymentConfigJson;
import ch.dvbern.oss.hemed.esc.api.features.pub.clientconfig.json.ClientFeatureConfigJson;
import ch.dvbern.oss.hemed.esc.api.features.pub.clientconfig.json.ConfigJson;
import ch.dvbern.oss.hemed.esc.api.shared.config.service.ConfigService;
import io.smallrye.faulttolerance.api.RateLimit;
import jakarta.annotation.security.PermitAll;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import lombok.RequiredArgsConstructor;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;

@JaxResource
@Path("/pub/client-config")
@RequiredArgsConstructor
@Tag(name = OpenApiConst.Tags.CLIENT_CONFIG)
@PermitAll
@RateLimit(RateLimitConst.DEFAULT_LIMIT)
public class ClientConfigResource {

    private final ConfigService configService;

    @GET
    @Path("/config")
    @RateLimit(RateLimitConst.DEFAULT_LIMIT)
    @PermitAll
    public ConfigJson config() {

        return new ConfigJson(
            new ClientDeploymentConfigJson(
                configService.getEnvironment(),
                configService.getVersion(),
                configService.getPublicUrl()),

            new ClientAuthConfigJson(
                configService.getClientAuthConfigClientId(),
                configService.getClientAuthConfigRealm(),
                configService.getClientAuthConfigAuthUrl(),
                configService.getClientAuthConfigLogoutUrl().orElse(null)),

            new ClientFeatureConfigJson(
                configService.isFeatureTimeTravelEndpointEnabled())
        );
    }
}
