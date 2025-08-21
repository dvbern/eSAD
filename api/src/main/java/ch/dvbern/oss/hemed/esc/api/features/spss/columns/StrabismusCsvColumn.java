package ch.dvbern.oss.hemed.esc.api.features.spss.columns;

import ch.dvbern.oss.hemed.esc.api.features.anlass.Einladung;
import ch.dvbern.oss.hemed.esc.api.features.anlass.types.StrabismusResultat;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;

public class StrabismusCsvColumn extends AbstractCsvColumn {

    private final StrabismusResultat strabismusResultat;

    public StrabismusCsvColumn(@NonNull CsvColumnConfig config, @Nullable String overrideHeader, @NonNull StrabismusResultat strabismusResultat) {
        super(config, overrideHeader);
        this.strabismusResultat = strabismusResultat;
    }

    @Override
    @NonNull
    public String extract(@NonNull Einladung einladung) {
        return ExtractorForCsvUtil.extractUntersuchungSehen(
            einladung,
            sehen -> sehen.getStrabismus().contains(strabismusResultat) ? "1" : ""
        );
    }

    @Override
    @NonNull
    protected String getDefaultHeaderKey() {
        return "CsvColumn.strabismus";
    }
}
