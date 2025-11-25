package ch.dvbern.oss.hemed.esc.api.shared.validator;

import java.math.BigDecimal;

import ch.dvbern.oss.hemed.esc.api.features.anlass.groessegewicht.types.PerzentileBmi;
import ch.dvbern.oss.hemed.esc.api.features.anlass.groessegewicht.types.PerzentileGewicht;
import ch.dvbern.oss.hemed.esc.api.features.anlass.groessegewicht.types.PerzentileGroesse;
import ch.dvbern.oss.hemed.esc.api.features.anlass.groessegewicht.UntersuchungGroesseGewicht;
import ch.dvbern.oss.hemed.esc.api.features.anlass.anlassconfig.types.RequirementSetting;
import ch.dvbern.oss.hemed.esc.api.testutil.builder.UntersuchungGroesseGewichtBuilder;
import org.assertj.core.api.Assertions;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

class CheckUntersuchungGroesseGewichtPresentIfReqValidatorTest {

    @ParameterizedTest
    @CsvSource({
        "REQUIRED	    ,	 ,false",
        "REQUIRED	    ,16	 ,true",
        "OPTIONAL	    ,    ,true",
        "OPTIONAL	    ,16	,true",
        "NOT_VISIBLE    ,   ,true",
        "NOT_VISIBLE    ,16	,true",
    })
    void validateGroesse(
        @NonNull RequirementSetting requirementSetting,
        @Nullable Integer groesse,
        boolean expectedValid
    ) {
        // ARRANGE
        UntersuchungGroesseGewicht entity = UntersuchungGroesseGewichtBuilder.create(
            builder -> builder
                .withGroesse(groesse));
        entity.getUntersuchung()
            .getEinladung()
            .getAnlass()
            .getAnlassConfig()
            .setGroesseGewichtGroesse(requirementSetting);
        // ACT
        CheckUntersuchungGroesseGewichtPresentIfReqValidator validator =
            new CheckUntersuchungGroesseGewichtPresentIfReqValidator();
        var result = validator.isEntityValid(entity);
        // ASSERT
        Assertions.assertThat(result).isEqualTo(expectedValid);
    }

    @ParameterizedTest
    @CsvSource({
        "REQUIRED	    ,	 ,false",
        "REQUIRED	    ,52	 ,true",
        "OPTIONAL	    ,    ,true",
        "OPTIONAL	    ,52	,true",
        "NOT_VISIBLE    ,   ,true",
        "NOT_VISIBLE    ,52	,true",
    })
    void validateGewicht(
        @NonNull RequirementSetting requirementSetting,
        @Nullable BigDecimal gewicht,
        boolean expectedValid
    ) {
        // ARRANGE
        UntersuchungGroesseGewicht entity = UntersuchungGroesseGewichtBuilder.create(
            builder -> builder
                .withGewicht(gewicht));
        entity.getUntersuchung()
            .getEinladung()
            .getAnlass()
            .getAnlassConfig()
            .setGroesseGewichtGewicht(requirementSetting);
        // ACT
        CheckUntersuchungGroesseGewichtPresentIfReqValidator validator =
            new CheckUntersuchungGroesseGewichtPresentIfReqValidator();
        var result = validator.isEntityValid(entity);
        // ASSERT
        Assertions.assertThat(result).isEqualTo(expectedValid);
    }

    @ParameterizedTest
    @CsvSource({
        "REQUIRED	    ,	 ,false",
        "REQUIRED	    ,23	 ,true",
        "OPTIONAL	    ,    ,true",
        "OPTIONAL	    ,23	,true",
        "NOT_VISIBLE    ,   ,true",
        "NOT_VISIBLE    ,23	,true",
    })
    void validateBmi(
        @NonNull RequirementSetting requirementSetting,
        @Nullable BigDecimal bmi,
        boolean expectedValid
    ) {
        // ARRANGE
        UntersuchungGroesseGewicht entity = UntersuchungGroesseGewichtBuilder.create(
            builder -> builder
                .withBmi(bmi));
        entity.getUntersuchung().getEinladung().getAnlass().getAnlassConfig().setGroesseGewichtBmi(requirementSetting);
        // ACT
        CheckUntersuchungGroesseGewichtPresentIfReqValidator validator =
            new CheckUntersuchungGroesseGewichtPresentIfReqValidator();
        var result = validator.isEntityValid(entity);
        // ASSERT
        Assertions.assertThat(result).isEqualTo(expectedValid);
    }

    @ParameterizedTest
    @CsvSource({
        "REQUIRED	    ,	    ,false",
        "REQUIRED	    ,NORMAL	,true",
        "OPTIONAL	    ,       ,true",
        "OPTIONAL	    ,NORMAL	,true",
        "NOT_VISIBLE    ,       ,true",
        "NOT_VISIBLE    ,NORMAL	,true",
    })
    void validateGroessePerzentile(
        @NonNull RequirementSetting requirementSetting,
        @Nullable PerzentileGroesse perzentile,
        boolean expectedValid
    ) {
        // ARRANGE
        UntersuchungGroesseGewicht entity = UntersuchungGroesseGewichtBuilder.create(
            builder -> builder
                .withGroessePerzentile(perzentile));
        entity.getUntersuchung()
            .getEinladung()
            .getAnlass()
            .getAnlassConfig()
            .setGroesseGewichtGroessePerzentile(requirementSetting);
        // ACT
        CheckUntersuchungGroesseGewichtPresentIfReqValidator validator =
            new CheckUntersuchungGroesseGewichtPresentIfReqValidator();
        var result = validator.isEntityValid(entity);
        // ASSERT
        Assertions.assertThat(result).isEqualTo(expectedValid);
    }

    @ParameterizedTest
    @CsvSource({
        "REQUIRED	    ,	    ,false",
        "REQUIRED	    ,NORMAL	,true",
        "OPTIONAL	    ,       ,true",
        "OPTIONAL	    ,NORMAL	,true",
        "NOT_VISIBLE    ,       ,true",
        "NOT_VISIBLE    ,NORMAL	,true",
    })
    void validateGewichtPerzentile(
        @NonNull RequirementSetting requirementSetting,
        @Nullable PerzentileGewicht perzentile,
        boolean expectedValid
    ) {
        // ARRANGE
        UntersuchungGroesseGewicht entity = UntersuchungGroesseGewichtBuilder.create(
            builder -> builder
                .withGewichtPerzentile(perzentile));
        entity.getUntersuchung()
            .getEinladung()
            .getAnlass()
            .getAnlassConfig()
            .setGroesseGewichtGewichtPerzentile(requirementSetting);
        // ACT
        CheckUntersuchungGroesseGewichtPresentIfReqValidator validator =
            new CheckUntersuchungGroesseGewichtPresentIfReqValidator();
        var result = validator.isEntityValid(entity);
        // ASSERT
        Assertions.assertThat(result).isEqualTo(expectedValid);
    }

    @ParameterizedTest
    @CsvSource({
        "REQUIRED	    ,	    ,false",
        "REQUIRED	    ,NORMAL	,true",
        "OPTIONAL	    ,       ,true",
        "OPTIONAL	    ,NORMAL	,true",
        "NOT_VISIBLE    ,       ,true",
        "NOT_VISIBLE    ,NORMAL	,true",
    })
    void validateBmiPerzentile(
        @NonNull RequirementSetting requirementSetting,
        @Nullable PerzentileBmi perzentile,
        boolean expectedValid
    ) {
        // ARRANGE
        UntersuchungGroesseGewicht entity = UntersuchungGroesseGewichtBuilder.create(
            builder -> builder
                .withBmiPerzentile(perzentile));
        entity.getUntersuchung()
            .getEinladung()
            .getAnlass()
            .getAnlassConfig()
            .setGroesseGewichtBmiPerzentile(requirementSetting);
        // ACT
        CheckUntersuchungGroesseGewichtPresentIfReqValidator validator =
            new CheckUntersuchungGroesseGewichtPresentIfReqValidator();
        var result = validator.isEntityValid(entity);
        // ASSERT
        Assertions.assertThat(result).isEqualTo(expectedValid);
    }

    @ParameterizedTest
    @CsvSource({
        "REQUIRED	    ,	        ,false",
        "REQUIRED	    ,Bemerkung	,true",
        "OPTIONAL	    ,           ,true",
        "OPTIONAL	    ,Bemerkung	,true",
        "NOT_VISIBLE    ,           ,true",
        "NOT_VISIBLE    ,Bemerkung	,true",
    })
    void validateBemerkung(
        @NonNull RequirementSetting requirementSetting,
        @Nullable String bemerkung,
        boolean expectedValid
    ) {
        // ARRANGE
        UntersuchungGroesseGewicht entity = UntersuchungGroesseGewichtBuilder.create(
            builder -> builder
                .withBemerkung(bemerkung));
        entity.getUntersuchung()
            .getEinladung()
            .getAnlass()
            .getAnlassConfig()
            .setGroesseGewichtBemerkungen(requirementSetting);
        // ACT
        CheckUntersuchungGroesseGewichtPresentIfReqValidator validator =
            new CheckUntersuchungGroesseGewichtPresentIfReqValidator();
        var result = validator.isEntityValid(entity);
        // ASSERT
        Assertions.assertThat(result).isEqualTo(expectedValid);
    }
}
