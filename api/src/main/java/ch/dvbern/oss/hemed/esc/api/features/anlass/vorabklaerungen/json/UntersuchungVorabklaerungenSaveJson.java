package ch.dvbern.oss.hemed.esc.api.features.anlass.vorabklaerungen.json;

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
public class UntersuchungVorabklaerungenSaveJson {

    @Nullable
    @Valid
    @Schema(required = false)
    private ValueEnhancedJson fragebogenStatus;

    @Nullable
    @Valid
    @Schema(required = false)
    private ValueEnhancedJson einverstaendnisImpfungen;

    @Nullable
    @Valid
    @Schema(required = false)
    private ValueEnhancedJson chronischeKrankheiten;

    @Schema(required = true)
    private long version;
}
