package ch.dvbern.dvbstarter.infrastructure.fileio;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.Set;

import ch.dvbern.dvbstarter.shared.types.cleanfilename.CleanFileName;
import ch.dvbern.oss.hemed.esc.api.features.labels.Label;
import ch.dvbern.oss.hemed.esc.api.shared.fileio.FileData;
import ch.dvbern.oss.hemed.esc.api.shared.fileio.FileTyp;

import lombok.experimental.UtilityClass;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.StringUtils;
import org.checkerframework.checker.nullness.qual.NonNull;

@UtilityClass
public class FileNameUtil {

    private static final char SEPARATOR = '-';
    private static final String UNDERLINE = "_";
    public static final DateTimeFormatter
        DATE_FORMATTER_FOR_FILENAME = DateTimeFormatter.ofPattern("dd-MM-yyyy", Locale.of("de", "CH"));

    /**
     * Includes the time: without it two  downloads on the same day land under the same name in the download folder.
     */
    public static final DateTimeFormatter DOWNLOAD_FILENAME_TIMESTAMP_FORMATTER =
        DateTimeFormatter.ofPattern("dd-MM-yyyy_HH-mm-ss", Locale.of("de", "CH"));

    /**
     * Generates a file with a standardized name based on various parameters.
     * The format of the calculated filename will be something like
     * * [Datum] _ [Kindname] _ [FileTyp].txt
     * * [Datum] _ [Kindname] _ [FileTyp]_2.txt
     * * e.g 05-03-2025_Musterkind-Max_Impfausweis.pdf
     */
    @NonNull
    public static CleanFileName calculateAutomaticFilename(
        @NonNull List<FileData> existingFiles,
        @NonNull LocalDate today,
        @NonNull String baseFilename,
        @NonNull FileTyp fileTyp,
        @NonNull String originalFileNameToGetExtension
    ) {
        final String fileEnding = '.' + FilenameUtils.getExtension(originalFileNameToGetExtension);

        String formattedDate = today.format(DATE_FORMATTER_FOR_FILENAME);
        String formattedName = baseFilename.replace(' ', SEPARATOR);

        // Build the base filename
        String baseFileName = formattedDate + UNDERLINE + formattedName;

        // Add prefix if needed
        if (fileTyp == FileTyp.IMPFAUSWEIS || fileTyp == FileTyp.FRAGEBOGEN || fileTyp == FileTyp.ANMELDUNG_BESTAETIGUNG || fileTyp == FileTyp.IMPFEINVERSTAENDNIS) {
            baseFileName += UNDERLINE + StringUtils.capitalize(fileTyp.getPrefix());
        }

        int versionNumber = getNextNumberForFile(today, fileTyp, existingFiles, formattedName);

        // Build the full filename
        String calculatedFileName;
        if (versionNumber > 0) {
            calculatedFileName = baseFileName + UNDERLINE + versionNumber + fileEnding;
        } else {
            calculatedFileName = baseFileName + fileEnding;
        }

        return new CleanFileName(calculatedFileName);
    }

    /**
     * Generates a folder with a standardized name based on various parameters.
     * The format of the calculated filename will be something like
     * * [Datum] _ [name]
     * * [Datum] _ [name] _ [laufnummer]
     * * e.g 05-03-2025_notiz-bezüglich-etwas_2
     * The name gets truncated to *50* symbols.
     */
    @NonNull
    public static String basicFolderName(
        @NonNull OffsetDateTime date,
        @NonNull String name,
        int laufnummer
    ) {
        String formattedDate = date.format(DATE_FORMATTER_FOR_FILENAME);
        String sanitized = name.replace(' ', SEPARATOR)
            .replace('/', SEPARATOR)
            .replace('\\', SEPARATOR)
            .replace("..", "");
        String formattedName = sanitized.substring(0, Math.min(sanitized.length(), 50));

        if (laufnummer > 1) {
            return formattedDate + UNDERLINE + formattedName + UNDERLINE + laufnummer;
        }
        return formattedDate + UNDERLINE + formattedName;
    }

    /**
     * Determines the next version number for a file.
     */
    public static int getNextNumberForFile(
        @NonNull LocalDate today,
        @NonNull FileTyp fileTyp,
        @NonNull List<FileData> files,
        @NonNull String formattedName
    ) {
        if (files.isEmpty()) {
            return 0;
        }

        // Filter files by type and date, sort by timestamp
        Optional<FileData> latestFile = files.stream()
            .filter(file -> file.getFileTyp() == fileTyp)
            .filter(file -> file.getTimestampErstellt().toLocalDate().isEqual(today))
            .max(Comparator.comparing(FileData::getTimestampErstellt));

        if (latestFile.isEmpty()) {
            return 0;
        }

        String fileName = latestFile.get().getCalculatedFileName().toString();
        List<String> seperatedFileNameParts = Arrays.stream(fileName.split(UNDERLINE)).toList();
        String possibleNumber = Arrays.stream(seperatedFileNameParts.getLast().split("\\.")).toList().getFirst();

        // das erste File hat keine Nummer, daher hier pruefen, ob das letzte file bereits nummeriert war
        // wenn nicht, ist dies das zweite file
        if (possibleNumber.equalsIgnoreCase(fileTyp.getPrefix())
            || possibleNumber.equalsIgnoreCase(formattedName)) {
            return 2;
        }
        return Integer.parseInt(possibleNumber) + 1;
    }

    public static FileTyp getTypFromLabel(Set<Label> labels, FileTyp defaultTyp) {
        boolean isImpfausweis = labels.stream().map(Label::getId).anyMatch(Label.ID_LABEL_IMPFAUSWEIS::equals);
        boolean isFragebogen = labels.stream().map(Label::getId).anyMatch(Label.ID_LABEL_FRAGEBOGEN::equals);
        boolean isAnmeldungBestaetigung = labels.stream().map(Label::getId).anyMatch(Label.ID_LABEL_ANMELDUNG_BESTAETIGUNG::equals);
        boolean isImpfeinverstaendnis = labels.stream().map(Label::getId).anyMatch(Label.ID_LABEL_IMPFEINVERSTAENDNIS::equals);
        if (isImpfausweis) {
            return FileTyp.IMPFAUSWEIS;
        }
        if (isFragebogen) {
            return FileTyp.FRAGEBOGEN;
        }
        if (isAnmeldungBestaetigung) {
            return FileTyp.ANMELDUNG_BESTAETIGUNG;
        }
        if (isImpfeinverstaendnis) {
            return FileTyp.IMPFEINVERSTAENDNIS;
        }
        return defaultTyp;
    }

    /**
     * Makes {@code entryName} unique within {@code usedEntryNames} by inserting a counter before the file extension,
     * registering the result as used.
     * <p>
     * {@code entryName} may be a zip path such as {@code Anhaenge/05-03-2025_Notiz/scan.pdf}. Only the segment after
     * the last {@code /} is ever modified - a counter spliced into a folder name would move the entry into a
     * different folder.
     */
    @NonNull
    public static String uniqueEntryName(@NonNull Set<String> usedEntryNames, @NonNull String entryName) {
        int slashIndex = entryName.lastIndexOf('/');
        int dotIndex = entryName.lastIndexOf('.');
        // the dot has to sit inside the last segment, and not be its first character (dotfiles have no extension)
        boolean hasExtension = dotIndex > slashIndex + 1;

        String candidate = entryName;
        int counter = 1;
        while (!usedEntryNames.add(candidate)) {
            candidate = hasExtension
                ? entryName.substring(0, dotIndex) + '_' + counter + entryName.substring(dotIndex)
                : entryName + '_' + counter;
            counter++;
        }
        return candidate;
    }
}
