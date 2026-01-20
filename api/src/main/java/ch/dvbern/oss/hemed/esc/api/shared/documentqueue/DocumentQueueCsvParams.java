package ch.dvbern.oss.hemed.esc.api.shared.documentqueue;

import ch.dvbern.oss.hemed.esc.api.features.spss.CsvReportType;
import lombok.AllArgsConstructor;
import lombok.Value;
import org.checkerframework.checker.nullness.qual.Nullable;

@Value
@AllArgsConstructor
public class DocumentQueueCsvParams {
    @Nullable
    CsvReportType csvReportType = null;
}
