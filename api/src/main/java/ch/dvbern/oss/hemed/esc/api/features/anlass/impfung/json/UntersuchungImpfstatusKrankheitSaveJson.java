package ch.dvbern.oss.hemed.esc.api.features.anlass.impfung.json;

import java.time.LocalDate;

import ch.dvbern.dvbstarter.infrastructure.stereotypes.JaxDTO;
import ch.dvbern.oss.hemed.esc.api.features.anlass.impfung.types.ImpfschutzGegeben;
import ch.dvbern.oss.hemed.esc.api.features.impfstoff.types.Krankheit;
import ch.dvbern.oss.hemed.esc.api.infrastructure.optimisticlocking.SaveJsonWithOptimisticLocking;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.eclipse.microprofile.openapi.annotations.media.Schema;

@Getter
@Setter
@JaxDTO
@AllArgsConstructor
public class UntersuchungImpfstatusKrankheitSaveJson implements SaveJsonWithOptimisticLocking {

    @NonNull
    @NotNull
    @Schema(required = true)
    private Krankheit krankheit;

    @Nullable
    @Schema(required = false)
    private Integer anzahlImpfungen;

    @Nullable
    @Schema(required = false)
    private LocalDate datumLetzteImpfung;

    @Schema(required = false)
    private ImpfschutzGegeben impfschutzGegeben = ImpfschutzGegeben.KEINE_ANGABE;

    @Schema(required = false)
    private boolean impfempfehlung = false;

    @Schema(required = false)
    private boolean impfentscheid = false;

    @Schema(required = true)
    private long version;

}
