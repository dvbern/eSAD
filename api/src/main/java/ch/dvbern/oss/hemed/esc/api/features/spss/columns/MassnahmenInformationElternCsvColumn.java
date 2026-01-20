package ch.dvbern.oss.hemed.esc.api.features.spss.columns;

import ch.dvbern.oss.hemed.esc.api.features.anlass.einladung.Einladung;
import ch.dvbern.oss.hemed.esc.api.features.anlass.massnahmen.types.InformationEltern;
import ch.dvbern.oss.hemed.esc.api.shared.printer.PrintUtil;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;

public class MassnahmenInformationElternCsvColumn extends AbstractCsvColumn {

    public MassnahmenInformationElternCsvColumn(
        @NonNull CsvColumnConfig config,
        @Nullable String overrideHeader) {
        super(config, overrideHeader);
    }

    @Override
    @NonNull
    public String extract(@NonNull Einladung einladung) {
        return ExtractorForCsvUtil.extractUntersuchungMassnahmen(
            einladung,
            untersuchungMassnahmen -> {
                if (untersuchungMassnahmen.getInformationEltern() == null) {
                    return "";
                }
                boolean infoAnEltern = untersuchungMassnahmen.getInformationEltern() == InformationEltern.KEINE;
                if (!config.forSpssReport()) {
                    return PrintUtil.trimToPlaceholder(!infoAnEltern, config.tl());
                }
                return infoAnEltern ? "" : "1";
            }
        );
    }

    @Override
    @NonNull
    protected String getDefaultHeaderKey() {
        return "CsvColumn.massnahmenInformationEltern";
    }
}
