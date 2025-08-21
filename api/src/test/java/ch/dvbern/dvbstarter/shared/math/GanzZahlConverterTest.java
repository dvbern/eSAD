package ch.dvbern.dvbstarter.shared.math;

import java.math.BigDecimal;

import org.junit.jupiter.api.Test;

import static ch.dvbern.dvbstarter.shared.math.GanzZahl.GANZ_ZAHL;
import static ch.dvbern.oss.hemed.esc.api.testutil.assertj.AttributeConverterAssert.assertThatAttributeConverter;

class GanzZahlConverterTest {
    @Test
    void converts_back_and_forth() {
        assertThatAttributeConverter(new GanzZahlConverter())
            .convertsBackAndForth(GANZ_ZAHL.of("1"), new BigDecimal("1"))
            .convertsNullToNull();
    }

    @Test
    void throws_for_invalid_db_value_with_too_much_precision() {

        assertThatAttributeConverter(new GanzZahlConverter())
            .throwsForInvalidDbValue(IllegalArgumentException.class, new BigDecimal("1.1"))
            .hasMessageContaining("1.1");
    }
}
