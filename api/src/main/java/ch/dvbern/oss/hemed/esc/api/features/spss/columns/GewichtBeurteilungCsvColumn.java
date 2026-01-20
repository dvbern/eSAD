package ch.dvbern.oss.hemed.esc.api.features.spss.columns;

import ch.dvbern.oss.hemed.esc.api.features.anlass.einladung.Einladung;
import ch.dvbern.oss.hemed.esc.api.features.anlass.groessegewicht.types.PerzentileGewicht;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;

public class GewichtBeurteilungCsvColumn extends AbstractCsvColumn {

    public GewichtBeurteilungCsvColumn(
        @NonNull CsvColumnConfig config,
        @Nullable String overrideHeader) {
        super(config, overrideHeader);
    }

    @Override
    @NonNull
    public String extract(@NonNull Einladung einladung) {
        return ExtractorForCsvUtil.extractUntersuchungGroesseGewicht(
            einladung,
            untersuchungGroesseGewicht -> {
                if (untersuchungGroesseGewicht.getGewichtPerzentile() == null) {
                    return "";
                }
                if (!config.forSpssReport()) {
                    return config.tl().translateValue(untersuchungGroesseGewicht.getGewichtPerzentile());
                }
                return switch (untersuchungGroesseGewicht.getGewichtPerzentile()) {
                    case PerzentileGewicht.NORMAL -> "1";
                    case PerzentileGewicht.PERZENTILE_TIEF -> "5";
                    case PerzentileGewicht.PERZENTILE_HOCH -> "6";
                    case PerzentileGewicht.NICHT_GEPRUEFT -> "4";
                };
            }
        );
    }

    @Override
    @NonNull
    protected String getDefaultHeaderKey() {
        return "CsvColumn.gewichtBeurteilung";
    }
}
