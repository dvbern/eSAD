package ch.dvbern.oss.hemed.esc.api.features.kinder.json;

import ch.dvbern.dvbstarter.infrastructure.stereotypes.JaxDTO;
import ch.dvbern.oss.hemed.esc.api.infrastructure.optimisticlocking.SaveJsonWithOptimisticLocking;
import ch.dvbern.oss.hemed.esc.api.shared.anlass.anlassflags.AnlassFlagsJson;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Value;
import org.eclipse.microprofile.openapi.annotations.media.Schema;

@Value
@AllArgsConstructor
@JaxDTO
public class NachkontrolleSaveJson implements SaveJsonWithOptimisticLocking {

    @NotNull
    @Schema(required = true)
    @Valid
    private AnlassFlagsJson anlassFlags;

    @Schema(required = true)
    private long version;
}
