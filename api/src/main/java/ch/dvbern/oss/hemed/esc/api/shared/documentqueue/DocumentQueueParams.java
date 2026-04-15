package ch.dvbern.oss.hemed.esc.api.shared.documentqueue;

import ch.dvbern.oss.hemed.esc.api.features.spss.SpssReportType;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.checkerframework.checker.nullness.qual.Nullable;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class DocumentQueueParams {
    @Nullable
    SpssReportType spssReportType = null;
}
