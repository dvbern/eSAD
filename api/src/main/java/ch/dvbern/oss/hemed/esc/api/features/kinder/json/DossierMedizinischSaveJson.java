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
@Schema(description = "Medical business data for a Dossier")
public class DossierMedizinischSaveJson implements SaveJsonWithOptimisticLocking {

    @NotNull
    @Schema(required = true)
    private boolean kinderschutzfall;

    @NotNull
    @Schema(required = true)
    private boolean keineAuskunftAnDrittpersonen;

    @NotNull
    @Schema(required = true)
    private boolean sansPapier;

    @Schema(required = false)
    private boolean andereZusatzinformation;

    @Nullable
    @Schema(required = false)
    private String andereZusatzInformationText;

    @NotNull
    @Schema(required = true)
    private boolean immunitaetMasern;

    @NotNull
    @Schema(required = true)
    private boolean immunitaetVarizellen;

    @NotNull
    @Schema(required = true)
    @Valid
    private NachkontrolleJson nachkontrolle;

    @Schema(required = true)
    private long version;
}
