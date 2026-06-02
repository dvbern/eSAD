package ch.dvbern.oss.hemed.esc.api.shared.preview.generator;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;

import ch.dvbern.oss.hemed.esc.api.shared.preview.PreviewGenerationException;
import ch.dvbern.oss.hemed.esc.api.shared.preview.PreviewGenerator;
import com.google.common.net.MediaType;
import jakarta.enterprise.context.ApplicationScoped;
import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.rendering.PDFRenderer;
import org.checkerframework.checker.nullness.qual.NonNull;

@ApplicationScoped
public class PdfPreviewGenerator implements PreviewGenerator {

    private static final String MIME_TYPE = MediaType.PDF.toString();
    private static final float DPI = 100;

    @Override
    public boolean supports(@NonNull String mimeType) {
        return MIME_TYPE.equalsIgnoreCase(mimeType);
    }

    @Override
    @NonNull
    public BufferedImage generatePreview(@NonNull InputStream inputStream) {
        try (PDDocument document = Loader.loadPDF(inputStream.readAllBytes())) {
            if (document.getNumberOfPages() == 0) {
                throw new PreviewGenerationException("PDF document has no pages");
            }
            PDFRenderer renderer = new PDFRenderer(document);
            return renderer.renderImageWithDPI(0, DPI);
        } catch (IOException e) {
            throw new PreviewGenerationException("Failed to generate preview from PDF", e);
        }
    }
}
