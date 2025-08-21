package ch.dvbern.oss.hemed.esc.api.features.anlass.groessegewicht;

import java.math.BigDecimal;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class GroesseGewichtUtilTest {

    @Test
    void bmi() {
        assertEquals(null, GroesseGewichtUtil.calculateBmi(null, null));
        assertEquals(null, GroesseGewichtUtil.calculateBmi(null, 68));
        assertEquals(null, GroesseGewichtUtil.calculateBmi(175, null));
        assertEquals(BigDecimal.valueOf(22.2), GroesseGewichtUtil.calculateBmi(175, 68)); //22.20408163265306
        assertEquals(BigDecimal.valueOf(22.5), GroesseGewichtUtil.calculateBmi(175, 69)); //22.53061224489796
        assertEquals(BigDecimal.valueOf(22.5), GroesseGewichtUtil.calculateBmi(174, 68)); //22.460034350640772
        assertEquals(BigDecimal.valueOf(22.0), GroesseGewichtUtil.calculateBmi(176, 68)); //21.952479338842977
    }
}
