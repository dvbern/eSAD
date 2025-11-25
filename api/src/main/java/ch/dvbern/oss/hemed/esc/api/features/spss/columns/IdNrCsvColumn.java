package ch.dvbern.oss.hemed.esc.api.features.spss.columns;

import ch.dvbern.oss.hemed.esc.api.features.anlass.einladung.Einladung;
import ch.dvbern.oss.hemed.esc.api.shared.printer.PrintUtil;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;

public class IdNrCsvColumn extends AbstractCsvColumn {

    public IdNrCsvColumn(@NonNull CsvColumnConfig config, @Nullable String overrideHeader) {
        super(config, overrideHeader);
    }

    @Override
    @NonNull
    public String extract(@NonNull Einladung einladung) {
        String esadId = einladung.getDossier().getKind().getEsadId();
        return esadId != null ? PrintUtil.WHITESPACE.matcher(esadId).replaceAll("") : "";
    }

    @Override
    @NonNull
    protected String getDefaultHeaderKey() {
        return "CsvColumn.IdNr";
    }
}
