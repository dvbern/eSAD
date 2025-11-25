package ch.dvbern.oss.hemed.esc.api.features.anlass.einladung.json;

import ch.dvbern.dvbstarter.infrastructure.stereotypes.JaxDTO;
import ch.dvbern.dvbstarter.shared.types.id.ID;
import ch.dvbern.oss.hemed.esc.api.features.anlass.einladung.Einladung;
import ch.dvbern.oss.hemed.esc.api.features.kinder.Kind;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Value;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.eclipse.microprofile.openapi.annotations.media.Schema;

@Value
@AllArgsConstructor
@JaxDTO
public class UntersuchungTabInfoJson {

    @NotNull
    @NonNull
    @Schema(required = true)
    private final ID<Einladung> einladungID;

    @NotNull
    @NonNull
    @Valid
    @Schema(required = true)
    private final String label;

    @NotNull
    @NonNull
    @Valid
    @Schema(required = true)
    private final ID<Kind> kindID;

}
