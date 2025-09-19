package ch.dvbern.oss.hemed.esc.api.features.spss.columns;

import ch.dvbern.oss.hemed.esc.api.features.anlass.Einladung;
import ch.dvbern.oss.hemed.esc.api.features.anlass.aerztlicheuntersuchung.types.Haut;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;

public class HautCsvColumn extends AbstractCsvColumn {

    private final Haut haut;

    public HautCsvColumn(
        @NonNull CsvColumnConfig config,
        @Nullable String overrideHeader,
        @NonNull Haut haut) {
        super(config, overrideHeader);
        this.haut = haut;
    }

    @Override
    @NonNull
    public String extract(@NonNull Einladung einladung) {
        return ExtractorForCsvUtil.extractUntersuchungArztBeurteilung(
            einladung,
            untersuchungArzt ->
                (untersuchungArzt.getHaut() != null && untersuchungArzt.getHaut() == haut) ? "1" : ""
        );
    }

    @Override
    @NonNull
    protected String getDefaultHeaderKey() {
        return "CsvColumn.haut";
    }
}
