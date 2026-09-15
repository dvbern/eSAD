package ch.dvbern.oss.hemed.esc.api.features.spss.columns;

import ch.dvbern.oss.hemed.esc.api.features.anlass.einladung.Einladung;
import ch.dvbern.oss.hemed.esc.api.features.anlass.groessegewicht.types.PerzentileBmi;
import ch.dvbern.oss.hemed.esc.api.shared.util.ValueEnhancedUtil;

import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;

public class BMIBeurteilungCsvColumn extends AbstractCsvColumn {

    public BMIBeurteilungCsvColumn(
        @NonNull CsvColumnConfig config,
        @Nullable String overrideHeader
    ) {
        super(config, overrideHeader);
    }

    @Override
    @NonNull
    public String extract(@NonNull Einladung einladung) {
        return ExtractorForCsvUtil.extractUntersuchungGroesseGewicht(
            einladung,
            untersuchungGroesseGewicht -> {
                PerzentileBmi resultat = ValueEnhancedUtil.getEnumValueOrNull(
                    untersuchungGroesseGewicht.getBmiPerzentileSingleValue(), PerzentileBmi.class);
                if (resultat == null) {
                    return "";
                }
                if (!config.forSpssReport()) {
                    return config.tl().translateValue(resultat);
                }
                return switch (resultat) {
                    case PerzentileBmi.NORMAL -> "1";
                    case PerzentileBmi.PERZENTILE_TIEF -> "2";
                    case PerzentileBmi.PERZENTILE_HOCH -> "3";
                    case PerzentileBmi.NICHT_GEPRUEFT -> "4";
                    default -> throw new IllegalStateException("Unexpected value for BmiPerzentile: "
                        + untersuchungGroesseGewicht.getBmiPerzentileSingleValue());
                };
            }
        );
    }

    @Override
    @NonNull
    protected String getDefaultHeaderKey() {
        return "CsvColumn.bmiBeurteilung";
    }
}
