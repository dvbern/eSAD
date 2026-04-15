package ch.dvbern.oss.hemed.esc.api.shared.valueenhanced;

import java.io.Serial;
import java.util.Arrays;
import java.util.EnumSet;
import java.util.stream.Collectors;

import ch.dvbern.dvbstarter.infrastructure.db.DbConst;
import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import jakarta.validation.constraints.Size;
import lombok.AccessLevel;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;

@Embeddable
@ToString
@EqualsAndHashCode
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class MultiValueEnhanced<E extends Enum<E>> extends AbstractValueEnhanced<E> {

    @Serial
    private static final long serialVersionUID = -1978635914323628399L;


    @Nullable
    @Size(max = DbConst.DB_ENUM_LIST_LENGTH)
    @Column(nullable = true, length = DbConst.DB_ENUM_LIST_LENGTH)
    @Getter(AccessLevel.NONE)
    @Setter(AccessLevel.NONE)
    private String value;

    private MultiValueEnhanced(
        @Nullable EnumSet<E> enumValues,
        @Nullable NichtGeprueftGrund grundNichtGeprueft
    ) {
        this.value = MultiValueEnhanced.toString(enumValues, grundNichtGeprueft);
    }

    @NonNull
    public static <E extends Enum<E>> MultiValueEnhanced<E> of() {
        return new MultiValueEnhanced<>();
    }

    @NonNull
    public static <E extends Enum<E>> MultiValueEnhanced<E> of(@Nullable EnumSet<E> enumValue, @Nullable NichtGeprueftGrund grundNichtGeprueft) {
        if (enumValue == null) {
            return new MultiValueEnhanced<>();
        }
        return new MultiValueEnhanced<>(enumValue, grundNichtGeprueft);
    }

    @Override
    @Nullable
    protected String getDbValue() {
        return value;
    }

    @Nullable
    public EnumSet<E> getEnumValues(@NonNull Class<E> enumClass) {
        if (value == null || value.trim().isEmpty()) {
            return EnumSet.noneOf(enumClass);
        }
        String enumPart = extractPart(value, ENUM_VALUES_DELIMITER);
        if (enumPart == null) {
            return EnumSet.noneOf(enumClass);
        }
        return Arrays.stream(enumPart.split(ENUM_DELIMITER))
            .map(String::trim)
            .map(s -> Enum.valueOf(enumClass, s))
            .collect(Collectors.toCollection(() -> EnumSet.noneOf(enumClass)));
    }

    @NonNull
    private static <E extends Enum<E>> String toString(
        @Nullable EnumSet<E> enumValues,
        @Nullable NichtGeprueftGrund grundNichtGeprueft
    ) {
        return toEnumPartString(enumValues, ENUM_VALUES_DELIMITER)
            + toPartString(grundNichtGeprueft, NICHT_GEPRUEFT_GRUND_DELIMITER);
    }

    @NonNull
    private static <E extends Enum<E>> String toEnumPartString(@Nullable EnumSet<E> enumValues, @NonNull String delimiter) {
        if (enumValues == null) {
            return delimiter + delimiter;
        }
        String part = enumValues.stream()
            .sorted()
            .map(Enum::name)
            .collect(Collectors.joining(ENUM_DELIMITER));
        return delimiter + part + delimiter;
    }
}
