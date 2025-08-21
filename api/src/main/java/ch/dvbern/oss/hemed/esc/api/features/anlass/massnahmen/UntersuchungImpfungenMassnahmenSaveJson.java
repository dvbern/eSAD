package ch.dvbern.oss.hemed.esc.api.features.anlass.massnahmen;

import ch.dvbern.dvbstarter.infrastructure.stereotypes.JaxDTO;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Value;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.eclipse.microprofile.openapi.annotations.media.Schema;

@Value
@AllArgsConstructor
@JaxDTO
public class UntersuchungImpfungenMassnahmenSaveJson {

    @NotNull
    @Schema(required = true)
    private boolean keineMassnahmen;

    @NotNull
    @Schema(required = true)
    private boolean vormerkenFuerGruppenFolgetermin;

    @NotNull
    @Schema(required = true)
    private boolean impfberatungMpaDurchgefuehrt;

    @Nullable
    @Schema(required = false)
    private String impfberatungMpaDurchgefuehrtBemerkungen;

    @NotNull
    @Schema(required = true)
    private boolean informationenAbgegeben;

    @Nullable
    @Schema(required = false)
    private InformationEltern informationEltern;

    @NotNull
    @Schema(required = true)
    private boolean informationHausarzt;

    @NotNull
    @Schema(required = true)
    private boolean rueckfrageHausarzt;

    @Nullable
    @Schema(required = false)
    private String bemerkungen;
}
