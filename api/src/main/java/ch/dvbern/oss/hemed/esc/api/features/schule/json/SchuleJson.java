package ch.dvbern.oss.hemed.esc.api.features.schule.json;

import java.util.List;

import ch.dvbern.dvbstarter.infrastructure.json.EntityInfoJson;
import ch.dvbern.dvbstarter.infrastructure.stereotypes.JaxDTO;
import ch.dvbern.dvbstarter.shared.types.id.ID;
import ch.dvbern.oss.hemed.esc.api.externalimportinfojson.ExternalImportInfoJson;
import ch.dvbern.oss.hemed.esc.api.features.schule.Schule;
import ch.dvbern.oss.hemed.esc.api.features.schule.klasse.klasseexternal.EscadaKlasseStufe;
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
public class SchuleJson {

    @NotNull
    @NonNull
    @Schema(required = true)
    private final ID<Schule> id;

    @Valid
    @Nullable
    @Schema(required = false)
    private final ExternalImportInfoJson externalImportInfoJson;

    @NotNull
    @Schema(required = true)
    private final boolean manualOrAktivInExternalSystem;

    @NotNull
    @NonNull
    @Schema(required = true)
    @Valid
    private final EntityInfoJson entityInfo;

    @NotNull
    @NonNull
    @Schema(required = true)
    @Valid
    private final SchuleSaveJson data;

    @NotNull
    @NonNull
    @Schema(required = true)
    private final List<EscadaKlasseStufe> klassenstufen;
}
