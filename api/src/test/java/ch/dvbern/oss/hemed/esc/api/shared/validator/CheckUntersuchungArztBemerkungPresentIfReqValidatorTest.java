package ch.dvbern.oss.hemed.esc.api.shared.validator;

import ch.dvbern.oss.hemed.esc.api.features.anlass.aerztlicheuntersuchung.UntersuchungArztBemerkung;
import ch.dvbern.oss.hemed.esc.api.features.anlass.anlassconfig.types.RequirementSetting;
import ch.dvbern.oss.hemed.esc.api.testutil.builder.UntersuchungArztBemerkungBuilder;
import org.assertj.core.api.Assertions;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

class CheckUntersuchungArztBemerkungPresentIfReqValidatorTest {

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
        UntersuchungArztBemerkung entity = UntersuchungArztBemerkungBuilder.create(
            builder -> builder
                .withBemerkungen(bemerkungen));
        entity.getUntersuchung().getAnlass().getAnlassConfig().setUntersuchungBemerkungen(requirementSetting);
        // ACT
        CheckUntersuchungArztBemerkungPresentIfReqValidator validator = new CheckUntersuchungArztBemerkungPresentIfReqValidator();
        var result = validator.isEntityValid(entity);
        // ASSERT
        Assertions.assertThat(result).isEqualTo(expectedValid);
    }
}
