package ch.dvbern.dvbstarter.infrastructure.health.tenant;

import java.util.List;
import java.util.Map;

import ch.dvbern.dvbstarter.infrastructure.health.tenant.AbstractTenantHealthCheck.TenantHealthCheckResult;
import ch.dvbern.dvbstarter.infrastructure.health.tenant.TenantHealthCheck.TenantOutcome;
import ch.dvbern.oss.hemed.esc.api.shared.auth.TenantContext;
import ch.dvbern.oss.hemed.esc.api.shared.auth.TenantEnum;
import ch.dvbern.oss.hemed.esc.api.shared.scheduler.PerTenantRunnerService;
import io.quarkus.test.junit.QuarkusTest;
import io.smallrye.health.api.Wellness;
import jakarta.inject.Inject;
import lombok.RequiredArgsConstructor;
import org.eclipse.microprofile.health.HealthCheckResponse;
import org.eclipse.microprofile.health.HealthCheckResponse.Status;
import org.junit.jupiter.api.DisplayNameGeneration;
import org.junit.jupiter.api.DisplayNameGenerator.ReplaceUnderscores;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@QuarkusTest
@RequiredArgsConstructor
@DisplayNameGeneration(ReplaceUnderscores.class)
class TenantHealthCheckTest {

    @Inject
    @Wellness
    protected TenantHealthCheck tenantHealthCheck;
    final PerTenantRunnerService perTenantRunnerService;
    final TenantContext tenantContext;

    @Test
    void checks_every_active_tenant_from_within_an_active_request_context() {
        tenantContext.setCurrentTenant(TenantEnum.BS);

        HealthCheckResponse response = tenantHealthCheck.call();

        Map<String, Object> data = response.getData()
            .orElseThrow();
        for (TenantEnum tenant : perTenantRunnerService.getActiveTenants()) {
            String prefix = '[' + tenant.name() + "] ";
            assertThat(data)
                .containsKey(prefix + "allChecksPassed")
                .containsKey(prefix + "tenantSettingsPresent")
                .containsKey(prefix + "s3EncryptionKeyPresent")
                .doesNotContainKey(prefix + "error");
        }
        // the WIN test database is deliberately not migrated (%test.quarkus.liquibase.win.migrate-at-start=false):
        // if WIN reused the request context (and datasource) of BS, it would report its settings as present
        assertThat(data)
            .containsEntry("[BS] tenantSettingsPresent", true)
            .containsEntry("[WIN] tenantSettingsPresent", false);
        assertThat(tenantContext.get())
            .as("the caller's tenant must be restored")
            .isEqualTo(TenantEnum.BS);
    }

    @Test
    void reports_up_with_tenant_keys_when_all_checks_pass() {
        HealthCheckResponse response = TenantHealthCheck.buildResponse(List.of(
            new TenantOutcome(TenantEnum.BS, passingResult(TenantEnum.BS), null)
        ));

        assertThat(response.getStatus())
            .isEqualTo(Status.UP);
        assertThat(response.getData()
            .orElseThrow())
            .containsEntry("[BS] allChecksPassed", true);
    }

    @Test
    void reports_down_with_error_for_the_failing_tenant_only() {
        HealthCheckResponse response = TenantHealthCheck.buildResponse(List.of(
            new TenantOutcome(TenantEnum.BS, null, "java.lang.IllegalStateException: boom"),
            new TenantOutcome(TenantEnum.WIN, passingResult(TenantEnum.WIN), null)
        ));

        Map<String, Object> data = response.getData()
            .orElseThrow();
        assertThat(response.getStatus())
            .isEqualTo(Status.DOWN);
        assertThat(data)
            .containsEntry("[BS] error", "java.lang.IllegalStateException: boom")
            .containsEntry("[BS] allChecksPassed", false)
            .containsEntry("[WIN] allChecksPassed", true)
            .containsEntry("[WIN] tenantSettingsPresent", true)
            .doesNotContainKey("[WIN] error");
    }

    private static TenantHealthCheckResult passingResult(TenantEnum tenant) {
        return new TenantHealthCheckResult(tenant, true, true, true, true, true, true, true);
    }
}
