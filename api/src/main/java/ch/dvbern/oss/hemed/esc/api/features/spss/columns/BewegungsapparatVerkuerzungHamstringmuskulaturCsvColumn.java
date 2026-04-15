package ch.dvbern.oss.hemed.esc.api.features.spss.columns;

import ch.dvbern.oss.hemed.esc.api.features.anlass.bewegungsapparat.types.VerkuerzungHamstringmuskulatur;
import ch.dvbern.oss.hemed.esc.api.features.anlass.einladung.Einladung;
import ch.dvbern.oss.hemed.esc.api.shared.util.ValueEnhancedUtil;
import org.checkerframework.checker.nullness.qual.NonNull;

public class BewegungsapparatVerkuerzungHamstringmuskulaturCsvColumn extends AbstractCsvColumn {

    public BewegungsapparatVerkuerzungHamstringmuskulaturCsvColumn(@NonNull CsvColumnConfig config) {
        super(config, null);
    }

    @Override
    @NonNull
    public String extract(@NonNull Einladung einladung) {
        return ExtractorForCsvUtil.extractUntersuchungBewegungsapparat(
            einladung,
            untersuchungBewegungsapparat ->
                ValueEnhancedUtil.translateEnhancedEnumValue(
                    untersuchungBewegungsapparat.getVerkuerzungHamstringmuskulatur(),
                    config.tl(),
                    VerkuerzungHamstringmuskulatur.class)
        );
    }

    @Override
    @NonNull
    protected String getDefaultHeaderKey() {
        return "CsvColumn.bewegungsapparat.verkuerzungHamstringmuskulatur";
    }
}
