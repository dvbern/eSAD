package ch.dvbern.oss.hemed.esc.api.features.spss.columns;

import ch.dvbern.oss.hemed.esc.api.features.anlass.einladung.Einladung;
import ch.dvbern.oss.hemed.esc.api.features.anlass.aerztlicheuntersuchung.types.MundAndZaehne;
import ch.dvbern.oss.hemed.esc.api.shared.printer.PrintUtil;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;

public class MundAndZaehneCsvColumn extends AbstractCsvColumn {

    private final MundAndZaehne mundAndZaehne;

    public MundAndZaehneCsvColumn(
        @NonNull CsvColumnConfig config,
        @Nullable String overrideHeader,
        @NonNull MundAndZaehne mundAndZaehne) {
        super(config, overrideHeader);
        this.mundAndZaehne = mundAndZaehne;
    }

    @Override
    @NonNull
    public String extract(@NonNull Einladung einladung) {
        return ExtractorForCsvUtil.extractUntersuchungArztBeurteilung(
            einladung,
            untersuchungArzt -> {
                if (config.forSpssReport()) {
                    return untersuchungArzt.getMundAndZaehne().contains(mundAndZaehne) ? "1" : "";
                }
                return PrintUtil.trimToPlaceholder(untersuchungArzt.getMundAndZaehne().contains(mundAndZaehne), config.tl());
            }
        );
    }

    @Override
    @NonNull
    protected String getDefaultHeaderKey() {
        return "CsvColumn.mundAndZaehne." + this.mundAndZaehne;
    }
}
