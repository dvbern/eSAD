package ch.dvbern.oss.hemed.esc.api.features.spss.columns;

import ch.dvbern.oss.hemed.esc.api.features.anlass.bewegungsapparat.types.Skoliose;
import ch.dvbern.oss.hemed.esc.api.features.anlass.einladung.Einladung;
import ch.dvbern.oss.hemed.esc.api.shared.util.ValueEnhancedUtil;
import org.checkerframework.checker.nullness.qual.NonNull;

public class BewegungsapparatSkolioseCsvColumn extends AbstractCsvColumn {

    public BewegungsapparatSkolioseCsvColumn(@NonNull CsvColumnConfig config) {
        super(config, null);
    }

    @Override
    @NonNull
    public String extract(@NonNull Einladung einladung) {
        return ExtractorForCsvUtil.extractUntersuchungBewegungsapparat(
            einladung,
            untersuchungBewegungsapparat ->
                ValueEnhancedUtil.translateEnhancedEnumValue(
                    untersuchungBewegungsapparat.getSkoliose(),
                    config.tl(),
                    Skoliose.class)
        );
    }

    @Override
    @NonNull
    protected String getDefaultHeaderKey() {
        return "CsvColumn.bewegungsapparat.skoliose";
    }
}
