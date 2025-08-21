package ch.dvbern.oss.hemed.esc.api.features.anlass.hoeren;

import java.util.List;

import ch.dvbern.dvbstarter.infrastructure.stereotypes.JaxDTO;
import ch.dvbern.oss.hemed.esc.api.features.anlass.json.AnlassConfigJson;
import ch.dvbern.oss.hemed.esc.api.shared.anlass.UntersuchungHoerenDefaultValueUtil;
import lombok.AllArgsConstructor;
import lombok.Value;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.eclipse.microprofile.openapi.annotations.media.Schema;

@Value
@AllArgsConstructor
@JaxDTO
public class UntersuchungHoerenOtoskopieSaveJson {

    @Nullable
    @Schema(required = false)
    private List<OtoskopieResultat> otoskopieRechts;

    @Nullable
    @Schema(required = false)
    private String otoskopieRechtsResultatAndere;

    @Nullable
    @Schema(required = false)
    private List<OtoskopieResultat> otoskopieLinks;

    @Nullable
    @Schema(required = false)
    private String otoskopieLinksResultatAndere;

    @Nullable
    @Schema(required = false)
    private String bemerkungen;

    @NonNull
    public static UntersuchungHoerenOtoskopieSaveJson defaultOtoskopieValuesForAnlassConfig(
        @NonNull AnlassConfigJson anlassConfig) {
        return new UntersuchungHoerenOtoskopieSaveJson(
            UntersuchungHoerenDefaultValueUtil.getDefaultOtoskopieResultat(
                anlassConfig.getHoerenOtoskopie(),
                anlassConfig.getAnlassTyp()),
            null,
            UntersuchungHoerenDefaultValueUtil.getDefaultOtoskopieResultat(
                anlassConfig.getHoerenOtoskopie(),
                anlassConfig.getAnlassTyp()),
            null,
            null
        );
    }
}
