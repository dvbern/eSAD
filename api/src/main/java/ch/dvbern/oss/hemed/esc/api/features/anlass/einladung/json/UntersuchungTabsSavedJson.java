package ch.dvbern.oss.hemed.esc.api.features.anlass.einladung.json;

import ch.dvbern.dvbstarter.infrastructure.stereotypes.JaxDTO;
import ch.dvbern.dvbstarter.shared.types.id.ID;
import ch.dvbern.oss.hemed.esc.api.features.anlass.untersuchung.Untersuchung;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Value;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.eclipse.microprofile.openapi.annotations.media.Schema;

@JaxDTO
@Value
@AllArgsConstructor
public class UntersuchungTabsSavedJson {

    @NonNull
    @NotNull
    @Schema(required = true)
    private final ID<Untersuchung> untersuchungID;

    @NotNull
    @Schema(required = true)
    private final boolean datenTabSaved;

    @NotNull
    @Schema(required = true)
    private final boolean vorabklaerungenTabSaved;

    @NotNull
    @Schema(required = true)
    private final boolean impfungenTabSaved;

    @NotNull
    @Schema(required = true)
    private final boolean sehenTabSaved;

    @NotNull
    @Schema(required = true)
    private final boolean hoerenAudiogrammTabSaved;

    @NotNull
    @Schema(required = true)
    private final boolean hoerenOtoskopieTabSaved;

    @NotNull
    @Schema(required = true)
    private final boolean groesseGewichtTabSaved;

    @NotNull
    @Schema(required = true)
    private final boolean triageTabSaved;

    @NotNull
    @Schema(required = true)
    private final boolean aerzlicheUnteruchungTabSaved;

    @NotNull
    @Schema(required = true)
    private final boolean spracheTabSaved;

    @NotNull
    @Schema(required = true)
    private final boolean bewegungsapparatTabSaved;

    @NotNull
    @Schema(required = true)
    private final boolean schulrelevanteBeeintrachtigungenTabSaved;

    @NotNull
    @Schema(required = true)
    private final boolean abschlussTabSaved;

    @NotNull
    @Schema(required = true)
    private final boolean signaturTabSaved;

    @NotNull
    @Schema(required = true)
    private final boolean massnahmenTabSaved;

    @NotNull
    @Schema(required = true)
    private final boolean laeusekontrolleTabSaved;

}
