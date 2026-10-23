package ch.dvbern.oss.hemed.esc.api.shared.scheduler;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import ch.dvbern.oss.hemed.esc.api.shared.auth.TenantContext;
import ch.dvbern.oss.hemed.esc.api.shared.auth.TenantEnum;
import ch.dvbern.oss.hemed.esc.api.testutil.security.TestAsAdminBS;
import io.quarkus.arc.Arc;
import io.quarkus.arc.InjectableContext.ContextState;
import io.quarkus.arc.ManagedContext;
import io.quarkus.test.junit.QuarkusTest;
import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.DisplayNameGeneration;
import org.junit.jupiter.api.DisplayNameGenerator.ReplaceUnderscores;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.slf4j.MDC;
import software.amazon.awssdk.services.s3.S3AsyncClient;

import static ch.dvbern.oss.hemed.esc.api.shared.auth.TenantContext.LOG_MDC_TENANT;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@QuarkusTest
@RequiredArgsConstructor
@DisplayNameGeneration(ReplaceUnderscores.class)
class PerTenantRunnerServiceTest {

    final PerTenantRunnerService perTenantRunnerService;
    final TenantContext tenantContext;
    final TenantRequestScopeProbe probe;

    @Nested
    class RunForTenant {

        @Test
        void gives_the_task_a_fresh_context_even_when_one_is_already_active() {
            ManagedContext requestContext = Arc.container().requestContext();
            assertThat(requestContext.isActive())
                .as("precondition: the test itself runs inside a request context")
                .isTrue();
            tenantContext.setCurrentTenant(TenantEnum.BS);
            ContextState callerState = requestContext.getState();

            ContextState taskState = perTenantRunnerService.runForTenant(TenantEnum.WIN, () -> {
                assertThat(tenantContext.get())
                    .isEqualTo(TenantEnum.WIN);
                return requestContext.getState();
            });

            assertThat(taskState)
                .as("the task must not inherit the caller's request-scoped beans")
                .isNotSameAs(callerState);
            assertThat(requestContext.getState())
                .as("the caller's request context must be active again")
                .isSameAs(callerState);
        }

        @Test
        void restores_the_callers_tenant_and_MDC() {
            tenantContext.setCurrentTenant(TenantEnum.BS);

            String mdcInsideTask = perTenantRunnerService.runForTenant(
                TenantEnum.WIN,
                () -> MDC.get(LOG_MDC_TENANT)
            );

            assertThat(mdcInsideTask)
                .isEqualTo(TenantEnum.WIN.name());
            assertThat(tenantContext.get())
                .isEqualTo(TenantEnum.BS);
            assertThat(MDC.get(LOG_MDC_TENANT))
                .isEqualTo(TenantEnum.BS.name());
        }

        @Test
        void creates_request_scoped_beans_per_tenant() {
            TenantRequestScopeProbe probeBS = perTenantRunnerService.runForTenant(TenantEnum.BS, probe::self);
            TenantRequestScopeProbe probeWIN = perTenantRunnerService.runForTenant(TenantEnum.WIN, probe::self);

            assertThat(probeWIN)
                .isNotSameAs(probeBS);
        }

        @Test
        void resolves_the_S3_client_of_each_tenant_one_after_the_other() {
            S3AsyncClient clientBS = perTenantRunnerService.runForTenant(
                TenantEnum.BS,
                probe::getS3AsyncClient
            );
            S3AsyncClient clientWIN = perTenantRunnerService.runForTenant(
                TenantEnum.WIN,
                probe::getS3AsyncClient
            );

            // regression ESC-FILESTORE-HEALTHCHECK: WIN used the S3 client (credentials) of BS
            assertThat(clientWIN)
                .isNotSameAs(clientBS);
        }

        @Test
        void uses_the_S3_credentials_of_each_tenant_one_after_the_other() {
            String accessKeyBS = perTenantRunnerService.runForTenant(TenantEnum.BS, probe::s3AccessKeyId);
            String accessKeyWIN = perTenantRunnerService.runForTenant(TenantEnum.WIN, probe::s3AccessKeyId);

            // regression ESC-FILESTORE-HEALTHCHECK: the 403 was caused by WIN using the credentials of BS
            assertThat(accessKeyBS)
                .isEqualTo("test-key-bs");
            assertThat(accessKeyWIN)
                .isEqualTo("test-key-win");
        }

        @Test
        void lists_only_the_bucket_of_each_tenant_one_after_the_other() {
            String keyBS = "per-tenant-runner-bs-" + UUID.randomUUID();
            String keyWIN = "per-tenant-runner-win-" + UUID.randomUUID();
            perTenantRunnerService.runForTenant(TenantEnum.BS, () -> probe.putIntoTenantBucket(keyBS));
            perTenantRunnerService.runForTenant(TenantEnum.WIN, () -> probe.putIntoTenantBucket(keyWIN));

            List<String> keysBS = perTenantRunnerService.runForTenant(TenantEnum.BS, probe::listTenantBucket);
            List<String> keysWIN = perTenantRunnerService.runForTenant(TenantEnum.WIN, probe::listTenantBucket);

            assertThat(keysBS)
                .contains(keyBS)
                .doesNotContain(keyWIN);
            assertThat(keysWIN)
                .contains(keyWIN)
                .doesNotContain(keyBS);
        }

        @Test
        void reads_from_the_datasource_of_each_tenant_without_transaction() {
            String databaseBS = perTenantRunnerService.runForTenant(TenantEnum.BS, probe::currentDatabase);
            String databaseWIN = perTenantRunnerService.runForTenant(TenantEnum.WIN, probe::currentDatabase);

            assertThat(databaseWIN)
                .isNotEqualTo(databaseBS);
        }

        @Test
        void nested_run_gets_its_own_context_and_restores_the_outer_one() {
            List<TenantEnum> seen = new ArrayList<>();

            perTenantRunnerService.runForTenant(TenantEnum.BS, () -> {
                TenantRequestScopeProbe outer = probe.self();
                TenantRequestScopeProbe inner = perTenantRunnerService.runForTenant(TenantEnum.WIN, () -> {
                    seen.add(tenantContext.get());
                    return probe.self();
                });
                seen.add(tenantContext.get());
                assertThat(inner)
                    .isNotSameAs(outer);
                assertThat(probe.self())
                    .isSameAs(outer);
            });

            assertThat(seen)
                .containsExactly(TenantEnum.WIN, TenantEnum.BS);
        }

        @Test
        @TestAsAdminBS
        void applies_RunAs_inside_the_task_and_keeps_the_callers_identity() {
            String callerIdentity = probe.currentIdentityName();

            String identityInTask = perTenantRunnerService.runForTenant(
                TenantEnum.WIN,
                probe::currentIdentityNameRunAs
            );

            assertThat(identityInTask)
                .isEqualTo(TenantRequestScopeProbe.RUN_AS_USER);
            assertThat(probe.currentIdentityName())
                .isEqualTo(callerIdentity);
        }

        @Test
        void terminates_the_task_context_when_the_task_fails() {
            tenantContext.setCurrentTenant(TenantEnum.BS);

            assertThatThrownBy(() -> perTenantRunnerService.runForTenant(TenantEnum.WIN, this::fail))
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("boom WIN");

            assertThat(tenantContext.get())
                .isEqualTo(TenantEnum.BS);
        }

        private void fail() {
            throw new IllegalStateException("boom " + tenantContext.get());
        }
    }

    @Nested
    class RunForActiveTenants {

        @Test
        void runs_each_active_tenant_with_its_own_tenant_set() {
            tenantContext.setCurrentTenant(TenantEnum.BS);
            List<TenantEnum> seen = new ArrayList<>();

            perTenantRunnerService.runForActiveTenants(() -> seen.add(tenantContext.get()));

            assertThat(perTenantRunnerService.getActiveTenants())
                .as("precondition: the bug only shows from the second tenant on")
                .hasSizeGreaterThanOrEqualTo(2)
                .doesNotContain(TenantEnum.DEFAULT);
            assertThat(seen)
                .isEqualTo(perTenantRunnerService.getActiveTenants());
            assertThat(tenantContext.get())
                .isEqualTo(TenantEnum.BS);
        }

        @Test
        void continues_after_a_failing_tenant_and_reports_all_failures_at_the_end() {
            List<TenantEnum> activeTenants = perTenantRunnerService.getActiveTenants();
            TenantEnum failingTenant = activeTenants.getFirst();
            List<TenantEnum> seen = new ArrayList<>();

            assertThatThrownBy(() -> perTenantRunnerService.runForActiveTenants(() -> {
                    TenantEnum tenant = tenantContext.get();
                    seen.add(tenant);
                    if (tenant == failingTenant) {
                        throw new IllegalStateException("boom " + tenant);
                    }
                }))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining(failingTenant.name())
                .satisfies(e -> assertThat(e.getSuppressed())
                    .extracting(Throwable::getMessage)
                    .containsExactly("boom " + failingTenant));

            assertThat(seen)
                .isEqualTo(activeTenants);
        }
    }
}
