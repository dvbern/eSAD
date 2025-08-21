package ch.dvbern.dvbstarter.infrastructure.errorhandling;

import ch.dvbern.oss.commons.i18nl10n.I18nMessage;
import org.junit.jupiter.api.Test;

import static ch.dvbern.oss.hemed.esc.api.testutil.assertj.AppValidationExceptionAssert.assertThatAppValidationException;

@SuppressWarnings("ResultOfMethodCallIgnored")
class AppValidationExceptionTest {

    @Test
    void invalidEmail() {
        var actual = new AppValidationException(AppValidationMessage.invalidEmail("foo@"));

        assertThatAppValidationException(actual)
            .isFeatureCompleteFor(
                "InvalidEmail",
                I18nMessage.of(
                    "AppValidation.INVALID_EMAIL",
                    "email",
                    "foo@"
                ));
    }
}
