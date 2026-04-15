package ch.dvbern.oss.hemed.esc.api.features.spss;

import java.util.List;

import ch.dvbern.dvbstarter.infrastructure.stereotypes.Service;
import ch.dvbern.dvbstarter.shared.i18n.translations.TL;
import ch.dvbern.oss.hemed.esc.api.features.anlass.impfung.UntersuchungImpfstatusKrankheitService;
import ch.dvbern.oss.hemed.esc.api.features.anlass.impfung.UntersuchungImpfungInternService;
import ch.dvbern.oss.hemed.esc.api.features.spss.columns.AbstractCsvColumn;
import ch.dvbern.oss.hemed.esc.api.features.spss.columns.CsvColumnConfig;
import ch.dvbern.oss.hemed.esc.api.features.spss.reports.CsvImpfstoffeReport;
import ch.dvbern.oss.hemed.esc.api.features.spss.reports.CsvSchuluntersuchungReport;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.checkerframework.checker.nullness.qual.NonNull;

@Service
@RequiredArgsConstructor
@Slf4j
public class CsvReportService {

    private final TL tl;
    private final UntersuchungImpfstatusKrankheitService untersuchungImpfstatusKrankheitService;
    private final UntersuchungImpfungInternService untersuchungImpfungInternService;

    @NonNull
    public List<AbstractCsvColumn> getColumns(@NonNull CsvReportType csvReportType) {
        CsvColumnConfig config = new CsvColumnConfig(
            tl,
            untersuchungImpfstatusKrankheitService,
            untersuchungImpfungInternService,
            true,
            false);

        return switch (csvReportType) {
            case SCHULUNTERSUCHUNG -> new CsvSchuluntersuchungReport(config).getReport();
            case IMPFSTOFFE -> new CsvImpfstoffeReport(config).getReport();
        };
    }

    @NonNull
    public static String[] extractHeaders(@NonNull List<AbstractCsvColumn> columns) {
        return columns.stream().map(AbstractCsvColumn::getHeader).toArray(String[]::new);
    }
}
