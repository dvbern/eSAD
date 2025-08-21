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

    @Scheduled(cron = "{app.scheduler.benutzer.inaktivieren.cronexpression}")
    @Transactional(TxType.NOT_SUPPORTED)
    public void scheduleBenutzerInaktivierenJob() {
        superAdminRunnerService.runBenutzerInaktivierenJob(false);
    }

    @Scheduled(cron = "{app.scheduler.document-queue.cronexpression}")
    @Transactional(TxType.NOT_SUPPORTED)
    public void scheduleDocumentQueueJob() {
        superAdminRunnerService.runDocumentQueueJob(false);
    }

    @Scheduled(cron = "{app.scheduler.kinder.inaktivieren.cronexpression}")
    @Transactional(TxType.NOT_SUPPORTED)
    public void scheduleKinderInaktivierenJob() {
        superAdminRunnerService.runKinderInaktivierenJob(false);
    }

    @Scheduled(cron = "{app.scheduler.document-queue.clean-up.cronexpression}")
    @Transactional(TxType.NOT_SUPPORTED)
    public void scheduleDocumentQueueCleanupJob() {
        superAdminRunnerService.runDocumentQueueCleanupJob(false);
    }

    @Scheduled(cron = "{app.scheduler.reload.duplikate.cronexpression}")
    @Transactional(TxType.NOT_SUPPORTED)
    public void scheduleReloadDuplikateJob() {
        superAdminRunnerService.runReloadDuplikateJob(false);
    }
}
