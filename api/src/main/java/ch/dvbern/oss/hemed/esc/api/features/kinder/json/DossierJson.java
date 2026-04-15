package ch.dvbern.oss.hemed.esc.api.features.kinder.json;

import java.time.LocalDateTime;

import ch.dvbern.dvbstarter.infrastructure.json.EntityInfoJson;
import ch.dvbern.dvbstarter.infrastructure.stereotypes.JaxDTO;
import ch.dvbern.dvbstarter.shared.types.id.ID;
import ch.dvbern.oss.hemed.esc.api.features.kinder.Dossier;
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
public class DossierJson {

    @NotNull
    @NonNull
    @Schema(required = true)
    private final ID<Dossier> id;

    @NotNull
    @NonNull
    @Valid
    @Schema(required = true)
    private final EntityInfoJson entityInfo;

    @Nullable
    @Valid
    @Schema(required = false)
    private final DossierMedizinischSaveJson dataMedizinisch;

    @NotNull
    @NonNull
    @Valid
    @Schema(required = true)
    private final DossierNichtMedizinischSaveJson dataNichtMedizinisch;

    @NotNull
    @NonNull
    @Valid
    @Schema(required = true)
    private final KindJson kind;

    @Nullable
    @Schema(required = false)
    private final LocalDateTime timestampDefinitivArchiviert;
}
