package ch.dvbern.oss.hemed.esc.api.features.briefvorlagen.briefgenerierung.docmerger;

import java.util.Map;

import lombok.Value;

@Value
public class HoerenAudiogrammDocMergerDTO {
    String beurteilungErkaeltung;
    String beurteilungHoerhilfe;
    Map<String, String> audiogrammRechts;
    Map<String, String> audiogrammLinks;
    String zusammenfassungOhren;
    String beurteilungAudiogramm;
    String empfehlungUeberpruefungArzt;
    String audiogrammSimpel;
    String hoerhilfe;
}
