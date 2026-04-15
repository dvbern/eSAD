package ch.dvbern.oss.hemed.esc.api.features.impfstoff.types;

public enum Krankheit {

    DIPHTERIE(false),
    TETANUS(false),
    PERTUSSIS(false),
    POLIO(false),
    HEPATITIS_B(true),
    HPV(true),
    FSME(false),
    MASERN(true),
    MUMPS(true),
    ROETELN(true),
    VARIZELLEN(true),
    MENINGOKOKKEN_A_C_W_Y(false),
    MENINGOKOKKEN_B(false),
    HAEMOPHILUS_INFLUENZAE(false),
    ;

    private final boolean benoetigtAuffrischung;

    Krankheit(boolean benoetigtAuffrischung) {
        this.benoetigtAuffrischung = benoetigtAuffrischung;
    }

    public boolean benoetigtAuffrischung() {
        return benoetigtAuffrischung;
    }
}
