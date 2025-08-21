package ch.dvbern.oss.hemed.esc.api.features.anlass.json;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import ch.dvbern.dvbstarter.infrastructure.stereotypes.JaxDTO;
import ch.dvbern.oss.hemed.esc.api.features.anlass.types.ImpfStatus;
import ch.dvbern.oss.hemed.esc.api.features.benutzer.json.BenutzerOptionJson;
import ch.dvbern.oss.hemed.esc.api.features.impfstoff.json.ImpfstoffChargeJson;
import ch.dvbern.oss.hemed.esc.api.features.impfstoff.json.ImpfstoffJson;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.eclipse.microprofile.openapi.annotations.media.Schema;

@Getter
@Setter
@AllArgsConstructor
@JaxDTO
public class UntersuchungImpfungInternSaveJson {

    @Valid
    @NonNull
    @NotNull
    @Schema(required = true)
    private ImpfstoffJson impfstoff;

    @Valid
    @Nullable
    @Schema(required = false)
    private ImpfstoffChargeJson charge;

    @Nullable
    @Schema(required = false)
    private LocalDate datumImpfung;

    @Valid
    @Nullable
    @Schema(required = false)
    private BenutzerOptionJson userDurchgefuehrt;

    @NonNull
    @NotNull
    @Schema(required = true)
    private ImpfStatus impfStatus;

    @Nullable
    @Schema(required = false)
    private String grundNichtDurchfuehrung;

    @Valid
    @NonNull
    @NotNull
    @Schema(required = true)
    private List<UntersuchungImpfungKrankheitImmunitaetJson> krankheitenImmunitaeten = new ArrayList<>();

}
