package ch.dvbern.oss.hemed.esc.api.shared.util;

import java.util.Objects;

import ch.dvbern.dvbstarter.shared.i18n.translations.TL;
import ch.dvbern.oss.hemed.esc.api.features.anlass.types.AnlassTyp;
import lombok.experimental.UtilityClass;
import org.apache.commons.lang3.StringUtils;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;

@UtilityClass
public class AnlassTitelUtil {

    @NonNull
    public static String getFullAnlassTitel(
        @Nullable String titel,
        int schuljahr,
        @NonNull AnlassTyp anlassTyp,
        @NonNull TL tl
    ) {
        String typ = tl.translate("AnlassTyp." + anlassTyp);
        return getFullAnlassTitel(titel, schuljahr, typ);
    }

    @NonNull
    public static String getFullAnlassTitel(
        @Nullable String titel,
        int schuljahr,
        @NonNull String translatedAnlassType
    ) {
        String prefix = getAnlassTitelPrefix(schuljahr, translatedAnlassType);
        return Objects.isNull(titel) || StringUtils.isBlank(titel.strip())
            ? prefix
            : prefix + ' ' + titel.strip();
    }

    @NonNull
    private static String getAnlassTitelPrefix(int schuljahr, @NonNull String translatedAnlassType) {
        int jahrNextTwoDigits = (schuljahr + 1) % 100;
        return "%d/%02d %s".formatted(schuljahr, jahrNextTwoDigits, translatedAnlassType);
    }
}
