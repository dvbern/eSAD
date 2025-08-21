package ch.dvbern.oss.hemed.esc.api.features.anlass.massnahmen;

import java.util.List;

import ch.dvbern.dvbstarter.infrastructure.stereotypes.JaxDTO;
import ch.dvbern.oss.hemed.esc.api.features.infomaterial.json.InfomaterialJson;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Value;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.eclipse.microprofile.openapi.annotations.media.Schema;

@Value
@AllArgsConstructor
@JaxDTO
public class UntersuchungSehenMassnahmenSaveJson {

    @NotNull
    @Schema(required = true)
    private boolean keineMassnahmen;

    @NotNull
    @Schema(required = true)
    private boolean bereitsBehandeltSehen;

    @NotNull
    @NonNull
    @Schema(required = true)
    private List<InfomaterialJson> abgegebenesInfomaterial;

    @Nullable
    @Schema(required = false)
    private InformationEltern informationEltern;

    @NotNull
    @Schema(required = true)
    private boolean ueberweisungAugenarzt;

    @NotNull
    @Schema(required = true)
    private boolean nachkontrolleSehen;
}
