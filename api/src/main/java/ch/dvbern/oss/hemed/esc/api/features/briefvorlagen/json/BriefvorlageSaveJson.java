package ch.dvbern.oss.hemed.esc.api.features.briefvorlagen.json;

import ch.dvbern.dvbstarter.infrastructure.stereotypes.JaxDTO;
import ch.dvbern.dvbstarter.shared.i18n.translations.AppLanguages;
import ch.dvbern.oss.hemed.esc.api.features.briefvorlagen.BriefvorlageType;
import ch.dvbern.oss.hemed.esc.api.infrastructure.optimisticlocking.SaveJsonWithOptimisticLocking;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Value;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.eclipse.microprofile.openapi.annotations.media.Schema;

@Value
@AllArgsConstructor
@JaxDTO
public class BriefvorlageSaveJson implements SaveJsonWithOptimisticLocking {

    @NotNull
    @NotEmpty
    @NonNull
    @Schema(required = true)
    private String name;

    @NotNull
    @NonNull
    @Schema(required = true)
    private BriefvorlageType type;

    @NotNull
    @NonNull
    @Schema(required = true)
    private AppLanguages sprache;

    @Schema(required = true)
    private long version;
}
