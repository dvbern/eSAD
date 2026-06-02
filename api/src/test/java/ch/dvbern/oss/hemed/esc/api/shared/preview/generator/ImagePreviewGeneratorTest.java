package ch.dvbern.oss.hemed.esc.api.shared.preview.generator;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

import ch.dvbern.oss.hemed.esc.api.shared.preview.PreviewGenerationException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import javax.imageio.ImageIO;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ImagePreviewGeneratorTest {

    private ImagePreviewGenerator generator;

    @BeforeEach
    void setUp() {
        generator = new ImagePreviewGenerator();
    }

    @Test
    void supports_png_mime_type() {
        assertThat(generator.supports("image/png")).isTrue();
    }

    @Test
    void supports_jpeg_mime_type() {
        assertThat(generator.supports("image/jpeg")).isTrue();
    }

    @Test
    void does_not_support_non_standard_jpg_mime_type() {
        assertThat(generator.supports("image/jpg")).isFalse();
    }

    @Test
    void does_not_support_other_mime_types() {
        assertThat(generator.supports("application/pdf")).isFalse();
        assertThat(generator.supports("text/plain")).isFalse();
    }

    @Test
    void generates_preview_from_png() throws Exception {
        byte[] pngBytes = createTestImage(100, 80, "png");
        InputStream inputStream = new ByteArrayInputStream(pngBytes);

        BufferedImage image = generator.generatePreview(inputStream);

        assertThat(image).isNotNull();
        assertThat(image.getWidth()).isEqualTo(100);
        assertThat(image.getHeight()).isEqualTo(80);
    }

    @Test
    void generates_preview_from_jpeg() throws Exception {
        byte[] jpegBytes = createTestImage(200, 150, "jpg");
        InputStream inputStream = new ByteArrayInputStream(jpegBytes);

        BufferedImage image = generator.generatePreview(inputStream);

        assertThat(image).isNotNull();
        assertThat(image.getWidth()).isEqualTo(200);
        assertThat(image.getHeight()).isEqualTo(150);
    }

    @Test
    void scales_down_large_images() throws Exception {
        byte[] largeImageBytes = createTestImage(2400, 1600, "png");
        InputStream inputStream = new ByteArrayInputStream(largeImageBytes);

        BufferedImage image = generator.generatePreview(inputStream);

        assertThat(image).isNotNull();
        assertThat(image.getWidth()).isEqualTo(800);
        assertThat(image.getHeight()).isEqualTo(533);
    }

    @Test
    void does_not_scale_image_within_max_width() throws Exception {
        byte[] imageBytes = createTestImage(800, 600, "png");
        InputStream inputStream = new ByteArrayInputStream(imageBytes);

        BufferedImage image = generator.generatePreview(inputStream);

        assertThat(image).isNotNull();
        assertThat(image.getWidth()).isEqualTo(800);
        assertThat(image.getHeight()).isEqualTo(600);
    }

    @Test
    void throws_exception_for_corrupted_image() {
        byte[] corruptedData = "this is not an image".getBytes(StandardCharsets.UTF_8);
        InputStream corruptedStream = new ByteArrayInputStream(corruptedData);

        assertThatThrownBy(() -> generator.generatePreview(corruptedStream))
            .isInstanceOf(PreviewGenerationException.class);
    }

    private byte[] createTestImage(int width, int height, String format) throws Exception {
        BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        Graphics2D g2d = image.createGraphics();
        g2d.setColor(Color.WHITE);
        g2d.fillRect(0, 0, width, height);
        g2d.setColor(Color.BLACK);
        g2d.drawString("Test", 10, 20);
        g2d.dispose();

        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        ImageIO.write(image, format, outputStream);
        return outputStream.toByteArray();
    }
}
