package ch.dvbern.oss.hemed.esc.api.features.anlass.groessegewicht.json;

import java.math.BigDecimal;

import ch.dvbern.dvbstarter.infrastructure.stereotypes.JaxDTO;
import ch.dvbern.oss.hemed.esc.api.infrastructure.optimisticlocking.SaveJsonWithOptimisticLocking;
import ch.dvbern.oss.hemed.esc.api.shared.valueenhanced.ValueEnhancedJson;
import jakarta.validation.Valid;
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
    private ValueEnhancedJson groessePerzentile;

    @NonNull
    @Schema(required = true)
    @Valid
    private ValueEnhancedJson gewichtPerzentile;

    @NonNull
    @Schema(required = true)
    @Valid
    private ValueEnhancedJson bmiPerzentile;

    @Nullable
    @Schema(required = false)
    private String bemerkungen;

    @Schema(required = true)
    private long version;

}
