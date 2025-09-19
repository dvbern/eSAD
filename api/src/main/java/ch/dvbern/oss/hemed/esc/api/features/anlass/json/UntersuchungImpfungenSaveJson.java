package ch.dvbern.oss.hemed.esc.api.features.anlass.json;

import ch.dvbern.dvbstarter.infrastructure.stereotypes.JaxDTO;
import ch.dvbern.oss.hemed.esc.api.infrastructure.optimisticlocking.SaveJsonWithOptimisticLocking;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.Value;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.eclipse.microprofile.openapi.annotations.media.Schema;

@Value
@JaxDTO
public class UntersuchungImpfungenSaveJson implements SaveJsonWithOptimisticLocking {

    @NotNull
    @NonNull
    @Valid
    @Schema(required = true)
    private final UntersuchungImpfungenImpfausweisSaveJson impfausweis;

    @NotNull
    @NonNull
    @Valid
    @Schema(required = true)
    private final UntersuchungImpfungenChecklisteSaveJson checkliste;

    @NotNull
    @NonNull
    @Valid
    @Schema(required = true)
    private final UntersuchungImpfungenDoppelkontrolleSaveJson doppelkontrolle;

    @Schema(required = true)
    private long version;
}
