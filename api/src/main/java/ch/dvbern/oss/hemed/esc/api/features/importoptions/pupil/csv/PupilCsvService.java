package ch.dvbern.oss.hemed.esc.api.features.importoptions.pupil.csv;

import java.io.FileWriter;
import java.io.IOException;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

import ch.dvbern.dvbstarter.infrastructure.errorhandling.AppFailureException;
import ch.dvbern.dvbstarter.infrastructure.errorhandling.AppFailureMessage;
import ch.dvbern.dvbstarter.infrastructure.errorhandling.AppValidationException;
import ch.dvbern.dvbstarter.infrastructure.errorhandling.AppValidationMessage;
import ch.dvbern.dvbstarter.infrastructure.stereotypes.Service;
import jakarta.transaction.Transactional;
import jakarta.transaction.Transactional.TxType;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;
import org.apache.commons.csv.CSVRecord;
import org.checkerframework.checker.nullness.qual.NonNull;

import static org.apache.commons.lang3.StringUtils.trimToNull;

@Service
@RequiredArgsConstructor
@Slf4j
public class PupilCsvService {

    @NonNull
    @Transactional(TxType.NOT_SUPPORTED)
    public List<PupilCsvDTO> parsePupilCSV(@NonNull Reader csvReader) {
        CSVFormat csvFormat = CSVFormat.EXCEL
            .builder()
            .setHeader() // when using the header from the csv be sure to strip the BOM, FileUtil#createReaderFromPath
            .setSkipHeaderRecord(true)
            .setDelimiter(';')
            .get();

        // parse using aut closable
        Iterable<CSVRecord> csvRows;

        try (Reader r = csvReader) {
            LOG.info("Pupil-CSV-Import: Parsing CSV file");
            csvRows = csvFormat.parse(r);
            List<PupilCsvDTO> result = new ArrayList<>();
            if (!csvRows.iterator().hasNext()) {
                LOG.warn(
                    "PUPIL-CSV-Import: The CSV File did not have any content rows. This is no longer allowed since "
                        + "it can lead to unintentional deactivations (see ESC-940)");
                throw new AppValidationException(AppValidationMessage.invalidCSVFormat(
                    2,
                    0,
                    PupilCsvDTO.COLUMN_NAMES));
            }

            for (CSVRecord csvRow : csvRows) {
                PupilCsvDTO dto = mapRecordToPupilCsvDTO(csvRow);
                result.add(dto);
            }
            LOG.info("PUPIL-CSV-Import: Parsing CSV file done. Got {} csvRows", result.size());
            return result;
        } catch (IOException e) {
            throw new AppFailureException(AppFailureMessage.internalError("Error while parsing CSV file"), e);
        }
    }

    @NonNull
    private PupilCsvDTO mapRecordToPupilCsvDTO(@NonNull CSVRecord csvRow) {
        try {
            String id = trimToNull(csvRow.get(PupilCsvDTO.SCHUELER_ID_COL_NAME));
            String name = trimToNull(csvRow.get(PupilCsvDTO.SCHUELER_NAME_COL_NAME));
            String vorname = trimToNull(csvRow.get(PupilCsvDTO.SCHUELER_VORNAME_COL_NAME));
            String geschlecht = trimToNull(csvRow.get(PupilCsvDTO.SCHUELER_GESCHLECHT_COL_NAME));
            String geburtsdatum = trimToNull(csvRow.get(PupilCsvDTO.SCHUELER_GEBURTSDATUM_COL_NAME));
            String klasseId = trimToNull(csvRow.get(PupilCsvDTO.KLASSE_ID_COL_NAME));
            String klasseName = trimToNull(csvRow.get(PupilCsvDTO.KLASSE_BEZEICHNUNG_COL_NAME));
            String klassstufe = trimToNull(csvRow.get(PupilCsvDTO.KLASSE_STUFE_COL_NAME));
            String programmJahr = trimToNull(csvRow.get(PupilCsvDTO.KLASSE_PROGRAMMJAHR_COL_NAME));
            String schuleId = trimToNull(csvRow.get(PupilCsvDTO.SCHULE_ID_COL_NAME));
            String schuleName = trimToNull(csvRow.get(PupilCsvDTO.SCHULE_BEZEICHNUNG_COL_NAME));
            return new PupilCsvDTO(id,
                name,
                vorname,
                geschlecht,
                geburtsdatum,
                klasseId,
                klasseName,
                klassstufe,
                programmJahr,
                schuleId,
                schuleName);
        } catch (IllegalArgumentException e) {
            long lineNum = csvRow.getParser().getCurrentLineNumber();
            int numOfValues = csvRow.size();
            LOG.error("PUPIL-CSV-Import: Error while parsing CSV row {}, probably the format is wrong", lineNum, e);
            throw new AppValidationException(
                AppValidationMessage.invalidCSVFormat(lineNum, numOfValues, PupilCsvDTO.COLUMN_NAMES),
                e);
        }
    }
}
