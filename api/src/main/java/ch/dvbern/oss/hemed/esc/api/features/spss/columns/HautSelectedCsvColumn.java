package ch.dvbern.oss.hemed.esc.api.features.spss.columns;

import ch.dvbern.oss.hemed.esc.api.features.anlass.einladung.Einladung;
import ch.dvbern.oss.hemed.esc.api.features.anlass.aerztlicheuntersuchung.types.Haut;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;

/**
 * Mehrere Columns für Haut: Fuer jeden Enum-Wert eine Column, die "1" schreibt, wenn der Wert gesetzt ist, sonst leer.
 */
public class HautSelectedCsvColumn extends AbstractCsvColumn {

    private final Haut haut;

    public HautSelectedCsvColumn(
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
