package ch.dvbern.dvbstarter.shared.math;

import java.io.Serial;
import java.math.BigDecimal;
import java.math.MathContext;

import com.github.honoluluhenk.fluentbigdecimals.Configuration;
import com.github.honoluluhenk.fluentbigdecimals.ConfigurationFactory;
import com.github.honoluluhenk.fluentbigdecimals.scaler.NopScaler;

import static java.math.RoundingMode.HALF_UP;

/**
 * Maximale Präzision zum Rechnen, keine Skalierung.
 */
public class Calculation extends MathUtil<Calculation> {
    @SuppressWarnings("java:S1700" /* allows for pretty static import */)
    public static final Configuration<Calculation> CALCULATION =
        ConfigurationFactory
            .create(
                new MathContext(15, HALF_UP),
                new NopScaler()
            )
            .withFactory(Calculation::new);
    public static final Calculation ZERO = Calculation.of(BigDecimal.ZERO);
    public static final Calculation ONE = Calculation.of(BigDecimal.ONE);
    public static final Calculation TEN = Calculation.of(BigDecimal.TEN);
    @Serial
    private static final long serialVersionUID = 1136059771822861030L;

    private Calculation(BigDecimal value, Configuration<Calculation> configuration) {
        super(value, configuration);
    }

    public static Calculation of(BigDecimal value) {
        return CALCULATION.of(value);
    }

    public static Calculation of(String value) {
        return CALCULATION.of(value);
    }

    public static Calculation of(long value) {
        return CALCULATION.of(value);
    }

}
