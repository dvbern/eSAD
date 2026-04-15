package ch.dvbern.oss.hemed.esc.api.shared.valueenhanced;

import java.util.List;

import ch.dvbern.dvbstarter.infrastructure.stereotypes.JaxDTO;
import lombok.AllArgsConstructor;
import lombok.Value;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.eclipse.microprofile.openapi.annotations.media.Schema;

@Value
@AllArgsConstructor
@JaxDTO
public class ValueEnhancedJson {

    @Nullable
    @Schema(required = false)
    private List<String> value;

    @Nullable
    @Schema(required = false)
    private NichtGeprueftGrund nichtGeprueftGrund;
}
