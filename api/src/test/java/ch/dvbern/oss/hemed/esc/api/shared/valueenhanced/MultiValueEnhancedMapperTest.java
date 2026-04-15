package ch.dvbern.oss.hemed.esc.api.shared.valueenhanced;

import java.util.EnumSet;

import ch.dvbern.oss.hemed.esc.api.features.anlass.sprache.types.Nachsprechen;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class MultiValueEnhancedMapperTest {


    @Test
    void empty_value_should_be_null() {
        // ARRANGE
        MultiValueEnhanced<Nachsprechen> multiValueEnhanced = new MultiValueEnhanced<>();

        // ACT
        ValueEnhancedJson json = MultiValueEnhancedMapper.toJson(Nachsprechen.class, multiValueEnhanced);

        // ASSERT
        assertThat(multiValueEnhanced.getDbValue()).isNull();
        assertThat(multiValueEnhanced.getEnumValues(Nachsprechen.class)).isEmpty();
        assertThat(multiValueEnhanced.getNichtGeprueftGrund()).isNull();

        assertThat(json.getValue()).isEmpty();
        assertThat(json.getNichtGeprueftGrund()).isNull();
    }

    @Test
    void bestanden_value_should_be_bestanden() {
        // ARRANGE
        MultiValueEnhanced<Nachsprechen> multiValueEnhanced = MultiValueEnhanced.of(
            EnumSet.of(Nachsprechen.BESTANDEN, Nachsprechen.NICHT_BESTANDEN), null);

        // ACT
        ValueEnhancedJson json = MultiValueEnhancedMapper.toJson(Nachsprechen.class, multiValueEnhanced);

        // ASSERT
        assertThat(multiValueEnhanced.getDbValue()).isEqualTo("$1$BESTANDEN,NICHT_BESTANDEN$1$$2$$2$");
        assertThat(multiValueEnhanced.getEnumValues(Nachsprechen.class)).isNotEmpty().contains(Nachsprechen.BESTANDEN, Nachsprechen.NICHT_BESTANDEN);
        assertThat(multiValueEnhanced.getNichtGeprueftGrund()).isNull();

        assertThat(json.getValue()).contains("BESTANDEN", "NICHT_BESTANDEN");
        assertThat(json.getNichtGeprueftGrund()).isNull();
    }

    @Test
    void nicht_geprueft_value_should_have_nicht_geprueft_grund() {
        // ARRANGE
        MultiValueEnhanced<Nachsprechen> multiValueEnhanced = MultiValueEnhanced.of(
            EnumSet.of(Nachsprechen.NICHT_GEPRUEFT), NichtGeprueftGrund.UNTERSUCHUNG_NICHT_INDIZIERT);

        // ACT
        ValueEnhancedJson json = MultiValueEnhancedMapper.toJson(Nachsprechen.class, multiValueEnhanced);

        // ASSERT
        assertThat(multiValueEnhanced.getDbValue()).isEqualTo("$1$NICHT_GEPRUEFT$1$$2$UNTERSUCHUNG_NICHT_INDIZIERT$2$");
        assertThat(multiValueEnhanced.getEnumValues(Nachsprechen.class)).isNotEmpty().contains(Nachsprechen.NICHT_GEPRUEFT);
        assertThat(multiValueEnhanced.getNichtGeprueftGrund()).isEqualTo(NichtGeprueftGrund.UNTERSUCHUNG_NICHT_INDIZIERT);

        assertThat(json.getValue()).contains("NICHT_GEPRUEFT");
        assertThat(json.getNichtGeprueftGrund()).isEqualTo(NichtGeprueftGrund.UNTERSUCHUNG_NICHT_INDIZIERT);
    }
}
