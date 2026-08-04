/*
 * Copyright (C) 2026 DV Bern AG, Switzerland
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

package ch.dvbern.oss.hemed.esc.api.features.example.developer;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.PosixFilePermission;
import java.util.List;
import java.util.UUID;
import java.util.stream.Stream;

import ch.dvbern.dvbstarter.infrastructure.errorhandling.AppFailureException;
import ch.dvbern.dvbstarter.infrastructure.errorhandling.AppFailureMessage;
import ch.dvbern.dvbstarter.infrastructure.fileio.FileNameUtil;
import ch.dvbern.dvbstarter.shared.types.cleanfilename.CleanFileName;
import ch.dvbern.oss.hemed.esc.api.features.spss.CsvReportType;
import ch.dvbern.oss.hemed.esc.api.features.spss.SpssReportType;
import ch.dvbern.oss.hemed.esc.api.shared.documentqueue.DocumentQueue;
import ch.dvbern.oss.hemed.esc.api.shared.documentqueue.DocumentQueueCsvParams;
import ch.dvbern.oss.hemed.esc.api.shared.documentqueue.DocumentQueueFileService;
import ch.dvbern.oss.hemed.esc.api.shared.documentqueue.DocumentQueueMassenbriefParams;
import ch.dvbern.oss.hemed.esc.api.shared.documentqueue.DocumentQueueParams;
import ch.dvbern.oss.hemed.esc.api.shared.fileio.FileTyp;
import ch.dvbern.oss.hemed.esc.api.testutil.AbstractITest;
import ch.dvbern.oss.hemed.esc.api.testutil.FixtureService;
import ch.dvbern.oss.hemed.esc.api.testutil.builder.DocumentQueueBuilder;
import ch.dvbern.oss.hemed.esc.api.testutil.security.TestAsSupportBS;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import io.quarkus.test.junit.QuarkusTest;
import jakarta.enterprise.context.control.ActivateRequestContext;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.TestInstance.Lifecycle;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import static ch.dvbern.oss.hemed.esc.api.testutil.FixtureService.BRIEFVORLAGE_ID_1;
import static ch.dvbern.oss.hemed.esc.api.testutil.FixtureService.BRIEFVORLAGE_ID_2;
import static ch.dvbern.oss.hemed.esc.api.testutil.FixtureService.DEFAULT_BENUTZER;
import static org.assertj.core.api.Assertions.assertThat;

@QuarkusTest
@RequiredArgsConstructor
@TestInstance(Lifecycle.PER_CLASS)
class DocumentQueueFileServiceITest extends AbstractITest {

    private final DocumentQueueFileService documentQueueFileService;
    private final FixtureService fixtureService;

    @Override
    @BeforeEach
    public void setUp() {
        super.setUp();
    }

    @BeforeAll
    @ActivateRequestContext
    void initalSetup() {
        fixtureService.persistBenutzerAndBriefvorlagenForDocumentQueueFileServiceITest();
    }

    @Test
    @TestAsSupportBS
    void createTempFile() {
        // ARRANGE
        CleanFileName fileName = CleanFileName.parse("test-document.pdf");

        // ACT
        Path result = documentQueueFileService.createTempFile(fileName);

        // ASSERT
        assertThat(Files.exists(result)).isTrue();
        assertThat(result.getFileName().toString())
            .startsWith("test-document_")
            .endsWith(".pdf");
    }

    @Test
    @TestAsSupportBS
    void createTempFileUsesUnpredictableNamesAndOwnerOnlyPermissions() throws Exception {
        // ARRANGE
        CleanFileName fileName = CleanFileName.parse("test-document.pdf");

        // ACT
        Path first = documentQueueFileService.createTempFile(fileName);
        Path second = documentQueueFileService.createTempFile(fileName);

        // ASSERT
        assertThat(first).isNotEqualTo(second);
        if (first.getFileSystem().supportedFileAttributeViews().contains("posix")) {
            assertThat(Files.getPosixFilePermissions(first)).containsExactlyInAnyOrder(
                PosixFilePermission.OWNER_READ,
                PosixFilePermission.OWNER_WRITE);
        }
    }

    @Test
    @TestAsSupportBS
    void createTempFileWithContent() throws Exception {
        // ARRANGE
        CleanFileName fileName = CleanFileName.parse("test-document.pdf");
        byte[] expectedContent = "Hello, this is test content!".getBytes(StandardCharsets.UTF_8);

        // ACT
        Path result = documentQueueFileService.createTempFile(fileName, expectedContent);

        // ASSERT
        assertThat(Files.exists(result)).isTrue();
        assertThat(Files.readAllBytes(result)).isEqualTo(expectedContent);
    }

    @ParameterizedTest
    @MethodSource("cleanFileNameResults")
    @TestAsSupportBS
    void getCleanFileNameForDocumentQueue(
        @NonNull DocumentQueue documentQueue,
        @NonNull String expectedBaseFileName
    ) {
        // ARRANGE
        String today = getAppClock().getNow().toLocalDate().format(FileNameUtil.DATE_FORMATTER_FOR_FILENAME);
        String expected = today + expectedBaseFileName;

        // ACT
        CleanFileName result = documentQueueFileService.getCleanFileNameFromDocumentQueue(documentQueue);

        // ASSERT
        assertThat(result.getFileName()).isEqualTo(expected);
    }

    @NonNull
    private static Stream<Arguments> cleanFileNameResults() {
        // Prepare Params for DocumentQueue Arguments
        DocumentQueueParams spssParamsKG = new DocumentQueueParams(SpssReportType.KINDERGARTEN);
        DocumentQueueParams spssParamsKl4 = new DocumentQueueParams(SpssReportType.KLASSE_4);
        DocumentQueueParams spssParamsKl8 = new DocumentQueueParams(SpssReportType.KLASSE_8);
        DocumentQueueParams spssParamsKl9 = new DocumentQueueParams(SpssReportType.KLASSE_9);
        DocumentQueueParams spssParamsFremdsprachen = new DocumentQueueParams(SpssReportType.FREMDSPRACHEN);
        DocumentQueueParams spssParamsImpfen = new DocumentQueueParams(SpssReportType.IMPF_ANLAESSE);
        DocumentQueueCsvParams csvParamsSchuluntersuchung = new DocumentQueueCsvParams(CsvReportType.SCHULUNTERSUCHUNG);
        DocumentQueueCsvParams csvParamsImpfstoffe = new DocumentQueueCsvParams(CsvReportType.IMPFSTOFFE);
        DocumentQueueMassenbriefParams massenbriefParams1 = new DocumentQueueMassenbriefParams(
            BRIEFVORLAGE_ID_1.getId(),
            null,
            List.of(UUID.randomUUID().toString())
        );
        DocumentQueueMassenbriefParams massenbriefParams2 = new DocumentQueueMassenbriefParams(
            BRIEFVORLAGE_ID_2.getId(),
            null,
            List.of(UUID.randomUUID().toString())
        );

        // Creating DocumentQueue Objects
        return Stream.of(
            Arguments.of(
                DocumentQueueBuilder.create(builder -> builder
                    .withFileTyp(FileTyp.PLAYGROUND)
                    .withDisplayName("Beispiel Eintrag")
                    .withBenutzer(DEFAULT_BENUTZER)),
                "_Playground.pdf"),
            Arguments.of(
                DocumentQueueBuilder.create(builder -> builder
                    .withFileTyp(FileTyp.PLAYGROUND_ERROR)
                    .withDisplayName("Beispiel Fehler-Eintrag")
                    .withBenutzer(DEFAULT_BENUTZER)),
                "_Playground-Fehler.pdf"),
            Arguments.of(
                DocumentQueueBuilder.create(builder -> builder
                    .withFileTyp(FileTyp.SPSS_REPORT)
                    .withParameter(getParamsAsString(spssParamsKG))
                    .withDisplayName("Kindergarten")
                    .withBenutzer(DEFAULT_BENUTZER)),
                "_Spss-Report-Kindergarten.csv"),
            Arguments.of(
                DocumentQueueBuilder.create(builder -> builder
                    .withFileTyp(FileTyp.SPSS_REPORT)
                    .withParameter(getParamsAsString(spssParamsKl4))
                    .withDisplayName("4_Primar")
                    .withBenutzer(DEFAULT_BENUTZER)),
                "_Spss-Report-4_Primar.csv"),
            Arguments.of(
                DocumentQueueBuilder.create(builder -> builder
                    .withFileTyp(FileTyp.SPSS_REPORT)
                    .withParameter(getParamsAsString(spssParamsKl8))
                    .withDisplayName("2_Sek")
                    .withBenutzer(DEFAULT_BENUTZER)),
                "_Spss-Report-2_Sek.csv"),
            Arguments.of(
                DocumentQueueBuilder.create(builder -> builder
                    .withFileTyp(FileTyp.SPSS_REPORT)
                    .withParameter(getParamsAsString(spssParamsKl9))
                    .withDisplayName("3_Sek")
                    .withBenutzer(DEFAULT_BENUTZER)),
                "_Spss-Report-3_Sek.csv"),
            Arguments.of(
                DocumentQueueBuilder.create(builder -> builder
                    .withFileTyp(FileTyp.SPSS_REPORT)
                    .withParameter(getParamsAsString(spssParamsFremdsprachen))
                    .withDisplayName("Fremdsprache")
                    .withBenutzer(DEFAULT_BENUTZER)),
                "_Spss-Report-Fremdsprache.csv"),
            Arguments.of(
                DocumentQueueBuilder.create(builder -> builder
                    .withFileTyp(FileTyp.SPSS_REPORT)
                    .withParameter(getParamsAsString(spssParamsImpfen))
                    .withDisplayName("Impfanlässe")
                    .withBenutzer(DEFAULT_BENUTZER)),
                "_Spss-Report-Impfanlässe.csv"),
            Arguments.of(
                DocumentQueueBuilder.create(builder -> builder
                    .withFileTyp(FileTyp.CSV_REPORT)
                    .withParameter(getParamsAsString(csvParamsSchuluntersuchung))
                    .withDisplayName("Schuluntersuchung")
                    .withBenutzer(DEFAULT_BENUTZER)),
                "_CSV-Report-Schuluntersuchung.csv"),
            Arguments.of(
                DocumentQueueBuilder.create(builder -> builder
                    .withFileTyp(FileTyp.CSV_REPORT)
                    .withParameter(getParamsAsString(csvParamsImpfstoffe))
                    .withDisplayName("Impfstoffe")
                    .withBenutzer(DEFAULT_BENUTZER)),
                "_CSV-Report-Impfstoffe.csv"),
            Arguments.of(
                DocumentQueueBuilder.create(builder -> builder
                    .withFileTyp(FileTyp.MASSENBRIEF_ZIP)
                    .withParameter(getParamsAsString(massenbriefParams1))
                    .withDisplayName("Test-Vorlage Massenbrief")
                    .withBenutzer(DEFAULT_BENUTZER)),
                "_Massenbrief-Test-Vorlage.zip"),
            Arguments.of(
                DocumentQueueBuilder.create(builder -> builder
                    .withFileTyp(FileTyp.MASSENBRIEF_ZIP)
                    .withParameter(getParamsAsString(massenbriefParams2))
                    .withDisplayName("Arztbesuch Kindergarten Massenbrief")
                    .withBenutzer(DEFAULT_BENUTZER)),
                "_Massenbrief-Arztbesuch-Kindergarten.zip")
        );
    }

    private static String getParamsAsString(@Nullable Object value) {
        ObjectMapper objectMapper = new ObjectMapper().registerModule(new JavaTimeModule());
        try {
            return objectMapper.writeValueAsString(value);
        } catch (Exception e) {
            throw new AppFailureException(
                AppFailureMessage.internalError(
                    "Could not store json parameter for doc creation"), e);
        }
    }
}
