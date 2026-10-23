package ch.dvbern.oss.hemed.esc.api.shared.valueenhanced;

import java.io.Serial;
import java.io.Serializable;
import java.util.Arrays;
import java.util.EnumSet;
import java.util.stream.Collectors;

import org.apache.commons.lang.StringUtils;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;

/**
 * Base class and parser for composite enum fields stored in a single database column.
 *
 * <h3>Wire Format</h3>
 * Encodes an enum value (or set of enum values) and an optional {@link NichtGeprueftGrund}
 * into a single delimited string:
 * <pre>
 *   "$1$" + [enum-values] + "$1$" + "$2$" + [nicht-geprueft-grund] + "$2$"
 * </pre>
 *
 * <h3>Delimiters</h3>
 * <ul>
 *   <li>{@link #ENUM_VALUES_DELIMITER} ({@code "$1$"}): Encloses the primary enum value or
 *       a comma-separated list of enum values (sorted lexicographically).</li>
 *   <li>{@link #NICHT_GEPRUEFT_GRUND_DELIMITER} ({@code "$2$"}): Encloses the optional
 *       reason why an examination was not conducted.</li>
 *   <li>{@link #ENUM_DELIMITER} ({@code ","}): Separates multiple enum values in multi-value fields.</li>
 * </ul>
 *
 * <h3>Examples</h3>
 * <ul>
 *   <li>{@code "$1$BESTANDEN$1$$2$$2$"} &rarr; Value: {@code BESTANDEN}, Grund: {@code null}</li>
 *   <li>{@code "$1$$1$$2$UNTERSUCHUNG_NICHT_INDIZIERT$2$"} &rarr; Value: {@code null}, Grund: {@code UNTERSUCHUNG_NICHT_INDIZIERT}</li>
 *   <li>{@code "$1$VAL_A,VAL_B$1$$2$$2$"} &rarr; Values: {@code [VAL_A, VAL_B]}, Grund: {@code null}</li>
 *   <li>{@code "$1$$1$$2$$2$"} or {@code null} &rarr; Empty/unassigned field</li>
 * </ul>
 *
 * <h3>Usage</h3>
 * Used both by JPA embeddables ({@link SingleValueEnhanced}, {@link MultiValueEnhanced})
 * during entity mapping, and directly as static extraction utilities ({@link #extractEnumValue},
 * {@link #extractEnumValues}, {@link #extractNichtGeprueftGrund}) in cursor-based query projections.
 */
public abstract class AbstractValueEnhanced<E extends Enum<E>> implements Serializable {

    @Serial
    private static final long serialVersionUID = -7419270054709439970L;

    public static final String ENUM_DELIMITER = ",";
    public static final String ENUM_VALUES_DELIMITER = "$1$";
    public static final String NICHT_GEPRUEFT_GRUND_DELIMITER = "$2$";

    @Nullable
    protected abstract String getDbValue();

    @Nullable
    public NichtGeprueftGrund getNichtGeprueftGrund() {
        return extractNichtGeprueftGrund(getDbValue());
    }

    @Nullable
    public static <E extends Enum<E>> E extractEnumValue(
        @Nullable String dbValue,
        @NonNull Class<E> enumClass
    ) {
        if (dbValue == null || dbValue.trim().isEmpty()) {
            return null;
        }
        String enumPart = extractPart(dbValue, ENUM_VALUES_DELIMITER);
        if (enumPart == null) {
            return null;
        }
        return Enum.valueOf(enumClass, enumPart);
    }

    @NonNull
    public static <E extends Enum<E>> EnumSet<E> extractEnumValues(
        @Nullable String dbValue,
        @NonNull Class<E> enumClass
    ) {
        if (dbValue == null || dbValue.trim().isEmpty()) {
            return EnumSet.noneOf(enumClass);
        }
        String enumPart = extractPart(dbValue, ENUM_VALUES_DELIMITER);
        if (enumPart == null) {
            return EnumSet.noneOf(enumClass);
        }
        return Arrays.stream(enumPart.split(ENUM_DELIMITER))
            .map(String::trim)
            .map(s -> Enum.valueOf(enumClass, s))
            .collect(Collectors.toCollection(() -> EnumSet.noneOf(enumClass)));
    }

    @Nullable
    public static NichtGeprueftGrund extractNichtGeprueftGrund(@Nullable String dbValue) {
        if (dbValue == null || dbValue.trim().isEmpty()) {
            return null;
        }
        String grundPart = extractPart(dbValue, NICHT_GEPRUEFT_GRUND_DELIMITER);
        if (grundPart == null) {
            return null;
        }
        return NichtGeprueftGrund.valueOf(grundPart);
    }

    @Nullable
    public static String extractPart(@NonNull String valueNonNull, @NonNull String delimiter) {
        int start = valueNonNull.indexOf(delimiter) + delimiter.length();
        int end = valueNonNull.indexOf(delimiter, start);
        if (start < delimiter.length() || end < 0 || end < start) {
            return null;
        }
        String part = valueNonNull.substring(start, end);
        if (StringUtils.isEmpty(part)) {
            return null;
        }
        return part;
    }

    @NonNull
    protected static String toPartString(@Nullable Object part, @NonNull String delimiter) {
        if (part == null) {
            return delimiter + delimiter;
        }
        return delimiter + part + delimiter;
    }
}
