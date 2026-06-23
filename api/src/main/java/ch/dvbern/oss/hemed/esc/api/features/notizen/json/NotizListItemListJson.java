package ch.dvbern.oss.hemed.esc.api.features.notizen.json;

import java.util.List;

import ch.dvbern.dvbstarter.infrastructure.stereotypes.JaxDTO;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import lombok.Value;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.eclipse.microprofile.openapi.annotations.media.Schema;

/**
 * Container DTO für eine Liste von {@link NotizListItemJson}.
 */
@Value
@AllArgsConstructor
@JaxDTO
public class NotizListItemListJson {

    @NonNull
    @Valid
    @Schema(required = true)
    List<NotizListItemJson> notizen;
}
