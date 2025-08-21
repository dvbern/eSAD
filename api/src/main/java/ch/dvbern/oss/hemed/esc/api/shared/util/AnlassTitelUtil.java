package ch.dvbern.oss.hemed.esc.api.shared.util;

import ch.dvbern.dvbstarter.shared.i18n.translations.TL;
import ch.dvbern.oss.hemed.esc.api.features.anlass.types.AnlassTyp;
import lombok.experimental.UtilityClass;
import org.checkerframework.checker.nullness.qual.NonNull;

@UtilityClass
public class AnlassTitelUtil {

    @NonNull
    public static String getFullAnlassTitel(@NonNull String titel, int schuljahr, @NonNull AnlassTyp anlassTyp, @NonNull TL tl) {
        String jahr =
            Integer.toString(schuljahr) + '/' + Integer.toString(schuljahr + 1).substring(2);
        String typ =
            tl.translate("AnlassTyp." + anlassTyp);

        return jahr + ' ' + typ + ' ' + titel;
    }
}
