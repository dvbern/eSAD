/*
 * Copyright © 2025 DV Bern AG, Switzerland
 *
 * Das vorliegende Dokument, einschliesslich aller seiner Teile, ist urheberrechtlich
 * geschützt. Jede Verwertung ist ohne Zustimmung der DV Bern AG unzulässig. Dies gilt
 * insbesondere für Vervielfältigungen, die Einspeicherung und Verarbeitung in
 * elektronischer Form. Wird das Dokument einem Kunden im Rahmen der Projektarbeit zur
 * Ansicht übergeben, ist jede weitere Verteilung durch den Kunden an Dritte untersagt.
 */

package ch.dvbern.oss.hemed.esc.api.shared.validator;

import ch.dvbern.oss.hemed.esc.api.features.anlass.anlassconfig.types.RequirementSetting;
import ch.dvbern.oss.hemed.esc.api.features.anlass.sprache.UntersuchungSprache;
import ch.dvbern.oss.hemed.esc.api.features.anlass.sprache.types.Nachsprechen;
import ch.dvbern.oss.hemed.esc.api.features.anlass.sprache.types.Sprachverstaendnis;
import ch.dvbern.oss.hemed.esc.api.features.anlass.sprache.types.Wortschatz;
import ch.dvbern.oss.hemed.esc.api.testutil.builder.UntersuchungSpracheBuilder;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import static org.assertj.core.api.Assertions.assertThat;

class CheckUntersuchungSprachePresentIfReqValidatorTest {

    @ParameterizedTest
    @CsvSource({
        "REQUIRED   ,           ,false",
        "REQUIRED   ,BESTANDEN  ,true",
        "OPTIONAL   ,           ,true",
        "OPTIONAL   ,BESTANDEN  ,true",
        "NOT_VISIBLE,           ,true",
        "NOT_VISIBLE,BESTANDEN  ,true",
    })
    void validateSpracheNachsprechen(
        @NonNull RequirementSetting requirementSetting,
        @Nullable Nachsprechen input,
        boolean expectedValid
    ) {
        // ARRANGE
        UntersuchungSprache sprache = UntersuchungSpracheBuilder.create(
            builder -> builder
                .withNachsprechen(input, null));
        sprache.getUntersuchung().getAnlass().getAnlassConfig().setUntersuchungSpracheNachsprechen(requirementSetting);
        // ACT
        CheckUntersuchungSprachePresentIfReqValidator
				validator = new CheckUntersuchungSprachePresentIfReqValidator();
        boolean result = validator.isEntityValid(sprache);
        // ASSERT
        assertThat(result).isEqualTo(expectedValid);
    }

    @ParameterizedTest
    @CsvSource({
        "REQUIRED   ,           ,false",
        "REQUIRED   ,BESTANDEN  ,true",
        "OPTIONAL   ,           ,true",
        "OPTIONAL   ,BESTANDEN  ,true",
        "NOT_VISIBLE,           ,true",
        "NOT_VISIBLE,BESTANDEN  ,true",
    })
    void validateSpracheWortschatz(
        @NonNull RequirementSetting requirementSetting,
        @Nullable Wortschatz input,
        boolean expectedValid
    ) {
        // ARRANGE
        UntersuchungSprache sprache = UntersuchungSpracheBuilder.create(
            builder -> builder
                .withWortschatz(input, null));
        sprache.getUntersuchung().getAnlass().getAnlassConfig().setUntersuchungSpracheWortschatz(requirementSetting);
        // ACT
        CheckUntersuchungSprachePresentIfReqValidator
            validator = new CheckUntersuchungSprachePresentIfReqValidator();
        boolean result = validator.isEntityValid(sprache);
        // ASSERT
        assertThat(result).isEqualTo(expectedValid);
    }

    @ParameterizedTest
    @CsvSource({
        "REQUIRED   ,           ,false",
        "REQUIRED   ,BESTANDEN  ,true",
        "OPTIONAL   ,           ,true",
        "OPTIONAL   ,BESTANDEN  ,true",
        "NOT_VISIBLE,           ,true",
        "NOT_VISIBLE,BESTANDEN  ,true",
    })
    void validateSpracheSprachverstaendnis(
        @NonNull RequirementSetting requirementSetting,
        @Nullable Sprachverstaendnis input,
        boolean expectedValid
    ) {
        // ARRANGE
        UntersuchungSprache sprache = UntersuchungSpracheBuilder.create(
            builder -> builder
                .withSprachverstaendnis(input, null));
        sprache.getUntersuchung().getAnlass().getAnlassConfig().setUntersuchungSpracheSprachverstaendnis(requirementSetting);
        // ACT
        CheckUntersuchungSprachePresentIfReqValidator
            validator = new CheckUntersuchungSprachePresentIfReqValidator();
        boolean result = validator.isEntityValid(sprache);
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
    void validateSehenBemerkungen(
        @NonNull RequirementSetting requirementSetting,
        @Nullable String bemerkung,
        boolean expectedValid
    ) {
        // ARRANGE
        UntersuchungSprache sprache = UntersuchungSpracheBuilder.create(
            builder -> builder
                .withBemerkung(bemerkung));
        sprache.getUntersuchung().getAnlass().getAnlassConfig().setUntersuchungSpracheBemerkungen(requirementSetting);
        // ACT
        CheckUntersuchungSprachePresentIfReqValidator
            validator = new CheckUntersuchungSprachePresentIfReqValidator();
        boolean result = validator.isEntityValid(sprache);
        // ASSERT
        assertThat(result).isEqualTo(expectedValid);
    }
}
