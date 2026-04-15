package ch.dvbern.oss.hemed.esc.api.features.spss.columns;

import ch.dvbern.oss.hemed.esc.api.features.anlass.einladung.Einladung;
import ch.dvbern.oss.hemed.esc.api.features.anlass.sehen.types.DistanzSehenResultat;
import ch.dvbern.oss.hemed.esc.api.shared.printer.PrintUtil;
import ch.dvbern.oss.hemed.esc.api.shared.util.ValueEnhancedUtil;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;

public class FernvisusCsvColumn extends AbstractCsvColumn {

    private final DistanzSehenResultat resultat;

    public FernvisusCsvColumn(
        @NonNull CsvColumnConfig config,
        @Nullable String overrideHeader,
        @NonNull DistanzSehenResultat resultat
    ) {
        super(config, overrideHeader);
        this.resultat = resultat;
    }

    @Override
    @NonNull
    public String extract(@NonNull Einladung einladung) {
        return ExtractorForCsvUtil.extractUntersuchungSehen(
            einladung,
            sehen -> {
                boolean resultatBool = ValueEnhancedUtil.getEnumSetOrEmpty(sehen.getFernvisus(), DistanzSehenResultat.class)
                    .contains(this.resultat);
                if (config.forSpssReport()) {
                    return resultatBool ? "1" : "";
                }
                return PrintUtil.trimToPlaceholder(resultatBool, config.tl());
            }
        );
    }

    @Override
    @NonNull
    protected String getDefaultHeaderKey() {
        return "CsvColumn.fernvisus." + this.resultat;
    }
}
