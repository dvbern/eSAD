package ch.dvbern.oss.hemed.esc.api.features.archivierung;

import java.util.List;

import lombok.Getter;

@Getter
public enum ArchivierungStatus {

    OFFEN,
    DOWNLOADED_READY_FOR_DELETION,
    DOWNLOADED_DELETED,
    DOWNLOADED;

    public static List<ArchivierungStatus> offeneStatus() {
        return List.of(OFFEN);
    }

    public static List<ArchivierungStatus> abgeschlosseneStatus() {
        return List.of(DOWNLOADED_READY_FOR_DELETION, DOWNLOADED_DELETED, DOWNLOADED);
    }
}
