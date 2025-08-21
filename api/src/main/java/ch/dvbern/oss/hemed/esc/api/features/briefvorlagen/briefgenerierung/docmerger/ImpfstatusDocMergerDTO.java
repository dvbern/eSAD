package ch.dvbern.oss.hemed.esc.api.features.briefvorlagen.briefgenerierung.docmerger;

import lombok.Value;

@Value
public class ImpfstatusDocMergerDTO {
    String krankheitKey;
    String krankheit;
    String anzahlImpfungen;
    String datumLetzteImpfung;
    String impfschutzGegeben;
    String impfempfehlung;
    String impfentscheid;
}
