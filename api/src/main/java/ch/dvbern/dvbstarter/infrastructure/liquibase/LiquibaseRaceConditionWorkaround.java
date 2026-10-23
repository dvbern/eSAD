package ch.dvbern.dvbstarter.infrastructure.liquibase;


import io.quarkus.runtime.ShutdownEvent;
import io.quarkus.runtime.StartupEvent;
import io.quarkus.runtime.configuration.ConfigUtils;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.event.Observes;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

/**
 * Tracks whether the application startup completed, used by the readiness health check.
 */
@Getter
@ApplicationScoped
@Slf4j
public class LiquibaseRaceConditionWorkaround {

    private boolean initialized = false;

    @SuppressWarnings("java:S1172" /* false positive */)
    void onStart(@Observes StartupEvent ignored) {
        var activeProfiles = ConfigUtils.getProfiles();
        LOG.info("The application is starting... (Profiles: {})", activeProfiles);

        initialized = true;

        LOG.info("The application started (Profiles: {})", activeProfiles);
    }

    @SuppressWarnings("java:S1172" /* false positive */)
    void onStop(@Observes ShutdownEvent ignored) {
        LOG.info("The application is stopping...");
        initialized = false;
    }

}
