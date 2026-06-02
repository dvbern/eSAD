package ch.dvbern.oss.hemed.esc.api.shared.util;

import java.time.LocalDate;
import java.util.stream.Stream;

import ch.dvbern.oss.hemed.esc.api.features.kinder.Kind;
import ch.dvbern.oss.hemed.esc.api.features.kinder.json.KindSaveJson;
import ch.dvbern.oss.hemed.esc.api.shared.person.Geschlecht;
import ch.dvbern.oss.hemed.esc.api.testutil.builder.KindBuilder;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.MethodSource;

import static org.assertj.core.api.Assertions.assertThat;

class HashValueGeneratorUtilTest {

    @ParameterizedTest
    @MethodSource("provideNeedsNewHashValueData")
    void testNeedsNewHashValue(Kind kind, KindSaveJson saveJson, boolean expected) {
        // ACT
        boolean result = HashValueGeneratorUtil.needsNewHashValue(kind, saveJson);

        // ASSERT
        assertThat(result).isEqualTo(expected);
    }

    private static Stream<Arguments> provideNeedsNewHashValueData() {
        Kind baseKind = KindBuilder.defaultKind();

        return Stream.of(
            // Identical values
            Arguments.of(
                baseKind,
                KindBuilder.createJax(b -> b.forKind(baseKind), LocalDate.now()).getData(),
                false),

            // Name changed
            Arguments.of(
                baseKind,
                KindBuilder.createJax(
                    b -> b.forKind(KindBuilder.defaultKind())
                        .withName("Other", baseKind.getVorname()), LocalDate.now()).getData(),
                true),

            // Vorname changed
            Arguments.of(
                baseKind,
                KindBuilder.createJax(
                    b -> b.forKind(KindBuilder.defaultKind()).withName(baseKind.getName(), "Other"),
                    LocalDate.now()).getData(),
                true),

            // Geschlecht changed
            Arguments.of(
                baseKind, KindBuilder.createJax(
                    b -> b.forKind(KindBuilder.defaultKind()).withGeschlecht(
                        baseKind.getGeschlecht() == Geschlecht.WEIBLICH ? Geschlecht.MAENNLICH : Geschlecht.WEIBLICH),
                    LocalDate.now()).getData(), true),

            // Geburtsdatum changed
            Arguments.of(
                baseKind,
                KindBuilder.createJax(
                    b -> b.forKind(KindBuilder.defaultKind())
                        .withGeburtsdatum(baseKind.getGeburtsdatum().plusDays(1)), LocalDate.now()).getData(),
                true)
        );
    }

    @ParameterizedTest
    @CsvSource({
        "Nachname, Vorname, MAENNLICH, 2020-01-01, 4effa393dd295603506f5b3157892fce9f79ef4cc7fa496ab95a9eda125f733a",
        "nachname, vorname, MAENNLICH, 2020-01-01, 4effa393dd295603506f5b3157892fce9f79ef4cc7fa496ab95a9eda125f733a",
        // Case sensitivity (normalized to lower)
        "Nächname, Vörname, MAENNLICH, 2020-01-01, 706da46a329f8327d4bbb2c59a705a8524fd1055ed3bcb83d5194dc405c329d6",
        // Diacritics (ä -> ae)
        "Different, Name, WEIBLICH, 2021-05-15, 1884a6be3d2e041bb9327cb79827731fd04096db00655da363ebf1d2df1d40b8"
    })
    void testGenerateHashValueForKindValues(
        String nachname,
        String vorname,
        Geschlecht geschlecht,
        LocalDate geburtsdatum,
        String expectedHash) {
        // ACT
        String result =
            HashValueGeneratorUtil.generateHashValueForKindValues(nachname, vorname, geschlecht, geburtsdatum);

        // ASSERT
        assertThat(result).isEqualTo(expectedHash);
    }

    @Test
    void testGenerateHashValueStability() {
        // Ensures that the hash is stable across multiple calls
        String hash1 = HashValueGeneratorUtil.generateHashValueForKindValues(
            "Müller",
            "Hans",
            Geschlecht.MAENNLICH,
            LocalDate.of(2010, 5, 20));
        String hash2 = HashValueGeneratorUtil.generateHashValueForKindValues(
            "Müller",
            "Hans",
            Geschlecht.MAENNLICH,
            LocalDate.of(2010, 5, 20));

        assertThat(hash1).isEqualTo(hash2);
    }
}
