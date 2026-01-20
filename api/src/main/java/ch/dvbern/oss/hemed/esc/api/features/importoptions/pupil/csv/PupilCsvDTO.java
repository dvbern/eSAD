package ch.dvbern.oss.hemed.esc.api.features.importoptions.pupil.csv;

import ch.dvbern.oss.hemed.esc.api.shared.types.IgnoreForIdClassTest;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public record PupilCsvDTO(
    @IgnoreForIdClassTest
    String schuelerId,
    String schuelerName,
    String schuelerVorname,
    String schuelerGeschlecht,
    String schuelerGeburtsdatum,

    @IgnoreForIdClassTest
    String klasseId,
    String klasseBezeichnung,
    String klasseStufe,
    String klasseProgrammjahr,

    @IgnoreForIdClassTest
    String schuleId,
    String schuleBezeichnung
) {
    public static final String SCHUELER_ID_COL_NAME = "s_id";
    public static final String SCHUELER_NAME_COL_NAME = "s_name";
    public static final String SCHUELER_VORNAME_COL_NAME = "s_vorname";
    public static final String SCHUELER_GEBURTSDATUM_COL_NAME = "s_geburtsdatum";
    public static final String SCHUELER_GESCHLECHT_COL_NAME = "s_geschlecht";
    public static final String KLASSE_ID_COL_NAME = "k_id";
    public static final String KLASSE_BEZEICHNUNG_COL_NAME = "k_bezeichnung";
    public static final String KLASSE_STUFE_COL_NAME = "k_stufe";
    public static final String KLASSE_PROGRAMMJAHR_COL_NAME = "k_programmjahr";
    public static final String SCHULE_ID_COL_NAME = "sch_id";
    public static final String SCHULE_BEZEICHNUNG_COL_NAME = "sch_bezeichnung";

    static final String[] COLUMN_NAMES = {
        SCHUELER_ID_COL_NAME,
        SCHUELER_NAME_COL_NAME,
        SCHUELER_VORNAME_COL_NAME,
        SCHUELER_GEBURTSDATUM_COL_NAME,
        SCHUELER_GESCHLECHT_COL_NAME,
        KLASSE_ID_COL_NAME,
        KLASSE_BEZEICHNUNG_COL_NAME,
        KLASSE_STUFE_COL_NAME,
        KLASSE_PROGRAMMJAHR_COL_NAME,
        SCHULE_ID_COL_NAME,
        SCHULE_BEZEICHNUNG_COL_NAME,
    };
}


