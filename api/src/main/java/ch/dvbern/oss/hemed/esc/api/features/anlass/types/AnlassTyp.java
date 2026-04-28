package ch.dvbern.oss.hemed.esc.api.features.anlass.types;

public enum AnlassTyp {

    KINDERGARTEN(false),
    KLASSE_4(false),
    KLASSE_8(false),
    KLASSE_9(false),
    FREMDSPRACHEN(false),
    IMPFAKTION(false),
    IMPFSPRECHSTUNDE(false),
    IMPFTERMIN(false),
    VERTRAUENSARZT(false),
    KLASSE_5(false),
    EINSCHULUNG(false),
    SONSTIGES(false),
    LAEUSEKONTROLLE(false),

    // Anonyme AnlassTypen
    IMPFTERMIN_ANONYM(true),
    KINDERGARTEN_ANONYM(true),
    KLASSE_4_ANONYM(true),
    KLASSE_8_ANONYM(true);

    private final boolean anonym;

    AnlassTyp(boolean anonym) {
        this.anonym = anonym;
    }

    public boolean isAnonym() {
        return anonym;
    }
}
