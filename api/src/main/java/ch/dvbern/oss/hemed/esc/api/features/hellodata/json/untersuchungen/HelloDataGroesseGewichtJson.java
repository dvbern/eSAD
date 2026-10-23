package ch.dvbern.oss.hemed.esc.api.features.hellodata.json.untersuchungen;

import java.math.BigDecimal;
import java.util.Set;
import java.util.UUID;
import jakarta.validation.constraints.NotNull;

import ch.dvbern.dvbstarter.infrastructure.stereotypes.JaxDTO;
import ch.dvbern.oss.hemed.esc.api.features.anlass.groessegewicht.types.PerzentileBmi;
import ch.dvbern.oss.hemed.esc.api.features.anlass.groessegewicht.types.PerzentileGewicht;
import ch.dvbern.oss.hemed.esc.api.features.anlass.groessegewicht.types.PerzentileGroesse;
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
public class HelloDataGroesseGewichtJson {

    @IgnoreForIdClassTest
    @NotNull
    @NonNull
    @Schema(required = true)
    UUID id;

    @Nullable
    @Schema(required = false)
    Integer groesse;

    @Nullable
    @Schema(required = false)
    BigDecimal gewicht;

    @Nullable
    @Schema(required = false)
    BigDecimal bmi;

    @Nullable
    @Schema(required = false)
    PerzentileGroesse groessePerzentileSingleValue;

    @Nullable
    @Schema(required = false)
    NichtGeprueftGrund groessePerzentileSingleValueNichtGeprueftGrund;

    @Nullable
    @Schema(required = false)
    Set<PerzentileGroesse> groessePerzentileMultiValue;

    @Nullable
    @Schema(required = false)
    NichtGeprueftGrund groessePerzentileMultiValueNichtGeprueftGrund;

    @Nullable
    @Schema(required = false)
    PerzentileGewicht gewichtPerzentileSingleValue;

    @Nullable
    @Schema(required = false)
    NichtGeprueftGrund gewichtPerzentileSingleValueNichtGeprueftGrund;

    @Nullable
    @Schema(required = false)
    Set<PerzentileGewicht> gewichtPerzentileMultiValue;

    @Nullable
    @Schema(required = false)
    NichtGeprueftGrund gewichtPerzentileMultiValueNichtGeprueftGrund;

    @Nullable
    @Schema(required = false)
    PerzentileBmi bmiPerzentileSingleValue;

    @Nullable
    @Schema(required = false)
    NichtGeprueftGrund bmiPerzentileSingleValueNichtGeprueftGrund;

    @Nullable
    @Schema(required = false)
    Set<PerzentileBmi> bmiPerzentileMultiValue;

    @Nullable
    @Schema(required = false)
    NichtGeprueftGrund bmiPerzentileMultiValueNichtGeprueftGrund;
}
