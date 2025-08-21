package ch.dvbern.dvbstarter.shared.math;

import java.math.BigDecimal;

import org.junit.jupiter.api.Test;

import static ch.dvbern.dvbstarter.shared.math.FrankenEiner.FRANKEN_EINER;
import static ch.dvbern.oss.hemed.esc.api.testutil.assertj.AttributeConverterAssert.assertThatAttributeConverter;

class FrankenEinerConverterTest {

    @Test
    void converts_back_and_forth() {
        assertThatAttributeConverter(new FrankenEinerConverter())
            .convertsBackAndForth(FRANKEN_EINER.of("1"), new BigDecimal("1.00"))
            .convertsNullToNull();
    }

    @Test
    void throws_for_invalid_db_value_with_too_much_precision() {

        assertThatAttributeConverter(new FrankenEinerConverter())
            .throwsForInvalidDbValue(IllegalArgumentException.class, new BigDecimal("1.001"))
            .hasMessageContaining("1.001");
    }

}
