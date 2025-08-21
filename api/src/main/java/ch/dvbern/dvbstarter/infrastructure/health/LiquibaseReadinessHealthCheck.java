package ch.dvbern.dvbstarter.infrastructure.health;

import ch.dvbern.dvbstarter.infrastructure.liquibase.LiquibaseRaceConditionWorkaround;
import jakarta.enterprise.context.RequestScoped;
import jakarta.inject.Inject;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.microprofile.health.HealthCheck;
import org.eclipse.microprofile.health.HealthCheckResponse;
import org.eclipse.microprofile.health.Readiness;

import static java.util.Objects.requireNonNull;

@RequestScoped
@Readiness
@Slf4j
public class LiquibaseReadinessHealthCheck implements HealthCheck {
    private static final String NAME = "Liquibase";

    private final LiquibaseRaceConditionWorkaround liquibaseWorkaround;

    @Inject
    public LiquibaseReadinessHealthCheck(LiquibaseRaceConditionWorkaround liquibaseWorkaround) {
        this.liquibaseWorkaround = requireNonNull(liquibaseWorkaround);
    }

    @Override
    public HealthCheckResponse call() {
        if (liquibaseWorkaround.isInitialized()) {
            return HealthCheckResponse.up(NAME);
        }

        LOG.error("probe failed");
        return HealthCheckResponse.builder()
            .name(NAME)
            .withData("initialized", false)
            .build();
    }
}
