package ch.dvbern.oss.hemed.esc.api.features.anlass.json;

import java.time.LocalDate;
import java.time.LocalDateTime;

import ch.dvbern.dvbstarter.infrastructure.stereotypes.JaxDTO;
import ch.dvbern.oss.hemed.esc.api.features.anlass.anlassconfig.json.AnlassConfigOptionJson;
import ch.dvbern.oss.hemed.esc.api.features.anlass.types.AnlassArt;
import ch.dvbern.oss.hemed.esc.api.features.anlass.types.AnlassStatus;
import ch.dvbern.oss.hemed.esc.api.features.anlass.types.AnlassTyp;
import ch.dvbern.oss.hemed.esc.api.features.benutzer.json.BenutzerOptionJson;
import ch.dvbern.oss.hemed.esc.api.infrastructure.optimisticlocking.SaveJsonWithOptimisticLocking;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Value;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.eclipse.microprofile.openapi.annotations.media.Schema;

@Value
@AllArgsConstructor
@JaxDTO
@Schema(description = "Business data for a Anlass")
public class AnlassSaveJson implements SaveJsonWithOptimisticLocking {

    @NotNull
    @NonNull
    @Schema(required = true)
    private final AnlassTyp anlassTyp;

    @NotNull
    @NonNull
    @Schema(required = true)
    @Valid
    private final AnlassConfigOptionJson anlassConfigOption;

    @Nullable
    @Schema(required = false)
    private final String titel;

    @NotNull
    @NonNull
    @Schema(required = true)
    private final AnlassStatus anlassStatus;

    @NotNull
    @NonNull
    @Schema(required = true)
    private final LocalDate datum;

    @NotNull
    @NonNull
    @Schema(required = true)
    private final Integer schuljahr;

    @Nullable
    @Schema(required = false)
    private final LocalDateTime zeitVon;

    @Nullable
    @Schema(required = false)
    private final LocalDateTime zeitBis;

    @NotNull
    @NonNull
    @Schema(required = true)
    private final AnlassArt anlassArt;

    @Nullable
    @Valid
    @Schema(required = false)
    private final BenutzerOptionJson arzt;

    @Nullable
    @Schema(required = false)
    private final String arztInfo;

    @Nullable
    @Valid
    @Schema(required = false)
    private final BenutzerOptionJson mpa;

    @Nullable
    @Schema(required = false)
    private final String mpaInfo;

    @Nullable
    @Valid
    @Schema(required = false)
    private final BenutzerOptionJson weiterePerson;

    @Nullable
    @Schema(required = false)
    private final String weiterePersonInfo;

    @Schema(required = true)
    private long version;

}
