package ch.dvbern.oss.hemed.esc.api.features.spss;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import ch.dvbern.dvbstarter.infrastructure.errorhandling.AppFailureException;
import ch.dvbern.dvbstarter.infrastructure.errorhandling.AppFailureMessage;
import ch.dvbern.dvbstarter.infrastructure.stereotypes.Service;
import ch.dvbern.dvbstarter.shared.types.emailaddress.EmailAddress;
import ch.dvbern.dvbstarter.shared.types.id.ID;
import ch.dvbern.oss.hemed.esc.api.features.anlass.Einladung;
import ch.dvbern.oss.hemed.esc.api.features.anlass.EinladungService;
import ch.dvbern.oss.hemed.esc.api.features.spss.columns.AbstractCsvColumn;
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

    private final EinladungService einladungService;
    private final CsvService csvService;
    private final SpssReportService spssReportService;

    @Transactional(TxType.NOT_SUPPORTED)
    public byte[] generateSpssReport(
        @NonNull SpssReportType spssReportType,
        @NonNull EmailAddress triggeringUser
    ) {
        List<ID<Einladung>> einladungIdList =
            einladungService.findAllForSpssReport(spssReportType.getAnlassStatusList());

        List<AbstractCsvColumn> columns = spssReportService.getColumns(spssReportType);
        CSVFormat csvFormat = CSVFormat.DEFAULT.builder().setHeader(SpssReportService.extractHeaders(columns)).get();
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        StringBuilder einladungIdsSB = new StringBuilder();
        try (final CSVPrinter printer = new CSVPrinter(
            new OutputStreamWriter(outputStream, StandardCharsets.UTF_8),
            csvFormat)
        ) {
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

        return outputStream.toByteArray();
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
}
