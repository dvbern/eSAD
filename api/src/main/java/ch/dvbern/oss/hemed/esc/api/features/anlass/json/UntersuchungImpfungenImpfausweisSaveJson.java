package ch.dvbern.oss.hemed.esc.api.features.anlass.json;

import ch.dvbern.dvbstarter.infrastructure.stereotypes.JaxDTO;
import jakarta.validation.constraints.NotNull;
import lombok.Value;
import org.eclipse.microprofile.openapi.annotations.media.Schema;

@Value
@JaxDTO
public class UntersuchungImpfungenImpfausweisSaveJson {

    @NotNull
    @Schema(required = true)
    private boolean impfausweisUnvollstaendig;

    @NotNull
    @Schema(required = true)
    private boolean impfausweisNichtVorhanden;

    @NotNull
    @Schema(required = true)
    private boolean impfausweisZweiPlusEinsSchema;
}
