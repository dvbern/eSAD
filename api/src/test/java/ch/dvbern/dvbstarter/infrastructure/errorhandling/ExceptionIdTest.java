package ch.dvbern.dvbstarter.infrastructure.errorhandling;

import java.util.UUID;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;

class ExceptionIdTest {
    @Test
    void fromUUID_generates_a_base64_encoded_string_without_padding() {
        var actual = ExceptionId.fromUUID(UUID.fromString("0111d7c0-9c85-4741-9121-babef95ad1c0"));

        // expected: the base64 representation of the UUID without padding
        assertThat(actual)
            .hasToString("ARHXwJyFR0GRIbq++VrRwA");
    }

    @Test
    void random_is_random() {
        var actual = ExceptionId.random();

        assertThat(actual.toString())
            .isNotEqualTo("ARHXwJyFR0GRIbq++VrRwA");
    }

    @Test
    void fromString_just_passes_the_string_along() {
        var actual = ExceptionId.fromString("whatever");

        assertThat(actual)
            .hasToString("whatever");
    }

    @Test
    @SuppressWarnings("ResultOfMethodCallIgnored")
    void fromString_throws_on_null_or_blank_string() {
        //noinspection DataFlowIssue
        assertThatCode(() -> ExceptionId.fromString(null))
            .isInstanceOf(NullPointerException.class)
            .hasMessage("id must not be null or empty");

        assertThatCode(() -> ExceptionId.fromString("    "))
            .isInstanceOf(NullPointerException.class)
            .hasMessage("id must not be null or empty");
    }
}
