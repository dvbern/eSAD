package ch.dvbern.oss.hemed.esc.api.features.anlass.json;

import ch.dvbern.dvbstarter.infrastructure.stereotypes.JaxDTO;
import ch.dvbern.oss.hemed.esc.api.features.impfstoff.types.Krankheit;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Value;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.eclipse.microprofile.openapi.annotations.media.Schema;

@Value
@AllArgsConstructor
@JaxDTO
@Schema(description = "Business data for a Anlass")
public class UntersuchungImpfungKrankheitImmunitaetJson {

    @NotNull
    @NonNull
    @Schema(required = true)
    private Krankheit krankheit;

    @NotNull
    @Schema(required = true)
    private boolean immunitaetGenuegend;

}
