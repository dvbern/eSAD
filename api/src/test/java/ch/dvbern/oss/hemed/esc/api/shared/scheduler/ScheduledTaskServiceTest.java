package ch.dvbern.oss.hemed.esc.api.shared.scheduler;

import ch.dvbern.oss.hemed.esc.api.shared.auth.TenantEnum;
import ch.dvbern.oss.hemed.esc.api.shared.auth.TenantProducer;
import ch.dvbern.oss.hemed.esc.api.shared.config.service.ConfigService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InOrder;

import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class ScheduledTaskServiceTest {

    private SuperAdminRunnerService superAdminRunnerService;
    private TenantProducer tenantProducer;
    private ConfigService configService;
    private ScheduledTaskService service;

    @BeforeEach
    void setUp() {
        superAdminRunnerService = mock(SuperAdminRunnerService.class);
        tenantProducer = mock(TenantProducer.class);
        configService = mock(ConfigService.class);
        service = new ScheduledTaskService(superAdminRunnerService, tenantProducer, configService);

        // Default: only BE is active and enabled
        for (TenantEnum tenant : TenantEnum.values()) {
            boolean enabled = tenant == TenantEnum.BE;
            when(configService.isTenantEnabledForCurrentEnvironment(tenant)).thenReturn(enabled);
        }
    }

    @Test
    void testScheduleBenutzerInaktivierenJob() {
        service.scheduleBenutzerInaktivierenJob();
        verifyJobWasCalledForEnabledTenants();
        verify(superAdminRunnerService, atLeastOnce()).runBenutzerInaktivierenJob(false);
    }

    @Test
    void testScheduleDocumentQueueJob() {
        service.scheduleDocumentQueueJob();
        verifyJobWasCalledForEnabledTenants();
        verify(superAdminRunnerService, atLeastOnce()).runDocumentQueueJob(false);
    }

    @Test
    void testScheduleKinderInaktivierenJob() {
        service.scheduleKinderInaktivierenJob();
        verifyJobWasCalledForEnabledTenants();
        verify(superAdminRunnerService, atLeastOnce()).runKinderInaktivierenJob(false);
    }

    @Test
    void testScheduleDocumentQueueCleanupJob() {
        service.scheduleDocumentQueueCleanupJob();
        verifyJobWasCalledForEnabledTenants();
        verify(superAdminRunnerService, atLeastOnce()).runDocumentQueueCleanupJob(false);
    }

    @Test
    void testScheduleReloadDuplikateJob() {
        service.scheduleReloadDuplikateJob();
        verifyJobWasCalledForEnabledTenants();
        verify(superAdminRunnerService, atLeastOnce()).runReloadDuplikateJob(false);
    }

    @Test
    void testScheduleDeactivateImpfstoffChargenJob() {
        service.scheduleDeactivateImpfstoffChargenJob();
        verifyJobWasCalledForEnabledTenants();
        verify(superAdminRunnerService, atLeastOnce()).runDeactivateImpfstoffChargenJob(false);
    }

    @Test
    void testScheduleDeleteImpfstoffChargenJob() {
        service.scheduleDeleteImpfstoffChargenJob();
        verifyJobWasCalledForEnabledTenants();
        verify(superAdminRunnerService, atLeastOnce()).runDeleteImpfstoffChargenJob(false);
    }

    @Test
    void testScheduleArchiveDownloadEmailReminder() {
        service.scheduleArchiveDownloadEmailReminderJob();
        verifyJobWasCalledForEnabledTenants();
        verify(superAdminRunnerService, atLeastOnce()).runArchiveDocumentDownloadEmailReminderJob(false);
    }

    @Test
    void testScheduleMarkForArchivierungDeleteJob() {
        service.scheduleArchiveDeleteKindJob();
        verifyJobWasCalledForEnabledTenants();
        verify(superAdminRunnerService, atLeastOnce()).runArchiveDocumentKindDeleteJob(false);
    }

    @Test
    void testScheduleDossierQueueProcessingJob() {
        service.scheduleDossierQueueProcessingJob();
        verifyJobWasCalledForEnabledTenants();
        verify(superAdminRunnerService, atLeastOnce()).runDossierQueueProcessingJob(false);
    }

    @Test
    void testScheduleDossierQueueCleanupJob() {
        service.scheduleDossierQueueCleanupJob();
        verifyJobWasCalledForEnabledTenants();
        verify(superAdminRunnerService, atLeastOnce()).runDossierQueueCleanupJob(false);
    }

    @Test
    void testScheduleMarkForArchivierungJob() {
        service.scheduleMarkForArchivierungJob();
        verifyJobWasCalledForEnabledTenants();
        verify(superAdminRunnerService, atLeastOnce()).runMarkForArchivierungJob(false);
    }

    @Test
    void testRunForActiveTenants_MultipleTenants() {
        // Arrange: Enable BE and BS
        when(configService.isTenantEnabledForCurrentEnvironment(TenantEnum.BE)).thenReturn(true);
        when(configService.isTenantEnabledForCurrentEnvironment(TenantEnum.BS)).thenReturn(true);
        when(configService.isTenantEnabledForCurrentEnvironment(TenantEnum.WIN)).thenReturn(false);

        // Act
        service.scheduleBenutzerInaktivierenJob();

        // Assert
        InOrder inOrder = inOrder(tenantProducer, superAdminRunnerService);

        // Order is BS, WIN, BE in Enum? Let's check TenantEnum. BS(13), WIN(14), BE(15).
        // Order: DEFAULT, BS, WIN, BE. DEFAULT.isAktiv() == false.

        inOrder.verify(tenantProducer).setCurrentTenant(TenantEnum.BS);
        inOrder.verify(superAdminRunnerService).runBenutzerInaktivierenJob(false);

        inOrder.verify(tenantProducer).setCurrentTenant(TenantEnum.BE);
        inOrder.verify(superAdminRunnerService).runBenutzerInaktivierenJob(false);

        verify(tenantProducer, never()).setCurrentTenant(TenantEnum.WIN);
        verify(tenantProducer, never()).setCurrentTenant(TenantEnum.DEFAULT);
    }

    private void verifyJobWasCalledForEnabledTenants() {
        verify(tenantProducer).setCurrentTenant(TenantEnum.BE);
        verify(tenantProducer, never()).setCurrentTenant(TenantEnum.BS);
        verify(tenantProducer, never()).setCurrentTenant(TenantEnum.WIN);
        verify(tenantProducer, never()).setCurrentTenant(TenantEnum.DEFAULT);
    }
}
