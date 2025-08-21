package ch.dvbern.oss.hemed.esc.api.features.anlass.groessegewicht;

import java.math.BigDecimal;
import java.math.RoundingMode;

import org.checkerframework.checker.nullness.qual.Nullable;

public final class GroesseGewichtUtil {

    private GroesseGewichtUtil() {
    }

    @Nullable
    public static BigDecimal calculateBmi(@Nullable Integer groesse, @Nullable Integer gewicht) {
        if (groesse == null || gewicht == null) {
            return null;
        }
        double bmi = gewicht.doubleValue() / (groesse.doubleValue() / 100 * groesse.doubleValue() / 100);
        BigDecimal bigDecimal = BigDecimal.valueOf(bmi);
        return bigDecimal.setScale(1, RoundingMode.HALF_EVEN);
    }
}
