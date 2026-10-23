package ch.dvbern.oss.hemed.esc.api.shared.scheduler;

import ch.dvbern.oss.hemed.esc.api.shared.auth.TenantEnum;
import org.junit.jupiter.api.DisplayNameGeneration;
import org.junit.jupiter.api.DisplayNameGenerator.ReplaceUnderscores;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;

@DisplayNameGeneration(ReplaceUnderscores.class)
class SuperAdminEventServiceTest {

    private final PerTenantRunnerService perTenantRunnerService = mock(PerTenantRunnerService.class);
    private final SuperAdminRunnerService superAdminRunnerService = mock(SuperAdminRunnerService.class);
    private final SuperAdminEventService sut =
        new SuperAdminEventService(perTenantRunnerService, superAdminRunnerService);

    @Test
    void runs_the_duplikat_recalculation_in_the_context_of_the_event_tenant() {
        ArgumentCaptor<Runnable> task = ArgumentCaptor.forClass(Runnable.class);

        sut.handleDuplikatRecalculationEvent(TenantEnum.WIN);

        verify(perTenantRunnerService).runForTenant(eq(TenantEnum.WIN), task.capture());
        verifyNoInteractions(superAdminRunnerService);

        task.getValue().run();

        verify(superAdminRunnerService).runReloadDuplikateJob(true);
    }

    @Test
    void does_not_touch_the_job_outside_the_runner() {
        sut.handleDuplikatRecalculationEvent(TenantEnum.BS);

        verify(perTenantRunnerService).runForTenant(eq(TenantEnum.BS), any(Runnable.class));
        verifyNoInteractions(superAdminRunnerService);
    }
}
