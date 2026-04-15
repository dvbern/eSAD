package ch.dvbern.oss.hemed.esc.api.features.spss;

import java.util.Arrays;
import java.util.List;
import java.util.function.Function;

import lombok.experimental.UtilityClass;
import org.checkerframework.checker.nullness.qual.NonNull;

@UtilityClass
public class CsvReportUtil {

    public static <T extends Enum<T>, C> void addEnumColumnsToReportList(
        @NonNull List<C> list,
        @NonNull Class<T> enumClass,
        @NonNull Function<T, C> columnCreator
    ) {
        Arrays.stream(enumClass.getEnumConstants()).map(columnCreator).forEach(list::add);
    }
}
