package ch.dvbern.dvbstarter.infrastructure.errorhandling;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class FailureTypeTest {

    @Test
    void isUserInfo() {
        assertThat(FailureType.VALIDATION.isUserInfo())
            .isTrue();
        assertThat(FailureType.FAILURE.isUserInfo())
            .isFalse();
    }

    @Test
    void isFailure() {
        assertThat(FailureType.VALIDATION.isFailure())
            .isFalse();
        assertThat(FailureType.FAILURE.isFailure())
            .isTrue();
    }
}
