package ch.dvbern.oss.hemed.esc.api.shared.documentqueue;

import java.time.OffsetDateTime;
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
import org.checkerframework.checker.nullness.qual.Nullable;

@Service
@RequiredArgsConstructor
@Slf4j
public class DocumentQueueRunnerService {
    private static final int MAX_RETRIES = 3;

    private final DocumentQueueService documentQueueService;
    private final DocumentMailService documentMailService;
    private final AppClock appClock;

    @NonNull
    @Transactional(TxType.NOT_SUPPORTED)
    public SchedulerProcessingResult performDocumentGeneration(@NonNull SchedulerConfig config) {
        SchedulerProcessingResult
            result = new SchedulerProcessingResult(config.getBatchsize(), 0, 0);
        List<DocumentQueue> documentQueueList = documentQueueService.findAllUnfinished(config.getBatchsize());
        result.setBatchsize(documentQueueList.size());
        int successCount = 0;
        int totalCount = 0;
        for (DocumentQueue documentQueue : documentQueueList) {
            boolean success = process(documentQueue);

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
    public SchedulerProcessingResult performDocumentQueueCleanup(@NonNull SchedulerConfig config) {
        SchedulerProcessingResult result = new SchedulerProcessingResult(config.getBatchsize(), 0, 0);

        long successCount = documentQueueService.cleanUpOldDocumentQueue();

        result.setSuccessCounter(successCount);
        result.setTotalCounter(successCount);
        return result;
    }

    private boolean process(@NonNull DocumentQueue documentQueue) {
        if (!documentQueue.isInProcessableStatus()) {
            LOG.info(
                "ESC-DOCUMENT-QUEUE: Queue item with id {} is not in a processable status. Maybe its in process "
                    + "already?",
                documentQueue.getId());
            return false;
        }
        try {
            documentQueueService.markAsInProgress(documentQueue.getId());
            byte[] content = documentQueueService.triggerGenerationFunction(documentQueue);
            documentQueueService.storeResult(documentQueue.getId(), content);
            markAsSuccess(documentQueue);

            documentMailService.sendDocumentQueueFinishedSuccessMail(documentQueue);
            return true;
        } catch (Exception e) {
            String rootCauseMessage = ExceptionUtils.getRootCauseMessage(e);
            markAsFailed(documentQueue, rootCauseMessage);

            documentMailService.sendDocumentQueueFinishedFailureMail(documentQueue);
            return false;
        } finally {
            documentQueueService.update(documentQueue);
        }
    }

    private void markAsSuccess(@NonNull DocumentQueue documentQueue) {
        documentQueue.setStatus(DocumentQueueStatus.SUCCESS);
        documentQueue.setTimestampResult(OffsetDateTime.now());
    }

    private void markAsFailed(@NonNull DocumentQueue documentQueue, @Nullable String rootCauseMessage) {
        if (documentQueue.getErrorCount() >= MAX_RETRIES) {
            documentQueue.setStatus(DocumentQueueStatus.FAILED);
        } else {
            documentQueue.setErrorCount(documentQueue.getErrorCount() + 1);
            documentQueue.setStatus(DocumentQueueStatus.FAILED_RETRY);
        }

        documentQueue.setLastError(rootCauseMessage);
        documentQueue.setTimestampResult(appClock.offsetDateTime().now());
    }
}
