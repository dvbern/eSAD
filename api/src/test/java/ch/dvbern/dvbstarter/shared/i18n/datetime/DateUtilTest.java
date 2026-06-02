package ch.dvbern.dvbstarter.shared.i18n.datetime;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.OffsetDateTime;
import java.time.ZonedDateTime;

import org.junit.jupiter.api.DisplayNameGeneration;
import org.junit.jupiter.api.DisplayNameGenerator;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayNameGeneration(DisplayNameGenerator.ReplaceUnderscores.class)
@SuppressWarnings("EmptyClass")
class DateUtilTest {

    @Nested
    class Truncation {
        @Test
        void truncates_LocalDateTime_to_micros() {
            var actual = DateUtil.Truncation.truncateToMicros(
                LocalDateTime.parse("2021-01-01T01:01:01.999999999")
            );

            assertThat(actual)
                .isEqualTo(LocalDateTime.parse("2021-01-01T01:01:01.999999"));
        }

        @Test
        void truncates_OffsetDateTime_to_micros() {
            var actual = DateUtil.Truncation.truncateToMicros(
                OffsetDateTime.parse("2021-01-01T01:01:01.999999999+01:00")
            );

            assertThat(actual)
                .isEqualTo(OffsetDateTime.parse("2021-01-01T01:01:01.999999+01:00"));
        }

        @Test
        void truncatesZonedDateTime_to_micros() {
            var actual = DateUtil.Truncation.truncateToMicros(
                ZonedDateTime.parse("2021-01-01T01:01:01.999999999+01:00[Europe/Paris]")
            );

            assertThat(actual)
                .isEqualTo(ZonedDateTime.parse("2021-01-01T01:01:01.999999+01:00[Europe/Paris]"));
        }

        @Test
        void truncates_LocalTime_to_micros() {
            var actual = DateUtil.Truncation.truncateToMicros(
                LocalTime.parse("01:01:01.999999999")
            );

            assertThat(actual)
                .isEqualTo(LocalTime.parse("01:01:01.999999"));
        }
    }

    @Test
    void calculateAlter() {
        LocalDate geburtsdatum = LocalDate.of(2000, 1, 1);
        LocalDate stichtag = geburtsdatum.plusYears(5).plusMonths(6);
        assertThat(DateUtil.calculateAlter(geburtsdatum, stichtag)).isEqualByComparingTo(BigDecimal.valueOf(5.5));
        assertThat(DateUtil.calculateAlterDuodezimal(geburtsdatum, stichtag)).isEqualTo("5 ⁶/₁₂");
    }

    @ParameterizedTest
    @CsvSource({
        "0  , 0 ⁰/₁₂",
        "1  , 0 ¹/₁₂",
        "11 , 0 ¹¹/₁₂",
        "12 , 1 ⁰/₁₂",
        "13 , 1 ¹/₁₂",
        "18 , 1 ⁶/₁₂",
        "24 , 2 ⁰/₁₂",
        "66 , 5 ⁶/₁₂",
        "119, 9 ¹¹/₁₂",
        "120, 10 ⁰/₁₂",
    })
    void calculateAlterDuodezimal_fromAnzahlMonate(int anzahlMonate, String expected) {
        assertThat(DateUtil.calculateAlterDuodezimal(anzahlMonate)).isEqualTo(expected);
    }
}
