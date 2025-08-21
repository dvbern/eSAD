package ch.dvbern.oss.hemed.esc.api.features.notizen.json;

import java.util.List;

import ch.dvbern.dvbstarter.infrastructure.stereotypes.JaxDTO;
import lombok.AllArgsConstructor;
import lombok.Value;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.eclipse.microprofile.openapi.annotations.media.Schema;

@Value
@AllArgsConstructor
@JaxDTO
public class NotizListJson {

    @NonNull
    @Schema(required = true)
    List<NotizJson> notizen;
}
