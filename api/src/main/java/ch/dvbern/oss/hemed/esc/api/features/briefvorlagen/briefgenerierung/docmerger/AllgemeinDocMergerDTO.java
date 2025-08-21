package ch.dvbern.oss.hemed.esc.api.features.briefvorlagen.briefgenerierung.docmerger;

import lombok.Value;

@Value
public class AllgemeinDocMergerDTO {
    String aktuellesDatum;
    String benutzerTitel;
    String benutzer;
    String benutzerEmail;
    String benutzerTelefon;
    String benutzerFunktion;
}
