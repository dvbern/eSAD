package ch.dvbern.oss.hemed.esc.api.features.schule.quartier.json;

import java.util.List;

import ch.dvbern.dvbstarter.infrastructure.stereotypes.JaxDTO;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Value;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.eclipse.microprofile.openapi.annotations.media.Schema;

@Value
@AllArgsConstructor
@JaxDTO
public class QuartierOptionListJson {

    @NotNull
    @NonNull
    @Schema(required = true)
    private final List<QuartierOptionJson> quartiereList;
}
