package ch.dvbern.oss.hemed.esc.api.shared.validator;

import java.util.List;

import ch.dvbern.oss.hemed.esc.api.features.anlass.hoeren.types.OtoskopieResultat;
import ch.dvbern.oss.hemed.esc.api.features.anlass.hoeren.UntersuchungHoerenOtoskopie;
import ch.dvbern.oss.hemed.esc.api.features.anlass.anlassconfig.types.RequirementSetting;
import ch.dvbern.oss.hemed.esc.api.testutil.builder.UntersuchungHoerenOtoskopieBuilder;
import org.assertj.core.api.Assertions;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

class CheckUntersuchungHoerenOtoskopiePresentIfReqValidatorTest {

    @ParameterizedTest
    @CsvSource({
        "REQUIRED	    ,	        ,NORMAL ,	    ,	    ,false",
        "REQUIRED	    ,NORMAL	    ,       ,	    ,	    ,false",
        "REQUIRED	    ,	        ,       ,	    ,	    ,false",
        "REQUIRED	    ,NORMAL	    ,NORMAL ,	    ,	    ,true",
        "OPTIONAL	    ,           ,NORMAL ,       ,       ,true",
        "OPTIONAL	    ,NORMAL	    ,       ,	    ,	    ,true",
        "OPTIONAL	    ,           ,       ,       ,       ,true",
        "OPTIONAL	    ,NORMAL	    ,NORMAL ,	    ,	    ,true",
        "NOT_VISIBLE    ,           ,NORMAL ,       ,       ,true",
        "NOT_VISIBLE    ,NORMAL	    ,       ,	    ,	    ,true",
        "NOT_VISIBLE    ,           ,       ,       ,       ,true",
        "NOT_VISIBLE    ,NORMAL	    ,NORMAL ,	    ,	    ,true",
        "REQUIRED	    ,ANDERES	,ANDERES,	    ,BemL	,false",
        "REQUIRED	    ,ANDERES	,ANDERES,BemR	,	    ,false",
        "REQUIRED	    ,ANDERES	,ANDERES,	    ,	    ,false",
        "REQUIRED	    ,ANDERES	,ANDERES,BemR	,BemL	,true",
    })
    void validateOtoskopie(
        @NonNull RequirementSetting requirementSetting,
        @Nullable OtoskopieResultat audiogrammRechts,
        @Nullable OtoskopieResultat audiogrammLinks,
        @Nullable String bemerkungRechts,
        @Nullable String bemerkungLinks,
        boolean expectedValid
    ) {
        // ARRANGE
        List<OtoskopieResultat> audiogrammRechtsList = audiogrammRechts != null
            ? List.of(audiogrammRechts)
            : null;
        List<OtoskopieResultat> audiogrammLinksList = audiogrammLinks != null
            ? List.of(audiogrammLinks)
            : null;
        UntersuchungHoerenOtoskopie hoeren = UntersuchungHoerenOtoskopieBuilder.create(
            builder -> builder
                .withOtoskopieRechts(audiogrammRechtsList, null, bemerkungRechts)
                .withOtoskopieLinks(audiogrammLinksList, null, bemerkungLinks));
        hoeren.getUntersuchung().getAnlass().getAnlassConfig().setHoerenOtoskopie(requirementSetting);
        // ACT
        CheckUntersuchungHoerenOtoskopiePresentIfReqValidator validator =
            new CheckUntersuchungHoerenOtoskopiePresentIfReqValidator();
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
        UntersuchungHoerenOtoskopie hoeren = UntersuchungHoerenOtoskopieBuilder.create(
            builder -> builder
                .withBemerkung(bemerkungen));
        hoeren.getUntersuchung()
            .getAnlass()
            .getAnlassConfig()
            .setHoerenOtoskopieBemerkungen(requirementSetting);
        // ACT
        CheckUntersuchungHoerenOtoskopiePresentIfReqValidator
            validator = new CheckUntersuchungHoerenOtoskopiePresentIfReqValidator();
        boolean result = validator.isEntityValid(hoeren);
        // ASSERT
        Assertions.assertThat(result).isEqualTo(expectedValid);
    }
}
