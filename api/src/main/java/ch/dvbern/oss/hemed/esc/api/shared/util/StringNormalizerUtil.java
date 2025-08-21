package ch.dvbern.oss.hemed.esc.api.shared.util;

import java.text.Normalizer;
import java.text.Normalizer.Form;
import java.util.Map;
import java.util.regex.Pattern;

import lombok.experimental.UtilityClass;
import org.checkerframework.checker.nullness.qual.NonNull;

@UtilityClass
public final class StringNormalizerUtil {

    public static final Pattern COMBINING_DIACRITICAL_MARKS_PATTERN = Pattern.compile("\\p{InCombiningDiacriticalMarks}+");

    private static final Map<String, String> MULTI_CHAR_REPLACEMENTS = Map.ofEntries(
        Map.entry("ä", "ae"),
        Map.entry("ö", "oe"),
        Map.entry("ü", "ue"),
        Map.entry("Ä", "Ae"),
        Map.entry("Ö", "Oe"),
        Map.entry("Ü", "Ue"),
        Map.entry("ß", "ss"),
        Map.entry("æ", "ae"),
        Map.entry("ø", "oe"),
        Map.entry("å", "aa")
    );

    @NonNull
    public static String normalizeAndStripDiarcritics(@NonNull String stringToNormalize) {
        // Der Normaliser ersetzt nur einzelne Zeichen ("ä" -> "a"), daher müssen wir mehrfache Zeichen
        // ("ä" -> "ae") explizit ersetzen, bevor wir den Normaliser aufrufen.
        for (Map.Entry<String, String> entry : MULTI_CHAR_REPLACEMENTS.entrySet()) {
            stringToNormalize = stringToNormalize.replace(entry.getKey(), entry.getValue());
        }

        String normalized = Normalizer.normalize(stringToNormalize, Form.NFD);
        return COMBINING_DIACRITICAL_MARKS_PATTERN.matcher(normalized).replaceAll("").toLowerCase();
    }
}
