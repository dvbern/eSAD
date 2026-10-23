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

import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import jakarta.persistence.EntityManager;

import ch.dvbern.oss.hemed.esc.api.shared.scheduler.SchedulerProcessingResult;
import ch.dvbern.oss.hemed.esc.api.testutil.AbstractITest;
import ch.dvbern.oss.hemed.esc.api.testutil.security.TestAsAdminBS;

import io.quarkus.narayana.jta.QuarkusTransaction;
import io.quarkus.test.junit.QuarkusMock;
import io.quarkus.test.junit.QuarkusTest;
import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentMatchers;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.spy;

@QuarkusTest
@RequiredArgsConstructor
class AuditRetentionServiceITest extends AbstractITest {

    private static final int RETENTION_YEARS = 20;
    private static final String AUD_TABLE_1 = "quartier_aud";
    private static final String AUD_TABLE_2 = "schedulerconfig_aud";
    private static final String REVINFO_TABLE = "revinfo";

    private final AuditRetentionService auditRetentionService;
    private final AuditRetentionCleanupHelperService auditRetentionCleanupHelperService;
    private final EntityManager entityManager;

    private final Set<Long> createdRevisionIds = ConcurrentHashMap.newKeySet();

    @AfterEach
    void tearDown() {
        if (createdRevisionIds.isEmpty()) {
            return;
        }

        QuarkusTransaction.requiringNew().run(() -> {
            for (String table : List.of(AUD_TABLE_1, AUD_TABLE_2, REVINFO_TABLE)) {
                entityManager
                    .createNativeQuery("DELETE FROM " + table + " WHERE rev IN (:revs)")
                    .setParameter("revs", createdRevisionIds)
                    .executeUpdate();
            }
        });
        createdRevisionIds.clear();
    }

    @Test
    @TestAsAdminBS
    void cleanupOldAuditRevisions_should_delete_old_revisions_in_all_aud_tables() {
        // ARRANGE
        long cutoff = cutoffMillis();
        long oldRev1 = insertRevision(cutoff - 3000);
        long oldRev2 = insertRevision(cutoff - 2000);
        insertAudRow(AUD_TABLE_1, oldRev1);
        insertAudRow(AUD_TABLE_2, oldRev1);
        insertAudRow(AUD_TABLE_1, oldRev2);

        // ACT
        SchedulerProcessingResult result = auditRetentionService.cleanupOldAuditRevisions(RETENTION_YEARS, 100);

        // ASSERT
        assertThat(result.getSuccessCounter()).isEqualTo(result.getTotalCounter());
        assertThat(result.getTotalCounter()).isGreaterThanOrEqualTo(2);
        assertThat(countRows(AUD_TABLE_1, oldRev1)).isZero();
        assertThat(countRows(AUD_TABLE_2, oldRev1)).isZero();
        assertThat(countRows(AUD_TABLE_1, oldRev2)).isZero();
        assertThat(countRows(REVINFO_TABLE, oldRev1)).isZero();
        assertThat(countRows(REVINFO_TABLE, oldRev2)).isZero();
    }

    @Test
    @TestAsAdminBS
    void cleanupOldAuditRevisions_should_not_touch_revisions_after_cutoff() {
        // ARRANGE
        long cutoff = cutoffMillis();
        long oldRev = insertRevision(cutoff - 2000);
        long newRev = insertRevision(cutoff + 2000);
        insertAudRow(AUD_TABLE_1, oldRev);
        insertAudRow(AUD_TABLE_1, newRev);
        insertAudRow(AUD_TABLE_2, newRev);

        // ACT
        auditRetentionService.cleanupOldAuditRevisions(RETENTION_YEARS, 100);

        // ASSERT
        assertThat(countRows(REVINFO_TABLE, oldRev)).isZero();
        assertThat(countRows(REVINFO_TABLE, newRev)).isEqualTo(1);
        assertThat(countRows(AUD_TABLE_1, newRev)).isEqualTo(1);
        assertThat(countRows(AUD_TABLE_2, newRev)).isEqualTo(1);
    }

    @Test
    @TestAsAdminBS
    void cleanupOldAuditRevisions_should_drain_backlog_across_multiple_batches() {
        // ARRANGE: batchsize von 1 erzwingt mehrere Batch-Durchlaeufe, trotzdem muss der gesamte
        // Rueckstau in einem Aufruf abgearbeitet werden ("drain to empty").
        long cutoff = cutoffMillis();
        long oldestRev = insertRevision(cutoff - 3000);
        long olderRev = insertRevision(cutoff - 2000);
        insertAudRow(AUD_TABLE_1, oldestRev);
        insertAudRow(AUD_TABLE_1, olderRev);

        // ACT
        SchedulerProcessingResult result = auditRetentionService.cleanupOldAuditRevisions(RETENTION_YEARS, 1);

        // ASSERT
        assertThat(result.getBatchsize()).isEqualTo(1);
        assertThat(result.getTotalCounter()).isEqualTo(2);
        assertThat(result.getSuccessCounter()).isEqualTo(2);
        assertThat(countRows(REVINFO_TABLE, oldestRev)).isZero();
        assertThat(countRows(REVINFO_TABLE, olderRev)).isZero();
        assertThat(countRows(AUD_TABLE_1, olderRev)).isZero();
    }

    @Test
    @TestAsAdminBS
    void cleanupOldAuditRevisions_should_return_empty_result_if_nothing_to_cleanup() {
        // ARRANGE
        long newRev = insertRevision(cutoffMillis() + 2000);
        insertAudRow(AUD_TABLE_1, newRev);

        // ACT
        SchedulerProcessingResult result = auditRetentionService.cleanupOldAuditRevisions(RETENTION_YEARS, 100);

        // ASSERT
        assertThat(result.getTotalCounter()).isZero();
        assertThat(result.getSuccessCounter()).isZero();
        assertThat(countRows(REVINFO_TABLE, newRev)).isEqualTo(1);
    }

    @Test
    @TestAsAdminBS
    void cleanupOldAuditRevisions_should_continue_after_error_on_single_revision() {
        // ARRANGE
        long cutoff = cutoffMillis();
        long failingRev = insertRevision(cutoff - 3000);
        long succeedingRev = insertRevision(cutoff - 2000);
        insertAudRow(AUD_TABLE_1, failingRev);
        insertAudRow(AUD_TABLE_1, succeedingRev);

        AuditRetentionCleanupHelperService helperSpy = spy(auditRetentionCleanupHelperService);
        doThrow(new IllegalStateException("Simulierter Fehler"))
            .when(helperSpy)
            .cleanupRevision(ArgumentMatchers.anyList(), ArgumentMatchers.eq(failingRev));
        QuarkusMock.installMockForType(helperSpy, AuditRetentionCleanupHelperService.class);

        // ACT
        SchedulerProcessingResult result = auditRetentionService.cleanupOldAuditRevisions(RETENTION_YEARS, 100);

        // ASSERT: die fehlerhafte Revision blockiert die Verarbeitung der anderen Revision nicht, wird aber
        // nicht geloescht. Sie wird danach in einem weiteren Batch erneut versucht (totalCounter zaehlt sie somit
        // zweimal); da dieser Folge-Batch keinen Fortschritt mehr macht, bricht die Drain-Schleife ab.
        assertThat(result.getTotalCounter()).isEqualTo(3);
        assertThat(result.getSuccessCounter()).isEqualTo(1);
        assertThat(countRows(REVINFO_TABLE, failingRev)).isEqualTo(1);
        assertThat(countRows(AUD_TABLE_1, failingRev)).isEqualTo(1);
        assertThat(countRows(REVINFO_TABLE, succeedingRev)).isZero();
        assertThat(countRows(AUD_TABLE_1, succeedingRev)).isZero();
    }

    @Test
    @TestAsAdminBS
    void cleanupRevision_should_delete_aud_rows_before_revinfo() {
        // ARRANGE
        long rev = insertRevision(cutoffMillis() - 2000);
        insertAudRow(AUD_TABLE_1, rev);
        insertAudRow(AUD_TABLE_2, rev);

        // ACT
        int deletedRows = auditRetentionCleanupHelperService.cleanupRevision(List.of(AUD_TABLE_1, AUD_TABLE_2), rev);

        // ASSERT
        assertThat(deletedRows).isEqualTo(3);
        assertThat(countRows(AUD_TABLE_1, rev)).isZero();
        assertThat(countRows(AUD_TABLE_2, rev)).isZero();
        assertThat(countRows(REVINFO_TABLE, rev)).isZero();
    }

    private long cutoffMillis() {
        return getAppClock().offsetDateTime().now().minusYears(RETENTION_YEARS).toInstant().toEpochMilli();
    }

    private long insertRevision(long revtstmp) {
        return QuarkusTransaction.requiringNew().call(() -> {
            long rev = nextFreeRevision();
            entityManager
                .createNativeQuery("INSERT INTO revinfo (rev, revtstmp) VALUES (:rev, :revtstmp)")
                .setParameter("rev", rev)
                .setParameter("revtstmp", revtstmp)
                .executeUpdate();
            createdRevisionIds.add(rev);
            return rev;
        });
    }

    private long nextFreeRevision() {
        Object result = entityManager
            .createNativeQuery("SELECT COALESCE(MAX(rev), 0) + 1 FROM revinfo")
            .getSingleResult();
        return ((Number) result).longValue();
    }

    private void insertAudRow(String tableName, long rev) {
        QuarkusTransaction.requiringNew().run(() -> {
            entityManager
                .createNativeQuery("INSERT INTO " + tableName + " (id, rev, revtype) VALUES (CAST(:id AS uuid), :rev, 0)")
                .setParameter("id", UUID.randomUUID().toString())
                .setParameter("rev", rev)
                .executeUpdate();
        });
    }

    private long countRows(String tableName, long rev) {
        return QuarkusTransaction.requiringNew().call(() -> {
            Object result = entityManager
                .createNativeQuery("SELECT COUNT(*) FROM " + tableName + " WHERE rev = :rev")
                .setParameter("rev", rev)
                .getSingleResult();
            return ((Number) result).longValue();
        });
    }
}
