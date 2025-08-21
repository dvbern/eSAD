package ch.dvbern.oss.hemed.esc.api.shared.plz;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import ch.dvbern.dvbstarter.infrastructure.errorhandling.AppFailureException;
import ch.dvbern.dvbstarter.infrastructure.errorhandling.AppFailureMessage;
import ch.dvbern.dvbstarter.infrastructure.stereotypes.Service;
import ch.dvbern.oss.hemed.esc.api.shared.config.service.ConfigService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVRecord;
import org.apache.commons.lang3.Validate;
import org.checkerframework.checker.nullness.qual.NonNull;

@Service
@Slf4j
@RequiredArgsConstructor
public class PLZImportService {

    private final ConfigService configService;

    private static final String[] HEADERS =
        { "REC_ART", "ONRP", "BFSNR", "PLZ_TYP", "POSTLEITZAHL", "PLZ_ZZ", "GPLZ", "ORTBEZ18",
            "ORTBEZ27", "KANTON", "SPRACHCODE", "SPRACHCODE_ ABW", "BRIEFZ_DURCH", "GILT_AB_DAT", "PLZ_BRIEFZUST",
            "PLZ_COFF", "Geo Shape", "Geokoordinaten"
        };

    /**
     * importiert das mit der App mitdeployte File in eine Liste von DTOs
     *
     * @return Liste von DTOs mit PLZ angaben
     */
    @NonNull
    public List<PLZData> importDefaultPlzDataFromCSV() {
        try (InputStream inputStreamOfCsv =
            PLZImportService.class.getResourceAsStream(configService.getPlzImportFileLocation())) {
            Objects.requireNonNull(inputStreamOfCsv);
            return this.importPlzDataFromCSV(inputStreamOfCsv);
        } catch (IOException e) {
            throw new AppFailureException(AppFailureMessage.internalError("Problem importing PLZ CSV File"), e);
        }
    }

    @NonNull
    public List<PLZData> importPlzDataFromCSV(@NonNull InputStream inputStreamOfCsv) {
        List<PLZData> result = new ArrayList<>();
        try {
            InputStreamReader in = new InputStreamReader(inputStreamOfCsv, StandardCharsets.UTF_8);

            Iterable<CSVRecord> records = CSVFormat.DEFAULT.builder()
                .setHeader(HEADERS)
                .setDelimiter(';')
                .setSkipHeaderRecord(true)
                .get()
                .parse(in);

            for (CSVRecord csvRecord : records) {
                PLZData plzData = toEntityForImport(csvRecord);
                result.add(plzData);
            }
        } catch (IOException e) {
            throw new AppFailureException(AppFailureMessage.internalError("Problem importing PLZ CSV File"), e);
        }
        return result;
    }

    @NonNull
    private PLZData toEntityForImport(@NonNull CSVRecord csvRecord) {
        String plz = csvRecord.get("POSTLEITZAHL");
        String kanton = csvRecord.get("KANTON");
        String name = csvRecord.get("ORTBEZ18");
        String plzTyp = csvRecord.get("PLZ_TYP");

        Validate.notEmpty(plz, "Invalid data in CSV, got empty string while readind PLZ");
        Validate.notEmpty(kanton, "Invalid data in CSV, got empty string while readind Kanton");

        return new PLZData(plz, name, kanton, plzTyp);
    }
}
