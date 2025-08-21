package ch.dvbern.dvbstarter.shared.clock;

import java.time.LocalDate;

import ch.dvbern.dvbstarter.shared.i18n.datetime.DateConst;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import static ch.dvbern.dvbstarter.shared.i18n.datetime.DateConst.ZONE_ID;
import static org.assertj.core.api.Assertions.assertThat;

class TimeTravelTest {
    private final SystemClockProvider clockProvider = new SystemClockProvider();

    private final AppClock appClock = new AppClock(clockProvider);

    @Test
    void testTimeTravel() {
        var today = LocalDate.now(ZONE_ID).atStartOfDay();
        var minus5days = today.minusDays(5);
        var minus5daysInstant = minus5days.atZone(DateConst.ZONE_ID).toInstant();
        var plus7days = today.plusDays(7);
        var plus7daysInstant = plus7days.atZone(DateConst.ZONE_ID).toInstant();

        clockProvider.reset();
        assertThat(clockProvider.isTimeTravelling()).isFalse();
        assertThat(appClock.localDateTime().atStartOfDay()).isEqualTo(today);

        clockProvider.resetUsing(minus5daysInstant);
        assertThat(clockProvider.isTimeTravelling()).isTrue();
        assertThat(appClock.localDateTime().atStartOfDay()).isEqualTo(minus5days);

        clockProvider.resetUsing(plus7daysInstant);
        assertThat(clockProvider.isTimeTravelling()).isTrue();
        assertThat(appClock.localDateTime().atStartOfDay()).isEqualTo(plus7days);

    }


    @AfterEach
    void tearDown() {
        clockProvider.reset();
    }

}
