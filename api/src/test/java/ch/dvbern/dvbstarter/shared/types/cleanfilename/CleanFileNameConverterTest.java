package ch.dvbern.dvbstarter.shared.types.cleanfilename;

import org.junit.jupiter.api.Test;

import static ch.dvbern.oss.hemed.esc.api.testutil.assertj.AttributeConverterAssert.assertThatAttributeConverter;

class CleanFileNameConverterTest {
    @Test
    void converts_back_and_forth() {
        assertThatAttributeConverter(new CleanFileNameConverter())
            .convertsBackAndForth(new CleanFileName("abc"), "abc")
            .convertsNullToNull();
    }
}
