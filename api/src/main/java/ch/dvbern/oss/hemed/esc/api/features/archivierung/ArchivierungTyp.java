package ch.dvbern.oss.hemed.esc.api.features.archivierung;

import lombok.Getter;

@Getter
public enum ArchivierungTyp {

    KIND_DEAKTIVIERT,
    ZUSTAENDIGER_ARZT_CHANGED,
    DUPLIKAT_MERGE;
}
