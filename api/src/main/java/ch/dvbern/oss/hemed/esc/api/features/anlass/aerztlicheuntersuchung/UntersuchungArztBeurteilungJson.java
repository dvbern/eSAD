package ch.dvbern.oss.hemed.esc.api.features.anlass.aerztlicheuntersuchung;

import ch.dvbern.dvbstarter.infrastructure.json.EntityInfoJson;
import ch.dvbern.dvbstarter.infrastructure.stereotypes.JaxDTO;
import ch.dvbern.dvbstarter.shared.types.id.ID;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Value;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.eclipse.microprofile.openapi.annotations.media.Schema;

@Value
@AllArgsConstructor
@JaxDTO
public class UntersuchungArztBeurteilungJson {

    @NotNull
    @NonNull
    @Schema(required = true)
    private final ID<UntersuchungArztBeurteilung> id;

    @NotNull
    @NonNull
    @Valid
    @Schema(required = true)
    private final EntityInfoJson entityInfo;

    @NotNull
    @NonNull
    @Valid
    @Schema(required = true)
    private final UntersuchungArztBeurteilungSaveJson data;

}
