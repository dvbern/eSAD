package ch.dvbern.oss.hemed.esc.api.shared.validator;

import java.util.List;

import ch.dvbern.oss.hemed.esc.api.features.anlass.laeusekontrolle.types.LaeusekontrolleBeurteilung;
import ch.dvbern.oss.hemed.esc.api.features.anlass.laeusekontrolle.UntersuchungLaeusekontrolle;
import ch.dvbern.oss.hemed.esc.api.features.anlass.anlassconfig.types.RequirementSetting;
import ch.dvbern.oss.hemed.esc.api.testutil.builder.UntersuchungLaeusekontrolleBuilder;
import org.assertj.core.api.Assertions;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

class CheckUntersuchungLaeusekontrolleValidatorTest {

    @ParameterizedTest
    @CsvSource({
        "REQUIRED	    ,	                ,false",
        "REQUIRED	    ,LAEUSE_VORHANDEN	,true",
        "OPTIONAL	    ,                   ,true",
        "OPTIONAL	    ,LAEUSE_VORHANDEN	,true",
        "NOT_VISIBLE    ,                   ,true",
        "NOT_VISIBLE    ,LAEUSE_VORHANDEN	,true",
    })
    void validateLaeusekontrolleBeurteilung(
        @NonNull RequirementSetting requirementSetting,
        @Nullable LaeusekontrolleBeurteilung beurteilung,
        boolean expectedValid
    ) {
        // ARRANGE
        List<LaeusekontrolleBeurteilung> beurteilungList = beurteilung != null
            ? List.of(beurteilung)
            : null;
        UntersuchungLaeusekontrolle laeusekontrolle = UntersuchungLaeusekontrolleBuilder.create(
            builder -> builder
                .withBeurteilung(beurteilungList));
        laeusekontrolle.getUntersuchung().getAnlass().getAnlassConfig().setLaeusekontrolleBeurteilung(requirementSetting);
        // ACT
        CheckUntersuchungLaeusekontrolleValidator validator = new CheckUntersuchungLaeusekontrolleValidator();
        boolean result = validator.isEntityValid(laeusekontrolle);
        // ASSERT
        Assertions.assertThat(result).isEqualTo(expectedValid);
    }

    @ParameterizedTest
    @CsvSource({
        "REQUIRED	    ,	                ,false",
        "REQUIRED	    ,Meine Bemerkung	,true",
        "OPTIONAL	    ,                   ,true",
        "OPTIONAL	    ,Meine Bemerkung	,true",
        "NOT_VISIBLE    ,                   ,true",
        "NOT_VISIBLE    ,Meine Bemerkung	,true",
    })
    void validateLaeusekontrolleBemerkung(
        @NonNull RequirementSetting requirementSetting,
        @Nullable String bemerkung,
        boolean expectedValid
    ) {
        // ARRANGE
        UntersuchungLaeusekontrolle laeusekontrolle = UntersuchungLaeusekontrolleBuilder.create(
            builder -> builder
                .withBemerkung(bemerkung));
        laeusekontrolle.getUntersuchung().getAnlass().getAnlassConfig().setLaeusekontrolleBemerkungen(requirementSetting);
        // ACT
        CheckUntersuchungLaeusekontrolleValidator validator = new CheckUntersuchungLaeusekontrolleValidator();
        boolean result = validator.isEntityValid(laeusekontrolle);
        // ASSERT
        Assertions.assertThat(result).isEqualTo(expectedValid);
    }
}
