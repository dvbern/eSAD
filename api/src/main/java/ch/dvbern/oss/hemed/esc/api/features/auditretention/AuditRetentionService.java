/*
 * Copyright (C) 2026 DV Bern AG, Switzerland
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package ch.dvbern.oss.hemed.esc.api.features.auditretention;

import java.time.OffsetDateTime;
import java.util.List;
import jakarta.transaction.Transactional;
import jakarta.transaction.Transactional.TxType;

import ch.dvbern.dvbstarter.infrastructure.stereotypes.Service;
import ch.dvbern.dvbstarter.shared.clock.AppClock;
import ch.dvbern.oss.hemed.esc.api.shared.scheduler.SchedulerProcessingResult;
import ch.dvbern.oss.hemed.esc.api.shared.types.IgnoreForIdClassTest;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.checkerframework.checker.nullness.qual.NonNull;

/**
 * Bereinigt die von Hibernate Envers geschriebenen Historisierungsdaten ({@code *_aud} und {@code revinfo}), sobald
 * sie älter sind als die konfigurierte Aufbewahrungsfrist.
 * <p>
 * Der Ablauf startet immer bei der Revisionstabelle {@code revinfo}: Es werden batchweise die ältesten Revisionen
 * vor dem Cutoff ermittelt und anschliessend pro Revision ("Commit") zuerst alle zugehörigen {@code _aud}-Zeilen und
 * erst danach die {@code revinfo}-Zeile selbst gelöscht.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class AuditRetentionService {

    private static final String LOG_PREFIX = "ESC-AUD-RETENTION";

    private final AuditRetentionRepo auditRetentionRepo;
    private final AuditRetentionCleanupHelperService auditRetentionCleanupHelperService;
    private final AppClock appClock;

    /**
     * Löscht alle Envers-Revisionen, welche älter sind als die Aufbewahrungsfrist, batchweise.
     *
     * Die Methode arbeitet den gesamten Rückstand ab ("drain to empty"): es werden solange Batches à
     * {@code batchsize} Revisionen geholt und verarbeitet, bis keine Revision mehr vor dem Cutoff liegt. Dadurch
     * kann der Job seltener laufen (z.B. wöchentlich), ohne dass sich ein Rückstau bildet.
     * <p>
     * Die Methode läuft bewusst ohne eigene Transaktion (der aufrufende Scheduler-Job ist bereits
     * {@code NOT_SUPPORTED}), damit jede Revision in ihrer eigenen Transaktion verarbeitet wird.
     *
     * @param retentionYears Aufbewahrungsfrist in Jahren
     * @param batchsize maximale Anzahl Revisionen, welche pro Batch geholt und verarbeitet werden
     */
    @NonNull
    @Transactional(TxType.NOT_SUPPORTED)
    public SchedulerProcessingResult cleanupOldAuditRevisions(int retentionYears, int batchsize) {
        SchedulerProcessingResult result = new SchedulerProcessingResult(batchsize, 0, 0);

        long cutoffEpochMillis = calculateCutoffEpochMillis(retentionYears);

        // Die Tabellennamen werden bewusst nur einmal pro Lauf aus dem DB-Katalog gelesen
        List<String> audTableNames = auditRetentionRepo.findAudTableNames();

        List<Long> revisionIds;
        boolean hasProgress = true;

        do {
            revisionIds = auditRetentionRepo.findOldRevisionIds(cutoffEpochMillis, batchsize);

            if (!revisionIds.isEmpty()) {
                LOG.info(
                    "{}: Bereinige Batch von {} Revisionen aelter als {} Jahre in {} Historisierungstabellen",
                    LOG_PREFIX,
                    revisionIds.size(),
                    retentionYears,
                    audTableNames.size());

                result.setTotalCounter(result.getTotalCounter() + revisionIds.size());
                long successesInBatch = 0;
                for (Long revisionId : revisionIds) {
                    if (cleanupRevision(audTableNames, revisionId)) {
                        successesInBatch++;
                    }
                }
                result.setSuccessCounter(result.getSuccessCounter() + successesInBatch);

                // Kein Fortschritt in diesem Batch (z.B. dieselbe fehlerhafte Revision wird immer wieder
                // zurueckgegeben): Abbruch, um eine Endlosschleife zu verhindern. Der Fehler wurde bereits geloggt.
                hasProgress = successesInBatch > 0;
            }
        } while (!revisionIds.isEmpty() && hasProgress);

        return result;
    }

    /**
     * Löscht die Historisierungsdaten einer einzelnen Revision. Ein Fehler wird geloggt, damit die nachfolgenden
     * Revisionen trotzdem verarbeitet werden.
     */
    private boolean cleanupRevision(@NonNull List<String> audTableNames, @IgnoreForIdClassTest long revisionId) {
        try {
            int deletedRows = auditRetentionCleanupHelperService.cleanupRevision(audTableNames, revisionId);
            LOG.debug("{}: Revision {} bereinigt, {} Zeilen geloescht", LOG_PREFIX, revisionId, deletedRows);
            return true;
        } catch (Exception e) {
            LOG.error("{}: Fehler beim Bereinigen der Revision {}", LOG_PREFIX, revisionId, e);
            return false;
        }
    }

    private long calculateCutoffEpochMillis(int retentionYears) {
        OffsetDateTime cutoff = appClock.offsetDateTime().now().minusYears(retentionYears);

        return cutoff.toInstant().toEpochMilli();
    }
}
