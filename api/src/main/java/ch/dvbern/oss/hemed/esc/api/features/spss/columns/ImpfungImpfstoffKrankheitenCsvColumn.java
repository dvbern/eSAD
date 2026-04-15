package ch.dvbern.oss.hemed.esc.api.features.spss.columns;

import java.util.stream.Collectors;

import ch.dvbern.oss.hemed.esc.api.features.anlass.einladung.Einladung;
import ch.dvbern.oss.hemed.esc.api.features.anlass.impfung.UntersuchungImpfungIntern;
import org.checkerframework.checker.nullness.qual.NonNull;

public class ImpfungImpfstoffKrankheitenCsvColumn extends AbstractCsvColumn {

    public ImpfungImpfstoffKrankheitenCsvColumn(@NonNull CsvColumnConfig config) {
        super(config, null);
    }

    @Override
    public @NonNull String extract(@NonNull Einladung einladung) {
        return "";
    }

    @Override
    @NonNull
    public String extract(@NonNull UntersuchungImpfungIntern impfung) {
        return impfung.getImpfstoff()
            .getKrankheiten()
            .stream()
            .map(krankheit -> config.tl().translateValue(krankheit))
            .collect(Collectors.joining(","));
    }

    @Override
    protected @NonNull String getDefaultHeaderKey() {
        return "CsvColumn.ImpfungImpfstoffKrankheiten";
    }
}
