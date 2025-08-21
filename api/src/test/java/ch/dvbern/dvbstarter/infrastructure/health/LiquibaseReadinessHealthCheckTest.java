package ch.dvbern.dvbstarter.infrastructure.health;

import ch.dvbern.dvbstarter.infrastructure.liquibase.LiquibaseRaceConditionWorkaround;
import org.eclipse.microprofile.health.HealthCheckResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import static org.assertj.core.api.Assertions.assertThat;

class LiquibaseReadinessHealthCheckTest {

    private LiquibaseReadinessHealthCheck sut;
    private LiquibaseRaceConditionWorkaround mock;

    @BeforeEach
    void beforeEach() {
        mock = Mockito.mock(LiquibaseRaceConditionWorkaround.class);
        sut = new LiquibaseReadinessHealthCheck(mock);
    }

    @Nested
    class if_everything_is_ok {
        @BeforeEach
        void beforeEach() {
            Mockito.doReturn(true)
                .when(mock)
                .isInitialized();
        }

        @Test
        void it_returns_status_UP() {
            var actual = sut.call();

            assertThat(actual.getName())
                .isEqualTo("Liquibase");
            assertThat(actual.getStatus())
                .isEqualTo(HealthCheckResponse.Status.UP);
        }
    }

    @Nested
    class if_liquibase_is_not_initialized {
        @BeforeEach
        void beforeEach() {
            Mockito.doReturn(false)
                .when(mock)
                .isInitialized();
        }

        @Test
        void it_returns_status_DOWN() {
            var actual = sut.call();

            assertThat(actual.getName())
                .isEqualTo("Liquibase");
            assertThat(actual.getStatus())
                .isEqualTo(HealthCheckResponse.Status.DOWN);
            assertThat(actual.getData().get())
                .containsEntry("initialized", false);
        }
    }

}
