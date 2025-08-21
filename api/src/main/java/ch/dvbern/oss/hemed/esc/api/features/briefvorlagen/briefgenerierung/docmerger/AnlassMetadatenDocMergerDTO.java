package ch.dvbern.oss.hemed.esc.api.features.briefvorlagen.briefgenerierung.docmerger;

import lombok.Value;

@Value
public class AnlassMetadatenDocMergerDTO {
    String datum;
    String zeit;
    String mpa;
    String arzt;
}
