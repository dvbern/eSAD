package ch.dvbern.oss.hemed.esc.api.shared.auth.berechtigugnsmatrix;

import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Stream;

import ch.dvbern.oss.hemed.esc.api.shared.auth.FachRolle;
import ch.dvbern.oss.hemed.esc.api.shared.auth.Berechtigung;
import com.google.common.collect.Streams;
import lombok.experimental.UtilityClass;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;
import org.apache.commons.csv.CSVRecord;
import org.apache.commons.io.ByteOrderMark;
import org.apache.commons.io.input.BOMInputStream;
import org.checkerframework.checker.nullness.qual.NonNull;

/**
 * This is a helper class for reading and writing the BerechtigungsMatrix to a CSV file. This is useful to check
 * if the FachRolle Enum has the correct Berechtigungen.
 */
@UtilityClass
public class BerechtigungsMatrixCSVHelper {

    public static CSVFormat getCsvFormat() {
        String[] colNames = getHeaderStrings();

        return CSVFormat.EXCEL.builder()
            .setHeader(colNames)
            .setDelimiter(';')
            .get();

    }

    public static String @NonNull [] getHeaderStrings() {
        String[] colNames = Streams.concat(
                Stream.of("Berechtigung"),
                Arrays.stream(FachRolle.values()).map(FachRolle::name)
            )
            .toArray(String[]::new);
        return colNames;
    }

    public static void writeBerechtigungenToCSV(Path path) throws IOException {
        try (FileWriter fileWriter = new FileWriter(path.toFile(), StandardCharsets.UTF_8);
            CSVPrinter csvPrinter = new CSVPrinter(fileWriter, getCsvFormat())) {
            for (Berechtigung berechtigung : Berechtigung.values()) {
                List<String> record = new ArrayList<>();
                record.add(berechtigung.name());
                for (FachRolle fachRolle : FachRolle.values()) {
                    record.add(fachRolle.hasBerechtigung(berechtigung) ? "x" : "");
                }
                csvPrinter.printRecord(record);
            }
            fileWriter.flush();

            System.out.println("printed Berechtigungen to " + path.toAbsolutePath());
        }
    }

    public static List<CSVRecord> readCSVRecords(Path path) throws IOException {
        BOMInputStream is = BOMInputStream.builder()
            .setPath(path)
            .setByteOrderMarks(ByteOrderMark.UTF_8)
            .setInclude(false)
            .get();

        try (InputStreamReader reader = new InputStreamReader(is, StandardCharsets.UTF_8)) {
            return getCsvFormat()
                .parse(reader)
                .getRecords();
        }
    }
}
