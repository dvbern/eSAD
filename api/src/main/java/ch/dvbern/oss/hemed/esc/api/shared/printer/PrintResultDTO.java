package ch.dvbern.oss.hemed.esc.api.shared.printer;

import ch.dvbern.oss.hemed.esc.api.shared.fileio.FileInfo;
import lombok.AllArgsConstructor;
import lombok.Value;

@Value
@AllArgsConstructor
public class PrintResultDTO {
    byte[] content;
    FileInfo fileInfo;
}
