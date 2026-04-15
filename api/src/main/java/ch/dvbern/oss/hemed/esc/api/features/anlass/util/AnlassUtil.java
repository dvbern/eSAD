package ch.dvbern.oss.hemed.esc.api.features.anlass.util;

import ch.dvbern.oss.hemed.esc.api.features.anlass.einladung.Einladung;
import lombok.experimental.UtilityClass;
import org.checkerframework.checker.nullness.qual.NonNull;

@UtilityClass
public class AnlassUtil {

    public static final String ANLASS_URL_PART = "/anlass";

    @NonNull
    public static String createAnlassLink(@NonNull Einladung einladung) {
        return ANLASS_URL_PART + '/' + einladung.getAnlass().getId().getId();
    }
}
