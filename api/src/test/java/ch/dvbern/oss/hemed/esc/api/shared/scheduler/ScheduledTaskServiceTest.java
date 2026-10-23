package ch.dvbern.oss.hemed.esc.api.shared.scheduler;

import java.util.function.Consumer;
import java.util.stream.Stream;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayNameGeneration;
import org.junit.jupiter.api.DisplayNameGenerator.ReplaceUnderscores;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import static org.junit.jupiter.params.provider.Arguments.argumentSet;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

/**
 * The tenant iteration itself is covered by {@code PerTenantRunnerServiceTest}; here we only verify that every job
 * is delegated to {@link PerTenantRunnerService#runForActiveTenants(Runnable)}.
 */
@DisplayNameGeneration(ReplaceUnderscores.class)
class ScheduledTaskServiceTest {

    private SuperAdminRunnerService superAdminRunnerService;
    private PerTenantRunnerService perTenantRunnerService;
    private ScheduledTaskService service;

    @BeforeEach
    void setUp() {
        superAdminRunnerService = mock(SuperAdminRunnerService.class);
        perTenantRunnerService = mock(PerTenantRunnerService.class);
        doAnswer(invocation -> {
            invocation.<Runnable>getArgument(0)
                .run();
            return null;
        }).when(perTenantRunnerService)
            .runForActiveTenants(any());
        service = new ScheduledTaskService(superAdminRunnerService, perTenantRunnerService);
    }

    static Stream<Arguments> jobs() {
        return Stream.of(
            job(
                "BenutzerInaktivieren",
                ScheduledTaskService::scheduleBenutzerInaktivierenJob,
                runner -> runner.runBenutzerInaktivierenJob(false)
            ),
            job(
                "KinderInaktivieren",
                ScheduledTaskService::scheduleKinderInaktivierenJob,
                runner -> runner.runKinderInaktivierenJob(false)
            ),
            job(
                "ReloadDuplikate",
                ScheduledTaskService::scheduleReloadDuplikateJob,
                runner -> runner.runReloadDuplikateJob(false)
            ),
            job(
                "DeactivateImpfstoffChargen",
                ScheduledTaskService::scheduleDeactivateImpfstoffChargenJob,
                runner -> runner.runDeactivateImpfstoffChargenJob(false)
            ),
            job(
                "DeleteImpfstoffChargen",
                ScheduledTaskService::scheduleDeleteImpfstoffChargenJob,
                runner -> runner.runDeleteImpfstoffChargenJob(false)
            ),
            job(
                "DocumentQueue",
                ScheduledTaskService::scheduleDocumentQueueJob,
                runner -> runner.runDocumentQueueJob(false)
            ),
            job(
                "DocumentQueueCleanup",
                ScheduledTaskService::scheduleDocumentQueueCleanupJob,
                runner -> runner.runDocumentQueueCleanupJob(false)
            ),
            job(
                "ArchiveDownloadEmailReminder",
                ScheduledTaskService::scheduleArchiveDownloadEmailReminderJob,
                runner -> runner.runArchiveDocumentDownloadEmailReminderJob(false)
            ),
            job(
                "MarkForArchivierung",
                ScheduledTaskService::scheduleMarkForArchivierungJob,
                runner -> runner.runMarkForArchivierungJob(false)
            ),
            job(
                "DossierQueueProcessing",
                ScheduledTaskService::scheduleDossierQueueProcessingJob,
                runner -> runner.runDossierQueueProcessingJob(false)
            ),
            job(
                "DossierQueueCleanup",
                ScheduledTaskService::scheduleDossierQueueCleanupJob,
                runner -> runner.runDossierQueueCleanupJob(false)
            ),
            job(
                "ArchiveDeleteKind",
                ScheduledTaskService::scheduleArchiveDeleteKindJob,
                runner -> runner.runArchiveDocumentKindDeleteJob(false)
            ),
            job(
                "FileStoreHealthCheck",
                ScheduledTaskService::scheduleFileStoreHealthCheckJob,
                runner -> runner.runFileStoreHealthCheckJob(false)
            ),
            job(
                "AuditRetentionCleanup",
                ScheduledTaskService::scheduleAuditRetentionCleanupJob,
                runner -> runner.runAuditRetentionCleanupJob(false)
            )
        );
    }

    @ParameterizedTest
    @MethodSource("jobs")
    void delegates_job_to_PerTenantRunnerService(
        Consumer<ScheduledTaskService> schedule,
        Consumer<SuperAdminRunnerService> expectedJob
    ) {
        schedule.accept(service);

        verify(perTenantRunnerService, times(1))
            .runForActiveTenants(any());
        expectedJob.accept(verify(superAdminRunnerService, times(1)));
    }

    private static Arguments job(
        String name,
        Consumer<ScheduledTaskService> schedule,
        Consumer<SuperAdminRunnerService> expectedJob
    ) {
        return argumentSet(name, schedule, expectedJob);
    }
}
