package ch.dvbern.dvbstarter.shared.math;

import java.math.BigDecimal;
import java.util.Collection;
import java.util.function.Function;
import java.util.stream.Stream;

import com.github.honoluluhenk.fluentbigdecimals.AbstractFluentBigDecimal;
import com.github.honoluluhenk.fluentbigdecimals.Configuration;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import org.checkerframework.checker.nullness.qual.NonNull;

public abstract class MathUtil<T extends MathUtil<T>> extends AbstractFluentBigDecimal<T> {

    private static final long serialVersionUID = 4468946248426533047L;

    protected MathUtil(@NonNull BigDecimal value, @NonNull Configuration<T> configuration) {
        super(value, configuration);
    }

    public static boolean isEven(int n) {
        return (n % 2) == 0;
    }

    public static <T extends MathUtil<T>, U> T sum(
        Collection<U> list,
        Function<U, T> valueExtractor,
        Configuration<T> configuration
    ) {
        return sum(list.stream(), valueExtractor, configuration);
    }

    public static <T extends MathUtil<T>, U> T sum(
        Stream<U> stream,
        Function<U, T> valueExtractor,
        Configuration<T> configuration
    ) {
        return stream
            .map(valueExtractor)
            .reduce(configuration.of(0), T::add);
    }

    public boolean isPositive() {
        return compareTo(BigDecimal.ZERO) > 0;
    }

    public boolean isNegative() {
        return compareTo(BigDecimal.ZERO) < 0;
    }

    public T negate() {
        return multiply(-1);
    }

    @Override
    @NonNull
    public String toString() {
        return new ToStringBuilder(this, ToStringStyle.SHORT_PREFIX_STYLE)
            .append(getValue().toPlainString())
            .build();
    }
}
