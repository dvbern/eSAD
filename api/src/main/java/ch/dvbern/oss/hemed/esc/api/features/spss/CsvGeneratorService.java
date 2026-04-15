package ch.dvbern.oss.hemed.esc.api.features.spss;

import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import ch.dvbern.dvbstarter.infrastructure.errorhandling.AppFailureException;
import ch.dvbern.dvbstarter.infrastructure.errorhandling.AppFailureMessage;
import ch.dvbern.dvbstarter.infrastructure.stereotypes.Service;
import ch.dvbern.dvbstarter.shared.types.cleanfilename.CleanFileName;
import ch.dvbern.dvbstarter.shared.types.emailaddress.EmailAddress;
import ch.dvbern.dvbstarter.shared.types.id.ID;
import ch.dvbern.oss.hemed.esc.api.features.anlass.einladung.Einladung;
import ch.dvbern.oss.hemed.esc.api.features.anlass.einladung.EinladungService;
import ch.dvbern.oss.hemed.esc.api.features.anlass.impfung.UntersuchungImpfungIntern;
import ch.dvbern.oss.hemed.esc.api.features.anlass.impfung.UntersuchungImpfungInternService;
import ch.dvbern.oss.hemed.esc.api.features.spss.columns.AbstractCsvColumn;
import ch.dvbern.oss.hemed.esc.api.shared.documentqueue.DocumentQueueFileService;
import ch.dvbern.oss.hemed.esc.api.shared.types.DateRange;
import jakarta.transaction.Transactional;
import jakarta.transaction.Transactional.TxType;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;
import org.apache.commons.text.StringSubstitutor;
import org.checkerframework.checker.nullness.qual.NonNull;

@Service
@RequiredArgsConstructor
@Slf4j
public class CsvGeneratorService {

    private final DocumentQueueFileService documentQueueFileService;
    private final EinladungService einladungService;
    private final CsvService csvService;
    private final SpssReportService spssReportService;
    private final CsvReportService csvReportService;
    private final UntersuchungImpfungInternService untersuchungImpfungInternService;

    @Transactional(TxType.NOT_SUPPORTED)
    public Path generateSpssReport(
        @NonNull SpssReportType spssReportType,
        @NonNull EmailAddress triggeringUser,
        @NonNull CleanFileName fileName
    ) {
        List<ID<Einladung>> einladungIdList =
            einladungService.findAllForSpssReport(spssReportType.getAnlassStatusList());

        List<AbstractCsvColumn> columns = spssReportService.getColumns(spssReportType);
        CSVFormat csvFormat = CSVFormat.DEFAULT.builder().setHeader(SpssReportService.extractHeaders(columns)).get();
        StringBuilder einladungIdsSB = new StringBuilder();

        Path tempFile = documentQueueFileService.createTempFile(fileName);
        try (OutputStream os = Files.newOutputStream(tempFile);
            CSVPrinter printer = new CSVPrinter(new OutputStreamWriter(os, StandardCharsets.UTF_8), csvFormat)) {

            for (ID<Einladung> id : einladungIdList) {
                csvService.processEinladung(id, columns, printer);
                einladungIdsSB.append(id.getUUID().toString()).append(',');
            }
        } catch (IOException e) {
            throw new AppFailureException(AppFailureMessage.internalError("Error while generating SPSS Report"), e);
        }

        einladungService.markAsSpssExported(einladungIdList);

        String printedIds = einladungIdsSB.isEmpty() ?
            einladungIdsSB.toString() :
            einladungIdsSB.substring(0, einladungIdsSB.length() - 1); // cut final ','
        logSpssReportGenerated(printedIds, spssReportType, triggeringUser);

        return tempFile;
    }

    @Transactional(TxType.NOT_SUPPORTED)
    public Path generateCsvReport(
        @NonNull CsvReportType csvReportType,
        @NonNull DateRange zeitraum,
        @NonNull EmailAddress triggeringUser,
        @NonNull CleanFileName fileName
    ) {
        List<AbstractCsvColumn> columns = csvReportService.getColumns(csvReportType);
        CSVFormat csvFormat = CSVFormat.DEFAULT.builder().setHeader(CsvReportService.extractHeaders(columns)).get();

        Path tempFile = documentQueueFileService.createTempFile(fileName);
        try {
            try (OutputStream os = Files.newOutputStream(tempFile);
                OutputStreamWriter writer = new OutputStreamWriter(os, StandardCharsets.UTF_8);
                CSVPrinter printer = new CSVPrinter(writer, csvFormat)) {

                String printedIds = switch (csvReportType) {
                    case IMPFSTOFFE -> exportImpfstoffeToCsv(columns, printer, zeitraum);
                    case SCHULUNTERSUCHUNG -> exportEinladungenToCsv(columns, printer, zeitraum);
                };
                logCsvReportGenerated(printedIds, csvReportType, triggeringUser);
            }

            return tempFile;
        } catch (IOException e) {
            throw new AppFailureException(AppFailureMessage.internalError("Error while generating CSV Report"), e);
        }
    }

    private String exportImpfstoffeToCsv(
        @NonNull List<AbstractCsvColumn> columns,
        @NonNull CSVPrinter printer,
        @NonNull DateRange zeitraum
    ) throws IOException {
        List<ID<UntersuchungImpfungIntern>> untersuchungImpfungInterIds =
            untersuchungImpfungInternService.findAllForCsvReport(zeitraum);
        for (ID<UntersuchungImpfungIntern> id : untersuchungImpfungInterIds) {
            csvService.processImpfstoff(id, columns, printer);
        }
        return untersuchungImpfungInterIds.stream().map(impungIntern -> impungIntern.getUUID().toString()).collect(
            Collectors.joining(","));
    }

    private String exportEinladungenToCsv(
        @NonNull List<AbstractCsvColumn> columns,
        @NonNull CSVPrinter printer,
        @NonNull DateRange zeitraum) throws IOException {
        List<ID<Einladung>> einladungIdList = einladungService.findAllForCsvReport(zeitraum);
        for (ID<Einladung> id : einladungIdList) {
            csvService.processEinladung(id, columns, printer);
        }
        return einladungIdList.stream().map(einladung -> einladung.getUUID().toString()).collect(
            Collectors.joining(","));
    }

    private void logSpssReportGenerated(
        @NonNull String printedIds,
        @NonNull SpssReportType type,
        @NonNull EmailAddress emailAddress
    ) {
        String template = "ESC-SPSS: Generation of SPSS Report typ=${type} "
            + "for einladungsIds=${einladungsIds}; triggering user was "
            + "username=${emailAddress}";
        Map<String, String> params = new HashMap<>();
        params.put("type", type.name());
        params.put("einladungsIds", printedIds);
        params.put("emailAddress", emailAddress.toString());

        String result = StringSubstitutor.replace(template, params, "${", "}");
        LOG.info(result);
    }

    private void logCsvReportGenerated(
        @NonNull String printedIds,
        @NonNull CsvReportType type,
        @NonNull EmailAddress emailAddress
    ) {
        String template = "ESC-CSV: Generation of CSV Report typ=${type} "
            + "for einladungsIds=${einladungsIds}; triggering user was "
            + "username=${emailAddress}";
        Map<String, String> params = new HashMap<>();
        params.put("type", type.name());
        params.put("einladungsIds", printedIds);
        params.put("emailAddress", emailAddress.toString());

        String result = StringSubstitutor.replace(template, params, "${", "}");
        LOG.info(result);
    }
}
