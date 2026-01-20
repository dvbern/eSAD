package ch.dvbern.oss.hemed.esc.api.features.fileingress;

import java.util.List;

import ch.dvbern.dvbstarter.infrastructure.errorhandling.json.AppValidationErrorJson;
import ch.dvbern.dvbstarter.infrastructure.stereotypes.JaxDTO;
import lombok.Data;
import org.eclipse.microprofile.openapi.annotations.media.Schema;

@JaxDTO
@Data
public class FileIngressResultJson {

    @Schema(nullable = false)
    private final FileIngressStatus status;

    @Schema(nullable = false)
    private final String successMessage;

    @Schema(nullable = false)
    private final List<AppValidationErrorJson> errors;
}
