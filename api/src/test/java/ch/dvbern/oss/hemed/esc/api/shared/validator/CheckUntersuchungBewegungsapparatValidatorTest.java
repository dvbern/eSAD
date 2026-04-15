package ch.dvbern.oss.hemed.esc.api.shared.validator;

import ch.dvbern.oss.hemed.esc.api.features.anlass.anlassconfig.types.RequirementSetting;
import ch.dvbern.oss.hemed.esc.api.features.anlass.bewegungsapparat.UntersuchungBewegungsapparat;
import ch.dvbern.oss.hemed.esc.api.features.anlass.bewegungsapparat.types.Haltungsschwaeche;
import ch.dvbern.oss.hemed.esc.api.features.anlass.bewegungsapparat.types.Skoliose;
import ch.dvbern.oss.hemed.esc.api.features.anlass.bewegungsapparat.types.VerkuerzungHamstringmuskulatur;
import ch.dvbern.oss.hemed.esc.api.testutil.builder.UntersuchungBewegungsapparatBuilder;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import static org.assertj.core.api.Assertions.assertThat;

class CheckUntersuchungBewegungsapparatValidatorTest {

    @ParameterizedTest
    @CsvSource({
        "REQUIRED   ,               ,false",
        "REQUIRED   ,SKOLIOSE_NEIN  ,true",
        "OPTIONAL   ,               ,true",
        "OPTIONAL   ,SKOLIOSE_NEIN  ,true",
        "NOT_VISIBLE,               ,true",
        "NOT_VISIBLE,SKOLIOSE_NEIN  ,true",
    })
    void validateSkoliose(
        @NonNull RequirementSetting requirementSetting,
        @Nullable Skoliose input,
        boolean expectedValid
    ) {
        // ARRANGE
        UntersuchungBewegungsapparat bewegungsapparat = UntersuchungBewegungsapparatBuilder.create(
            builder -> builder
                .withSkoliose(input, null));
        bewegungsapparat.getUntersuchung().getEinladung().getAnlass().getAnlassConfig().setBewegungsapparatSkoliose(requirementSetting);
        // ACT
        CheckUntersuchungBewegungsapparatValidator
            validator = new CheckUntersuchungBewegungsapparatValidator();
        boolean result = validator.isEntityValid(bewegungsapparat);
        // ASSERT
        assertThat(result).isEqualTo(expectedValid);
    }

    @ParameterizedTest
    @CsvSource({
        "REQUIRED   ,                 ,false",
        "REQUIRED   ,PATHOLOGISCH     ,true",
        "OPTIONAL   ,                 ,true",
        "OPTIONAL   ,PATHOLOGISCH     ,true",
        "NOT_VISIBLE,                 ,true",
        "NOT_VISIBLE,PATHOLOGISCH     ,true",
    })
    void validateHaltungsschwache(
        @NonNull RequirementSetting requirementSetting,
        @Nullable Haltungsschwaeche input,
        boolean expectedValid
    ) {
        // ARRANGE
        UntersuchungBewegungsapparat bewegungsapparat = UntersuchungBewegungsapparatBuilder.create(
            builder -> builder
                .withHaltungsschwache(input, null));
        bewegungsapparat.getUntersuchung().getEinladung().getAnlass().getAnlassConfig().setBewegungsapparatHaltungsschwaeche(requirementSetting);
        // ACT
        CheckUntersuchungBewegungsapparatValidator
            validator = new CheckUntersuchungBewegungsapparatValidator();
        boolean result = validator.isEntityValid(bewegungsapparat);
        // ASSERT
        assertThat(result).isEqualTo(expectedValid);
    }

    @ParameterizedTest
    @CsvSource({
        "REQUIRED   ,       ,false",
        "REQUIRED   ,NORMAL ,true",
        "OPTIONAL   ,       ,true",
        "OPTIONAL   ,NORMAL ,true",
        "NOT_VISIBLE,       ,true",
        "NOT_VISIBLE,NORMAL ,true",
    })
    void validateGrafomotrik(
        @NonNull RequirementSetting requirementSetting,
        @Nullable VerkuerzungHamstringmuskulatur input,
        boolean expectedValid
    ) {
        // ARRANGE
        UntersuchungBewegungsapparat bewegungsapparat = UntersuchungBewegungsapparatBuilder.create(
            builder -> builder
                .withVerkuerzungHamstringmuskulatur(input, null));
        bewegungsapparat.getUntersuchung().getEinladung().getAnlass().getAnlassConfig().setBewegungsapparatVerkuerzungHamstringmuskulatur(requirementSetting);
        // ACT
        CheckUntersuchungBewegungsapparatValidator
            validator = new CheckUntersuchungBewegungsapparatValidator();
        boolean result = validator.isEntityValid(bewegungsapparat);
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
    void validateBemerkung(
        @NonNull RequirementSetting requirementSetting,
        @Nullable String input,
        boolean expectedValid
    ) {
        // ARRANGE
        UntersuchungBewegungsapparat beeintraechtigung = UntersuchungBewegungsapparatBuilder.create(
            builder -> builder
                .withBemerkung(input));
        beeintraechtigung.getUntersuchung().getEinladung().getAnlass().getAnlassConfig().setBewegungsapparatBemerkung(requirementSetting);
        // ACT
        CheckUntersuchungBewegungsapparatValidator
            validator = new CheckUntersuchungBewegungsapparatValidator();
        boolean result = validator.isEntityValid(beeintraechtigung);
        // ASSERT
        assertThat(result).isEqualTo(expectedValid);
    }
}

