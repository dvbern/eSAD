package ch.dvbern.oss.hemed.esc.api.shared.anlass;

import ch.dvbern.oss.hemed.esc.api.features.anlass.hoeren.json.AudiogrammSaveJson;
import ch.dvbern.oss.hemed.esc.api.features.anlass.hoeren.types.Dezibel;
import ch.dvbern.oss.hemed.esc.api.features.anlass.types.AnlassTyp;
import ch.dvbern.oss.hemed.esc.api.features.anlass.anlassconfig.types.RequirementSetting;
import lombok.experimental.UtilityClass;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;

@UtilityClass
public class UntersuchungHoerenDefaultValueUtil {

    @Nullable
    public static AudiogrammSaveJson getDefaultAudiogramm(
        @NonNull RequirementSetting requirementSetting,
        @NonNull AnlassTyp anlassTyp
    ) {
        if (requirementSetting.isVisible()) {
            if (anlassTyp == AnlassTyp.KINDERGARTEN) {
                return new AudiogrammSaveJson(
                    null,
                    Dezibel.DB20,
                    Dezibel.DB20,
                    Dezibel.DB20,
                    Dezibel.DB20,
                    Dezibel.DB20,
                    null,
                    0L
                );
            }
            return new AudiogrammSaveJson(
                Dezibel.DB20,
                Dezibel.DB20,
                Dezibel.DB20,
                Dezibel.DB20,
                Dezibel.DB20,
                Dezibel.DB20,
                Dezibel.DB20,
                0L
            );
        }
        return null;
    }
}
