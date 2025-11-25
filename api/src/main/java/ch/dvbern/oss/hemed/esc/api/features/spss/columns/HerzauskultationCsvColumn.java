package ch.dvbern.oss.hemed.esc.api.features.spss.columns;

import ch.dvbern.oss.hemed.esc.api.features.anlass.einladung.Einladung;
import ch.dvbern.oss.hemed.esc.api.features.anlass.aerztlicheuntersuchung.types.Herzauskultation;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;

public class HerzauskultationCsvColumn extends AbstractCsvColumn {

    public HerzauskultationCsvColumn(
        @NonNull CsvColumnConfig config,
        @Nullable String overrideHeader) {
        super(config, overrideHeader);
    }

    @Override
    @NonNull
    public String extract(@NonNull Einladung einladung) {
        return ExtractorForCsvUtil.extractUntersuchungArztBeurteilung(
            einladung,
            untersuchungArzt -> {
                if (untersuchungArzt.getHerzauskultation() == null) {
                    return "";
                }
                return switch (untersuchungArzt.getHerzauskultation()) {
                    case Herzauskultation.UNAUFFAELLIG -> "1";
                    case Herzauskultation.AKZIDENTELLES_SYSTOLIKUM -> "2";
                    case Herzauskultation.ANDERES -> "5";
                    case Herzauskultation.NICHT_GEPRUEFT -> "6";
                };
            }
        );
    }

    @Override
    @NonNull
    protected String getDefaultHeaderKey() {
        return "CsvColumn.herzauskultation";
    }
}
