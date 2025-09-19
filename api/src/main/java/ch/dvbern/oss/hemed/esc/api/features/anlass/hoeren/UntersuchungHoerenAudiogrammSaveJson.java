package ch.dvbern.oss.hemed.esc.api.features.anlass.hoeren;

import java.util.List;

import ch.dvbern.dvbstarter.infrastructure.stereotypes.JaxDTO;
import ch.dvbern.oss.hemed.esc.api.features.anlass.json.AnlassConfigJson;
import ch.dvbern.oss.hemed.esc.api.infrastructure.optimisticlocking.SaveJsonWithOptimisticLocking;
import ch.dvbern.oss.hemed.esc.api.shared.anlass.UntersuchungHoerenDefaultValueUtil;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import lombok.Value;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.eclipse.microprofile.openapi.annotations.media.Schema;

@Value
@AllArgsConstructor
@JaxDTO
public class UntersuchungHoerenAudiogrammSaveJson implements SaveJsonWithOptimisticLocking {

    @Nullable
    @Valid
    @Schema(required = false)
    private AudiogrammSaveJson audiogrammRechts;

    @Nullable
    @Valid
    @Schema(required = false)
    private AudiogrammSaveJson audiogrammLinks;

    @Nullable
    @Schema(required = false)
    private List<AudiogrammBeurteilung> audiogrammBeurteilung;

    @Nullable
    @Schema(required = false)
    private List<HoerenZusaetzlicheAngaben> zusaetzlicheAngaben;

    @Nullable
    @Schema(required = false)
    private String bemerkungen;

    @Schema(required = true)
    private long version;

    @NonNull
    public static UntersuchungHoerenAudiogrammSaveJson defaultAudiogrammValuesForAnlassConfig(
        @NonNull AnlassConfigJson anlassConfig) {
        return new UntersuchungHoerenAudiogrammSaveJson(
            UntersuchungHoerenDefaultValueUtil.getDefaultAudiogramm(
                anlassConfig.getHoerenAudiogramm(),
                anlassConfig.getAnlassTyp()),
            UntersuchungHoerenDefaultValueUtil.getDefaultAudiogramm(
                anlassConfig.getHoerenAudiogramm(),
                anlassConfig.getAnlassTyp()),
            UntersuchungHoerenDefaultValueUtil.getDefaultAudiogrammBeurteilung(anlassConfig.getHoerenAudiogrammBeurteilung()),
            UntersuchungHoerenDefaultValueUtil.getDefaultZusaetzlicheAngaben(anlassConfig.getHoerenAudiogrammZusaetzlicheAngaben()),
            null,
            0L
        );
    }
}
