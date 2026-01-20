package ch.dvbern.oss.hemed.esc.api.features.spss.columns;

import ch.dvbern.oss.hemed.esc.api.features.anlass.einladung.Einladung;
import ch.dvbern.oss.hemed.esc.api.features.anlass.sehen.types.DistanzSehenResultat;
import ch.dvbern.oss.hemed.esc.api.shared.printer.PrintUtil;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;

public class NahvisusCsvColumn extends AbstractCsvColumn {

    private final DistanzSehenResultat resultat;

    public NahvisusCsvColumn(
        @NonNull CsvColumnConfig config,
        @Nullable String overrideHeader,
        @NonNull DistanzSehenResultat resultat) {
        super(config, overrideHeader);
        this.resultat = resultat;
    }

    @Override
    @NonNull
    public String extract(@NonNull Einladung einladung) {
        return ExtractorForCsvUtil.extractUntersuchungSehen(
            einladung,
            sehen -> {
                if (config.forSpssReport()) {
                    return sehen.getNahvisus().contains(resultat) ? "1" : "";
                }
                return PrintUtil.trimToPlaceholder(sehen.getNahvisus().contains(resultat), config.tl());
            }
        );
    }

    @Override
    @NonNull
    protected String getDefaultHeaderKey() {
        return "CsvColumn.nahvisus." + this.resultat;
    }
}
