package ch.dvbern.oss.hemed.esc.api.shared.validator;

import ch.dvbern.oss.hemed.esc.api.features.anlass.impfung.UntersuchungImpfungen;
import ch.dvbern.oss.hemed.esc.api.features.anlass.anlassconfig.types.RequirementSetting;
import ch.dvbern.oss.hemed.esc.api.testutil.builder.UntersuchungImpfungenBuilder;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import static org.assertj.core.api.Assertions.assertThat;

class CheckUntersuchungImpfungenPresentIfReqValidatorTest {

    @ParameterizedTest
    @CsvSource({
        "REQUIRED   ,false  ,false",
        "REQUIRED   ,true   ,true",
        "OPTIONAL   ,false  ,true",
        "OPTIONAL   ,true   ,true",
        "NOT_VISIBLE,false  ,true",
        "NOT_VISIBLE,true   ,true",
    })
    void validateImpfungEinwilligungVorhanden(
        @NonNull RequirementSetting requirementSetting,
        boolean check,
        boolean expectedValid
    ) {
        // ARRANGE
        UntersuchungImpfungen impfungen = UntersuchungImpfungenBuilder.create(
            builder -> builder
                .withCheckEinwilligungVorhanden(check));
        impfungen.getUntersuchung().getEinladung().getAnlass().getAnlassConfig().setCheckEinwilligungVorhanden(requirementSetting);
        // ACT
        CheckUntersuchungImpfungenPresentIfReqValidator
            validatorUnderTest = new CheckUntersuchungImpfungenPresentIfReqValidator();
        var result = validatorUnderTest.isEntityValid(impfungen);
        // ASSERT
        assertThat(result).isEqualTo(expectedValid);
    }

    @ParameterizedTest
    @CsvSource({
        "REQUIRED   ,false  ,false",
        "REQUIRED   ,true   ,true",
        "OPTIONAL   ,false  ,true",
        "OPTIONAL   ,true   ,true",
        "NOT_VISIBLE,false  ,true",
        "NOT_VISIBLE,true   ,true",
    })
    void validateImpfungKontraindikationenAbgeklaert(
        @NonNull RequirementSetting requirementSetting,
        boolean check,
        boolean expectedValid
    ) {
        // ARRANGE
        UntersuchungImpfungen impfungen = UntersuchungImpfungenBuilder.create(
            builder -> builder
                .withCheckKontraindikationenAbgeklaert(check));
        impfungen.getUntersuchung().getEinladung().getAnlass().getAnlassConfig().setCheckKontraindikationenAbgeklaert(requirementSetting);
        // ACT
        CheckUntersuchungImpfungenPresentIfReqValidator
            validatorUnderTest = new CheckUntersuchungImpfungenPresentIfReqValidator();
        var result = validatorUnderTest.isEntityValid(impfungen);
        // ASSERT
        assertThat(result).isEqualTo(expectedValid);
    }

    @ParameterizedTest
    @CsvSource({
        "REQUIRED   ,false  ,false",
        "REQUIRED   ,true   ,true",
        "OPTIONAL   ,false  ,true",
        "OPTIONAL   ,true   ,true",
        "NOT_VISIBLE,false  ,true",
        "NOT_VISIBLE,true   ,true",
    })
    void validateImpfungDoppelkontrolleImpfentscheidDurchgefuehrt(
        @NonNull RequirementSetting requirementSetting,
        boolean check,
        boolean expectedValid
    ) {
        // ARRANGE
        UntersuchungImpfungen impfungen = UntersuchungImpfungenBuilder.create(
            builder -> builder
                .withCheckDoppelkontrolleImpfentscheidDurchgefuehrt(check));
        impfungen.getUntersuchung().getEinladung().getAnlass().getAnlassConfig().setCheckDoppelkontrolleImpfentscheidDurchgefuehrt(requirementSetting);
        // ACT
        CheckUntersuchungImpfungenPresentIfReqValidator
            validatorUnderTest = new CheckUntersuchungImpfungenPresentIfReqValidator();
        var result = validatorUnderTest.isEntityValid(impfungen);
        // ASSERT
        assertThat(result).isEqualTo(expectedValid);
    }

    @ParameterizedTest
    @CsvSource({
        "REQUIRED   ,           ,false",
        "REQUIRED   ,Bemerkung  ,true",
        "OPTIONAL   ,           ,true",
        "OPTIONAL   ,Bemerkung  ,true",
        "NOT_VISIBLE,           ,true",
        "NOT_VISIBLE,Bemerkung  ,true",
    })
    void validateImpfungBemerkungen(
        @NonNull RequirementSetting requirementSetting,
        @Nullable String bemerkung,
        boolean expectedValid
    ) {
        // ARRANGE
        UntersuchungImpfungen impfungen = UntersuchungImpfungenBuilder.create(
            builder -> builder
                .withBemerkungen(bemerkung));
        impfungen.getUntersuchung().getEinladung().getAnlass().getAnlassConfig().setUntersuchungImpfungBemerkungen(requirementSetting);
        // ACT
        CheckUntersuchungImpfungenPresentIfReqValidator
            validatorUnderTest = new CheckUntersuchungImpfungenPresentIfReqValidator();
        var result = validatorUnderTest.isEntityValid(impfungen);
        // ASSERT
        assertThat(result).isEqualTo(expectedValid);
    }
}
