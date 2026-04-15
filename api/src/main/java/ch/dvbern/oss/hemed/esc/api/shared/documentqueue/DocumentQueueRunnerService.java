package ch.dvbern.oss.hemed.esc.api.shared.documentqueue;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
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
        Path filePath = null;
        try {
            documentQueueService.markAsInProgress(documentQueue.getId());
            filePath = documentQueueService.triggerGenerationFunction(documentQueue);
            documentQueueService.storeResult(documentQueue.getId(), filePath);
            markAsSuccess(documentQueue);

            documentMailService.sendDocumentQueueFinishedSuccessMail(documentQueue);
            return true;
        } catch (Exception e) {
            String rootCauseMessage = ExceptionUtils.getRootCauseMessage(e);
            markAsFailedAndSendMailIfLastRetry(documentQueue, rootCauseMessage);

            LOG.error("ESC-DOCUMENT-QUEUE: ", e);
            return false;
        } finally {
            documentQueueService.update(documentQueue);
            if (filePath != null) {
                try {
                    Files.deleteIfExists(filePath);
                } catch (IOException e) {
                    LOG.warn("Failed to delete temp DocumentQueue file: {}", filePath, e);
                }
            }
        }
    }

    private void markAsSuccess(@NonNull DocumentQueue documentQueue) {
        documentQueue.setStatus(DocumentQueueStatus.SUCCESS);
        documentQueue.setTimestampResult(appClock.offsetDateTime().now());
    }

    private void markAsFailedAndSendMailIfLastRetry(
        @NonNull DocumentQueue documentQueue,
        @NonNull String rootCauseMessage) {
        if (documentQueue.getErrorCount() >= MAX_RETRIES) {
            documentQueue.setStatus(DocumentQueueStatus.FAILED);
            documentMailService.sendDocumentQueueFinishedFailureMail(documentQueue, rootCauseMessage);
        } else {
            documentQueue.setErrorCount(documentQueue.getErrorCount() + 1);
            documentQueue.setStatus(DocumentQueueStatus.FAILED_RETRY);
        }

        documentQueue.setLastError(rootCauseMessage);
        documentQueue.setTimestampResult(appClock.offsetDateTime().now());
    }
}
