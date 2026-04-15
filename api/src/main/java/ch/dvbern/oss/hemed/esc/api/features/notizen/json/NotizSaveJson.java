package ch.dvbern.oss.hemed.esc.api.features.notizen.json;

import java.util.List;

import ch.dvbern.dvbstarter.infrastructure.stereotypes.JaxDTO;
import ch.dvbern.oss.hemed.esc.api.features.benutzer.json.BenutzerOptionJson;
import ch.dvbern.oss.hemed.esc.api.features.labels.json.LabelJson;
import ch.dvbern.oss.hemed.esc.api.features.notizen.NotizStatus;
import ch.dvbern.oss.hemed.esc.api.features.notizen.NotizTyp;
import ch.dvbern.oss.hemed.esc.api.infrastructure.optimisticlocking.SaveJsonWithOptimisticLocking;
import ch.dvbern.oss.hemed.esc.api.shared.fileio.FileDataJson;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Value;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.eclipse.microprofile.openapi.annotations.media.Schema;

@Value
@AllArgsConstructor
@JaxDTO
public class NotizSaveJson implements SaveJsonWithOptimisticLocking {

    @NotNull
    @NotEmpty
    @NonNull
    @Schema(required = true)
    private String titel;

    @Nullable
    @Schema(required = false)
    private String bemerkungen;

    @NotNull
    @NonNull
    @Schema(required = true)
    private NotizStatus status;

    @NotNull
    @NonNull
    @Schema(required = true)
    private NotizTyp typ;

    @NotNull
    @NonNull
    @Schema(required = true)
    @Valid
    private BenutzerOptionJson verantwortlicher;

    @Nullable
    @Schema(required = false)
    private String anlassLink;

    @NotNull
    @NonNull
    @Schema(required = true)
    private List<LabelJson> labels;

    @NotNull
    @NonNull
    @Schema(required = true)
    private List<FileDataJson> files;

    @NotNull
    @Schema(required = true)
    private long version;
}
