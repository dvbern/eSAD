package ch.dvbern.oss.hemed.esc.api.shared.person;

import java.util.EnumSet;

import lombok.Getter;
import org.checkerframework.checker.nullness.qual.Nullable;

@Getter
public enum LandCode {

    ALBANIEN(EnumSet.of(Land.ALBANIEN), "Albanien", 7),
    ANDERER_STAAT_OST_SUEDEUROPA(
        EnumSet.of(
            Land.ANDORRA,
            Land.BULGARIEN,
            Land.GRIECHENLAND,
            Land.MALTA,
            Land.MONACO,
            Land.RUMANIEN,
            Land.SAN_MARINO,
            Land.VATIKANSTADT,
            Land.ZYPERN,
            Land.SLOWAKEI,
            Land.TSCHECHIEN,
            Land.ESTLAND,
            Land.LETTLAND,
            Land.LITAUEN
        ), "Anderer Staat in Ost- oder Südeuropa", 30),
    ANDERER_STAAT_WEST_NORDEUROPA(
        EnumSet.of(
            Land.BELGIEN,
            Land.DANEMARK,
            Land.FINNLAND,
            Land.GROSSBRITANNIEN,
            Land.IRLAND,
            Land.ISLAND,
            Land.LIECHTENSTEIN,
            Land.LUXEMBURG,
            Land.NIEDERLANDE,
            Land.NORWEGEN,
            Land.SCHWEDEN
        ), "Anderer Staat in West- oder Nordeuropa", 20),
    DEUTSCHLAND(EnumSet.of(Land.DEUTSCHLAND), "Deutschland", 1),
    FRANKREICH(EnumSet.of(Land.FRANKREICH), "Frankreich", 22),
    ITALIEN(EnumSet.of(Land.ITALIEN), "Italien", 2),
    OESTERREICH(EnumSet.of(Land.OSTERREICH), "Österreich", 21),
    POLEN(EnumSet.of(Land.POLEN), "Polen", 31),
    PORTUGAL(EnumSet.of(Land.PORTUGAL), "Portugal", 4),
    CH(EnumSet.of(Land.SCHWEIZ), "CH", 1),
    SPANIEN(EnumSet.of(Land.SPANIEN), "Spanien", 3),
    TUERKEI(EnumSet.of(Land.TURKEI), "Türkei", 5),
    UNGARN(EnumSet.of(Land.UNGARN), "Ungarn", 32),
    EX_JUGOSLAVIEN_BALKANSTAATEN(
        EnumSet.of(
            Land.SERBIEN,
            Land.KROATIEN,
            Land.SLOWENIEN,
            Land.BOSNIEN_UND_HERZEGOWINA,
            Land.MONTENEGRO,
            Land.NORDMAZEDONIEN,
            Land.KOSOVO
        ), "Ex-Jugoslavien / Balkanstaaten", 6),
    GUS(
        EnumSet.of(
            Land.MOLDOVA,
            Land.RUSSLAND,
            Land.UKRAINE,
            Land.BELARUS,
            Land.ARMENIEN,
            Land.ASERBAIDSCHAN,
            Land.GEORGIEN,
            Land.KASACHSTAN,
            Land.KIRGISISTAN,
            Land.TADSCHIKISTAN,
            Land.TURKMENISTAN,
            Land.USBEKISTAN
        ), "GUS", 11),
    ANDERER_STAAT_AFRIKA(
        EnumSet.of(
            Land.AQUATORIALGUINEA,
            Land.ATHIOPIEN,
            Land.DSCHIBUTI,
            Land.ANGOLA,
            Land.BOTSUANA,
            Land.BURUNDI,
            Land.BENIN,
            Land.COTE_D_IVOIRE,
            Land.GABUN,
            Land.GAMBIA,
            Land.GHANA,
            Land.GUINEA_BISSAU,
            Land.GUINEA,
            Land.KAMERUN,
            Land.CABO_VERDE,
            Land.KENIA,
            Land.KOMOREN,
            Land.KONGO_BRAZZAVILLE,
            Land.KONGO_KINSHASA,
            Land.LESOTHO,
            Land.LIBERIA,
            Land.MADAGASKAR,
            Land.MALAWI,
            Land.MALI,
            Land.MAURETANIEN,
            Land.MAURITIUS,
            Land.MOSAMBIK,
            Land.NIGER,
            Land.NIGERIA,
            Land.BURKINA_FASO,
            Land.SIMBABWE,
            Land.RUANDA,
            Land.SAMBIA,
            Land.SAO_TOME_UND_PRINCIPE,
            Land.SENEGAL,
            Land.SEYCHELLEN,
            Land.SIERRA_LEONE,
            Land.SUDAFRIKA,
            Land.SUDAN,
            Land.NAMIBIA,
            Land.ESWATINI,
            Land.TANSANIA,
            Land.TOGO,
            Land.TSCHAD,
            Land.UGANDA,
            Land.ZENTRALAFRIKANISCHE_REPUBLIK,
            Land.SUDSUDAN,
            Land.WESTSAHARA), "Anderer Staat in Afrika", 60),
    NORDAFRIKA(
        EnumSet.of(
            Land.ALGERIEN,
            Land.LIBYEN,
            Land.MAROKKO,
            Land.TUNESIEN,
            Land.AGYPTEN
        ), "Nordafrika", 61),
    SOMALIA(EnumSet.of(Land.SOMALIA), "Somalia", 62),
    ERITREA(EnumSet.of(Land.ERITREA), "Eritrea", 63),
    SUED_UND_MITTELAMERIKA(
        EnumSet.of(
            Land.ARGENTINIEN,
            Land.BAHAMAS,
            Land.BARBADOS,
            Land.BOLIVIEN,
            Land.BRASILIEN,
            Land.CHILE,
            Land.COSTA_RICA,
            Land.DOMINIKANISCHE_REPUBLIK,
            Land.ECUADOR,
            Land.EL_SALVADOR,
            Land.GUATEMALA,
            Land.GUYANA,
            Land.HAITI,
            Land.BELIZE,
            Land.HONDURAS,
            Land.JAMAIKA,
            Land.KANADA,
            Land.KOLUMBIEN,
            Land.KUBA,
            Land.MEXIKO,
            Land.NICARAGUA,
            Land.PANAMA,
            Land.PARAGUAY,
            Land.PERU,
            Land.SURINAME,
            Land.TRINIDAD_UND_TOBAGO,
            Land.URUGUAY,
            Land.VENEZUELA,
            Land.VEREINIGTE_STAATEN,
            Land.DOMINICA,
            Land.GRENADA,
            Land.ANTIGUA_UND_BARBUDA,
            Land.ST_LUCIA,
            Land.ST_VINCENT_UND_DIE_GRENADINEN,
            Land.ST_KITTS_UND_NEVIS
        ), "Süd- und Mittelamerika", 71),
    AFGHANISTAN(EnumSet.of(Land.AFGHANISTAN), "Afghanistan", 41),
    ANDERER_STAAT_NAHER_MITTLERER_OSTEN(
        EnumSet.of(
            Land.BAHRAIN,
            Land.IRAN,
            Land.ISRAEL,
            Land.JEMEN,
            Land.JORDANIEN,
            Land.KATAR,
            Land.KUWAIT,
            Land.LIBANON,
            Land.OMAN,
            Land.SAUDI_ARABIEN,
            Land.VEREINIGTE_ARABISCHE_EMIRATE), "Anderer Staat im nahen oder mittleren Osten", 40),
    SRI_LANKA(EnumSet.of(Land.SRI_LANKA), "Sri Lanka", 51),
    INDIEN(EnumSet.of(Land.INDIEN), "Indien", 52),
    ANDERER_STAAT_ASIEN(
        EnumSet.of(
            Land.BHUTAN,
            Land.BRUNEI_DARUSSALAM,
            Land.MYANMAR,
            Land.TAIWAN,
            Land.CHINA,
            Land.TIBET,
            Land.INDONESIEN,
            Land.JAPAN,
            Land.KAMBODSCHA,
            Land.LAOS,
            Land.MACAO,
            Land.MALAYSIA,
            Land.MALEDIVEN,
            Land.MONGOLEI,
            Land.NEPAL,
            Land.KOREA_NORD,
            Land.KOREA_SUD,
            Land.PAKISTAN,
            Land.PHILIPPINEN,
            Land.SINGAPUR,
            Land.THAILAND,
            Land.VIETNAM,
            Land.BANGLADESCH,
            Land.TIMOR_LESTE), "Anderer Staat in Asien", 50),
    IRAK(EnumSet.of(Land.IRAK), "Irak", 42),
    SYRIEN(EnumSet.of(Land.SYRIEN), "Syrien", 43),
    AUSTRALIEN_UND_UMGEBUNG(
        EnumSet.of(
            Land.AUSTRALIEN,
            Land.FIDSCHI,
            Land.NAURU,
            Land.VANUATU,
            Land.NEUSEELAND,
            Land.PAPUA_NEUGUINEA,
            Land.TONGA,
            Land.SAMOA,
            Land.SALOMONINSELN,
            Land.TUVALU,
            Land.KIRIBATI,
            Land.MARSHALLINSELN,
            Land.MIKRONESIEN,
            Land.PALAU,
            Land.COOKINSELN
        ), "Australien und Umgebung", 80);

    private final EnumSet<Land> laender;

    private final String beschreibung;

    private final int code;

    LandCode(EnumSet<Land> laender, String beschreibung, int code) {
        this.laender = laender;
        this.beschreibung = beschreibung;
        this.code = code;
    }

    @Nullable
    public static LandCode getCode(@Nullable Land land) {
        if (land == null) {
            return null;
        }
        for (LandCode code : LandCode.values()) {
            if (code.laender.contains(land)) {
                return code;
            }
        }
        return null;
    }
}
