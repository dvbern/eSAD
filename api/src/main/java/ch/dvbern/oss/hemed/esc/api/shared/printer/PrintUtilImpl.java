package ch.dvbern.oss.hemed.esc.api.shared.printer;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.util.function.Function;

import ch.dvbern.dvbstarter.shared.i18n.translations.TL;
import ch.dvbern.oss.hemed.esc.api.shared.benutzer.Benutzer;
import ch.dvbern.oss.hemed.esc.api.shared.person.Geschlecht;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;

@AllArgsConstructor
public final class PrintUtilImpl {
    @Getter
    public final String placeholder;
    public final DateTimeFormatter printDateFormat;
    public final DateTimeFormatter printTimeFormat;
    public final Function<String, String> stringSanitizer; // will be applied to strings, if you don't need it pass the identity function

    public PrintUtilImpl(@NonNull String placeholder) {
        this.placeholder = placeholder;
        this.printDateFormat = DateTimeFormatter.ofPattern("dd.MM.yyyy");
        this.printTimeFormat = DateTimeFormatter.ofPattern("HH:mm");
        this.stringSanitizer = Function.identity();
    }

    @NonNull
    public String trimToPlaceholder(@Nullable String input) {
        return input == null || input.isBlank() ? placeholder : stringSanitizer.apply(input.trim());
    }

    @NonNull
    public String trimNameVornameToPlaceholder(@Nullable Benutzer benutzer) {
        return benutzer == null ? placeholder : stringSanitizer.apply(benutzer.getNameVorname());
    }

    @NonNull
    public String trimVornameNameToPlaceholder(@Nullable Benutzer benutzer) {
        return benutzer == null ? placeholder : stringSanitizer.apply(benutzer.getVornameName());
    }

    @NonNull
    public String trimToPlaceholder(@Nullable LocalDate date) {
        return date == null ? placeholder : date.format(printDateFormat);
    }

    @NonNull
    public String trimToPlaceholder(@Nullable LocalDate date, @NonNull String format) {
        return date == null ? placeholder : date.format(DateTimeFormatter.ofPattern(format));
    }

    @NonNull
    public String trimToPlaceholder(@Nullable Integer integer) {
        return integer == null ? placeholder : String.valueOf(integer);
    }

    @NonNull
    public String trimToPlaceholder(@Nullable BigDecimal bigDecimal) {
        return bigDecimal == null ? placeholder : bigDecimal.toPlainString();
    }

    @NonNull
    public String trimToPlaceholder(@Nullable Boolean input, TL tl) {
        if (input == null) {
            return placeholder;
        }
        return input
            ? tl.translate("printer.boolean.true")
            : tl.translate("printer.boolean.false");
    }

    @NonNull
    public String replaceWhitespaceWithNonBreakingSpace(@NonNull String input) {
        return input.replaceAll("\\s", "\u00A0");
    }

    /**
     * Truncates the given input to the given maximum length. If the input is longer than {@code maxLength},
     * the result is cut and ends with an ellipsis ("…"). Returns the placeholder if input is null or blank.
     */
    @NonNull
    public String truncate(@Nullable String input, int maxLength) {
        if (input == null || input.isBlank()) {
            return placeholder;
        }
        String sanitized = stringSanitizer.apply(input.trim());
        if (maxLength <= 0 || sanitized.length() <= maxLength) {
            return sanitized;
        }
        if (maxLength == 1) {
            return "…";
        }
        return sanitized.substring(0, maxLength - 1) + '…';
    }

    @NonNull
    public String formatDate(@NonNull LocalDate date) {
        return date.format(printDateFormat);
    }

    @NonNull
    public String formatDate(@NonNull OffsetDateTime date) {
        return date.format(printDateFormat);
    }

    @NonNull
    public String formatTime(@Nullable LocalDateTime localDateTime) {
        return localDateTime != null ? localDateTime.format(printTimeFormat) : placeholder;
    }

    @NonNull
    String getNameWithAnrede(@NonNull BasePrinter basePrinter, @NonNull Geschlecht geschlecht, @NonNull String name) {
        String anrede = switch (geschlecht) {
            case MAENNLICH ->  basePrinter.translateValue("printer.anrede.MAENNLICH");
            case WEIBLICH ->  basePrinter.translateValue("printer.anrede.WEIBLICH");
            default -> "";
        };

        return String.join(" ", anrede, name).trim();
    }
}
