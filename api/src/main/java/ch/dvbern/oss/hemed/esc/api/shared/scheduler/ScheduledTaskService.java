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
import io.quarkus.scheduler.Scheduled;
import jakarta.transaction.Transactional;
import jakarta.transaction.Transactional.TxType;
import lombok.RequiredArgsConstructor;

/**
 * Service zum starten periodischer Tasks
 */
@Service
@RequiredArgsConstructor
public class ScheduledTaskService {

    private final SuperAdminRunnerService superAdminRunnerService;
    private final PerTenantRunnerService perTenantRunnerService;

    // Divers

    @Scheduled(cron = "{app.scheduler.benutzer.inaktivieren.cronexpression}")
    @Transactional(TxType.NOT_SUPPORTED)
    public void scheduleBenutzerInaktivierenJob() {
        perTenantRunnerService.runForActiveTenants(
            () -> superAdminRunnerService.runBenutzerInaktivierenJob(false)
        );
    }

    @Scheduled(cron = "{app.scheduler.kinder.inaktivieren.cronexpression}")
    @Transactional(TxType.NOT_SUPPORTED)
    public void scheduleKinderInaktivierenJob() {
        perTenantRunnerService.runForActiveTenants(
            () -> superAdminRunnerService.runKinderInaktivierenJob(false)
        );
    }

    @Scheduled(cron = "{app.scheduler.reload.duplikate.cronexpression}")
    @Transactional(TxType.NOT_SUPPORTED)
    public void scheduleReloadDuplikateJob() {
        perTenantRunnerService.runForActiveTenants(
            () -> superAdminRunnerService.runReloadDuplikateJob(false)
        );
    }

    // Impfstoffe

    @Scheduled(cron = "{app.scheduler.deactivate.impfstoffchargen.cronexpression}")
    @Transactional(TxType.NOT_SUPPORTED)
    public void scheduleDeactivateImpfstoffChargenJob() {
        perTenantRunnerService.runForActiveTenants(
            () -> superAdminRunnerService.runDeactivateImpfstoffChargenJob(false)
        );
    }

    @Scheduled(cron = "{app.scheduler.delete.impfstoffchargen.cronexpression}")
    @Transactional(TxType.NOT_SUPPORTED)
    public void scheduleDeleteImpfstoffChargenJob() {
        perTenantRunnerService.runForActiveTenants(
            () -> superAdminRunnerService.runDeleteImpfstoffChargenJob(false)
        );
    }

    // DocumentQueue

    @Scheduled(cron = "{app.scheduler.document-queue.cronexpression}")
    @Transactional(TxType.NOT_SUPPORTED)
    public void scheduleDocumentQueueJob() {
        perTenantRunnerService.runForActiveTenants(
            () -> superAdminRunnerService.runDocumentQueueJob(false)
        );
    }

    @Scheduled(cron = "{app.scheduler.document-queue.clean-up.cronexpression}")
    @Transactional(TxType.NOT_SUPPORTED)
    public void scheduleDocumentQueueCleanupJob() {
        perTenantRunnerService.runForActiveTenants(
            () -> superAdminRunnerService.runDocumentQueueCleanupJob(false)
        );
    }

    // DossierQueue/ Archivierung

    @Scheduled(cron = "{app.scheduler.archive.documents.download.cronexpression}")
    @Transactional(TxType.NOT_SUPPORTED)
    public void scheduleArchiveDownloadEmailReminderJob() {
        perTenantRunnerService.runForActiveTenants(
            () -> superAdminRunnerService.runArchiveDocumentDownloadEmailReminderJob(false)
        );
    }

    @Scheduled(cron = "{app.scheduler.archivierung.cronexpression}")
    @Transactional(TxType.NOT_SUPPORTED)
    public void scheduleMarkForArchivierungJob() {
        perTenantRunnerService.runForActiveTenants(
            () -> superAdminRunnerService.runMarkForArchivierungJob(false)
        );
    }

    @Scheduled(cron = "{app.scheduler.dossier.queue.processing.cronexpression}")
    @Transactional(TxType.NOT_SUPPORTED)
    public void scheduleDossierQueueProcessingJob() {
        perTenantRunnerService.runForActiveTenants(
            () -> superAdminRunnerService.runDossierQueueProcessingJob(false)
        );
    }

    @Scheduled(cron = "{app.scheduler.dossier.queue.cleanup.cronexpression}")
    @Transactional(TxType.NOT_SUPPORTED)
    public void scheduleDossierQueueCleanupJob() {
        perTenantRunnerService.runForActiveTenants(
            () -> superAdminRunnerService.runDossierQueueCleanupJob(false)
        );
    }

    @Scheduled(cron = "{app.scheduler.archive.kind.delete.cronexpression}")
    @Transactional(TxType.NOT_SUPPORTED)
    public void scheduleArchiveDeleteKindJob() {
        perTenantRunnerService.runForActiveTenants(
            () -> superAdminRunnerService.runArchiveDocumentKindDeleteJob(false)
        );
    }

    @Scheduled(cron = "{app.scheduler.s3-file-consistency.cronexpression}")
    @Transactional(TxType.NOT_SUPPORTED)
    public void scheduleFileStoreHealthCheckJob() {
        perTenantRunnerService.runForActiveTenants(
            () -> superAdminRunnerService.runFileStoreHealthCheckJob(false)
        );
    }

    @Scheduled(cron = "{app.scheduler.audit-retention.cronexpression}")
    @Transactional(TxType.NOT_SUPPORTED)
    public void scheduleAuditRetentionCleanupJob() {
        perTenantRunnerService.runForActiveTenants(
            () -> superAdminRunnerService.runAuditRetentionCleanupJob(false)
        );
    }
}
