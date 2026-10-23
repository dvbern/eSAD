package ch.dvbern.oss.hemed.esc.api.features.hellodata.json.untersuchungen;

import java.util.UUID;
import jakarta.validation.constraints.NotNull;

import ch.dvbern.dvbstarter.infrastructure.stereotypes.JaxDTO;
import ch.dvbern.oss.hemed.esc.api.features.anlass.hoeren.types.Dezibel;
import ch.dvbern.oss.hemed.esc.api.shared.types.IgnoreForIdClassTest;

import lombok.AllArgsConstructor;
import lombok.Value;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.eclipse.microprofile.openapi.annotations.media.Schema;

@Value
@AllArgsConstructor
@JaxDTO
public class HelloDataAudiogrammJson {

    @IgnoreForIdClassTest
    @NotNull
    @NonNull
    @Schema(required = true)
    UUID id;

    @Nullable
    @Schema(required = false)
    Dezibel dbHz250;

    @Nullable
    @Schema(required = false)
    Dezibel dbHz500;

    @Nullable
    @Schema(required = false)
    Dezibel dbHz1000;

    @Nullable
    @Schema(required = false)
    Dezibel dbHz2000;

    @Nullable
    @Schema(required = false)
    Dezibel dbHz4000;

    @Nullable
    @Schema(required = false)
    Dezibel dbHz6000;

    @Nullable
    @Schema(required = false)
    Dezibel dbHz8000;
}
