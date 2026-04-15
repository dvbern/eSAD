package ch.dvbern.oss.hemed.esc.api.features.briefvorlagen.briefgenerierung.docmerger;

import lombok.Value;

@Value
public class SehenDocMergerDTO {
    String elternBrilleLinsen;
    String arztBrilleLinsen;
    String elternStereotest;
    String arztStereotest;
    String farbsehen;
    String tafel;
    String fernvisus;
    String fernvisusSehschaerfeRechts;
    String fernvisusSehschaerfeLinks;
    String fernvisusAugeLinks;
    String fernvisusAugeRechts;
    String nahvisus;
    String nahvisusSehschaerfeRechts;
    String nahvisusSehschaerfeLinks;
    String strabismus;
    String strabismusAuspraegung;
    String strabismusAuftreten;
    String inspektion;
    String konvergenz;
    String beurteilung;
    String motilitaet;
    String empfehlungUeberpruefungAugenarzt;
}
