package ch.dvbern.oss.hemed.esc.api.shared.preview.generator;

import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.InputStream;

import ch.dvbern.oss.hemed.esc.api.shared.preview.PreviewGenerationException;
import ch.dvbern.oss.hemed.esc.api.shared.preview.PreviewService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class DocXPreviewGeneratorTest {

    private DocxPreviewGenerator generator;

    private PreviewService previewService = Mockito.mock(PreviewService.class);


    @BeforeEach
    void setUp() {
        previewService = Mockito.mock(PreviewService.class);

        // Mock: whenever called, return a dummy image
        Mockito.when(previewService.generatePreviewImage(
            Mockito.any(byte[].class),
            Mockito.eq("application/pdf")
        )).thenReturn(new BufferedImage(100, 100, BufferedImage.TYPE_INT_RGB));

        generator = new DocxPreviewGenerator(previewService);
    }


    @Test
    void supports_docx_mime_type() {

        assertThat(generator.supports("application/vnd.openxmlformats-officedocument.wordprocessingml.document"))
            .isTrue();

    }

    @Test
    void does_not_support_other_mime_types() {
        assertThat(generator.supports("image/png")).isFalse();
        assertThat(generator.supports("text/plain")).isFalse();
        assertThat(generator.supports("application/pdf")).isFalse();
    }

    @Test
    void generates_preview_from_valid_pdf() throws Exception {
        try (InputStream pdfStream = getClass().getResourceAsStream("/FileUtilTest/test-docx.docx")) {
            assertThat(pdfStream).isNotNull();

            BufferedImage image = generator.generatePreview(pdfStream);

            assertThat(image).isNotNull();
            assertThat(image.getWidth()).isGreaterThan(0);
            assertThat(image.getHeight()).isGreaterThan(0);
        }
    }

    @Test
    void throws_exception_for_corrupted_pdf() {
        byte[] corruptedData = "this is not a valid docx".getBytes();
        InputStream corruptedStream = new ByteArrayInputStream(corruptedData);

        assertThatThrownBy(() -> generator.generatePreview(corruptedStream))
            .isInstanceOf(PreviewGenerationException.class);
    }
}
