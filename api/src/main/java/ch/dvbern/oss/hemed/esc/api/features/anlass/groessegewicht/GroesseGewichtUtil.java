package ch.dvbern.oss.hemed.esc.api.features.anlass.groessegewicht;

import java.math.BigDecimal;
import java.math.RoundingMode;

import ch.dvbern.dvbstarter.infrastructure.db.DbConst;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;

public final class GroesseGewichtUtil {

    private GroesseGewichtUtil() {
    }

    @Nullable
    public static BigDecimal calculateBmi(@Nullable Integer groesse, @Nullable BigDecimal gewicht) {
        if (groesse == null || gewicht == null) {
            return null;
        }
        double bmi = gewicht.doubleValue() / (groesse.doubleValue() / 100 * groesse.doubleValue() / 100);
        BigDecimal bigDecimal = BigDecimal.valueOf(bmi);
        return roundScaleOne(bigDecimal, RoundingMode.HALF_UP);
    }

    @Nullable
    public static BigDecimal roundScaleOne(@Nullable BigDecimal value, @NonNull RoundingMode roundingMode) {
        if (value != null) {
            return value.setScale(DbConst.DB_ZAHL_SCALE_1, roundingMode);
        }
        return null;
    }
}
