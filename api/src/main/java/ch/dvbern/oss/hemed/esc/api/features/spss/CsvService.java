package ch.dvbern.oss.hemed.esc.api.features.spss;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import ch.dvbern.dvbstarter.infrastructure.stereotypes.Service;
import ch.dvbern.dvbstarter.shared.types.id.ID;
import ch.dvbern.oss.hemed.esc.api.features.anlass.einladung.Einladung;
import ch.dvbern.oss.hemed.esc.api.features.anlass.einladung.EinladungService;
import ch.dvbern.oss.hemed.esc.api.features.anlass.impfung.UntersuchungImpfungIntern;
import ch.dvbern.oss.hemed.esc.api.features.anlass.impfung.UntersuchungImpfungInternService;
import ch.dvbern.oss.hemed.esc.api.features.spss.columns.AbstractCsvColumn;
import jakarta.transaction.Transactional;
import jakarta.transaction.Transactional.TxType;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.csv.CSVPrinter;
import org.checkerframework.checker.nullness.qual.NonNull;

@Service
@RequiredArgsConstructor
@Slf4j
public class CsvService {

    private final EinladungService einladungService;
    private final UntersuchungImpfungInternService  untersuchungImpfungInternService;

    @Transactional(TxType.REQUIRES_NEW)
    public void processEinladung(
        @NonNull ID<Einladung> id,
        @NonNull List<AbstractCsvColumn> columns,
        @NonNull CSVPrinter printer
    ) throws IOException {
        Einladung einladung = einladungService.getById(id);
        List<String> columValueList = new ArrayList<>();
        for (AbstractCsvColumn column : columns) {
            columValueList.add(column.extract(einladung));
        }
        printer.printRecords(List.of(columValueList));
    }

    @Transactional(TxType.REQUIRES_NEW)
    public void processImpfstoff(
        @NonNull ID<UntersuchungImpfungIntern> id,
        @NonNull List<AbstractCsvColumn> columns,
        @NonNull CSVPrinter printer
    ) throws IOException {
        UntersuchungImpfungIntern impfung = untersuchungImpfungInternService.getById(id);
        List<String> columValueList = new ArrayList<>();
        for (AbstractCsvColumn column : columns) {
            columValueList.add(column.extract(impfung));
        }
        printer.printRecords(List.of(columValueList));
    }
}
