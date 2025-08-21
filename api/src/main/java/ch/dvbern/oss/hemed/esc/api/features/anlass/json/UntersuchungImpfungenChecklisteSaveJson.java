package ch.dvbern.oss.hemed.esc.api.features.anlass.json;

import ch.dvbern.dvbstarter.infrastructure.stereotypes.JaxDTO;
import jakarta.validation.constraints.NotNull;
import lombok.Value;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.eclipse.microprofile.openapi.annotations.media.Schema;

@Value
@JaxDTO
public class UntersuchungImpfungenChecklisteSaveJson {

    @NotNull
    @Schema(required = true)
    private boolean checkEinwilligungVorhanden;

    @NotNull
    @Schema(required = true)
    private boolean checkKontraindikationenAbgeklaert;

    @NotNull
    @Schema(required = true)
    private boolean checkDoppelkontrolleImpfentscheidDurchgefuehrt;

    @Nullable
    @Schema(required = false)
    private String bemerkungen;

}
