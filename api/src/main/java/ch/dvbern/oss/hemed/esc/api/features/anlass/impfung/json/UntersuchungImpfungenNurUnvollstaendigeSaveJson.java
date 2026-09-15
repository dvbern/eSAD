package ch.dvbern.oss.hemed.esc.api.features.anlass.impfung.json;

import jakarta.validation.constraints.NotNull;

import ch.dvbern.dvbstarter.infrastructure.stereotypes.JaxDTO;
import ch.dvbern.oss.hemed.esc.api.features.anlass.impfung.types.ImpfausweisGrundUnvollstaendig;
import ch.dvbern.oss.hemed.esc.api.features.anlass.impfung.types.ImpfausweisVollstaendig;
import ch.dvbern.oss.hemed.esc.api.infrastructure.optimisticlocking.SaveJsonWithOptimisticLocking;

import lombok.Value;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.eclipse.microprofile.openapi.annotations.media.Schema;

@Value
@JaxDTO
public class UntersuchungImpfungenNurUnvollstaendigeSaveJson implements SaveJsonWithOptimisticLocking {

    @Nullable
    @Schema(required = false)
    private ImpfausweisVollstaendig impfausweisVollstaendig;

    @Nullable
    @Schema(required = false)
    private ImpfausweisGrundUnvollstaendig impfausweisGrundUnvollstaendig;

    @Schema(required = true)
    private long version;
}
