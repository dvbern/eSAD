package ch.dvbern.oss.hemed.esc.api.features.spss.columns;

import java.time.OffsetDateTime;

import ch.dvbern.oss.hemed.esc.api.features.anlass.Einladung;
import ch.dvbern.oss.hemed.esc.api.shared.abstractentity.AbstractEntity;
import ch.dvbern.oss.hemed.esc.api.shared.printer.PrintUtil;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;

public class LastUpdateImpfungenCsvColumn extends AbstractCsvColumn {

    public LastUpdateImpfungenCsvColumn(
        @NonNull CsvColumnConfig config,
        @Nullable String overrideHeader
    ) {
        super(config, overrideHeader);
    }

    @Override
    @NonNull
    public String extract(@NonNull Einladung einladung) {

        return ExtractorForCsvUtil.extractUntersuchungImpfungen(
            einladung,
            impfungen ->
                config.untersuchungImpfstatusKrankheitService()
                    .findByUntersuchung(impfungen.getUntersuchung().getId())
                    .stream()
                    .map(AbstractEntity::getTimestampMutiert)
                    .max(OffsetDateTime::compareTo)
                    .map(PrintUtil::formatDate)
                    .orElse(""));
    }

    @Override
    @NonNull
    protected String getDefaultHeaderKey() {
        return "CsvColumn.lastUpdateImpfungen";
    }
}
