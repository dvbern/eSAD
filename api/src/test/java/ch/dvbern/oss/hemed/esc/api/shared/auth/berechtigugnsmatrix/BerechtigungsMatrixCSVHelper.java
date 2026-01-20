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
import ch.dvbern.oss.hemed.esc.api.shared.auth.TenantEnum;
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
        return Streams.concat(
                Stream.of("Berechtigung"),
                Arrays.stream(FachRolle.values()).map(FachRolle::name)
            )
            .toArray(String[]::new);
    }

    public static void writeBerechtigungenToCSV(@NonNull Path path, @NonNull TenantEnum tenant) throws IOException {
        try (FileWriter fileWriter = new FileWriter(path.toFile(), StandardCharsets.UTF_8);
            CSVPrinter csvPrinter = new CSVPrinter(fileWriter, getCsvFormat())) {
            for (Berechtigung berechtigung : Berechtigung.values()) {
                List<String> csvRecord = new ArrayList<>();
                csvRecord.add(berechtigung.name());
                for (FachRolle fachRolle : FachRolle.values()) {
                    csvRecord.add(fachRolle.hasBerechtigung(berechtigung, tenant) ? "x" : "");
                }
                csvPrinter.printRecord(csvRecord);
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
