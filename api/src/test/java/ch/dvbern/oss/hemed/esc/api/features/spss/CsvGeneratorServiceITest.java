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

package ch.dvbern.oss.hemed.esc.api.features.spss;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.util.List;
import java.util.stream.Stream;

import ch.dvbern.dvbstarter.infrastructure.fileio.FileUtil;
import ch.dvbern.dvbstarter.shared.types.emailaddress.EmailAddress;
import ch.dvbern.oss.hemed.esc.api.features.anlass.Anlass;
import ch.dvbern.oss.hemed.esc.api.features.anlass.AnlassService;
import ch.dvbern.oss.hemed.esc.api.features.anlass.einladung.Einladung;
import ch.dvbern.oss.hemed.esc.api.features.anlass.einladung.EinladungService;
import ch.dvbern.oss.hemed.esc.api.features.anlass.anlassconfig.AnlassConfig;
import ch.dvbern.oss.hemed.esc.api.features.anlass.types.AnlassStatus;
import ch.dvbern.oss.hemed.esc.api.testutil.AbstractITest;
import ch.dvbern.oss.hemed.esc.api.testutil.FixtureService;
import ch.dvbern.oss.hemed.esc.api.testutil.builder.AnlassBuilder;
import ch.dvbern.oss.hemed.esc.api.testutil.security.TestAsAdminBS;
import io.quarkus.test.junit.QuarkusTest;
import lombok.RequiredArgsConstructor;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import static ch.dvbern.oss.hemed.esc.api.testutil.TestUtil.loadTestfile;
import static org.assertj.core.api.Assertions.assertThat;

@QuarkusTest
@RequiredArgsConstructor
class CsvGeneratorServiceITest extends AbstractITest {

    private final CsvGeneratorService csvGeneratorService;
    private final FixtureService fixtureService;
    private final AnlassService anlassService;
    private final EinladungService einladungService;

    @Override
    @BeforeEach
    public void setUp() {
        super.setUp();
    }

    @ParameterizedTest
    @MethodSource("spssReportResults")
    @TestAsAdminBS
    void generateSpssReport(
        @NonNull SpssReportType spssReportType,
        @NonNull String expectedCsvPath
    ) {
        // ARRANCE
        LocalDate anlassDatum = LocalDate.of(2025, 6, 22);
        AnlassConfig anlassConfig = fixtureService.createAnlassConfigAllOptional(
            anlassDatum.getYear(),
            spssReportType.getAnlassStatusList().getFirst());
        Anlass anlass = AnlassBuilder.create(builder -> builder
            .withDatumSetSchuljahr(anlassDatum)
            .withAnlassStatus(AnlassStatus.ABGESCHLOSSEN));
        anlass.setAnlassConfig(anlassConfig);
        anlassService.persist(anlass);
        Einladung einladung = fixtureService.createEinladung(anlass);

        // ACT
        byte[] result =
            csvGeneratorService.generateSpssReport(spssReportType, EmailAddress.fromString("admin.basel@example.com"));

        // ASSERT
        // Before it was not exported
        assertThat(einladung.isSpssExported()).isFalse();

        // Now its exported
        Einladung einladungAfter = einladungService.getById(einladung.getId());
        assertThat(einladungAfter.isSpssExported()).isTrue();

        // And the result contains our expectations
        File expectedCsv = loadTestfile(expectedCsvPath);
        List<String> expectedStrings =
            FileUtil.readLineBasedTextFromPath(expectedCsv.toPath()); // read excted csv lines
        String actual = new String(result, StandardCharsets.UTF_8);

        // all expected lines must be present
        assertThat(expectedStrings).isNotEmpty()
            .allSatisfy(expectedString -> assertThat(actual).as(
                    "actual should contain expected string: %s",
                    expectedString)
                .contains(expectedString));
    }

    @NonNull
    private static Stream<Arguments> spssReportResults() {
        return Stream.of(
            Arguments.of(SpssReportType.KINDERGARTEN, "/CsvGenerator/expected/kindergarten.csv"),
            Arguments.of(SpssReportType.KLASSE_4, "/CsvGenerator/expected/klasse4.csv"),
            Arguments.of(SpssReportType.KLASSE_8, "/CsvGenerator/expected/klasse8.csv"),
            Arguments.of(SpssReportType.KLASSE_9, "/CsvGenerator/expected/klasse9.csv"),
            Arguments.of(SpssReportType.FREMDSPRACHEN, "/CsvGenerator/expected/fremdsprache.csv"),
            Arguments.of(SpssReportType.IMPF_ANLAESSE, "/CsvGenerator/expected/impfanlass.csv")
        );
    }
}
