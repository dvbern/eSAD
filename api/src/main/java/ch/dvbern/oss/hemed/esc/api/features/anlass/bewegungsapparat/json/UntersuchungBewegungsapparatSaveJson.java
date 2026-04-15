package ch.dvbern.oss.hemed.esc.api.features.anlass.bewegungsapparat.json;

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
public class UntersuchungBewegungsapparatSaveJson implements SaveJsonWithOptimisticLocking {

    @NonNull
    @Schema(required = true)
    @Valid
    private ValueEnhancedJson skoliose;

    @NonNull
    @Schema(required = true)
    @Valid
    private ValueEnhancedJson haltungsschwaeche;

    @NonNull
    @Schema(required = true)
    @Valid
    private ValueEnhancedJson verkuerzungHamstringmuskulatur;

    @Nullable
    @Schema(required = false)
    private String bemerkungen;

    @Schema(required = true)
    private long version;
}
