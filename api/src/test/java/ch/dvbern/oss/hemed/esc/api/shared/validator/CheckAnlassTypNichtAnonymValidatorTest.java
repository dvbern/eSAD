package ch.dvbern.oss.hemed.esc.api.shared.validator;

import ch.dvbern.oss.hemed.esc.api.features.anlass.Anlass;
import ch.dvbern.oss.hemed.esc.api.features.anlass.anlassconfig.AnlassConfig;
import ch.dvbern.oss.hemed.esc.api.features.anlass.einladung.Einladung;
import ch.dvbern.oss.hemed.esc.api.features.anlass.types.AnlassTyp;
import ch.dvbern.oss.hemed.esc.api.testutil.builder.AnlassBuilder;
import ch.dvbern.oss.hemed.esc.api.testutil.builder.AnlassConfigBuilder;
import ch.dvbern.oss.hemed.esc.api.testutil.builder.EinladungBuilder;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class CheckAnlassTypNichtAnonymValidatorTest {

    private final CheckAnlassTypNichtAnonymValidator validatorUdnerTest = new CheckAnlassTypNichtAnonymValidator();

    @Test
    void test_CheckAnlassTypNichtAnonymValidator_returnsFalse_whenAnlassTypIsAnonym() {
        // ARRANGE

        AnlassConfig config = AnlassConfigBuilder.create(ac -> ac.withAnlassTyp(
            AnlassTyp.KINDERGARTEN_ANONYM));
        Anlass anlass =
            AnlassBuilder.create(anlassBuilder -> {
                anlassBuilder.withAnlassConfig(config);
            });
        Einladung einladung = EinladungBuilder.create(builder -> builder.withAnlass(anlass));

        // ACT
        boolean result = validatorUdnerTest.isEntityValid(einladung);

        // ASSERT
        assertThat(result).isFalse();
    }

    @Test
    void test_CheckAnlassTypNichtAnonymValidator_returnsTrue_whenAnlassTypIsNotAnonym() {
        // ARRANGE
        AnlassConfig config = AnlassConfigBuilder.create(ac -> ac.withAnlassTyp(
            AnlassTyp.KINDERGARTEN));
        Anlass anlass =
            AnlassBuilder.create(anlassBuilder -> {
                anlassBuilder.withAnlassConfig(config);
            });
        Einladung einladung = EinladungBuilder.create(builder -> builder.withAnlass(anlass));

        // ACT
        boolean result = validatorUdnerTest.isEntityValid(einladung);

        // ASSERT
        assertThat(result).isTrue();
    }

}
