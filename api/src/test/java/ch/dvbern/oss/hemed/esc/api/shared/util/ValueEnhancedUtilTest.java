package ch.dvbern.oss.hemed.esc.api.shared.util;

import ch.dvbern.dvbstarter.shared.i18n.translations.TL;
import ch.dvbern.oss.hemed.esc.api.features.anlass.anlassconfig.AnlassConfig;
import ch.dvbern.oss.hemed.esc.api.features.anlass.anlassconfig.types.RequirementSetting;
import ch.dvbern.oss.hemed.esc.api.features.anlass.sprache.types.Nachsprechen;
import ch.dvbern.oss.hemed.esc.api.shared.abstractentity.AbstractEntity;
import ch.dvbern.oss.hemed.esc.api.shared.valueenhanced.MultiValueEnhanced;
import ch.dvbern.oss.hemed.esc.api.shared.valueenhanced.NichtGeprueftGrund;
import ch.dvbern.oss.hemed.esc.api.shared.valueenhanced.SingleValueEnhanced;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class ValueEnhancedUtilTest {

    @ParameterizedTest
    @CsvSource({
        "printer.boolean.false, - , true, ",
        "printer.boolean.false, - , false, ",
        "printer.boolean.true, X , false, BESTANDEN",
        "printer.boolean.false, - , false, NICHT_BESTANDEN",

    })
    void translateEnumValueForColumnToFlag(
        String expectedTranslationKey,
        String expectedTranslation,
        boolean isNullValue,
        Nachsprechen nachsprechen) {
        // ARRANGE
        TL tl = mock(TL.class);
        when(tl.translate(expectedTranslationKey)).thenReturn(expectedTranslation);
        SingleValueEnhanced<Nachsprechen> value = isNullValue ? null : SingleValueEnhanced.of(nachsprechen, null);

        // ACT
        String result =
            ValueEnhancedUtil.translateEnumValueForColumnToFlag(value, Nachsprechen.BESTANDEN, tl, Nachsprechen.class);

        // ASSERT
        assertThat(result).isEqualTo(expectedTranslation);
    }

    @Test
    void translateEnhancedEnumValue_should_return_translation_when_present() {
        // ARRANGE
        TL tl = mock(TL.class);
        when(tl.translateValue(Nachsprechen.BESTANDEN)).thenReturn("Bestanden Translation");
        SingleValueEnhanced<Nachsprechen> value = SingleValueEnhanced.of(Nachsprechen.BESTANDEN, null);

        // ACT
        String result = ValueEnhancedUtil.translateEnhancedEnumValue(value, tl, Nachsprechen.class);

        // ASSERT
        assertThat(result).isEqualTo("Bestanden Translation");
    }

    @Test
    void getSingleValueEnhancedForUntersuchungAbschliessen_should_return_of_when_not_visible() {
        // ARRANGE
        AnlassConfig config = mock(AnlassConfig.class);

        // ACT
        SingleValueEnhanced<Nachsprechen> result = ValueEnhancedUtil.getSingleValueEnhancedForUntersuchungAbschliessen(
            config,
            c -> RequirementSetting.NOT_VISIBLE,
            null,
            u -> null,
            Nachsprechen.BESTANDEN,
            false
        );

        // ASSERT
        assertThat(result.getEnumValue(Nachsprechen.class)).isNull();
        assertThat(result.getNichtGeprueftGrund()).isNull();
    }

    @Test
    void getSingleValueEnhancedForUntersuchungAbschliessen_should_return_default_when_untersuchung_null() {
        // ARRANGE
        AnlassConfig config = mock(AnlassConfig.class);

        // ACT
        SingleValueEnhanced<Nachsprechen> result = ValueEnhancedUtil.getSingleValueEnhancedForUntersuchungAbschliessen(
            config,
            c -> RequirementSetting.OPTIONAL,
            null,
            u -> null,
            Nachsprechen.BESTANDEN,
            false
        );

        // ASSERT
        assertThat(result.getEnumValue(Nachsprechen.class)).isEqualTo(Nachsprechen.BESTANDEN);
        assertThat(result.getNichtGeprueftGrund()).isNull();
    }

    @Test
    void getSingleValueEnhancedForUntersuchungAbschliessen_should_return_extracted_value() {
        // ARRANGE
        AnlassConfig config = mock(AnlassConfig.class);
        TestEntity entity = new TestEntity();
        SingleValueEnhanced<Nachsprechen> value = SingleValueEnhanced.of(Nachsprechen.NICHT_BESTANDEN, null);

        // ACT
        SingleValueEnhanced<Nachsprechen> result = ValueEnhancedUtil.getSingleValueEnhancedForUntersuchungAbschliessen(
            config,
            c -> RequirementSetting.OPTIONAL,
            entity,
            u -> value,
            Nachsprechen.BESTANDEN,
            false
        );

        // ASSERT
        assertThat(result).isEqualTo(value);
        assertThat(result.getNichtGeprueftGrund()).isNull();
    }

    @Test
    void getSingleValueEnhancedForUntersuchungAbschliessen_should_return_default_when_extracted_null() {
        // ARRANGE
        AnlassConfig config = mock(AnlassConfig.class);
        TestEntity entity = new TestEntity();

        // ACT
        SingleValueEnhanced<Nachsprechen> result = ValueEnhancedUtil.getSingleValueEnhancedForUntersuchungAbschliessen(
            config,
            c -> RequirementSetting.OPTIONAL,
            entity,
            u -> null,
            Nachsprechen.BESTANDEN,
            false
        );

        // ASSERT
        assertThat(result.getEnumValue(Nachsprechen.class)).isEqualTo(Nachsprechen.BESTANDEN);
    }

    @Test
    void getSingleValueEnhancedForUntersuchungAbschliessen_should_return_default_with_nichtGeprueftGrund_when_feature_enabled_and_extracted_null() {
        // ARRANGE
        AnlassConfig config = mock(AnlassConfig.class);
        TestEntity entity = new TestEntity();

        // ACT
        SingleValueEnhanced<Nachsprechen> result = ValueEnhancedUtil.getSingleValueEnhancedForUntersuchungAbschliessen(
            config,
            c -> RequirementSetting.OPTIONAL,
            entity,
            u -> null,
            Nachsprechen.NICHT_GEPRUEFT,
            true
        );

        // ASSERT
        assertThat(result.getEnumValue(Nachsprechen.class)).isEqualTo(Nachsprechen.NICHT_GEPRUEFT);
        assertThat(result.getNichtGeprueftGrund()).isEqualTo(NichtGeprueftGrund.ANDERER_GRUND);
    }

    @Test
    void getSingleValueEnhancedForUntersuchungAbschliessen_should_return_default_with_nichtGeprueftGrund_when_feature_enabled_and_extracted_empty() {
        // ARRANGE
        AnlassConfig config = mock(AnlassConfig.class);
        TestEntity entity = new TestEntity();
        SingleValueEnhanced<Nachsprechen> emptyValue = SingleValueEnhanced.of();

        // ACT
        SingleValueEnhanced<Nachsprechen> result = ValueEnhancedUtil.getSingleValueEnhancedForUntersuchungAbschliessen(
            config,
            c -> RequirementSetting.OPTIONAL,
            entity,
            u -> emptyValue,
            Nachsprechen.NICHT_GEPRUEFT,
            true
        );

        // ASSERT
        assertThat(result.getEnumValue(Nachsprechen.class)).isEqualTo(Nachsprechen.NICHT_GEPRUEFT);
        assertThat(result.getNichtGeprueftGrund()).isEqualTo(NichtGeprueftGrund.ANDERER_GRUND);
    }

    @Test
    void getSingleValueEnhancedForUntersuchungAbschliessen_should_return_default_without_nichtGeprueftGrund_when_feature_enabled_but_defaultWert_not_nichtGeprueft() {
        // ARRANGE
        AnlassConfig config = mock(AnlassConfig.class);
        TestEntity entity = new TestEntity();

        // ACT
        SingleValueEnhanced<Nachsprechen> result = ValueEnhancedUtil.getSingleValueEnhancedForUntersuchungAbschliessen(
            config,
            c -> RequirementSetting.OPTIONAL,
            entity,
            u -> null,
            Nachsprechen.BESTANDEN,
            true
        );

        // ASSERT
        assertThat(result.getEnumValue(Nachsprechen.class)).isEqualTo(Nachsprechen.BESTANDEN);
        assertThat(result.getNichtGeprueftGrund()).isNull();
    }

    @Test
    void getMultiValueEnhancedForUntersuchungAbschliessen_should_return_default_with_nichtGeprueftGrund_when_feature_enabled_and_extracted_null() {
        // ARRANGE
        AnlassConfig config = mock(AnlassConfig.class);
        TestEntity entity = new TestEntity();

        // ACT
        MultiValueEnhanced<Nachsprechen> result = ValueEnhancedUtil.getMultiValueEnhancedForUntersuchungAbschliessen(
            config,
            c -> RequirementSetting.OPTIONAL,
            entity,
            u -> null,
            Nachsprechen.NICHT_GEPRUEFT,
            true
        );

        // ASSERT
        assertThat(result.getEnumValues(Nachsprechen.class)).containsExactly(Nachsprechen.NICHT_GEPRUEFT);
        assertThat(result.getNichtGeprueftGrund()).isEqualTo(NichtGeprueftGrund.ANDERER_GRUND);
    }

    @Test
    void getMultiValueEnhancedForUntersuchungAbschliessen_should_return_default_with_nichtGeprueftGrund_when_feature_enabled_and_extracted_empty() {
        // ARRANGE
        AnlassConfig config = mock(AnlassConfig.class);
        TestEntity entity = new TestEntity();
        MultiValueEnhanced<Nachsprechen> emptyValue = MultiValueEnhanced.of();

        // ACT
        MultiValueEnhanced<Nachsprechen> result = ValueEnhancedUtil.getMultiValueEnhancedForUntersuchungAbschliessen(
            config,
            c -> RequirementSetting.OPTIONAL,
            entity,
            u -> emptyValue,
            Nachsprechen.NICHT_GEPRUEFT,
            true
        );

        // ASSERT
        assertThat(result.getEnumValues(Nachsprechen.class)).containsExactly(Nachsprechen.NICHT_GEPRUEFT);
        assertThat(result.getNichtGeprueftGrund()).isEqualTo(NichtGeprueftGrund.ANDERER_GRUND);
    }

    @Test
    void getMultiValueEnhancedForUntersuchungAbschliessen_should_return_default_without_nichtGeprueftGrund_when_feature_enabled_but_defaultWert_not_nichtGeprueft() {
        // ARRANGE
        AnlassConfig config = mock(AnlassConfig.class);
        TestEntity entity = new TestEntity();

        // ACT
        MultiValueEnhanced<Nachsprechen> result = ValueEnhancedUtil.getMultiValueEnhancedForUntersuchungAbschliessen(
            config,
            c -> RequirementSetting.OPTIONAL,
            entity,
            u -> null,
            Nachsprechen.BESTANDEN,
            true
        );

        // ASSERT
        assertThat(result.getEnumValues(Nachsprechen.class)).containsExactly(Nachsprechen.BESTANDEN);
        assertThat(result.getNichtGeprueftGrund()).isNull();

    }

    @Test
    void isRequiredAndNull() {
        // ACT & ASSERT
        assertThat(ValueEnhancedUtil.isRequiredAndNull(RequirementSetting.REQUIRED, null, Nachsprechen.class)).isTrue();
        assertThat(ValueEnhancedUtil.isRequiredAndNull(
            RequirementSetting.REQUIRED,
            SingleValueEnhanced.of(),
            Nachsprechen.class)).isTrue();
        assertThat(ValueEnhancedUtil.isRequiredAndNull(
            RequirementSetting.OPTIONAL,
            null,
            Nachsprechen.class)).isFalse();
        assertThat(ValueEnhancedUtil.isRequiredAndNull(
            RequirementSetting.REQUIRED,
            SingleValueEnhanced.of(Nachsprechen.BESTANDEN, null),
            Nachsprechen.class)).isFalse();
    }

    @Test
    void isVisibleAndNull_should_return_true_when_visible_and_null() {
        // ACT & ASSERT
        assertThat(ValueEnhancedUtil.isVisibleAndNull(RequirementSetting.OPTIONAL, null, Nachsprechen.class)).isTrue();
        assertThat(ValueEnhancedUtil.isVisibleAndNull(RequirementSetting.REQUIRED, null, Nachsprechen.class)).isTrue();
        assertThat(ValueEnhancedUtil.isVisibleAndNull(
            RequirementSetting.NOT_VISIBLE,
            null,
            Nachsprechen.class)).isFalse();
        assertThat(ValueEnhancedUtil.isVisibleAndNull(
            RequirementSetting.OPTIONAL,
            SingleValueEnhanced.of(Nachsprechen.BESTANDEN, null),
            Nachsprechen.class)).isFalse();
    }

    @Test
    void geToEnumValueOrNull_should_return_value_or_null() {
        // ACT & ASSERT
        assertThat(ValueEnhancedUtil.getEnumValueOrNull(null, Nachsprechen.class)).isNull();
        assertThat(ValueEnhancedUtil.getEnumValueOrNull(SingleValueEnhanced.of(), Nachsprechen.class)).isNull();
        assertThat(ValueEnhancedUtil.getEnumValueOrNull(
            SingleValueEnhanced.of(Nachsprechen.BESTANDEN, null),
            Nachsprechen.class)).isEqualTo(Nachsprechen.BESTANDEN);
    }

    @Test
    void isNull_should_return_correct_boolean() {
        // ACT & ASSERT
        assertThat(ValueEnhancedUtil.isNull(null, Nachsprechen.class)).isTrue();
        assertThat(ValueEnhancedUtil.isNull(SingleValueEnhanced.of(), Nachsprechen.class)).isTrue();
        assertThat(ValueEnhancedUtil.isNull(
            SingleValueEnhanced.of(Nachsprechen.BESTANDEN, null),
            Nachsprechen.class)).isFalse();
    }

    private static class TestEntity extends AbstractEntity<TestEntity> {
    }
}
