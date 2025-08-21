package ch.dvbern.dvbstarter.infrastructure.errorhandling;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThatCode;

class AppExceptionUtilTest {

    @Test
    void accepts_if_correct_number_of_arguments() {
        assertThatCode(() -> AppExceptionUtil.validateArgs(0))
            .doesNotThrowAnyException();
        assertThatCode(() -> AppExceptionUtil.validateArgs(1, "a"))
            .doesNotThrowAnyException();
        assertThatCode(() -> AppExceptionUtil.validateArgs(2, "a", "b"))
            .doesNotThrowAnyException();
    }

    @Test
    void throws_if_number_of_arguments_does_not_match() {
        assertThatCode(() -> AppExceptionUtil.validateArgs(0, "a"))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessage("Need 0 arguments but got: 1: a");

        assertThatCode(() -> AppExceptionUtil.validateArgs(1))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessage("Need 1 arguments but got: 0: ");

        assertThatCode(() -> AppExceptionUtil.validateArgs(1, "a", "b"))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessage("Need 1 arguments but got: 2: a,b");
    }
}
