package ch.dvbern.dvbstarter.shared.types.emailaddress;

import ch.dvbern.dvbstarter.infrastructure.errorhandling.AppValidationException;
import org.junit.jupiter.api.DisplayNameGeneration;
import org.junit.jupiter.api.DisplayNameGenerator;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DisplayNameGeneration(DisplayNameGenerator.ReplaceUnderscores.class)
class EmailAddressTest {

    @Test
    void parses_valid_email() {
        var email = EmailAddress.fromString("anna.muster@example.com");

        assertThat(email.getAddress()).isEqualTo("anna.muster@example.com");
        assertThat(email).hasToString("anna.muster@example.com");
    }

    @Test
    void normalizes_email_to_lowercase() {
        var email = EmailAddress.fromString("Anna.Muster@Example.COM");

        assertThat(email.getAddress()).isEqualTo("anna.muster@example.com");
    }

    @Test
    void trims_surrounding_whitespace() {
        var email = EmailAddress.fromString("  anna.muster@example.com  ");

        assertThat(email.getAddress()).isEqualTo("anna.muster@example.com");
    }

    @ParameterizedTest
    @ValueSource(strings = { "", "invalid-email", "user@", "@domain.com", "user@domain..com" })
    void throws_validation_exception_on_invalid_email(String invalid) {
        assertThatThrownBy(() -> EmailAddress.fromString(invalid))
            .isInstanceOf(AppValidationException.class);
    }
}
