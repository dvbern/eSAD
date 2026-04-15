package ch.dvbern.oss.hemed.esc.api.features.spss.columns;

import ch.dvbern.oss.hemed.esc.api.features.anlass.einladung.Einladung;
import ch.dvbern.oss.hemed.esc.api.features.anlass.schulrelevantebeeintraechtigung.types.Grafomotorik;
import ch.dvbern.oss.hemed.esc.api.shared.util.ValueEnhancedUtil;
import org.checkerframework.checker.nullness.qual.NonNull;

public class GrafomotorikCsvColumn extends AbstractCsvColumn {

    public GrafomotorikCsvColumn(
        @NonNull CsvColumnConfig config) {
        super(config, null);
    }

    @Override
    @NonNull
    public String extract(@NonNull Einladung einladung) {
        return ExtractorForCsvUtil.extractUntersuchungSchulrelevanteBeeintraechtigung(
            einladung,
            beeintraechtigung ->
                ValueEnhancedUtil.translateEnhancedEnumValue(
                    beeintraechtigung.getGrafomotorik(),
                    config.tl(),
                    Grafomotorik.class)
        );
    }

    @Override
    @NonNull
    protected String getDefaultHeaderKey() {
        return "CsvColumn.schulrelevanteBeeintraechtigung.grafomotorik";
    }
}

