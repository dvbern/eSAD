package ch.dvbern.oss.hemed.esc.api.features.impfstoff.json;

import ch.dvbern.dvbstarter.infrastructure.stereotypes.JaxDTO;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.eclipse.microprofile.openapi.annotations.media.Schema;

@Getter
@Setter
@AllArgsConstructor
@JaxDTO
public class ImpfstoffChargeSaveJson {

    @NotNull
    @NonNull
    @Schema(required = true)
    private final String name;

    @NotNull
    @Schema(required = true)
    private boolean aktiv;
}
