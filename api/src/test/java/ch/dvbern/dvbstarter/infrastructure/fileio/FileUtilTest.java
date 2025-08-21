/*
 *
 * Copyright (C) 2024 DV Bern AG, Switzerland
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

package ch.dvbern.dvbstarter.infrastructure.fileio;

import ch.dvbern.dvbstarter.infrastructure.errorhandling.AppFailureException;
import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.Set;
import java.util.UUID;

import ch.dvbern.dvbstarter.shared.types.cleanfilename.CleanFileName;
import ch.dvbern.oss.hemed.esc.api.Util;
import ch.dvbern.oss.hemed.esc.api.shared.fileio.FileTyp;
import ch.dvbern.oss.hemed.esc.api.shared.fileio.FileUploadWrapper;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.tika.mime.MediaType;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import static org.assertj.core.api.Assertions.assertThat;

class FileUtilTest {

    private static final String FIXTURES_PATH = FileUtilTest.class.getSimpleName();

    @Test
    void generateUUIDWithFileExtension() {
        String s = FileUtil.generateUUIDWithFileExtension(
            CleanFileName.parse("test.txt"));
        assertThat(s).hasSize(UUID.randomUUID().toString().length() + 4);
    }

    @Test
    void generateUUIDWithFileExtension_withPrefix() {
        String s = FileUtil.generateUUIDWithFileExtension(
            FileTyp.IMPFAUSWEIS.getPrefix(),
            CleanFileName.parse("test.png"));
        assertThat(s)
            .hasSize(FileTyp.IMPFAUSWEIS.getPrefix().length() + 1 + UUID.randomUUID().toString().length() + 4)
            .startsWith(FileTyp.IMPFAUSWEIS.getPrefix() + '-')
            .endsWith(".png");
    }

    @Test
    void generateUUIDWithOriginalFilename() {
        CleanFileName originalFilename = CleanFileName.parse("original-file_name.png");
        String s = FileUtil.generateUUIDWithOriginalFilename(
            FileTyp.ESCADA_SYNC.getPrefix(),
            originalFilename);
        assertThat(s)
            .hasSize(
                FileTyp.ESCADA_SYNC.getPrefix().length()
                    + 1
                    + UUID.randomUUID().toString().length()
                    + 2
                    + originalFilename.getFileName().length()
            )
            .startsWith(FileTyp.ESCADA_SYNC.getPrefix() + '-')
            .endsWith(originalFilename.getFileName());

        String extractedOriginalFilename = FileUtil.extractOriginalFileNameFromFileBucketId(s);
        assertThat(extractedOriginalFilename).isEqualTo(originalFilename.getFileName());
    }

    @ParameterizedTest
    @CsvSource({
        "some-prefix_someUuuid__originalFileName.png, originalFileName.png",
        "some-prefix_someUuuid__originalFileName, originalFileName",
        "some-prefix_someUuuid__originalFileName.txt, originalFileName.txt",
        "some_prefix_someUuuid__original_FileName.txt, original_FileName.txt",
        "some-prefix_someUuuid__original_filename__with__underscores.txt, original_filename__with__underscores.txt",
    })
    void testFileNameExtraction(String fileId, String expectedOriginalFileName) {
        String extractedOriginalFileName = FileUtil.extractOriginalFileNameFromFileBucketId(fileId);
        assertThat(extractedOriginalFileName).isEqualTo(expectedOriginalFileName);
    }

    @Nested
    class mediaTypeIsAllowed {
        private final Set<MediaType> allowedTypes = Set.of(
            MediaType.parse("image/png"),
            MediaType.parse("image/jpeg"),
            MediaType.application("vnd.openxmlformats-officedocument.wordprocessingml.document"),
            MediaType.application("msword")
        );

        @ParameterizedTest
        @CsvSource({
            "logo.png,           true",
            "sample.jpg,         true",
            "pdfFileExample.pdf, false",
            "rectangle.svg,      false",
            "sampletext.txt,     false",
            "test-docx.docx,     true",
            "test-doc.doc,       true"
        })
        void is_implemented_for_various_file_types(
            String fixtureFileResource,
            boolean expectAllowed
        ) {
            File file = createTempFileFor(fixtureFileResource);

            assertThat(FileUtil.mediaTypeIsAllowed(FileUploadWrapper.fromFile(file), allowedTypes))
                .isEqualTo(expectAllowed);
        }
    }

    @Test
    void readTextFromPath() {
        File file = createTempFileFor("sampletext.txt");
        assertThat(FileUtil.readTextFromPath(file.toPath()))
            .isNotEmpty().isEqualTo("""
                This is  a samplefile for testing
                It has an empty line at the end
                """);
    }

    @Test
    void readTextFromPath_shouldThrowExceptionForNonExistentFile() {
        // Create a path to a non-existent file
        File nonExistentFile = new File("non-existent-file.txt");
        Path nonExistentFilePath = nonExistentFile.toPath();

        // Assert that the method throws an exception when given a non-existent file path
        Assertions.assertThrows(
            AppFailureException.class,
            () -> FileUtil.readTextFromPath(nonExistentFilePath),
            "Expected readTextFromPath to throw Exception for non-existent file"
        );
    }

    @ParameterizedTest
    @CsvSource({
        ".png       , image/png       , false",
        ".jpg       , image/jpeg      , false",
        ".pdf       , application/pdf , false",
        ".csv       , text/csv        , false",
        ".eml       , message/rfc822  , false",
        "png        ,                 , true",
        "text/plain ,                 , true",
        ".docx      , application/vnd.openxmlformats-officedocument.wordprocessingml.document, false",
        ".doc       , application/msword, false"
    })
    void getMediaTypeFromExtension(String extensionInput, String expectedMediaType, boolean expectException) {

        if (!expectException) {
            MediaType mediaTypeFromExtension = FileUtil.getMediaTypeFromExtension(extensionInput);
            assertThat(mediaTypeFromExtension).isEqualTo(MediaType.parse(expectedMediaType));
        } else {
            Assertions.assertThrows(
                IllegalArgumentException.class,
                () -> FileUtil.getMediaTypeFromExtension(extensionInput));
        }
    }

    @ParameterizedTest
    @CsvSource({
        ".png       , image/png       , false",
        ".jpg       , image/jpeg      , false",
        ".pdf       , application/pdf , false",
        ".csv       , text/csv        , false",
        ".eml       , message/rfc822  , false",
        "png        ,                 , true",
        "text/plain , text/plain      , false",
        "text/csv   , text/csv        , false"

    })
    void parseMediaTypeOrFileExtensionToMediaType(
        String extensionInput,
        String expectedMediaType,
        boolean expectException) {

        if (!expectException) {
            MediaType mediaTypeFromExtension = FileUtil.parseMediaTypeOrFileExtensionToMediaType(extensionInput);
            assertThat(mediaTypeFromExtension).isEqualTo(MediaType.parse(expectedMediaType));
        } else {
            Assertions.assertThrows(
                IllegalArgumentException.class,
                () -> FileUtil.getMediaTypeFromExtension(extensionInput));
        }
    }

    private File createTempFileFor(String testFixture) {
        try {
            var path = "%s/%s".formatted(FIXTURES_PATH, testFixture);
            var bytes = Util.readResource(path);

            String baseName = FilenameUtils.getBaseName(testFixture);
            String suffix = '.' + FilenameUtils.getExtension(testFixture);
            File tempFile = File.createTempFile(baseName, suffix);
            tempFile.deleteOnExit();

            FileUtils.writeByteArrayToFile(tempFile, bytes);

            return tempFile;
        } catch (IOException e) {
            throw new RuntimeException("Problem reading/writing file: " + testFixture, e);
        }
    }
}
