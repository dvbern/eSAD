package ch.dvbern.oss.hemed.esc.api.features.spss.columns;

import ch.dvbern.oss.hemed.esc.api.features.anlass.einladung.Einladung;
import ch.dvbern.oss.hemed.esc.api.features.anlass.sehen.types.DistanzSehenAugeEinzelnResultat;
import ch.dvbern.oss.hemed.esc.api.shared.util.ValueEnhancedUtil;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;

public class FernvisusAugeRechtsCsvColumn extends AbstractCsvColumn {

    private final DistanzSehenAugeEinzelnResultat resultat;

    public FernvisusAugeRechtsCsvColumn(
        @NonNull CsvColumnConfig config,
        @Nullable String overrideHeader,
        @NonNull DistanzSehenAugeEinzelnResultat resultat
    ) {
        super(config, overrideHeader);
        this.resultat = resultat;
    }

    @Override
    @NonNull
    public String extract(@NonNull Einladung einladung) {
        return ExtractorForCsvUtil.extractUntersuchungSehen(
            einladung,
            sehen ->
                ValueEnhancedUtil.translateEnumValueForColumnToFlag(
                    sehen.getFernvisusAugeRechts(),
                    this.resultat,
                    config.tl(),
                     DistanzSehenAugeEinzelnResultat.class
                )
        );
    }

    @Override
    @NonNull
    protected String getDefaultHeaderKey() {
        return "CsvColumn.fernvisusAugeRechts." + this.resultat;
    }
}
