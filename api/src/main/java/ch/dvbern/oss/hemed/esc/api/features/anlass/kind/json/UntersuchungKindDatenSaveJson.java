package ch.dvbern.oss.hemed.esc.api.features.anlass.kind.json;

import java.time.LocalDate;

import ch.dvbern.dvbstarter.infrastructure.stereotypes.JaxDTO;
import ch.dvbern.oss.hemed.esc.api.features.schule.types.Klassenstufe;
import ch.dvbern.oss.hemed.esc.api.infrastructure.optimisticlocking.SaveJsonWithOptimisticLocking;
import ch.dvbern.oss.hemed.esc.api.shared.person.Geschlecht;
import ch.dvbern.oss.hemed.esc.api.shared.person.Land;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Value;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.eclipse.microprofile.openapi.annotations.media.Schema;

@Value
@AllArgsConstructor
@JaxDTO
public class UntersuchungKindDatenSaveJson implements SaveJsonWithOptimisticLocking {

    @NotNull
    @NonNull
    @Schema(required = true)
    private String name;

    @NotNull
    @NonNull
    @Schema(required = true)
    private String vorname;

    @NotNull
    @NonNull
    @Schema(required = true)
    private Geschlecht geschlecht;

    @NotNull
    @NonNull
    @Schema(required = true)
    private LocalDate geburtsdatum;

    @Nullable
    @Schema(required = false)
    private Land nationalitaet;

    @Nullable
    @Schema(required = false)
    private String schulhaus;

    @Nullable
    @Schema(required = false)
    private String quartier;

    @Nullable
    @Schema(required = false)
    private String schulkreis;

    @Nullable
    @Schema(required = false)
    private String schulhausnummer;

    @Nullable
    @Schema(required = false)
    private Klassenstufe klassenstufe;

    @Nullable
    @Schema(required = false)
    private String klassentyp;

    @Nullable
    @Schema(required = false)
    private String klasse;

    @Schema(required = true)
    private long version;
}
