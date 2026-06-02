package ch.dvbern.oss.hemed.esc.api.shared.types;

import java.time.LocalDate;

import ch.dvbern.oss.hemed.esc.api.shared.validator.CheckDateRangeValidator;
import jakarta.validation.ConstraintValidatorContext;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import static org.assertj.core.api.Assertions.assertThat;

class CheckDateRangeValidatorTest {

    private ConstraintValidatorContext ctxStub = null;

    @BeforeEach
    void before() {
        ctxStub = Mockito.mock(ConstraintValidatorContext.class);
    }

    private static CheckDateRangeValidator instance() {
        return new CheckDateRangeValidator();
    }

    @Test
    void shouldValidateOnNullInput() {
        // ARRANGE, ACT, ASSERT
        assertThat(instance().isValid(null, ctxStub)).isTrue();
    }

    @Test
    void shouldValidateOnValidInput() {
        // ARRANGE
        DateRange validRange = DateRange.of(
            LocalDate.of(2020, 1, 1),
            LocalDate.of(2020, 12, 31));

        // ACT
        boolean actual = instance().isValid(validRange, ctxStub);

        // ASSERT
        assertThat(actual).isTrue();
    }

    @Test
    void shouldFailOnInvalidInput() {
        // ARRANGE
        DateRange invalidRange = DateRange.of(
            LocalDate.of(2020, 12, 31),
            LocalDate.of(2020, 1, 1));

        // ACT
        boolean actual = instance().isValid(invalidRange, ctxStub);

        // ASSERT
        assertThat(actual).isFalse();
    }
}
