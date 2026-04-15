package ch.dvbern.oss.hemed.esc.api.shared.valueenhanced;

import ch.dvbern.oss.hemed.esc.api.features.anlass.sprache.types.Nachsprechen;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class SingleValueEnhancedMapperTest {

    @Test
    void empty_value_should_be_null() {
        // ARRANGE
        SingleValueEnhanced<Nachsprechen> singleValueEnhanced = SingleValueEnhanced.of();

        // ACT
        ValueEnhancedJson json = SingleValueEnhancedMapper.toJson(Nachsprechen.class, singleValueEnhanced);

        // ASSERT
        assertThat(singleValueEnhanced.getDbValue()).isNull();
        assertThat(singleValueEnhanced.getEnumValue(Nachsprechen.class)).isNull();
        assertThat(singleValueEnhanced.getNichtGeprueftGrund()).isNull();

        assertThat(json.getValue()).isNull();
        assertThat(json.getNichtGeprueftGrund()).isNull();
    }

    @Test
    void bestanden_value_should_be_bestanden() {
        // ARRANGE
        SingleValueEnhanced<Nachsprechen> singleValueEnhanced = SingleValueEnhanced.of(Nachsprechen.BESTANDEN, null);

        // ACT
        ValueEnhancedJson json = SingleValueEnhancedMapper.toJson(Nachsprechen.class, singleValueEnhanced);

        // ASSERT
        assertThat(singleValueEnhanced.getDbValue()).isEqualTo("$1$BESTANDEN$1$$2$$2$");
        assertThat(singleValueEnhanced.getEnumValue(Nachsprechen.class)).isEqualTo(Nachsprechen.BESTANDEN);
        assertThat(singleValueEnhanced.getNichtGeprueftGrund()).isNull();

        assertThat(json.getValue()).contains("BESTANDEN");
        assertThat(json.getNichtGeprueftGrund()).isNull();
    }

    @Test
    void nicht_geprueft_value_should_have_nicht_geprueft_grund() {
        // ARRANGE
        SingleValueEnhanced<Nachsprechen> singleValueEnhanced = SingleValueEnhanced.of(Nachsprechen.NICHT_GEPRUEFT, NichtGeprueftGrund.UNTERSUCHUNG_NICHT_INDIZIERT);

        // ACT
        ValueEnhancedJson json = SingleValueEnhancedMapper.toJson(Nachsprechen.class, singleValueEnhanced);

        // ASSERT
        assertThat(singleValueEnhanced.getDbValue()).isEqualTo("$1$NICHT_GEPRUEFT$1$$2$UNTERSUCHUNG_NICHT_INDIZIERT$2$");
        assertThat(singleValueEnhanced.getEnumValue(Nachsprechen.class)).isEqualTo(Nachsprechen.NICHT_GEPRUEFT);
        assertThat(singleValueEnhanced.getNichtGeprueftGrund()).isEqualTo(NichtGeprueftGrund.UNTERSUCHUNG_NICHT_INDIZIERT);

        assertThat(json.getValue()).contains("NICHT_GEPRUEFT");
        assertThat(json.getNichtGeprueftGrund()).isEqualTo(NichtGeprueftGrund.UNTERSUCHUNG_NICHT_INDIZIERT);
    }
}
