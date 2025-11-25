package ch.dvbern.oss.hemed.esc.api.features.anlass.hoeren.json;

import ch.dvbern.dvbstarter.infrastructure.stereotypes.JaxDTO;
import ch.dvbern.oss.hemed.esc.api.features.anlass.hoeren.types.Dezibel;
import ch.dvbern.oss.hemed.esc.api.infrastructure.optimisticlocking.SaveJsonWithOptimisticLocking;
import lombok.AllArgsConstructor;
import lombok.Value;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.eclipse.microprofile.openapi.annotations.media.Schema;

@Value
@AllArgsConstructor
@JaxDTO
public class AudiogrammSaveJson implements SaveJsonWithOptimisticLocking {

    @Nullable
    @Schema(required = false)
    private Dezibel dbHz250;

    @Nullable
    @Schema(required = false)
    private Dezibel dbHz500;

    @Nullable
    @Schema(required = false)
    private Dezibel dbHz1000;

    @Nullable
    @Schema(required = false)
    private Dezibel dbHz2000;

    @Nullable
    @Schema(required = false)
    private Dezibel dbHz4000;

    @Nullable
    @Schema(required = false)
    private Dezibel dbHz6000;

    @Nullable
    @Schema(required = false)
    private Dezibel dbHz8000;

    @Schema(required = true)
    private long version;

}
