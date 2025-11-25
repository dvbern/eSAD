package ch.dvbern.oss.hemed.esc.api.features.kinder.json;

import ch.dvbern.dvbstarter.infrastructure.stereotypes.JaxDTO;
import ch.dvbern.dvbstarter.shared.types.id.ID;
import ch.dvbern.oss.hemed.esc.api.features.kinder.Kind;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Value;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.eclipse.microprofile.openapi.annotations.media.Schema;

@Value
@AllArgsConstructor
@JaxDTO
public class KindOptionJson {

    @NotNull
    @NonNull
    @Schema(required = true)
    private final ID<Kind> id;

    @NotNull
    @NonNull
    @Schema(required = true)
    private final String name;

    @NotNull
    @NonNull
    @Schema(required = true)
    private final String vorname;

}
