package ch.dvbern.dvbstarter.infrastructure.fileio;

import ch.dvbern.dvbstarter.shared.clock.AppClock;
import ch.dvbern.dvbstarter.shared.clock.FixedClockProvider;
import ch.dvbern.dvbstarter.shared.types.cleanfilename.CleanFileName;
import ch.dvbern.dvbstarter.shared.types.id.ID;
import ch.dvbern.oss.hemed.esc.api.features.labels.Label;
import ch.dvbern.oss.hemed.esc.api.features.labels.types.LabelTyp;
import ch.dvbern.oss.hemed.esc.api.shared.fileio.FileData;
import ch.dvbern.oss.hemed.esc.api.shared.fileio.FileInfoS3;
import ch.dvbern.oss.hemed.esc.api.shared.fileio.FileTyp;
import ch.dvbern.oss.hemed.esc.api.shared.mandant.TranslatedString;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import org.apache.commons.lang3.StringUtils;
import org.apache.tika.mime.MediaType;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import static ch.dvbern.dvbstarter.shared.i18n.datetime.DateConst.ZONE_ID;
import static org.assertj.core.api.Assertions.assertThat;

class FileNameUtilTest {

    private final FixedClockProvider clockProvider =
        FixedClockProvider.of(OffsetDateTime.of(2025, 4, 11, 0, 0, 0, 0, ZoneOffset.UTC).toInstant());
    private final AppClock appClock = new AppClock(clockProvider);

    @ParameterizedTest
    @CsvSource({
        "3, 4",
        "2, 3",
        "1, 2",
        "0, 0"
    })
    void testGetNextNumber(int listSize, int expected) {
        // ARRANGE
        LocalDate today = this.appClock.localDateTime().now().toLocalDate();
        FileTyp fileTyp = FileTyp.IMPFAUSWEIS;
        FileInfoS3 fileInfoS3 = FileInfoS3.of(CleanFileName.parse("File.pdf"), MediaType.TEXT_PLAIN, 92378L);
        List<FileData> files = List.of(
            new FileData(
                FileTyp.IMPFAUSWEIS,
                "12-03-2025_Max-Mustermann_Impfausweis.pdf",
                fileInfoS3).setTimestampErstellt(
                OffsetDateTime.now(this.clockProvider.getClock()).minusMinutes(2)),
            new FileData(
                FileTyp.IMPFAUSWEIS,
                "12-03-2025_Max-Mustermann_Impfausweis_2.pdf",
                fileInfoS3).setTimestampErstellt(
                OffsetDateTime.now(this.clockProvider.getClock()).minusMinutes(1)),
            new FileData(
                FileTyp.IMPFAUSWEIS,
                "12-03-2025_Max-Mustermann_Impfausweis_3.pdf",
                fileInfoS3).setTimestampErstellt(
                OffsetDateTime.now(this.clockProvider.getClock())));
        String formattedName = "Max-Mustermann";
        if (listSize != 0) {
            files = files.subList(0, listSize);
        } else {
            files = List.of();
        }
        // ACT
        int result = FileNameUtil.getNextNumberForFile(today, fileTyp, files, formattedName);

        // ASSERT
        assertThat(result).isEqualTo(expected);
    }

    @ParameterizedTest
    @CsvSource({
        "350f2746-7db4-4dfa-ae28-1b3b5812434a, Impfausweis, impfausweis", // Impfausweis Label Id
        "f8514824-9694-408e-bdb6-d72e376d7833, Fragebogen, fragebogen", // Fragebogen Label Id
        ", ,notizupload"
    })
    void testGetTypFromLabel(String id, String translatedString, String expectedTyp) {
        // ARRANGE
        Set<Label> labels = Set.of();
        if (!StringUtils.isEmpty(id)) {
            Label label = new Label(
                new TranslatedString(translatedString, translatedString),
                LabelTyp.NOTIZEN,
                true,
                false,
                true);
            label.setId(ID.of(UUID.fromString(id), Label.class));
            labels = Set.of(label);
        }
        // ACT
        FileTyp result = FileNameUtil.getTypFromLabel(labels, FileTyp.NOTIZUPLOAD);
        // ASSERT
        assertThat(result.getPrefix()).isEqualTo(expectedTyp);
    }

    @Test
    void testGenerateFileWithCorrectNameForImpfausweis() {
        // ARRANGE
        LocalDate today = this.appClock.localDateTime().now().toLocalDate();

        FileTyp fileTyp = FileTyp.IMPFAUSWEIS;
        FileInfoS3 fileInfoS3 = FileInfoS3.of(CleanFileName.parse("File.pdf"), MediaType.TEXT_PLAIN, 92378L);
        List<FileData> files = List.of(
            new FileData(
                fileTyp,
                "12-03-2025_Mustermann-Max_Impfausweis.pdf",
                fileInfoS3).setTimestampErstellt(
                OffsetDateTime.now(this.clockProvider.getClock())));
        String formattedName = "Mustermann Max";

        // ACT
        String originalFileName = fileInfoS3.getFileName().getFileName();
        String calculatedFileName = FileNameUtil.calculateAutomaticFilename(
            files,
            today,
            formattedName,
            fileTyp,
            originalFileName);

        FileData result = new FileData(fileTyp, calculatedFileName, fileInfoS3);
        // ASSERT
        assertThat(result.getFileTyp()).isEqualTo(fileTyp);
        assertThat(result.getFileInfoBucket().getFileName().getFileName()).isEqualTo("File.pdf");

        String expectedCalculatedName = "11-04-2025_Mustermann-Max_Impfausweis_2.pdf";
        assertThat(result.getCalculatedFileName()).isEqualTo(expectedCalculatedName);
    }

    @Test
    void basicFolderName() {
        // ARRANGE
        OffsetDateTime today = LocalDate.of(2025, 05, 06).atStartOfDay(ZONE_ID).toOffsetDateTime();

        // ACT + ASSERT
        assertThat(FileNameUtil.basicFolderName(today, "shortName", 0)).isEqualTo("06-05-2025_shortName");
        assertThat(FileNameUtil.basicFolderName(today, "shortName", 1)).isEqualTo("06-05-2025_shortName");
        assertThat(FileNameUtil.basicFolderName(today, "shortName", 2)).isEqualTo("06-05-2025_shortName_2");
        assertThat(FileNameUtil.basicFolderName(
            today,
            "ext e ee eee eeee eeeee eeeeee eeeeee mely long name",
            0)).isEqualTo("06-05-2025_ext-e-ee-eee-eeee-eeeee-eeeeee-eeeeee-mely-long-na");
        assertThat(FileNameUtil.basicFolderName(
            today,
            "ext e ee eee eeee eeeee eeeeee eeeeee mely long name",
            1)).isEqualTo("06-05-2025_ext-e-ee-eee-eeee-eeeee-eeeeee-eeeeee-mely-long-na");
        assertThat(FileNameUtil.basicFolderName(
            today,
            "ext e ee eee eeee eeeee eeeeee eeeeee mely long name",
            2)).isEqualTo("06-05-2025_ext-e-ee-eee-eeee-eeeee-eeeeee-eeeeee-mely-long-na_2");
    }
}
