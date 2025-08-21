package ch.dvbern.oss.hemed.esc.api.features.impfstoff.json;


import ch.dvbern.dvbstarter.infrastructure.stereotypes.JaxDTO;
import ch.dvbern.dvbstarter.shared.types.id.ID;
import ch.dvbern.oss.hemed.esc.api.features.impfstoff.Impfstoff;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Value;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.eclipse.microprofile.openapi.annotations.media.Schema;

@Value
@AllArgsConstructor
@JaxDTO
public class ImpfstoffJson {

    @NotNull
    @NonNull
    @Schema(required = true)
    private final ID<Impfstoff> id;

    @NonNull
    @NotNull
    @Valid
    @Schema(required = true)
    private final ImpfstoffSaveJson data;
}
