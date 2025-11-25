package ch.dvbern.oss.hemed.esc.api.features.spss.columns;

import ch.dvbern.oss.hemed.esc.api.features.anlass.einladung.Einladung;
import ch.dvbern.oss.hemed.esc.api.features.anlass.hoeren.types.AudiogrammBeurteilung;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;

public class HoerenBeurteilungCsvColumn extends AbstractCsvColumn {

    private final AudiogrammBeurteilung audiogrammBeurteilung;

    public HoerenBeurteilungCsvColumn(
        @NonNull CsvColumnConfig config,
        @Nullable String overrideHeader,
        @NonNull AudiogrammBeurteilung audiogrammBeurteilung) {
        super(config, overrideHeader);
        this.audiogrammBeurteilung = audiogrammBeurteilung;
    }

    @Override
    @NonNull
    public String extract(@NonNull Einladung einladung) {
        return ExtractorForCsvUtil.extractUntersuchungHoerenAudiogramm(
            einladung,
            untersuchungHoeren ->
                untersuchungHoeren.getAudiogrammBeurteilung().contains(audiogrammBeurteilung) ? "1" : ""
        );
    }

    @Override
    @NonNull
    protected String getDefaultHeaderKey() {
        return "CsvColumn.hoerenBeurteilung";
    }
}
