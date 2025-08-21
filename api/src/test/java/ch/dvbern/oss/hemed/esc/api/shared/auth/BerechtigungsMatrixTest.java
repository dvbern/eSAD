package ch.dvbern.oss.hemed.esc.api.shared.auth;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.List;

import ch.dvbern.oss.hemed.esc.api.shared.auth.berechtigugnsmatrix.BerechtigungsMatrixCSVHelper;
import ch.dvbern.oss.hemed.esc.api.testutil.TestUtil;
import org.apache.commons.csv.CSVRecord;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;

class BerechtigungsMatrixTest {

    private static final Path OUTPUT_PATH = Path.of("target/exported-fach-rollen.csv");

    public static final String EXPECTED_FACH_ROLLEN_CSV = "expected-fach-rollen.csv";
    private static final Path EXPECTED_PATH = TestUtil.createTempFileFor(
        Path.of(BerechtigungsMatrixTest.class.getSimpleName(), EXPECTED_FACH_ROLLEN_CSV),
        EXPECTED_FACH_ROLLEN_CSV
    );

    @Test
    void checkExpectedCSVFileExistsAndContainsExpectedSeparator() throws IOException {
        try (BufferedReader reader = new BufferedReader(new FileReader(
            EXPECTED_PATH.toFile(),
            StandardCharsets.UTF_8))) {
            String firstLine = reader.readLine();  // Read the first line
            Assertions.assertThat(firstLine)
                .as(
                    "The first line of the %s file should contain the correct column separator",
                    EXPECTED_FACH_ROLLEN_CSV)
                .contains(BerechtigungsMatrixCSVHelper.getCsvFormat().getDelimiterString());
        }
    }

    @Test
    void exportedCSVMatchesExpectedOutput() throws IOException {
        // Generate fresh CSV Export file in /tartget
        BerechtigungsMatrixCSVHelper.writeBerechtigungenToCSV(OUTPUT_PATH);

        // Read both files and compare
        List<CSVRecord> actualRecords = BerechtigungsMatrixCSVHelper.readCSVRecords(OUTPUT_PATH);
        List<CSVRecord> expectedRecords = BerechtigungsMatrixCSVHelper.readCSVRecords(EXPECTED_PATH);

        // Compare number of records
        Assertions.assertThat(actualRecords)
            .as("Number of records should match")
            .hasSameSizeAs(expectedRecords);

        // Compare each record
        for (int i = 0; i < expectedRecords.size(); i++) {
            CSVRecord expectedRecord = expectedRecords.get(i);
            CSVRecord actualRecord = actualRecords.get(i);

            // Compare number of columns
            Assertions.assertThat(actualRecord).as(
                    "Number of columns in record should match, maybe check column separater is correct",
                    expectedRecord.get(0))
                .hasSize(expectedRecord.size());

            // Compare values in each column
            for (int j = 0; j < expectedRecord.size(); j++) {
                String actualCellValue = actualRecord.get(j);
                String expectedCellValue = expectedRecord.get(j);
                Assertions.assertThat(actualCellValue)
                    .as(
                        "Values should match at record %d, column %d. "
                            + "The problem concerns the Berechtigung %s for the FachRolle %s",
                        i,
                        j,
                        expectedRecord.get(0),
                        BerechtigungsMatrixCSVHelper.getHeaderStrings()[j])
                    .isEqualTo(expectedCellValue);
            }
        }
    }

}
