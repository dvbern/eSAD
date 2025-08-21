package ch.dvbern.dvbstarter.shared.types.emailaddress;

import org.junit.jupiter.api.DisplayNameGeneration;
import org.junit.jupiter.api.DisplayNameGenerator;
import org.junit.jupiter.api.Test;

import static ch.dvbern.oss.hemed.esc.api.testutil.assertj.AttributeConverterAssert.assertThatAttributeConverter;

@DisplayNameGeneration(DisplayNameGenerator.ReplaceUnderscores.class)
class EmailAddressConverterTest {

    @Test
    void converts_back_and_forth() {
        assertThatAttributeConverter(new EmailAddressConverter())
            .convertsBackAndForth(new EmailAddress("somebody@localhost"), "somebody@localhost")
            .convertsNullToNull();
    }
}
