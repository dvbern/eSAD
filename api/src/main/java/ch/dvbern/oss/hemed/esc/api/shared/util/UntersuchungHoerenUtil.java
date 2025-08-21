package ch.dvbern.oss.hemed.esc.api.shared.util;

import java.util.List;

import ch.dvbern.oss.hemed.esc.api.features.anlass.AnlassConfig;
import ch.dvbern.oss.hemed.esc.api.features.anlass.hoeren.AudiogrammBeurteilung;
import ch.dvbern.oss.hemed.esc.api.features.anlass.hoeren.HoerenZusaetzlicheAngaben;
import ch.dvbern.oss.hemed.esc.api.features.anlass.hoeren.OtoskopieResultat;
import ch.dvbern.oss.hemed.esc.api.features.anlass.hoeren.UntersuchungHoerenAudiogramm;
import ch.dvbern.oss.hemed.esc.api.features.anlass.hoeren.UntersuchungHoerenOtoskopie;
import lombok.experimental.UtilityClass;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;

@UtilityClass
public class UntersuchungHoerenUtil {

    @Nullable
    public static List<AudiogrammBeurteilung> getAudiogrammBeurteilung(
        @NonNull AnlassConfig config,
        @Nullable UntersuchungHoerenAudiogramm hoeren,
        @NonNull AudiogrammBeurteilung defaultWert
    ) {
        if (config.getHoerenAudiogramm().isVisible()) {
            if (hoeren != null && !hoeren.getAudiogrammBeurteilung().isEmpty()) {
                return hoeren.getAudiogrammBeurteilung();
            }
            return List.of(defaultWert);
        }
        return null;
    }

    @Nullable
    public static List<HoerenZusaetzlicheAngaben> getZusaetzlicheAngaben(
        @NonNull AnlassConfig config,
        @Nullable UntersuchungHoerenAudiogramm hoeren,
        @NonNull HoerenZusaetzlicheAngaben defaultWert
    ) {
        if (config.getHoerenAudiogrammZusaetzlicheAngaben().isVisible()) {
            if (hoeren != null && !hoeren.getZusaetzlicheAngaben().isEmpty()) {
                return hoeren.getZusaetzlicheAngaben();
            }
            return List.of(defaultWert);
        }
        return null;
    }

    @Nullable
    public static List<OtoskopieResultat> getOtoskopieRechts(
        @NonNull AnlassConfig config,
        @Nullable UntersuchungHoerenOtoskopie hoeren,
        @NonNull OtoskopieResultat defaultWert
    ) {
        if (config.getHoerenOtoskopie().isVisible()) {
            if (hoeren != null && !hoeren.getOtoskopieRechts().isEmpty()) {
                return hoeren.getOtoskopieRechts();
            }
            return List.of(defaultWert);
        }
        return null;
    }

    @Nullable
    public static List<OtoskopieResultat> getOtoskopieLinks(
        @NonNull AnlassConfig config,
        @Nullable UntersuchungHoerenOtoskopie hoeren,
        @NonNull OtoskopieResultat defaultWert
    ) {
        if (config.getHoerenOtoskopie().isVisible()) {
            if (hoeren != null && !hoeren.getOtoskopieLinks().isEmpty()) {
                return hoeren.getOtoskopieLinks();
            }
            return List.of(defaultWert);
        }
        return null;
    }
}
