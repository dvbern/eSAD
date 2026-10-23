package ch.dvbern.oss.hemed.esc.api.shared.valueenhanced;

import ch.dvbern.oss.hemed.esc.api.features.anlass.sprache.types.Nachsprechen;
import org.junit.jupiter.api.DisplayNameGeneration;
import org.junit.jupiter.api.DisplayNameGenerator.ReplaceUnderscores;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayNameGeneration(ReplaceUnderscores.class)
class AbstractValueEnhancedTest {

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {"   ", "\t\n"})
    void extractEnumValue_returns_null_for_empty_or_blank_input(String input) {
        assertThat(AbstractValueEnhanced.extractEnumValue(input, Nachsprechen.class)).isNull();
    }

    @Test
    void extractEnumValue_returns_enum_when_valid_input() {
        String dbValue = "$1$BESTANDEN$1$$2$$2$";
        assertThat(AbstractValueEnhanced.extractEnumValue(dbValue, Nachsprechen.class))
            .isEqualTo(Nachsprechen.BESTANDEN);
    }

    @Test
    void extractEnumValue_returns_null_when_enum_part_is_empty_or_missing() {
        assertThat(AbstractValueEnhanced.extractEnumValue("$1$$1$$2$UNTERSUCHUNG_NICHT_INDIZIERT$2$", Nachsprechen.class))
            .isNull();
        assertThat(AbstractValueEnhanced.extractEnumValue("INVALID_FORMAT", Nachsprechen.class))
            .isNull();
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {"   ", "\t\n"})
    void extractEnumValues_returns_empty_set_for_empty_or_blank_input(String input) {
        assertThat(AbstractValueEnhanced.extractEnumValues(input, Nachsprechen.class))
            .isEmpty();
    }

    @Test
    void extractEnumValues_returns_enum_set_when_valid_input() {
        String dbValue = "$1$BESTANDEN,NICHT_BESTANDEN$1$$2$$2$";
        assertThat(AbstractValueEnhanced.extractEnumValues(dbValue, Nachsprechen.class))
            .containsExactlyInAnyOrder(Nachsprechen.BESTANDEN, Nachsprechen.NICHT_BESTANDEN);
    }

    @Test
    void extractEnumValues_returns_empty_set_when_enum_part_is_empty_or_missing() {
        assertThat(AbstractValueEnhanced.extractEnumValues("$1$$1$$2$UNTERSUCHUNG_NICHT_INDIZIERT$2$", Nachsprechen.class))
            .isEmpty();
        assertThat(AbstractValueEnhanced.extractEnumValues("INVALID_FORMAT", Nachsprechen.class))
            .isEmpty();
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {"   ", "\t\n"})
    void extractNichtGeprueftGrund_returns_null_for_empty_or_blank_input(String input) {
        assertThat(AbstractValueEnhanced.extractNichtGeprueftGrund(input)).isNull();
    }

    @Test
    void extractNichtGeprueftGrund_returns_grund_when_valid_input() {
        String dbValue = "$1$NICHT_GEPRUEFT$1$$2$UNTERSUCHUNG_NICHT_INDIZIERT$2$";
        assertThat(AbstractValueEnhanced.extractNichtGeprueftGrund(dbValue))
            .isEqualTo(NichtGeprueftGrund.UNTERSUCHUNG_NICHT_INDIZIERT);
    }

    @Test
    void extractNichtGeprueftGrund_returns_null_when_grund_part_is_empty_or_missing() {
        assertThat(AbstractValueEnhanced.extractNichtGeprueftGrund("$1$BESTANDEN$1$$2$$2$"))
            .isNull();
        assertThat(AbstractValueEnhanced.extractNichtGeprueftGrund("INVALID_FORMAT"))
            .isNull();
    }
}
