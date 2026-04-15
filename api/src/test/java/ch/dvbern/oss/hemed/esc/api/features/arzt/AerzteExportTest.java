/*
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

package ch.dvbern.oss.hemed.esc.api.features.arzt;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Paths;
import java.util.List;

import ch.dvbern.dvbstarter.infrastructure.fileio.FileUtil;
import ch.dvbern.dvbstarter.shared.i18n.translations.AppLanguages;
import ch.dvbern.dvbstarter.shared.i18n.translations.TL;
import ch.dvbern.dvbstarter.shared.i18n.translations.TLProducer;
import ch.dvbern.oss.hemed.esc.api.features.arzt.print.ArztExportService;
import ch.dvbern.oss.hemed.esc.api.features.arzt.types.ExportMode;
import ch.dvbern.oss.hemed.esc.api.shared.auth.TenantEnum;
import ch.dvbern.oss.hemed.esc.api.shared.printer.PrintResultDTO;
import ch.dvbern.oss.hemed.esc.api.testutil.assertj.DocxAssert;
import ch.dvbern.oss.hemed.esc.api.testutil.builder.AdresseBuilder;
import ch.dvbern.oss.hemed.esc.api.testutil.builder.ArztBuilder;
import lombok.RequiredArgsConstructor;
import org.apache.commons.io.FileUtils;
import org.assertj.core.api.Assertions;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

import static ch.dvbern.oss.hemed.esc.api.testutil.TestUtil.loadTestfile;
import static org.assertj.core.api.Assertions.assertThat;

@RequiredArgsConstructor
class AerzteExportTest {
    private final TL tl = new TLProducer(() -> AppLanguages.DE, () -> TenantEnum.BS).forAppLanguage(AppLanguages.DE);
    private static final String OUTPUT_PATH =
        Paths.get(FileUtils.getTempDirectoryPath(), "esc", "aerztemerge").toString();

    @BeforeAll
    static void setUp() throws IOException {
        FileUtils.forceMkdir(new File(OUTPUT_PATH));
        for (TenantEnum tenant : TenantEnum.values()) {
            FileUtils.forceMkdir(new File(OUTPUT_PATH + '/' + tenant));
        }
    }

    @ParameterizedTest
    @EnumSource(TenantEnum.class)
    void exportAerzteDocMerger(TenantEnum tenant) throws IOException {
        // ARRANGE
        List<Arzt> arztList = getArztListForTest();

        PrintResultDTO result = null;

        // ACT
        try (FileOutputStream outputStream = getFileOutputStream("arzt-test", tenant)) {
            result = ArztExportService.generatAearzteExport(arztList, "", tl, ExportMode.TEXTDOCUMENT);
            outputStream.write(result.getContent());

            // ASSERT
            Assertions.assertThat(result).isNotNull();
            Assertions.assertThat(result.getContent()).hasSizeGreaterThan(0);

            String expectedTextKind = readExpectedContent(tenant);

            DocxAssert.assertThatDocx(new ByteArrayInputStream(result.getContent()))
                .containsExactly(expectedTextKind);
        }
    }

    @ParameterizedTest
    @EnumSource(TenantEnum.class)
    void exportAerzteSpreadsheet(TenantEnum tenant) throws IOException {
        // ARRANGE
        List<Arzt> arztList = getArztListForTest();

        PrintResultDTO result = null;

        // ACT
        try (FileOutputStream outputStream = getFileOutputStream("arzt-test", tenant)) {
            result = ArztExportService.generatAearzteExport(arztList, "", tl, ExportMode.SPREADSHEET);
            outputStream.write(result.getContent());

            // ASSERT
            Assertions.assertThat(result).isNotNull();
            Assertions.assertThat(result.getContent()).hasSizeGreaterThan(0);

            // And the result contains our expectations
            List<String> expectedRecords = readExpectedLines(tenant);
            String actual = new String(result.getContent(), StandardCharsets.UTF_8);

            // all expected lines must be present
            assertThat(expectedRecords).isNotEmpty()
                .allSatisfy(expected -> assertThat(actual).as("actual should contain expected string: %s", expected)
                    .contains(expected));
        }
    }

    private String readExpectedContent(TenantEnum tenant) {
        String expectedResultFilename =
            switch (tenant) {
                case BE, BS, WIN, DEFAULT -> "aerzte-export-textcontent.txt";
            };
        File expectedFile = loadTestfile("/AerzteDocMergerTest/expected/" + expectedResultFilename);
        return FileUtil.readTextFromPath(expectedFile.toPath());
    }

    private List<String> readExpectedLines(TenantEnum tenant) {
        String expectedResultFilename =
            switch (tenant) {
                case BE, BS, WIN, DEFAULT -> "aerzte-export-textcontent.csv";
            };
        File expectedFile = loadTestfile("/AerzteDocMergerTest/expected/" + expectedResultFilename);
        return FileUtil.readLineBasedTextFromPath(expectedFile.toPath());
    }

    private FileOutputStream getFileOutputStream(@NonNull String filename, @NonNull TenantEnum tenant)
        throws IOException {
        return new FileOutputStream(getTempFilenamePath(filename, tenant));
    }

    private static String getTempFilenamePath(@NonNull String filename, @NonNull TenantEnum tenant) {
        return OUTPUT_PATH + '/' + tenant.name() + '/' + filename + ".docx";
    }

    private static List<Arzt> getArztListForTest() {
        return List.of(
            ArztBuilder.create(builder -> builder.withName("Arzt1")
                .withBemerkungen("Lorem ipsum")
                .withAdresse(AdresseBuilder.create(ab -> ab.withPlz("90001")))),
            ArztBuilder.create(builder -> builder.withName("Arzt2")
                .withAdresse(AdresseBuilder.create(ab -> ab.withPlz("80001")))),
            ArztBuilder.create(builder -> builder.withName("Arzt21")
                .withAdresse(AdresseBuilder.create(ab -> ab.withPlz("80001")))),
            ArztBuilder.create(builder -> builder.withName("Arzt22")
                .withAdresse(AdresseBuilder.create(ab -> ab.withPlz("80001").withAdresseZusatz(null)))),

            ArztBuilder.create(builder -> builder.withName("Arzt33")
                .withAdresse(AdresseBuilder.create(ab -> ab.withPlz("80001").withOrt(null)))),
            ArztBuilder.create(builder -> builder.withName("Arzt34")
                .withAdresse(AdresseBuilder.create(ab -> ab.withPlz("80001").withPlz(null)))),
            ArztBuilder.create(builder -> builder.withName("Arzt3")
                .withAdresse(AdresseBuilder.create(ab -> ab.withPlz("90001")))),
            ArztBuilder.create(builder -> builder.withAnrede("DR.").withName("Max").withVorame("Muster"))
        );

    }
}
