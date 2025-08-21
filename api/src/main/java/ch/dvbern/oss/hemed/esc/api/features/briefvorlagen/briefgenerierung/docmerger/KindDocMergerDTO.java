package ch.dvbern.oss.hemed.esc.api.features.briefvorlagen.briefgenerierung.docmerger;

import lombok.Value;

@Value
public class KindDocMergerDTO {
    String vorname;
    String nachname;
    String geburtsdatum;
    String adresse;
    String zustaendigArzt;
    String schulhaus;
    String klasseStufe;
    String klasseBezeichnung;
    String nachkontrollen;
    String kontaktpersonName;
    String kontaktpersonVorname;
}
