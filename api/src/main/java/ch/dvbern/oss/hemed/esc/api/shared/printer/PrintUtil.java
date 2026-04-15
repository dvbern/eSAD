package ch.dvbern.oss.hemed.esc.api.shared.printer;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.util.regex.Pattern;

import ch.dvbern.dvbstarter.shared.i18n.translations.TL;
import ch.dvbern.oss.hemed.esc.api.shared.benutzer.Benutzer;
import ch.dvbern.oss.hemed.esc.api.shared.person.Geschlecht;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;

public final class PrintUtil {
    public static final String PLACEHOLDER = "-";
    public static final DateTimeFormatter PRINT_DATE_FORMAT = DateTimeFormatter.ofPattern("dd.MM.yyyy");
    public static final DateTimeFormatter PRINT_TIME_FORMAT = DateTimeFormatter.ofPattern("HH:mm");
    public static final Pattern WHITESPACE = Pattern.compile("\\s", Pattern.UNICODE_CHARACTER_CLASS);
    private static final PrintUtilImpl PRINT_UTIL =
        new PrintUtilImpl(PLACEHOLDER, PRINT_DATE_FORMAT, PRINT_TIME_FORMAT, PDFAPrintUtil::sanitizeForPDFA);


    private PrintUtil() {
        // utility class
    }

    @NonNull
    public static String trimToPlaceholder(@Nullable String input) {
        return PRINT_UTIL.trimToPlaceholder(input);
    }

    @NonNull
    public static String trimToPlaceholder(@Nullable Benutzer benutzer) {
        return PRINT_UTIL.trimNameVornameToPlaceholder(benutzer);
    }

    @NonNull
    public static String trimToPlaceholder(@Nullable LocalDate date) {
        return PRINT_UTIL.trimToPlaceholder(date);
    }

    @NonNull
    public static String trimToPlaceholder(@Nullable LocalDate date, @NonNull String format) {
        return PRINT_UTIL.trimToPlaceholder(date, format);
    }

    @NonNull
    public static String trimToPlaceholder(@Nullable Integer integer) {
        return PRINT_UTIL.trimToPlaceholder(integer);
    }

    @NonNull
    public static String trimToPlaceholder(@Nullable BigDecimal bigDecimal) {
        return PRINT_UTIL.trimToPlaceholder(bigDecimal);
    }

    @NonNull
    public static String trimToPlaceholder(@Nullable Boolean input, TL tl) {
        return PRINT_UTIL.trimToPlaceholder(input, tl);
    }

    @NonNull
    public static String replaceWhitespaceWithNonBreakingSpace(@NonNull String input) {
        return PRINT_UTIL.replaceWhitespaceWithNonBreakingSpace(input);
    }

    @NonNull
    public static String formatDate(@NonNull LocalDate date) {
        return PRINT_UTIL.formatDate(date);
    }

    @NonNull
    public static String formatDate(@NonNull OffsetDateTime date) {
        return PRINT_UTIL.formatDate(date);
    }

    @NonNull
    public static String formatTime(@NonNull LocalDateTime localDateTime) {
        return PRINT_UTIL.formatTime(localDateTime);
    }

    @NonNull
    public static String formatLocalDateTime(@Nullable LocalDateTime dateTime) {
        if (dateTime == null) {
            return PrintUtil.PLACEHOLDER;
        }
        return dateTime.format(DateTimeFormatter.ofPattern("HH:mm"));
    }

    @NonNull
    public static String getNameWithAnrede(@NonNull BasePrinter basePrinter, @NonNull Geschlecht geschlecht, @NonNull String nameVorname) {
        return PRINT_UTIL.getNameWithAnrede(basePrinter,geschlecht, nameVorname);
    }
}
