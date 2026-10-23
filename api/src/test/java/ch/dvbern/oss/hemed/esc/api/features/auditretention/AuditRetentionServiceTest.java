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

import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.Collections;
import java.util.List;

import ch.dvbern.dvbstarter.shared.clock.AppClock;
import ch.dvbern.dvbstarter.shared.clock.FixedClockProvider;
import ch.dvbern.oss.hemed.esc.api.shared.scheduler.SchedulerProcessingResult;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayNameGeneration;
import org.junit.jupiter.api.DisplayNameGenerator;
import org.junit.jupiter.api.Test;
import org.mockito.InOrder;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@DisplayNameGeneration(DisplayNameGenerator.ReplaceUnderscores.class)
class AuditRetentionServiceTest {

    private static final int RETENTION_YEARS = 20;
    private static final List<String> AUD_TABLES = List.of("quartier_aud", "schedulerconfig_aud");

    private final AuditRetentionRepo auditRetentionRepo = mock(AuditRetentionRepo.class);
    private final AuditRetentionCleanupHelperService cleanupHelperService = mock(AuditRetentionCleanupHelperService.class);

    private final FixedClockProvider clockProvider = FixedClockProvider.now();
    private final AppClock appClock = new AppClock(clockProvider);

    private final AuditRetentionService auditRetentionService =
        new AuditRetentionService(auditRetentionRepo, cleanupHelperService, appClock);

    @BeforeEach
    void setUp() {
        OffsetDateTime fixedNow = OffsetDateTime.of(2026, 9, 23, 12, 0, 0, 0, ZoneOffset.UTC);
        clockProvider.resetUsing(fixedNow.toInstant());

        given(auditRetentionRepo.findAudTableNames()).willReturn(AUD_TABLES);
    }

    @Test
    void cleanupOldAuditRevisions_calculates_cutoff_and_cleans_up_revisions() {
        // ARRANGE
        Instant expectedCutoff = OffsetDateTime.of(2006, 9, 23, 12, 0, 0, 0, ZoneOffset.UTC).toInstant();
        long cutoffEpochMillis = expectedCutoff.toEpochMilli();

        given(auditRetentionRepo.findOldRevisionIds(cutoffEpochMillis, 100))
            .willReturn(List.of(101L, 102L))
            .willReturn(Collections.emptyList());

        given(cleanupHelperService.cleanupRevision(AUD_TABLES, 101L)).willReturn(3);
        given(cleanupHelperService.cleanupRevision(AUD_TABLES, 102L)).willReturn(2);

        // ACT
        SchedulerProcessingResult result = auditRetentionService.cleanupOldAuditRevisions(RETENTION_YEARS, 100);

        // ASSERT
        assertThat(result.getBatchsize()).isEqualTo(100);
        assertThat(result.getTotalCounter()).isEqualTo(2);
        assertThat(result.getSuccessCounter()).isEqualTo(2);

        verify(auditRetentionRepo, times(1)).findAudTableNames();
        verify(cleanupHelperService).cleanupRevision(AUD_TABLES, 101L);
        verify(cleanupHelperService).cleanupRevision(AUD_TABLES, 102L);
    }

    @Test
    void cleanupOldAuditRevisions_drains_backlog_across_multiple_batches() {
        // ARRANGE
        long cutoffEpochMillis = OffsetDateTime.of(2006, 9, 23, 12, 0, 0, 0, ZoneOffset.UTC)
            .toInstant()
            .toEpochMilli();

        given(auditRetentionRepo.findOldRevisionIds(cutoffEpochMillis, 1))
            .willReturn(List.of(101L))
            .willReturn(List.of(102L))
            .willReturn(Collections.emptyList());

        given(cleanupHelperService.cleanupRevision(AUD_TABLES, 101L)).willReturn(1);
        given(cleanupHelperService.cleanupRevision(AUD_TABLES, 102L)).willReturn(1);

        // ACT
        SchedulerProcessingResult result = auditRetentionService.cleanupOldAuditRevisions(RETENTION_YEARS, 1);

        // ASSERT
        assertThat(result.getBatchsize()).isEqualTo(1);
        assertThat(result.getTotalCounter()).isEqualTo(2);
        assertThat(result.getSuccessCounter()).isEqualTo(2);

        InOrder inOrder = inOrder(cleanupHelperService);
        inOrder.verify(cleanupHelperService).cleanupRevision(AUD_TABLES, 101L);
        inOrder.verify(cleanupHelperService).cleanupRevision(AUD_TABLES, 102L);
    }

    @Test
    void cleanupOldAuditRevisions_returns_empty_result_when_no_revisions_to_clean() {
        // ARRANGE
        given(auditRetentionRepo.findOldRevisionIds(anyLong(), eq(100)))
            .willReturn(Collections.emptyList());

        // ACT
        SchedulerProcessingResult result = auditRetentionService.cleanupOldAuditRevisions(RETENTION_YEARS, 100);

        // ASSERT
        assertThat(result.getTotalCounter()).isZero();
        assertThat(result.getSuccessCounter()).isZero();
        verify(cleanupHelperService, never()).cleanupRevision(anyList(), anyLong());
    }

    @Test
    void cleanupOldAuditRevisions_continues_on_single_error_and_halts_when_batch_makes_no_progress() {
        // ARRANGE
        long cutoffEpochMillis = OffsetDateTime.of(2006, 9, 23, 12, 0, 0, 0, ZoneOffset.UTC)
            .toInstant()
            .toEpochMilli();

        // Batch 1 returns 101 (fails) and 102 (succeeds) -> progress was made (1 success)
        // Batch 2 returns 101 again -> fails -> 0 successes -> no progress -> loop halts
        given(auditRetentionRepo.findOldRevisionIds(cutoffEpochMillis, 100))
            .willReturn(List.of(101L, 102L))
            .willReturn(List.of(101L));

        given(cleanupHelperService.cleanupRevision(AUD_TABLES, 101L))
            .willThrow(new IllegalStateException("Simulierter DB-Fehler"));
        given(cleanupHelperService.cleanupRevision(AUD_TABLES, 102L))
            .willReturn(2);

        // ACT
        SchedulerProcessingResult result = auditRetentionService.cleanupOldAuditRevisions(RETENTION_YEARS, 100);

        // ASSERT
        assertThat(result.getTotalCounter()).isEqualTo(3);
        assertThat(result.getSuccessCounter()).isEqualTo(1);

        verify(cleanupHelperService, times(2)).cleanupRevision(AUD_TABLES, 101L);
        verify(cleanupHelperService, times(1)).cleanupRevision(AUD_TABLES, 102L);
    }

    @Test
    void cleanupOldAuditRevisions_halts_immediately_when_first_batch_has_zero_progress() {
        // ARRANGE
        long cutoffEpochMillis = OffsetDateTime.of(2006, 9, 23, 12, 0, 0, 0, ZoneOffset.UTC)
            .toInstant()
            .toEpochMilli();

        given(auditRetentionRepo.findOldRevisionIds(cutoffEpochMillis, 50))
            .willReturn(List.of(101L));

        given(cleanupHelperService.cleanupRevision(AUD_TABLES, 101L))
            .willThrow(new RuntimeException("Immediate failure"));

        // ACT
        SchedulerProcessingResult result = auditRetentionService.cleanupOldAuditRevisions(RETENTION_YEARS, 50);

        // ASSERT
        assertThat(result.getTotalCounter()).isEqualTo(1);
        assertThat(result.getSuccessCounter()).isZero();

        // Should not repeat query because hasProgress was false
        verify(auditRetentionRepo, times(1)).findOldRevisionIds(cutoffEpochMillis, 50);
    }
}
