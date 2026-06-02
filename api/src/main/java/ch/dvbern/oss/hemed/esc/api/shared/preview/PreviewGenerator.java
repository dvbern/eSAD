package ch.dvbern.oss.hemed.esc.api.shared.preview;

import java.awt.image.BufferedImage;
import java.io.InputStream;

import org.checkerframework.checker.nullness.qual.NonNull;

public interface PreviewGenerator {

    /**
     * Can this generator handle the given mimetype?
     * @param mimeType mimetype to check for
     * @return true if handled
     */
    boolean supports(@NonNull String mimeType);

    /**
     * Generate a preview image from the given input stream.
     *
     * @param inputStream the input stream to read the data to convert
     * @return Buffered Image containing the preview
     */
    @NonNull
    BufferedImage generatePreview(@NonNull InputStream inputStream);
}
