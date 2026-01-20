package ch.dvbern.oss.hemed.esc.api.features.spss.columns;

import ch.dvbern.oss.hemed.esc.api.features.anlass.aerztlicheuntersuchung.types.Einverstaendniserklaerung;
import ch.dvbern.oss.hemed.esc.api.features.anlass.einladung.Einladung;
import ch.dvbern.oss.hemed.esc.api.shared.printer.PrintUtil;
import org.checkerframework.checker.nullness.qual.NonNull;

public class EinverstaendniserklaerungCsvColumn extends AbstractCsvColumn {

    private final Einverstaendniserklaerung einverstaendniserklaerung;

    public EinverstaendniserklaerungCsvColumn(
        @NonNull CsvColumnConfig config,
        @NonNull Einverstaendniserklaerung einverstaendniserklaerung
    ) {
        super(config, null);
        this.einverstaendniserklaerung = einverstaendniserklaerung;
    }

    @Override
    @NonNull
    public String extract(@NonNull Einladung einladung) {
        return ExtractorForCsvUtil.extractUntersuchungArztBeurteilung(
            einladung,
            untersuchungArzt ->
                PrintUtil.trimToPlaceholder(untersuchungArzt.getEinverstaendniserklaerung().contains(
                    einverstaendniserklaerung), config.tl())
        );
    }

    @Override
    @NonNull
    protected String getDefaultHeaderKey() {
        return "CsvColumn.einverstaendniserklaerung." + this.einverstaendniserklaerung;
    }
}
