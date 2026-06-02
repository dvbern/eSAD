package ch.dvbern.oss.hemed.esc.api.shared.types;

import java.time.LocalDateTime;

import ch.dvbern.oss.hemed.esc.api.shared.validator.CheckDateTimeRangeValidator;
import jakarta.validation.ConstraintValidatorContext;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import static org.assertj.core.api.Assertions.assertThat;

class CheckDateTimeRangeValidatorTest {

	private ConstraintValidatorContext ctxStub = null;

	@BeforeEach
	void before() {
		ctxStub = Mockito.mock(ConstraintValidatorContext.class);
	}

	private static CheckDateTimeRangeValidator instance() {
		return new CheckDateTimeRangeValidator();
	}

	@Test
	void shouldValidateOnNullInput() {
		// ARRANGE, ACT, ASSERT
		assertThat(instance().isValid(null, ctxStub)).isTrue();
	}

	@Test
	void shouldValidateOnValidInput() {
		// ARRANGE
		DateTimeRange validRange = DateTimeRange.of(
				LocalDateTime.of(2025, 1, 1, 0, 0, 0),
				LocalDateTime.of(2025, 12, 31, 15, 30, 0));

		// ACT
		boolean actual = instance().isValid(validRange, ctxStub);

		// ASSERT
		assertThat(actual).isTrue();
	}

	@Test
	void shouldFailOnInvalidTimeInput() {
		// ARRANGE
		DateTimeRange invalidRange = DateTimeRange.of(
				LocalDateTime.of(2025, 12, 31, 18, 30, 0),
				LocalDateTime.of(2025, 12, 31, 12, 0, 0));

		// ACT
		boolean actual = instance().isValid(invalidRange, ctxStub);

		// ASSERT
		assertThat(actual).isFalse();
	}

	@Test
	void shouldFailOnInvalidDateInput() {
		// ARRANGE
		DateTimeRange invalidRange = DateTimeRange.of(
				LocalDateTime.of(2025, 12, 31, 0, 0, 0),
				LocalDateTime.of(2025, 1, 1, 0, 0, 0));

		// ACT
		boolean actual = instance().isValid(invalidRange, ctxStub);

		// ASSERT
		assertThat(actual).isFalse();
	}

	@Test
	void shouldNotFailIfInRange() {
		// ARRANGE
		DateTimeRange dateTimeRange = DateTimeRange.of(
				LocalDateTime.of(2025, 1, 1, 0, 0, 0),
				LocalDateTime.of(2025, 12, 31, 15, 30, 0));
		LocalDateTime timeInRange = LocalDateTime.of(2025, 12, 31, 1, 30, 0);

		// ACT
		boolean actual = dateTimeRange.isInRange(timeInRange);

		// ASSERT
		assertThat(actual).isTrue();
	}

	@Test
	void shouldFailIfNotInRange() {
		// ARRANGE
		DateTimeRange dateTimeRange = DateTimeRange.of(
				LocalDateTime.of(2025, 1, 1, 0, 0, 0),
				LocalDateTime.of(2025, 12, 31, 15, 30, 0));
		LocalDateTime timeNotInRange = LocalDateTime.of(2025, 12, 31, 16, 30, 0);

		// ACT
		boolean actual = dateTimeRange.isInRange(timeNotInRange);

		// ASSERT
		assertThat(actual).isFalse();
	}

	@Test
	void setTimeOfDateTimeRangeShouldWork() {
		// ARRANGE
		LocalDateTime von = LocalDateTime.of(2025, 1, 1, 0, 0, 0);
		LocalDateTime bis = LocalDateTime.of(2025, 12, 31, 15, 30, 0);
		DateTimeRange dateTimeRange = DateTimeRange.of(new DateTimeRange(von, bis));

		LocalDateTime newVon = LocalDateTime.of(2026, 1, 1, 0, 0, 0);
		LocalDateTime newBis = LocalDateTime.of(2026, 12, 31, 15, 30, 0);

		// ACT
		dateTimeRange.setVon(newVon);
		dateTimeRange.setBis(newBis);

		// ASSERT
		assertThat(von).isNotEqualTo(newVon);
		assertThat(bis).isNotEqualTo(newBis);
		assertThat(dateTimeRange.getVon()).isNotEqualTo(von);
		assertThat(dateTimeRange.getBis()).isNotEqualTo(bis);
		assertThat(dateTimeRange.getVon()).isEqualTo(newVon);
		assertThat(dateTimeRange.getBis()).isEqualTo(newBis);
	}

	@Test
	void shouldBeEqualIfTheyAre() {
		// ARRANGE
		DateTimeRange dateTimeRange1 = DateTimeRange.of(
				LocalDateTime.of(2025, 1, 1, 0, 0, 0),
				LocalDateTime.of(2025, 12, 31, 15, 30, 0));
		DateTimeRange dateTimeRange2 = DateTimeRange.of(
				LocalDateTime.of(2025, 1, 1, 0, 0, 0),
				LocalDateTime.of(2025, 12, 31, 15, 30, 0));

		// ACT
		boolean actual = dateTimeRange1.equals(dateTimeRange2);

		// ASSERT
		assertThat(actual).isTrue();
	}
}
