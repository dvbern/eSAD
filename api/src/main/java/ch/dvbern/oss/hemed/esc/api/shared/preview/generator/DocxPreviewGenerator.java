package ch.dvbern.oss.hemed.esc.api.shared.preview.generator;

import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;

import ch.dvbern.oss.hemed.esc.api.shared.preview.PreviewGenerationException;
import ch.dvbern.oss.hemed.esc.api.shared.preview.PreviewGenerator;
import ch.dvbern.oss.hemed.esc.api.shared.preview.PreviewService;
import com.google.common.net.MediaType;
import jakarta.enterprise.context.ApplicationScoped;
import lombok.RequiredArgsConstructor;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.docx4j.Docx4J;
import org.docx4j.openpackaging.packages.WordprocessingMLPackage;

@ApplicationScoped
@RequiredArgsConstructor
public class DocxPreviewGenerator implements PreviewGenerator {

    private static final String MIME_TYPE = MediaType.OOXML_DOCUMENT.toString(); // vnd.openxmlformats-officedocument.wordprocessingml.document
    private static final String PDF_MIME_TYPE = MediaType.PDF.toString();

    private final PreviewService previewService;

    @Override
    public boolean supports(@NonNull String mimeType) {
        return MIME_TYPE.equalsIgnoreCase(mimeType);
    }

    @Override
    @NonNull
    public BufferedImage generatePreview(@NonNull InputStream inputStream) {
        try {
            WordprocessingMLPackage wordMLPackage = WordprocessingMLPackage.load(inputStream);
            ByteArrayOutputStream pdfOutputStream = new ByteArrayOutputStream();
            Docx4J.toPDF(wordMLPackage, pdfOutputStream);
            byte[] pdfBytes = pdfOutputStream.toByteArray();

            // Delegate to PreviewService which routes to PdfPreviewGenerator (renders page 0 only)
            return previewService.generatePreviewImage(pdfBytes, PDF_MIME_TYPE);
        } catch (PreviewGenerationException e) {
            throw e;
        } catch (Exception e) {
            throw new PreviewGenerationException("Failed to generate preview from DOCX", e);
        }
    }
}
