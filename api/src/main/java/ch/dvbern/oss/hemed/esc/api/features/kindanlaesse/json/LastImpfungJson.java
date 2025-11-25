package ch.dvbern.oss.hemed.esc.api.features.kindanlaesse.json;

import java.time.LocalDateTime;

import ch.dvbern.dvbstarter.infrastructure.stereotypes.JaxDTO;
import ch.dvbern.dvbstarter.shared.types.id.ID;
import ch.dvbern.oss.hemed.esc.api.features.anlass.Anlass;
import ch.dvbern.oss.hemed.esc.api.features.anlass.einladung.Einladung;
import lombok.AllArgsConstructor;
import lombok.Value;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.eclipse.microprofile.openapi.annotations.media.Schema;

@Value
@AllArgsConstructor
@JaxDTO
public class LastImpfungJson {
    @Nullable
    @Schema(required = false)
    private final LocalDateTime timestampLastManualChange;

    @Nullable
    @Schema(required = false)
    private final ID<Anlass> anlassId;

    @Nullable
    @Schema(required = false)
    private final ID<Einladung> einladungId;
}
