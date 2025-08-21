package ch.dvbern.oss.hemed.esc.api.features.pub.clientconfig.json;

import ch.dvbern.dvbstarter.infrastructure.deployment.DeploymentEnvironment;
import ch.dvbern.dvbstarter.infrastructure.stereotypes.JaxDTO;
import ch.dvbern.dvbstarter.shared.types.url.URLSchema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import java.net.URL;
import org.eclipse.microprofile.openapi.annotations.media.Schema;

@JaxDTO
public record ClientDeploymentConfigJson(

    @Valid
    @NotNull
    @Schema(required = true)
    DeploymentEnvironment environment,

    @NotNull
    @Valid
    @Schema(required = true)
    String version,

    @NotNull
    @Valid
    @Schema(required = true, implementation = URLSchema.class)
    URL publicUrl


    ) {
}
