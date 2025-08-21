package ch.dvbern.dvbstarter.shared.clock;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.OffsetDateTime;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class AppClockTest {
    private final FixedClockProvider fixedClockProvider = FixedClockProvider.of(
        OffsetDateTime.parse("2021-01-01T00:00:00.999999999+00:00").toInstant()
    );

    private final AppClock appClock = new AppClock(fixedClockProvider);

    // <editor-fold desc="LocalTimeProvider" defaultstate="collapsed">
    @Nested
    class LocalTimeProvider {
        private final AppClock.LocalTimeProvider provider = appClock.localTime();

        @Test
        void now_creates_a_truncated_instance() {
            var actual = provider.now();

            assertThat(actual)
                .isEqualTo(LocalTime.parse("01:00:00.999999"));
        }

        @Test
        void nowWithNanos_creates_a_truncated_instance_with_nanos() {
            var actual = provider.nowWithNanos();

            assertThat(actual)
                .isEqualTo(LocalTime.parse("01:00:00.999999999"));
        }
    }
    // </editor-fold>

    // <editor-fold desc="LocalDateTimeProvider" defaultstate="collapsed">
    @Nested
    class LocalDateTimeProvider {
        private final AppClock.LocalDateTimeProvider provider = appClock.localDateTime();

        @Test
        void now_creates_a_truncated_instance() {
            var actual = provider.now();

            assertThat(actual)
                .isEqualTo(LocalDateTime.parse("2021-01-01T01:00:00.999999"));
        }

        @Test
        void nowWithNanos_creates_an_instance_with_nanos() {
            var actual = provider.nowWithNanos();

            assertThat(actual)
                .isEqualTo(LocalDateTime.parse("2021-01-01T01:00:00.999999999"));
        }

        @Test
        void atStartOfDay_creates_a_truncated_instance_at_midnight() {
            var actual = provider.atStartOfDay();

            assertThat(actual)
                .isEqualTo(LocalDateTime.parse("2021-01-01T00:00:00.000000"));
        }

        @Test
        void atEndOfDay_creates_a_truncated_instance_just_before_the_next_day() {
            var actual = provider.atEndOfDay();

            assertThat(actual)
                .isEqualTo(LocalDateTime.parse("2021-01-01T23:59:59.999999"));
        }
    }
    // </editor-fold>

    // <editor-fold desc="OffsetDateTimeProvider" defaultstate="collapsed">
    @Nested
    class OffsetDateTimeProvider {
        private final AppClock.OffsetDateTimeProvider provider = appClock.offsetDateTime();

        @Test
        void now_creates_a_truncated_instance() {
            var actual = provider.now();

            assertThat(actual)
                .isEqualTo(OffsetDateTime.parse("2021-01-01T00:00:00.999999+00:00"));
        }

        @Test
        void nowWithNanos_creates_an_instance_with_nanos() {
            var actual = provider.nowWithNanos();

            assertThat(actual)
                .isEqualTo(OffsetDateTime.parse("2021-01-01T00:00:00.999999999+00:00"));
        }
    }
    // </editor-fold>

    // <editor-fold desc="ZonedDateTimeProvider" defaultstate="collapsed">
    @Nested
    class ZonedDateTimeProvider {
        private final AppClock.ZonedDateTimeProvider provider = appClock.zonedDateTime();

        @Test
        void now_creates_a_truncated_instance() {
            var actual = provider.now();

            assertThat(actual)
                .isEqualTo("2021-01-01T00:00:00.999999+00:00");
        }

        @Test
        void nowWithNanos_creates_an_instance_with_nanos() {
            var actual = provider.nowWithNanos();

            assertThat(actual)
                .isEqualTo("2021-01-01T00:00:00.999999999+00:00");
        }
    }
    // </editor-fold>

}
