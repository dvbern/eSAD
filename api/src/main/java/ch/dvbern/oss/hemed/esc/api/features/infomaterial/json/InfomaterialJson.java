package ch.dvbern.oss.hemed.esc.api.features.infomaterial.json;

import ch.dvbern.dvbstarter.infrastructure.json.EntityInfoJson;
import ch.dvbern.dvbstarter.infrastructure.stereotypes.JaxDTO;
import ch.dvbern.dvbstarter.shared.types.id.ID;
import ch.dvbern.oss.hemed.esc.api.features.infomaterial.Infomaterial;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Value;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.eclipse.microprofile.openapi.annotations.media.Schema;

@Value
@AllArgsConstructor
@JaxDTO
public class InfomaterialJson {

    /**
     * Um auf einfache Art den EntityId contract vom ngrx SignalStore mit dem Entities feature erfuellen zu koennen
     * wollen wir die ID auf dem Rootlevel behalten. Sie wird nicht ins EntityInfoJson geschoben
     */
    @NotNull
    @NonNull
    @Schema(required = true)
    private final ID<Infomaterial> id;

    @NotNull
    @NonNull
    @Valid
    @Schema(required = true)
    private final EntityInfoJson entityInfo;

    @NotNull
    @NonNull
    @Valid
    @Schema(required = true)
    private final InfomaterialSaveJson data;

}
