package ch.dvbern.oss.hemed.esc.api.features.spss.columns;

import ch.dvbern.oss.hemed.esc.api.features.anlass.Einladung;
import ch.dvbern.oss.hemed.esc.api.features.anlass.aerztlicheuntersuchung.types.Bewegungsapparat;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;

public class BewegungsapparatCsvColumn extends AbstractCsvColumn {

    private final Bewegungsapparat bewegungsapparat;

    public BewegungsapparatCsvColumn(
        @NonNull CsvColumnConfig config,
        @Nullable String overrideHeader,
        @NonNull Bewegungsapparat bewegungsapparat) {
        super(config, overrideHeader);
        this.bewegungsapparat = bewegungsapparat;
    }

    @Override
    @NonNull
    public String extract(@NonNull Einladung einladung) {
        return ExtractorForCsvUtil.extractUntersuchungArztBeurteilung(
            einladung,
            untersuchungArzt ->
                untersuchungArzt.getBewegungsapparat().contains(bewegungsapparat) ? "1" : ""
        );
    }

    @Override
    @NonNull
    protected String getDefaultHeaderKey() {
        return "CsvColumn.bewegungsapparat";
    }
}
