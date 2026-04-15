package ch.dvbern.oss.hemed.esc.api.shared.dossierqueue;

import java.util.List;

import org.checkerframework.checker.nullness.qual.NonNull;

public enum DossierQueueStatus {

    NEW,
    IN_PROGRESS,
    SUCCESS,
    FAILED_RETRY,
    FAILED;

    @NonNull
    public static List<DossierQueueStatus> processableStatus() {
        return List.of(NEW, FAILED_RETRY);
    }

    @NonNull
    public static List<DossierQueueStatus> finishStatus() {
        return List.of(SUCCESS, FAILED);
    }
}
