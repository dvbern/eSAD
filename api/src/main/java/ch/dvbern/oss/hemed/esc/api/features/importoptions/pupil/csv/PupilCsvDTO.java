package ch.dvbern.oss.hemed.esc.api.features.importoptions.pupil.csv;

import ch.dvbern.oss.hemed.esc.api.shared.types.IgnoreForIdClassTest;
import lombok.extern.slf4j.Slf4j;
import org.checkerframework.checker.nullness.qual.Nullable;

@Slf4j
public record PupilCsvDTO(
    // Kind
    @IgnoreForIdClassTest
    String schuelerId,
    String schuelerName,
    String schuelerVorname,
    String schuelerGeburtsdatum,
    @Nullable String schuelerGeschlecht,
    // Kind Adresse
    String schuelerAdresseStrasse,
    String schuelerAdressePlz,
    String schuelerAdresseOrt,
    String schuelerTelefon,
    String schuelerEmail,
    String schuelerNationalitaet,
    String schuelerStaatcodeIso,
    String schuelerErstsprache,
    // Schule
    String schulkreis,
    @IgnoreForIdClassTest
    String schuleId,
    String schuleBezeichnung,
    // Klasse
    @IgnoreForIdClassTest
    String klasseId,
    String klasseBezeichnung,
    String klasseStufe,
    String klasseSchulart,
    String klasseProgrammjahr,
    // Lehrperson 1
    @IgnoreForIdClassTest
    String lehrperson1Id,
    String lehrperson1Anrede,
    String lehrperson1Name,
    String lehrperson1Vorname,
    // Lehrperson 2
    @IgnoreForIdClassTest
    String lehrperson2Id,
    String lehrperson2Anrede,
    String lehrperson2Name,
    String lehrperson2Vorname,
    // KontaktPerson 1
    @IgnoreForIdClassTest
    String kontaktperson1Id,
    String kontaktperson1Beziehung,
    String kontaktperson1Anrede,
    String kontaktperson1Name,
    String kontaktperson1Vorname,
    String kontaktperson1Strasse,
    String kontaktperson1Hausnummer,
    String kontaktperson1Plz,
    String kontaktperson1Ort,
    String kontaktperson1TelefonPrivat,
    String kontaktperson1TelefonGeschaeft,
    String kontaktperson1TelefonMobile,
    String kontaktperson1Email,
    // Kontaktperson 2
    @IgnoreForIdClassTest
    String kontaktperson2Id,
    String kontaktperson2Beziehung,
    String kontaktperson2Anrede,
    String kontaktperson2Name,
    String kontaktperson2Vorname,
    String kontaktperson2Strasse,
    String kontaktperson2Hausnummer,
    String kontaktperson2Plz,
    String kontaktperson2Ort,
    String kontaktperson2TelefonPrivat,
    String kontaktperson2TelefonGeschaeft,
    String kontaktperson2TelefonMobile,
    String kontaktperson2Email
) {
    // Kind
    public static final String SCHUELER_ID_COL_NAME = "SchülerId";
    public static final String SCHUELER_NAME_COL_NAME = "SchülerName";
    public static final String SCHUELER_VORNAME_COL_NAME = "SchülerVorname";
    public static final String SCHUELER_GEBURTSDATUM_COL_NAME = "SchülerGeburtsdatum";
    public static final String SCHUELER_GESCHLECHT_COL_NAME = "SchülerGeschlecht";
    // Kind Adresse
    public static final String SCHUELER_ADRESSE_STRASSE_COL_NAME = "SchülerAdresse";
    public static final String SCHUELER_ADRESSE_PLZ_COL_NAME = "SchülerPlz";
    public static final String SCHUELER_ADRESSE_ORT_COL_NAME = "SchülerOrt";
    public static final String SCHUELER_TELEFON_COL_NAME = "SchülerMobile";
    public static final String SCHUELER_EMAIL_COL_NAME = "SchülerEMail";
    public static final String SCHUELER_NATIONALITAET_COL_NAME = "SchülerNationalität";
    public static final String SCHUELER_STAATSCODE_ISO_COL_NAME = "SchülerStaatcodeIso";
    public static final String SCHUELER_ERSTSPRACHE_COL_NAME = "SchülerErstsprache";
    // Schule
    public static final String SCHULE_SCHULKREIS_BEZEICHNUNG_COL_NAME = "SchulkreisBezeichnung";
    public static final String SCHULE_ID_COL_NAME = "SchulhausId";
    public static final String SCHULE_BEZEICHNUNG_COL_NAME = "SchulhausBezeichnung";
    // Klasse
    public static final String KLASSE_ID_COL_NAME = "KlasseId";
    public static final String KLASSE_BEZEICHNUNG_COL_NAME = "KlasseBezeichnung";
    public static final String KLASSE_STUFE_COL_NAME = "KlasseStufe";
    public static final String KLASSE_SCHULART_COL_NAME = "KlasseSchulart";
    public static final String KLASSE_PROGRAMMJAHR_COL_NAME = "KlasseProgrammjahr";
    // Lehrperson 1
    public static final String KLASSE_LEHRPERSON_1_ID_COL_NAME = "KlasseLehrpersonId";
    public static final String KLASSE_LEHRPERSON_1_ANREDE_COL_NAME = "KlasseLehrpersonAnrede";
    public static final String KLASSE_LEHRPERSON_1_NAME_COL_NAME = "KlasseLehrpersonName";
    public static final String KLASSE_LEHRPERSON_1_VORNAME_COL_NAME = "KlasseLehrpersonVorname";
    // Lehrperson 2
    public static final String KLASSE_LEHRPERSON_2_ID_COL_NAME = "KlasseLehrperson2Id";
    public static final String KLASSE_LEHRPERSON_2_ANREDE_COL_NAME = "KlasseLehrperson2Anrede";
    public static final String KLASSE_LEHRPERSON_2_NAME_COL_NAME = "KlasseLehrperson2Name";
    public static final String KLASSE_LEHRPERSON_2_VORNAME_COL_NAME = "KlasseLehrperson2Vorname";
    // Kontaktperson 1
    public static final String KONTAKTPERSON_1_ID_COL_NAME = "AdressierungNr";
    public static final String KONTAKTPERSON_1_BEZIEHUNG_COL_NAME = "AdressierungBeziehung";
    public static final String KONTAKTPERSON_1_ANREDE_COL_NAME = "AdressierungAnrede";
    public static final String KONTAKTPERSON_1_NAME_COL_NAME = "AdressierungName";
    public static final String KONTAKTPERSON_1_VORNAME_COL_NAME = "AdressierungVorname";
    public static final String KONTAKTPERSON_1_STRASSE_COL_NAME = "AdressierungStrasse";
    public static final String KONTAKTPERSON_1_HAUSNUMMER_COL_NAME = "AdressierungHausnummer";
    public static final String KONTAKTPERSON_1_PLZ_COL_NAME = "AdressierungPlz";
    public static final String KONTAKTPERSON_1_ORT_COL_NAME = "AdressierungOrt";
    public static final String KONTAKTPERSON_1_TELEFON_PRIVAT_COL_NAME = "AdressierungTelefonPrivat";
    public static final String KONTAKTPERSON_1_TELEFON_GESCHAEFT_COL_NAME = "AdressierungTelefonGeschäft";
    public static final String KONTAKTPERSON_1_TELEFON_MOBILE_COL_NAME = "AdressierungNatel";
    public static final String KONTAKTPERSON_1_EMAIL_COL_NAME = "AdressierungEMail";
    // Kontaktperson 2
    public static final String KONTAKTPERSON_2_ID_COL_NAME = "AdressierungNr2";
    public static final String KONTAKTPERSON_2_BEZIEHUNG_COL_NAME = "AdressierungBeziehung2";
    public static final String KONTAKTPERSON_2_ANREDE_COL_NAME = "AdressierungAnrede2";
    public static final String KONTAKTPERSON_2_NAME_COL_NAME = "AdressierungName2";
    public static final String KONTAKTPERSON_2_VORNAME_COL_NAME = "AdressierungVorname2";
    public static final String KONTAKTPERSON_2_STRASSE_COL_NAME = "AdressierungStrasse2";
    public static final String KONTAKTPERSON_2_HAUSNUMMER_COL_NAME = "AdressierungHausnummer2";
    public static final String KONTAKTPERSON_2_PLZ_COL_NAME = "AdressierungPlz2";
    public static final String KONTAKTPERSON_2_ORT_COL_NAME = "AdressierungOrt2";
    public static final String KONTAKTPERSON_2_TELEFON_PRIVAT_COL_NAME = "AdressierungTelefonPrivat2";
    public static final String KONTAKTPERSON_2_TELEFON_GESCHAEFT_COL_NAME = "AdressierungTelefonGeschäft2";
    public static final String KONTAKTPERSON_2_TELEFON_MOBILE_COL_NAME = "AdressierungNatel2";
    public static final String KONTAKTPERSON_2_EMAIL_COL_NAME = "AdressierungEMail2";

    static final String[] COLUMN_NAMES = {
        // Kind
        SCHUELER_ID_COL_NAME,
        SCHUELER_NAME_COL_NAME,
        SCHUELER_VORNAME_COL_NAME,
        SCHUELER_GEBURTSDATUM_COL_NAME,
        SCHUELER_GESCHLECHT_COL_NAME,
        // Kind Adresse
        SCHUELER_ADRESSE_STRASSE_COL_NAME,
        SCHUELER_ADRESSE_PLZ_COL_NAME,
        SCHUELER_ADRESSE_ORT_COL_NAME,
        SCHUELER_TELEFON_COL_NAME,
        SCHUELER_EMAIL_COL_NAME,
        SCHUELER_NATIONALITAET_COL_NAME,
        SCHUELER_STAATSCODE_ISO_COL_NAME,
        SCHUELER_ERSTSPRACHE_COL_NAME,
        // Schule
        SCHULE_SCHULKREIS_BEZEICHNUNG_COL_NAME,
        SCHULE_ID_COL_NAME,
        SCHULE_BEZEICHNUNG_COL_NAME,
        // Klasse
        KLASSE_ID_COL_NAME,
        KLASSE_BEZEICHNUNG_COL_NAME,
        KLASSE_STUFE_COL_NAME,
        KLASSE_SCHULART_COL_NAME,
        KLASSE_PROGRAMMJAHR_COL_NAME,
        // Lehrperson 1
        KLASSE_LEHRPERSON_1_ID_COL_NAME,
        KLASSE_LEHRPERSON_1_ANREDE_COL_NAME,
        KLASSE_LEHRPERSON_1_NAME_COL_NAME,
        KLASSE_LEHRPERSON_1_VORNAME_COL_NAME,
        // Lehrperson 2
        KLASSE_LEHRPERSON_2_ID_COL_NAME,
        KLASSE_LEHRPERSON_2_ANREDE_COL_NAME,
        KLASSE_LEHRPERSON_2_NAME_COL_NAME,
        KLASSE_LEHRPERSON_2_VORNAME_COL_NAME,
        // Kontaktperson 1
        KONTAKTPERSON_1_ID_COL_NAME,
        KONTAKTPERSON_1_BEZIEHUNG_COL_NAME,
        KONTAKTPERSON_1_ANREDE_COL_NAME,
        KONTAKTPERSON_1_NAME_COL_NAME,
        KONTAKTPERSON_1_VORNAME_COL_NAME,
        KONTAKTPERSON_1_STRASSE_COL_NAME,
        KONTAKTPERSON_1_HAUSNUMMER_COL_NAME,
        KONTAKTPERSON_1_PLZ_COL_NAME,
        KONTAKTPERSON_1_ORT_COL_NAME,
        KONTAKTPERSON_1_TELEFON_PRIVAT_COL_NAME,
        KONTAKTPERSON_1_TELEFON_GESCHAEFT_COL_NAME,
        KONTAKTPERSON_1_TELEFON_MOBILE_COL_NAME,
        KONTAKTPERSON_1_EMAIL_COL_NAME,
        // Kontaktperson 2
        KONTAKTPERSON_2_ID_COL_NAME,
        KONTAKTPERSON_2_BEZIEHUNG_COL_NAME,
        KONTAKTPERSON_2_ANREDE_COL_NAME,
        KONTAKTPERSON_2_NAME_COL_NAME,
        KONTAKTPERSON_2_VORNAME_COL_NAME,
        KONTAKTPERSON_2_STRASSE_COL_NAME,
        KONTAKTPERSON_2_HAUSNUMMER_COL_NAME,
        KONTAKTPERSON_2_PLZ_COL_NAME,
        KONTAKTPERSON_2_ORT_COL_NAME,
        KONTAKTPERSON_2_TELEFON_PRIVAT_COL_NAME,
        KONTAKTPERSON_2_TELEFON_GESCHAEFT_COL_NAME,
        KONTAKTPERSON_2_TELEFON_MOBILE_COL_NAME,
        KONTAKTPERSON_2_EMAIL_COL_NAME,
    };
}


