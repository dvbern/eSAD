package ch.dvbern.oss.hemed.esc.api.features.schule.klasse.json;

import java.util.List;

import ch.dvbern.dvbstarter.infrastructure.json.EntityInfoJson;
import ch.dvbern.dvbstarter.infrastructure.stereotypes.JaxDTO;
import ch.dvbern.dvbstarter.shared.types.id.ID;
import ch.dvbern.oss.hemed.esc.api.externalimportinfojson.ExternalImportInfoJson;
import ch.dvbern.oss.hemed.esc.api.features.schule.klasse.Klasse;
import ch.dvbern.oss.hemed.esc.api.features.schule.lehrperson.json.LehrpersonOptionJson;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Value;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.eclipse.microprofile.openapi.annotations.media.Schema;

@Value
@AllArgsConstructor
@JaxDTO
public class KlasseJson {

    /**
     * Um auf einfache Art den EntityId contract vom ngrx SignalStore mit dem Entities feature erfuellen zu koennen
     * wollen wir die ID auf dem Rootlevel behalten. Sie wird nicht ins EntityInfoJson geschoben
     */
    @NotNull
    @NonNull
    @Schema(required = true)
    private final ID<Klasse> id;

    @Valid
    @Nullable
    @Schema(required = false)
    private final ExternalImportInfoJson externalImportInfoJson;

    @NonNull
    @Valid
    @Schema(required = true)
    private final EntityInfoJson entityInfo;

    @NotNull
    @NonNull
    @Valid
    @Schema(required = true)
    private final KlasseSaveJson data;

    @NonNull
    @Schema(required = true)
    private final List<LehrpersonOptionJson> lehrpersonen;
}
