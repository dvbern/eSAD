package ch.dvbern.oss.hemed.esc.api.shared.validator;

import ch.dvbern.oss.hemed.esc.api.features.anlass.untersuchung.Untersuchung;
import ch.dvbern.oss.hemed.esc.api.testutil.builder.AnonymerEintragBuilder;
import ch.dvbern.oss.hemed.esc.api.testutil.builder.UntersuchungBuilder;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class CheckUntersuchungEinladungOrAnonymerEintragValidatorTest {

	private final CheckUntersuchungEinladungOrAnonymerEintragValidator validator =
		new CheckUntersuchungEinladungOrAnonymerEintragValidator();

	@Test
	void shouldBeValid_whenOnlyEinladungIsSet() {
		// ARRANGE - default builder sets einladung, anonymerEintrag is null
		Untersuchung untersuchung = UntersuchungBuilder.create(builder -> {});

		// ACT
		boolean result = validator.isEntityValid(untersuchung);

		// ASSERT
		assertThat(result).isTrue();
	}

	@Test
	void shouldBeValid_whenOnlyAnonymerEintragIsSet() {
		// ARRANGE
		Untersuchung untersuchung = UntersuchungBuilder.create(builder -> builder
			.withEinladung(null)
			.withAnonymerEintrag(AnonymerEintragBuilder.defaultAnonymerEintrag()));

		// ACT
		boolean result = validator.isEntityValid(untersuchung);

		// ASSERT
		assertThat(result).isTrue();
	}

	@Test
	void shouldBeInvalid_whenBothAreSet() {
		// ARRANGE
		Untersuchung untersuchung = UntersuchungBuilder.create(builder -> builder
			.withAnonymerEintrag(AnonymerEintragBuilder.defaultAnonymerEintrag()));

		// ACT
		boolean result = validator.isEntityValid(untersuchung);

		// ASSERT
		assertThat(result).isFalse();
	}

	@Test
	void shouldBeInvalid_whenNeitherIsSet() {
		// ARRANGE
		Untersuchung untersuchung = UntersuchungBuilder.create(builder -> builder
			.withEinladung(null));

		// ACT
		boolean result = validator.isEntityValid(untersuchung);

		// ASSERT
		assertThat(result).isFalse();
	}
}
