package ch.dvbern.oss.hemed.esc.api.features.spss.columns;

import ch.dvbern.oss.hemed.esc.api.features.anlass.einladung.Einladung;
import ch.dvbern.oss.hemed.esc.api.features.anlass.hoeren.types.HoerenHoerhilfe;
import ch.dvbern.oss.hemed.esc.api.shared.util.ValueEnhancedUtil;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;

public class AudiogrammHoerHilfeCsvColumn extends AbstractCsvColumn {

    private final HoerenHoerhilfe reultat;

    public AudiogrammHoerHilfeCsvColumn(
        @NonNull CsvColumnConfig config,
        @Nullable String overrideHeader,
        @NonNull HoerenHoerhilfe reultat
    ) {
        super(config, overrideHeader);
        this.reultat = reultat;
    }

    @Override
    @NonNull
    public String extract(@NonNull Einladung einladung) {
        return ExtractorForCsvUtil.extractUntersuchungHoerenAudiogramm(
            einladung,
            untersuchungHoeren ->
                ValueEnhancedUtil.translateEnumValueForColumnToFlag(
                    untersuchungHoeren.getHoerhilfe(),
                    this.reultat,
                    config.tl(),
                    HoerenHoerhilfe.class
                ));
    }

    @Override
    @NonNull
    protected String getDefaultHeaderKey() { return "CsvColumn.hoerenHoerhilfe." + this.reultat; }
}
