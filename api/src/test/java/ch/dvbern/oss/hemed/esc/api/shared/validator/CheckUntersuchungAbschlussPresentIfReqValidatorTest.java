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

import ch.dvbern.oss.hemed.esc.api.features.anlass.abschluss.UntersuchungAbschluss;
import ch.dvbern.oss.hemed.esc.api.features.anlass.abschluss.types.AbschlussResultat;
import ch.dvbern.oss.hemed.esc.api.features.anlass.anlassconfig.types.RequirementSetting;
import ch.dvbern.oss.hemed.esc.api.testutil.builder.UntersuchungAbschlussBuilder;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import static org.assertj.core.api.Assertions.assertThat;

class CheckUntersuchungAbschlussPresentIfReqValidatorTest {

    @ParameterizedTest
    @CsvSource({
        "REQUIRED   ,           ,false",
        "REQUIRED   ,JA         ,true",
        "OPTIONAL   ,           ,true",
        "OPTIONAL   ,JA         ,true",
        "NOT_VISIBLE,           ,true",
        "NOT_VISIBLE,JA         ,true",
    })
    void validateAbschlussUeberweisungErfolgt(
        @NonNull RequirementSetting requirementSetting,
        @Nullable AbschlussResultat input,
        boolean expectedValid
    ) {
        // ARRANGE
        UntersuchungAbschluss abschluss = UntersuchungAbschlussBuilder.create(
            builder -> builder
                .withUeberweisungErfolgt(input, null));
        abschluss.getUntersuchung().getAnlass().getAnlassConfig().setAbschlussUeberweisungErfolgt(requirementSetting);
        // ACT
        CheckUntersuchungAbschlussPresentIfReqValidator
            validator = new CheckUntersuchungAbschlussPresentIfReqValidator();
        boolean result = validator.isEntityValid(abschluss);
        // ASSERT
        assertThat(result).isEqualTo(expectedValid);
    }

    @ParameterizedTest
    @CsvSource({
        "REQUIRED   ,           ,false",
        "REQUIRED   ,JA         ,true",
        "OPTIONAL   ,           ,true",
        "OPTIONAL   ,JA         ,true",
        "NOT_VISIBLE,           ,true",
        "NOT_VISIBLE,JA         ,true",
    })
    void validateAbschlussAerztlicheValidierungErforderlich(
        @NonNull RequirementSetting requirementSetting,
        @Nullable AbschlussResultat input,
        boolean expectedValid
    ) {
        // ARRANGE
        UntersuchungAbschluss abschluss = UntersuchungAbschlussBuilder.create(
            builder -> builder
                .withAerztlicheValidierungErforderlich(input, null));
        abschluss.getUntersuchung().getAnlass().getAnlassConfig().setAbschlussAerztlicheValidierungErforderlich(requirementSetting);
        // ACT
        CheckUntersuchungAbschlussPresentIfReqValidator
            validator = new CheckUntersuchungAbschlussPresentIfReqValidator();
        boolean result = validator.isEntityValid(abschluss);
        // ASSERT
        assertThat(result).isEqualTo(expectedValid);
    }

    @ParameterizedTest
    @CsvSource({
        "REQUIRED   ,           ,false",
        "REQUIRED   ,JA         ,true",
        "OPTIONAL   ,           ,true",
        "OPTIONAL   ,JA         ,true",
        "NOT_VISIBLE,           ,true",
        "NOT_VISIBLE,JA         ,true",
    })
    void validateAbschlussBeratungDurchgefuehrt(
        @NonNull RequirementSetting requirementSetting,
        @Nullable AbschlussResultat input,
        boolean expectedValid
    ) {
        // ARRANGE
        UntersuchungAbschluss abschluss = UntersuchungAbschlussBuilder.create(
            builder -> builder
                .withBeratungDurchgefuehrt(input, null));
        abschluss.getUntersuchung().getAnlass().getAnlassConfig().setAbschlussBeratungDurchgefuehrt(requirementSetting);
        // ACT
        CheckUntersuchungAbschlussPresentIfReqValidator
            validator = new CheckUntersuchungAbschlussPresentIfReqValidator();
        boolean result = validator.isEntityValid(abschluss);
        // ASSERT
        assertThat(result).isEqualTo(expectedValid);
    }

    @ParameterizedTest
    @CsvSource({
        "REQUIRED   ,           ,false",
        "REQUIRED   ,Massnahmen ,true",
        "OPTIONAL   ,           ,true",
        "OPTIONAL   ,Massnahmen ,true",
        "NOT_VISIBLE,           ,true",
        "NOT_VISIBLE,Massnahmen ,true",
    })
    void validateAbschlussBeurteilungEmpfohleneMassnahmen(
        @NonNull RequirementSetting requirementSetting,
        @Nullable String massnahmen,
        boolean expectedValid
    ) {
        // ARRANGE
        UntersuchungAbschluss abschluss = UntersuchungAbschlussBuilder.create(
            builder -> builder
                .withBeurteilungEmpfohleneMassnahmen(massnahmen));
        abschluss.getUntersuchung().getAnlass().getAnlassConfig().setAbschlussBeurteilungEmpfohleneMassnahmen(requirementSetting);
        // ACT
        CheckUntersuchungAbschlussPresentIfReqValidator
            validator = new CheckUntersuchungAbschlussPresentIfReqValidator();
        boolean result = validator.isEntityValid(abschluss);
        // ASSERT
        assertThat(result).isEqualTo(expectedValid);
    }
}
