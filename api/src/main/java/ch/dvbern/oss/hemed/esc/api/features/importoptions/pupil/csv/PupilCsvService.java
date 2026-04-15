package ch.dvbern.oss.hemed.esc.api.features.importoptions.pupil.csv;

import java.io.IOException;
import java.io.Reader;
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
import org.apache.commons.csv.CSVRecord;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;

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

    @Nullable
    private String mapGeschlecht(@Nullable String geschlecht) {
        if (geschlecht == null) {
            return null;
        }
        if (geschlecht.equals("m")) {
            return "1";
        }
        if (geschlecht.equals("w")) {
            return "2";
        }
        return null;
    }

    @NonNull
    private PupilCsvDTO mapRecordToPupilCsvDTO(@NonNull CSVRecord csvRow) {
        try {
            // Kind
            String schuelerId = trimToNull(csvRow.get(PupilCsvDTO.SCHUELER_ID_COL_NAME));
            String schuelerName = trimToNull(csvRow.get(PupilCsvDTO.SCHUELER_NAME_COL_NAME));
            String schuelerVorname = trimToNull(csvRow.get(PupilCsvDTO.SCHUELER_VORNAME_COL_NAME));
            String schuelerGeburtsdatum = trimToNull(csvRow.get(PupilCsvDTO.SCHUELER_GEBURTSDATUM_COL_NAME));
            String schuelerGeschlecht = mapGeschlecht(csvRow.get(PupilCsvDTO.SCHUELER_GESCHLECHT_COL_NAME));
            // Kind Adresse
            String schuelerStrasse = trimToNull(csvRow.get(PupilCsvDTO.SCHUELER_ADRESSE_STRASSE_COL_NAME));
            String schuelerPlz = trimToNull(csvRow.get(PupilCsvDTO.SCHUELER_ADRESSE_PLZ_COL_NAME));
            String schuelerOrt = trimToNull(csvRow.get(PupilCsvDTO.SCHUELER_ADRESSE_ORT_COL_NAME));
            String schuelerTelefon = trimToNull(csvRow.get(PupilCsvDTO.SCHUELER_TELEFON_COL_NAME));
            String schuelerEmail = trimToNull(csvRow.get(PupilCsvDTO.SCHUELER_EMAIL_COL_NAME));
            String schuelerNationalitaet = trimToNull(csvRow.get(PupilCsvDTO.SCHUELER_NATIONALITAET_COL_NAME));
            String schuelerStaatcodeIso = trimToNull(csvRow.get(PupilCsvDTO.SCHUELER_STAATSCODE_ISO_COL_NAME));
            String schuelerErstsprache = trimToNull(csvRow.get(PupilCsvDTO.SCHUELER_ERSTSPRACHE_COL_NAME));
            // Schule
            String schuleSchulkreis = trimToNull(csvRow.get(PupilCsvDTO.SCHULE_SCHULKREIS_BEZEICHNUNG_COL_NAME));
            String schuleId = trimToNull(csvRow.get(PupilCsvDTO.SCHULE_ID_COL_NAME));
            String schuleName = trimToNull(csvRow.get(PupilCsvDTO.SCHULE_BEZEICHNUNG_COL_NAME));
            // Klasse
            String klasseId = trimToNull(csvRow.get(PupilCsvDTO.KLASSE_ID_COL_NAME));
            String klasseName = trimToNull(csvRow.get(PupilCsvDTO.KLASSE_BEZEICHNUNG_COL_NAME));
            String klasseStufe = trimToNull(csvRow.get(PupilCsvDTO.KLASSE_STUFE_COL_NAME));
            String klasseSchulart = trimToNull(csvRow.get(PupilCsvDTO.KLASSE_SCHULART_COL_NAME));
            String klasseProgrammjahr = trimToNull(csvRow.get(PupilCsvDTO.KLASSE_PROGRAMMJAHR_COL_NAME));
            // Lehrperson 1
            String lehrperson1Id = trimToNull(csvRow.get(PupilCsvDTO.KLASSE_LEHRPERSON_1_ID_COL_NAME));
            String lehrperson1Anrede = trimToNull(csvRow.get(PupilCsvDTO.KLASSE_LEHRPERSON_1_ANREDE_COL_NAME));
            String lehrperson1Name = trimToNull(csvRow.get(PupilCsvDTO.KLASSE_LEHRPERSON_1_NAME_COL_NAME));
            String lehrperson1Vorname = trimToNull(csvRow.get(PupilCsvDTO.KLASSE_LEHRPERSON_1_VORNAME_COL_NAME));
            // Lehrperson 2
            String lehrperson2Id = trimToNull(csvRow.get(PupilCsvDTO.KLASSE_LEHRPERSON_2_ID_COL_NAME));
            String lehrperson2Anrede = trimToNull(csvRow.get(PupilCsvDTO.KLASSE_LEHRPERSON_2_ANREDE_COL_NAME));
            String lehrperson2Name = trimToNull(csvRow.get(PupilCsvDTO.KLASSE_LEHRPERSON_2_NAME_COL_NAME));
            String lehrperson2Vorname = trimToNull(csvRow.get(PupilCsvDTO.KLASSE_LEHRPERSON_2_VORNAME_COL_NAME));
            // Kontaktperson 1
            String kontaktperson1Id = trimToNull(csvRow.get(PupilCsvDTO.KONTAKTPERSON_1_ID_COL_NAME));
            String kontaktperson1Beziehung = trimToNull(csvRow.get(PupilCsvDTO.KONTAKTPERSON_1_BEZIEHUNG_COL_NAME));
            String kontaktperson1Anrede = trimToNull(csvRow.get(PupilCsvDTO.KONTAKTPERSON_1_ANREDE_COL_NAME));
            String kontaktperson1Vorname = trimToNull(csvRow.get(PupilCsvDTO.KONTAKTPERSON_1_VORNAME_COL_NAME));
            String kontaktperson1Name = trimToNull(csvRow.get(PupilCsvDTO.KONTAKTPERSON_1_NAME_COL_NAME));
            String kontaktperson1Strasse = trimToNull(csvRow.get(PupilCsvDTO.KONTAKTPERSON_1_STRASSE_COL_NAME));
            String kontaktperson1Hausnummer = trimToNull(csvRow.get(PupilCsvDTO.KONTAKTPERSON_1_HAUSNUMMER_COL_NAME));
            String kontaktperson1Plz = trimToNull(csvRow.get(PupilCsvDTO.KONTAKTPERSON_1_PLZ_COL_NAME));
            String kontaktperson1Ort = trimToNull(csvRow.get(PupilCsvDTO.KONTAKTPERSON_1_ORT_COL_NAME));
            String kontaktperson1TelefonPrivat = trimToNull(csvRow.get(PupilCsvDTO.KONTAKTPERSON_1_TELEFON_PRIVAT_COL_NAME));
            String kontaktperson1TelefonGeschaeft = trimToNull(csvRow.get(PupilCsvDTO.KONTAKTPERSON_1_TELEFON_GESCHAEFT_COL_NAME));
            String kontaktperson1TelefonMobile = trimToNull(csvRow.get(PupilCsvDTO.KONTAKTPERSON_1_TELEFON_MOBILE_COL_NAME));
            String kontaktperson1Email = trimToNull(csvRow.get(PupilCsvDTO.KONTAKTPERSON_1_EMAIL_COL_NAME));
            // KontaktPerson 2
            String kontaktperson2Id = trimToNull(csvRow.get(PupilCsvDTO.KONTAKTPERSON_2_ID_COL_NAME));
            String kontaktperson2Beziehung = trimToNull(csvRow.get(PupilCsvDTO.KONTAKTPERSON_2_BEZIEHUNG_COL_NAME));
            String kontaktperson2Anrede = trimToNull(csvRow.get(PupilCsvDTO.KONTAKTPERSON_2_ANREDE_COL_NAME));
            String kontaktperson2Vorname = trimToNull(csvRow.get(PupilCsvDTO.KONTAKTPERSON_2_VORNAME_COL_NAME));
            String kontaktperson2Name = trimToNull(csvRow.get(PupilCsvDTO.KONTAKTPERSON_2_NAME_COL_NAME));
            String kontaktperson2Strasse = trimToNull(csvRow.get(PupilCsvDTO.KONTAKTPERSON_2_STRASSE_COL_NAME));
            String kontaktperson2Hausnummer = trimToNull(csvRow.get(PupilCsvDTO.KONTAKTPERSON_2_HAUSNUMMER_COL_NAME));
            String kontaktperson2Plz = trimToNull(csvRow.get(PupilCsvDTO.KONTAKTPERSON_2_PLZ_COL_NAME));
            String kontaktperson2Ort = trimToNull(csvRow.get(PupilCsvDTO.KONTAKTPERSON_2_ORT_COL_NAME));
            String kontaktperson2TelefonPrivat = trimToNull(csvRow.get(PupilCsvDTO.KONTAKTPERSON_2_TELEFON_PRIVAT_COL_NAME));
            String kontaktperson2TelefonGeschaeft = trimToNull(csvRow.get(PupilCsvDTO.KONTAKTPERSON_2_TELEFON_GESCHAEFT_COL_NAME));
            String kontaktperson2TelefonMobile = trimToNull(csvRow.get(PupilCsvDTO.KONTAKTPERSON_2_TELEFON_MOBILE_COL_NAME));
            String kontaktperson2Email = trimToNull(csvRow.get(PupilCsvDTO.KONTAKTPERSON_2_EMAIL_COL_NAME));

            return new PupilCsvDTO(
                // Kind
                schuelerId,
                schuelerName,
                schuelerVorname,
                schuelerGeburtsdatum,
                schuelerGeschlecht,
                // Kind Adresse
                schuelerStrasse,
                schuelerPlz,
                schuelerOrt,
                schuelerTelefon,
                schuelerEmail,
                schuelerNationalitaet,
                schuelerStaatcodeIso,
                schuelerErstsprache,
                // Schule
                schuleSchulkreis,
                schuleId,
                schuleName,
                // Klasse
                klasseId,
                klasseName,
                klasseStufe,
                klasseSchulart,
                klasseProgrammjahr,
                // Lehrperson 1
                lehrperson1Id,
                lehrperson1Anrede,
                lehrperson1Name,
                lehrperson1Vorname,
                // Lehrperson 2
                lehrperson2Id,
                lehrperson2Anrede,
                lehrperson2Name,
                lehrperson2Vorname,
                // Kontaktperson 1
                kontaktperson1Id,
                kontaktperson1Beziehung,
                kontaktperson1Anrede,
                kontaktperson1Name,
                kontaktperson1Vorname,
                kontaktperson1Strasse,
                kontaktperson1Hausnummer,
                kontaktperson1Plz,
                kontaktperson1Ort,
                kontaktperson1TelefonPrivat,
                kontaktperson1TelefonMobile,
                kontaktperson1TelefonGeschaeft,
                kontaktperson1Email,
                // KontaktPerson 2
                kontaktperson2Id,
                kontaktperson2Beziehung,
                kontaktperson2Anrede,
                kontaktperson2Name,
                kontaktperson2Vorname,
                kontaktperson2Strasse,
                kontaktperson2Hausnummer,
                kontaktperson2Plz,
                kontaktperson2Ort,
                kontaktperson2TelefonPrivat,
                kontaktperson2TelefonMobile,
                kontaktperson2TelefonGeschaeft,
                kontaktperson2Email
            );
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
