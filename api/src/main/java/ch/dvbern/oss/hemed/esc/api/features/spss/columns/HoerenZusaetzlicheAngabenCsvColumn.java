package ch.dvbern.oss.hemed.esc.api.features.spss.columns;

import ch.dvbern.oss.hemed.esc.api.features.anlass.einladung.Einladung;
import ch.dvbern.oss.hemed.esc.api.features.anlass.hoeren.types.HoerenZusaetzlicheAngaben;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;

public class HoerenZusaetzlicheAngabenCsvColumn extends AbstractCsvColumn {

    private final HoerenZusaetzlicheAngaben zusaetzlicheAngaben;

    public HoerenZusaetzlicheAngabenCsvColumn(
        @NonNull CsvColumnConfig config,
        @Nullable String overrideHeader,
        @NonNull HoerenZusaetzlicheAngaben zusaetzlicheAngaben) {
        super(config, overrideHeader);
        this.zusaetzlicheAngaben = zusaetzlicheAngaben;
    }

    @Override
    @NonNull
    public String extract(@NonNull Einladung einladung) {
        return ExtractorForCsvUtil.extractUntersuchungHoerenAudiogramm(
            einladung,
            untersuchungHoeren ->
                untersuchungHoeren.getZusaetzlicheAngaben().contains(zusaetzlicheAngaben) ? "1" : ""
        );
    }

    @Override
    @NonNull
    protected String getDefaultHeaderKey() {
        return "CsvColumn.hoereZusaetzlicheAngaben";
    }
}
