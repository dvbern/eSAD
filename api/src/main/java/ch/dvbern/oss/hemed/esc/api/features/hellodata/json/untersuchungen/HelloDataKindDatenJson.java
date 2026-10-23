package ch.dvbern.oss.hemed.esc.api.features.hellodata.json.untersuchungen;

import java.util.UUID;
import jakarta.validation.constraints.NotNull;

import ch.dvbern.dvbstarter.infrastructure.stereotypes.JaxDTO;
import ch.dvbern.oss.hemed.esc.api.features.schule.types.Klassenstufe;
import ch.dvbern.oss.hemed.esc.api.shared.person.Geschlecht;
import ch.dvbern.oss.hemed.esc.api.shared.person.Land;
import ch.dvbern.oss.hemed.esc.api.shared.types.IgnoreForIdClassTest;

import lombok.AllArgsConstructor;
import lombok.Value;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.eclipse.microprofile.openapi.annotations.media.Schema;

@Value
@AllArgsConstructor
@JaxDTO
public class HelloDataKindDatenJson {

    @IgnoreForIdClassTest
    @NotNull
    @NonNull
    @Schema(required = true)
    UUID id;

    @Nullable
    @Schema(required = false)
    Klassenstufe klassenstufe;

    @Nullable
    @Schema(required = false)
    String klassentyp;

    @Nullable
    @Schema(required = false)
    String klasse;

    @Nullable
    @Schema(required = false)
    String schulhaus;

    @Nullable
    @Schema(required = false)
    String schulstandort;

    @Nullable
    @Schema(required = false)
    String quartier;

    @Nullable
    @Schema(required = false)
    String schulkreis;

    @Nullable
    @Schema(required = false)
    String schulhausnummer;

    @Nullable
    @Schema(required = false)
    Land nationalitaet;

    @Nullable
    @Schema(required = false)
    Geschlecht geschlecht;
}
