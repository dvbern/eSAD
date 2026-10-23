package ch.dvbern.oss.hemed.esc.api.shared.auth;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayNameGeneration;
import org.junit.jupiter.api.DisplayNameGenerator.ReplaceUnderscores;
import org.junit.jupiter.api.Test;
import org.slf4j.MDC;

import static ch.dvbern.oss.hemed.esc.api.shared.auth.TenantContext.LOG_MDC_TENANT;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DisplayNameGeneration(ReplaceUnderscores.class)
class TenantContextTest {

    private final TenantContext tenantContext = new TenantContext();

    @AfterEach
    void cleanUpMdc() {
        MDC.remove(LOG_MDC_TENANT);
    }

    @Test
    void get_throws_when_no_tenant_is_set() {
        assertThatThrownBy(tenantContext::get)
            .isInstanceOf(IllegalStateException.class)
            .hasMessageContaining("PerTenantRunnerService");
    }

    @Test
    void getOrNull_returns_null_when_no_tenant_is_set() {
        assertThat(tenantContext.getOrNull())
            .isNull();
    }

    @Test
    void setCurrentTenant_sets_tenant_and_MDC() {
        tenantContext.setCurrentTenant(TenantEnum.WIN);

        assertThat(tenantContext.get())
            .isEqualTo(TenantEnum.WIN);
        assertThat(MDC.get(LOG_MDC_TENANT))
            .isEqualTo(TenantEnum.WIN.name());
    }

    @Test
    void assertCurrentTenantIs_passes_for_the_current_tenant() {
        tenantContext.setCurrentTenant(TenantEnum.BS);

        tenantContext.assertCurrentTenantIs(TenantEnum.BS, "test");

        assertThat(tenantContext.get())
            .isEqualTo(TenantEnum.BS);
    }

    @Test
    void assertCurrentTenantIs_fails_for_another_tenant() {
        tenantContext.setCurrentTenant(TenantEnum.WIN);

        assertThatThrownBy(() -> tenantContext.assertCurrentTenantIs(TenantEnum.BS, "AuthorizerService"))
            .isInstanceOf(IllegalStateException.class)
            .hasMessageContaining("AuthorizerService")
            .hasMessageContaining("WIN");
    }

    @Test
    void forTenant_creates_a_detached_context() {
        TenantContext detached = TenantContext.forTenant(TenantEnum.BE);

        assertThat(detached.get())
            .isEqualTo(TenantEnum.BE);
    }

    @Test
    void forTenant_does_not_touch_the_MDC_of_the_running_thread() {
        tenantContext.setCurrentTenant(TenantEnum.WIN);

        TenantContext tc = TenantContext.forTenant(TenantEnum.BE);

        // Assert that this leaves the MDC / Threadcontext alone. Only methods that create a new Request / Threadcontext
        // should set the mdc
        assertThat(tc).isNotNull().extracting(TenantContext::get).isEqualTo(TenantEnum.BE);
        assertThat(MDC.get(LOG_MDC_TENANT))
            .isEqualTo(TenantEnum.WIN.name());
    }
}
