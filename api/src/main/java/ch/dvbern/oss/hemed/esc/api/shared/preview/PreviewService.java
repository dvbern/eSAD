package ch.dvbern.oss.hemed.esc.api.shared.preview;

import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

import ch.dvbern.dvbstarter.infrastructure.errorhandling.AppFailureException;
import ch.dvbern.dvbstarter.infrastructure.errorhandling.AppFailureMessage;
import ch.dvbern.dvbstarter.infrastructure.stereotypes.NoTransactionService;
import ch.dvbern.dvbstarter.infrastructure.stereotypes.Service;
import jakarta.enterprise.inject.Any;
import jakarta.enterprise.inject.Instance;
import lombok.extern.slf4j.Slf4j;
import org.checkerframework.checker.nullness.qual.NonNull;

import javax.imageio.ImageIO;

@NoTransactionService
@Slf4j
public class PreviewService {

    private static final String OUTPUT_MIME_TYPE = "image/png";
    private static final String OUTPUT_FORMAT = "png";

    // this injects all the Implementations for the different mime types
    private final Instance<PreviewGenerator> generators;

    public PreviewService(@Any Instance<PreviewGenerator> generators) {
        this.generators = generators;
    }

    @NonNull
    public PreviewResult generatePreview(@NonNull InputStream inputStream, @NonNull String mimeType) {
        PreviewGenerator generator = findGenerator(mimeType);
        BufferedImage image = generator.generatePreview(inputStream);
        byte[] imageData = convertToBytes(image);

        return new PreviewResult(imageData, OUTPUT_MIME_TYPE, image.getWidth(), image.getHeight());
    }

    /**
     * Generate a preview image without converting to PNG bytes.
     * Useful for generators that need to delegate to other generators.
     */
    @NonNull
    public BufferedImage generatePreviewImage(@NonNull byte[] data, @NonNull String mimeType) {
        PreviewGenerator generator = findGenerator(mimeType);
        return generator.generatePreview(new ByteArrayInputStream(data));
    }

    public boolean isSupported(@NonNull String mimeType) {
        for (PreviewGenerator generator : generators) {
            if (generator.supports(mimeType)) {
                return true;
            }
        }
        return false;
    }

    @NonNull
    private PreviewGenerator findGenerator(@NonNull String mimeType) {
        for (PreviewGenerator generator : generators) {
            if (generator.supports(mimeType)) {
                return generator;
            }
        }
        throw new AppFailureException(AppFailureMessage.previewUnsupportedMimeType(mimeType));
    }

    private byte[] convertToBytes(@NonNull BufferedImage image) {
        try {
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            ImageIO.write(image, OUTPUT_FORMAT, outputStream);
            return outputStream.toByteArray();
        } catch (IOException e) {
            throw new PreviewGenerationException("Failed to convert preview image to PNG bytes", e);
        }
    }
}
