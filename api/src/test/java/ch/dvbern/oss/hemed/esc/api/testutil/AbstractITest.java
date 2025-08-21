package ch.dvbern.oss.hemed.esc.api.testutil;

import java.time.OffsetDateTime;

import ch.dvbern.dvbstarter.shared.clock.AppClock;
import ch.dvbern.dvbstarter.shared.clock.FixedClockProvider;
import io.quarkus.test.junit.QuarkusMock;
import lombok.Getter;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.junit.jupiter.api.BeforeEach;

public abstract class AbstractITest {

    private final FixedClockProvider clockProvider = FixedClockProvider.now();

    @Getter
    private final AppClock appClock = new AppClock(clockProvider);


    @BeforeEach
    public void setUp() {
        // Clock wieder zurücksetzen vom letzten Test
        resetTime();
        QuarkusMock.installMockForType(appClock, AppClock.class);
    }

    public void resetTime() {
        clockProvider.resetUsing(OffsetDateTime.now().toInstant());
    }

    public void setTime(@NonNull OffsetDateTime time) {
        clockProvider.resetUsing(time.toInstant());
    }
}
