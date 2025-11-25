package ch.dvbern.oss.hemed.esc.api.features.spss.columns;

import ch.dvbern.oss.hemed.esc.api.features.anlass.einladung.Einladung;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;

public abstract class AbstractCsvColumn {

    @NonNull
    final CsvColumnConfig config;

    @Nullable
    private final String overrideHeader;

    protected AbstractCsvColumn(@NonNull CsvColumnConfig config, @Nullable String overrideHeader) {
        this.config = config;
        this.overrideHeader = overrideHeader;
    }

    @NonNull
    public abstract String extract(@NonNull Einladung einladung);

    @NonNull
    protected abstract String getDefaultHeaderKey();

    @NonNull
    public String getHeader() {
        if (overrideHeader != null) {
            return config.shouldTranslateHeader() ? config.tl().translate(overrideHeader) : overrideHeader;
        }
        return config.tl().translate(getDefaultHeaderKey());
    }
}
