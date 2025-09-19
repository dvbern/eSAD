package ch.dvbern.oss.hemed.esc.api.features.impfstoff.json;

import java.util.List;

import ch.dvbern.dvbstarter.infrastructure.stereotypes.JaxDTO;
import ch.dvbern.oss.hemed.esc.api.features.impfstoff.types.Krankheit;
import ch.dvbern.oss.hemed.esc.api.infrastructure.optimisticlocking.SaveJsonWithOptimisticLocking;
import ch.dvbern.oss.hemed.esc.api.shared.types.EntityAktivStatus;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.eclipse.microprofile.openapi.annotations.media.Schema;

@Getter
@Setter
@AllArgsConstructor
@JaxDTO
public class ImpfstoffSaveJson implements SaveJsonWithOptimisticLocking {
    @NotNull
    @NonNull
    @Schema(required = true)
    private final String name;

    @NotNull
    @NonNull
    @Schema(required = true)
    private final List<Krankheit> krankheiten;

    @NotNull
    @Schema(required = true)
    private EntityAktivStatus impfstoffAktivStatus;

    @Schema(required = true)
    private long version;
}
