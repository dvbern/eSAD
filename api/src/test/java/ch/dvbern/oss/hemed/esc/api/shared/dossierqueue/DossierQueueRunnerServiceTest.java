package ch.dvbern.oss.hemed.esc.api.shared.dossierqueue;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

import ch.dvbern.dvbstarter.shared.clock.AppClock;
import ch.dvbern.dvbstarter.shared.types.id.ID;
import ch.dvbern.oss.hemed.esc.api.shared.scheduler.SchedulerConfig;
import ch.dvbern.oss.hemed.esc.api.shared.scheduler.SchedulerProcessingResult;
import ch.dvbern.oss.hemed.esc.api.shared.scheduler.SchedulerTyp;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class DossierQueueRunnerServiceTest {

    private DossierQueueService dossierQueueService;
    private AppClock appClock;
    private DossierQueueRunnerService service;
    private DossierMailService dossierMailService;
    private AppClock.OffsetDateTimeProvider offsetDateTimeProvider;

    @BeforeEach
    void setUp() {
        dossierQueueService = mock(DossierQueueService.class);
        appClock = mock(AppClock.class);
        offsetDateTimeProvider = mock(AppClock.OffsetDateTimeProvider.class);
        dossierMailService = mock(DossierMailService.class);
        when(appClock.offsetDateTime()).thenReturn(offsetDateTimeProvider);
        service = new DossierQueueRunnerService(dossierQueueService, dossierMailService, appClock);
    }

    @Test
    void testProcessQueueItems_Success() {
        // ARRANGE
        SchedulerConfig config = new SchedulerConfig(SchedulerTyp.DOSSIER_QUEUE_PROCESSING, false, 10);
        DossierQueue item1 = createItem(DossierQueueStatus.NEW, 0);
        DossierQueue item2 = createItem(DossierQueueStatus.FAILED_RETRY, 1);
        when(dossierQueueService.findAllUnfinished(10)).thenReturn(List.of(item1, item2));

        OffsetDateTime now = OffsetDateTime.now();
        when(offsetDateTimeProvider.now()).thenReturn(now);

        // ACT
        SchedulerProcessingResult result = service.processQueueItems(config);

        // ASSERT
        assertThat(result.getBatchsize()).isEqualTo(2);
        assertThat(result.getSuccessCounter()).isEqualTo(2);
        assertThat(result.getTotalCounter()).isEqualTo(2);

        verify(dossierQueueService).markAsInProgress(item1.getId());
        verify(dossierQueueService).processDossierQueueItem(item1);
        verify(dossierQueueService).update(item1);
        assertThat(item1.getStatus()).isEqualTo(DossierQueueStatus.SUCCESS);
        assertThat(item1.getTimestampResult()).isEqualTo(now);

        verify(dossierQueueService).markAsInProgress(item2.getId());
        verify(dossierQueueService).processDossierQueueItem(item2);
        verify(dossierQueueService).update(item2);
        assertThat(item2.getStatus()).isEqualTo(DossierQueueStatus.SUCCESS);
        assertThat(item2.getTimestampResult()).isEqualTo(now);
    }

    @Test
    void testProcessQueueItems_SkipNonProcessable() {
        // ARRANGE
        SchedulerConfig config = new SchedulerConfig(SchedulerTyp.DOSSIER_QUEUE_PROCESSING, false, 10);
        DossierQueue item1 = createItem(DossierQueueStatus.IN_PROGRESS, 0);
        when(dossierQueueService.findAllUnfinished(10)).thenReturn(List.of(item1));

        // ACT
        SchedulerProcessingResult result = service.processQueueItems(config);

        // ASSERT
        assertThat(result.getBatchsize()).isEqualTo(1);
        assertThat(result.getSuccessCounter()).isZero();
        assertThat(result.getTotalCounter()).isEqualTo(1);

        verify(dossierQueueService, times(0)).markAsInProgress(any());
        verify(dossierQueueService, times(0)).update(any());
    }

    @Test
    void testProcessQueueItems_RetryOnError() {
        // ARRANGE
        SchedulerConfig config = new SchedulerConfig(SchedulerTyp.DOSSIER_QUEUE_PROCESSING, false, 10);
        DossierQueue item1 = createItem(DossierQueueStatus.NEW, 0);
        when(dossierQueueService.findAllUnfinished(10)).thenReturn(List.of(item1));

        doThrow(new RuntimeException("Test error")).when(dossierQueueService).processDossierQueueItem(item1);

        OffsetDateTime now = OffsetDateTime.now();
        when(offsetDateTimeProvider.now()).thenReturn(now);

        // ACT
        SchedulerProcessingResult result = service.processQueueItems(config);

        // ASSERT
        assertThat(result.getSuccessCounter()).isZero();
        assertThat(result.getTotalCounter()).isEqualTo(1);

        verify(dossierQueueService).update(item1);
        assertThat(item1.getStatus()).isEqualTo(DossierQueueStatus.FAILED_RETRY);
        assertThat(item1.getErrorCount()).isEqualTo(1);
        assertThat(item1.getLastError()).contains("RuntimeException: Test error");
        assertThat(item1.getTimestampResult()).isEqualTo(now);
    }

    @Test
    void testProcessQueueItems_FinalFailure() {
        // ARRANGE
        SchedulerConfig config = new SchedulerConfig(SchedulerTyp.DOSSIER_QUEUE_PROCESSING, false, 10);
        DossierQueue item1 = createItem(DossierQueueStatus.FAILED_RETRY, 3); // MAX_RETRIES = 3
        when(dossierQueueService.findAllUnfinished(10)).thenReturn(List.of(item1));

        doThrow(new RuntimeException("Final error")).when(dossierQueueService).processDossierQueueItem(item1);

        OffsetDateTime now = OffsetDateTime.now();
        when(offsetDateTimeProvider.now()).thenReturn(now);

        // ACT
        SchedulerProcessingResult result = service.processQueueItems(config);

        // ASSERT
        assertThat(result.getSuccessCounter()).isZero();
        assertThat(result.getTotalCounter()).isEqualTo(1);

        verify(dossierQueueService).update(item1);
        assertThat(item1.getStatus()).isEqualTo(DossierQueueStatus.FAILED);
        assertThat(item1.getErrorCount()).isEqualTo(3); // Error count not incremented if it reached MAX_RETRIES?
        // Yes, if it's already 3, it just sets FAILED.
        assertThat(item1.getLastError()).contains("RuntimeException: Final error");
    }

    @Test
    void testPerformDossierQueueCleanup() {
        // ARRANGE
        SchedulerConfig config = new SchedulerConfig(SchedulerTyp.DOSSIER_QUEUE_CLEAN_UP, false, 100);
        when(dossierQueueService.cleanUpOldDossierQueue()).thenReturn(5L);

        // ACT
        SchedulerProcessingResult result = service.performDossierQueueCleanup(config);

        // ASSERT
        assertThat(result.getSuccessCounter()).isEqualTo(5);
        assertThat(result.getTotalCounter()).isEqualTo(5);
        verify(dossierQueueService).cleanUpOldDossierQueue();
    }

    private DossierQueue createItem(DossierQueueStatus status, int errorCount) {
        DossierQueue item = DossierQueue.of(UUID.randomUUID(), DossierQueueTyp.ARCHIVIERUNG, null);
        item.setId(ID.of(UUID.randomUUID(), DossierQueue.class));
        item.setStatus(status);
        item.setErrorCount(errorCount);
        return item;
    }
}
