package ch.dvbern.oss.hemed.esc.api.features.anlass.groessegewicht;

import java.math.BigDecimal;
import java.util.List;

import ch.dvbern.dvbstarter.infrastructure.stereotypes.JaxDTO;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Value;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.eclipse.microprofile.openapi.annotations.media.Schema;

@Value
@AllArgsConstructor
@JaxDTO
public class UntersuchungGroesseGewichtDataJson {

    @Nullable
    @Valid
    @Schema(required = false)
    private final UntersuchungGroesseGewichtJson untersuchungGroesseGewichtJson;

    @NotNull
    @NonNull
    @Valid
    @Schema(required = true)
    private final List<GroesseGewichtDataPointJson> previousDataPoints;

    @Nullable
    @Schema(required = false)
    private BigDecimal alter;

    @Nullable
    @Schema(required = false)
    private String alterDuodezimal;
}
