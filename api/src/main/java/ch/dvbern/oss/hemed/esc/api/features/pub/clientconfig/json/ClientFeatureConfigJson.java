package ch.dvbern.oss.hemed.esc.api.features.pub.clientconfig.json;

import ch.dvbern.dvbstarter.infrastructure.stereotypes.JaxDTO;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import org.eclipse.microprofile.openapi.annotations.media.Schema;

@JaxDTO
public record ClientFeatureConfigJson(

    @Valid
    @NotNull
    @Schema(required = true)
    boolean timetravelEnabled


    ) {
}
