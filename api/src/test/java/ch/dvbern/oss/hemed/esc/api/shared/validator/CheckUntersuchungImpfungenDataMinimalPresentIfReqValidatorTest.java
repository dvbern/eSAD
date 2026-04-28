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
import ch.dvbern.oss.hemed.esc.api.features.anlass.impfung.UntersuchungImpfungenDataMinimal;
import ch.dvbern.oss.hemed.esc.api.features.anlass.impfung.types.Immunsuppression;
import ch.dvbern.oss.hemed.esc.api.features.anlass.impfung.types.ImpfStatusMinimal;
import ch.dvbern.oss.hemed.esc.api.features.anlass.impfung.types.ImpfausweisGeprueft;
import ch.dvbern.oss.hemed.esc.api.features.anlass.impfung.types.ImpfempfehlungMinimal;
import ch.dvbern.oss.hemed.esc.api.features.anlass.impfung.types.ImpfungDurchgefuehrtMinimal;
import ch.dvbern.oss.hemed.esc.api.testutil.builder.UntersuchungImpfungenDataMinimalBuilder;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import static org.assertj.core.api.Assertions.assertThat;

class CheckUntersuchungImpfungenDataMinimalPresentIfReqValidatorTest {

    @ParameterizedTest
    @CsvSource({
        "REQUIRED   ,           ,false",
        "REQUIRED   ,JA         ,true",
        "OPTIONAL   ,           ,true",
        "OPTIONAL   ,JA         ,true",
        "NOT_VISIBLE,           ,true",
        "NOT_VISIBLE,JA         ,true",
    })
    void validateImpfausweisGeprueft(
        @NonNull RequirementSetting requirementSetting,
        @Nullable ImpfausweisGeprueft input,
        boolean expectedValid
    ) {
        // ARRANGE
        UntersuchungImpfungenDataMinimal dataMinimal = UntersuchungImpfungenDataMinimalBuilder.create(
            builder -> builder.withImpfausweisGeprueft(input, null));
        dataMinimal.getUntersuchungImpfungen().getUntersuchung().getAnlass().getAnlassConfig()
            .setImpfungenDataMinimalImpfausweisGeprueft(requirementSetting);

        // ACT
        CheckUntersuchungImpfungenDataMinimalPresentIfReqValidator validator = new CheckUntersuchungImpfungenDataMinimalPresentIfReqValidator();
        boolean result = validator.isEntityValid(dataMinimal);

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
    void validateImmunsuppression(
        @NonNull RequirementSetting requirementSetting,
        @Nullable Immunsuppression input,
        boolean expectedValid
    ) {
        // ARRANGE
        UntersuchungImpfungenDataMinimal dataMinimal = UntersuchungImpfungenDataMinimalBuilder.create(
            builder -> builder.withImmunsuppression(input, null));
        dataMinimal.getUntersuchungImpfungen().getUntersuchung().getAnlass().getAnlassConfig()
            .setImpfungenDataMinimalImmunsuppression(requirementSetting);

        // ACT
        CheckUntersuchungImpfungenDataMinimalPresentIfReqValidator validator = new CheckUntersuchungImpfungenDataMinimalPresentIfReqValidator();
        boolean result = validator.isEntityValid(dataMinimal);

        // ASSERT
        assertThat(result).isEqualTo(expectedValid);
    }

    @ParameterizedTest
    @CsvSource({
        "REQUIRED   ,           ,false",
        "REQUIRED   ,VOLLSTAENDIG,true",
        "OPTIONAL   ,           ,true",
        "OPTIONAL   ,VOLLSTAENDIG,true",
        "NOT_VISIBLE,           ,true",
        "NOT_VISIBLE,VOLLSTAENDIG,true",
    })
    void validateMasernImpfstatus(
        @NonNull RequirementSetting requirementSetting,
        @Nullable ImpfStatusMinimal input,
        boolean expectedValid
    ) {
        // ARRANGE
        UntersuchungImpfungenDataMinimal dataMinimal = UntersuchungImpfungenDataMinimalBuilder.create(
            builder -> builder.withMasernImpfstatus(input, null));
        dataMinimal.getUntersuchungImpfungen().getUntersuchung().getAnlass().getAnlassConfig()
            .setImpfungenDataMinimalMasernImpfstatus(requirementSetting);

        // ACT
        CheckUntersuchungImpfungenDataMinimalPresentIfReqValidator validator = new CheckUntersuchungImpfungenDataMinimalPresentIfReqValidator();
        boolean result = validator.isEntityValid(dataMinimal);

        // ASSERT
        assertThat(result).isEqualTo(expectedValid);
    }

    @ParameterizedTest
    @CsvSource({
        "REQUIRED   ,  ,false",
        "REQUIRED   , 2,true",
        "OPTIONAL   ,  ,true",
        "OPTIONAL   , 2,true",
        "NOT_VISIBLE,  ,true",
        "NOT_VISIBLE, 2,true",
    })
    void validateMasernAnzahlImpfungen(
        @NonNull RequirementSetting requirementSetting,
        @Nullable Integer input,
        boolean expectedValid
    ) {
        // ARRANGE
        UntersuchungImpfungenDataMinimal dataMinimal = UntersuchungImpfungenDataMinimalBuilder.create(
            builder -> builder.withMasernAnzahlImpfungen(input));
        dataMinimal.getUntersuchungImpfungen().getUntersuchung().getAnlass().getAnlassConfig()
            .setImpfungenDataMinimalMasernAnzahlImpfungen(requirementSetting);

        // ACT
        CheckUntersuchungImpfungenDataMinimalPresentIfReqValidator validator = new CheckUntersuchungImpfungenDataMinimalPresentIfReqValidator();
        boolean result = validator.isEntityValid(dataMinimal);

        // ASSERT
        assertThat(result).isEqualTo(expectedValid);
    }

    @ParameterizedTest
    @CsvSource({
        "REQUIRED   ,           ,false",
        "REQUIRED   ,VOLLSTAENDIG,true",
        "OPTIONAL   ,           ,true",
        "OPTIONAL   ,VOLLSTAENDIG,true",
        "NOT_VISIBLE,           ,true",
        "NOT_VISIBLE,VOLLSTAENDIG,true",
    })
    void validateHpvImpfstatus(
        @NonNull RequirementSetting requirementSetting,
        @Nullable ImpfStatusMinimal input,
        boolean expectedValid
    ) {
        // ARRANGE
        UntersuchungImpfungenDataMinimal dataMinimal = UntersuchungImpfungenDataMinimalBuilder.create(
            builder -> builder.withHpvImpfstatus(input, null));
        dataMinimal.getUntersuchungImpfungen().getUntersuchung().getAnlass().getAnlassConfig()
            .setImpfungenDataMinimalHpvImpfstatus(requirementSetting);

        // ACT
        CheckUntersuchungImpfungenDataMinimalPresentIfReqValidator validator = new CheckUntersuchungImpfungenDataMinimalPresentIfReqValidator();
        boolean result = validator.isEntityValid(dataMinimal);

        // ASSERT
        assertThat(result).isEqualTo(expectedValid);
    }

    @ParameterizedTest
    @CsvSource({
        "REQUIRED   ,  ,false",
        "REQUIRED   , 1,true",
        "OPTIONAL   ,  ,true",
        "OPTIONAL   , 1,true",
        "NOT_VISIBLE,  ,true",
        "NOT_VISIBLE, 1,true",
    })
    void validateHpvAnzahlImpfungen(
        @NonNull RequirementSetting requirementSetting,
        @Nullable Integer input,
        boolean expectedValid
    ) {
        // ARRANGE
        UntersuchungImpfungenDataMinimal dataMinimal = UntersuchungImpfungenDataMinimalBuilder.create(
            builder -> builder.withHpvAnzahlImpfungen(input));
        dataMinimal.getUntersuchungImpfungen().getUntersuchung().getAnlass().getAnlassConfig()
            .setImpfungenDataMinimalHpvAnzahlImpfungen(requirementSetting);

        // ACT
        CheckUntersuchungImpfungenDataMinimalPresentIfReqValidator validator = new CheckUntersuchungImpfungenDataMinimalPresentIfReqValidator();
        boolean result = validator.isEntityValid(dataMinimal);

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
    void validateImpfempfehlung(
        @NonNull RequirementSetting requirementSetting,
        @Nullable ImpfempfehlungMinimal input,
        boolean expectedValid
    ) {
        // ARRANGE
        UntersuchungImpfungenDataMinimal dataMinimal = UntersuchungImpfungenDataMinimalBuilder.create(
            builder -> builder.withImpfempfehlung(input, null));
        dataMinimal.getUntersuchungImpfungen().getUntersuchung().getAnlass().getAnlassConfig()
            .setImpfungenDataMinimalImpfempfehlung(requirementSetting);

        // ACT
        CheckUntersuchungImpfungenDataMinimalPresentIfReqValidator validator = new CheckUntersuchungImpfungenDataMinimalPresentIfReqValidator();
        boolean result = validator.isEntityValid(dataMinimal);

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
    void validateImpfungDurchgefuehrt(
        @NonNull RequirementSetting requirementSetting,
        @Nullable ImpfungDurchgefuehrtMinimal input,
        boolean expectedValid
    ) {
        // ARRANGE
        UntersuchungImpfungenDataMinimal dataMinimal = UntersuchungImpfungenDataMinimalBuilder.create(
            builder -> builder.withImpfungDurchgefuehrt(input, null));
        dataMinimal.getUntersuchungImpfungen().getUntersuchung().getAnlass().getAnlassConfig()
            .setImpfungenDataMinimalImpfungDurchgefuehrt(requirementSetting);

        // ACT
        CheckUntersuchungImpfungenDataMinimalPresentIfReqValidator validator = new CheckUntersuchungImpfungenDataMinimalPresentIfReqValidator();
        boolean result = validator.isEntityValid(dataMinimal);

        // ASSERT
        assertThat(result).isEqualTo(expectedValid);
    }
}
