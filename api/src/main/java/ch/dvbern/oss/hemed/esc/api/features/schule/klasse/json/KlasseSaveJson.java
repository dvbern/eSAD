package ch.dvbern.oss.hemed.esc.api.features.schule.klasse.json;

import ch.dvbern.dvbstarter.infrastructure.stereotypes.JaxDTO;
import ch.dvbern.oss.hemed.esc.api.features.schule.json.SchuleOptionJson;
import ch.dvbern.oss.hemed.esc.api.features.schule.types.Klassenstufe;
import ch.dvbern.oss.hemed.esc.api.infrastructure.optimisticlocking.SaveJsonWithOptimisticLocking;
import ch.dvbern.oss.hemed.esc.api.shared.types.EntityAktivStatus;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Value;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.eclipse.microprofile.openapi.annotations.media.Schema;

@Value
@AllArgsConstructor
@JaxDTO
public class KlasseSaveJson implements SaveJsonWithOptimisticLocking {

    @Valid
    @NonNull
    @NotNull
    @Schema(required = true)
    private SchuleOptionJson schule;

    @Valid
    @NonNull
    @NotNull
    @Schema(required = true)
    private String bezeichnung;

    @NonNull
    @NotNull
    @Schema(required = true)
    private final Klassenstufe klassenstufe;

    @NonNull
    @NotNull
    @Schema(required = true)
    private String klasseSchulart;

    @NotNull
    @Schema(required = true)
    private EntityAktivStatus klasseAktivStatus;

    @NotNull
    @Schema(required = true)
    private long version;
}
