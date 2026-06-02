package ch.dvbern.oss.hemed.esc.api.shared.preview.generator;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;
import java.util.Set;

import ch.dvbern.oss.hemed.esc.api.shared.preview.PreviewGenerationException;
import ch.dvbern.oss.hemed.esc.api.shared.preview.PreviewGenerator;
import com.google.common.net.MediaType;
import jakarta.enterprise.context.ApplicationScoped;
import org.checkerframework.checker.nullness.qual.NonNull;

import javax.imageio.ImageIO;

@ApplicationScoped
public class ImagePreviewGenerator implements PreviewGenerator {

    private static final Set<String> SUPPORTED_MIME_TYPES = Set.of(
        MediaType.PNG.toString(), // image/png
        MediaType.JPEG.toString() // image/jpeg
    );

    private static final int MAX_WIDTH = 800;

    @Override
    public boolean supports(@NonNull String mimeType) {
        return SUPPORTED_MIME_TYPES.contains(mimeType.toLowerCase());
    }

    @Override
    @NonNull
    public BufferedImage generatePreview(@NonNull InputStream inputStream) {
        try {
            BufferedImage image = ImageIO.read(inputStream);
            if (image == null) {
                throw new PreviewGenerationException("Failed to read image: unsupported or corrupted image data");
            }
            if (image.getWidth() > MAX_WIDTH) {
                return scaleImage(image);
            }
            return image;
        } catch (IOException e) {
            throw new PreviewGenerationException("Failed to generate preview from image", e);
        }
    }

    @NonNull
    private BufferedImage scaleImage(@NonNull BufferedImage original) {
        double scaleFactor = (double) MAX_WIDTH / original.getWidth();
        int newHeight = (int) (original.getHeight() * scaleFactor);

        BufferedImage scaled = new BufferedImage(MAX_WIDTH, newHeight, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2d = scaled.createGraphics();
        try {
            g2d.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
            g2d.drawImage(original, 0, 0, MAX_WIDTH, newHeight, null);
        } finally {
            g2d.dispose();
        }
        return scaled;
    }
}
