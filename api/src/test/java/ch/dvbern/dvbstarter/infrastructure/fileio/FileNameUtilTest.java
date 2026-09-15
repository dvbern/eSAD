package ch.dvbern.dvbstarter.infrastructure.fileio;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import ch.dvbern.dvbstarter.shared.clock.AppClock;
import ch.dvbern.dvbstarter.shared.clock.FixedClockProvider;
import ch.dvbern.dvbstarter.shared.types.cleanfilename.CleanFileName;
import ch.dvbern.dvbstarter.shared.types.id.ID;
import ch.dvbern.oss.hemed.esc.api.features.labels.Label;
import ch.dvbern.oss.hemed.esc.api.features.labels.types.LabelTyp;
import ch.dvbern.oss.hemed.esc.api.shared.fileio.FileData;
import ch.dvbern.oss.hemed.esc.api.shared.fileio.FileInfo;
import ch.dvbern.oss.hemed.esc.api.shared.fileio.FileTyp;
import ch.dvbern.oss.hemed.esc.api.shared.mandant.TranslatedString;

import org.apache.commons.lang3.StringUtils;
import org.apache.tika.mime.MediaType;
import org.checkerframework.checker.nullness.qual.NonNull;
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
        LocalDate today = this.appClock.getToday();
        FileTyp fileTyp = FileTyp.IMPFAUSWEIS;
        FileInfo fileInfo = FileInfo.of(CleanFileName.parse("File.pdf"), MediaType.TEXT_PLAIN, 92378L);
        List<FileData> files = List.of(
            new FileData(
                FileTyp.IMPFAUSWEIS,
                new CleanFileName("12-03-2025_Max-Mustermann_Impfausweis.pdf"),
                fileInfo).setTimestampErstellt(
                OffsetDateTime.now(this.clockProvider.getClock()).minusMinutes(2)),
            new FileData(
                FileTyp.IMPFAUSWEIS,
                new CleanFileName("12-03-2025_Max-Mustermann_Impfausweis_2.pdf"),
                fileInfo).setTimestampErstellt(
                OffsetDateTime.now(this.clockProvider.getClock()).minusMinutes(1)),
            new FileData(
                FileTyp.IMPFAUSWEIS,
                new CleanFileName("12-03-2025_Max-Mustermann_Impfausweis_3.pdf"),
                fileInfo).setTimestampErstellt(
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


    @ParameterizedTest
    @CsvSource({
        "Mustermann Max, 11-04-2025_Mustermann-Max_Impfausweis_2.pdf",
        "Tester Theophylus\tTab, 11-04-2025_Tester-TheophylusTab_Impfausweis_2.pdf"
    })
    void testGenerateFileWithCorrectNameForImpfausweis(@NonNull String formattedName, @NonNull String expectedCalculatedName) {
        // ARRANGE
        LocalDate today = this.appClock.getToday();

        FileTyp fileTyp = FileTyp.IMPFAUSWEIS;
        FileInfo fileInfo = FileInfo.of(CleanFileName.parse("File.pdf"), MediaType.TEXT_PLAIN, 92378L);
        List<FileData> files = List.of(
            new FileData(
                fileTyp,
                new CleanFileName("12-03-2025_Mustermann-Max_Impfausweis.pdf"),
                fileInfo).setTimestampErstellt(
                OffsetDateTime.now(this.clockProvider.getClock())));


        // ACT
        String originalFileName = fileInfo.getFileName().getFileName();
        CleanFileName calculatedFileName = FileNameUtil.calculateAutomaticFilename(
            files,
            today,
            formattedName,
            fileTyp,
            originalFileName);

        FileData result = new FileData(fileTyp, calculatedFileName, fileInfo);
        // ASSERT
        assertThat(result.getFileTyp()).isEqualTo(fileTyp);
        assertThat(result.getFileInfoBucket().getFileName().getFileName()).isEqualTo("File.pdf");


        assertThat(result.getCalculatedFileName()).hasToString(expectedCalculatedName);
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

    @Test
    void basicFolderName_neutralizesPathTraversal() {
        OffsetDateTime today = LocalDate.of(2025, 05, 06).atStartOfDay(ZONE_ID).toOffsetDateTime();

        assertThat(FileNameUtil.basicFolderName(today, "x/../../../tmp/evil", 0))
            .isEqualTo("06-05-2025_x----tmp-evil");
        assertThat(FileNameUtil.basicFolderName(today, "Notiz\\Test", 0))
            .isEqualTo("06-05-2025_Notiz-Test");
        assertThat(FileNameUtil.basicFolderName(today, "..", 0))
            .isEqualTo("06-05-2025_");
        assertThat(FileNameUtil.basicFolderName(today, "../../etc/passwd", 0))
            .isEqualTo("06-05-2025_--etc-passwd");
        assertThat(FileNameUtil.basicFolderName(today, "Schuluntersuchung 2024/2025", 0))
            .isEqualTo("06-05-2025_Schuluntersuchung-2024-2025");
    }

    /**
     * Verifies {@link FileNameUtil#uniqueEntryName(Set, String)}.
     * <p>
     * The {@code alreadyUsed} column holds the entry names that are already present in the set
     * (pipe-separated, empty means the set starts out empty). {@code fileName} is the name we want
     * to add and {@code expected} is the unique name that must be returned.
     */
    @ParameterizedTest
    @CsvSource(delimiter = ';', value = {
        // empty set -> name is returned unchanged
        "; report.pdf; report.pdf",
        // single collision -> counter inserted before the extension
        "report.pdf; report.pdf; report_1.pdf",
        // two collisions -> counter keeps counting up
        "report.pdf|report_1.pdf; report.pdf; report_2.pdf",
        // collision on a name without extension -> counter appended at the end
        "report; report; report_1",
        // dotfile (leading dot, no real extension) -> counter appended at the end
        ".gitignore; .gitignore; .gitignore_1",
        // multiple dots -> counter inserted before the last dot
        "archive.tar.gz; archive.tar.gz; archive.tar_1.gz",
        // no collision even though other names exist -> returned unchanged
        "a.pdf|b.pdf; c.pdf; c.pdf",
        // zip path -> only the file segment gets the counter, the folder must stay untouched
        "Anhaenge/notiz/scan.pdf; Anhaenge/notiz/scan.pdf; Anhaenge/notiz/scan_1.pdf",
        // dot in a folder name, no extension on the file -> counter appended, folder untouched
        "Anhaenge/v1.2/scan; Anhaenge/v1.2/scan; Anhaenge/v1.2/scan_1",
        // same file name in two folders is not a collision at all
        "Anhaenge/a/scan.pdf; Anhaenge/b/scan.pdf; Anhaenge/b/scan.pdf"
    })
    void testUniqueEntryName(String alreadyUsed, String fileName, String expected) {
        // ARRANGE
        Set<String> usedEntryNames = new LinkedHashSet<>();
        if (!StringUtils.isEmpty(alreadyUsed)) {
            for (String used : alreadyUsed.split("\\|")) {
                usedEntryNames.add(used.trim());
            }
        }

        // ACT
        String result = FileNameUtil.uniqueEntryName(usedEntryNames, fileName.trim());

        // ASSERT
        assertThat(result).isEqualTo(expected.trim());
        // the returned name must always end up registered in the set
        assertThat(usedEntryNames).contains(expected.trim());
    }

    @Test
    void uniqueEntryName_generatesDistinctNamesForRepeatedCalls() {
        // ARRANGE
        Set<String> usedEntryNames = new LinkedHashSet<>();

        // ACT
        String first = FileNameUtil.uniqueEntryName(usedEntryNames, "report.pdf");
        String second = FileNameUtil.uniqueEntryName(usedEntryNames, "report.pdf");
        String third = FileNameUtil.uniqueEntryName(usedEntryNames, "report.pdf");

        // ASSERT
        assertThat(first).isEqualTo("report.pdf");
        assertThat(second).isEqualTo("report_1.pdf");
        assertThat(third).isEqualTo("report_2.pdf");
        assertThat(usedEntryNames).containsExactlyInAnyOrder("report.pdf", "report_1.pdf", "report_2.pdf");
    }
}
