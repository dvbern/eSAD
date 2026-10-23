package ch.dvbern.oss.hemed.esc.api.features.hellodata.json.untersuchungen;

import java.math.BigDecimal;
import java.util.Set;
import java.util.UUID;
import jakarta.validation.constraints.NotNull;

import ch.dvbern.dvbstarter.infrastructure.stereotypes.JaxDTO;
import ch.dvbern.oss.hemed.esc.api.features.anlass.sehen.types.BrilleLinsenResultat;
import ch.dvbern.oss.hemed.esc.api.features.anlass.sehen.types.DistanzSehenAugeEinzelnResultat;
import ch.dvbern.oss.hemed.esc.api.features.anlass.sehen.types.DistanzSehenResultat;
import ch.dvbern.oss.hemed.esc.api.features.anlass.sehen.types.FarbsehenResultat;
import ch.dvbern.oss.hemed.esc.api.features.anlass.sehen.types.InspektionKonvergenzResultat;
import ch.dvbern.oss.hemed.esc.api.features.anlass.sehen.types.MotilitaetResultat;
import ch.dvbern.oss.hemed.esc.api.features.anlass.sehen.types.SehenEmpfehlung;
import ch.dvbern.oss.hemed.esc.api.features.anlass.sehen.types.StereotestResultat;
import ch.dvbern.oss.hemed.esc.api.features.anlass.sehen.types.StrabismusAuftretenResultat;
import ch.dvbern.oss.hemed.esc.api.features.anlass.sehen.types.StrabismusAuspraegungResultat;
import ch.dvbern.oss.hemed.esc.api.features.anlass.sehen.types.StrabismusResultat;
import ch.dvbern.oss.hemed.esc.api.features.anlass.sehen.types.VerwendetTafelResultat;
import ch.dvbern.oss.hemed.esc.api.shared.types.IgnoreForIdClassTest;
import ch.dvbern.oss.hemed.esc.api.shared.valueenhanced.NichtGeprueftGrund;

import lombok.AllArgsConstructor;
import lombok.Value;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.eclipse.microprofile.openapi.annotations.media.Schema;

@Value
@AllArgsConstructor
@JaxDTO
public class HelloDataSehenJson {

    @IgnoreForIdClassTest
    @NotNull
    @NonNull
    @Schema(required = true)
    UUID id;

    @Nullable
    @Schema(required = false)
    BrilleLinsenResultat brilleLinsen;

    @Nullable
    @Schema(required = false)
    NichtGeprueftGrund brilleLinsenNichtGeprueftGrund;

    @Nullable
    @Schema(required = false)
    StereotestResultat stereotest;

    @Nullable
    @Schema(required = false)
    NichtGeprueftGrund stereotestNichtGeprueftGrund;

    @Nullable
    @Schema(required = false)
    FarbsehenResultat farbsehen;

    @Nullable
    @Schema(required = false)
    NichtGeprueftGrund farbsehenNichtGeprueftGrund;

    @Nullable
    @Schema(required = false)
    VerwendetTafelResultat verwendeteTafel;

    @Nullable
    @Schema(required = false)
    NichtGeprueftGrund verwendeteTafelNichtGeprueftGrund;

    @Nullable
    @Schema(required = false)
    Set<DistanzSehenResultat> fernvisus;

    @Nullable
    @Schema(required = false)
    NichtGeprueftGrund fernvisusNichtGeprueftGrund;

    @Nullable
    @Schema(required = false)
    BigDecimal fernvisusRechts;

    @Nullable
    @Schema(required = false)
    BigDecimal fernvisusLinks;

    @Nullable
    @Schema(required = false)
    DistanzSehenAugeEinzelnResultat fernvisusAugeLinks;

    @Nullable
    @Schema(required = false)
    NichtGeprueftGrund fernvisusAugeLinksNichtGeprueftGrund;

    @Nullable
    @Schema(required = false)
    DistanzSehenAugeEinzelnResultat fernvisusAugeRechts;

    @Nullable
    @Schema(required = false)
    NichtGeprueftGrund fernvisusAugeRechtsNichtGeprueftGrund;

    @Nullable
    @Schema(required = false)
    Set<DistanzSehenResultat> nahvisus;

    @Nullable
    @Schema(required = false)
    NichtGeprueftGrund nahvisusNichtGeprueftGrund;

    @Nullable
    @Schema(required = false)
    BigDecimal nahvisusRechts;

    @Nullable
    @Schema(required = false)
    BigDecimal nahvisusLinks;

    @Nullable
    @Schema(required = false)
    MotilitaetResultat motilitaet;

    @Nullable
    @Schema(required = false)
    NichtGeprueftGrund motilitaetNichtGeprueftGrund;

    @Nullable
    @Schema(required = false)
    Set<StrabismusResultat> strabismus;

    @Nullable
    @Schema(required = false)
    NichtGeprueftGrund strabismusNichtGeprueftGrund;

    @Nullable
    @Schema(required = false)
    StrabismusAuspraegungResultat strabismusAuspraegung;

    @Nullable
    @Schema(required = false)
    NichtGeprueftGrund strabismusAuspraegungNichtGeprueftGrund;

    @Nullable
    @Schema(required = false)
    StrabismusAuftretenResultat strabismusAuftreten;

    @Nullable
    @Schema(required = false)
    NichtGeprueftGrund strabismusAuftretenNichtGeprueftGrund;

    @Nullable
    @Schema(required = false)
    InspektionKonvergenzResultat inspektion;

    @Nullable
    @Schema(required = false)
    NichtGeprueftGrund inspektionNichtGeprueftGrund;

    @Nullable
    @Schema(required = false)
    InspektionKonvergenzResultat konvergenz;

    @Nullable
    @Schema(required = false)
    NichtGeprueftGrund konvergenzNichtGeprueftGrund;

    @Nullable
    @Schema(required = false)
    Set<SehenEmpfehlung> empfehlung;

    @Nullable
    @Schema(required = false)
    NichtGeprueftGrund empfehlungNichtGeprueftGrund;
}
