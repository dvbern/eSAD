package ch.dvbern.oss.hemed.esc.api.features.spss.columns;

import ch.dvbern.oss.hemed.esc.api.features.anlass.einladung.Einladung;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;

public class InspektionCsvColumn extends AbstractCsvColumn {

    public InspektionCsvColumn(@NonNull CsvColumnConfig config, @Nullable String overrideHeader) {
        super(config, overrideHeader);
    }

    @Override
    @NonNull
    public String extract(@NonNull Einladung einladung) {
        return ExtractorForCsvUtil.extractUntersuchungSehen(
            einladung,
            sehen -> {
                if (sehen.getInspektion() == null) {
                    return "";
                }

                if (!config.forSpssReport()) {
                    return config.tl().translateValue(sehen.getInspektion());
                }

                return switch (sehen.getInspektion()) {
                    case UNAUFFAELLIG -> "1";
                    case AUFFAELLIG -> "2";
                    case NICHT_GEPRUEFT -> "3";
                };
            }
        );
    }

    @Override
    @NonNull
    protected String getDefaultHeaderKey() {
        return "CsvColumn.inspektion";
    }
}
