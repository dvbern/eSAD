package ch.dvbern.oss.hemed.esc.api.shared.timetravel.service;

import java.time.LocalDate;

import ch.dvbern.dvbstarter.infrastructure.deployment.DeploymentEnvironment;
import ch.dvbern.dvbstarter.infrastructure.errorhandling.AppFailureException;
import ch.dvbern.dvbstarter.shared.clock.AppClock;
import ch.dvbern.dvbstarter.shared.clock.ClockProvider;
import ch.dvbern.dvbstarter.shared.clock.SystemClockProvider;
import ch.dvbern.oss.hemed.esc.api.shared.config.service.ConfigService;
import ch.dvbern.oss.hemed.esc.api.shared.timetravel.TimeTravelService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import static ch.dvbern.dvbstarter.shared.i18n.datetime.DateConst.ZONE_ID;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

class TimeTravelServiceTest {

    private final ClockProvider clockProvider = new SystemClockProvider();
    private final AppClock appClock = new AppClock(clockProvider);
    private final ConfigService configService = Mockito.mock(ConfigService.class, Mockito.CALLS_REAL_METHODS);

    private final TimeTravelService timetravelService = new TimeTravelService(clockProvider, appClock, configService);

    @BeforeEach
    void setUp() {
        Mockito.reset(configService);
        when(configService.getEnvironment()).thenReturn(DeploymentEnvironment.DEV);
        when(configService.isFeatureTimeTravelEndpointEnabled()).thenReturn(true);
    }

    @Test
    void initialState() {
        LocalDate systemToday = LocalDate.now(ZONE_ID);

        // Act
        LocalDate serviceToday = timetravelService.getNow();

        // Assert
        assertThat(serviceToday).isEqualTo(systemToday);
        assertThat(timetravelService.isTimeTravelling()).isFalse();

    }

    @Test
    void timetravelBackAndReset() {
        LocalDate systemToday = LocalDate.now(ZONE_ID);
        LocalDate minus5days = systemToday.minusDays(5);

        // Arrange
        timetravelService.setNow(minus5days);

        // Act
        LocalDate serviceToday = timetravelService.getNow();

        // Assert
        assertThat(serviceToday).isEqualTo(minus5days);
        assertThat(timetravelService.isTimeTravelling()).isTrue();

        // Arrange
        timetravelService.resetNow();

        // Act
        serviceToday = timetravelService.getNow();

        // Assert
        assertThat(serviceToday).isEqualTo(systemToday);
        assertThat(timetravelService.isTimeTravelling()).isFalse();
    }

    @Test
    void timetravelFutureAndReset() {
        LocalDate systemToday = LocalDate.now(ZONE_ID);
        LocalDate plus88days = systemToday.plusDays(88);

        // Arrange
        timetravelService.setNow(plus88days);

        // Act
        LocalDate serviceToday = timetravelService.getNow();

        // Assert
        assertThat(serviceToday).isEqualTo(plus88days);
        assertThat(timetravelService.isTimeTravelling()).isTrue();

        // Arrange
        timetravelService.resetNow();

        // Act
        serviceToday = timetravelService.getNow();

        // Assert
        assertThat(serviceToday).isEqualTo(systemToday);
        assertThat(timetravelService.isTimeTravelling()).isFalse();
    }

    @Test
    void timetravelInProd_ThrowsException() {
        LocalDate systemToday = LocalDate.now(ZONE_ID);
        LocalDate minus5days = systemToday.minusDays(5);

        // Arrange
        when(configService.getEnvironment()).thenReturn(DeploymentEnvironment.PROD);

        // Act & Assert
        assertThrows(
            AppFailureException.class,
            () -> timetravelService.setNow(minus5days)
        );

    }

    @Test
    void timetravelFeatureDisabled_ThrowsException() {
        LocalDate systemToday = LocalDate.now(ZONE_ID);
        LocalDate minus5days = systemToday.minusDays(5);

        // Arrange
        when(configService.isFeatureTimeTravelEndpointEnabled()).thenReturn(false);

        // Act & Assert
        assertThrows(
            AppFailureException.class,
            () -> timetravelService.setNow(minus5days)
        );

    }

}
