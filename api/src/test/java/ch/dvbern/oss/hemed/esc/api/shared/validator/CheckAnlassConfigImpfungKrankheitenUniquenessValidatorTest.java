package ch.dvbern.oss.hemed.esc.api.shared.validator;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.EnumSet;
import java.util.stream.Collectors;

import ch.dvbern.dvbstarter.shared.types.id.ID;
import ch.dvbern.oss.hemed.esc.api.features.anlass.anlassconfig.AnlassConfig;
import ch.dvbern.oss.hemed.esc.api.features.anlass.types.AnlassTyp;
import ch.dvbern.oss.hemed.esc.api.features.impfstoff.types.Krankheit;
import ch.dvbern.oss.hemed.esc.api.testutil.builder.AnlassConfigBuilder;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.assertj.core.api.Assertions;


class CheckAnlassConfigImpfungKrankheitenUniquenessValidatorTest {

    @ParameterizedTest
    @CsvSource(value = {
        "MASERN,MUMPS,ROETELN   |POLIO,TETANUS  |true",
        "MASERN,MUMPS,ROETELN   |POLIO,MASERN   |false",
        "MASERN,MUMPS,ROETELN   |               |true",
        "                       |POLIO,TETANUS  |true",
    }, delimiter = '|')
    void validate(
        @Nullable String krankheitenLinksString,
        @Nullable String krankheitenRechtsString,
        boolean expectedValid) {
        // ARRANGE
        EnumSet<Krankheit> krankheitenLinks = EnumSet.noneOf(Krankheit.class);
        EnumSet<Krankheit> krankheitenRechts = EnumSet.noneOf(Krankheit.class);

        if (krankheitenLinksString != null) {
            krankheitenLinks = getKrankheitenEnumSetFromString(krankheitenLinksString);
        }
        if (krankheitenRechtsString != null) {
            krankheitenRechts = getKrankheitenEnumSetFromString(krankheitenRechtsString);
        }

        AnlassConfig anlassConfig = AnlassConfigBuilder.create(builder -> builder
            .withID(ID.random(AnlassConfig.class))
            .withGueltigkeitsZeitraum(LocalDate.of(1998, 1, 1), LocalDate.of(1998, 12, 31))
            .withAnlassTyp(AnlassTyp.KLASSE_4));
        anlassConfig.setUntersuchungImpfungKrankheitenLinksValues(krankheitenLinks);
        anlassConfig.setUntersuchungImpfungKrankheitenRechtsValues(krankheitenRechts);

        // ACT
        CheckAnlassConfigImpfungKrankheitenUniquenessValidator
            validatorUnderTest = new CheckAnlassConfigImpfungKrankheitenUniquenessValidator();
        var result = validatorUnderTest.isEntityValid(anlassConfig);

        // ASSERT
        Assertions.assertThat(result).isEqualTo(expectedValid);
    }

    private EnumSet<Krankheit> getKrankheitenEnumSetFromString(@NonNull String krankheitenString) {
        return Arrays.stream(krankheitenString.split(","))
            .map(String::trim)
            .map(Krankheit::valueOf)
            .collect(Collectors.collectingAndThen(Collectors.toSet(), EnumSet::copyOf));
    }
}
