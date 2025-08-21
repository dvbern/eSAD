package ch.dvbern.oss.hemed.esc.api.shared.aufgabe.json;

import ch.dvbern.dvbstarter.infrastructure.stereotypes.JaxDTO;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Value;
import org.eclipse.microprofile.openapi.annotations.media.Schema;

@Value
@AllArgsConstructor
@JaxDTO
public class NumOfOffeneFaelligeAufgabeJson {

    @NotNull
    @Schema(required = true)
    private final long numOfOffeneFaelligeAufgaben;
}
