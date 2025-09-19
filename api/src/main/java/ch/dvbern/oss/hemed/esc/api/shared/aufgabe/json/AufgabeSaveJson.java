package ch.dvbern.oss.hemed.esc.api.shared.aufgabe.json;

import java.net.URL;
import java.time.LocalDate;
import java.util.List;

import ch.dvbern.dvbstarter.infrastructure.stereotypes.JaxDTO;
import ch.dvbern.dvbstarter.shared.types.url.URLSchema;
import ch.dvbern.oss.hemed.esc.api.features.anlass.json.AnlassOptionJson;
import ch.dvbern.oss.hemed.esc.api.features.benutzer.json.BenutzerOptionJson;
import ch.dvbern.oss.hemed.esc.api.features.labels.json.LabelJson;
import ch.dvbern.oss.hemed.esc.api.features.search.json.KindSearchJson;
import ch.dvbern.oss.hemed.esc.api.infrastructure.optimisticlocking.SaveJsonWithOptimisticLocking;
import ch.dvbern.oss.hemed.esc.api.shared.aufgabe.types.AufgabeStatus;
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
public class AufgabeSaveJson implements SaveJsonWithOptimisticLocking {

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
    private AufgabeStatus status;

    @NotNull
    @NonNull
    @Schema(required = true)
    private LocalDate faelligBis;

    @NotNull
    @NonNull
    @Schema(required = true)
    @Valid
    private BenutzerOptionJson empfaenger;

    @NotNull
    @NonNull
    @Schema(required = true)
    @Valid
    private BenutzerOptionJson userErfasst;

    @NotNull
    @NonNull
    @Schema(required = true)
    private List<LabelJson> labels;

    @Nullable
    @Schema(required = false)
    @Valid
    private KindSearchJson kind;

    @Nullable
    @Schema(required = false)
    @Valid
    private AnlassOptionJson anlass;

    @NotNull
    @NonNull
    @Schema(required = true)
    private List<FileDataJson> files;

    @Nullable
    @Schema(required = false, implementation = URLSchema.class)
    private URL link;

    @NotNull
    @Schema(required = true)
    private long version;
}
