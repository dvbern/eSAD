package ch.dvbern.oss.hemed.esc.api.features.kindanlaesse.json;

import java.time.LocalDate;

import ch.dvbern.dvbstarter.infrastructure.stereotypes.JaxDTO;
import ch.dvbern.dvbstarter.shared.types.id.ID;
import ch.dvbern.oss.hemed.esc.api.features.anlass.Anlass;
import ch.dvbern.oss.hemed.esc.api.features.anlass.einladung.Einladung;
import ch.dvbern.oss.hemed.esc.api.features.anlass.types.AnlassStatus;
import ch.dvbern.oss.hemed.esc.api.features.anlass.types.AnlassTyp;
import ch.dvbern.oss.hemed.esc.api.features.anlass.einladung.types.EinladungStatus;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Value;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.eclipse.microprofile.openapi.annotations.media.Schema;

@Value
@AllArgsConstructor
@JaxDTO
public class KindAnlassSummaryJson {

    @NotNull
    @NonNull
    @Schema(required = true)
    private final ID<Einladung> id;

    @NotNull
    @NonNull
    @Schema(required = true)
    private final ID<Anlass> anlassId;

    @NotNull
    @NonNull
    @Schema(required = true)
    private final ID<Einladung> einladungId;

    @NotNull
    @NonNull
    @Schema(required = true)
    private final AnlassTyp anlassTyp;

    @NotNull
    @NonNull
    @Schema(required = true)
    private final LocalDate anlassDatum;

    @NotNull
    @NonNull
    @Schema(required = true)
    private final String titel;

    @NotNull
    @NonNull
    @Schema(required = true)
    private final Integer schuljahr;

    @NotNull
    @NonNull
    @Schema(required = true)
    private final EinladungStatus einladungStatus;

    @NotNull
    @NonNull
    @Schema(required = true)
    private final AnlassStatus anlassStatus;
}
