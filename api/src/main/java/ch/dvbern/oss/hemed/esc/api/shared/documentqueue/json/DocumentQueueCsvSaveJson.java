package ch.dvbern.oss.hemed.esc.api.shared.documentqueue.json;

import java.time.LocalDate;

import ch.dvbern.dvbstarter.infrastructure.stereotypes.JaxDTO;
import ch.dvbern.oss.hemed.esc.api.shared.documentqueue.DocumentQueueCsvParams;
import ch.dvbern.oss.hemed.esc.api.shared.fileio.FileTyp;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Value;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.eclipse.microprofile.openapi.annotations.media.Schema;

@Value
@AllArgsConstructor
@JaxDTO
public class DocumentQueueCsvSaveJson {

    @NotNull
    @NonNull
    @Schema(required = true)
    FileTyp type;

    @NotNull
    @NonNull
    @Schema(required = true)
    LocalDate datumVon;

    @NotNull
    @NonNull
    @Schema(required = true)
    LocalDate datumBis;

    @NotNull
    @NonNull
    @Schema(required = true)
    DocumentQueueCsvParams documentQueueCsvParams;
}
