package ch.dvbern.oss.hemed.esc.api.shared.plz;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import ch.dvbern.oss.hemed.esc.api.shared.config.service.ConfigService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.mockito.Mockito;

import static org.assertj.core.api.Assertions.assertThat;

public class PLZImportServiceTest {
    public static final String PATH_TO_TESTFILE = "/plz/plz_verzeichnis_v2_test.csv";

    private final ConfigService configServiceMock = Mockito.mock(ConfigService.class);

    @BeforeEach
    void beforeEach() {
        Mockito
            .when(configServiceMock.getPlzImportFileLocation())
            .thenReturn(PATH_TO_TESTFILE);
    }

    @Test
    void testCsvParsing() throws IOException {
        PLZImportService plzImportService = new PLZImportService(configServiceMock);

        List<PLZData> importedData;
        try (InputStream inputStreamOfCsv = PLZImportServiceTest.class.getResourceAsStream(PATH_TO_TESTFILE)) {
            Objects.requireNonNull(inputStreamOfCsv);
            importedData = plzImportService.importPlzDataFromCSV(inputStreamOfCsv);
        }
        assertThat(importedData)
            .isNotNull()
            .hasSize(29);

    }

    @Nested
    class ImportedNameCorrectTest {

        private final List<PLZData> importedData;

        public ImportedNameCorrectTest() throws IOException {
            PLZImportService plzImportService = new PLZImportService(configServiceMock);
            try (InputStream inputStreamOfCsv = PLZImportServiceTest.class.getResourceAsStream(PATH_TO_TESTFILE)) {
                Objects.requireNonNull(inputStreamOfCsv);
                importedData = plzImportService.importPlzDataFromCSV(inputStreamOfCsv);
            }
        }

        @ParameterizedTest
        @CsvSource({ "Lausanne", "Valendas", "Castrisch", "Ilanz", "Ladir", "Falera", "Morissen", "Degen", "Genève",
            "Bulle Jardins P.L.", "Zürich 27 Zust", "Daillens SPS", "Martigny rue Avou.", "Cadenazzo ZF",
            "Urdorf PZ", "Pratteln Sperrgut", "Grabs Staatsstr", "Zofingen U. Graben", "Malters Luzernstr",
            "Lausen Grammontstr", "Bern Weltpoststr", "Oftringen Tychb.", "Wimmis Herrenmatte", "Cevio Dist",
            "Glis Gliserallee", "Luzern Hirschengr", "Beride d. Bedigli.", "Chapella", "Urtenen-S. Z-Platz" })
        void testImportedName(String expectedImportedName) {
            assertThat(importedData.stream()
                .map(PLZData::getOrtsBezeichnung)
                .anyMatch(s -> s.equals(expectedImportedName))).as("Expected to find "
                + expectedImportedName
                + " in list of names "
                + importedData.stream()
                .map(PLZData::getOrtsBezeichnung)
                .collect(Collectors.joining(";"))).isTrue();

        }
    }
}
