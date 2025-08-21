package ch.dvbern.dvbstarter.shared.math;

import java.math.BigDecimal;

import org.junit.jupiter.api.Test;

import static ch.dvbern.dvbstarter.shared.math.DbNumber.DB_NUMBER;
import static ch.dvbern.oss.hemed.esc.api.testutil.assertj.AttributeConverterAssert.assertThatAttributeConverter;

class DbNumberConverterTest {
    @Test
    void converts_back_and_forth() {
        assertThatAttributeConverter(new DbNumberConverter())
            .convertsBackAndForth(DB_NUMBER.of("123.45"), new BigDecimal("123.45"))
            .convertsNullToNull();
    }

    @Test
    void throws_for_invalid_db_value_with_too_much_precision() {
        assertThatAttributeConverter(new DbNumberConverter())
            .throwsForInvalidDbValue(IllegalArgumentException.class, new BigDecimal("0.12345678901"))
            .hasMessageContaining("0.12345678901");
    }

    @Test
    void throws_for_invalid_db_value_with_too_much_scale() {
        assertThatAttributeConverter(new DbNumberConverter())
            .throwsForInvalidDbValue(ArithmeticException.class, new BigDecimal("1234567890123456789012345678901"))
            .hasMessageContaining("1234567890123456789012345678901")
        ;
    }
}
