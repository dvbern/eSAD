package ch.dvbern.dvbstarter.shared.math;

import java.math.BigDecimal;

import org.junit.jupiter.api.Test;

import static ch.dvbern.dvbstarter.shared.math.FrankenFuenfer.FRANKEN_FUENFER;
import static ch.dvbern.oss.hemed.esc.api.testutil.assertj.AttributeConverterAssert.assertThatAttributeConverter;

class FrankenFuenferConverterTest {
    @Test
    void converts_back_and_forth() {
        assertThatAttributeConverter(new FrankenFuenferConverter())
            .convertsBackAndForth(FRANKEN_FUENFER.of("1.05"), new BigDecimal("1.05"))
            .convertsNullToNull();
    }

    @Test
    void throws_for_invalid_db_value_with_too_much_precision() {

        assertThatAttributeConverter(new FrankenFuenferConverter())
            .throwsForInvalidDbValue(IllegalArgumentException.class, new BigDecimal("1.06"))
            .hasMessageContaining("1.06");
    }

}
