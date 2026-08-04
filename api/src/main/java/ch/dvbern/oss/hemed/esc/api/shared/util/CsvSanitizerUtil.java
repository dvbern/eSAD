package ch.dvbern.oss.hemed.esc.api.shared.util;

import java.util.regex.Pattern;

import lombok.experimental.UtilityClass;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;

/**
 * Prevents CSV formula injection (CWE-1236) by prefixing cell values that start with
 * characters spreadsheet applications interpret as formula triggers (= + - @ TAB CR)
 * with a single quote, which forces text interpretation in excel.
 * + is allowed if followed only by numbers to allow for phone numbers.
 * - is allowed if followed only by numbers (or nothing) to allow for negative numbers
 * and lone "-" placeholder values.
 */
@UtilityClass
public final class CsvSanitizerUtil {

	private static final char FORMULA_NEUTRALIZATION_PREFIX = '\'';

	// A leading '+' followed only by digits and spaces (e.g. an international phone number
	// like "+41 79 123 45 67") cannot form an executable formula: without letters or
	// operators there is no DDE, HYPERLINK or arithmetic payload. Exempting this shape
	// keeps phone numbers free of the visible quote prefix.
	private static final Pattern INERT_PLUS_NUMERIC = Pattern.compile("\\+[\\d ]+");

	// A leading '-' followed only by digits and spaces, or nothing at all (e.g. a plain
	// negative number like "-42" or the lone "-" placeholder used for empty values),
	// cannot form an executable formula either: without letters or operators there is no
	// DDE, HYPERLINK or arithmetic payload. Exempting this shape keeps negative numbers and
	// placeholder dashes free of the visible quote prefix.
	private static final Pattern INERT_MINUS_NUMERIC = Pattern.compile("-[\\d ]*");


	@NonNull
	public static String sanitize(@Nullable String value) {
		if (value == null || value.isEmpty()) {
			return "";
		}
		if (startsWithFormulaTrigger(value) && !isInertNumeric(value)) {
			return FORMULA_NEUTRALIZATION_PREFIX + value;
		}
		return value;
	}

	private static boolean isInertNumeric(@NonNull String value) {
		return INERT_PLUS_NUMERIC.matcher(value).matches() || INERT_MINUS_NUMERIC.matcher(value).matches();
	}

	private static boolean startsWithFormulaTrigger(@NonNull String value) {
		char first = value.charAt(0);
		return first == '=' || first == '+' || first == '-' || first == '@'
			|| first == '\t' || first == '\r';
	}
}
