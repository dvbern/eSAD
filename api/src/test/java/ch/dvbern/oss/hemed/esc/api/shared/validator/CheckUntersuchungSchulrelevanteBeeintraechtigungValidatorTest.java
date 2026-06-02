package ch.dvbern.oss.hemed.esc.api.shared.validator;

import ch.dvbern.oss.hemed.esc.api.features.anlass.anlassconfig.types.RequirementSetting;
import ch.dvbern.oss.hemed.esc.api.features.anlass.schulrelevantebeeintraechtigung.UntersuchungSchulrelevanteBeeintraechtigung;
import ch.dvbern.oss.hemed.esc.api.features.anlass.schulrelevantebeeintraechtigung.types.Feinmotorik;
import ch.dvbern.oss.hemed.esc.api.features.anlass.schulrelevantebeeintraechtigung.types.FreiesGehen;
import ch.dvbern.oss.hemed.esc.api.features.anlass.schulrelevantebeeintraechtigung.types.GleichgewichtEinbeinstand;
import ch.dvbern.oss.hemed.esc.api.features.anlass.schulrelevantebeeintraechtigung.types.Grafomotorik;
import ch.dvbern.oss.hemed.esc.api.features.anlass.schulrelevantebeeintraechtigung.types.GrobmotorikEinbeinhuepfen;
import ch.dvbern.oss.hemed.esc.api.features.anlass.schulrelevantebeeintraechtigung.types.OhneStuetzradVelofahren;
import ch.dvbern.oss.hemed.esc.api.testutil.builder.UntersuchungSchulrelevanteBeeintraechtigungBuilder;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import static org.assertj.core.api.Assertions.assertThat;

class CheckUntersuchungSchulrelevanteBeeintraechtigungValidatorTest {

    @ParameterizedTest
    @CsvSource({
        // REQUIRED: mindestens eines von freiesGehen oder anzahlMonateFreiesGehen muss gesetzt sein
        "REQUIRED   ,               ,    ,false",
        "REQUIRED   ,NICHT_GEPRUEFT ,    ,true",
        "REQUIRED   ,               ,12  ,true",
        "REQUIRED   ,               ,0   ,true",
        "REQUIRED   ,NICHT_GEPRUEFT ,24  ,true",
        // OPTIONAL: immer valid
        "OPTIONAL   ,               ,    ,true",
        "OPTIONAL   ,NICHT_GEPRUEFT ,    ,true",
        "OPTIONAL   ,               ,16  ,true",
        // NOT_VISIBLE: immer valid
        "NOT_VISIBLE,               ,    ,true",
        "NOT_VISIBLE,NICHT_GEPRUEFT ,    ,true",
        "NOT_VISIBLE,               ,12  ,true",
    })
    void validateFreiesGehen(
        @NonNull RequirementSetting requirementSetting,
        @Nullable FreiesGehen freiesGehen,
        @Nullable Integer anzahlMonateFreiesGehen,
        boolean expectedValid
    ) {
        // ARRANGE
        UntersuchungSchulrelevanteBeeintraechtigung beeintraechtigung =
            UntersuchungSchulrelevanteBeeintraechtigungBuilder.create(
                builder -> builder
                    .withFreiesGehen(freiesGehen, null)
                    .withAnzahlMonateFreiesGehen(anzahlMonateFreiesGehen));
        beeintraechtigung.getUntersuchung()
            .getAnlass()
            .getAnlassConfig()
            .setUntersuchungBeeintraechtigungFreiesGehen(requirementSetting);
        // ACT
        CheckUntersuchungSchulrelevanteBeeintraechtigungPresentIfReqValidator
            validator = new CheckUntersuchungSchulrelevanteBeeintraechtigungPresentIfReqValidator();
        boolean result = validator.isEntityValid(beeintraechtigung);
        // ASSERT
        assertThat(result).isEqualTo(expectedValid);
    }

    @ParameterizedTest
    @CsvSource({
        "REQUIRED   ,                                   ,false",
        "REQUIRED   ,NEIN_OHNE_STUETZRAD_VELOFAHREN     ,true",
        "OPTIONAL   ,                                   ,true",
        "OPTIONAL   ,NEIN_OHNE_STUETZRAD_VELOFAHREN     ,true",
        "NOT_VISIBLE,                                   ,true",
        "NOT_VISIBLE,NEIN_OHNE_STUETZRAD_VELOFAHREN     ,true",
    })
    void validateOhneStuetzradVelofahren(
        @NonNull RequirementSetting requirementSetting,
        @Nullable OhneStuetzradVelofahren input,
        boolean expectedValid
    ) {
        // ARRANGE
        UntersuchungSchulrelevanteBeeintraechtigung beeintraechtigung =
            UntersuchungSchulrelevanteBeeintraechtigungBuilder.create(
                builder -> builder
                    .withOhneStuetzradVelofahren(input, null));
        beeintraechtigung.getUntersuchung()
            .getAnlass()
            .getAnlassConfig()
            .setUntersuchungBeeintraechtigungOhneStuetzradVelofahren(requirementSetting);
        // ACT
        CheckUntersuchungSchulrelevanteBeeintraechtigungPresentIfReqValidator
            validator = new CheckUntersuchungSchulrelevanteBeeintraechtigungPresentIfReqValidator();
        boolean result = validator.isEntityValid(beeintraechtigung);
        // ASSERT
        assertThat(result).isEqualTo(expectedValid);
    }

    @ParameterizedTest
    @CsvSource({
        "REQUIRED   ,       ,false",
        "REQUIRED   ,GUT    ,true",
        "OPTIONAL   ,       ,true",
        "OPTIONAL   ,GUT    ,true",
        "NOT_VISIBLE,       ,true",
        "NOT_VISIBLE,GUT    ,true",
    })
    void validateGrafomotrik(
        @NonNull RequirementSetting requirementSetting,
        @Nullable Grafomotorik input,
        boolean expectedValid
    ) {
        // ARRANGE
        UntersuchungSchulrelevanteBeeintraechtigung beeintraechtigung =
            UntersuchungSchulrelevanteBeeintraechtigungBuilder.create(
                builder -> builder
                    .withGrafomotorik(input, null));
        beeintraechtigung.getUntersuchung()
            .getAnlass()
            .getAnlassConfig()
            .setUntersuchungBeeintraechtigungGrafomotorik(requirementSetting);
        // ACT
        CheckUntersuchungSchulrelevanteBeeintraechtigungPresentIfReqValidator
            validator = new CheckUntersuchungSchulrelevanteBeeintraechtigungPresentIfReqValidator();
        boolean result = validator.isEntityValid(beeintraechtigung);
        // ASSERT
        assertThat(result).isEqualTo(expectedValid);
    }

    @ParameterizedTest
    @CsvSource({
        "REQUIRED   ,                  ,false",
        "REQUIRED   ,NICHT_BESTANDEN   ,true",
        "OPTIONAL   ,                  ,true",
        "OPTIONAL   ,NICHT_BESTANDEN   ,true",
        "NOT_VISIBLE,                  ,true",
        "NOT_VISIBLE,NICHT_BESTANDEN   ,true",
    })
    void validateFeinmotorik(
        @NonNull RequirementSetting requirementSetting,
        @Nullable Feinmotorik input,
        boolean expectedValid
    ) {
        // ARRANGE
        UntersuchungSchulrelevanteBeeintraechtigung beeintraechtigung =
            UntersuchungSchulrelevanteBeeintraechtigungBuilder.create(
                builder -> builder
                    .withFeinmotorik(input, null));
        beeintraechtigung.getUntersuchung()
            .getAnlass()
            .getAnlassConfig()
            .setUntersuchungBeeintraechtigungFeinmotorik(requirementSetting);
        // ACT
        CheckUntersuchungSchulrelevanteBeeintraechtigungPresentIfReqValidator
            validator = new CheckUntersuchungSchulrelevanteBeeintraechtigungPresentIfReqValidator();
        boolean result = validator.isEntityValid(beeintraechtigung);
        // ASSERT
        assertThat(result).isEqualTo(expectedValid);
    }

    @ParameterizedTest
    @CsvSource({
        "REQUIRED   ,            ,false",
        "REQUIRED   ,BESTANDEN   ,true",
        "OPTIONAL   ,            ,true",
        "OPTIONAL   ,BESTANDEN   ,true",
        "NOT_VISIBLE,            ,true",
        "NOT_VISIBLE,BESTANDEN   ,true",
    })
    void validateGrobmotorik(
        @NonNull RequirementSetting requirementSetting,
        @Nullable GrobmotorikEinbeinhuepfen input,
        boolean expectedValid
    ) {
        // ARRANGE
        UntersuchungSchulrelevanteBeeintraechtigung beeintraechtigung =
            UntersuchungSchulrelevanteBeeintraechtigungBuilder.create(
                builder -> builder
                    .withGrobmotorik(input, null));
        beeintraechtigung.getUntersuchung()
            .getAnlass()
            .getAnlassConfig()
            .setUntersuchungBeeintraechtigungGrobmotorik(requirementSetting);
        // ACT
        CheckUntersuchungSchulrelevanteBeeintraechtigungPresentIfReqValidator
            validator = new CheckUntersuchungSchulrelevanteBeeintraechtigungPresentIfReqValidator();
        boolean result = validator.isEntityValid(beeintraechtigung);
        // ASSERT
        assertThat(result).isEqualTo(expectedValid);
    }

    @ParameterizedTest
    @CsvSource({
        "REQUIRED   ,          ,false",
        "REQUIRED   ,BESTANDEN ,true",
        "OPTIONAL   ,          ,true",
        "OPTIONAL   ,BESTANDEN ,true",
        "NOT_VISIBLE,          ,true",
        "NOT_VISIBLE,BESTANDEN ,true",
    })
    void validateGleichgewicht(
        @NonNull RequirementSetting requirementSetting,
        @Nullable GleichgewichtEinbeinstand input,
        boolean expectedValid
    ) {
        // ARRANGE
        UntersuchungSchulrelevanteBeeintraechtigung beeintraechtigung =
            UntersuchungSchulrelevanteBeeintraechtigungBuilder.create(
                builder -> builder
                    .withGleichgewicht(input, null));
        beeintraechtigung.getUntersuchung()
            .getAnlass()
            .getAnlassConfig()
            .setUntersuchungBeeintraechtigungGleichgewicht(requirementSetting);
        // ACT
        CheckUntersuchungSchulrelevanteBeeintraechtigungPresentIfReqValidator
            validator = new CheckUntersuchungSchulrelevanteBeeintraechtigungPresentIfReqValidator();
        boolean result = validator.isEntityValid(beeintraechtigung);
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
        UntersuchungSchulrelevanteBeeintraechtigung beeintraechtigung =
            UntersuchungSchulrelevanteBeeintraechtigungBuilder.create(
                builder -> builder
                    .withBemerkungen(input));
        beeintraechtigung.getUntersuchung()
            .getAnlass()
            .getAnlassConfig()
            .setUntersuchungBeeintraechtigungBemerkung(requirementSetting);
        // ACT
        CheckUntersuchungSchulrelevanteBeeintraechtigungPresentIfReqValidator
            validator = new CheckUntersuchungSchulrelevanteBeeintraechtigungPresentIfReqValidator();
        boolean result = validator.isEntityValid(beeintraechtigung);
        // ASSERT
        assertThat(result).isEqualTo(expectedValid);
    }
}
