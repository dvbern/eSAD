package ch.dvbern.oss.hemed.esc.api.features.spss.columns;

import ch.dvbern.oss.hemed.esc.api.features.anlass.einladung.Einladung;
import ch.dvbern.oss.hemed.esc.api.features.anlass.schulrelevantebeeintraechtigung.types.GrobmotorikEinbeinhuepfen;
import ch.dvbern.oss.hemed.esc.api.shared.util.ValueEnhancedUtil;
import org.checkerframework.checker.nullness.qual.NonNull;

public class GrobmotorikEinbeinhuepfenCsvColumn extends AbstractCsvColumn {

    public GrobmotorikEinbeinhuepfenCsvColumn(
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
                    beeintraechtigung.getGrobmotorikEinbeinhuepfen(),
                    config.tl(),
                    GrobmotorikEinbeinhuepfen.class)
        );
    }

    @Override
    @NonNull
    protected String getDefaultHeaderKey() {
        return "CsvColumn.schulrelevanteBeeintraechtigung.grobmotorikEinbeinhuepfen";
    }
}

