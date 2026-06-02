package ch.dvbern.oss.hemed.esc.api.shared.preview;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.util.List;

import ch.dvbern.dvbstarter.infrastructure.errorhandling.AppFailureException;
import ch.dvbern.oss.hemed.esc.api.shared.preview.generator.ImagePreviewGenerator;
import ch.dvbern.oss.hemed.esc.api.shared.preview.generator.PdfPreviewGenerator;
import jakarta.enterprise.inject.Instance;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import javax.imageio.ImageIO;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.lenient;

@ExtendWith(MockitoExtension.class)
class PreviewServiceTest {

    private static final List<PreviewGenerator> GENERATORS = List.of(
        new PdfPreviewGenerator(),
        new ImagePreviewGenerator()
    );

    @Mock
    private Instance<PreviewGenerator> generatorInstance;

    private PreviewService previewService;

    @BeforeEach
    void setUp() {
        lenient().when(generatorInstance.iterator()).thenAnswer(invocation -> GENERATORS.iterator());
        previewService = new PreviewService(generatorInstance);
    }

    @Test
    void generates_preview_for_supported_image() throws Exception {
        byte[] pngBytes = createTestImage(100, 80);
        InputStream pngStream = new ByteArrayInputStream(pngBytes);

        PreviewResult result = previewService.generatePreview(pngStream, "image/png");

        assertThat(result).isNotNull();
        assertThat(result.getMimeType()).isEqualTo("image/png");
        assertThat(result.getImageData()).isNotEmpty();
        assertThat(result.getWidth()).isEqualTo(100);
        assertThat(result.getHeight()).isEqualTo(80);
    }

    @Test
    void generates_preview_for_pdf() throws Exception {
        try (InputStream pdfStream = getClass().getResourceAsStream("/FileUtilTest/pdfFileExample.pdf")) {
            assertThat(pdfStream).isNotNull();

            PreviewResult result = previewService.generatePreview(pdfStream, "application/pdf");

            assertThat(result).isNotNull();
            assertThat(result.getMimeType()).isEqualTo("image/png");
            assertThat(result.getImageData()).isNotEmpty();
            assertThat(result.getWidth()).isGreaterThan(0);
            assertThat(result.getHeight()).isGreaterThan(0);
        }
    }

    @Test
    void throws_exception_for_unsupported_mime_type() {
        InputStream inputStream = new ByteArrayInputStream("test".getBytes());

        assertThatThrownBy(() -> previewService.generatePreview(inputStream, "text/plain"))
            .isInstanceOf(AppFailureException.class);
    }

    @Test
    void is_supported_returns_true_for_supported_types() {
        assertThat(previewService.isSupported("application/pdf")).isTrue();
        assertThat(previewService.isSupported("image/png")).isTrue();
        assertThat(previewService.isSupported("image/jpeg")).isTrue();
    }

    @Test
    void is_supported_returns_false_for_unsupported_types() {
        assertThat(previewService.isSupported("text/plain")).isFalse();
        assertThat(previewService.isSupported("application/xml")).isFalse();
    }

    private byte[] createTestImage(int width, int height) throws Exception {
        BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        Graphics2D g2d = image.createGraphics();
        g2d.setColor(Color.WHITE);
        g2d.fillRect(0, 0, width, height);
        g2d.dispose();

        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        ImageIO.write(image, "png", outputStream);
        return outputStream.toByteArray();
    }
}
