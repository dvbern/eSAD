package ch.dvbern.oss.hemed.esc.api.shared.validator;

import ch.dvbern.oss.hemed.esc.api.features.anlass.impfung.UntersuchungImpfungen;
import ch.dvbern.oss.hemed.esc.api.features.anlass.massnahmen.UntersuchungImpfungenMassnahmen;
import ch.dvbern.oss.hemed.esc.api.features.anlass.anlassconfig.types.RequirementSetting;
import ch.dvbern.oss.hemed.esc.api.testutil.builder.UntersuchungImpfungenBuilder;
import ch.dvbern.oss.hemed.esc.api.testutil.builder.UntersuchungImpfungenMassnahmenBuilder;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import static org.assertj.core.api.Assertions.assertThat;

class CheckUntersuchungImpfungenMassnahmenPresentIfReqValidatorTest {

    @ParameterizedTest
    @CsvSource({
        "REQUIRED   ,           ,false",
        "REQUIRED   ,Allergie   ,true",
        "OPTIONAL   ,           ,true",
        "OPTIONAL   ,Allergie   ,true",
        "NOT_VISIBLE,           ,true",
        "NOT_VISIBLE,Allergie  ,true",
    })
    void validateImpfungAchtung(
        @NonNull RequirementSetting requirementSetting,
        @Nullable String achtung,
        boolean expectedValid
    ) {
        // ARRANGE
        UntersuchungImpfungen impfungen = UntersuchungImpfungenBuilder.create(
            builder -> builder
                .withCheckEinwilligungVorhanden(true));
        UntersuchungImpfungenMassnahmen massnahmen = UntersuchungImpfungenMassnahmenBuilder.create(
            builder -> builder
                .forUntersuchungImpfungen(impfungen)
                .withAchtung(achtung));
        massnahmen.setVersion(1L); // Weil wir erst beim zweiten Speichern validieren
        massnahmen.getUntersuchungImpfungen().getUntersuchung().getAnlass().getAnlassConfig().setUntersuchungImpfungAchtung(requirementSetting);
        // ACT
        CheckUntersuchungImpfungenMassnahmenPresentIfReqValidator
            validatorUnderTest = new CheckUntersuchungImpfungenMassnahmenPresentIfReqValidator();
        var result = validatorUnderTest.isEntityValid(massnahmen);
        // ASSERT
        assertThat(result).isEqualTo(expectedValid);
    }
}
