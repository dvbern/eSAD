package ch.dvbern.oss.hemed.esc.api.features.anlass.groessegewicht;

import java.math.BigDecimal;

import ch.dvbern.dvbstarter.infrastructure.stereotypes.JaxDTO;
import lombok.AllArgsConstructor;
import lombok.Value;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.eclipse.microprofile.openapi.annotations.media.Schema;

@Value
@AllArgsConstructor
@JaxDTO
public class GroesseGewichtDataPointJson {

    @Nullable
    @Schema(required = false)
    private Integer groesse;

    @Nullable
    @Schema(required = false)
    private BigDecimal gewicht;

    @Nullable
    @Schema(required = false)
    private BigDecimal alter;

    @Nullable
    @Schema(required = false)
    private String alterDuodezimal;

    @Nullable
    @Schema(required = false)
    private BigDecimal bmi;

}
