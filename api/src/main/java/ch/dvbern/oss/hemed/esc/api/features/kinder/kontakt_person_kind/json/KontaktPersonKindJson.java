package ch.dvbern.oss.hemed.esc.api.features.kinder.kontakt_person_kind.json;

import ch.dvbern.dvbstarter.infrastructure.json.EntityInfoJson;
import ch.dvbern.dvbstarter.infrastructure.stereotypes.JaxDTO;
import ch.dvbern.dvbstarter.shared.types.id.ID;
import ch.dvbern.oss.hemed.esc.api.externalimportinfojson.ExternalImportInfoJson;
import ch.dvbern.oss.hemed.esc.api.features.kinder.kontakt_person_kind.KontaktPersonKind;
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
public class KontaktPersonKindJson {

    @NotNull
    @NonNull
    @Schema(required = true)
    private final ID<KontaktPersonKind> id;

    @Valid
    @Nullable
    @Schema(required = false)
    private final ExternalImportInfoJson externalImportInfoJson;

    @NotNull
    @Schema(required = true)
    private final boolean manualOrAktivInExternalSystem;

    @NotNull
    @NonNull
    @Valid
    @Schema(required = true)
    private final EntityInfoJson entityInfo;

    @NotNull
    @NonNull
    @Valid
    @Schema(required = true)
    private final KontaktPersonKindSaveJson data;

}
