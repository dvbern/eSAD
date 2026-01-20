package ch.dvbern.oss.hemed.esc.api.features.anlass.json;

import ch.dvbern.dvbstarter.infrastructure.stereotypes.JaxDTO;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Value;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.eclipse.microprofile.openapi.annotations.media.Schema;

import java.util.List;

@Value
@AllArgsConstructor
@JaxDTO
public class AnlassWithDateTimeListJson {

    @NotNull
    @NonNull
    @Schema(required = true)
    private List<AnlassWithDateTimeJson> anlaesseWithDateTime;
}
