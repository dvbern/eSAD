package ch.dvbern.oss.hemed.esc.api.testutil;

import ch.dvbern.dvbstarter.shared.clock.AppClock;
import ch.dvbern.dvbstarter.shared.clock.FixedClockProvider;
import ch.dvbern.oss.hemed.esc.api.shared.auth.TenantEnum;
import ch.dvbern.oss.hemed.esc.api.shared.auth.TenantContext;
import io.quarkus.test.junit.QuarkusMock;
import jakarta.inject.Inject;
import java.time.OffsetDateTime;
import lombok.Getter;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.junit.jupiter.api.BeforeEach;

public abstract class AbstractITest {

    private final FixedClockProvider clockProvider = FixedClockProvider.now();

    @Getter
    private final AppClock appClock = new AppClock(clockProvider);

    @Inject
    TenantContext tenantContext;

    @BeforeEach
    public void setUp() {
        // Clock wieder zurücksetzen vom letzten Test
        resetTime();
        QuarkusMock.installMockForType(appClock, AppClock.class);
        tenantContext.setCurrentTenant(TenantEnum.BS);
    }

    public void resetTime() {
        clockProvider.resetUsing(OffsetDateTime.now().toInstant());
    }

    public void setTime(@NonNull OffsetDateTime time) {
        clockProvider.resetUsing(time.toInstant());
    }
}
