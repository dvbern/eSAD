package ch.dvbern.oss.hemed.esc.api.shared.util;

import ch.dvbern.oss.hemed.esc.api.features.anlass.AnlassConfig;
import ch.dvbern.oss.hemed.esc.api.features.anlass.UntersuchungDurchfuehrung;
import ch.dvbern.oss.hemed.esc.api.shared.benutzer.Benutzer;
import lombok.experimental.UtilityClass;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;

@UtilityClass
public class UntersuchungDurchfuehrungUtil {

    @Nullable
    public static Benutzer getArzt(
        @NonNull AnlassConfig config,
        @Nullable UntersuchungDurchfuehrung durchfuehrung,
        @Nullable Benutzer defaultWert
    ) {
        if (config.getDatenUntersuchung().isVisible()) {
            if (durchfuehrung != null && durchfuehrung.getArzt() != null) {
                return durchfuehrung.getArzt();
            }
            return defaultWert;
        }
        return null;
    }

    @Nullable
    public static Benutzer getMpa(
        @NonNull AnlassConfig config,
        @Nullable UntersuchungDurchfuehrung durchfuehrung,
        @Nullable Benutzer defaultWert
    ) {
        if (config.getDatenUntersuchung().isVisible()) {
            if (durchfuehrung != null && durchfuehrung.getMpa() != null) {
                return durchfuehrung.getMpa();
            }
            return defaultWert;
        }
        return null;
    }
}
