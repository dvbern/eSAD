package ch.dvbern.oss.hemed.esc.api.shared.documentqueue.json;

import ch.dvbern.dvbstarter.infrastructure.stereotypes.JaxDTO;
import ch.dvbern.oss.hemed.esc.api.shared.documentqueue.DocumentQueueParams;
import ch.dvbern.oss.hemed.esc.api.shared.fileio.FileTyp;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Value;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.eclipse.microprofile.openapi.annotations.media.Schema;

@Value
@AllArgsConstructor
@JaxDTO
public class DocumentQueueSaveJson {
    @NotNull
    @NonNull
    @Schema(required = true)
    FileTyp type;

    @Nullable
    @Schema(required = false)
    DocumentQueueParams documentQueueParams;
}
