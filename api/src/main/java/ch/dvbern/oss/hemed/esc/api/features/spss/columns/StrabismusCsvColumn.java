package ch.dvbern.oss.hemed.esc.api.features.spss.columns;

import ch.dvbern.oss.hemed.esc.api.features.anlass.einladung.Einladung;
import ch.dvbern.oss.hemed.esc.api.features.anlass.sehen.types.StrabismusResultat;
import ch.dvbern.oss.hemed.esc.api.shared.printer.PrintUtil;
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
            sehen -> {
                if (config.forSpssReport()) {
                    return sehen.getStrabismus().contains(strabismusResultat) ? "1" : "";
                }
                return PrintUtil.trimToPlaceholder(sehen.getStrabismus().contains(strabismusResultat), config.tl());
            }
        );
    }

    @Override
    @NonNull
    protected String getDefaultHeaderKey() {
        return "CsvColumn.strabismus." + this.strabismusResultat;
    }
}
