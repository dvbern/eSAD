package ch.dvbern.oss.hemed.esc.api.shared.preview.generator;

import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.InputStream;

import ch.dvbern.oss.hemed.esc.api.shared.preview.PreviewGenerationException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class PdfPreviewGeneratorTest {

    private PdfPreviewGenerator generator;

    @BeforeEach
    void setUp() {
        generator = new PdfPreviewGenerator();
    }

    @Test
    void supports_pdf_mime_type() {
        assertThat(generator.supports("application/pdf")).isTrue();
    }

    @Test
    void does_not_support_other_mime_types() {
        assertThat(generator.supports("image/png")).isFalse();
        assertThat(generator.supports("text/plain")).isFalse();
        assertThat(generator.supports("application/vnd.openxmlformats-officedocument.wordprocessingml.document"))
            .isFalse();
    }

    @Test
    void generates_preview_from_valid_pdf() throws Exception {
        try (InputStream pdfStream = getClass().getResourceAsStream("/FileUtilTest/pdfFileExample.pdf")) {
            assertThat(pdfStream).isNotNull();

            BufferedImage image = generator.generatePreview(pdfStream);

            assertThat(image).isNotNull();
            assertThat(image.getWidth()).isGreaterThan(0);
            assertThat(image.getHeight()).isGreaterThan(0);
        }
    }

    @Test
    void throws_exception_for_corrupted_pdf() {
        byte[] corruptedData = "this is not a valid pdf".getBytes();
        InputStream corruptedStream = new ByteArrayInputStream(corruptedData);

        assertThatThrownBy(() -> generator.generatePreview(corruptedStream))
            .isInstanceOf(PreviewGenerationException.class);
    }
}
