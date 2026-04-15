package ch.dvbern.oss.hemed.esc.api.features.spss.columns;

import ch.dvbern.oss.hemed.esc.api.features.anlass.einladung.Einladung;
import ch.dvbern.oss.hemed.esc.api.features.anlass.schulrelevantebeeintraechtigung.types.FreiesGehen;
import ch.dvbern.oss.hemed.esc.api.shared.util.ValueEnhancedUtil;
import org.checkerframework.checker.nullness.qual.NonNull;

public class FreiesGehenCsvColumn extends AbstractCsvColumn {

    public FreiesGehenCsvColumn(
        @NonNull CsvColumnConfig config) {
        super(config, null);
    }

    @Override
    @NonNull
    public String extract(@NonNull Einladung einladung) {
        return ExtractorForCsvUtil.extractUntersuchungSchulrelevanteBeeintraechtigung(
            einladung,
            beeintraechtigung -> ValueEnhancedUtil.translateEnhancedEnumValue(
                beeintraechtigung.getFreiesGehen(),
                config.tl(),
                FreiesGehen.class)
        );
    }

    @Override
    @NonNull
    protected String getDefaultHeaderKey() {
        return "CsvColumn.schulrelevanteBeeintraechtigung.freiesGehen";
    }
}

