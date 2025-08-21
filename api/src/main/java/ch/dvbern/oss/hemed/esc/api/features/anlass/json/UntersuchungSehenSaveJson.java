package ch.dvbern.oss.hemed.esc.api.features.anlass.json;

import java.math.BigDecimal;
import java.util.List;

import ch.dvbern.dvbstarter.infrastructure.stereotypes.JaxDTO;
import ch.dvbern.oss.hemed.esc.api.features.anlass.massnahmen.UntersuchungSehenMassnahmenSaveJson;
import ch.dvbern.oss.hemed.esc.api.features.anlass.types.BrilleLinsenResultat;
import ch.dvbern.oss.hemed.esc.api.features.anlass.types.DistanzSehenResultat;
import ch.dvbern.oss.hemed.esc.api.features.anlass.types.FarbsehenResultat;
import ch.dvbern.oss.hemed.esc.api.features.anlass.types.InspektionKonvergenzResultat;
import ch.dvbern.oss.hemed.esc.api.features.anlass.types.MotilitaetResultat;
import ch.dvbern.oss.hemed.esc.api.features.anlass.types.StereotestResultat;
import ch.dvbern.oss.hemed.esc.api.features.anlass.types.StrabismusAuftretenResultat;
import ch.dvbern.oss.hemed.esc.api.features.anlass.types.StrabismusAuspraegungResultat;
import ch.dvbern.oss.hemed.esc.api.features.anlass.types.StrabismusResultat;
import ch.dvbern.oss.hemed.esc.api.features.anlass.types.VerwendetTafelResultat;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import lombok.Value;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.eclipse.microprofile.openapi.annotations.media.Schema;

@Value
@AllArgsConstructor
@JaxDTO
public class UntersuchungSehenSaveJson {

    @Nullable
    @Schema(required = false)
    private BrilleLinsenResultat brilleLinsen;

    @Nullable
    @Schema(required = false)
    private StereotestResultat stereotest;

    @Nullable
    @Schema(required = false)
    private FarbsehenResultat farbsehen;

    @Nullable
    @Schema(required = false)
    private VerwendetTafelResultat verwendeteTafel;

    @Nullable
    @Schema(required = false)
    private List<DistanzSehenResultat> fernvisus;

    @Nullable
    @Schema(required = false)
    private BigDecimal fernvisusRechts;

    @Nullable
    @Schema(required = false)
    private BigDecimal fernvisusLinks;

    @Nullable
    @Schema(required = false)
    private List<DistanzSehenResultat> nahvisus;

    @Nullable
    @Schema(required = false)
    private BigDecimal nahvisusRechts;

    @Nullable
    @Schema(required = false)
    private BigDecimal nahvisusLinks;

    @Nullable
    @Schema(required = false)
    private MotilitaetResultat motilitaet;

    @Nullable
    @Schema(required = false)
    private List<StrabismusResultat> strabismus;

    @Nullable
    @Schema(required = false)
    private StrabismusAuspraegungResultat strabismusAuspraegung;

    @Nullable
    @Schema(required = false)
    private StrabismusAuftretenResultat strabismusAuftreten;

    @Nullable
    @Schema(required = false)
    private InspektionKonvergenzResultat inspektion;

    @Nullable
    @Schema(required = false)
    private InspektionKonvergenzResultat konvergenz;

    @Nullable
    @Schema(required = false)
    private String bemerkungen;

    @Nullable
    @Valid
    @Schema(required = false)
    private final UntersuchungSehenMassnahmenSaveJson massnahmen;

}
