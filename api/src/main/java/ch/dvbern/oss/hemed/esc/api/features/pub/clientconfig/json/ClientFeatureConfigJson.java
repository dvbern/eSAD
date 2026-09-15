package ch.dvbern.oss.hemed.esc.api.features.pub.clientconfig.json;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;

import ch.dvbern.dvbstarter.infrastructure.stereotypes.JaxDTO;

import org.eclipse.microprofile.openapi.annotations.media.Schema;

@JaxDTO
public record ClientFeatureConfigJson(

    @Valid
    @NotNull
    @Schema(required = true)
    boolean timetravelEnabled,

    @NotNull
    @Schema(required = true, description = "Maximum number of files that are included in a single bulk archive download zip.")
    int archiveBulkDownloadMaxFiles

    ) {
}
