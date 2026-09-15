package ch.dvbern.oss.hemed.esc.api.features.anlass.groessegewicht.json;

import java.math.BigDecimal;
import jakarta.validation.Valid;

import ch.dvbern.dvbstarter.infrastructure.stereotypes.JaxDTO;
import ch.dvbern.oss.hemed.esc.api.infrastructure.optimisticlocking.SaveJsonWithOptimisticLocking;
import ch.dvbern.oss.hemed.esc.api.shared.valueenhanced.ValueEnhancedJson;

import lombok.AllArgsConstructor;
import lombok.Value;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.eclipse.microprofile.openapi.annotations.media.Schema;

@Value
@AllArgsConstructor
@JaxDTO
public class UntersuchungGroesseGewichtSaveJson implements SaveJsonWithOptimisticLocking {

    @Nullable
    @Schema(required = false)
    private Integer groesse;

    @Nullable
    @Schema(required = false)
    private BigDecimal gewicht;

    @NonNull
    @Schema(required = true)
    @Valid
    private ValueEnhancedJson groessePerzentileSingleValue;

    @NonNull
    @Schema(required = true)
    @Valid
    private ValueEnhancedJson groessePerzentileMultiValue;

    @NonNull
    @Schema(required = true)
    @Valid
    private ValueEnhancedJson gewichtPerzentileSingleValue;

    @NonNull
    @Schema(required = true)
    @Valid
    private ValueEnhancedJson gewichtPerzentileMultiValue;

    @NonNull
    @Schema(required = true)
    @Valid
    private ValueEnhancedJson bmiPerzentileSingleValue;

    @NonNull
    @Schema(required = true)
    @Valid
    private ValueEnhancedJson bmiPerzentileMultiValue;

    @Nullable
    @Schema(required = false)
    private String bemerkungen;

    @Schema(required = true)
    private long version;

}
