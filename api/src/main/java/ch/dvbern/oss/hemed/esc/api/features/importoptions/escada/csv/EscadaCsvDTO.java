package ch.dvbern.oss.hemed.esc.api.features.importoptions.escada.csv;

import ch.dvbern.oss.hemed.esc.api.shared.types.IgnoreForIdClassTest;
import lombok.With;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public record EscadaCsvDTO(
    @IgnoreForIdClassTest
    String schuelerId,
    String wohntyp,
    String schuelerName,
    String schuelerVorname,
    String schuelerGeburtsdatum,
    String schuelerGeschlecht,
    @With
    String schuelerSozialversicheurng,
    String schuelerAdresse,
    String schuelerAdresse1,
    String schuelerAdresse2,
    String schuelerPlz,
    String schuelerOrt,
    String schuelerMobile,
    String schuelerEmail,
    String schuelerNationalitaet,
    String schuelerStaatcodeIso,
    String schuelerSprache,
    String schuelerErstsprache,

    String schulkreisBezeichnung,

    @IgnoreForIdClassTest
    String schulanlageId,
    String schulanlageBezeichnung,
    @IgnoreForIdClassTest
    String schulhausId,
    String schulhausBezeichnung,

    @IgnoreForIdClassTest
    String klasseId,
    String klasseBezeichnung,
    String klasseStufe,
    String klasseSchulart,
    String klasseProgrammjahr,

    @IgnoreForIdClassTest
    String klasseLehrpersonId,
    String klasseLehrpersonAnrede,
    String klasseLehrpersonName,
    String klasseLehrpersonVorname,
    @IgnoreForIdClassTest
    String klasseLehrperson2Id,
    String klasseLehrperson2Anrede,
    String klasseLehrperson2Name,
    String klasseLehrperson2Vorname,
    @IgnoreForIdClassTest
    String klasseLehrperson3Id,
    String klasseLehrperson3Anrede,
    String klasseLehrperson3Name,
    String klasseLehrperson3Vorname,
    @IgnoreForIdClassTest
    String klasseLehrperson4Id,
    String klasseLehrperson4Anrede,
    String klasseLehrperson4Name,
    String klasseLehrperson4Vorname,

    String adressierungNr,
    String adressierungAnrede,
    String adressierungName,
    String adressierungVorname,
    String adressierungStrasse,
    String adressierungHausnummer,
    String adressierungPlz,
    String adressierungOrt,
    String adressierungTelefonPrivat,
    String adressierungTelefonGeschaeft,
    String adressierungNatel,
    String adressierungEmail,

    String adressierungNr2,
    String adressierungAnrede2,
    String adressierungName2,
    String adressierungVorname2,
    String adressierungStrasse2,
    String adressierungHausnummer2,
    String adressierungPlz2,
    String adressierungOrt2,
    String adressierungTelefonPrivat2,
    String adressierungTelefonGeschaeft2,
    String adressierungNatel2,
    String adressierungEmail2
) {

    public static final String SCHUELER_ID_COL_NAME = "SchülerID";
    public static final String WOHNTYP_COL_NAME = "Wohntyp";
    public static final String SCHUELER_NAME_COL_NAME = "SchülerName";
    public static final String SCHUELER_VORNAME_COL_NAME = "SchülerVorname";
    public static final String SCHUELER_GEBURTSDATUM_COL_NAME = "SchülerGeburtsdatum";
    public static final String SCHUELER_GESCHLECHT_COL_NAME = "SchülerGeschlecht";
    public static final String SCHUELER_SOZIALVERSICHEURNG_COL_NAME = "SchülerSozialversicheurng";
    public static final String SCHUELER_ADRESSE_COL_NAME = "SchülerAdresse";
    public static final String SCHUELER_ADRESSE1_COL_NAME = "SchülerAdresse1";
    public static final String SCHUELER_ADRESSE2_COL_NAME = "SchülerAdresse2";
    public static final String SCHUELER_PLZ_COL_NAME = "SchülerPLZ";
    public static final String SCHUELER_ORT_COL_NAME = "SchülerOrt";
    public static final String SCHUELER_MOBILE_COL_NAME = "SchülerMobile";
    public static final String SCHUELER_EMAIL_COL_NAME = "SchülerEMail";
    public static final String SCHUELER_NATIONALITAET_COL_NAME = "SchülerNationalität";
    public static final String SCHUELER_STAATCODE_ISO_COL_NAME = "SchülerStaatcodeISO";
    public static final String SCHUELER_SPRACHE_COL_NAME = "SchülerSprache";
    public static final String SCHUELER_ERSTSPRACHE_COL_NAME = "SchülerErstsprache";
    public static final String SCHULKREIS_BEZEICHNUNG_COL_NAME = "SchulkreisBezeichnung";
    public static final String SCHULANLAGE_ID_COL_NAME = "SchulanlageID";
    public static final String SCHULANLAGE_BEZEICHNUNG_COL_NAME = "SchulanlageBezeichnung";
    public static final String SCHULHAUS_ID_COL_NAME = "SchulhausID";
    public static final String SCHULHAUS_BEZEICHNUNG_COL_NAME = "SchulhausBezeichnung";
    public static final String KLASSE_ID_COL_NAME = "KlasseID";
    public static final String KLASSE_BEZEICHNUNG_COL_NAME = "KlasseBezeichnung";
    public static final String KLASSE_STUFE_COL_NAME = "KlasseStufe";
    public static final String KLASSE_SCHULART_COL_NAME = "KlasseSchulart";
    public static final String KLASSE_PROGRAMMJAHR_COL_NAME = "KlasseProgrammjahr";
    public static final String KLASSE_LEHRPERSON_ID_COL_NAME = "KlasseLehrpersonID";
    public static final String KLASSE_LEHRPERSON_ANREDE_COL_NAME = "KlasseLehrpersonAnrede";
    public static final String KLASSE_LEHRPERSON_NAME_COL_NAME = "KlasseLehrpersonName";
    public static final String KLASSE_LEHRPERSON_VORNAME_COL_NAME = "KlasseLehrpersonVorname";
    public static final String KLASSE_LEHRPERSON2_ID_COL_NAME = "KlasseLehrperson2ID";
    public static final String KLASSE_LEHRPERSON2_ANREDE_COL_NAME = "KlasseLehrperson2Anrede";
    public static final String KLASSE_LEHRPERSON2_NAME_COL_NAME = "KlasseLehrperson2Name";
    public static final String KLASSE_LEHRPERSON2_VORNAME_COL_NAME = "KlasseLehrperson2Vorname";
    public static final String KLASSE_LEHRPERSON3_ID_COL_NAME = "KlasseLehrperson3ID";
    public static final String KLASSE_LEHRPERSON3_ANREDE_COL_NAME = "KlasseLehrperson3Anrede";
    public static final String KLASSE_LEHRPERSON3_NAME_COL_NAME = "KlasseLehrperson3Name";
    public static final String KLASSE_LEHRPERSON3_VORNAME_COL_NAME = "KlasseLehrperson3Vorname";
    public static final String KLASSE_LEHRPERSON4_ID_COL_NAME = "KlasseLehrperson4ID";
    public static final String KLASSE_LEHRPERSON4_ANREDE_COL_NAME = "KlasseLehrperson4Anrede";
    public static final String KLASSE_LEHRPERSON4_NAME_COL_NAME = "KlasseLehrperson4Name";
    public static final String KLASSE_LEHRPERSON4_VORNAME_COL_NAME = "KlasseLehrperson4Vorname";
    public static final String ADRESSIERUNG_NR_COL_NAME = "AdressierungNr";
    public static final String ADRESSIERUNG_ANREDE_COL_NAME = "AdressierungAnrede";
    public static final String ADRESSIERUNG_NAME_COL_NAME = "AdressierungName";
    public static final String ADRESSIERUNG_VORNAME_COL_NAME = "AdressierungVorname";
    public static final String ADRESSIERUNG_STRASSE_COL_NAME = "AdressierungStrasse";
    public static final String ADRESSIERUNG_HAUSNUMMER_COL_NAME = "AdressierungHausnummer";
    public static final String ADRESSIERUNG_PLZ_COL_NAME = "AdressierungPLZ";
    public static final String ADRESSIERUNG_ORT_COL_NAME = "AdressierungOrt";
    public static final String ADRESSIERUNG_TELEFON_PRIVAT_COL_NAME = "AdressierungTelefonPrivat";
    public static final String ADRESSIERUNG_TELEFON_GESCHAEFT_COL_NAME = "AdressierungTelefonGeschäft";
    public static final String ADRESSIERUNG_NATEL_COL_NAME = "AdressierungNatel";
    public static final String ADRESSIERUNG_EMAIL_COL_NAME = "AdressierungEMail";
    public static final String ADRESSIERUNG_NR2_COL_NAME = "AdressierungNr2";
    public static final String ADRESSIERUNG_ANREDE2_COL_NAME = "AdressierungAnrede2";
    public static final String ADRESSIERUNG_NAME2_COL_NAME = "AdressierungName2";
    public static final String ADRESSIERUNG_VORNAME2_COL_NAME = "AdressierungVorname2";
    public static final String ADRESSIERUNG_STRASSE2_COL_NAME = "AdressierungStrasse2";
    public static final String ADRESSIERUNG_HAUSNUMMER2_COL_NAME = "AdressierungHausnummer2";
    public static final String ADRESSIERUNG_PLZ2_COL_NAME = "AdressierungPLZ2";
    public static final String ADRESSIERUNG_ORT2_COL_NAME = "AdressierungOrt2";
    public static final String ADRESSIERUNG_TELEFON_PRIVAT2_COL_NAME = "AdressierungTelefonPrivat2";
    public static final String ADRESSIERUNG_TELEFON_GESCHAEFT2_COL_NAME = "AdressierungTelefonGeschäft2";
    public static final String ADRESSIERUNG_NATEL2_COL_NAME = "AdressierungNatel2";
    public static final String ADRESSIERUNG_EMAIL2_COL_NAME = "AdressierungEMail2";

    static final String[] COLUMN_NAMES = {
        SCHUELER_ID_COL_NAME,
        WOHNTYP_COL_NAME,
        SCHUELER_NAME_COL_NAME,
        SCHUELER_VORNAME_COL_NAME,
        SCHUELER_GEBURTSDATUM_COL_NAME,
        SCHUELER_GESCHLECHT_COL_NAME,
        SCHUELER_SOZIALVERSICHEURNG_COL_NAME,
        SCHUELER_ADRESSE_COL_NAME,
        SCHUELER_ADRESSE1_COL_NAME,
        SCHUELER_ADRESSE2_COL_NAME,
        SCHUELER_PLZ_COL_NAME,
        SCHUELER_ORT_COL_NAME,
        SCHUELER_MOBILE_COL_NAME,
        SCHUELER_EMAIL_COL_NAME,
        SCHUELER_NATIONALITAET_COL_NAME,
        SCHUELER_STAATCODE_ISO_COL_NAME,
        SCHUELER_SPRACHE_COL_NAME,
        SCHUELER_ERSTSPRACHE_COL_NAME,
        SCHULKREIS_BEZEICHNUNG_COL_NAME,
        SCHULANLAGE_ID_COL_NAME,
        SCHULANLAGE_BEZEICHNUNG_COL_NAME,
        SCHULHAUS_ID_COL_NAME,
        SCHULHAUS_BEZEICHNUNG_COL_NAME,
        KLASSE_ID_COL_NAME,
        KLASSE_BEZEICHNUNG_COL_NAME,
        KLASSE_STUFE_COL_NAME,
        KLASSE_SCHULART_COL_NAME,
        KLASSE_PROGRAMMJAHR_COL_NAME,
        KLASSE_LEHRPERSON_ID_COL_NAME,
        KLASSE_LEHRPERSON_ANREDE_COL_NAME,
        KLASSE_LEHRPERSON_NAME_COL_NAME,
        KLASSE_LEHRPERSON_VORNAME_COL_NAME,
        KLASSE_LEHRPERSON2_ID_COL_NAME,
        KLASSE_LEHRPERSON2_ANREDE_COL_NAME,
        KLASSE_LEHRPERSON2_NAME_COL_NAME,
        KLASSE_LEHRPERSON2_VORNAME_COL_NAME,
        KLASSE_LEHRPERSON3_ID_COL_NAME,
        KLASSE_LEHRPERSON3_ANREDE_COL_NAME,
        KLASSE_LEHRPERSON3_NAME_COL_NAME,
        KLASSE_LEHRPERSON3_VORNAME_COL_NAME,
        KLASSE_LEHRPERSON4_ID_COL_NAME,
        KLASSE_LEHRPERSON4_ANREDE_COL_NAME,
        KLASSE_LEHRPERSON4_NAME_COL_NAME,
        KLASSE_LEHRPERSON4_VORNAME_COL_NAME,
        ADRESSIERUNG_NR_COL_NAME,
        ADRESSIERUNG_ANREDE_COL_NAME,
        ADRESSIERUNG_NAME_COL_NAME,
        ADRESSIERUNG_VORNAME_COL_NAME,
        ADRESSIERUNG_STRASSE_COL_NAME,
        ADRESSIERUNG_HAUSNUMMER_COL_NAME,
        ADRESSIERUNG_PLZ_COL_NAME,
        ADRESSIERUNG_ORT_COL_NAME,
        ADRESSIERUNG_TELEFON_PRIVAT_COL_NAME,
        ADRESSIERUNG_TELEFON_GESCHAEFT_COL_NAME,
        ADRESSIERUNG_NATEL_COL_NAME,
        ADRESSIERUNG_EMAIL_COL_NAME,
        ADRESSIERUNG_NR2_COL_NAME,
        ADRESSIERUNG_ANREDE2_COL_NAME,
        ADRESSIERUNG_NAME2_COL_NAME,
        ADRESSIERUNG_VORNAME2_COL_NAME,
        ADRESSIERUNG_STRASSE2_COL_NAME,
        ADRESSIERUNG_HAUSNUMMER2_COL_NAME,
        ADRESSIERUNG_PLZ2_COL_NAME,
        ADRESSIERUNG_ORT2_COL_NAME,
        ADRESSIERUNG_TELEFON_PRIVAT2_COL_NAME,
        ADRESSIERUNG_TELEFON_GESCHAEFT2_COL_NAME,
        ADRESSIERUNG_NATEL2_COL_NAME,
        ADRESSIERUNG_EMAIL2_COL_NAME,
    };

}
