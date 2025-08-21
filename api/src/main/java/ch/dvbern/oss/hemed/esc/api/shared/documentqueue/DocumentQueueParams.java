package ch.dvbern.oss.hemed.esc.api.shared.documentqueue;

import ch.dvbern.oss.hemed.esc.api.features.spss.SpssReportType;
import lombok.AllArgsConstructor;
import lombok.Value;
import org.checkerframework.checker.nullness.qual.Nullable;

@Value
@AllArgsConstructor
public class DocumentQueueParams {
    @Nullable
    SpssReportType spssReportType = null;
}
