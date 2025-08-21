package ch.dvbern.dvbstarter.shared.clock;

import java.time.Clock;
import java.time.Instant;
import java.time.OffsetDateTime;

import ch.dvbern.dvbstarter.shared.i18n.datetime.DateConst;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.ToString;

@ToString
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class FixedClockProvider implements ClockProvider {

    private Clock clock;

    public static FixedClockProvider now() {
        return new FixedClockProvider(Clock.system(DateConst.ZONE_ID));
    }

    public static FixedClockProvider of(Clock clock) {
        return new FixedClockProvider(clock);
    }

    public static FixedClockProvider of(Instant instant) {
        return new FixedClockProvider(clockAtOurZone(instant));
    }

    private static Clock clockAtOurZone(Instant instant) {
        return Clock.fixed(instant, DateConst.ZONE_ID);
    }

    @Override
    public void resetUsing(Instant instant) {
        this.clock = clockAtOurZone(instant);
    }

    @Override
    public void reset() {
        this.resetUsing(OffsetDateTime.now().toInstant());
    }

    @Override
    public Clock getClock() {
        return clock;
    }

    @Override
    public boolean isTimeTravelling() {
        return OffsetDateTime.now().toInstant().equals(this.clock.instant());
    }
}
