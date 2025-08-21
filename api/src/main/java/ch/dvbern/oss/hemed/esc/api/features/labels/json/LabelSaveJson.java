package ch.dvbern.oss.hemed.esc.api.features.labels.json;

import ch.dvbern.oss.hemed.esc.api.infrastructure.optimisticlocking.SaveJsonWithOptimisticLocking;
import ch.dvbern.dvbstarter.infrastructure.stereotypes.JaxDTO;
import ch.dvbern.oss.hemed.esc.api.features.labels.types.LabelTyp;
import ch.dvbern.oss.hemed.esc.api.shared.mandant.TranslatedStringJson;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.eclipse.microprofile.openapi.annotations.media.Schema;

@Getter
@Setter
@AllArgsConstructor
@JaxDTO
public class LabelSaveJson implements SaveJsonWithOptimisticLocking {

    @Valid
    @NonNull
    @NotNull
    @Schema(required = true)
    private TranslatedStringJson name;

    @NotNull
    @NonNull
    @Schema(required = true)
    private final LabelTyp labelTyp;

    @NotNull
    @Schema(required = true)
    private boolean autoLabel = false;

    @NotNull
    @Schema(required = true)
    private boolean disabled = false;

    @NotNull
    @Schema(required = true)
    private boolean aktiv = true;

    @Schema(required = true)
    private long version;
}
