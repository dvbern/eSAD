/*
 * Copyright (C) 2025 DV Bern AG, Switzerland
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package ch.dvbern.oss.hemed.esc.api.features.importoptions.massenimport;

import java.io.FileInputStream;
import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.stream.IntStream;

import ch.dvbern.dvbstarter.infrastructure.errorhandling.AppValidationException;
import ch.dvbern.dvbstarter.infrastructure.errorhandling.AppValidationMessage;
import ch.dvbern.dvbstarter.shared.types.emailaddress.EmailAddress;
import ch.dvbern.dvbstarter.shared.types.id.ID;
import ch.dvbern.dvbstarter.shared.types.phonenumber.NormalizedPhoneNumber;
import ch.dvbern.dvbstarter.shared.types.sozialversicherungsnummer.SozialversicherungsNummer;
import ch.dvbern.oss.hemed.esc.api.features.importoptions.massenimport.dto.MassenimportErrorDTO;
import ch.dvbern.oss.hemed.esc.api.features.importoptions.massenimport.dto.MassenimportProblemSeverity;
import ch.dvbern.oss.hemed.esc.api.features.importoptions.massenimport.dto.ValueErrorObject;
import ch.dvbern.oss.hemed.esc.api.features.schule.Schule;
import ch.dvbern.oss.hemed.esc.api.features.schule.klasse.Klasse;
import ch.dvbern.oss.hemed.esc.api.features.schule.klasse.KlasseService;
import lombok.experimental.UtilityClass;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.DateUtil;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.jboss.resteasy.reactive.multipart.FileUpload;

import static ch.dvbern.oss.hemed.esc.api.features.importoptions.massenimport.dto.MassenimportFields.COL_INDEX_ADRESSE_LAND_KP;
import static ch.dvbern.oss.hemed.esc.api.features.importoptions.massenimport.dto.MassenimportFields.COL_INDEX_ADRESSE_ORT_KP;
import static ch.dvbern.oss.hemed.esc.api.features.importoptions.massenimport.dto.MassenimportFields.COL_INDEX_ADRESSE_PLZ_KP;
import static ch.dvbern.oss.hemed.esc.api.features.importoptions.massenimport.dto.MassenimportFields.COL_INDEX_ADRESSE_STRASSE_NR_KP;
import static ch.dvbern.oss.hemed.esc.api.features.importoptions.massenimport.dto.MassenimportFields.COL_INDEX_ADRESSE_ZUSATZ_KP;
import static ch.dvbern.oss.hemed.esc.api.features.importoptions.massenimport.dto.MassenimportFields.COL_INDEX_BEZIEHUNG_KP;
import static ch.dvbern.oss.hemed.esc.api.features.importoptions.massenimport.dto.MassenimportFields.COL_INDEX_EMAIL_KP;
import static ch.dvbern.oss.hemed.esc.api.features.importoptions.massenimport.dto.MassenimportFields.COL_INDEX_GESCHLECHT_KP;
import static ch.dvbern.oss.hemed.esc.api.features.importoptions.massenimport.dto.MassenimportFields.COL_INDEX_KORRESPONDENZ_KP;
import static ch.dvbern.oss.hemed.esc.api.features.importoptions.massenimport.dto.MassenimportFields.COL_INDEX_NACHNAME_KP;
import static ch.dvbern.oss.hemed.esc.api.features.importoptions.massenimport.dto.MassenimportFields.COL_INDEX_SVNR;
import static ch.dvbern.oss.hemed.esc.api.features.importoptions.massenimport.dto.MassenimportFields.COL_INDEX_TELEFON_KP;
import static ch.dvbern.oss.hemed.esc.api.features.importoptions.massenimport.dto.MassenimportFields.COL_INDEX_VORNAME_KP;

@Slf4j
@UtilityClass
public class MassenimportUtil {

    public final String NACHNAME = "Nachname";
    public final String GESCHLECHT = "Geschlecht";
    public final String EMAIL = "E-Mail";
    public final String TELEFON = "Telefon";
    public final String STRASSE_NR = "Strasse & Nummer";
    public final String ADRESSE_ZUSATZ = "Adresszusatz";
    public final String PLZ = "PLZ";
    public final String ORT = "Ort";

    public static final List<String> COLUMNS_STANDARD_IMPORT = List.of(
        "AHV Nummer",
        NACHNAME,
        "Vorname",
        GESCHLECHT,
        "Geburtstag",
        "Nationalität",
        "Erstsprache",
        EMAIL,
        TELEFON,
        "Wohntyp",
        "Adresse Land",
        STRASSE_NR,
        ADRESSE_ZUSATZ,
        PLZ,
        ORT,
        GESCHLECHT,
        NACHNAME,
        "Kontaktperson Vorname",
        "Beziehung",
        EMAIL,
        TELEFON,
        "Korrespondenzadresse?",
        "Land",
        STRASSE_NR,
        ADRESSE_ZUSATZ,
        PLZ,
        ORT
    );
    public static final List<String> COLUMNS_KLASSEN_IMPORT = List.of(
        "AHV Nummer",
        NACHNAME,
        "Vorname",
        GESCHLECHT,
        "Geburtstag",
        "Nationalität",
        "Erstsprache",
        EMAIL,
        TELEFON,
        "Wohntyp",
        "Adresse Land",
        STRASSE_NR,
        ADRESSE_ZUSATZ,
        PLZ,
        ORT,
        GESCHLECHT,
        NACHNAME,
        "Kontaktperson Vorname",
        "Beziehung",
        EMAIL,
        TELEFON,
        "Korrespondenzadresse?",
        "Land",
        STRASSE_NR,
        ADRESSE_ZUSATZ,
        PLZ,
        ORT,
        "Klasse-Bezeichnung"
    );
    public static final String FIELD_REQUIRED = "Field is Required";

    @NonNull
    public static Sheet getSheetFromFileUpload(@NonNull FileUpload fileUpload) {
        try (
            FileInputStream fileInputStream = new FileInputStream(fileUpload.filePath().toFile());
            XSSFWorkbook workbook = new XSSFWorkbook(fileInputStream)
        ) {
            return workbook.getSheetAt(0);
        } catch (IOException e) {
            throw new AppValidationException(AppValidationMessage.errorMassenimport(e.getMessage()));
        }
    }

    public static boolean isAnyKontaktPersonKindFieldFilledOut(@NonNull Row row) {
        List<String> values = new ArrayList<>();
        values.add(getStringCellValue(row, COL_INDEX_GESCHLECHT_KP.getColIndex()));
        values.add(getStringCellValue(row, COL_INDEX_NACHNAME_KP.getColIndex()));
        values.add(getStringCellValue(row, COL_INDEX_VORNAME_KP.getColIndex()));
        values.add(getStringCellValue(row, COL_INDEX_BEZIEHUNG_KP.getColIndex()));
        values.add(getStringCellValue(row, COL_INDEX_EMAIL_KP.getColIndex()));
        values.add(getStringCellValue(row, COL_INDEX_TELEFON_KP.getColIndex()));
        values.add(getStringCellValue(row, COL_INDEX_KORRESPONDENZ_KP.getColIndex()));
        values.add(getStringCellValue(row, COL_INDEX_ADRESSE_LAND_KP.getColIndex()));
        values.add(getStringCellValue(row, COL_INDEX_ADRESSE_STRASSE_NR_KP.getColIndex()));
        values.add(getStringCellValue(row, COL_INDEX_ADRESSE_ZUSATZ_KP.getColIndex()));
        values.add(getStringCellValue(row, COL_INDEX_ADRESSE_PLZ_KP.getColIndex()));
        values.add(getStringCellValue(row, COL_INDEX_ADRESSE_ORT_KP.getColIndex()));

        return values.stream()
            .filter(Objects::nonNull)
            .anyMatch(value -> !isNullishOrEmpty(value));
    }

    public static boolean isSheetStructureValid(@NonNull Sheet sheet, boolean standardImport) {
        List<String> validHeaderList = standardImport ? COLUMNS_STANDARD_IMPORT : COLUMNS_KLASSEN_IMPORT;
        final int expectedFieldsInFirstRow = standardImport ? 27 : 28;
        final int expectedFieldsInSecondRow = validHeaderList.size();
        Row row1 = sheet.getRow(0);
        Row row2 = sheet.getRow(1);
        // Check first two rows aren't null && have expected amount of cells
        if (row1 == null || row1.getPhysicalNumberOfCells() != expectedFieldsInFirstRow
            || row2 == null || row2.getPhysicalNumberOfCells() != expectedFieldsInSecondRow) {
            return false;
        }
        return IntStream.range(0, expectedFieldsInSecondRow)
            .allMatch(i -> {
                Cell cell = row2.getCell(i);
                return cell != null && cell.getStringCellValue().trim().equals(validHeaderList.get(i));
            });
    }

    public static ValueErrorObject<String> mapStringToValueErrorObject(
        @NonNull Integer rowIndex,
        @NonNull Integer colIndex,
        @NonNull Row row,
        boolean required
    ) {
        String value = getStringCellValue(row, colIndex);
        if (isNullishOrEmpty(value)) {
            if (required) {
                return new ValueErrorObject<>(
                    null,
                    new MassenimportErrorDTO(rowIndex, colIndex, FIELD_REQUIRED, MassenimportProblemSeverity.ROLLBACK));
            } else {
                return new ValueErrorObject<>(null, null);
            }
        }
        return new ValueErrorObject<>(value, null);
    }

    public static ValueErrorObject<SozialversicherungsNummer> mapSvnrStringToValueErrorObject(
        @NonNull Integer rowIndex,
        @NonNull Integer colIndex,
        @NonNull Row row
    ) {
        String value = getStringCellValue(row, colIndex);
        if (isNullishOrEmpty(value)) {
            return new ValueErrorObject<>(null, null);
        }
        try {
            SozialversicherungsNummer validSvnr = new SozialversicherungsNummer(value);
            return new ValueErrorObject<>(validSvnr, null);
        } catch (Exception e) {
            return new ValueErrorObject<>(
                null,
                new MassenimportErrorDTO(
                    rowIndex,
                    COL_INDEX_SVNR.getColIndex(),
                    e.getMessage(),
                    MassenimportProblemSeverity.WARNING));
        }
    }

    public static ValueErrorObject<LocalDate> mapLocalDateStringToValueErrorObject(
        @NonNull Integer rowIndex,
        @NonNull Integer colIndex,
        @NonNull Row row,
        boolean required
    ) {
        String value = getStringCellValue(row, colIndex);
        LocalDate date;
        if (isNullishOrEmpty(value)) {
            if (required) {
                return new ValueErrorObject<>(
                    null,
                    new MassenimportErrorDTO(rowIndex, colIndex, FIELD_REQUIRED, MassenimportProblemSeverity.ROLLBACK));
            } else {
                return new ValueErrorObject<>(null, null);
            }
        }

        try {
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm");
            date = LocalDate.parse(value.trim(), formatter);
            return new ValueErrorObject<>(date, null);
        } catch (Exception e) {
            try {
                DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd.MM.yyyy");
                date = LocalDate.parse(value.trim(), formatter);
                return new ValueErrorObject<>(date, null);
            } catch (Exception ex) {
                return new ValueErrorObject<>(
                    null,
                    new MassenimportErrorDTO(
                        rowIndex,
                        colIndex,
                        e.getMessage(),
                        required ? MassenimportProblemSeverity.ROLLBACK : MassenimportProblemSeverity.WARNING));
            }
        }
    }

    public static <T extends Enum<T>> ValueErrorObject<T> mapEnumStringToValueErrorObject(
        @NonNull Integer rowIndex,
        @NonNull Integer colIndex,
        @NonNull Row row,
        @NonNull Class<T> enumClass,
        boolean required
    ) {
        String value = getStringCellValue(row, colIndex);
        if (isNullishOrEmpty(value)) {
            if (required) {
                return new ValueErrorObject<>(
                    null,
                    new MassenimportErrorDTO(rowIndex, colIndex, FIELD_REQUIRED, MassenimportProblemSeverity.ROLLBACK));
            } else {
                return new ValueErrorObject<>(null, null);
            }
        }
        try {
            T enumValue = Arrays.stream(enumClass.getEnumConstants())
                .filter(e -> e.name().equalsIgnoreCase(value.trim()))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException(
                    "Ungültiger Wert für " + enumClass.getSimpleName() + ": " + value
                ));

            return new ValueErrorObject<>(enumValue, null);
        } catch (Exception e) {
            return new ValueErrorObject<>(
                null,
                new MassenimportErrorDTO(
                    rowIndex,
                    colIndex,
                    e.getMessage(),
                    required ? MassenimportProblemSeverity.ROLLBACK : MassenimportProblemSeverity.WARNING
                )
            );
        }
    }

    public static ValueErrorObject<NormalizedPhoneNumber> mapTelefonStringToValueErrorObject(
        @NonNull Integer rowIndex,
        @NonNull Integer colIndex,
        @NonNull Row row
    ) {
        String value = getStringCellValue(row, colIndex);
        if (isNullishOrEmpty(value)) {
            return new ValueErrorObject<>(null, null);
        }
        try {
            NormalizedPhoneNumber validPhone = NormalizedPhoneNumber.parse(value);
            return new ValueErrorObject<>(validPhone, null);
        } catch (Exception e) {
            return new ValueErrorObject<>(
                null,
                new MassenimportErrorDTO(rowIndex, colIndex, e.getMessage(), MassenimportProblemSeverity.WARNING));
        }
    }

    public static ValueErrorObject<EmailAddress> mapEmailStringToValueErrorObject(
        @NonNull Integer rowIndex,
        @NonNull Integer colIndex,
        @NonNull Row row
    ) {
        String value = getStringCellValue(row, colIndex);
        if (isNullishOrEmpty(value)) {
            return new ValueErrorObject<>(null, null);
        }
        try {
            EmailAddress validEmail = EmailAddress.fromString(value);
            return new ValueErrorObject<>(validEmail, null);
        } catch (Exception e) {
            return new ValueErrorObject<>(
                null,
                new MassenimportErrorDTO(rowIndex, colIndex, e.getMessage(), MassenimportProblemSeverity.WARNING));
        }
    }

    public static ValueErrorObject<Boolean> mapBooleanStringToValueErrorObject(
        @NonNull Integer rowIndex,
        @NonNull Integer colIndex,
        @NonNull Row row
    ) {
        String value = getStringCellValue(row, colIndex);
        if (isNullishOrEmpty(value)) {
            return new ValueErrorObject<>(null, null);
        }
        try {
            boolean booleanValue = switch (value.trim().toLowerCase()) {
                case "ja", "true" -> true;
                case "nein", "false" -> false;
                default -> throw new IllegalArgumentException(
                    "Ungültiger Wert für BooleanValue: "
                        + value
                        + ".\n Erwarteter Wert: 'ja', 'nein', 'true' oder 'false', wurde als Nein    gespeichert"
                );
            };
            return new ValueErrorObject<>(booleanValue, null);
        } catch (Exception e) {
            return new ValueErrorObject<>(
                false,
                new MassenimportErrorDTO(rowIndex, colIndex, e.getMessage(), MassenimportProblemSeverity.WARNING));
        }
    }

    public static ValueErrorObject<Klasse> mapKlasseStringToValueErrorObject(
        @NonNull Integer rowIndex,
        @NonNull Integer colIndex,
        @NonNull Row row,
        @NonNull ID<Schule> schuleID,
        @NonNull KlasseService klasseService,
        boolean standardImport
    ) {
        String value = getStringCellValue(row, colIndex);
        if (isNullishOrEmpty(value) || standardImport) {
            return new ValueErrorObject<>(null, null);
        }
        List<Klasse> klassen = klasseService.findBySchuleAndBezeichnung(schuleID, value);
        if (klassen.size() > 1) {
            String errorMessage = "Für die Schule mit der ID: '"
                + schuleID
                + "' wurden mehrere Klassen gefunden mit Bezeichnung '"
                + value
                + "'. Für automatische Zuweisung eindeutige Klassennamen verwenden.";
            LOG.warn(errorMessage);
            return new ValueErrorObject<>(
                null,
                new MassenimportErrorDTO(rowIndex, colIndex, errorMessage, MassenimportProblemSeverity.WARNING));
        }
        if (klassen.size() == 1) {
            return new ValueErrorObject<>(klassen.getFirst(), null);
        }
        String errorMessage = "Für die Schule mit der ID: '"
            + schuleID
            + "' wurde keine Klasse gefunden mit Bezeichnung '"
            + value
            + "'.";
        return new ValueErrorObject<>(
            null,
            new MassenimportErrorDTO(rowIndex, colIndex, errorMessage, MassenimportProblemSeverity.WARNING));
    }

    @Nullable
    public static String getStringCellValue(@NonNull Row row, @NonNull Integer index) {
        Cell cell = row.getCell(index);
        if (cell == null) {
            return null;
        }
        switch (cell.getCellType()) {
        case STRING:
            return cell.getStringCellValue().trim();

        case NUMERIC:
            if (DateUtil.isCellDateFormatted(cell)) {
                return cell.getLocalDateTimeCellValue().toString().trim();
            } else {
                return String.valueOf(cell.getNumericCellValue()).trim();
            }

        case BOOLEAN:
            return String.valueOf(cell.getBooleanCellValue()).trim();

        case FORMULA:
            try {
                return cell.getCellFormula().trim();
            } catch (Exception e) {
                return null;
            }
        default:
            return null;
        }
    }

    public static int getLastNonEmptyRowIndex(Sheet sheet) {
        int lastRow = sheet.getLastRowNum();
        for (int rowIndex = lastRow; rowIndex >= 0; rowIndex--) {
            Row row = sheet.getRow(rowIndex);
            if (row != null) {
                boolean rowHasData = false;
                for (Cell cell : row) {
                    if (cell != null && cell.getCellType() != CellType.BLANK) {
                        rowHasData = true;
                        break;
                    }
                }
                if (rowHasData) {
                    return rowIndex;
                }
            }
        }
        return -1; // all rows are empty
    }

    public static boolean isNullishOrEmpty(@Nullable String value) {
        return value == null || StringUtils.trimToEmpty(value).isEmpty();
    }

}
