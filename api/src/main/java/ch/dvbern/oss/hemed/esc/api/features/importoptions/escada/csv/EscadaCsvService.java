package ch.dvbern.oss.hemed.esc.api.features.importoptions.escada.csv;

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
public class EscadaCsvService {

    @NonNull
    @Transactional(TxType.NOT_SUPPORTED)
    public List<EscadaCsvDTO> parseEscadaCSV(@NonNull Reader csvReader) {
        CSVFormat csvFormat = CSVFormat.EXCEL
            .builder()
            .setHeader() // when using the header from the csv be sure to strip the BOM, FileUtil#createReaderFromPath
            .setSkipHeaderRecord(true)
            .setDelimiter(';')
            .get();

        // parse using aut closable
        Iterable<CSVRecord> csvRows = null;

        try (Reader r = csvReader) {
            LOG.info("ESCADA-CSV-Import: Parsing CSV file");
            csvRows = csvFormat.parse(r);
            List<EscadaCsvDTO> result = new ArrayList<>();
            if (!csvRows.iterator().hasNext()) {
                LOG.warn(
                    "ESCADA-CSV-Import: The CSV File did not have any content rows. This is no longer allowed since "
                        + "it can lead to unintentional deactivations (see ESC-940)");
                throw new AppValidationException(AppValidationMessage.invalidCSVFormat(
                    2,
                    0,
                    EscadaCsvDTO.COLUMN_NAMES));
            }

            for (CSVRecord csvRow : csvRows) {
                EscadaCsvDTO dto = mapRecordToEscadaCsvDTO(csvRow);
                result.add(dto);
            }
            LOG.info("ESCADA-CSV-Import: Parsing CSV file done. Got {} csvRows", result.size());
            return result;
        } catch (IOException e) {
            throw new AppFailureException(AppFailureMessage.internalError("Error while parsing CSV file"), e);
        }
    }

    @NonNull
    private EscadaCsvDTO mapRecordToEscadaCsvDTO(@NonNull CSVRecord csvRow) {
        try {
            return new EscadaCsvDTO(
                trimToNull(csvRow.get(EscadaCsvDTO.SCHUELER_ID_COL_NAME)),
                trimToNull(csvRow.get(EscadaCsvDTO.WOHNTYP_COL_NAME)),
                trimToNull(csvRow.get(EscadaCsvDTO.SCHUELER_NAME_COL_NAME)),
                trimToNull(csvRow.get(EscadaCsvDTO.SCHUELER_VORNAME_COL_NAME)),
                trimToNull(csvRow.get(EscadaCsvDTO.SCHUELER_GEBURTSDATUM_COL_NAME)),
                trimToNull(csvRow.get(EscadaCsvDTO.SCHUELER_GESCHLECHT_COL_NAME)),
                trimToNull(csvRow.get(EscadaCsvDTO.SCHUELER_SOZIALVERSICHEURNG_COL_NAME)),
                trimToNull(csvRow.get(EscadaCsvDTO.SCHUELER_ADRESSE_COL_NAME)),
                trimToNull(csvRow.get(EscadaCsvDTO.SCHUELER_ADRESSE1_COL_NAME)),
                trimToNull(csvRow.get(EscadaCsvDTO.SCHUELER_ADRESSE2_COL_NAME)),
                trimToNull(csvRow.get(EscadaCsvDTO.SCHUELER_PLZ_COL_NAME)),
                trimToNull(csvRow.get(EscadaCsvDTO.SCHUELER_ORT_COL_NAME)),
                trimToNull(csvRow.get(EscadaCsvDTO.SCHUELER_MOBILE_COL_NAME)),
                trimToNull(csvRow.get(EscadaCsvDTO.SCHUELER_EMAIL_COL_NAME)),
                trimToNull(csvRow.get(EscadaCsvDTO.SCHUELER_NATIONALITAET_COL_NAME)),
                trimToNull(csvRow.get(EscadaCsvDTO.SCHUELER_STAATCODE_ISO_COL_NAME)),
                trimToNull(csvRow.get(EscadaCsvDTO.SCHUELER_SPRACHE_COL_NAME)),
                trimToNull(csvRow.get(EscadaCsvDTO.SCHUELER_ERSTSPRACHE_COL_NAME)),
                trimToNull(csvRow.get(EscadaCsvDTO.SCHULKREIS_BEZEICHNUNG_COL_NAME)),
                trimToNull(csvRow.get(EscadaCsvDTO.SCHULANLAGE_ID_COL_NAME)),
                trimToNull(csvRow.get(EscadaCsvDTO.SCHULANLAGE_BEZEICHNUNG_COL_NAME)),
                trimToNull(csvRow.get(EscadaCsvDTO.SCHULHAUS_ID_COL_NAME)),
                trimToNull(csvRow.get(EscadaCsvDTO.SCHULHAUS_BEZEICHNUNG_COL_NAME)),
                trimToNull(csvRow.get(EscadaCsvDTO.KLASSE_ID_COL_NAME)),
                trimToNull(csvRow.get(EscadaCsvDTO.KLASSE_BEZEICHNUNG_COL_NAME)),
                trimToNull(csvRow.get(EscadaCsvDTO.KLASSE_STUFE_COL_NAME)),
                trimToNull(csvRow.get(EscadaCsvDTO.KLASSE_SCHULART_COL_NAME)),
                trimToNull(csvRow.get(EscadaCsvDTO.KLASSE_PROGRAMMJAHR_COL_NAME)),
                trimToNull(csvRow.get(EscadaCsvDTO.KLASSE_LEHRPERSON_ID_COL_NAME)),
                trimToNull(csvRow.get(EscadaCsvDTO.KLASSE_LEHRPERSON_ANREDE_COL_NAME)),
                trimToNull(csvRow.get(EscadaCsvDTO.KLASSE_LEHRPERSON_NAME_COL_NAME)),
                trimToNull(csvRow.get(EscadaCsvDTO.KLASSE_LEHRPERSON_VORNAME_COL_NAME)),
                trimToNull(csvRow.get(EscadaCsvDTO.KLASSE_LEHRPERSON2_ID_COL_NAME)),
                trimToNull(csvRow.get(EscadaCsvDTO.KLASSE_LEHRPERSON2_ANREDE_COL_NAME)),
                trimToNull(csvRow.get(EscadaCsvDTO.KLASSE_LEHRPERSON2_NAME_COL_NAME)),
                trimToNull(csvRow.get(EscadaCsvDTO.KLASSE_LEHRPERSON2_VORNAME_COL_NAME)),
                trimToNull(csvRow.get(EscadaCsvDTO.KLASSE_LEHRPERSON3_ID_COL_NAME)),
                trimToNull(csvRow.get(EscadaCsvDTO.KLASSE_LEHRPERSON3_ANREDE_COL_NAME)),
                trimToNull(csvRow.get(EscadaCsvDTO.KLASSE_LEHRPERSON3_NAME_COL_NAME)),
                trimToNull(csvRow.get(EscadaCsvDTO.KLASSE_LEHRPERSON3_VORNAME_COL_NAME)),
                trimToNull(csvRow.get(EscadaCsvDTO.KLASSE_LEHRPERSON4_ID_COL_NAME)),
                trimToNull(csvRow.get(EscadaCsvDTO.KLASSE_LEHRPERSON4_ANREDE_COL_NAME)),
                trimToNull(csvRow.get(EscadaCsvDTO.KLASSE_LEHRPERSON4_NAME_COL_NAME)),
                trimToNull(csvRow.get(EscadaCsvDTO.KLASSE_LEHRPERSON4_VORNAME_COL_NAME)),
                trimToNull(csvRow.get(EscadaCsvDTO.ADRESSIERUNG_NR_COL_NAME)),
                trimToNull(csvRow.get(EscadaCsvDTO.ADRESSIERUNG_ANREDE_COL_NAME)),
                trimToNull(csvRow.get(EscadaCsvDTO.ADRESSIERUNG_NAME_COL_NAME)),
                trimToNull(csvRow.get(EscadaCsvDTO.ADRESSIERUNG_VORNAME_COL_NAME)),
                trimToNull(csvRow.get(EscadaCsvDTO.ADRESSIERUNG_STRASSE_COL_NAME)),
                trimToNull(csvRow.get(EscadaCsvDTO.ADRESSIERUNG_HAUSNUMMER_COL_NAME)),
                trimToNull(csvRow.get(EscadaCsvDTO.ADRESSIERUNG_PLZ_COL_NAME)),
                trimToNull(csvRow.get(EscadaCsvDTO.ADRESSIERUNG_ORT_COL_NAME)),
                trimToNull(csvRow.get(EscadaCsvDTO.ADRESSIERUNG_TELEFON_PRIVAT_COL_NAME)),
                trimToNull(csvRow.get(EscadaCsvDTO.ADRESSIERUNG_TELEFON_GESCHAEFT_COL_NAME)),
                trimToNull(csvRow.get(EscadaCsvDTO.ADRESSIERUNG_NATEL_COL_NAME)),
                trimToNull(csvRow.get(EscadaCsvDTO.ADRESSIERUNG_EMAIL_COL_NAME)),
                trimToNull(csvRow.get(EscadaCsvDTO.ADRESSIERUNG_NR2_COL_NAME)),
                trimToNull(csvRow.get(EscadaCsvDTO.ADRESSIERUNG_ANREDE2_COL_NAME)),
                trimToNull(csvRow.get(EscadaCsvDTO.ADRESSIERUNG_NAME2_COL_NAME)),
                trimToNull(csvRow.get(EscadaCsvDTO.ADRESSIERUNG_VORNAME2_COL_NAME)),
                trimToNull(csvRow.get(EscadaCsvDTO.ADRESSIERUNG_STRASSE2_COL_NAME)),
                trimToNull(csvRow.get(EscadaCsvDTO.ADRESSIERUNG_HAUSNUMMER2_COL_NAME)),
                trimToNull(csvRow.get(EscadaCsvDTO.ADRESSIERUNG_PLZ2_COL_NAME)),
                trimToNull(csvRow.get(EscadaCsvDTO.ADRESSIERUNG_ORT2_COL_NAME)),
                trimToNull(csvRow.get(EscadaCsvDTO.ADRESSIERUNG_TELEFON_PRIVAT2_COL_NAME)),
                trimToNull(csvRow.get(EscadaCsvDTO.ADRESSIERUNG_TELEFON_GESCHAEFT2_COL_NAME)),
                trimToNull(csvRow.get(EscadaCsvDTO.ADRESSIERUNG_NATEL2_COL_NAME)),
                trimToNull(csvRow.get(EscadaCsvDTO.ADRESSIERUNG_EMAIL2_COL_NAME))
            );
        } catch (IllegalArgumentException e) {
            long lineNum = csvRow.getParser().getCurrentLineNumber();
            int numOfValues = csvRow.size();
            LOG.error("ESCADA-CSV-Import: Error while parsing CSV row {}, probably the format is wrong", lineNum, e);
            throw new AppValidationException(
                AppValidationMessage.invalidCSVFormat(lineNum, numOfValues, EscadaCsvDTO.COLUMN_NAMES),
                e);
        }
    }

    public void writeCsv(@NonNull String name, @NonNull List<EscadaCsvDTO> fixedDTOs) {
        CSVFormat csvFormat = CSVFormat.EXCEL
            .builder()
            .setHeader(EscadaCsvDTO.COLUMN_NAMES)
            .setDelimiter(';')
            .get();

        try (var writer = new FileWriter(name, StandardCharsets.UTF_8)) {
            CSVPrinter csvPrinter = new CSVPrinter(writer, csvFormat);
            for (EscadaCsvDTO dto : fixedDTOs) {
                csvPrinter.printRecord(
                    dto.schuelerId(),
                    dto.wohntyp(),
                    dto.schuelerName(),
                    dto.schuelerVorname(),
                    dto.schuelerGeburtsdatum(),
                    dto.schuelerGeschlecht(),
                    dto.schuelerSozialversicheurng(),
                    dto.schuelerAdresse(),
                    dto.schuelerAdresse1(),
                    dto.schuelerAdresse2(),
                    dto.schuelerPlz(),
                    dto.schuelerOrt(),
                    dto.schuelerMobile(),
                    dto.schuelerEmail(),
                    dto.schuelerNationalitaet(),
                    dto.schuelerStaatcodeIso(),
                    dto.schuelerSprache(),
                    dto.schuelerErstsprache(),
                    dto.schulkreisBezeichnung(),
                    dto.schulanlageId(),
                    dto.schulanlageBezeichnung(),
                    dto.schulhausId(),
                    dto.schulhausBezeichnung(),
                    dto.klasseId(),
                    dto.klasseBezeichnung(),
                    dto.klasseStufe(),
                    dto.klasseSchulart(),
                    dto.klasseProgrammjahr(),
                    dto.klasseLehrpersonId(),
                    dto.klasseLehrpersonAnrede(),
                    dto.klasseLehrpersonName(),
                    dto.klasseLehrpersonVorname(),
                    dto.klasseLehrperson2Id(),
                    dto.klasseLehrperson2Anrede(),
                    dto.klasseLehrperson2Name(),
                    dto.klasseLehrperson2Vorname(),
                    dto.klasseLehrperson3Id(),
                    dto.klasseLehrperson3Anrede(),
                    dto.klasseLehrperson3Name(),
                    dto.klasseLehrperson3Vorname(),
                    dto.klasseLehrperson4Id(),
                    dto.klasseLehrperson4Anrede(),
                    dto.klasseLehrperson4Name(),
                    dto.klasseLehrperson4Vorname(),
                    dto.adressierungNr(),
                    dto.adressierungAnrede(),
                    dto.adressierungName(),
                    dto.adressierungVorname(),
                    dto.adressierungStrasse(),
                    dto.adressierungHausnummer(),
                    dto.adressierungPlz(),
                    dto.adressierungOrt(),
                    dto.adressierungTelefonPrivat(),
                    dto.adressierungTelefonGeschaeft(),
                    dto.adressierungNatel(),
                    dto.adressierungEmail(),
                    dto.adressierungNr2(),
                    dto.adressierungAnrede2(),
                    dto.adressierungName2(),
                    dto.adressierungVorname2(),
                    dto.adressierungStrasse2(),
                    dto.adressierungHausnummer2(),
                    dto.adressierungPlz2(),
                    dto.adressierungOrt2(),
                    dto.adressierungTelefonPrivat2(),
                    dto.adressierungTelefonGeschaeft2(),
                    dto.adressierungNatel2(),
                    dto.adressierungEmail2()
                );
            }
        } catch (IOException e) {
            throw new AppFailureException(AppFailureMessage.internalError("Error while writing CSV file"), e);
        }
    }
}
