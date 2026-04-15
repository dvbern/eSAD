package ch.dvbern.oss.hemed.esc.api.shared.validator;

import java.util.List;

import ch.dvbern.oss.hemed.esc.api.features.anlass.hoeren.types.AudiogrammBeurteilung;
import ch.dvbern.oss.hemed.esc.api.features.anlass.hoeren.types.Dezibel;
import ch.dvbern.oss.hemed.esc.api.features.anlass.hoeren.types.HoerenZusaetzlicheAngaben;
import ch.dvbern.oss.hemed.esc.api.features.anlass.hoeren.types.AudiogrammSimpel;
import ch.dvbern.oss.hemed.esc.api.features.anlass.hoeren.types.HoerenHoerhilfe;
import ch.dvbern.oss.hemed.esc.api.features.anlass.hoeren.UntersuchungHoerenAudiogramm;
import ch.dvbern.oss.hemed.esc.api.features.anlass.hoeren.types.HoerenEmpfehlung;
import ch.dvbern.oss.hemed.esc.api.features.anlass.anlassconfig.types.RequirementSetting;
import ch.dvbern.oss.hemed.esc.api.shared.valueenhanced.SingleValueEnhanced;
import ch.dvbern.oss.hemed.esc.api.testutil.builder.UntersuchungHoerenAudiogrammBuilder;
import org.assertj.core.api.Assertions;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

class CheckUntersuchungHoerenAudiogrammPresentIfReqValidatorTest {

    @ParameterizedTest
    @CsvSource({
        "REQUIRED	,	    ,DB10   ,false",
        "REQUIRED	,DB10	,       ,false",
        "REQUIRED	,	    ,       ,false",
        "REQUIRED	,DB10	,DB10   ,true",
        "OPTIONAL	,       ,DB10   ,true",
        "OPTIONAL	,DB10	,       ,true",
        "OPTIONAL	,       ,       ,true",
        "OPTIONAL	,DB10	,DB10   ,true",
        "NOT_VISIBLE,       ,DB10   ,true",
        "NOT_VISIBLE,DB10	,       ,true",
        "NOT_VISIBLE,       ,       ,true",
        "NOT_VISIBLE,DB10	,DB10   ,true",
    })
    void validateAudiogramm(
        @NonNull RequirementSetting requirementSetting,
        @Nullable Dezibel audiogrammRechts,
        @Nullable Dezibel audiogrammLinks,
        boolean expectedValid
    ) {
        // ARRANGE
        UntersuchungHoerenAudiogramm hoeren = UntersuchungHoerenAudiogrammBuilder.create(
            builder -> builder
                .withAudiogrammRechts(audiogrammRechts)
                .withAudiogrammLinks(audiogrammLinks));
        hoeren.getUntersuchung().getEinladung().getAnlass().getAnlassConfig().setHoerenAudiogramm(requirementSetting);
        // ACT
        CheckUntersuchungHoerenAudiogrammPresentIfReqValidator
            validator = new CheckUntersuchungHoerenAudiogrammPresentIfReqValidator();
        boolean result = validator.isEntityValid(hoeren);
        // ASSERT
        Assertions.assertThat(result).isEqualTo(expectedValid);
    }

    @ParameterizedTest
    @CsvSource({
        "REQUIRED	,REQUIRED	,	    ,	    ,false",
        "REQUIRED	,REQUIRED	,DB10	,DB10	,false",
        "REQUIRED   ,OPTIONAL	,DB10   ,       ,false",
        "OPTIONAL   ,REQUIRED	,       ,DB10   ,false",
        "OPTIONAL   ,REQUIRED	,DB10   ,DB10   ,false",
        "OPTIONAL	,OPTIONAL	,       ,       ,true",
        "OPTIONAL	,OPTIONAL	,DB10	,DB10	,true",
        "NOT_VISIBLE,NOT_VISIBLE,       ,       ,true",
        "NOT_VISIBLE,NOT_VISIBLE,DB10	,DB10	,true",
    })
    void valdidateAudiogrammWithout250Hz(
        @NonNull RequirementSetting requirementSetting,
        @NonNull RequirementSetting requirementSetting250Hz,
        @Nullable Dezibel audiogrammRechts,
        @Nullable Dezibel audiogrammLinks,
        boolean expectedValid
    ) {
        // ARRANGE
        UntersuchungHoerenAudiogramm hoeren = UntersuchungHoerenAudiogrammBuilder.create(
            builder -> builder
                .withAudiogrammRechtsWithout250Hz(audiogrammRechts)
                .withAudiogrammLinksWithout250Hz(audiogrammLinks));
        hoeren.getUntersuchung().getEinladung().getAnlass().getAnlassConfig().setHoerenAudiogramm(requirementSetting);
        hoeren.getUntersuchung()
            .getEinladung()
            .getAnlass()
            .getAnlassConfig()
            .setHoerenAudiogramm250Hz(requirementSetting250Hz);
        // ACT
        CheckUntersuchungHoerenAudiogrammPresentIfReqValidator
            validator = new CheckUntersuchungHoerenAudiogrammPresentIfReqValidator();
        boolean result = validator.isEntityValid(hoeren);
        // ASSERT
        Assertions.assertThat(result).isEqualTo(expectedValid);
    }

    @ParameterizedTest
    @CsvSource({
        "REQUIRED	,REQUIRED	,	    ,	    ,false",
        "REQUIRED	,REQUIRED	,DB10	,DB10	,true",
        "OPTIONAL	,REQUIRED	,	    ,	    ,false",
        "REQUIRED	,OPTIONAL	,	    ,	    ,false",
        "REQUIRED	,OPTIONAL	,DB10	,DB10	,true",
        "OPTIONAL	,REQUIRED	,DB10	,DB10	,true",
        "REQUIRED	,OPTIONAL	,DB10	,    	,false",
        "OPTIONAL	,REQUIRED	,DB10	,    	,false",
        "OPTIONAL	,OPTIONAL	,       ,       ,true",
        "OPTIONAL	,OPTIONAL	,DB10	,DB10	,true",
        "NOT_VISIBLE,NOT_VISIBLE,       ,       ,true",
        "NOT_VISIBLE,NOT_VISIBLE,DB10	,DB10	,true",
    })
    void validateAudiogrammWith250Hz(
        @NonNull RequirementSetting requirementSetting,
        @NonNull RequirementSetting requirementSetting250Hz,
        @Nullable Dezibel audiogrammRechts,
        @Nullable Dezibel audiogrammLinks,
        boolean expectedValid
    ) {
        // ARRANGE
        UntersuchungHoerenAudiogramm hoeren = UntersuchungHoerenAudiogrammBuilder.create(
            builder -> builder
                .withAudiogrammRechtsWithout8000Hz(audiogrammRechts)
                .withAudiogrammLinksWithout8000Hz(audiogrammLinks));
        hoeren.getUntersuchung().getEinladung().getAnlass().getAnlassConfig().setHoerenAudiogramm(requirementSetting);
        hoeren.getUntersuchung()
            .getEinladung()
            .getAnlass()
            .getAnlassConfig()
            .setHoerenAudiogramm250Hz(requirementSetting250Hz);
        // ACT
        CheckUntersuchungHoerenAudiogrammPresentIfReqValidator
            validator = new CheckUntersuchungHoerenAudiogrammPresentIfReqValidator();
        boolean result = validator.isEntityValid(hoeren);
        // ASSERT
        Assertions.assertThat(result).isEqualTo(expectedValid);
    }

    @ParameterizedTest
    @CsvSource({
        "REQUIRED	,REQUIRED	,	    ,	    ,false",
        "REQUIRED	,REQUIRED	,DB10	,DB10	,false",
        "REQUIRED   ,OPTIONAL	,DB10   ,       ,false",
        "OPTIONAL   ,REQUIRED	,       ,DB10   ,false",
        "OPTIONAL   ,REQUIRED	,DB10   ,DB10   ,false",
        "OPTIONAL	,OPTIONAL	,       ,       ,true",
        "OPTIONAL	,OPTIONAL	,DB10	,DB10	,true",
        "NOT_VISIBLE,NOT_VISIBLE,       ,       ,true",
        "NOT_VISIBLE,NOT_VISIBLE,DB10	,DB10	,true",
    })
    void valdidateAudiogrammWithout8000Hz(
        @NonNull RequirementSetting requirementSetting,
        @NonNull RequirementSetting requirementSetting8000Hz,
        @Nullable Dezibel audiogrammRechts,
        @Nullable Dezibel audiogrammLinks,
        boolean expectedValid
    ) {
        // ARRANGE
        UntersuchungHoerenAudiogramm hoeren = UntersuchungHoerenAudiogrammBuilder.create(
            builder -> builder
                .withAudiogrammRechtsWithout8000Hz(audiogrammRechts)
                .withAudiogrammLinksWithout8000Hz(audiogrammLinks));
        hoeren.getUntersuchung().getEinladung().getAnlass().getAnlassConfig().setHoerenAudiogramm(requirementSetting);
        hoeren.getUntersuchung()
            .getEinladung()
            .getAnlass()
            .getAnlassConfig()
            .setHoerenAudiogramm8000Hz(requirementSetting8000Hz);
        // ACT
        CheckUntersuchungHoerenAudiogrammPresentIfReqValidator
            validator = new CheckUntersuchungHoerenAudiogrammPresentIfReqValidator();
        boolean result = validator.isEntityValid(hoeren);
        // ASSERT
        Assertions.assertThat(result).isEqualTo(expectedValid);
    }

    @ParameterizedTest
    @CsvSource({
        "REQUIRED	,REQUIRED	,	    ,	    ,false",
        "REQUIRED	,REQUIRED	,DB10	,DB10	,true",
        "OPTIONAL	,REQUIRED	,	    ,	    ,false",
        "REQUIRED	,OPTIONAL	,	    ,	    ,false",
        "REQUIRED	,OPTIONAL	,DB10	,DB10	,true",
        "OPTIONAL	,REQUIRED	,DB10	,DB10	,true",
        "REQUIRED	,OPTIONAL	,DB10	,    	,false",
        "OPTIONAL	,REQUIRED	,DB10	,    	,false",
        "OPTIONAL	,OPTIONAL	,       ,       ,true",
        "OPTIONAL	,OPTIONAL	,DB10	,DB10	,true",
        "NOT_VISIBLE,NOT_VISIBLE,       ,       ,true",
        "NOT_VISIBLE,NOT_VISIBLE,DB10	,DB10	,true",
    })
    void validateAudiogrammWith8000Hz(
        @NonNull RequirementSetting requirementSetting,
        @NonNull RequirementSetting requirementSetting8000Hz,
        @Nullable Dezibel audiogrammRechts,
        @Nullable Dezibel audiogrammLinks,
        boolean expectedValid
    ) {
        // ARRANGE
        UntersuchungHoerenAudiogramm hoeren = UntersuchungHoerenAudiogrammBuilder.create(
            builder -> builder
                .withAudiogrammRechtsWithout250Hz(audiogrammRechts)
                .withAudiogrammLinksWithout250Hz(audiogrammLinks));
        hoeren.getUntersuchung().getEinladung().getAnlass().getAnlassConfig().setHoerenAudiogramm(requirementSetting);
        hoeren.getUntersuchung()
            .getEinladung()
            .getAnlass()
            .getAnlassConfig()
            .setHoerenAudiogramm8000Hz(requirementSetting8000Hz);
        // ACT
        CheckUntersuchungHoerenAudiogrammPresentIfReqValidator
            validator = new CheckUntersuchungHoerenAudiogrammPresentIfReqValidator();
        boolean result = validator.isEntityValid(hoeren);
        // ASSERT
        Assertions.assertThat(result).isEqualTo(expectedValid);
    }

    @ParameterizedTest
    @CsvSource({
        "REQUIRED	,REQUIRED	,REQUIRED	,	    ,	    ,false",
        "REQUIRED	,NOT_VISIBLE,NOT_VISIBLE,DB10	,DB10	,true",
        "REQUIRED	,NOT_VISIBLE,REQUIRED   ,DB10	,DB10	,false",
        "REQUIRED	,REQUIRED   ,NOT_VISIBLE,DB10	,DB10	,false",
        "REQUIRED	,REQUIRED	,REQUIRED	,DB10	,DB10	,false",
        "NOT_VISIBLE,REQUIRED	,REQUIRED	,	    ,	    ,false",
        "NOT_VISIBLE,REQUIRED	,REQUIRED	,DB10	,DB10   ,false",
        "OPTIONAL	,OPTIONAL	,OPTIONAL	,       ,       ,true",
        "OPTIONAL	,OPTIONAL	,OPTIONAL	,DB10	,DB10	,true",
        "NOT_VISIBLE,NOT_VISIBLE,NOT_VISIBLE,       ,       ,true",
        "NOT_VISIBLE,NOT_VISIBLE,NOT_VISIBLE,DB10	,DB10	,true",
    })
    void validateAudiogrammWithout250HzAnd8000Hz(
        @NonNull RequirementSetting requirementSetting,
        @NonNull RequirementSetting requirementSetting250Hz,
        @NonNull RequirementSetting requirementSetting8000Hz,
        @Nullable Dezibel audiogrammRechts,
        @Nullable Dezibel audiogrammLinks,
        boolean expectedValid
    ) {
        // ARRANGE
        UntersuchungHoerenAudiogramm hoeren = UntersuchungHoerenAudiogrammBuilder.create(
            builder -> builder
                .withAudiogrammRechtsWithout250HzAnd8000Hz(audiogrammRechts)
                .withAudiogrammLinksWithout250HzAnd8000Hz(audiogrammLinks));
        hoeren.getUntersuchung().getEinladung().getAnlass().getAnlassConfig().setHoerenAudiogramm(requirementSetting);
        hoeren.getUntersuchung()
            .getEinladung()
            .getAnlass()
            .getAnlassConfig()
            .setHoerenAudiogramm250Hz(requirementSetting250Hz);
        hoeren.getUntersuchung()
            .getEinladung()
            .getAnlass()
            .getAnlassConfig()
            .setHoerenAudiogramm8000Hz(requirementSetting8000Hz);
        // ACT
        CheckUntersuchungHoerenAudiogrammPresentIfReqValidator
            validator = new CheckUntersuchungHoerenAudiogrammPresentIfReqValidator();
        boolean result = validator.isEntityValid(hoeren);
        // ASSERT
        Assertions.assertThat(result).isEqualTo(expectedValid);
    }

    @ParameterizedTest
    @CsvSource({
        "REQUIRED	,REQUIRED	,REQUIRED	,	    ,	    ,false",
        "REQUIRED	,NOT_VISIBLE,NOT_VISIBLE,DB10	,DB10	,true",
        "REQUIRED	,NOT_VISIBLE,REQUIRED   ,DB10	,DB10	,true",
        "REQUIRED	,REQUIRED   ,NOT_VISIBLE,DB10	,DB10	,true",
        "REQUIRED	,NOT_VISIBLE,REQUIRED   ,DB10	,    	,false",
        "REQUIRED	,REQUIRED   ,NOT_VISIBLE,DB10	,    	,false",
        "REQUIRED	,REQUIRED	,REQUIRED	,DB10	,DB10	,true",
        "NOT_VISIBLE,REQUIRED	,REQUIRED	,	    ,	    ,false",
        "NOT_VISIBLE,REQUIRED	,REQUIRED	,DB10	,DB10   ,true",
        "OPTIONAL	,OPTIONAL	,OPTIONAL	,       ,       ,true",
        "OPTIONAL	,OPTIONAL	,OPTIONAL	,DB10	,DB10	,true",
        "NOT_VISIBLE,NOT_VISIBLE,NOT_VISIBLE,       ,       ,true",
        "NOT_VISIBLE,NOT_VISIBLE,NOT_VISIBLE,DB10	,DB10	,true",
    })
    void validateAudiogrammWithAllHz(
        @NonNull RequirementSetting requirementSetting,
        @NonNull RequirementSetting requirementSetting250Hz,
        @NonNull RequirementSetting requirementSetting8000Hz,
        @Nullable Dezibel audiogrammRechts,
        @Nullable Dezibel audiogrammLinks,
        boolean expectedValid
    ) {
        // ARRANGE
        UntersuchungHoerenAudiogramm hoeren = UntersuchungHoerenAudiogrammBuilder.create(
            builder -> builder
                .withAudiogrammRechts(audiogrammRechts)
                .withAudiogrammLinks(audiogrammLinks));
        hoeren.getUntersuchung().getEinladung().getAnlass().getAnlassConfig().setHoerenAudiogramm(requirementSetting);
        hoeren.getUntersuchung()
            .getEinladung()
            .getAnlass()
            .getAnlassConfig()
            .setHoerenAudiogramm250Hz(requirementSetting250Hz);
        hoeren.getUntersuchung()
            .getEinladung()
            .getAnlass()
            .getAnlassConfig()
            .setHoerenAudiogramm8000Hz(requirementSetting8000Hz);
        // ACT
        CheckUntersuchungHoerenAudiogrammPresentIfReqValidator
            validator = new CheckUntersuchungHoerenAudiogrammPresentIfReqValidator();
        boolean result = validator.isEntityValid(hoeren);
        // ASSERT
        Assertions.assertThat(result).isEqualTo(expectedValid);
    }

    @ParameterizedTest
    @CsvSource({
        "REQUIRED	,	        ,false",
        "REQUIRED	,DB_TIEF    ,true",
        "OPTIONAL	,           ,true",
        "OPTIONAL	,DB_TIEF    ,true",
        "NOT_VISIBLE,           ,true",
        "NOT_VISIBLE,DB_TIEF    ,true",
    })
    void validateAudiogrammSimpel(
        @NonNull RequirementSetting requirementSetting,
        @Nullable AudiogrammSimpel audiogrammSimpel,
        boolean expectedValid
    ) {
        // ARRANGE
        UntersuchungHoerenAudiogramm hoeren = UntersuchungHoerenAudiogrammBuilder.create(builder -> {});
        hoeren.getUntersuchung().getEinladung().getAnlass().getAnlassConfig().setHoerenAudiogrammSimpel(requirementSetting);
        hoeren.setAudiogrammSimpel(SingleValueEnhanced.of(audiogrammSimpel, null));
        // ACT
        CheckUntersuchungHoerenAudiogrammPresentIfReqValidator
            validator = new CheckUntersuchungHoerenAudiogrammPresentIfReqValidator();
        boolean result = validator.isEntityValid(hoeren);
        // ASSERT
        Assertions.assertThat(result).isEqualTo(expectedValid);
    }

    @ParameterizedTest
    @CsvSource({
        "REQUIRED	,	    ,false",
        "REQUIRED	,JA     ,true",
        "OPTIONAL	,       ,true",
        "OPTIONAL	,JA     ,true",
        "NOT_VISIBLE,       ,true",
        "NOT_VISIBLE,JA     ,true",
    })
    void validateHoerhilfe(
        @NonNull RequirementSetting requirementSetting,
        @Nullable HoerenHoerhilfe hoerhilfe,
        boolean expectedValid
    ) {
        // ARRANGE
        UntersuchungHoerenAudiogramm hoeren = UntersuchungHoerenAudiogrammBuilder.create(builder -> {});
        hoeren.getUntersuchung().getEinladung().getAnlass().getAnlassConfig().setHoerenHoerhilfe(requirementSetting);
        hoeren.setHoerhilfe(SingleValueEnhanced.of(hoerhilfe, null));
        // ACT
        CheckUntersuchungHoerenAudiogrammPresentIfReqValidator
            validator = new CheckUntersuchungHoerenAudiogrammPresentIfReqValidator();
        boolean result = validator.isEntityValid(hoeren);
        // ASSERT
        Assertions.assertThat(result).isEqualTo(expectedValid);
    }

    @ParameterizedTest
    @CsvSource({
        "REQUIRED	,	    ,false",
        "REQUIRED	,NORMAL	,true",
        "OPTIONAL	,       ,true",
        "OPTIONAL	,NORMAL	,true",
        "NOT_VISIBLE,       ,true",
        "NOT_VISIBLE,NORMAL	,true",
    })
    void validateAudiogrammBeurteilung(
        @NonNull RequirementSetting requirementSetting,
        @Nullable AudiogrammBeurteilung beurteilung,
        boolean expectedValid
    ) {
        // ARRANGE
        List<AudiogrammBeurteilung> beurteilungList = beurteilung != null
            ? List.of(beurteilung)
            : null;
        UntersuchungHoerenAudiogramm hoeren = UntersuchungHoerenAudiogrammBuilder.create(
            builder -> builder
                .withAudiogrammBeurteilung(beurteilungList, null));
        hoeren.getUntersuchung()
            .getEinladung()
            .getAnlass()
            .getAnlassConfig()
            .setHoerenAudiogrammBeurteilung(requirementSetting);
        // ACT
        CheckUntersuchungHoerenAudiogrammPresentIfReqValidator
            validator = new CheckUntersuchungHoerenAudiogrammPresentIfReqValidator();
        boolean result = validator.isEntityValid(hoeren);
        // ASSERT
        Assertions.assertThat(result).isEqualTo(expectedValid);
    }

    @ParameterizedTest
    @CsvSource({
        "REQUIRED   ,REQUIRED  ,DB10    ,DB10   ,NORMAL         ,true",
        "REQUIRED   ,REQUIRED  ,DB10    ,DB10   ,NICHT_GEPRUEFT ,true",
        "REQUIRED   ,REQUIRED  ,DB10    ,DB10   ,               ,false",
        "REQUIRED   ,REQUIRED  ,DB10    ,       ,NORMAL         ,false",
        "REQUIRED   ,REQUIRED  ,        ,DB10   ,NORMAL         ,false",
        "REQUIRED   ,REQUIRED  ,        ,       ,NICHT_GEPRUEFT ,true",
        "REQUIRED   ,REQUIRED  ,        ,DB10   ,NICHT_GEPRUEFT ,true",
        "REQUIRED   ,REQUIRED  ,        ,       ,               ,false",
    })
    void validateAudiogrammDependantOnAudiogrammBeurteilung(
        @NonNull RequirementSetting requirementAudiogramm,
        @NonNull RequirementSetting requirementAudiogrammBeurteilung,
        @Nullable Dezibel audiogrammRechts,
        @Nullable Dezibel audiogrammLinks,
        @Nullable AudiogrammBeurteilung audiogrammBeurteilung,
        boolean expectedValid
    ) {
        // ARRANGE
        List<AudiogrammBeurteilung> beurteilungList = audiogrammBeurteilung != null
            ? List.of(audiogrammBeurteilung)
            : null;
        UntersuchungHoerenAudiogramm hoeren = UntersuchungHoerenAudiogrammBuilder.create(
            builder -> builder
                .withAudiogrammRechts(audiogrammRechts)
                .withAudiogrammLinks(audiogrammLinks)
                .withAudiogrammBeurteilung(beurteilungList, null));
        hoeren.getUntersuchung()
            .getEinladung()
            .getAnlass()
            .getAnlassConfig()
            .setHoerenAudiogramm(requirementAudiogramm);
        hoeren.getUntersuchung()
            .getEinladung()
            .getAnlass()
            .getAnlassConfig()
            .setHoerenAudiogrammBeurteilung(requirementAudiogrammBeurteilung);

        // ACT
        CheckUntersuchungHoerenAudiogrammPresentIfReqValidator
            validator = new CheckUntersuchungHoerenAudiogrammPresentIfReqValidator();
        boolean result = validator.isEntityValid(hoeren);
        // ASSERT
        Assertions.assertThat(result).isEqualTo(expectedValid);
    }

    @ParameterizedTest
    @CsvSource({
        "REQUIRED	,	        ,false",
        "REQUIRED	,ERKAELTUNG	,true",
        "OPTIONAL	,           ,true",
        "OPTIONAL	,ERKAELTUNG	,true",
        "NOT_VISIBLE,           ,true",
        "NOT_VISIBLE,ERKAELTUNG	,true",
    })
    void validateZusaetzlicheAngaben(
        @NonNull RequirementSetting requirementSetting,
        @Nullable HoerenZusaetzlicheAngaben angaben,
        boolean expectedValid
    ) {
        // ARRANGE
        List<HoerenZusaetzlicheAngaben> beurteilungList = angaben != null
            ? List.of(angaben)
            : null;
        UntersuchungHoerenAudiogramm hoeren = UntersuchungHoerenAudiogrammBuilder.create(
            builder -> builder
                .withZusaetzlicheAngaben(beurteilungList, null));
        hoeren.getUntersuchung()
            .getEinladung()
            .getAnlass()
            .getAnlassConfig()
            .setHoerenAudiogrammZusaetzlicheAngaben(requirementSetting);
        // ACT
        CheckUntersuchungHoerenAudiogrammPresentIfReqValidator
            validator = new CheckUntersuchungHoerenAudiogrammPresentIfReqValidator();
        boolean result = validator.isEntityValid(hoeren);
        // ASSERT
        Assertions.assertThat(result).isEqualTo(expectedValid);
    }

    @ParameterizedTest
    @CsvSource({
        "REQUIRED   ,                      ,false",
        "REQUIRED   ,UEBERPRUEFUNG_ARZT    ,true",
        "OPTIONAL   ,                      ,true",
        "OPTIONAL   ,UEBERPRUEFUNG_ARZT    ,true",
        "NOT_VISIBLE,                      ,true",
        "NOT_VISIBLE,UEBERPRUEFUNG_ARZT    ,true",
    })
    void validateHoerenEmpfehlung(
        @NonNull RequirementSetting requirementSetting,
        @Nullable HoerenEmpfehlung empfehlung,
        boolean expectedValid
    ) {
        // ARRANGE
        List<HoerenEmpfehlung> empfehlungList = empfehlung == null
            ? null
            : List.of(empfehlung);
        UntersuchungHoerenAudiogramm hoeren = UntersuchungHoerenAudiogrammBuilder.create(
            builder -> builder
                .withEmpfehlung(empfehlungList, null));
        hoeren.getUntersuchung().getEinladung().getAnlass().getAnlassConfig().setHoerenAudiogrammEmpfehlung(requirementSetting);
        // ACT
        CheckUntersuchungHoerenAudiogrammPresentIfReqValidator
            validator = new CheckUntersuchungHoerenAudiogrammPresentIfReqValidator();
        boolean result = validator.isEntityValid(hoeren);
        // ASSERT
        Assertions.assertThat(result).isEqualTo(expectedValid);
    }

    @ParameterizedTest
    @CsvSource({
        "REQUIRED	,	        ,false",
        "REQUIRED	,Bemerkung	,true",
        "OPTIONAL	,           ,true",
        "OPTIONAL	,Bemerkung	,true",
        "NOT_VISIBLE,           ,true",
        "NOT_VISIBLE,Bemerkung	,true",
    })
    void validateBemerkungen(
        @NonNull RequirementSetting requirementSetting,
        @Nullable String bemerkungen,
        boolean expectedValid
    ) {
        // ARRANGE
        UntersuchungHoerenAudiogramm hoeren = UntersuchungHoerenAudiogrammBuilder.create(
            builder -> builder
                .withBemerkung(bemerkungen));
        hoeren.getUntersuchung()
            .getEinladung()
            .getAnlass()
            .getAnlassConfig()
            .setHoerenAudiogrammBemerkungen(requirementSetting);
        // ACT
        CheckUntersuchungHoerenAudiogrammPresentIfReqValidator
            validator = new CheckUntersuchungHoerenAudiogrammPresentIfReqValidator();
        boolean result = validator.isEntityValid(hoeren);
        // ASSERT
        Assertions.assertThat(result).isEqualTo(expectedValid);
    }
}
