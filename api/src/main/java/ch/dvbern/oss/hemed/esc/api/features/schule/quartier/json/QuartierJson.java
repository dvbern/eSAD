package ch.dvbern.oss.hemed.esc.api.features.schule.quartier.json;

import ch.dvbern.dvbstarter.infrastructure.json.EntityInfoJson;
import ch.dvbern.dvbstarter.infrastructure.stereotypes.JaxDTO;
import ch.dvbern.dvbstarter.shared.types.id.ID;
import ch.dvbern.oss.hemed.esc.api.features.schule.quartier.Quartier;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Value;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.eclipse.microprofile.openapi.annotations.media.Schema;

@Value
@AllArgsConstructor
@JaxDTO
public class QuartierJson {

    @NotNull
    @NonNull
    @Schema(required = true)
    private final ID<Quartier> id;

    @NotNull
    @NonNull
    @Schema(required = true)
    @Valid
    private final EntityInfoJson entityInfo;

    @NotNull
    @NonNull
    @Schema(required = true)
    @Valid
    private final QuartierSaveJson data;
}
