package ch.dvbern.oss.hemed.esc.api.features.anlass.groessegewicht;

import java.math.BigDecimal;
import java.math.RoundingMode;

import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import static org.junit.jupiter.api.Assertions.assertEquals;

class GroesseGewichtUtilTest {

    @ParameterizedTest
    @CsvSource(value = {
        "null,null,HALF_EVEN",
        "1,1.0,HALF_EVEN",
        "1.2,1.2,HALF_EVEN",
        "1.25,1.2,HALF_EVEN",
        "1.75,1.8,HALF_EVEN",
        "1.245,1.2,HALF_EVEN",
        "1.255,1.3,HALF_EVEN",

        "null,null,HALF_UP",
        "1,1.0,HALF_UP",
        "1.2,1.2,HALF_UP",
        "1.25,1.3,HALF_UP",
        "1.75,1.8,HALF_UP",
        "1.245,1.2,HALF_UP",
        "1.255,1.3,HALF_UP",

        "null,null,HALF_DOWN",
        "1,1.0,HALF_DOWN",
        "1.2,1.2,HALF_DOWN",
        "1.25,1.2,HALF_DOWN",
        "1.75,1.7,HALF_DOWN",
        "1.245,1.2,HALF_DOWN",
        "1.255,1.3,HALF_DOWN",
    }, nullValues = "null")
    void testRoundScaleOne(
        @Nullable BigDecimal value,
        @Nullable BigDecimal expected,
        @NonNull RoundingMode roundingMode) {
        BigDecimal result = GroesseGewichtUtil.roundScaleOne(value, roundingMode);
        assertEquals(expected, result);

    }

    @Test
    void testBmi() {
        assertEquals(null, GroesseGewichtUtil.calculateBmi(null, null));
        assertEquals(null, GroesseGewichtUtil.calculateBmi(null, BigDecimal.valueOf(68)));
        assertEquals(null, GroesseGewichtUtil.calculateBmi(175, null));
        assertEquals(
            BigDecimal.valueOf(22.2),
            GroesseGewichtUtil.calculateBmi(175, BigDecimal.valueOf(68))); //22.20408163265306
        assertEquals(
            BigDecimal.valueOf(22.5),
            GroesseGewichtUtil.calculateBmi(175, BigDecimal.valueOf(69))); //22.53061224489796
        assertEquals(
            BigDecimal.valueOf(22.5),
            GroesseGewichtUtil.calculateBmi(174, BigDecimal.valueOf(68))); //22.460034350640772
        assertEquals(
            BigDecimal.valueOf(22.0),
            GroesseGewichtUtil.calculateBmi(176, BigDecimal.valueOf(68))); //21.952479338842977
    }
}
