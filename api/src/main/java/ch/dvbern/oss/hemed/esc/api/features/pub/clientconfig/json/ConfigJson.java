package ch.dvbern.oss.hemed.esc.api.features.pub.clientconfig.json;

import ch.dvbern.dvbstarter.infrastructure.stereotypes.JaxDTO;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import org.eclipse.microprofile.openapi.annotations.media.Schema;

@JaxDTO
public record ConfigJson(

    @Valid
    @NotNull
    @Schema(required = true)
    ClientDeploymentConfigJson clientDeploymentConfigJson,


    @Valid
    @NotNull
    @Schema(required = true)
    ClientAuthConfigJson clientAuthConfig,

    @Valid
    @NotNull
    @Schema(required = true)
    ClientFeatureConfigJson featureConfigJson

) {
}
