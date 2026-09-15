package ch.dvbern.oss.hemed.esc.api.shared.anlass;

import ch.dvbern.oss.hemed.esc.api.features.anlass.anlassconfig.AnlassConfig;
import ch.dvbern.oss.hemed.esc.api.features.anlass.hoeren.json.AudiogrammSaveJson;
import ch.dvbern.oss.hemed.esc.api.features.anlass.hoeren.types.Dezibel;
import lombok.experimental.UtilityClass;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;

@UtilityClass
public class UntersuchungHoerenDefaultValueUtil {

    @Nullable
    public static AudiogrammSaveJson getDefaultAudiogramm(
        @NonNull AnlassConfig config
    ) {
        if (!config.getHoerenAudiogramm().isVisible()) {
            return null;
        }
        Dezibel mainDefault = config.getHoerenAudiogrammDefaultValue();
        Dezibel default250Hz = config.getHoerenAudiogramm250Hz().isVisible()
            ? config.getHoerenAudiogramm250HzDefaultValue()
            : null;
        Dezibel default8000Hz = config.getHoerenAudiogramm8000Hz().isVisible()
            ? config.getHoerenAudiogramm8000HzDefaultValue()
            : null;
        return new AudiogrammSaveJson(
            default250Hz,
            mainDefault,
            mainDefault,
            mainDefault,
            mainDefault,
            mainDefault,
            default8000Hz,
            0L
        );
    }
}
