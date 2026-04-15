package ch.dvbern.oss.hemed.esc.api.features.anlass.schulrelevantebeeintraechtigung.json;

import ch.dvbern.dvbstarter.infrastructure.stereotypes.JaxDTO;
import ch.dvbern.oss.hemed.esc.api.infrastructure.optimisticlocking.SaveJsonWithOptimisticLocking;
import ch.dvbern.oss.hemed.esc.api.shared.valueenhanced.ValueEnhancedJson;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import lombok.Value;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.eclipse.microprofile.openapi.annotations.media.Schema;

@Value
@AllArgsConstructor
@JaxDTO
public class UntersuchungSchulrelevanteBeeintraechtigungSaveJson implements SaveJsonWithOptimisticLocking {

    @NonNull
    @Schema(required = true)
    @Valid
    private ValueEnhancedJson freiesGehen;

    @NonNull
    @Schema(required = true)
    @Valid
    private ValueEnhancedJson ohneStuetzradVelofahren;

    @NonNull
    @Schema(required = true)
    @Valid
    private ValueEnhancedJson grafomotorik;

    @NonNull
    @Schema(required = true)
    @Valid
    private ValueEnhancedJson feinmotorik;

    @NonNull
    @Schema(required = true)
    @Valid
    private ValueEnhancedJson grobmotorikEinbeinhuepfen;

    @NonNull
    @Schema(required = true)
    @Valid
    private ValueEnhancedJson gleichgewichtEinbeinstand;

    @Nullable
    @Schema(required = false)
    private String bemerkungen;

    @Schema(required = true)
    private long version;
}
