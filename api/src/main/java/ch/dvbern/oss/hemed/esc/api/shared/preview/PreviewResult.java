package ch.dvbern.oss.hemed.esc.api.shared.preview;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.checkerframework.checker.nullness.qual.NonNull;

@Getter
@RequiredArgsConstructor
public class PreviewResult {

    private final byte[] imageData;

    @NonNull
    private final String mimeType;

    private final int width;

    private final int height;
}
