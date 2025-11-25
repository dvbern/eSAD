package ch.dvbern.oss.hemed.esc.api.features.spss.columns;

import ch.dvbern.dvbstarter.shared.i18n.datetime.DateUtil;
import ch.dvbern.oss.hemed.esc.api.features.anlass.einladung.Einladung;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;

public class AlterCsvColumn extends AbstractCsvColumn {

    public AlterCsvColumn(@NonNull CsvColumnConfig config, @Nullable String overrideHeader) {
        super(config, overrideHeader);
    }

    @Override
    @NonNull
    public String extract(@NonNull Einladung einladung) {
		return DateUtil.calculateAlterDuodezimal(
			einladung.getDossier().getKind().getGeburtsdatum(),
			einladung.getAnlass().getDatum());
    }

    @Override
    @NonNull
    protected String getDefaultHeaderKey() {
        return "CsvColumn.alter";
    }
}
