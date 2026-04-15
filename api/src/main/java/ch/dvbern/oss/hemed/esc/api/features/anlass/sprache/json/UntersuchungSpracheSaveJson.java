package ch.dvbern.oss.hemed.esc.api.features.anlass.sprache.json;

import ch.dvbern.dvbstarter.infrastructure.stereotypes.JaxDTO;
import ch.dvbern.oss.hemed.esc.api.features.anlass.sprache.types.Sprachverstaendnis;
import ch.dvbern.oss.hemed.esc.api.features.anlass.sprache.types.Wortschatz;
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
public class UntersuchungSpracheSaveJson implements SaveJsonWithOptimisticLocking {

    @NonNull
    @Schema(required = true)
    @Valid
    private ValueEnhancedJson nachsprechen;

    @NonNull
    @Schema(required = true)
    @Valid
    private ValueEnhancedJson wortschatz;

    @NonNull
    @Schema(required = true)
    @Valid
    private ValueEnhancedJson sprachverstaendnis;

    @Nullable
    @Schema(required = false)
    private String bemerkungen;

    @Schema(required = true)
    private long version;
}
