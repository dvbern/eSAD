package ch.dvbern.oss.hemed.esc.api.shared.valueenhanced;

import java.io.Serial;

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
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class SingleValueEnhanced<E extends Enum<E>> extends AbstractValueEnhanced<E> {

    @Serial
    private static final long serialVersionUID = -1978635914323628399L;

    @Nullable
    @Size(max = DbConst.DB_DEFAULT_MAX_LENGTH)
    @Column(nullable = true, length = DbConst.DB_DEFAULT_MAX_LENGTH)
    @Getter(AccessLevel.NONE)
    @Setter(AccessLevel.NONE)
    private String value;

    private SingleValueEnhanced(
        @Nullable E enumValue,
        @Nullable NichtGeprueftGrund grundNichtGeprueft
    ) {
        this.value = SingleValueEnhanced.toString(enumValue, grundNichtGeprueft);
    }

    @NonNull
    public static <E extends Enum<E>> SingleValueEnhanced<E> of() {
        return new SingleValueEnhanced<>();
    }

    @NonNull
    public static <E extends Enum<E>> SingleValueEnhanced<E> of(@Nullable E enumValue, @Nullable NichtGeprueftGrund grundNichtGeprueft) {
        if (enumValue == null) {
            return new SingleValueEnhanced<>();
        }
        return new SingleValueEnhanced<>(enumValue, grundNichtGeprueft);
    }

    @Override
    @Nullable
    protected String getDbValue() {
        return value;
    }

    @Nullable
    public E getEnumValue(@NonNull Class<E> enumClass) {
        if (value == null || value.trim().isEmpty()) {
            return null;
        }
        String enumPart = extractPart(value, ENUM_VALUES_DELIMITER);
        if (enumPart == null) {
            return null;
        }
        return Enum.valueOf(enumClass, enumPart);
    }

    @Nullable
    public String toEnumValueName(@NonNull Class<E> enumClass) {
        E enumValue = getEnumValue(enumClass);
        if (enumValue != null) {
            return enumValue.name();
        }
        return null;
    }

    @NonNull
    private static <E extends Enum<E>> String toString(
        @Nullable E enumValue,
        @Nullable NichtGeprueftGrund grundNichtGeprueft
    ) {
        return toPartString(enumValue, ENUM_VALUES_DELIMITER)
            + toPartString(grundNichtGeprueft, NICHT_GEPRUEFT_GRUND_DELIMITER);
    }
}
