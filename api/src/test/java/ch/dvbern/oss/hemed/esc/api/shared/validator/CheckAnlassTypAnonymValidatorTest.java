package ch.dvbern.oss.hemed.esc.api.shared.validator;

import ch.dvbern.oss.hemed.esc.api.features.anlass.types.AnlassTyp;
import ch.dvbern.oss.hemed.esc.api.testutil.builder.AnlassBuilder;
import ch.dvbern.oss.hemed.esc.api.testutil.builder.AnlassConfigBuilder;
import ch.dvbern.oss.hemed.esc.api.testutil.builder.AnonymerEintragBuilder;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class CheckAnlassTypAnonymValidatorTest {

    private final CheckAnlassTypAnonymValidator validatorUdnerTest = new CheckAnlassTypAnonymValidator();

    /**
	 * Validates that the isEntityValid method correctly identifies a valid AnonymerEintrag entity
	 * with an Anlass of type 'anonym'.
	 */
	@Test
	void test_isEntityValid_returnsTrue_whenAnlassTypIsAnonym() {
		// ARRANGE

        var anonymerEintrag = AnonymerEintragBuilder.create(builder -> builder.withAnlass(
            AnlassBuilder.create(anlassBuilder -> anlassBuilder.withAnlassConfig(AnlassConfigBuilder.create(ac -> ac.withAnlassTyp(
                AnlassTyp.KINDERGARTEN_ANONYM))))
        ));

		// ACT
		boolean result = validatorUdnerTest.isEntityValid(anonymerEintrag);

		// ASSERT
		assertThat(result).isTrue();
	}

	/**
	 * Validates that the isEntityValid method correctly identifies an invalid AnonymerEintrag entity
	 * with an Anlass of type 'nicht-anonym'.
	 */
	@Test
	void test_isEntityValid_returnsFalse_whenAnlassTypIsNichtAnonym() {
		// ARRANGE
        var anonymerEintrag = AnonymerEintragBuilder.create(builder -> builder.withAnlass(
            AnlassBuilder.create(anlassBuilder -> anlassBuilder.withAnlassConfig(AnlassConfigBuilder.create(ac -> ac.withAnlassTyp(
                AnlassTyp.KINDERGARTEN))))
        ));

		// ACT
		boolean result = validatorUdnerTest.isEntityValid(anonymerEintrag);

		// ASSERT
		assertThat(result).isFalse();
	}

}
