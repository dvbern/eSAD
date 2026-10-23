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
import java.util.UUID;

import ch.dvbern.dvbstarter.infrastructure.errorhandling.AppFailureException;
import ch.dvbern.oss.hemed.esc.api.testutil.AbstractITest;
import ch.dvbern.oss.hemed.esc.api.testutil.security.TestAsAdminBS;

import io.quarkus.test.TestTransaction;
import io.quarkus.test.junit.QuarkusTest;
import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@QuarkusTest
@RequiredArgsConstructor
class AuditRetentionRepoITest extends AbstractITest {

    private static final String AUD_TABLE_1 = "quartier_aud";
    private static final String AUD_TABLE_2 = "schedulerconfig_aud";

    private final AuditRetentionRepo auditRetentionRepo;
    private final EntityManager entityManager;

    @Test
    @TestAsAdminBS
    @TestTransaction
    void findAudTableNames_should_find_all_envers_tables_dynamically() {
        // ACT
        List<String> audTableNames = auditRetentionRepo.findAudTableNames();

        // ASSERT
        assertThat(audTableNames)
            .isNotEmpty()
            .contains(AUD_TABLE_1, AUD_TABLE_2, "kind_aud")
            .doesNotContain("revinfo", "kind")
            .allSatisfy(tableName -> assertThat(tableName).endsWith("_aud"))
            .doesNotHaveDuplicates()
            .isSorted();
    }

    @Test
    @TestAsAdminBS
    @TestTransaction
    void findOldRevisionIds_should_only_return_revisions_before_cutoff() {
        // ARRANGE
        long cutoff = cutoffMillis();
        long oldestRev = insertRevision(cutoff - 3000);
        long olderRev = insertRevision(cutoff - 2000);
        long newRev = insertRevision(cutoff + 2000);

        // ACT
        List<Long> result = auditRetentionRepo.findOldRevisionIds(cutoff, 100);

        // ASSERT
        assertThat(result)
            .contains(oldestRev, olderRev)
            .doesNotContain(newRev);
    }

    @Test
    @TestAsAdminBS
    @TestTransaction
    void findOldRevisionIds_should_respect_batchsize_and_return_oldest_first() {
        // ARRANGE
        long cutoff = cutoffMillis();
        long oldestRev = insertRevision(cutoff - 3000);
        long olderRev = insertRevision(cutoff - 2000);

        // ACT
        List<Long> result = auditRetentionRepo.findOldRevisionIds(cutoff, 1);

        // ASSERT
        assertThat(result)
            .containsExactly(oldestRev)
            .doesNotContain(olderRev);
    }

    @Test
    @TestAsAdminBS
    @TestTransaction
    void deleteAudRowsForRevision_should_delete_rows_of_given_revision_in_all_aud_tables() {
        // ARRANGE
        long cutoff = cutoffMillis();
        long oldRev = insertRevision(cutoff - 2000);
        long newRev = insertRevision(cutoff + 2000);
        insertAudRow(AUD_TABLE_1, oldRev);
        insertAudRow(AUD_TABLE_2, oldRev);
        insertAudRow(AUD_TABLE_1, newRev);

        // ACT
        int deletedTable1 = auditRetentionRepo.deleteAudRowsForRevision(AUD_TABLE_1, oldRev);
        int deletedTable2 = auditRetentionRepo.deleteAudRowsForRevision(AUD_TABLE_2, oldRev);

        // ASSERT
        assertThat(deletedTable1).isEqualTo(1);
        assertThat(deletedTable2).isEqualTo(1);
        assertThat(countAudRows(AUD_TABLE_1, oldRev)).isZero();
        assertThat(countAudRows(AUD_TABLE_2, oldRev)).isZero();
        assertThat(countAudRows(AUD_TABLE_1, newRev)).isEqualTo(1);
    }

    @Test
    @TestAsAdminBS
    @TestTransaction
    void deleteRevisionInfo_should_delete_only_the_given_revision() {
        // ARRANGE
        long cutoff = cutoffMillis();
        long oldRev = insertRevision(cutoff - 2000);
        long newRev = insertRevision(cutoff + 2000);
        insertAudRow(AUD_TABLE_1, oldRev);

        // ACT
        auditRetentionRepo.deleteAudRowsForRevision(AUD_TABLE_1, oldRev);
        int deletedRevisions = auditRetentionRepo.deleteRevisionInfo(oldRev);

        // ASSERT
        assertThat(deletedRevisions).isEqualTo(1);
        assertThat(countRevisions(oldRev)).isZero();
        assertThat(countRevisions(newRev)).isEqualTo(1);
        assertThat(auditRetentionRepo.findOldRevisionIds(cutoff, 100)).doesNotContain(oldRev);
    }

    @Test
    @TestAsAdminBS
    @TestTransaction
    void deleteAudRowsForRevision_should_reject_tables_that_are_no_aud_tables() {
        assertThatThrownBy(() -> auditRetentionRepo.deleteAudRowsForRevision("kind", 1L))
            .isInstanceOf(AppFailureException.class);

        assertThatThrownBy(() -> auditRetentionRepo.deleteAudRowsForRevision("kind_aud; DROP TABLE kind", 1L))
            .isInstanceOf(AppFailureException.class);
    }

    private long cutoffMillis() {
        return OffsetDateTime.now().minusYears(20).toInstant().toEpochMilli();
    }

    private long insertRevision(long revtstmp) {
        long rev = nextFreeRevision();
        entityManager
            .createNativeQuery("INSERT INTO revinfo (rev, revtstmp) VALUES (:rev, :revtstmp)")
            .setParameter("rev", rev)
            .setParameter("revtstmp", revtstmp)
            .executeUpdate();
        return rev;
    }

    private long nextFreeRevision() {
        Object result = entityManager
            .createNativeQuery("SELECT COALESCE(MAX(rev), 0) + 1 FROM revinfo")
            .getSingleResult();
        return ((Number) result).longValue();
    }

    private void insertAudRow(String tableName, long rev) {
        entityManager
            .createNativeQuery("INSERT INTO " + tableName + " (id, rev, revtype) VALUES (CAST(:id AS uuid), :rev, 0)")
            .setParameter("id", UUID.randomUUID().toString())
            .setParameter("rev", rev)
            .executeUpdate();
    }

    private long countAudRows(String tableName, long rev) {
        Object result = entityManager
            .createNativeQuery("SELECT COUNT(*) FROM " + tableName + " WHERE rev = :rev")
            .setParameter("rev", rev)
            .getSingleResult();
        return ((Number) result).longValue();
    }

    private long countRevisions(long rev) {
        Object result = entityManager
            .createNativeQuery("SELECT COUNT(*) FROM revinfo WHERE rev = :rev")
            .setParameter("rev", rev)
            .getSingleResult();
        return ((Number) result).longValue();
    }
}
