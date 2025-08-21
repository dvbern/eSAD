package ch.dvbern.oss.hemed.esc.api.shared.printer;

import com.itextpdf.kernel.events.Event;
import com.itextpdf.kernel.events.IEventHandler;
import com.itextpdf.kernel.events.PdfDocumentEvent;
import com.itextpdf.kernel.geom.Rectangle;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfPage;
import com.itextpdf.layout.Canvas;
import com.itextpdf.layout.element.Paragraph;
import com.itextpdf.layout.properties.TextAlignment;
import org.checkerframework.checker.nullness.qual.NonNull;

/**
 * Should you run into a OutOfMemoryException because the page number is too large, check out
 * <a href="https://kb.itextpdf.com/itext/chapter-7-handling-events-setting-viewer-preferenc#Chapter7:Handlingevents;settingviewerpreferencesandprinterproperties-Solvingthe%22PageXofY%22problem">this</a>
 */
public class FooterWithPageCounterHandler implements IEventHandler {
    private static final int MARGIN_X = 35;
    private static final int MARGIN_Y = 18;

    private final Paragraph leftSide;

    public FooterWithPageCounterHandler(@NonNull Paragraph paragraph) {
        this.leftSide = paragraph;
    }

    @Override
    public void handleEvent(Event event) {
        PdfDocumentEvent docEvent = (PdfDocumentEvent) event;
        PdfDocument pdf = docEvent.getDocument();
        PdfPage page = docEvent.getPage();
        int pageNumber = pdf.getPageNumber(page);
        Rectangle pageSize = page.getPageSize();

        try (Canvas canvas = new Canvas(page, pageSize)) {
            canvas.setFont(BasePrinter.getFont());
            canvas.setFontSize(8.0f);
            Paragraph p = new Paragraph()
                .add(String.valueOf(pageNumber))
                .add("/")
                .add(String.valueOf(pdf.getNumberOfPages()));
            canvas.showTextAligned(p, pageSize.getWidth() - MARGIN_X, MARGIN_Y, TextAlignment.RIGHT);

            canvas.showTextAligned(leftSide, MARGIN_X, MARGIN_Y, TextAlignment.LEFT);
        }
    }
}
