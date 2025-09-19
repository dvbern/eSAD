package ch.dvbern.dvbstarter.infrastructure.health;

import java.util.Optional;

import ch.dvbern.dvbstarter.infrastructure.db.Db;
import ch.dvbern.oss.hemed.esc.api.features.benutzer.BenutzerFixtureBuilder;
import ch.dvbern.oss.hemed.esc.api.shared.auth.TenantProducer;
import ch.dvbern.oss.hemed.esc.api.shared.benutzer.Benutzer;
import org.eclipse.microprofile.health.HealthCheckResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import static ch.dvbern.oss.hemed.esc.api.shared.benutzer.Benutzer.SYSTEM_ADMIN_BS_ID;
import static org.assertj.core.api.Assertions.assertThat;

class DatabaseLivenessHealthCheckTest {

    private final Db mock = Mockito.mock(Db.class);
    private final TenantProducer tenantProducerMock = Mockito.mock(TenantProducer.class);
    private final DatabaseLivenessHealthCheck sut = new DatabaseLivenessHealthCheck(mock, tenantProducerMock);

    @Nested
    class if_an_admin_user_is_present {
        @BeforeEach
        void beforeEach() {
            Benutzer admin = BenutzerFixtureBuilder.validBenutzer()
                .withId(SYSTEM_ADMIN_BS_ID)
                .withEmailString("foo@localhost")
                .build();

            Mockito.doReturn(Optional.of(admin))
                .when(mock)
                .find(SYSTEM_ADMIN_BS_ID);
        }

        @Test
        void it_returns_status_UP() {
            var actual = sut.call();

            assertThat(actual.getName())
                .isEqualTo("Database");
            assertThat(actual.getStatus())
                .isEqualTo(HealthCheckResponse.Status.UP);
        }
    }

    @Nested
    class if_database_is_ready_but_no_admin_user_is_present {
        @BeforeEach
        void beforeEach() {
            Mockito.doReturn(Optional.empty())
                .when(mock)
                .find(SYSTEM_ADMIN_BS_ID);
        }

        @Test
        void it_returns_status_DOWN() {
            var actual = sut.call();

            assertThat(actual.getStatus())
                .isEqualTo(HealthCheckResponse.Status.DOWN);

            assertThat(actual.getName())
                .isEqualTo("Database");
            assertThat(actual.getData().get())
                .containsEntry("message", "Admin not found");
        }
    }

    @Nested
    class if_database_is_down {
        @BeforeEach
        void beforeEach() {
            Mockito.doThrow(new RuntimeException("some-message"))
                .when(mock)
                .find(SYSTEM_ADMIN_BS_ID);
        }

        @Test
        void it_returns_status_DOWN() {
            var actual = sut.call();

            assertThat(actual.getStatus()).isEqualTo(HealthCheckResponse.Status.DOWN);
            assertThat(actual.getName()).isEqualTo("Database");
            assertThat(actual.getData()).isPresent();
            assertThat(actual.getData().get()).containsEntry("exception", "some-message");
        }
    }
}
