package ch.dvbern.oss.hemed.esc.api.shared.printer;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;

import ch.dvbern.dvbstarter.shared.i18n.translations.TL;
import ch.dvbern.oss.hemed.esc.api.shared.benutzer.Benutzer;
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

    public PrintUtilImpl(@NonNull String placeholder) {
        this.placeholder = placeholder;
        this.printDateFormat = DateTimeFormatter.ofPattern("dd.MM.yyyy");
        this.printTimeFormat = DateTimeFormatter.ofPattern("HH:mm");
    }

    @NonNull
    public String trimToPlaceholder(@Nullable String input) {
        return input == null || input.isBlank() ? placeholder : input.trim();
    }

    @NonNull
    public String trimNameVornameToPlaceholder(@Nullable Benutzer benutzer) {
        return benutzer == null ? placeholder : benutzer.getNameVorname();
    }

    @NonNull
    public String trimVornameNameToPlaceholder(@Nullable Benutzer benutzer) {
        return benutzer == null ? placeholder : benutzer.getVornameName();
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
}
