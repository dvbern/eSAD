package ch.dvbern.oss.hemed.esc.api.features.hellodata.json.untersuchungen;

import java.util.UUID;
import jakarta.validation.constraints.NotNull;

import ch.dvbern.dvbstarter.infrastructure.stereotypes.JaxDTO;
import ch.dvbern.oss.hemed.esc.api.shared.types.IgnoreForIdClassTest;

import lombok.AllArgsConstructor;
import lombok.Value;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.eclipse.microprofile.openapi.annotations.media.Schema;

@Value
@AllArgsConstructor
@JaxDTO
public class HelloDataMassnahmenJson {

    @IgnoreForIdClassTest
    @NotNull
    @NonNull
    @Schema(required = true)
    UUID id;

    @Schema(required = true)
    boolean keineMassnahmen;

    @Schema(required = true)
    boolean beratungMPA;

    @Schema(required = true)
    boolean beratungSchulAerztin;

    @Schema(required = true)
    boolean anderes;

    @Schema(required = true)
    boolean bereitsBehandeltSehen;

    @Schema(required = true)
    boolean bereitsBehandeltHoeren;

    @Schema(required = true)
    boolean bereitsBehandeltGroesseGewicht;

    @Schema(required = true)
    boolean bereitsBehandeltSprache;

    @Schema(required = true)
    boolean bereitsBehandeltMotorik;

    @Schema(required = true)
    boolean bereitsBehandeltAnderes;
}
