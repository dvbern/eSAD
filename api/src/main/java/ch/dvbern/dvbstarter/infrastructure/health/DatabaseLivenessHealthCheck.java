package ch.dvbern.dvbstarter.infrastructure.health;

import ch.dvbern.dvbstarter.infrastructure.db.Db;
import ch.dvbern.oss.hemed.esc.api.shared.auth.TenantEnum;
import ch.dvbern.oss.hemed.esc.api.shared.auth.TenantProducer;
import ch.dvbern.oss.hemed.esc.api.shared.benutzer.Benutzer;
import io.smallrye.health.api.Wellness;
import jakarta.enterprise.context.RequestScoped;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.microprofile.health.HealthCheck;
import org.eclipse.microprofile.health.HealthCheckResponse;

@RequestScoped
@Wellness
@RequiredArgsConstructor
@Slf4j
public class DatabaseLivenessHealthCheck implements HealthCheck {

    private static final String NAME = "Database";
    private final Db db;
    private final TenantProducer tenantProducer;

    @Override
    public HealthCheckResponse call() {
        try {
            tenantProducer.setCurrentTenant(TenantEnum.BS);
            var adminFound = db.find(Benutzer.SYSTEM_ADMIN_BS_ID)
                .isPresent();
            if (!adminFound) {
                LOG.error("probe failed: Admin not found");
                return HealthCheckResponse
                    .named(NAME)
                    .down()
                    .withData("message", "Admin not found")
                    .build();
            }
            return HealthCheckResponse.up(NAME);

        } catch (Exception e) {
            LOG.error("probe failed", e);
            return HealthCheckResponse
                .named(NAME)
                .down()
                .withData("exception", e.getMessage())
                .build();
        }
    }
}
