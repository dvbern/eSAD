package ch.dvbern.oss.hemed.esc.api.features.briefvorlagen.briefgenerierung.docmerger;

import lombok.Value;

@Value
public class HoerenOtoskopieDocMergerDTO {
    String otoskopieRechts;
    String otoskopieLinks;
    String zusammenfassungOhren;
}
