package ch.dvbern.oss.hemed.esc.api.shared.util;

import ch.dvbern.oss.hemed.esc.api.features.anlass.AnlassConfig;
import ch.dvbern.oss.hemed.esc.api.features.anlass.groessegewicht.PerzentileBmi;
import ch.dvbern.oss.hemed.esc.api.features.anlass.groessegewicht.PerzentileGewicht;
import ch.dvbern.oss.hemed.esc.api.features.anlass.groessegewicht.PerzentileGroesse;
import ch.dvbern.oss.hemed.esc.api.features.anlass.groessegewicht.UntersuchungGroesseGewicht;
import lombok.experimental.UtilityClass;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;

@UtilityClass
public class UntersuchungGroesseGewichtUtil {

    @Nullable
    public static PerzentileGroesse getGroessePerzentile(
        @NonNull AnlassConfig config,
        @Nullable UntersuchungGroesseGewicht groesseGewicht,
        @NonNull PerzentileGroesse defaultWert
    ) {
        if (config.getGroesseGewichtGroessePerzentile().isVisible()) {
            if (groesseGewicht != null && groesseGewicht.getGroessePerzentile() != null) {
                return groesseGewicht.getGroessePerzentile();
            }
            return defaultWert;
        }
        return null;
    }

    @Nullable
    public static PerzentileGewicht getGewichtPerzentile(
        @NonNull AnlassConfig config,
        @Nullable UntersuchungGroesseGewicht groesseGewicht,
        @NonNull PerzentileGewicht defaultWert
    ) {
        if (config.getGroesseGewichtGewichtPerzentile().isVisible()) {
            if (groesseGewicht != null && groesseGewicht.getGewichtPerzentile() != null) {
                return groesseGewicht.getGewichtPerzentile();
            }
            return defaultWert;
        }
        return null;
    }

    @Nullable
    public static PerzentileBmi getBmiPerzentile(
        @NonNull AnlassConfig config,
        @Nullable UntersuchungGroesseGewicht groesseGewicht,
        @NonNull PerzentileBmi defaultWert
    ) {
        if (config.getGroesseGewichtBmiPerzentile().isVisible()) {
            if (groesseGewicht != null && groesseGewicht.getBmiPerzentile() != null) {
                return groesseGewicht.getBmiPerzentile();
            }
            return defaultWert;
        }
        return null;
    }
}
