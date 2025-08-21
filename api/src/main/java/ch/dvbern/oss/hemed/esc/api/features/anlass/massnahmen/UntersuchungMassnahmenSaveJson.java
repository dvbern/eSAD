package ch.dvbern.oss.hemed.esc.api.features.anlass.massnahmen;

import java.util.List;

import ch.dvbern.dvbstarter.infrastructure.stereotypes.JaxDTO;
import ch.dvbern.oss.hemed.esc.api.features.infomaterial.json.InfomaterialJson;
import ch.dvbern.oss.hemed.esc.api.shared.anlass.anlassflags.AnlassFlagsJson;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Value;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.eclipse.microprofile.openapi.annotations.media.Schema;

@Value
@AllArgsConstructor
@JaxDTO
public class UntersuchungMassnahmenSaveJson {

    @Schema(required = true)
    private boolean keineMassnahmen;

    @Schema(required = true)
    private boolean beratungMPA;

    @Schema(required = true)
    private boolean beratungSchulAerztin;

    @Schema(required = true)
    private boolean anderes;

    @Nullable
    @Schema(required = false)
    private String anderesBemerkung;

    @NotNull
    @Schema(required = true)
    private boolean bereitsBehandeltSehen;

    @NotNull
    @Schema(required = true)
    private boolean bereitsBehandeltHoeren;

    @NotNull
    @Schema(required = true)
    private boolean bereitsBehandeltGroesseGewicht;

    @NotNull
    @Schema(required = true)
    private boolean bereitsBehandeltSprache;

    @NotNull
    @Schema(required = true)
    private boolean bereitsBehandeltMotorik;

    @NotNull
    @Schema(required = true)
    private boolean bereitsBehandeltAnderes;

    @Nullable
    @Schema(required = false)
    private String bereitsBehandeltAnderesBemerkung;

    @NotNull
    @NonNull
    @Schema(required = true)
    private List<InfomaterialJson> abgegebenesInfomaterial;

    @Nullable
    @Schema(required = false)
    private InformationEltern informationEltern;

    @Schema(required = true)
    private boolean ueberweisungHausarzt;

    @Schema(required = true)
    private boolean ueberweisungAugenarzt;

    @NotNull
    @Schema(required = true)
    @Valid
    private AnlassFlagsJson nachkontrolle;

    @Nullable
    @Schema(required = false)
    private String bemerkungen;
}
