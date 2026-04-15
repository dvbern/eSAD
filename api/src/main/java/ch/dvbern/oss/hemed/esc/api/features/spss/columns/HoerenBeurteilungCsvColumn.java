package ch.dvbern.oss.hemed.esc.api.features.spss.columns;

import ch.dvbern.oss.hemed.esc.api.features.anlass.einladung.Einladung;
import ch.dvbern.oss.hemed.esc.api.features.anlass.hoeren.types.AudiogrammBeurteilung;
import ch.dvbern.oss.hemed.esc.api.shared.printer.PrintUtil;
import ch.dvbern.oss.hemed.esc.api.shared.util.ValueEnhancedUtil;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;

public class HoerenBeurteilungCsvColumn extends AbstractCsvColumn {

    private final AudiogrammBeurteilung audiogrammBeurteilung;

    public HoerenBeurteilungCsvColumn(
        @NonNull CsvColumnConfig config,
        @Nullable String overrideHeader,
        @NonNull AudiogrammBeurteilung audiogrammBeurteilung
    ) {
        super(config, overrideHeader);
        this.audiogrammBeurteilung = audiogrammBeurteilung;
    }

    @Override
    @NonNull
    public String extract(@NonNull Einladung einladung) {
        return ExtractorForCsvUtil.extractUntersuchungHoerenAudiogramm(
            einladung,
            untersuchungHoeren -> {
                boolean resultat = ValueEnhancedUtil.getEnumSetOrEmpty(
                    untersuchungHoeren.getAudiogrammBeurteilung(),
                    AudiogrammBeurteilung.class).contains(audiogrammBeurteilung);
                if (config.forSpssReport()) {
                    return resultat ? "1" : "";
                }
                return PrintUtil.trimToPlaceholder(resultat, config.tl());
            }
        );
    }

    @Override
    @NonNull
    protected String getDefaultHeaderKey() {
        return "CsvColumn.hoerenBeurteilung." + this.audiogrammBeurteilung;
    }
}
