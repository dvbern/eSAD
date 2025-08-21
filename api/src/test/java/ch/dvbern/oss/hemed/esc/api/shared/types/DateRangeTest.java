package ch.dvbern.oss.hemed.esc.api.shared.types;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import ch.dvbern.dvbstarter.shared.clock.AppClock;
import ch.dvbern.dvbstarter.shared.clock.FixedClockProvider;
import ch.dvbern.dvbstarter.shared.i18n.datetime.DateConst;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import static java.util.Collections.singletonList;
import static org.assertj.core.api.Assertions.assertThat;

class DateRangeTest {
    private final FixedClockProvider clockProvider = FixedClockProvider.now();
    private final AppClock appClock = new AppClock(clockProvider);

    private final DateRange year2015 = DateRange.of(
        LocalDate.of(2015, 1, 1),
        LocalDate.of(2015, 12, 31)
    );

    @Nested
    class HasValidRangeTest {
        @ParameterizedTest
        @CsvSource({
            "2000-01-01, 2000-01-01",
            "2000-01-01, 2000-05-12",
            "2000-01-01, 2000-12-31",
            "2000-12-31, 2000-12-31",
        })
        void shouldValidateOnValidInput(LocalDate start, LocalDate end) {
            DateRange dateRange = DateRange.of(start, end);
            boolean actual = dateRange.hasValidRange();
            assertThat(actual)
                .isTrue();
        }

        @ParameterizedTest
        @CsvSource({
            "2000-01-01, 1999-12-31",
            "2000-12-31, 2000-01-01",
        })
        void shouldFailOnInvalidInput(LocalDate start, LocalDate end) {
            DateRange dateRange = DateRange.of(start, end);
            boolean actual = dateRange.hasValidRange();
            assertThat(actual)
                .isFalse();
        }
    }

    @Nested
    class ContainsTest {
        @Test
        void testContains_shouldBeFalseWhenNotInRange() {
            assertThat(year2015.contains(LocalDate.of(2014, 12, 31))).isFalse();
            assertThat(year2015.contains(LocalDate.of(2016, 1, 1))).isFalse();
        }

        @Test
        void testContains_shouldBeTrueWhenInRange() {
            assertThat(year2015.contains(LocalDate.of(2015, 1, 1))).isTrue();
            assertThat(year2015.contains(LocalDate.of(2015, 12, 31))).isTrue();
            assertThat(year2015.contains(LocalDate.of(2015, 2, 10))).isTrue();
        }

        @Test
        void testContains_shouldBeFalseWhenOutsideRange() {
            assertThat(year2015.contains(DateRange.of(LocalDate.of(2016, 1, 1), LocalDate.of(2016, 12, 31)))).isFalse();
        }

        @Test
        void testContains_shouldBeFalseWhenPartiallyOutsideRange() {
            assertThat(year2015.contains(DateRange.of(LocalDate.of(2015, 1, 1), LocalDate.of(2016, 12, 31)))).isFalse();
        }

        @Test
        void testContains_shouldBeTrueWhenEqualRange() {
            assertThat(year2015.contains(DateRange.of(year2015))).isTrue();
        }

        @Test
        void testContains_shouldBeTrueWhenInnerRange() {
            assertThat(year2015.contains(DateRange.of(LocalDate.of(2015, 1, 1), LocalDate.of(2015, 6, 3)))).isTrue();
        }
    }

    @Nested
    class EqualsTest {
        @Test
        void testEquals() {
            assertThat(DateRange.of(year2015)).isEqualTo(year2015);
            assertThat(DateRange.of(LocalDate.of(2015, 1, 2), LocalDate.of(2015, 12, 31))).isNotEqualTo(year2015);
            assertThat(DateRange.of(LocalDate.of(2015, 1, 1), LocalDate.of(2015, 12, 30))).isNotEqualTo(year2015);
            assertThat(DateRange.of(LocalDate.of(2016, 1, 1), LocalDate.of(2016, 12, 31))).isNotEqualTo(year2015);
        }
    }

    @Nested
    class OverlapTest {
        @Test
        void testGetOverlap_shouldBeEqualForIdenticalRanges() {
            assertThat(year2015.getOverlap(DateRange.of(year2015)))
                .hasValue(year2015);
        }

        @Test
        void testGetOverlap_shouldBeTheSubRange() {
            DateRange subRange1 = DateRange.of(year2015.getDatumVon(), year2015.getDatumBis().minusDays(1));
            assertThat(year2015.getOverlap(subRange1))
                .hasValue(subRange1);
            assertThat(subRange1.getOverlap(year2015))
                .hasValue(subRange1);

            DateRange subRange2 = DateRange.of(year2015.getDatumVon().plusDays(1), year2015.getDatumBis());
            assertThat(year2015.getOverlap(subRange2))
                .hasValue(subRange2);
            assertThat(subRange2.getOverlap(year2015))
                .hasValue(subRange2);

            DateRange subRange3 = DateRange.of(
                year2015.getDatumVon().plusDays(1),
                year2015.getDatumBis().minusDays(1));
            assertThat(year2015.getOverlap(subRange3))
                .hasValue(subRange3);
            assertThat(subRange3.getOverlap(year2015))
                .hasValue(subRange3);
        }

        @Test
        void testGetOverlap_schnittpunktIstStartOderEnde() {
            LocalDate schnittpunkt = LocalDate.of(1976, 11, 19);
            DateRange schnittpunktRange = DateRange.of(schnittpunkt, schnittpunkt);

            DateRange a = DateRange.of(schnittpunkt.minusYears(1), schnittpunkt);
            DateRange b = DateRange.of(schnittpunkt, schnittpunkt.plusYears(1));

            assertThat(a.getOverlap(b))
                .hasValue(schnittpunktRange);

            // nochmal andersrum
            assertThat(b.getOverlap(a))
                .hasValue(schnittpunktRange);
        }

        @Test
        void testGetOverlap_shouldBeEmptyWhenNoOverlap() {
            assertThat(year2015.getOverlap(DateRange.of(
                LocalDate.of(2014, 1, 1),
                LocalDate.of(2014, 12, 31)))).isNotPresent();
        }

        @Test
        void testGetOverlap_stichtag() {
            LocalDate stichtag = year2015.getDatumVon();
            DateRange expected = DateRange.of(stichtag, stichtag);

            assertThat(year2015.getOverlap(DateRange.of(stichtag, stichtag)))
                .hasValue(expected);
        }
    }

    @Nested
    class GetDaysTest {
        @Test
        void testGetDays_shouldCountDaysFromGueltigAbToGueltigBis() {
            LocalDate date = LocalDate.of(2014, 1, 1);
            DateRange singleDay = DateRange.of(date, date);
            assertThat(singleDay.getDays()).isEqualTo(1);

            DateRange range = DateRange.of(date, date.plusDays(3));
            assertThat(range.getDays()).isEqualTo(4);
        }
    }

    @Nested
    class EndsDayBeforeTest {
        @Test
        void testEndsDayBefore_localdate() {
            DateRange a = year2015;
            LocalDate adjacent = year2015.getDatumVon().plusYears(1);
            LocalDate before = adjacent.minusDays(1);
            LocalDate same = year2015.getDatumVon();
            LocalDate copy = year2015.getDatumVon().plusDays(0); // copy
            LocalDate after = adjacent.plusDays(1);

            assertThat(a.endsDayBefore(adjacent)).isTrue();
            assertThat(a.endsDayBefore(before)).isFalse();
            assertThat(a.endsDayBefore(same)).isFalse();
            assertThat(a.endsDayBefore(copy)).isFalse();
            assertThat(a.endsDayBefore(after)).isFalse();
        }

        @Test
        void testEndsDayBefore_daterange() {
            DateRange a = year2015;
            DateRange adjacent = DateRange.of(LocalDate.of(2016, 1, 1), LocalDate.of(2016, 12, 31));
            DateRange before = DateRange.of(LocalDate.of(2012, 1, 1), LocalDate.of(2012, 12, 31));
            DateRange intersectBefore = DateRange.of(LocalDate.of(2012, 1, 1), LocalDate.of(2015, 10, 10));
            DateRange intersectAfter = DateRange.of(LocalDate.of(2015, 3, 3), LocalDate.of(2016, 12, 31));
            DateRange after = DateRange.of(LocalDate.of(2017, 1, 1), LocalDate.of(2017, 12, 31));

            assertThat(a.endsDayBefore(adjacent)).isTrue();
            assertThat(a.endsDayBefore(a)).isFalse(); // same ref
            assertThat(a.endsDayBefore(before)).isFalse();
            assertThat(a.endsDayBefore(intersectBefore)).isFalse();
            assertThat(a.endsDayBefore(intersectAfter)).isFalse();
            assertThat(a.endsDayBefore(after)).isFalse();
        }
    }

    @Nested
    class StichtagTest {
        @Test
        void testStichtag() {
            LocalDate d = appClock.offsetDateTime().now().toLocalDate();
            LocalDate other = d.plusDays(1);
            assertThat(DateRange.of(d).isStichtag()).isTrue();
            assertThat(DateRange.of(d, other).isStichtag()).isFalse();
        }
    }

    @Nested
    class WithFullWeeksTest {
        @Test
        void testWithFullWeeks_stichtag() {
            LocalDate montag = LocalDate.of(2015, 10, 12); // Montag
            assertThat(montag.getDayOfWeek()).isEqualTo(DayOfWeek.MONDAY); // nur zur Sicherheit :)
            LocalDate mittwoch = LocalDate.of(2015, 10, 14); // Mittwoch
            assertThat(mittwoch.getDayOfWeek()).isEqualTo(DayOfWeek.WEDNESDAY); // nur zur Sicherheit :)
            LocalDate sonntag = LocalDate.of(2015, 10, 18); // Sonntag
            assertThat(sonntag.getDayOfWeek()).isEqualTo(DayOfWeek.SUNDAY); // nur zur Sicherheit :)

            // Stichtag
            DateRange stichtag = DateRange.of(mittwoch);
            assertThat(stichtag.withFullWeeks().getDatumVon()).isEqualTo(montag);
            assertThat(stichtag.withFullWeeks().getDatumBis()).isEqualTo(sonntag);
        }

        @Test
        void testWithFullWeeks_range() {
            LocalDate montag = LocalDate.of(2015, 10, 12); // Montag
            assertThat(montag.getDayOfWeek()).isEqualTo(DayOfWeek.MONDAY); // nur zur Sicherheit :)
            LocalDate mittwoch = LocalDate.of(2015, 10, 14); // Mittwoch
            assertThat(mittwoch.getDayOfWeek()).isEqualTo(DayOfWeek.WEDNESDAY); // nur zur Sicherheit :)
            LocalDate donnerstag = LocalDate.of(2015, 10, 22); // Donnerstag, eine Woche spaeter
            assertThat(donnerstag.getDayOfWeek()).isEqualTo(DayOfWeek.THURSDAY); // nur zur Sicherheit :)
            LocalDate sonntag = LocalDate.of(2015, 10, 25); // Sonntag, eine Woche spaeter
            assertThat(sonntag.getDayOfWeek()).isEqualTo(DayOfWeek.SUNDAY); // nur zur Sicherheit :)

            // Stichtag
            DateRange stichtag = DateRange.of(mittwoch, donnerstag);
            assertThat(stichtag.withFullWeeks().getDatumVon()).isEqualTo(montag);
            assertThat(stichtag.withFullWeeks().getDatumBis()).isEqualTo(sonntag);
        }
    }

    @Nested
    class WithFullMonthsTest {
        @Test
        void testWithFullMonths() {
            LocalDate stichtag = LocalDate.of(2015, 5, 5);
            DateRange oneMonth = DateRange.of(stichtag).withFullMonths();
            assertThat(oneMonth.getDatumVon()).isEqualTo(LocalDate.of(2015, 5, 1));
            assertThat(oneMonth.getDatumBis()).isEqualTo(LocalDate.of(2015, 5, 31));

            LocalDate stichtagSchaltjahr = LocalDate.of(2016, 2, 5);
            DateRange oneMonthSchaltjahr = DateRange.of(stichtagSchaltjahr).withFullMonths();
            assertThat(oneMonthSchaltjahr.getDatumVon()).isEqualTo(LocalDate.of(2016, 2, 1));
            assertThat(oneMonthSchaltjahr.getDatumBis()).isEqualTo(LocalDate.of(2016, 2, 29));

            DateRange range = DateRange.of(LocalDate.of(2014, 5, 5), LocalDate.of(2015, 5, 5));
            DateRange multiYear = range.withFullMonths();
            assertThat(multiYear).isEqualTo(DateRange.of(LocalDate.of(2014, 5, 1), LocalDate.of(2015, 5, 31)));
        }
    }

    @Nested
    class WithFullYearsTest {
        @Test
        void testWithFullYears() {
            LocalDate stichtag = LocalDate.of(2015, 5, 5);
            DateRange oneYear = DateRange.of(stichtag).withFullYears();
            assertThat(oneYear.getDatumVon()).isEqualTo(LocalDate.of(2015, 1, 1));
            assertThat(oneYear.getDatumBis()).isEqualTo(LocalDate.of(2015, 12, 31));

            DateRange range = DateRange.of(LocalDate.of(2014, 5, 5), LocalDate.of(2015, 5, 5));
            DateRange multiYear = range.withFullYears();
            assertThat(multiYear).isEqualTo(DateRange.of(LocalDate.of(2014, 1, 1), LocalDate.of(2015, 12, 31)));
        }
    }

    @Nested
    class CompareToTest {
        private int sign(int value) {
            //noinspection NumericCastThatLosesPrecision
            return (int) Math.signum(value);
        }

        @Test
        void testCompareTo() {
            DateRange ref = DateRange.of(LocalDate.of(2016, 1, 1), LocalDate.of(2016, 12, 31));
            DateRange refSame = DateRange.of(LocalDate.of(2016, 1, 1), LocalDate.of(2016, 12, 31));
            DateRange later = DateRange.of(LocalDate.of(9999, 1, 1), LocalDate.of(9999, 12, 31));
            DateRange laterLonger = DateRange.of(LocalDate.of(2016, 1, 1), LocalDate.of(9999, 12, 31));
            DateRange before = DateRange.of(LocalDate.of(1000, 1, 1), LocalDate.of(1000, 12, 31));
            DateRange beforeLonger = DateRange.of(LocalDate.of(1000, 1, 1), LocalDate.of(2016, 12, 31));

            assertThat(ref)
                .isEqualByComparingTo(ref)
                .isEqualByComparingTo(refSame);
            assertThat(sign(ref.compareTo(later))).isEqualTo(-1);
            assertThat(sign(ref.compareTo(laterLonger))).isEqualTo(-1);
            assertThat(sign(ref.compareTo(before))).isEqualTo(1);
            assertThat(sign(ref.compareTo(beforeLonger))).isEqualTo(1);
        }
    }

    @Nested
    class StreamingTest {
        @Test
        void testStreamDays_shouldStreamDaysInclusive() {
            LocalDate gueltigAb = LocalDate.of(2018, 1, 1);
            LocalDate gueltigBis = LocalDate.of(2018, 1, 2);
            DateRange dateRange = DateRange.of(gueltigAb, gueltigBis);

            List<LocalDate> dates = dateRange.streamDays().toList();

            assertThat(dates).hasSize(2);
            assertThat(dates.get(0)).isEqualTo(gueltigAb);
            assertThat(dates.get(1)).isEqualTo(gueltigBis);
        }

        @Test
        void testStreamDays_shouldStreamSingleDay() {
            LocalDate gueltigAb = LocalDate.of(2018, 1, 1);
            LocalDate gueltigBis = LocalDate.of(2018, 1, 1);
            DateRange dateRange = DateRange.of(gueltigAb, gueltigBis);

            List<LocalDate> dates = dateRange.streamDays().toList();

            assertThat(dates).hasSize(1);
            assertThat(dates.get(0)).isEqualTo(gueltigAb);
        }

        @Test
        void testStreamDays_shouldStreamEmpty() {
            LocalDate gueltigAb = LocalDate.of(2018, 1, 2);
            LocalDate gueltigBis = LocalDate.of(2018, 1, 1);
            DateRange dateRange = DateRange.of(gueltigAb, gueltigBis);

            List<LocalDate> dates = dateRange.streamDays().toList();

            assertThat(dates).isEmpty();
        }
    }

    @Nested
    class ExceptTest {
        @Test
        void testExcept() {
            DateRange t1r1 = DateRange.of(LocalDate.of(2016, 8, 11), LocalDate.of(2016, 8, 14));
            DateRange t1r2 = DateRange.of(LocalDate.of(2016, 8, 12), LocalDate.of(2016, 8, 13));

            assertThat(t1r1.except(t1r2)).isEqualTo(Arrays.asList(
                DateRange.of(LocalDate.of(2016, 8, 11), LocalDate.of(2016, 8, 11)),
                DateRange.of(LocalDate.of(2016, 8, 14), LocalDate.of(2016, 8, 14))
            ));

            DateRange t2r1 = DateRange.of(LocalDate.of(2016, 8, 11), LocalDate.of(2016, 8, 14));
            DateRange t2r2 = DateRange.of(LocalDate.of(2016, 8, 12), LocalDate.of(2016, 8, 15));

            assertThat(t2r1.except(t2r2)).isEqualTo(singletonList(
                DateRange.of(LocalDate.of(2016, 8, 11), LocalDate.of(2016, 8, 11))
            ));

            DateRange t3r1 = DateRange.of(LocalDate.of(2016, 8, 11), LocalDate.of(2016, 8, 14));
            DateRange t3r2 = DateRange.of(LocalDate.of(2016, 8, 15), LocalDate.of(2016, 8, 15));

            assertThat(t3r1.except(t3r2)).isEqualTo(new ArrayList<DateRange>());

            DateRange t4r1 = DateRange.of(LocalDate.of(2016, 8, 1), DateConst.END_OF_TIME);
            DateRange t4r2 = DateRange.of(LocalDate.of(2016, 8, 1), LocalDate.of(2016, 8, 31));

            assertThat(t4r1.except(t4r2)).isEqualTo(singletonList(
                DateRange.of(LocalDate.of(2016, 9, 1), DateConst.END_OF_TIME)
            ));

            DateRange t5r1 = DateRange.of(LocalDate.of(2016, 8, 11), LocalDate.of(2016, 8, 14));
            DateRange t5r2 = DateRange.of(LocalDate.of(2016, 8, 9), LocalDate.of(2016, 8, 20));
            assertThat(t5r1.except(t5r2)).isEqualTo(new ArrayList<DateRange>());
        }
    }

    @ParameterizedTest
    @CsvSource({
        "2000-01-01, 2000-01-01, 2000-01-31",
        "2000-01-31, 2000-01-01, 2000-01-31",
        "2000-01-15, 2000-01-01, 2000-01-31",
        "2024-02-29, 2024-02-01, 2024-02-29",
    })
    void ofMonthContaining(
        LocalDate stichtag,
        LocalDate expectedVon,
        LocalDate expectedBis
    ) {
        final DateRange dateRange = DateRange.ofMonthContaining(stichtag);
        assertThat(dateRange).isNotNull();
        assertThat(dateRange.getDatumVon()).isEqualTo(expectedVon);
        assertThat(dateRange.getDatumBis()).isEqualTo(expectedBis);
    }

    @ParameterizedTest
    @CsvSource({
        "2000-01-01, 2000-01-01, 2000-12-31",
        "2000-12-31, 2000-01-01, 2000-12-31",
        "2000-05-06, 2000-01-01, 2000-12-31",
        "2024-02-29, 2024-01-01, 2024-12-31",
    })
    void ofYearContaining(
        LocalDate stichtag,
        LocalDate expectedVon,
        LocalDate expectedBis
    ) {
        final DateRange dateRange = DateRange.ofYearContaining(stichtag);
        assertThat(dateRange).isNotNull();
        assertThat(dateRange.getDatumVon()).isEqualTo(expectedVon);
        assertThat(dateRange.getDatumBis()).isEqualTo(expectedBis);
    }
}
