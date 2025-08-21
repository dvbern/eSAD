/*
 * Copyright (C) 2023 DV Bern AG, Switzerland
 *
 * Das vorliegende Dokument, einschliesslich aller seiner Teile, ist urheberrechtlich
 * geschuezt. Jede Verwertung ist ohne Zustimmung der DV Bern AG unzulaessig. Dies gilt
 * insbesondere für Vervielfaeltigungen, die Einspeicherung und Verarbeitung in
 * elektronischer Form. Wird das Dokument einem Kunden im Rahmen der Projektarbeit zur
 * Ansicht uebergeben, ist jede weitere Verteilung durch den Kunden an Dritte untersagt.
 */

package ch.dvbern.oss.hemed.esc.api.shared.person;

import java.util.HashMap;
import java.util.Map;

import lombok.Getter;
import org.checkerframework.checker.nullness.qual.NonNull;

@Getter
public enum Land {

    ALBANIEN("AL"),
    ANDORRA("AD"),
    BELGIEN("BE"),
    BULGARIEN("BG"),
    DANEMARK("DK"),
    DEUTSCHLAND("DE"),
    FINNLAND("FI"),
    FRANKREICH("FR"),
    GRIECHENLAND("GR"),
    GROSSBRITANNIEN("GB"),
    IRLAND("IE"),
    ISLAND("IS"),
    ITALIEN("IT"),
    LIECHTENSTEIN("LI"),
    LUXEMBURG("LU"),
    MALTA("MT"),
    MONACO("MC"),
    NIEDERLANDE("NL"),
    NORWEGEN("NO"),
    OSTERREICH("AT"),
    POLEN("PL"),
    PORTUGAL("PT"),
    RUMANIEN("RO"),
    SAN_MARINO("SM"),
    SCHWEDEN("SE"),
    SCHWEIZ("CH"),
    SPANIEN("ES"),
    TURKEI("TR"),
    UNGARN("HU"),
    VATIKANSTADT("VA"),
    ZYPERN("CY"),
    SLOWAKEI("SK"),
    TSCHECHIEN("CZ"),
    SERBIEN("RS"),
    KROATIEN("HR"),
    SLOWENIEN("SI"),
    BOSNIEN_UND_HERZEGOWINA("BA"),
    MONTENEGRO("ME"),
    NORDMAZEDONIEN("MK"),
    KOSOVO("XK"),
    ESTLAND("EE"),
    LETTLAND("LV"),
    LITAUEN("LT"),
    MOLDOVA("MD"),
    RUSSLAND("RU"),
    UKRAINE("UA"),
    BELARUS("BY"),
    AQUATORIALGUINEA("GQ"),
    ATHIOPIEN("ET"),
    DSCHIBUTI("DJ"),
    ALGERIEN("DZ"),
    ANGOLA("AO"),
    BOTSUANA("BW"),
    BURUNDI("BI"),
    BENIN("BJ"),
    COTE_D_IVOIRE("CI"),
    GABUN("GA"),
    GAMBIA("GM"),
    GHANA("GH"),
    GUINEA_BISSAU("GW"),
    GUINEA("GN"),
    KAMERUN("CM"),
    CABO_VERDE("CV"),
    KENIA("KE"),
    KOMOREN("KM"),
    KONGO_BRAZZAVILLE("CG"),
    KONGO_KINSHASA("CD"),
    LESOTHO("LS"),
    LIBERIA("LR"),
    LIBYEN("LY"),
    MADAGASKAR("MG"),
    MALAWI("MW"),
    MALI("ML"),
    MAROKKO("MA"),
    MAURETANIEN("MR"),
    MAURITIUS("MU"),
    MOSAMBIK("MZ"),
    NIGER("NE"),
    NIGERIA("NG"),
    BURKINA_FASO("BF"),
    SIMBABWE("ZW"),
    RUANDA("RW"),
    SAMBIA("ZM"),
    SAO_TOME_UND_PRINCIPE("ST"),
    SENEGAL("SN"),
    SEYCHELLEN("SC"),
    SIERRA_LEONE("SL"),
    SOMALIA("SO"),
    SUDAFRIKA("ZA"),
    SUDAN("SD"),
    NAMIBIA("NA"),
    ESWATINI("SZ"),
    TANSANIA("TZ"),
    TIBET("TI"),
    TOGO("TG"),
    TSCHAD("TD"),
    TUNESIEN("TN"),
    UGANDA("UG"),
    AGYPTEN("EG"),
    ZENTRALAFRIKANISCHE_REPUBLIK("CF"),
    ERITREA("ER"),
    SUDSUDAN("SS"),
    WESTSAHARA("EH"),
    ARGENTINIEN("AR"),
    BAHAMAS("BS"),
    BARBADOS("BB"),
    BOLIVIEN("BO"),
    BRASILIEN("BR"),
    CHILE("CL"),
    COSTA_RICA("CR"),
    DOMINIKANISCHE_REPUBLIK("DO"),
    ECUADOR("EC"),
    EL_SALVADOR("SV"),
    GUATEMALA("GT"),
    GUYANA("GY"),
    HAITI("HT"),
    BELIZE("BZ"),
    HONDURAS("HN"),
    JAMAIKA("JM"),
    KANADA("CA"),
    KOLUMBIEN("CO"),
    KUBA("CU"),
    MEXIKO("MX"),
    NICARAGUA("NI"),
    PANAMA("PA"),
    PARAGUAY("PY"),
    PERU("PE"),
    SURINAME("SR"),
    TRINIDAD_UND_TOBAGO("TT"),
    URUGUAY("UY"),
    VENEZUELA("VE"),
    VEREINIGTE_STAATEN("US"),
    DOMINICA("DM"),
    GRENADA("GD"),
    ANTIGUA_UND_BARBUDA("AG"),
    ST_LUCIA("LC"),
    ST_VINCENT_UND_DIE_GRENADINEN("VC"),
    ST_KITTS_UND_NEVIS("KN"),
    AFGHANISTAN("AF"),
    BAHRAIN("BH"),
    BHUTAN("BT"),
    BRUNEI_DARUSSALAM("BN"),
    MYANMAR("MM"),
    SRI_LANKA("LK"),
    TAIWAN("TW"),
    CHINA("CN"),
    INDIEN("IN"),
    INDONESIEN("ID"),
    IRAK("IQ"),
    IRAN("IR"),
    ISRAEL("IL"),
    JAPAN("JP"),
    JEMEN("YE"),
    JORDANIEN("JO"),
    KAMBODSCHA("KH"),
    KATAR("QA"),
    KUWAIT("KW"),
    LAOS("LA"),
    LIBANON("LB"),
    MACAO("MO"),
    MALAYSIA("MY"),
    MALEDIVEN("MV"),
    OMAN("OM"),
    MONGOLEI("MN"),
    NEPAL("NP"),
    KOREA_NORD("KP"),
    VEREINIGTE_ARABISCHE_EMIRATE("AE"),
    PAKISTAN("PK"),
    PHILIPPINEN("PH"),
    SAUDI_ARABIEN("SA"),
    SINGAPUR("SG"),
    KOREA_SUD("KR"),
    SYRIEN("SY"),
    THAILAND("TH"),
    VIETNAM("VN"),
    BANGLADESCH("BD"),
    TIMOR_LESTE("TL"),
    ARMENIEN("AM"),
    ASERBAIDSCHAN("AZ"),
    GEORGIEN("GE"),
    KASACHSTAN("KZ"),
    KIRGISISTAN("KG"),
    TADSCHIKISTAN("TJ"),
    TURKMENISTAN("TM"),
    USBEKISTAN("UZ"),
    AUSTRALIEN("AU"),
    FIDSCHI("FJ"),
    NAURU("NR"),
    VANUATU("VU"),
    NEUSEELAND("NZ"),
    PAPUA_NEUGUINEA("PG"),
    TONGA("TO"),
    SAMOA("WS"),
    SALOMONINSELN("SB"),
    TUVALU("TV"),
    KIRIBATI("KI"),
    MARSHALLINSELN("MH"),
    MIKRONESIEN("FM"),
    PALAU("PW"),
    COOKINSELN("CK");

    private static final Map<String, Land> LAND_MAP = new HashMap<>();

    private final String isoCode;

    static {
        for (Land land : values()) {
            LAND_MAP.put(land.getIsoCode(), land);
        }
    }

    Land(@NonNull String isoCode) {
        this.isoCode = isoCode;
    }

    @NonNull
    public static Land getByIsoCode(@NonNull String code) {
        if (code.trim().isEmpty()) {
            throw new IllegalArgumentException("Invalid country code");
        }
        String trimmedCode = code.trim().toUpperCase();
        Land land = LAND_MAP.get(trimmedCode);
        if (land == null) {
            throw new IllegalArgumentException("Country code not found: " + trimmedCode);
        }
        return land;
    }

    /**
     * Escada does not use the standard ISO codes for some countries. This method maps the Escada country code to our code
     */
    @NonNull
    public static Land getByEscadaIsoCode(@NonNull String escadaIsoCode) {
        if (escadaIsoCode.equals("MKD")) {
            return NORDMAZEDONIEN;
        }
        if (escadaIsoCode.equals("KO")) {
            return KOSOVO;
        }
        return getByIsoCode(escadaIsoCode);
    }
}
