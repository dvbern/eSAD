package ch.dvbern.dvbstarter.shared.math;

import java.math.BigDecimal;

import org.junit.jupiter.api.Test;

import static ch.dvbern.dvbstarter.shared.math.Zahl.ZAHL;
import static ch.dvbern.oss.hemed.esc.api.testutil.assertj.AttributeConverterAssert.assertThatAttributeConverter;

class ZahlConverterTest {
    @Test
    void converts_back_and_forth() {
        assertThatAttributeConverter(new ZahlConverter())
            .convertsBackAndForth(ZAHL.of("1.00"), new BigDecimal("1.00"))
            .convertsNullToNull();
    }

    @Test
    void throws_for_invalid_db_value_with_too_much_precision() {

        assertThatAttributeConverter(new ZahlConverter())
            .throwsForInvalidDbValue(IllegalArgumentException.class, new BigDecimal("1.001"))
            .hasMessageContaining("1.001");
    }
}
