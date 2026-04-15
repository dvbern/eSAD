package ch.dvbern.oss.hemed.esc.api.features.anlass.abschluss.json;

import ch.dvbern.dvbstarter.infrastructure.stereotypes.JaxDTO;
import ch.dvbern.oss.hemed.esc.api.shared.valueenhanced.ValueEnhancedJson;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import lombok.Value;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.eclipse.microprofile.openapi.annotations.media.Schema;

@Value
@AllArgsConstructor
@JaxDTO
public class UntersuchungAbschlussSaveJson {

    @Nullable
    @Valid
    @Schema(required = false)
    private ValueEnhancedJson ueberweisungErfolgt;

    @Nullable
    @Valid
    @Schema(required = false)
    private ValueEnhancedJson aerztlicheValidierungErforderlich;

    @Nullable
    @Valid
    @Schema(required = false)
    private ValueEnhancedJson beratungDurchgefuehrt;

    @Nullable
    @Schema(required = false)
    private String beurteilungEmpfohleneMassnahmen;

    @Schema(required = true)
    private long version;
}
