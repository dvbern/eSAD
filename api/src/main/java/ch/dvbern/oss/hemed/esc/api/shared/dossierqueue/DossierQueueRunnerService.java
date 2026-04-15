package ch.dvbern.oss.hemed.esc.api.shared.dossierqueue;

import java.util.List;

import ch.dvbern.dvbstarter.infrastructure.stereotypes.Service;
import ch.dvbern.dvbstarter.shared.clock.AppClock;
import ch.dvbern.oss.hemed.esc.api.shared.scheduler.SchedulerConfig;
import ch.dvbern.oss.hemed.esc.api.shared.scheduler.SchedulerProcessingResult;
import jakarta.transaction.Transactional;
import jakarta.transaction.Transactional.TxType;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.checkerframework.checker.nullness.qual.NonNull;

@Service
@RequiredArgsConstructor
@Slf4j
public class DossierQueueRunnerService {
    private static final int MAX_RETRIES = 3;

    private final DossierQueueService dossierQueueService;
    private final DossierMailService dossierMailService;
    private final AppClock appClock;

    @NonNull
    @Transactional(TxType.NOT_SUPPORTED)
    public SchedulerProcessingResult processQueueItems(@NonNull SchedulerConfig config) {
        SchedulerProcessingResult
            result = new SchedulerProcessingResult(config.getBatchsize(), 0, 0);
        List<DossierQueue> dossierQueueList = dossierQueueService.findAllUnfinished(config.getBatchsize());
        result.setBatchsize(dossierQueueList.size());
        int successCount = 0;
        int totalCount = 0;
        for (DossierQueue dossierQueue : dossierQueueList) {
            boolean success = process(dossierQueue);
            if (success) {
                successCount++;
            }
            totalCount++;
        }
        result.setTotalCounter(totalCount);
        result.setSuccessCounter(successCount);
        return result;
    }

    @NonNull
    @Transactional(TxType.NOT_SUPPORTED)
    public SchedulerProcessingResult performDossierQueueCleanup(@NonNull SchedulerConfig config) {
        SchedulerProcessingResult result = new SchedulerProcessingResult(config.getBatchsize(), 0, 0);

        long successCount = dossierQueueService.cleanUpOldDossierQueue();

        result.setSuccessCounter(successCount);
        result.setTotalCounter(successCount);
        return result;
    }

    private boolean process(@NonNull DossierQueue dossierQueue) {
        if (!dossierQueue.isInProcessableStatus()) {
            LOG.info(
                "ESC-DOSSIER-QUEUE: Queue item with id {} is not in a processable status. Maybe its in process "
                    + "already? Current status: {}",
                dossierQueue.getId(), dossierQueue.getStatus());
            return false;
        }
        try {
            dossierQueueService.markAsInProgress(dossierQueue.getId());
            dossierQueueService.processDossierQueueItem(dossierQueue);
            markAsSuccess(dossierQueue);

            return true;
        } catch (Exception e) {
            String rootCauseMessage = ExceptionUtils.getRootCauseMessage(e);
            markAsFailed(dossierQueue, rootCauseMessage);

            LOG.error("ESC-DOSSIER-QUEUE: ", e);
            return false;
        } finally {
            dossierQueueService.update(dossierQueue);
        }
    }

    private void markAsSuccess(@NonNull DossierQueue dossierQueue) {
        dossierQueue.setStatus(DossierQueueStatus.SUCCESS);
        dossierQueue.setTimestampResult(appClock.offsetDateTime().now());
    }

    private void markAsFailed(@NonNull DossierQueue dossierQueue, @NonNull String rootCauseMessage) {
        dossierQueue.setLastError(rootCauseMessage);
        dossierQueue.setTimestampResult(appClock.offsetDateTime().now());
        if (dossierQueue.getErrorCount() >= MAX_RETRIES) {
            dossierQueue.setStatus(DossierQueueStatus.FAILED);
            dossierMailService.sendDossierQueueFinishedFailureMail(dossierQueue);
        } else {
            dossierQueue.setErrorCount(dossierQueue.getErrorCount() + 1);
            dossierQueue.setStatus(DossierQueueStatus.FAILED_RETRY);
        }
    }
}
