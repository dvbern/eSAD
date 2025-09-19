package ch.dvbern.oss.hemed.esc.api.shared.anlass;

import java.util.List;

import ch.dvbern.oss.hemed.esc.api.features.anlass.hoeren.AudiogrammBeurteilung;
import ch.dvbern.oss.hemed.esc.api.features.anlass.hoeren.AudiogrammSaveJson;
import ch.dvbern.oss.hemed.esc.api.features.anlass.hoeren.Dezibel;
import ch.dvbern.oss.hemed.esc.api.features.anlass.hoeren.HoerenZusaetzlicheAngaben;
import ch.dvbern.oss.hemed.esc.api.features.anlass.hoeren.OtoskopieResultat;
import ch.dvbern.oss.hemed.esc.api.features.anlass.types.AnlassTyp;
import ch.dvbern.oss.hemed.esc.api.features.anlass.types.RequirementSetting;
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

    @Nullable
    public static List<AudiogrammBeurteilung> getDefaultAudiogrammBeurteilung(
        @NonNull RequirementSetting requirementSetting
    ) {
        if (requirementSetting.isVisible()) {
            return List.of(AudiogrammBeurteilung.NORMAL);
        }
        return null;
    }

    @Nullable
    public static List<HoerenZusaetzlicheAngaben> getDefaultZusaetzlicheAngaben(
        @NonNull RequirementSetting requirementSetting
    ) {
        if (requirementSetting.isVisible()) {
            return List.of(HoerenZusaetzlicheAngaben.KEINE);
        }
        return null;
    }

    @Nullable
    public static List<OtoskopieResultat> getDefaultOtoskopieResultat(
        @NonNull RequirementSetting requirementSetting,
        @NonNull AnlassTyp anlassTyp
    ) {
        if (requirementSetting.isVisible() &&
            List.of(AnlassTyp.KLASSE_8, AnlassTyp.KLASSE_9, AnlassTyp.KLASSE_4, AnlassTyp.FREMDSPRACHEN)
                .contains(anlassTyp)) {
            return List.of(OtoskopieResultat.NICHT_GEPRUEFT);
        }
        return null;
    }
}
