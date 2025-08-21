package ch.dvbern.oss.hemed.esc.api.features.briefvorlagen.briefgenerierung.docmerger;

import lombok.Value;

@Value
public class ExternerArztDocMergerDTO {
    String anrede;
    String name;
    String adresse;
    String telefon;
    String email;
}
