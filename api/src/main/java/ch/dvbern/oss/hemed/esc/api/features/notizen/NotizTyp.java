package ch.dvbern.oss.hemed.esc.api.features.notizen;

import lombok.Getter;

@Getter
public enum NotizTyp {
    DEFAULT(false),
    FRAGEBOGEN(true),
    IMPFAUSWEIS(true),
    ANMELDUNG_BESTAETIGUNG(true),
    ABMELDUNG(true),
    IMPFANMELDUNG(true);

    private final boolean unique;

    NotizTyp(boolean unique) {
        this.unique = unique;
    }
}
