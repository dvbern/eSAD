package ch.dvbern.oss.hemed.esc.api.features.anlass.json;

import ch.dvbern.dvbstarter.infrastructure.stereotypes.JaxDTO;
import ch.dvbern.oss.hemed.esc.api.features.schule.types.Klassenstufe;
import ch.dvbern.oss.hemed.esc.api.shared.person.Geschlecht;
import ch.dvbern.oss.hemed.esc.api.shared.person.Land;
import ch.dvbern.oss.hemed.esc.api.shared.person.LandCode;
import ch.dvbern.oss.hemed.esc.api.shared.types.IgnoreForIdClassTest;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;
import lombok.AllArgsConstructor;
import lombok.Value;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.eclipse.microprofile.openapi.annotations.media.Schema;

@Value
@AllArgsConstructor
@JaxDTO
public class UntersuchungKindDatenJson {

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

    @Nullable
    @Schema(required = false)
    @IgnoreForIdClassTest
    private String esadId;

    @NotNull
    @NonNull
    @Schema(required = true)
    private LocalDate geburtsdatum;

    @NotNull
    @NonNull
    @Schema(required = true)
    private String alterDuodezimal;

    @Nullable
    @Schema(required = false)
    private Land nationalitaet;

    @Nullable
    @Schema(required = false)
    private LandCode landCode;

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

    @Nullable
    @Schema(required = false)
    @IgnoreForIdClassTest
    private String klasseId;

    @NotNull
    @NonNull
    @Schema(required = true)
    private LocalDate anlassDatum;
}
