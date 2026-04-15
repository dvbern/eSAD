/*
 *
 * Copyright (C) 2025 DV Bern AG, Switzerland
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package ch.dvbern.oss.hemed.esc.api.features.importoptions.escada;

import java.io.File;
import java.io.Reader;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

import ch.dvbern.dvbstarter.infrastructure.errorhandling.AppFailureException;
import ch.dvbern.dvbstarter.infrastructure.errorhandling.AppFailureMessage;
import ch.dvbern.oss.hemed.esc.api.features.importoptions.escada.csv.EscadaCsvDTO;
import ch.dvbern.oss.hemed.esc.api.testutil.TestUtil;
import ch.dvbern.oss.hemed.esc.api.testutil.security.TestAsEscadaBS;
import io.quarkus.test.junit.QuarkusTest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.MethodOrderer.OrderAnnotation;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.TestMethodOrder;

import static ch.dvbern.dvbstarter.infrastructure.fileio.FileUtil.createReaderFromPath;
import static ch.dvbern.oss.hemed.esc.api.features.importoptions.escada.SozialversicherungsnummerFixPreprocessor.preprocToEnsureUniqueAhvPerSchuelerID;
import static ch.dvbern.oss.hemed.esc.api.features.importoptions.escada.SozialversicherungsnummerFixPreprocessor.preprocessAndFixInvalidCSVNumbers;

@QuarkusTest
@RequiredArgsConstructor
@Slf4j
@TestMethodOrder(OrderAnnotation.class)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class SozialversicherungsnummerFixPreprocessorTest {
    private final EscadaService escadaService;

    @Test
    @Order(1)
    void preprocessAndFixInvalidCSVNumbers_should_fix_same_broken_nr_in_same_manner() {

        // read csv file
        String fileImport = "/SozialversicherungsnummerFix/ahv-fix_1.csv";
        File firstFile = TestUtil.loadTestfile(fileImport);
        Reader reader = createReaderFromPath(Path.of(firstFile.getPath()));
        List<EscadaCsvDTO> escadaCsvDTOS = escadaService.parseEscadaCsvAndRemoveUndesired(reader);
        List<EscadaCsvDTO> fixedDTOs =
            preprocessAndFixInvalidCSVNumbers(escadaCsvDTOS);

        Assertions.assertThat(fixedDTOs).isNotNull()
            .hasSize(13)
            .extracting(EscadaCsvDTO::schuelerSozialversicheurng)
            .containsExactly(
                "756.2298.3284.00",
                "756.5128.4603.01",
                "756.5830.4285.07",
                "756.1415.6532.03",
                "756.5466.4960.00",
                "756.2247.3235.00",
                "756.0505.4923.02",
                "756.4138.1100.08",
                "756.5914.1512.08",
                "756.0316.2394.00",
                "756.5914.1512.08",
                "756.5914.1512.08",
                "756.4166.8958.09"

            );

        fixedDTOs = preprocToEnsureUniqueAhvPerSchuelerID(fixedDTOs);
        Assertions.assertThat(fixedDTOs).isNotNull()
            .hasSize(13)
            .extracting(EscadaCsvDTO::schuelerSozialversicheurng)
            .containsExactly(
                "756.2298.3284.00",
                "756.5128.4603.01",
                "756.5830.4285.07",
                "756.1415.6532.03",
                "756.5466.4960.00",
                "756.2247.3235.00",
                "756.0505.4923.02",
                "756.4138.1100.08",
                "756.1234.0000.11",
                "756.0316.2394.00",
                "756.1234.0000.11",
                "756.1234.0000.04",
                "756.4166.8958.09"
            );

        String path = getDirToWriteTo();
        LOG.info("Writing fixed csv file to {}", path);
        escadaService.writeCsv(path, fixedDTOs);

    }

    @Test
    @Order(2)
    @TestAsEscadaBS
    @Disabled("This test is disabled because it is only needed when fixing the csv file from base.")
    void escada_csv_upload_pseudo_data() {
        // read csv file
        String fileImport = "/EscadaTest/csv_pseudonymisiert.csv";
        File firstFile = TestUtil.loadTestfile(fileImport);
        Reader reader = createReaderFromPath(Path.of(firstFile.getPath()));
        List<EscadaCsvDTO> escadaCsvDTOS = escadaService.parseEscadaCsvAndRemoveUndesired(reader);
        List<EscadaCsvDTO> fixedDTOs = preprocessAndFixInvalidCSVNumbers(escadaCsvDTOS);
        fixedDTOs = preprocToEnsureUniqueAhvPerSchuelerID(fixedDTOs);
        Assertions.assertThat(fixedDTOs).hasSameSizeAs(escadaCsvDTOS);
        escadaService.writeCsv("csv_pseudonymisiert-fixed.csv", fixedDTOs);

    }

    private String getDirToWriteTo() {
        try {
            // Get the test resources directory dynamically
            URL resourceUrl = SozialversicherungsnummerFixPreprocessorTest.class.getClassLoader().getResource("");
            if (resourceUrl == null) {
                throw new IllegalStateException("Test resource directory not found!");
            }

            // Convert URL to file path
            File resourceDir = Paths.get(resourceUrl.toURI()).toFile();
            File file = new File(resourceDir, "fixed.csv");
            return file.getPath();
        } catch (URISyntaxException e) {
            throw new AppFailureException(
                AppFailureMessage.internalError("Error while getting the test resources directory"), e);
        }
    }
}
