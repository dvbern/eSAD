package ch.dvbern.oss.hemed.esc.api.features.spss.columns;

import ch.dvbern.oss.hemed.esc.api.features.anlass.einladung.Einladung;
import ch.dvbern.oss.hemed.esc.api.features.anlass.hoeren.types.HoerenZusaetzlicheAngaben;
import ch.dvbern.oss.hemed.esc.api.shared.printer.PrintUtil;
import ch.dvbern.oss.hemed.esc.api.shared.util.ValueEnhancedUtil;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;

public class HoerenZusaetzlicheAngabenCsvColumn extends AbstractCsvColumn {

    private final HoerenZusaetzlicheAngaben zusaetzlicheAngaben;

    public HoerenZusaetzlicheAngabenCsvColumn(
        @NonNull CsvColumnConfig config,
        @Nullable String overrideHeader,
        @NonNull HoerenZusaetzlicheAngaben zusaetzlicheAngaben
    ) {
        super(config, overrideHeader);
        this.zusaetzlicheAngaben = zusaetzlicheAngaben;
    }

    @Override
    @NonNull
    public String extract(@NonNull Einladung einladung) {
        return ExtractorForCsvUtil.extractUntersuchungHoerenAudiogramm(
            einladung,
            untersuchungHoeren -> {
                boolean resultat = ValueEnhancedUtil.getEnumSetOrEmpty(
                    untersuchungHoeren.getZusaetzlicheAngaben(),
                    HoerenZusaetzlicheAngaben.class).contains(zusaetzlicheAngaben);
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
        return "CsvColumn.hoereZusaetzlicheAngaben." + this.zusaetzlicheAngaben;
    }
}
