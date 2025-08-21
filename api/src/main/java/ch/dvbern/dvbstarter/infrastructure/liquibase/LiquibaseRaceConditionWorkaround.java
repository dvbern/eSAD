package ch.dvbern.dvbstarter.infrastructure.liquibase;


import ch.dvbern.oss.hemed.esc.api.shared.benutzer.QBenutzer;
import io.quarkus.runtime.ShutdownEvent;
import io.quarkus.runtime.StartupEvent;
import io.quarkus.runtime.configuration.ConfigUtils;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.event.Observes;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

@Getter
@ApplicationScoped
@Slf4j
public class LiquibaseRaceConditionWorkaround {

    private boolean initialized = false;

    @SuppressWarnings("java:S1172" /* false positive */)
    void onStart(@Observes StartupEvent ignored) {
        var activeProfiles = ConfigUtils.getProfiles();
        LOG.info("The application is starting... (Profiles: {})", activeProfiles);

        liquibaseRaceConditionWorkaround();

        initialized = true;

        LOG.info("The application started (Profiles: {})", activeProfiles);
    }

    private void liquibaseRaceConditionWorkaround() {
        // Es war einmal...
        // Liquibase hat eine Race-Condition bei der Initialisierung der Q-Klassen.
        // Wenn *erstmalig* zwei Queries gleichzeitig kommen, kommt es vor,
        // das es ein Java-Deadlock in der Klasseninitialisierung
        // (sehr wahrscheinlich in QAbstractUUIDEntity und/oder QAbstractEntity) gibt.
        // => Workaround: eine Q-Klasse hier mal explizit initialisieren
        // Wahrscheinlich ist der Bug hier relevant: https://github.com/querydsl/querydsl/issues/2334
        if (QBenutzer.benutzer == null) {
            throw new IllegalStateException("Uhm... wtf?");
        }
    }

    @SuppressWarnings("java:S1172" /* false positive */)
    void onStop(@Observes ShutdownEvent ignored) {
        LOG.info("The application is stopping...");
        initialized = false;
    }

}
