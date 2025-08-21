package ch.dvbern.oss.hemed.esc.api.shared.validator;

import java.time.LocalDate;

import ch.dvbern.oss.hemed.esc.api.features.anlass.Anlass;
import ch.dvbern.oss.hemed.esc.api.features.anlass.AnlassConfig;
import ch.dvbern.oss.hemed.esc.api.testutil.builder.AnlassBuilder;
import ch.dvbern.oss.hemed.esc.api.testutil.builder.AnlassConfigBuilder;
import org.assertj.core.api.Assertions;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

class CheckAnlassConfigZeitraumValidatorTest {

    @ParameterizedTest
    @CsvSource({
        "2025-09-15	,2025-09-01	,2025-09-30   ,true",
        "2025-09-01	,2025-09-01	,2025-09-30   ,true",
        "2025-09-30	,2025-09-01	,2025-09-30   ,true",
        "2025-09-15	,2025-09-15	,2025-09-15   ,true",
        "2025-09-15	,2025-09-16	,2025-09-30   ,false",
        "2025-09-15	,2025-09-01	,2025-09-14   ,false",
    })
    void validate(
        @NonNull LocalDate anlassDatum,
        @NonNull LocalDate configGueltigVon,
        @NonNull LocalDate configGueltigBis,
        boolean expectedValid
    ) {
        AnlassConfig anlassConfig = AnlassConfigBuilder.create(builder -> builder
            .withGueltigkeitsZeitraum(
                configGueltigVon,
                configGueltigBis));
        Anlass anlass = AnlassBuilder.create(builder -> builder
            .withDatumSetSchuljahr(anlassDatum)
            .withAnlassConfig(anlassConfig));

        CheckAnlassConfigZeitraumValidator validatorUnderTest = new CheckAnlassConfigZeitraumValidator();
        var result = validatorUnderTest.isEntityValid(anlass);
        Assertions.assertThat(result).isEqualTo(expectedValid);
    }
}
