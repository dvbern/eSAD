package ch.dvbern.dvbstarter.shared.math;

import java.math.BigDecimal;

import org.junit.jupiter.api.Test;

import static ch.dvbern.dvbstarter.shared.math.Calculation.CALCULATION;
import static ch.dvbern.oss.hemed.esc.api.testutil.assertj.AttributeConverterAssert.assertThatAttributeConverter;

class CalculationConverterTest {

    @Test
    void converts_back_and_forth() {
        assertThatAttributeConverter(new CalculationConverter())
            .convertsBackAndForth(CALCULATION.of("123.45"), new BigDecimal("123.45"))
            .convertsNullToNull();
    }

    @Test
    void throws_for_invalid_db_value_with_too_much_precision() {
        assertThatAttributeConverter(new CalculationConverter())
            .throwsForInvalidDbValue(IllegalArgumentException.class, new BigDecimal("0.1234567890123456"))
            .hasMessageContaining("0.1234567890123456");
    }
}
