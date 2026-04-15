package ch.dvbern.oss.hemed.esc.api.shared.valueenhanced;

import java.io.Serial;
import java.io.Serializable;

import org.apache.commons.lang.StringUtils;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;

public abstract class AbstractValueEnhanced <E extends Enum<E>> implements Serializable {

    @Serial
    private static final long serialVersionUID = -7419270054709439970L;

    public static final String ENUM_DELIMITER = ",";
    public static final String ENUM_VALUES_DELIMITER = "$1$";
    public static final String NICHT_GEPRUEFT_GRUND_DELIMITER = "$2$";


    @Nullable
    protected abstract String getDbValue();

    @Nullable
    public NichtGeprueftGrund getNichtGeprueftGrund() {
        if (getDbValue() == null || getDbValue().trim().isEmpty()) {
            return null;
        }
        String grundPart = extractPart(getDbValue(), NICHT_GEPRUEFT_GRUND_DELIMITER);
        if (grundPart == null) {
            return null;
        }
        return NichtGeprueftGrund.valueOf(grundPart);
    }

    @Nullable
    protected String extractPart(@NonNull String valueNonNull, @NonNull String delimiter) {
        int start = valueNonNull.indexOf(delimiter) + delimiter.length();
        int end = valueNonNull.indexOf(delimiter, start);
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
