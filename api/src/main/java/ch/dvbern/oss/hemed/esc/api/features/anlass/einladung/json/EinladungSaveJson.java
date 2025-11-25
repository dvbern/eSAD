package ch.dvbern.oss.hemed.esc.api.features.anlass.einladung.json;

import java.util.List;

import ch.dvbern.dvbstarter.infrastructure.stereotypes.JaxDTO;
import ch.dvbern.dvbstarter.shared.types.id.ID;
import ch.dvbern.oss.hemed.esc.api.features.anlass.Anlass;
import ch.dvbern.oss.hemed.esc.api.features.anlass.einladung.types.ArtAbmeldung;
import ch.dvbern.oss.hemed.esc.api.features.anlass.einladung.types.EinladungStatus;
import ch.dvbern.oss.hemed.esc.api.features.anlass.einladung.types.GrundAbmeldung;
import ch.dvbern.oss.hemed.esc.api.features.kinder.Kind;
import ch.dvbern.oss.hemed.esc.api.features.notizen.json.NotizJson;
import ch.dvbern.oss.hemed.esc.api.infrastructure.optimisticlocking.SaveJsonWithOptimisticLocking;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Value;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.eclipse.microprofile.openapi.annotations.media.Schema;

@Value
@AllArgsConstructor
@JaxDTO
@Schema(description = "Business data for a Einladung")
public class EinladungSaveJson implements SaveJsonWithOptimisticLocking {

    @NotNull
    @NonNull
    @Schema(required = true)
    private final ID<Anlass> anlassId;

    @NotNull
    @NonNull
    @Schema(required = true)
    private final ID<Kind> kindId;

    @NotNull
    @NonNull
    @Schema(required = true)
    private final EinladungStatus einladungStatus;

    @Nullable
    @Schema(required = false)
    private final GrundAbmeldung grundAbmeldung;

    @Nullable
    @Schema(required = false)
    private final ArtAbmeldung artAbmeldung;

    @Nullable
    @Schema(required = false)
    private final String bemerkungAbmeldung;

    @NotNull
    @NonNull
    @Schema(required = true)
    private List<NotizJson> notizen;

    @Schema(required = true)
    private long version;

    @Schema(required = false)
    private long versionAbmeldungsNotiz;

}
