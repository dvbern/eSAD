package ch.dvbern.oss.hemed.esc.api.features.kinder.json;

import ch.dvbern.dvbstarter.infrastructure.stereotypes.JaxDTO;
import ch.dvbern.oss.hemed.esc.api.features.benutzer.json.BenutzerOptionJson;
import ch.dvbern.oss.hemed.esc.api.features.kinder.types.PapierakteStatus;
import ch.dvbern.oss.hemed.esc.api.infrastructure.optimisticlocking.SaveJsonWithOptimisticLocking;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Value;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.eclipse.microprofile.openapi.annotations.media.Schema;

@Value
@AllArgsConstructor
@JaxDTO
@Schema(description = "Non medical business data for a Dossier")
public class DossierNichtMedizinischSaveJson implements SaveJsonWithOptimisticLocking {
    @Nullable
    @Schema(required = false)
    @Valid
    private BenutzerOptionJson zustaendigArzt;

    @Nullable
    @Schema(required = false)
    @Valid
    private BenutzerOptionJson zustaendigMPA;

    @NotNull
    @Schema(required = true)
    private PapierakteStatus papierakteStatus;

    @Schema(required = true)
    private long version;
}
