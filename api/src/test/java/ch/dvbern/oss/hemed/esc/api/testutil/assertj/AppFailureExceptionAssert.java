package ch.dvbern.oss.hemed.esc.api.testutil.assertj;

import java.util.Objects;

import ch.dvbern.dvbstarter.infrastructure.errorhandling.AppFailureException;
import ch.dvbern.oss.commons.i18nl10n.I18nMessage;
import org.assertj.core.api.AbstractAssert;

@SuppressWarnings({ "UnusedReturnValue", "unused" })
public class AppFailureExceptionAssert extends AbstractAssert<AppFailureExceptionAssert, AppFailureException> {

    private AppFailureExceptionAssert(AppFailureException actual) {
        super(actual, AppFailureExceptionAssert.class);
    }

    public static AppFailureExceptionAssert assertThatAppFailureException(AppFailureException actual) {
        return new AppFailureExceptionAssert(actual);
    }

    public AppFailureExceptionAssert hasId() {
        isNotNull();

        if (actual.getId() == null) {
            failWithMessage("Expected AppFailureException to have an id but id was null/blank");
        }

        return this;
    }

    public AppFailureExceptionAssert hasRawMessage(String expectedMessage) {
        isNotNull();

        if (!Objects.equals(actual.getMessage(), expectedMessage)) {
            failWithActualExpectedAndMessage(
                actual.getMessage(),
                expectedMessage,
                "Expected AppFailureException to have message >%s< but was >%s<",
                expectedMessage, actual.getMessage());
        }

        return this;
    }

    public AppFailureExceptionAssert hasRawMessageContaining(String expectedMessage) {
        isNotNull();

        if (!actual.getMessage().contains(expectedMessage)) {
            failWithActualExpectedAndMessage(
                actual.getMessage(),
                expectedMessage,
                "Expected AppFailureException to have message containing >%s< but was >%s<",
                expectedMessage, actual.getMessage());
        }

        return this;
    }

    public AppFailureExceptionAssert hasI18nMessage(I18nMessage expectedMessage) {
        isNotNull();

        if (!Objects.equals(actual.getI18nMessage(), expectedMessage)) {
            failWithActualExpectedAndMessage(
                actual.getI18nMessage(),
                expectedMessage,
                "Expected AppFailureException to have I18nMessage >%s< but was >%s<",
                expectedMessage, actual.getI18nMessage());
        }

        return this;
    }

    /**
     * Tests all requirements for a feature complete exception.
     */
    @SuppressWarnings("ResultOfMethodCallIgnored")
    public AppFailureExceptionAssert isFeatureCompleteFor(I18nMessage expectedMessage) {
        isNotNull();

        hasId();

        hasI18nMessage(expectedMessage);

        hasRawMessageContaining(expectedMessage.key().value());
        expectedMessage.args().forEach((key, value) -> {
            hasRawMessageContaining(String.valueOf(key));
            hasRawMessageContaining(String.valueOf(value));
        });
        hasRawMessageContaining(actual.getId().toString());

        return this;
    }
}
