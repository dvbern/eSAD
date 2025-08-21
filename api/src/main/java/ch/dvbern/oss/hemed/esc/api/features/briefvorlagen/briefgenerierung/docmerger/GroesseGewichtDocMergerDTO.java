package ch.dvbern.oss.hemed.esc.api.features.briefvorlagen.briefgenerierung.docmerger;

import lombok.Value;

@Value
public class GroesseGewichtDocMergerDTO {
    String groesseWert;
    String groesseBeurteilung;
    String gewichtWert;
    String gewichtBeurteilung;
    String bmiWert;
    String bmiBeurteilung;
}
