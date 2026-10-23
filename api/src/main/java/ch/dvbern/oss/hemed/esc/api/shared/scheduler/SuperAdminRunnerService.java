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

import java.util.List;

import ch.dvbern.dvbstarter.infrastructure.stereotypes.Service;
import ch.dvbern.dvbstarter.shared.clock.AppClock;
import ch.dvbern.dvbstarter.shared.runas.RunAs;
import ch.dvbern.dvbstarter.shared.types.id.ID;
import ch.dvbern.oss.hemed.esc.api.features.anlass.impfung.UntersuchungImpfungInternService;
import ch.dvbern.oss.hemed.esc.api.shared.fileio.healthcheck.FileStoreHealthCheckService;
import ch.dvbern.oss.hemed.esc.api.features.archivierung.ArchivdokumentService;
import ch.dvbern.oss.hemed.esc.api.features.auditretention.AuditRetentionService;
import ch.dvbern.oss.hemed.esc.api.features.benutzer.BenutzerService;
import ch.dvbern.oss.hemed.esc.api.features.kinddeactivation.KindDeactivationService;
import ch.dvbern.oss.hemed.esc.api.features.kinder.Dossier;
import ch.dvbern.oss.hemed.esc.api.features.kinder.Kind;
import ch.dvbern.oss.hemed.esc.api.features.kinder.KindService;
import ch.dvbern.oss.hemed.esc.api.features.kinder.duplikate.Duplikat;
import ch.dvbern.oss.hemed.esc.api.features.kinder.duplikate.DuplikatService;
import ch.dvbern.oss.hemed.esc.api.features.kinder.duplikate.DuplikatStatus;
import ch.dvbern.oss.hemed.esc.api.shared.archivierung.ArchivierungRuleService;
import ch.dvbern.oss.hemed.esc.api.shared.auth.BerechtigungName;
import ch.dvbern.oss.hemed.esc.api.shared.benutzer.Benutzer;
import ch.dvbern.oss.hemed.esc.api.shared.config.service.ConfigService;
import ch.dvbern.oss.hemed.esc.api.shared.documentqueue.DocumentQueueRunnerService;
import ch.dvbern.oss.hemed.esc.api.shared.dossierqueue.DossierQueueRunnerService;
import ch.dvbern.oss.hemed.esc.api.shared.dossierqueue.DossierQueueService;
import ch.dvbern.oss.hemed.esc.api.shared.dossierqueue.DossierQueueTyp;
import ch.dvbern.oss.hemed.esc.api.shared.types.EntityAktivStatus;
import jakarta.transaction.Transactional;
import jakarta.transaction.Transactional.TxType;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.Validate;

@Service
@Slf4j
@RequiredArgsConstructor
public class SuperAdminRunnerService {

    private final SchedulerConfigService schedulerConfigService;
    private final ConfigService configService;
    private final BenutzerService benutzerService;
    private final AppClock appClock;
    private final DocumentQueueRunnerService documentQueueRunnerService;
    private final KindDeactivationService kindDeactivationService;
    private final KindService kindService;
    private final DuplikatService duplikatService;
    private final UntersuchungImpfungInternService impfungInternService;
    private final ArchivdokumentService archivdokumentService;
    private final DossierQueueRunnerService dossierQueueRunnerService;
    private final ArchivierungRuleService archivierungRuleService;
    private final DossierQueueService dossierQueueService;
    private final FileStoreHealthCheckService fileStoreHealthCheckService;
    private final AuditRetentionService auditRetentionService;

    @RunAs(username = "Irrelevant solange Rolle SYSTEM_ADMIN", roles = { BerechtigungName.SYSTEM_ADMIN })
    public void runBenutzerInaktivierenJob(boolean forcerun) {
        final SchedulerConfig config = schedulerConfigService.getSchedulerConfig(SchedulerTyp.BENUTZER_INAKVIEREN);
        if (!config.isDisabled() || forcerun) {
            int minutesInactive = configService.getInactivateBenutzerAfterMinutesUnused();
            List<ID<Benutzer>> benutzerToDeactivate =
                this.benutzerService.getBenutzerToDeactivate(
                    minutesInactive,
                    config.getBatchsize());

            SchedulerProcessingResult
                result = new SchedulerProcessingResult(config.getBatchsize(), 0, benutzerToDeactivate.size());
            if (!benutzerToDeactivate.isEmpty()) {
                LOG.info(
                    "ESC-DEACTIVATE-USERS: Found {} Benutzer that did not login since {}. ",
                    benutzerToDeactivate.size(),
                    appClock.localDateTime().now().minusMinutes(minutesInactive));
                long anzahlDeaktiviert = benutzerService.deactivateListOfBenutzer(benutzerToDeactivate);
                result.setSuccessCounter(anzahlDeaktiviert);
            }
            LOG.info(result.getResultString("ESC-DEACTIVATE-USERS"));
        } else {
            LOG.debug("ESC-DEACTIVATE-USERS: Scheduler is disabled.");
        }
    }

    @RunAs(username = "bs-esc@dvbern.ch", roles = { BerechtigungName.SYSTEM_ADMIN })
    @Transactional(TxType.NOT_SUPPORTED)
    public void runDocumentQueueJob(boolean forcerun) {
        final SchedulerConfig config = schedulerConfigService.getSchedulerConfig(SchedulerTyp.DOCUMENT_QUEUE);
        if (!config.isDisabled() || forcerun) {
            SchedulerProcessingResult result = documentQueueRunnerService.performDocumentGeneration(config);
            if (result.getTotalCounter() != 0) {
                LOG.info(result.getResultString("ESC-DOCUMENT-QUEUE"));
            }
        } else {
            LOG.debug("ESC-DOCUMENT-QUEUE: Scheduler is disabled.");
        }
    }

    @RunAs(username = "bs-esc@dvbern.ch", roles = { BerechtigungName.SYSTEM_ADMIN })
    @Transactional(TxType.NOT_SUPPORTED)
    public void runDocumentQueueCleanupJob(boolean forcerun) {
        final SchedulerConfig config = schedulerConfigService.getSchedulerConfig(SchedulerTyp.DOCUMENT_QUEUE_CLEAN_UP);
        if (!config.isDisabled() || forcerun) {
            SchedulerProcessingResult result = documentQueueRunnerService.performDocumentQueueCleanup(config);
            LOG.info(result.getResultString("ESC-DOCUMENT-QUEUE-CLEAN-UP"));
        } else {
            LOG.debug("ESC-DOCUMENT-QUEUE-CLEAN-UP: Scheduler Clean up is disabled.");
        }
    }

    @RunAs(username = "bs-esc@dvbern.ch", roles = { BerechtigungName.SYSTEM_ADMIN })
    @Transactional(TxType.NOT_SUPPORTED)
    public void runKinderInaktivierenJob(boolean forcerun) {
        final SchedulerConfig config = schedulerConfigService.getSchedulerConfig(SchedulerTyp.KINDER_INAKTIVIEREN);
        LOG.info("ESC-DEACTIVATE-KINDER: Start");
        if (!config.isDisabled() || forcerun) {
            int monthsUnused = configService.getInactivateKinderAfterInactiveMoths();
            int manualKinderInactivationAge = configService.getGetInactiveKinderManualKinderInactivationAge();

            List<ID<Kind>> kinderToDeactivate = this.kindDeactivationService.findKinderToDeactivate(
                monthsUnused,
                manualKinderInactivationAge,
                config.getBatchsize()
            );
            SchedulerProcessingResult
                result = new SchedulerProcessingResult(config.getBatchsize(), 0, kinderToDeactivate.size());
            if (!kinderToDeactivate.isEmpty()) {
                LOG.info(
                    "ESC-DEACTIVATE-KINDER: Found {} Kinder that meet conditions to set INAKTIV status",
                    kinderToDeactivate.size());

                long anzahlDeaktiviert = 0;
                for (ID<Kind> kindID : kinderToDeactivate) {
                    try {
                        kindDeactivationService.setKindAktivStatusForKind(
                            kindID,
                            EntityAktivStatus.INAKTIV);
                        anzahlDeaktiviert += 1;
                    } catch (Exception e) {
                        LOG.error(
                            "ESC-DEACTIVATE-KINDER: Error while setting INAKTIV status for Kind with ID: {}",
                            kindID, e);

                    }
                }
                result.setSuccessCounter(anzahlDeaktiviert);
            }
            LOG.info(result.getResultString("ESC-DEACTIVATE-KINDER"));
        } else {
            LOG.debug("ESC-DEACTIVATE-KINDER: Scheduler is disabled.");
        }
    }

    @Transactional(TxType.NOT_SUPPORTED)
    @RunAs(username = "bs-esc@dvbern.ch", roles = { BerechtigungName.SYSTEM_ADMIN })
    public void normalizeKindNamen() {
        List<ID<Kind>> ids = kindService.findAllIds();
        LOG.info("ESC-NORMALIZE-KIND-NAMEN: Start normalizing {} Kinder", ids.size());
        for (ID<Kind> id : ids) {
            try {
                kindService.normalizeKindName(id);
            } catch (Exception e) {
                LOG.info("ESC-NORMALIZE-KIND-NAMEN: Error normalizing Kind {}", id);
            }

        }
        LOG.info("ESC-NORMALIZE-KIND-NAMEN: Finished normalizing {} Kinder", ids.size());
    }

    @Transactional(TxType.NOT_SUPPORTED)
    @RunAs(username = "bs-esc@dvbern.ch", roles = { BerechtigungName.SYSTEM_ADMIN })
    public void runReloadDuplikateJob(boolean forcerun) {
        final SchedulerConfig config = schedulerConfigService.getSchedulerConfig(SchedulerTyp.RELOAD_DUPLIKATE);
        if (!config.isDisabled() || forcerun) {
            // Alle bestehende Duplikate pruefen, ob sie noch aktuell sind
            reevaluateAllExistingDuplikate();
            // Alle Kinder auf Duplikate prüfen
            reevaluateAllKinder();
        } else {
            LOG.debug("ESC-RELOAD_DUPLIKATE: Scheduler is disabled.");
        }
    }

    @Transactional(TxType.NOT_SUPPORTED)
    @RunAs(username = "bs-esc@dvbern.ch", roles = { BerechtigungName.SYSTEM_ADMIN })
    public void runDeactivateImpfstoffChargenJob(boolean forcerun) {
        final SchedulerConfig config =
            schedulerConfigService.getSchedulerConfig(SchedulerTyp.DEACTIVATE_IMPFSTOFF_CHARGEN);
        if (!config.isDisabled() || forcerun) {
            int monthsUnused = configService.getDeactivateImpfstoffChargenAfterUnusedMonths();
            SchedulerProcessingResult result = impfungInternService.deactivateImpfstoffChargeIfNotUsedSince(
                monthsUnused,
                config.getBatchsize()
            );
            LOG.info(result.getResultString("ESC_DEACTIVATE_IMPFSTOFF_CHARGEN"));
        } else {
            LOG.debug("ESC_DEACTIVATE_IMPFSTOFF_CHARGEN: Scheduler is disabled.");
        }
    }

    @Transactional(TxType.NOT_SUPPORTED)
    @RunAs(username = "bs-esc@dvbern.ch", roles = { BerechtigungName.SYSTEM_ADMIN })
    public void runDeleteImpfstoffChargenJob(boolean forcerun) {
        final SchedulerConfig config = schedulerConfigService.getSchedulerConfig(SchedulerTyp.DELETE_IMPFSTOFF_CHARGEN);
        if (!config.isDisabled() || forcerun) {
            int monthsUnreferenced = configService.getDeleteImpfstoffChargenAfterUnreferencedMonths();
            SchedulerProcessingResult result = impfungInternService.deleteImpfstoffChargeIfNotReferencedSince(
                monthsUnreferenced,
                config.getBatchsize()
            );
            LOG.info(result.getResultString("ESC_DELETE_IMPFSTOFF_CHARGEN"));
        } else {
            LOG.debug("ESC_DELETE_IMPFSTOFF_CHARGEN: Scheduler is disabled.");
        }
    }

    @Transactional(TxType.NOT_SUPPORTED)
    @RunAs(username = "be-esc@dvbern.ch", roles = { BerechtigungName.SYSTEM_ADMIN })
    public void runArchiveDocumentDownloadEmailReminderJob(boolean forcerun) {
        final SchedulerConfig config =
            schedulerConfigService.getSchedulerConfig(SchedulerTyp.ARCHIVE_DOC_DOWNLOAD_REMINDER);
        if (!config.isDisabled() || forcerun) {
            archivdokumentService.sendArchiveDocumentDownloadReminder();
            LOG.info("ARCHIVE_DOC_DOWNLOAD_REMINDER: beendet");
        } else {
            LOG.debug("ARCHIVE_DOC_DOWNLOAD_REMINDER: Scheduler is disabled.");
        }
    }

    @RunAs(username = "Irrelevant solange Rolle SYSTEM_ADMIN", roles = { BerechtigungName.SYSTEM_ADMIN })
    @Transactional(TxType.NOT_SUPPORTED)
    public void runMarkForArchivierungJob(boolean forcerun) {
        final SchedulerConfig config = schedulerConfigService.getSchedulerConfig(SchedulerTyp.MARK_FOR_ARCHIVIERUNG);
        if (!config.isDisabled() || forcerun) {
            List<ID<Dossier>> dossiersToArchive = archivierungRuleService.findDossiersToArchive(config.getBatchsize());
            for (ID<Dossier> dossierToArchive : dossiersToArchive) {
                dossierQueueService.createDossierQueueItem(dossierToArchive, DossierQueueTyp.ARCHIVIERUNG, null);
            }
            SchedulerProcessingResult
                result = new SchedulerProcessingResult(config.getBatchsize(), 0, dossiersToArchive.size());
            if (!dossiersToArchive.isEmpty()) {
                LOG.info(
                    "ESC-ARCHIVIERUNG: Found {} Dossiers to archive",
                    dossiersToArchive.size());
                result.setSuccessCounter(dossiersToArchive.size());
            }
            LOG.info(result.getResultString("ESC-ARCHIVIERUNG"));
        } else {
            LOG.debug("ESC-ARCHIVIERUNG: Scheduler is disabled.");
        }
    }

    @RunAs(username = "Irrelevant solange Rolle SYSTEM_ADMIN", roles = { BerechtigungName.SYSTEM_ADMIN })
    @Transactional(TxType.NOT_SUPPORTED)
    public void runDossierQueueProcessingJob(boolean forcerun) {
        final SchedulerConfig config = schedulerConfigService.getSchedulerConfig(SchedulerTyp.DOSSIER_QUEUE_PROCESSING);
        if (!config.isDisabled() || forcerun) {
            SchedulerProcessingResult result = dossierQueueRunnerService.processQueueItems(config);
            if (result.getTotalCounter() != 0) {
                LOG.info(result.getResultString("ESC-DOSSIER_QUEUE_PROCESSING"));
            }
        } else {
            LOG.debug("ESC-DOSSIER_QUEUE_PROCESSING: DossierQueue Scheduler is disabled.");
        }
    }

    @RunAs(username = "Irrelevant solange Rolle SYSTEM_ADMIN", roles = { BerechtigungName.SYSTEM_ADMIN })
    @Transactional(TxType.NOT_SUPPORTED)
    public void runDossierQueueCleanupJob(boolean forcerun) {
        final SchedulerConfig config = schedulerConfigService.getSchedulerConfig(SchedulerTyp.DOSSIER_QUEUE_CLEAN_UP);
        if (!config.isDisabled() || forcerun) {
            SchedulerProcessingResult result = dossierQueueRunnerService.performDossierQueueCleanup(config);
            LOG.info(result.getResultString("ESC-DOSSIER_QUEUE_CLEAN_UP"));
        } else {
            LOG.debug("ESC-DOSSIER_QUEUE_CLEAN_UP: DossierQueue Clean up Scheduler is disabled.");
        }
    }

    @Transactional(TxType.NOT_SUPPORTED)
    @RunAs(username = "be-esc@dvbern.ch", roles = { BerechtigungName.SYSTEM_ADMIN })
    public void runArchiveDocumentKindDeleteJob(boolean forcerun) {
        final SchedulerConfig config = schedulerConfigService.getSchedulerConfig(SchedulerTyp.ARCHIVIERUNG_KIND_DELETE);
        if (!config.isDisabled() || forcerun) {
            List<ID<Dossier>> dossiersToDelete = archivierungRuleService
                .findDossiersToDelete(configService.getArchiveMonthsBeforeDeletion(), config.getBatchsize());
            for (ID<Dossier> dossierToDelete : dossiersToDelete) {
                dossierQueueService.createDossierQueueItem(dossierToDelete, DossierQueueTyp.ARCHIVIERUNG_DELETE, null);
            }
            SchedulerProcessingResult
                result = new SchedulerProcessingResult(config.getBatchsize(), 0, dossiersToDelete.size());
            if (!dossiersToDelete.isEmpty()) {
                LOG.info(
                    "ESC-ARCHIVIERUNG-DELETE: Found {} Dossiers to delete",
                    dossiersToDelete.size());
                result.setSuccessCounter(dossiersToDelete.size());
            }
            LOG.info(result.getResultString("ESC-ARCHIVIERUNG-DELETE"));
        } else {
            LOG.debug("ESC-ARCHIVIERUNG-DELETE: Scheduler is disabled.");
        }
    }

    @Transactional(TxType.NOT_SUPPORTED)
    @RunAs(username = "be-esc@dvbern.ch", roles = { BerechtigungName.SYSTEM_ADMIN })
    public void runAuditRetentionCleanupJob(boolean forcerun) {
        final SchedulerConfig config = schedulerConfigService.getSchedulerConfig(SchedulerTyp.AUD_TABELLEN_BEREINIGEN);
        if (!config.isDisabled() || forcerun) {
            SchedulerProcessingResult result =
                auditRetentionService.cleanupOldAuditRevisions(configService.getAudRetentionYears(), config.getBatchsize());
            LOG.info(result.getResultString("ESC-AUD-RETENTION"));
        } else {
            LOG.debug("ESC-AUD-RETENTION: Scheduler is disabled.");
        }
    }

    @Transactional(TxType.NOT_SUPPORTED)
    @RunAs(username = "bs-esc@dvbern.ch", roles = { BerechtigungName.SYSTEM_ADMIN })
    public void runFileStoreHealthCheckJob(boolean forcerun) {
        if (!configService.isFilestoreHealthcheckEnabled() && !forcerun) {
            LOG.debug("ESC-FILESTORE-HEALTHCHECK: disabled");
            return;
        }
        fileStoreHealthCheckService.runForCurrentTenant();
    }

    private void reevaluateAllExistingDuplikate() {
        List<Duplikat> duplikate = duplikatService.findUnmergedDuplikate();
        LOG.info("ESC-RELOAD_DUPLIKATE: Checking {} existing Duplikate", duplikate.size());
        int removedCounter = 0;
        for (Duplikat duplikat : duplikate) {
            try {
                if (duplikat.getKind1() == null || duplikat.getKind2() == null) {
                    Validate.isTrue(duplikat.getDuplikatStatus() != DuplikatStatus.OFFEN);
                    continue;
                }
                removedCounter += duplikatService.reevaluateExistingDuplikat(duplikat);
            } catch (Exception e) {
                LOG.error("ESC-RELOAD_DUPLIKATE: Error while reloading duplicate with ID: {}", duplikat.getId(), e);
            }
        }
        LOG.info(
            "ESC-RELOAD_DUPLIKATE: Finished checking existing Duplikate. We removed {} Duplikat(e) ",
            removedCounter);
    }

    private void reevaluateAllKinder() {
        List<ID<Kind>> ids = kindService.findAllIds();
        LOG.info("ESC-RELOAD_DUPLIKATE: Checking {} Kinder for potentially being a Duplikat", ids.size());
        int successCounter = 0;
        for (ID<Kind> id : ids) {
            try {
                successCounter += duplikatService.reevaluateKind(id);
            } catch (Exception e) {
                LOG.error("ESC-RELOAD_DUPLIKATE: Error while reloading duplicates for Kind with ID: {}", id, e);
            }
        }
        LOG.info(
            "ESC-RELOAD_DUPLIKATE: Finished checking for duplicates. We analyzed {} Kinder and created {} new "
                + "Duplikate.",
            ids.size(), successCounter);
    }

}

