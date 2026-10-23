package ch.dvbern.oss.hemed.esc.api.features.briefvorlagen.briefgenerierung.docmerger;

import lombok.AllArgsConstructor;
import lombok.Value;
import org.checkerframework.checker.nullness.qual.NonNull;

@Value
@AllArgsConstructor
public class ImpfungDocMergerDTO {
    @NonNull
    String impfungList;
    @NonNull
    String impfausweisNichtVorhanden;
    @NonNull
    String massnahmeImpfungBeimHausarzt;

    public ImpfungDocMergerDTO(@NonNull String impfungList) {
        this(
            impfungList,
            "",
            ""
        );
    }
}
