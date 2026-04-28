package ch.dvbern.oss.hemed.esc.api.shared.validator;

import ch.dvbern.oss.hemed.esc.api.features.anlass.anlassconfig.types.RequirementSetting;
import ch.dvbern.oss.hemed.esc.api.features.anlass.vorabklaerungen.UntersuchungVorabklaerungen;
import ch.dvbern.oss.hemed.esc.api.features.anlass.vorabklaerungen.types.ChronischeKrankheiten;
import ch.dvbern.oss.hemed.esc.api.features.anlass.vorabklaerungen.types.EinverstaendnisImpfungen;
import ch.dvbern.oss.hemed.esc.api.features.anlass.vorabklaerungen.types.FragebogenStatus;
import ch.dvbern.oss.hemed.esc.api.testutil.builder.UntersuchungVorabklaerungenBuilder;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import static org.assertj.core.api.Assertions.assertThat;

class CheckUntersuchungVorabklaerungenPresentIfReqValidatorTest {

    @ParameterizedTest
    @CsvSource({
        "REQUIRED   ,                               ,false",
        "REQUIRED   ,ERHALTEN_VOLLSTAENDIG          ,true",
        "OPTIONAL   ,                               ,true",
        "OPTIONAL   ,ERHALTEN_VOLLSTAENDIG          ,true",
        "NOT_VISIBLE,                               ,true",
        "NOT_VISIBLE,ERHALTEN_VOLLSTAENDIG          ,true",
    })
    void validateVorabklaerungenFragebogenStatus(
        @NonNull RequirementSetting requirementSetting,
        @Nullable FragebogenStatus input,
        boolean expectedValid
    ) {
        // ARRANGE
        UntersuchungVorabklaerungen vorabklaerungen = UntersuchungVorabklaerungenBuilder.create(
            builder -> builder
                .withFragebogenStatus(input, null));
        vorabklaerungen.getUntersuchung().getAnlass().getAnlassConfig().setVorabklaerungenFragebogenStatus(requirementSetting);
        // ACT
        CheckUntersuchungVorabklaerungenPresentIfReqValidator
            validator = new CheckUntersuchungVorabklaerungenPresentIfReqValidator();
        boolean result = validator.isEntityValid(vorabklaerungen);
        // ASSERT
        assertThat(result).isEqualTo(expectedValid);
    }

    @ParameterizedTest
    @CsvSource({
        "REQUIRED   ,                   ,false",
        "REQUIRED   ,EINVERSTANDEN      ,true",
        "OPTIONAL   ,                   ,true",
        "OPTIONAL   ,EINVERSTANDEN      ,true",
        "NOT_VISIBLE,                   ,true",
        "NOT_VISIBLE,EINVERSTANDEN      ,true",
    })
    void validateVorabklaerungenEinverstaendnisImpfungen(
        @NonNull RequirementSetting requirementSetting,
        @Nullable EinverstaendnisImpfungen input,
        boolean expectedValid
    ) {
        // ARRANGE
        UntersuchungVorabklaerungen vorabklaerungen = UntersuchungVorabklaerungenBuilder.create(
            builder -> builder
                .withEinverstaendnisImpfungen(input, null));
        vorabklaerungen.getUntersuchung().getAnlass().getAnlassConfig().setVorabklaerungenEinverstaendnisImpfungen(requirementSetting);
        // ACT
        CheckUntersuchungVorabklaerungenPresentIfReqValidator
            validator = new CheckUntersuchungVorabklaerungenPresentIfReqValidator();
        boolean result = validator.isEntityValid(vorabklaerungen);
        // ASSERT
        assertThat(result).isEqualTo(expectedValid);
    }

    @ParameterizedTest
    @CsvSource({
        "REQUIRED   ,                   ,false",
        "REQUIRED   ,VORHANDEN          ,true",
        "OPTIONAL   ,                   ,true",
        "OPTIONAL   ,VORHANDEN          ,true",
        "NOT_VISIBLE,                   ,true",
        "NOT_VISIBLE,VORHANDEN          ,true",
    })
    void validateVorabklaerungenChronischeKrankheiten(
        @NonNull RequirementSetting requirementSetting,
        @Nullable ChronischeKrankheiten input,
        boolean expectedValid
    ) {
        // ARRANGE
        UntersuchungVorabklaerungen vorabklaerungen = UntersuchungVorabklaerungenBuilder.create(
            builder -> builder
                .withChronischeKrankheiten(input, null));
        vorabklaerungen.getUntersuchung().getAnlass().getAnlassConfig().setVorabklaerungenChronischeKrankheiten(requirementSetting);
        // ACT
        CheckUntersuchungVorabklaerungenPresentIfReqValidator
            validator = new CheckUntersuchungVorabklaerungenPresentIfReqValidator();
        boolean result = validator.isEntityValid(vorabklaerungen);
        // ASSERT
        assertThat(result).isEqualTo(expectedValid);
    }
}
