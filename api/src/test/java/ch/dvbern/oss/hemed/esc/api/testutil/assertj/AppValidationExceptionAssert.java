package ch.dvbern.oss.hemed.esc.api.testutil.assertj;

import java.util.Objects;

import ch.dvbern.dvbstarter.infrastructure.errorhandling.AppValidationException;
import ch.dvbern.oss.commons.i18nl10n.I18nMessage;
import org.apache.commons.lang3.StringUtils;
import org.assertj.core.api.AbstractAssert;

@SuppressWarnings({ "UnusedReturnValue", "unused" })
public class AppValidationExceptionAssert extends AbstractAssert<AppValidationExceptionAssert, AppValidationException> {

    private AppValidationExceptionAssert(AppValidationException actual) {
        super(actual, AppValidationExceptionAssert.class);
    }

    public static AppValidationExceptionAssert assertThatAppValidationException(AppValidationException actual) {
        return new AppValidationExceptionAssert(actual);
    }

    public AppValidationExceptionAssert hasId() {
        isNotNull();

        if (actual.getId() == null) {
            failWithMessage("Expected AppValidationException to have an id but id was null/blank");
        }

        return this;
    }

    public AppValidationExceptionAssert hasClientKey() {
        isNotNull();

        if (StringUtils.isBlank(actual.getClientKey())) {
            failWithMessage(
                "Expected AppValidationException to have a non-blank clientKey but clientKey was: %s",
                actual.getClientKey()
            );
        }

        return this;
    }

    public AppValidationExceptionAssert hasClientKey(String expectedClientKey) {
        isNotNull();

        if (!Objects.equals(actual.getClientKey(), expectedClientKey)) {
            failWithActualExpectedAndMessage(
                actual.getClientKey(),
                expectedClientKey,
                "Expected AppValidationException to have clientKey >%s< but was >%s<",
                expectedClientKey,
                actual.getClientKey()
            );
        }

        return this;
    }

    public AppValidationExceptionAssert hasRawMessage(String expectedMessage) {
        isNotNull();

        if (!Objects.equals(actual.getMessage(), expectedMessage)) {
            failWithActualExpectedAndMessage(
                actual.getMessage(),
                expectedMessage,
                "Expected AppValidationException to have message >%s< but was >%s<",
                expectedMessage, actual.getMessage());
        }

        return this;
    }

    public AppValidationExceptionAssert hasRawMessageContaining(String expectedMessage) {
        isNotNull();

        if (!actual.getMessage().contains(expectedMessage)) {
            failWithActualExpectedAndMessage(
                actual.getMessage(),
                expectedMessage,
                "Expected AppValidationException to have message containing >%s< but was >%s<",
                expectedMessage, actual.getMessage());
        }

        return this;
    }

    public AppValidationExceptionAssert hasI18nMessage(I18nMessage expectedMessage) {
        isNotNull();

        if (!Objects.equals(actual.getI18nMessage(), expectedMessage)) {
            failWithActualExpectedAndMessage(
                actual.getI18nMessage(),
                expectedMessage,
                "Expected AppValidationException to have I18nMessage >%s< but was >%s<",
                expectedMessage, actual.getI18nMessage());
        }

        return this;
    }

    /**
     * Tests all requirements for a feature complete exception.
     */
    @SuppressWarnings("ResultOfMethodCallIgnored")
    public AppValidationExceptionAssert isFeatureCompleteFor(
        String clientKey,
        I18nMessage expectedMessage
    ) {
        isNotNull();

        hasId();
        hasClientKey(clientKey);

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
