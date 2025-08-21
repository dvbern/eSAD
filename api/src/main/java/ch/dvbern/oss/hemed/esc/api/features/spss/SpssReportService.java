package ch.dvbern.oss.hemed.esc.api.features.spss;

import java.util.List;

import ch.dvbern.dvbstarter.infrastructure.stereotypes.Service;
import ch.dvbern.dvbstarter.shared.i18n.translations.TL;
import ch.dvbern.oss.hemed.esc.api.features.anlass.UntersuchungImpfstatusKrankheitService;
import ch.dvbern.oss.hemed.esc.api.features.anlass.UntersuchungImpfungInternService;
import ch.dvbern.oss.hemed.esc.api.features.schule.klasse.KlasseService;
import ch.dvbern.oss.hemed.esc.api.features.spss.columns.AbstractCsvColumn;
import ch.dvbern.oss.hemed.esc.api.features.spss.columns.CsvColumnConfig;
import ch.dvbern.oss.hemed.esc.api.features.spss.reports.SpssFremdsprachenReport;
import ch.dvbern.oss.hemed.esc.api.features.spss.reports.SpssImpfanlaesseReport;
import ch.dvbern.oss.hemed.esc.api.features.spss.reports.SpssKindergartenReport;
import ch.dvbern.oss.hemed.esc.api.features.spss.reports.SpssKlasse4Report;
import ch.dvbern.oss.hemed.esc.api.features.spss.reports.SpssKlasse8Report;
import ch.dvbern.oss.hemed.esc.api.features.spss.reports.SpssKlasse9Report;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.checkerframework.checker.nullness.qual.NonNull;

@Service
@RequiredArgsConstructor
@Slf4j
public class SpssReportService {

    private final TL tl;
    private final UntersuchungImpfstatusKrankheitService untersuchungImpfstatusKrankheitService;
    private final UntersuchungImpfungInternService untersuchungImpfungInternService;
    private final KlasseService klasseService;

    @NonNull
    public List<AbstractCsvColumn> getColumns(@NonNull SpssReportType spssReportType) {
        CsvColumnConfig config = new CsvColumnConfig(
            tl,
            untersuchungImpfstatusKrankheitService,
            untersuchungImpfungInternService,
            false);
        return switch (spssReportType) {
            case KINDERGARTEN -> new SpssKindergartenReport(config).getReport();
            case KLASSE_4 -> new SpssKlasse4Report(config).getReport();
            case KLASSE_8 -> new SpssKlasse8Report(config).getReport();
            case KLASSE_9 -> new SpssKlasse9Report(config).getReport();
            case FREMDSPRACHEN -> new SpssFremdsprachenReport(config).getReport();
            case IMPF_ANLAESSE -> new SpssImpfanlaesseReport(config).getReport();
        };
    }

    @NonNull
    public static String[] extractHeaders(@NonNull List<AbstractCsvColumn> columns) {
        return columns.stream().map(AbstractCsvColumn::getHeader).toArray(String[]::new);
    }
}
