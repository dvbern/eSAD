package ch.dvbern.dvbstarter.shared.types.phonenumber;

import org.junit.jupiter.api.DisplayNameGeneration;
import org.junit.jupiter.api.DisplayNameGenerator;
import org.junit.jupiter.api.Test;

import static ch.dvbern.oss.hemed.esc.api.testutil.assertj.AttributeConverterAssert.assertThatAttributeConverter;

@DisplayNameGeneration(DisplayNameGenerator.ReplaceUnderscores.class)
class NormalizedPhoneNumberConverterTest {

    @Test
    void converts_to_db_and_back() {
        assertThatAttributeConverter(new NormalizedPhoneNumberConverter())
            .convertsBackAndForth(new NormalizedPhoneNumber("+41 076 309 30 58"), "+41763093058")
            .convertsBackAndForth(new NormalizedPhoneNumber("076 309 30 58"), "+41763093058")
            .convertsBackAndForth(new NormalizedPhoneNumber("76   30930  58"), "+41763093058")
            .convertsNullToNull();
    }
}
