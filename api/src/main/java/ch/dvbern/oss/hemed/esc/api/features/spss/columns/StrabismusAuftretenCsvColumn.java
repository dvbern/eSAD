package ch.dvbern.oss.hemed.esc.api.features.spss.columns;

import ch.dvbern.oss.hemed.esc.api.features.anlass.Einladung;
import ch.dvbern.oss.hemed.esc.api.features.anlass.types.StrabismusAuftretenResultat;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;

public class StrabismusAuftretenCsvColumn extends AbstractCsvColumn {

    public StrabismusAuftretenCsvColumn(@NonNull CsvColumnConfig config, @Nullable String overrideHeader) {
        super(config, overrideHeader);
    }

    @Override
    @NonNull
    public String extract(@NonNull Einladung einladung) {
        return ExtractorForCsvUtil.extractUntersuchungSehen(
            einladung,
            sehen -> {
                StrabismusAuftretenResultat resultat = sehen.getStrabismusAuftreten();
                if (resultat == null) {
                    return "";
                }
                return switch (resultat) {
                    case KEIN_STRABISMUS -> "";
                    case RECHTS -> "1";
                    case LINKS -> "2";
                    case ALTERNNIEREND -> "3";
                    case NICHT_GEPRUEFT -> "4";
                };
            }
        );
    }

    @Override
    @NonNull
    protected String getDefaultHeaderKey() {
        return "CsvColumn.strabismusAuftreten";
    }
}
