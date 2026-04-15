package ch.dvbern.oss.hemed.esc.api.features.spss.columns;

import ch.dvbern.oss.hemed.esc.api.features.anlass.einladung.Einladung;
import ch.dvbern.oss.hemed.esc.api.features.anlass.schulrelevantebeeintraechtigung.types.Feinmotorik;
import ch.dvbern.oss.hemed.esc.api.shared.util.ValueEnhancedUtil;
import org.checkerframework.checker.nullness.qual.NonNull;

public class FeinmotorikCsvColumn extends AbstractCsvColumn {

    public FeinmotorikCsvColumn(
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
                    beeintraechtigung.getFeinmotorik(),
                    config.tl(),
                    Feinmotorik.class)
        );
    }

    @Override
    @NonNull
    protected String getDefaultHeaderKey() {
        return "CsvColumn.schulrelevanteBeeintraechtigung.feinmotorik";
    }
}

