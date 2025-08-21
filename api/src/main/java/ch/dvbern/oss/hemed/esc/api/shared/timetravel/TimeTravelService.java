package ch.dvbern.oss.hemed.esc.api.shared.timetravel;

import ch.dvbern.dvbstarter.infrastructure.stereotypes.Service;
import ch.dvbern.dvbstarter.shared.clock.AppClock;
import ch.dvbern.dvbstarter.shared.clock.ClockProvider;
import ch.dvbern.dvbstarter.shared.i18n.datetime.DateConst;
import ch.dvbern.oss.hemed.esc.api.shared.config.service.ConfigService;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import lombok.RequiredArgsConstructor;
import org.checkerframework.checker.nullness.qual.NonNull;

@Service
@RequiredArgsConstructor
public class TimeTravelService {

    private final ClockProvider clockProvider;
    private final AppClock appClock;
    private final ConfigService configService;

    @NonNull
    public LocalDate getNow() {
        return appClock.localDateTime().now().toLocalDate();
    }

    public boolean isTimeTravelling() {
        return clockProvider.isTimeTravelling();
    }

    @NonNull
    public LocalDate setNow(@NonNull LocalDate newNow) {
        configService.validateNotProdAndTimeTravelFeatureEnabled();
        LocalDateTime realTime = appClock.localDateTime().now();
        Instant newNowInstant = newNow.atTime(realTime.toLocalTime()).atZone(DateConst.ZONE_ID).toInstant();
        clockProvider.resetUsing(newNowInstant);
        return getNow();
    }

    @NonNull
    public LocalDate resetNow() {
        clockProvider.reset();
        return getNow();
    }
}
