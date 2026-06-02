/*
 *
 * Copyright (C) 2024 DV Bern AG, Switzerland
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package ch.dvbern.oss.hemed.esc.api.shared.scheduler;

import ch.dvbern.dvbstarter.infrastructure.stereotypes.Service;
import ch.dvbern.oss.hemed.esc.api.shared.auth.TenantEnum;
import ch.dvbern.oss.hemed.esc.api.shared.auth.TenantProducer;
import ch.dvbern.oss.hemed.esc.api.shared.config.service.ConfigService;
import io.quarkus.scheduler.Scheduled;
import jakarta.transaction.Transactional;
import jakarta.transaction.Transactional.TxType;
import lombok.RequiredArgsConstructor;
import org.checkerframework.checker.nullness.qual.NonNull;

/**
 * Service zum starten periodischer Tasks
 */
@Service
@RequiredArgsConstructor
public class ScheduledTaskService {

    private final SuperAdminRunnerService superAdminRunnerService;
    private final TenantProducer tenantProducer;
    private final ConfigService configService;

    // Divers

    @Scheduled(cron = "{app.scheduler.benutzer.inaktivieren.cronexpression}")
    @Transactional(TxType.NOT_SUPPORTED)
    public void scheduleBenutzerInaktivierenJob() {
        runForActiveTenants(() -> superAdminRunnerService.runBenutzerInaktivierenJob(false));
    }

    @Scheduled(cron = "{app.scheduler.kinder.inaktivieren.cronexpression}")
    @Transactional(TxType.NOT_SUPPORTED)
    public void scheduleKinderInaktivierenJob() {
        runForActiveTenants(() -> superAdminRunnerService.runKinderInaktivierenJob(false));
    }

    @Scheduled(cron = "{app.scheduler.reload.duplikate.cronexpression}")
    @Transactional(TxType.NOT_SUPPORTED)
    public void scheduleReloadDuplikateJob() {
        runForActiveTenants(() -> superAdminRunnerService.runReloadDuplikateJob(false));
    }

    // Impfstoffe

    @Scheduled(cron = "{app.scheduler.deactivate.impfstoffchargen.cronexpression}")
    @Transactional(TxType.NOT_SUPPORTED)
    public void scheduleDeactivateImpfstoffChargenJob() {
        runForActiveTenants(() -> superAdminRunnerService.runDeactivateImpfstoffChargenJob(false));
    }

    @Scheduled(cron = "{app.scheduler.delete.impfstoffchargen.cronexpression}")
    @Transactional(TxType.NOT_SUPPORTED)
    public void scheduleDeleteImpfstoffChargenJob() {
        runForActiveTenants(() -> superAdminRunnerService.runDeleteImpfstoffChargenJob(false));
    }

    // DocumentQueue

    @Scheduled(cron = "{app.scheduler.document-queue.cronexpression}")
    @Transactional(TxType.NOT_SUPPORTED)
    public void scheduleDocumentQueueJob() {
        runForActiveTenants(() -> superAdminRunnerService.runDocumentQueueJob(false));
    }

    @Scheduled(cron = "{app.scheduler.document-queue.clean-up.cronexpression}")
    @Transactional(TxType.NOT_SUPPORTED)
    public void scheduleDocumentQueueCleanupJob() {
        runForActiveTenants(() -> superAdminRunnerService.runDocumentQueueCleanupJob(false));
    }

    // DossierQueue/ Archivierung

    @Scheduled(cron = "{app.scheduler.archive.documents.download.cronexpression}")
    @Transactional(TxType.NOT_SUPPORTED)
    public void scheduleArchiveDownloadEmailReminderJob() {
        runForActiveTenants(() -> superAdminRunnerService.runArchiveDocumentDownloadEmailReminderJob(false));
    }

    @Scheduled(cron = "{app.scheduler.archivierung.cronexpression}")
    @Transactional(TxType.NOT_SUPPORTED)
    public void scheduleMarkForArchivierungJob() {
        runForActiveTenants(() -> superAdminRunnerService.runMarkForArchivierungJob(false));
    }

    @Scheduled(cron = "{app.scheduler.dossier.queue.processing.cronexpression}")
    @Transactional(TxType.NOT_SUPPORTED)
    public void scheduleDossierQueueProcessingJob() {
        runForActiveTenants(() -> superAdminRunnerService.runDossierQueueProcessingJob(false));
    }

    @Scheduled(cron = "{app.scheduler.dossier.queue.cleanup.cronexpression}")
    @Transactional(TxType.NOT_SUPPORTED)
    public void scheduleDossierQueueCleanupJob() {
        runForActiveTenants(() -> superAdminRunnerService.runDossierQueueCleanupJob(false));
    }

    @Scheduled(cron = "{app.scheduler.archive.kind.delete.cronexpression}")
    @Transactional(TxType.NOT_SUPPORTED)
    public void scheduleArchiveDeleteKindJob() {
        runForActiveTenants(() -> superAdminRunnerService.runArchiveDocumentKindDeleteJob(false));
    }

    @Scheduled(cron = "{app.scheduler.s3-file-consistency.cronexpression}")
    @Transactional(TxType.NOT_SUPPORTED)
    public void scheduleFileStoreHealthCheckJob() {
        runForActiveTenants(() -> superAdminRunnerService.runFileStoreHealthCheckJob(false));
    }

    // Helper Methods

    private void runForActiveTenants(@NonNull Runnable task) {
        for (TenantEnum tenant : TenantEnum.values()) {
            if (tenant.isAktiv() && configService.isTenantEnabledForCurrentEnvironment(tenant)) {
                tenantProducer.setCurrentTenant(tenant);
                task.run();
            }
        }
    }
}
