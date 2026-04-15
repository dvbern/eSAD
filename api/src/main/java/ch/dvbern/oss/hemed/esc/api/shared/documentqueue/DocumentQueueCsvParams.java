package ch.dvbern.oss.hemed.esc.api.shared.documentqueue;

import ch.dvbern.oss.hemed.esc.api.features.spss.CsvReportType;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.checkerframework.checker.nullness.qual.Nullable;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class DocumentQueueCsvParams {
    @Nullable
    CsvReportType csvReportType = null;
}
