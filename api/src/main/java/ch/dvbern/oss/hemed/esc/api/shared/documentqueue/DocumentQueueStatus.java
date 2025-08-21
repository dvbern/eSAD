package ch.dvbern.oss.hemed.esc.api.shared.documentqueue;

import java.util.List;

public enum DocumentQueueStatus {
    NEW,
    IN_PROGRESS,
    SUCCESS,
    FAILED_RETRY,
    FAILED;

    public static List<DocumentQueueStatus> processableStatus() {
        return List.of(NEW, FAILED_RETRY);
    }

    public static List<DocumentQueueStatus> finishStatus() {
        return List.of(SUCCESS, FAILED);
    }
}
