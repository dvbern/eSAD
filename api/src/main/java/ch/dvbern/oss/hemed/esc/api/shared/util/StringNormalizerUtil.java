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
        Map.entry("Æ", "Ae"),
        Map.entry("ø", "oe"),
        Map.entry("Ø", "Oe"),
        Map.entry("å", "aa"),
        Map.entry("Å", "Aa"),
        // Characters where Java's NFD Normalizer and PostgreSQL's unaccent() diverge.
        // NFD does not decompose these (they have no combining marks), so we must
        // replace them explicitly to match unaccent()'s behavior.
        Map.entry("Đ", "D"),  // U+0110 D with stroke (Croatian/Vietnamese)
        Map.entry("đ", "d"),
        Map.entry("Ł", "L"),  // U+0141 L with stroke (Polish)
        Map.entry("ł", "l"),
        Map.entry("Þ", "Th"), // U+00DE Thorn (Icelandic)
        Map.entry("þ", "th"),
        Map.entry("Ð", "D"),  // U+00D0 Eth (Icelandic)
        Map.entry("ð", "d"),
        Map.entry("Œ", "Oe"), // U+0152 Latin ligature OE
        Map.entry("œ", "oe")
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
