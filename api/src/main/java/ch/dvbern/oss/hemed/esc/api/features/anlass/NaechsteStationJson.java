package ch.dvbern.oss.hemed.esc.api.features.anlass;

import java.time.LocalDateTime;

import ch.dvbern.dvbstarter.infrastructure.stereotypes.JaxDTO;
import ch.dvbern.oss.hemed.esc.api.features.anlass.types.NaechsteStation;
import lombok.AllArgsConstructor;
import lombok.Value;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.eclipse.microprofile.openapi.annotations.media.Schema;

@Value
@AllArgsConstructor
@JaxDTO
public class NaechsteStationJson {

    @Nullable
    @Schema(required = false)
    private NaechsteStation naechsteStation;

    @Nullable
    @Schema(required = false)
    private LocalDateTime naechsteStationSeit;
}
