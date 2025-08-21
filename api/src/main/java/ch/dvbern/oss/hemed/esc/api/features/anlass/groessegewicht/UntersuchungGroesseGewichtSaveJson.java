package ch.dvbern.oss.hemed.esc.api.features.anlass.groessegewicht;

import ch.dvbern.dvbstarter.infrastructure.stereotypes.JaxDTO;
import lombok.AllArgsConstructor;
import lombok.Value;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.eclipse.microprofile.openapi.annotations.media.Schema;

@Value
@AllArgsConstructor
@JaxDTO
public class UntersuchungGroesseGewichtSaveJson {

    @Nullable
    @Schema(required = false)
    private Integer groesse;

    @Nullable
    @Schema(required = false)
    private Integer gewicht;

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

}
