package ch.dvbern.oss.hemed.esc.api.features.spss.columns;

import ch.dvbern.oss.hemed.esc.api.features.anlass.bewegungsapparat.types.Haltungsschwaeche;
import ch.dvbern.oss.hemed.esc.api.features.anlass.einladung.Einladung;
import ch.dvbern.oss.hemed.esc.api.shared.util.ValueEnhancedUtil;
import org.checkerframework.checker.nullness.qual.NonNull;

public class BewegungsapparatHaltungsschwacheCsvColumn extends AbstractCsvColumn {

    public BewegungsapparatHaltungsschwacheCsvColumn(@NonNull CsvColumnConfig config) {
        super(config, null);
    }

    @Override
    @NonNull
    public String extract(@NonNull Einladung einladung) {
        return ExtractorForCsvUtil.extractUntersuchungBewegungsapparat(
            einladung,
            untersuchungBewegungsapparat ->
                ValueEnhancedUtil.translateEnhancedEnumValue(
                    untersuchungBewegungsapparat.getHaltungsschwaeche(),
                    config.tl(),
                    Haltungsschwaeche.class)
        );
    }

    @Override
    @NonNull
    protected String getDefaultHeaderKey() {
        return "CsvColumn.bewegungsapparat.haltungssschwaeche";
    }
}
