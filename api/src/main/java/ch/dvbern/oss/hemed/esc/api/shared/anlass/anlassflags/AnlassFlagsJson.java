package ch.dvbern.oss.hemed.esc.api.shared.anlass.anlassflags;

import ch.dvbern.dvbstarter.infrastructure.stereotypes.JaxDTO;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Value;
import org.eclipse.microprofile.openapi.annotations.media.Schema;

@Value
@AllArgsConstructor
@JaxDTO
public class AnlassFlagsJson {

    @NotNull
    @Schema(required = true)
    private boolean nachkontrolleSehen;

    @NotNull
    @Schema(required = true)
    private boolean nachkontrolleHoeren;

    @NotNull
    @Schema(required = true)
    private boolean nachkontrolleGroesseGewicht;

    @NotNull
    @Schema(required = true)
    private boolean nachkontrolleSprache;

    @NotNull
    @Schema(required = true)
    private boolean nachkontrolleMotorik;

    @NotNull
    @Schema(required = true)
    private boolean nachkontrolleAnderes;
}
