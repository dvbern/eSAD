package ch.dvbern.oss.hemed.esc.api.features.anlass.groessegewicht;

import java.math.BigDecimal;

import ch.dvbern.dvbstarter.infrastructure.stereotypes.JaxDTO;
import ch.dvbern.oss.hemed.esc.api.infrastructure.optimisticlocking.SaveJsonWithOptimisticLocking;
import lombok.AllArgsConstructor;
import lombok.Value;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.eclipse.microprofile.openapi.annotations.media.Schema;

@Value
@AllArgsConstructor
@JaxDTO
public class UntersuchungGroesseGewichtSaveJson implements SaveJsonWithOptimisticLocking {

    @Nullable
    @Schema(required = false)
    private Integer groesse;

    @Nullable
    @Schema(required = false)
    private BigDecimal gewicht;

    @Nullable
    @Schema(required = false)
    private PerzentileGroesse groessePerzentile;

    @Nullable
    @Schema(required = false)
    private PerzentileGewicht gewichtPerzentile;

    @Nullable
    @Schema(required = false)
    private PerzentileBmi bmiPerzentile;

    @Nullable
    @Schema(required = false)
    private String bemerkungen;

    @Schema(required = true)
    private long version;

}
