package ch.dvbern.oss.hemed.esc.api.features.spss;

import java.util.List;

import ch.dvbern.oss.hemed.esc.api.features.anlass.types.AnlassTyp;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.checkerframework.checker.nullness.qual.NonNull;

@AllArgsConstructor
@Getter
public enum SpssReportType {

    KINDERGARTEN(List.of(AnlassTyp.KINDERGARTEN)),
    KLASSE_4(List.of(AnlassTyp.KLASSE_4)),
    KLASSE_8(List.of(AnlassTyp.KLASSE_8)),
    KLASSE_9(List.of(AnlassTyp.KLASSE_9)),
    FREMDSPRACHEN(List.of(AnlassTyp.FREMDSPRACHEN)),
    IMPF_ANLAESSE(List.of(AnlassTyp.IMPFAKTION, AnlassTyp.IMPFSPRECHSTUNDE, AnlassTyp.IMPFTERMIN));

    @NonNull
    private final List<AnlassTyp> anlassStatusList;
}
