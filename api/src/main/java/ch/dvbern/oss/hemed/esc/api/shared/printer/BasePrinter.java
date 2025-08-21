package ch.dvbern.oss.hemed.esc.api.shared.printer;

import com.itextpdf.layout.properties.UnitValue;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.List;
import java.util.Objects;
import java.util.regex.Pattern;

import ch.dvbern.dvbstarter.infrastructure.errorhandling.AppFailureException;
import ch.dvbern.dvbstarter.infrastructure.errorhandling.AppFailureMessage;
import ch.dvbern.dvbstarter.shared.i18n.translations.AppLanguages;
import ch.dvbern.dvbstarter.shared.i18n.translations.TL;
import ch.dvbern.dvbstarter.shared.i18n.translations.TLProducer;
import ch.dvbern.dvbstarter.shared.types.emailaddress.EmailAddress;
import ch.dvbern.dvbstarter.shared.types.phonenumber.NormalizedPhoneNumber;
import ch.dvbern.dvbstarter.shared.types.sozialversicherungsnummer.SozialversicherungsNummer;
import ch.dvbern.oss.commons.i18nl10n.I18nMessage;
import ch.dvbern.oss.hemed.esc.api.shared.auth.TenantEnum;
import ch.dvbern.oss.hemed.esc.api.shared.fileio.FileData;
import ch.dvbern.oss.hemed.esc.api.shared.fileio.FileStoreService;
import ch.dvbern.oss.hemed.esc.api.shared.mandant.TranslatedString;
import ch.dvbern.oss.hemed.esc.api.shared.person.Adresse;
import com.itextpdf.io.font.PdfEncodings;
import com.itextpdf.io.image.ImageData;
import com.itextpdf.io.image.ImageDataFactory;
import com.itextpdf.kernel.events.PdfDocumentEvent;
import com.itextpdf.kernel.font.PdfFont;
import com.itextpdf.kernel.font.PdfFontFactory;
import com.itextpdf.kernel.font.PdfFontFactory.EmbeddingStrategy;
import com.itextpdf.kernel.geom.PageSize;
import com.itextpdf.kernel.pdf.PdfAConformanceLevel;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfDocumentInfo;
import com.itextpdf.kernel.pdf.PdfOutputIntent;
import com.itextpdf.kernel.pdf.PdfPage;
import com.itextpdf.kernel.pdf.PdfReader;
import com.itextpdf.kernel.pdf.PdfString;
import com.itextpdf.kernel.pdf.PdfVersion;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.kernel.pdf.WriterProperties;
import com.itextpdf.kernel.pdf.canvas.PdfCanvas;
import com.itextpdf.kernel.pdf.xobject.PdfFormXObject;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.borders.Border;
import com.itextpdf.layout.element.AreaBreak;
import com.itextpdf.layout.element.Cell;
import com.itextpdf.layout.element.Image;
import com.itextpdf.layout.element.ListItem;
import com.itextpdf.layout.element.Paragraph;
import com.itextpdf.layout.element.Table;
import com.itextpdf.layout.properties.HorizontalAlignment;
import com.itextpdf.layout.properties.TextAlignment;
import com.itextpdf.layout.properties.VerticalAlignment;
import com.itextpdf.pdfa.PdfADocument;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.tika.mime.MediaType;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;

@Slf4j
public class BasePrinter {

    private static final String COLOR_PROFILE = "/color-profile/sRGB_CS_profile.icm";
    private static final String OPEN_SANS_REGULAR = "/font/OpenSans-Regular.ttf";
    private static final String COLOR_PROFILE_NAME = "sRGB IEC61966-2.1";

    private static final String BS_HEADER_IMAGE = "/logo/logo_bs.jpg";
    private static final String BE_HEADER_IMAGE = "/logo/logo_be.jpg";

    // !!! Don't add any other services !!!
    // Otherwise testing becomes really difficult!
    private final TL tl;
    private final AppLanguages appLanguages;
    private final TenantEnum tenant;
    private final FileStoreService fileStoreService;

    public BasePrinter(
        @NonNull AppLanguages appLanguage,
        @NonNull TenantEnum tenant,
        @NonNull FileStoreService fileStoreService
    ) {
        this.tl = new TLProducer(() -> appLanguage).forAppLanguage(appLanguage);
        this.appLanguages = appLanguage;
        this.tenant = tenant;
        this.fileStoreService = fileStoreService;
    }

    public Document getDocument(@NonNull OutputStream outputStream) {
        return getDocument(outputStream, PageSize.A4);
    }
    public Document getDocument(@NonNull OutputStream outputStream, @NonNull PageSize pageSize) {

        //PDF/A-4 requires a PDF 2.0 document
        PdfWriter writer = new PdfWriter(outputStream, new WriterProperties().setPdfVersion(PdfVersion.PDF_2_0));

        PdfADocument pdfA = new PdfADocument(writer, PdfAConformanceLevel.PDF_A_4, getOutputIntent());

        PdfFont font = getFont();
        Document document = new Document(pdfA, pageSize);

        document.setFont(font);
        document.setFontSize(10.0f);

        //Taking care of the additional PDF/A requirements
        pdfA.getCatalog().setLang(new PdfString(appLanguages.locale().toString()));
        pdfA.setTagged();
        PdfDocumentInfo info = pdfA.getDocumentInfo();
        info
            .setCreator("eSAD - DV Bern")
            .addCreationDate();

        return document;
    }

    public void addHeader(@NonNull Document document) {
        try {
            InputStream logoStream = BasePrinter.class.getResourceAsStream(getTenantHeaderImage(this.tenant));
            if (logoStream != null) {
                ImageData imageData = ImageDataFactory.create(logoStream.readAllBytes());
                logoStream.close();

                Image image = new Image(imageData);
                image.setMaxHeight(60);

                Paragraph adressBlock = new Paragraph();
                adressBlock.setMultipliedLeading(1.0f);
                adressBlock.add(tl.translate("printer.kinderdossier.tenant.adresse"));
                adressBlock.setFontSize(10);

                var table = new Table(2).useAllAvailableWidth();
                table.addCell(new Cell().add(image)
                    .setBorder(Border.NO_BORDER));
                table.addCell(new Cell().add(adressBlock)
                    .setBorder(Border.NO_BORDER)
                    .setVerticalAlignment(VerticalAlignment.MIDDLE)
                    .setHorizontalAlignment(HorizontalAlignment.CENTER));
                document.add(table);
            }

        } catch (IOException e) {
            throw new AppFailureException(AppFailureMessage.internalError("Error while adding PDF header"), e);
        }
    }

    public void addPaginationFooter(@NonNull I18nMessage key, @NonNull Document document) {
        FooterWithPageCounterHandler footerWithPageCounterHandler =
            new FooterWithPageCounterHandler(new Paragraph(tl.translate(key)));
        document.getPdfDocument().addEventHandler(PdfDocumentEvent.END_PAGE, footerWithPageCounterHandler);
    }

    public void addH1Title(@NonNull String text, @NonNull Document document) {
        Paragraph paragraph = new Paragraph(text);
        paragraph.setBold();
        paragraph.setFontSize(20.0f);
        paragraph.setTextAlignment(TextAlignment.LEFT);

        document.add(paragraph);
    }

    public void addH1Title(@NonNull I18nMessage key, @NonNull Document document) {
        addH1Title(tl.translate(key), document);
    }

    public void addH2Title(@NonNull String text, @NonNull Document document) {
        Paragraph paragraph = new Paragraph(text);
        paragraph.setBold();
        paragraph.setFontSize(14.0f);
        document.add(paragraph);
    }

    public void addH2Title(@NonNull I18nMessage key, @NonNull Document document) {
        addH2Title(tl.translate(key), document);
    }

    public void addH3Title(@NonNull String text, @NonNull Document document) {
        Paragraph paragraph = new Paragraph(text);
        paragraph.setUnderline();
        paragraph.setFontSize(14.0f);
        document.add(paragraph);
    }

    public void addH3Title(@NonNull I18nMessage key, @NonNull Document document) {
        addH3Title(tl.translate(key), document);
    }

    @NonNull
    public String translateValue(@NonNull String key) {
        return tl.translate(I18nMessage.of(key));
    }

    @NonNull
    public String translateValue(@Nullable Boolean value) {
        if (value == null) {
            return PrintUtil.PLACEHOLDER;
        }
        return value ?
            tl.translate(I18nMessage.of("printer.boolean.true")) :
            tl.translate(I18nMessage.of("printer.boolean.false"));
    }

    @NonNull
    public String translateValue(@Nullable Enum<?> value) {
        if (value == null) {
            return PrintUtil.PLACEHOLDER;
        }
        return tl.translate(I18nMessage.of(value.getDeclaringClass().getSimpleName() + '.' + value.name()));
    }

    @NonNull
    public String getTranslatedString(@NonNull TranslatedString translatedString) {
        return switch (this.appLanguages) {
            case DE:
                yield translatedString.getNameDe();
            case FR:
                yield translatedString.getNameFr();
            default:
                yield PrintUtil.PLACEHOLDER;
        };
    }

    public void addKeyValue(@NonNull String key, @Nullable String value, @NonNull Document document) {
        String translatedKey = tl.translate(I18nMessage.of(key));
        float keyPercentage = getKeyWidthPercentage(translatedKey.length());
        float valuePercentage = 100 - keyPercentage;

        Table keyValueTable = new Table(UnitValue.createPercentArray(new float[]{keyPercentage, valuePercentage}));

        keyValueTable.useAllAvailableWidth();
        keyValueTable.setMarginTop(0.0f);

        String displayValue = PrintUtil.trimToPlaceholder(value);

        // Key cell
        Paragraph labelParagraph = new Paragraph(translatedKey);
        labelParagraph.setMultipliedLeading(1.3f);
        labelParagraph.setBold();
        Cell keyCell = new Cell()
            .add(labelParagraph)
            .setBorder(Border.NO_BORDER)
            .setPadding(0)
            .setPaddingRight(1.0f)
            .setVerticalAlignment(VerticalAlignment.TOP);

        // Value cell
        Paragraph valueParagraph = new Paragraph(displayValue);
        valueParagraph.setMultipliedLeading(1.3f);
        Cell valueCell = new Cell()
            .add(valueParagraph)
            .setBorder(Border.NO_BORDER)
            .setPadding(0)
            .setVerticalAlignment(VerticalAlignment.TOP);

        keyValueTable.addCell(keyCell);
        keyValueTable.addCell(valueCell);

        document.add(keyValueTable);
    }

    // Overloaded methods for different value types
    public void addKeyValue(@NonNull String key, @Nullable Enum<?> value, @NonNull Document document) {
        if (value == null) {
            addKeyValue(key, PrintUtil.PLACEHOLDER, document);
            return;
        }
        addKeyValue(key, translateValue(value), document);
    }

    public void addKeyValue(@NonNull String key, @Nullable Adresse adresse, @NonNull Document document) {
        String adresseFormat = PrintUtil.PLACEHOLDER;
        if (adresse != null) {
            String translatedLand = adresse.getLand() == null ? "" : tl.translate("Land." + adresse.getLand());

            final Pattern replaceEmptyCommaSeparators = Pattern.compile("\\s,");

            adresseFormat = replaceEmptyCommaSeparators.matcher(String.format(
                    "%s, %s, %s %s, %s",
                    StringUtils.trimToEmpty(adresse.getStrasseUndNr()),
                    StringUtils.trimToEmpty(adresse.getAdresseZusatz()),
                    StringUtils.trimToEmpty(adresse.getPlz()),
                    StringUtils.trimToEmpty(adresse.getOrt()),
                    translatedLand))
                .replaceAll("");

            if (adresseFormat.isBlank()) {
                adresseFormat = PrintUtil.PLACEHOLDER;
            }
        }

        addKeyValue(key, adresseFormat, document);
    }

    public void addKeyValue(@NonNull String key, @Nullable Boolean value, @NonNull Document document) {
        if (value == null) {
            addKeyValue(key, PrintUtil.PLACEHOLDER, document);
            return;
        }

        addKeyValue(key, translateValue(value), document);
    }

    public void addKeyValue(@NonNull String key, @Nullable EmailAddress email, @NonNull Document document) {
        addKeyValue(key, email == null ? null : email.getAddress(), document);
    }

    public void addKeyValue(@NonNull String key, @Nullable NormalizedPhoneNumber phone, @NonNull Document document) {
        addKeyValue(key, phone == null ? null : phone.getFormattedPhoneNumber(), document);
    }

    public void addKeyValue(
        @NonNull String key,
        @Nullable SozialversicherungsNummer ahvNr,
        @NonNull Document document) {
        addKeyValue(key, ahvNr == null ? null : ahvNr.toFormattedString(), document);
    }

    public void addVerticalKeyValue(@NonNull String key, @NonNull String value, @NonNull Document document) {
        Paragraph keyParagraph = new Paragraph();
        keyParagraph.setMarginBottom(0.0f);
        keyParagraph.setMarginTop(0.0f);
        keyParagraph.setMultipliedLeading(1.1f);

        keyParagraph.add(tl.translate(I18nMessage.of(key)));

        Paragraph valueParagraph = new Paragraph(value);
        valueParagraph.setMarginBottom(20.0f);

        document.add(keyParagraph);
        document.add(valueParagraph);
    }

    public void addValue(@NonNull I18nMessage key, boolean bold, @NonNull Document document) {
        addValue(tl.translate(key), bold, document);
    }

    public void addValue(@NonNull String text, boolean bold, @NonNull Document document) {
        Paragraph paragraph = new Paragraph(text);
        paragraph.setMarginBottom(0.0f);
        paragraph.setMarginTop(0.0f);
        paragraph.setMultipliedLeading(1.1f);
        if (bold) {
            paragraph.setBold();
        }

        document.add(paragraph);
    }

    public void addValueList(List<I18nMessage> key, @NonNull Document document) {
        com.itextpdf.layout.element.List list = new com.itextpdf.layout.element.List();
        list.setListSymbol("• ");
        list.setMarginLeft(30);
        for (I18nMessage i18nMessage : key) {
            ListItem listItem = new ListItem(tl.translate(i18nMessage));
            list.add(listItem);
        }
        document.add(list);
    }

    @NonNull
    public Paragraph addBlockText(@NonNull I18nMessage key) {
        return addBlockText(tl.translate(key));
    }

    @NonNull
    public Paragraph addBlockText(@NonNull String text) {
        Paragraph blockText = new Paragraph();
        blockText.setMultipliedLeading(1.0f);
        blockText.add(text);
        blockText.setFontSize(10);
        return blockText;
    }

    @NonNull
    public Paragraph getTableHeaderText(@NonNull I18nMessage key) {
        Paragraph blockText = new Paragraph();
        blockText.setMultipliedLeading(1.0f);
        blockText.add(tl.translate(key));
        blockText.setFontSize(10);
        blockText.setBold();
        return blockText;
    }

    public void addHeaderToTable(@NonNull List<I18nMessage> keys, @NonNull Table table) {
        for (I18nMessage key : keys) {
            table.addCell(new Cell().add(getTableHeaderText(key)));
        }
    }

    public void addRowToTable(@NonNull List<String> values, @NonNull Table table) {
        for (String value : values) {
            table.addCell(new Cell().add(addBlockText(value)));
        }
    }

    public void addTable(
        @NonNull List<I18nMessage> headerColumns,
        @NonNull List<List<String>> rows,
        @NonNull Document document
    ) {
        addTable(headerColumns, rows, document, false);
    }

    public void addTable(
        @NonNull List<I18nMessage> headerColumns,
        @NonNull List<List<String>> rows,
        @NonNull Document document,
        boolean useFullWidth
    ) {
        Table table = new Table(headerColumns.size());
        table.setMarginBottom(10.0f);
        // Make table use full width if requested
        if (useFullWidth) {
            table.useAllAvailableWidth();
        }


        addHeaderToTable(headerColumns, table);

        if (rows.isEmpty()) {
            table.addCell(new Cell(1, headerColumns.size())
                .add(addBlockText(I18nMessage.of("printer.table.no-entries")))
                .setTextAlignment(TextAlignment.CENTER));
        } else {
            for (List<String> row : rows) {
                addRowToTable(row, table);
            }
        }

        document.add(table);
    }

    public void addFile(@NonNull FileData fileData, @NonNull Document document) {
        byte[] fileBytesSync = fileStoreService.getFileBytesSync(fileData.getFileInfoBucket().getFileIdInBucket());

        MediaType fileMediaType = fileData.getFileInfoBucket().getMediaType();
        switch (fileMediaType.toString()) {
        case "application/pdf" -> addPdf(fileBytesSync, document);
        // According to this documentation: https://kb.itextpdf.com/itext/which-image-types-are-supported-by-itext#:~:text=iText%20supports%20JPEG%2C%20JPEG2000%2C%20GIF,TIFF%2C%20CCITT%20and%20JBIG2%20images.
        case "image/jpeg", "image/png", "image/gif", "image/svg+xml", "image/bmp", "windows/metafile", "image/tiff" ->
            addImage(fileBytesSync, document);
        default -> {
            document.add(new AreaBreak());
            addValue(I18nMessage.of("printer.file-format.not-supported", "extension", fileMediaType), true, document);
        }
        }
    }

    @NonNull
    public static PdfFont getFont() {
        try {
            InputStream fontStream = BasePrinter.class.getResourceAsStream(OPEN_SANS_REGULAR);
            Objects.requireNonNull(fontStream);
            byte[] bytes = fontStream.readAllBytes();
            PdfFont pdfFont = PdfFontFactory.createFont(bytes, PdfEncodings.IDENTITY_H, EmbeddingStrategy.FORCE_EMBEDDED);
            fontStream.close();
            return pdfFont;
        } catch (IOException e) {
            throw new AppFailureException(AppFailureMessage.internalError("Error while loading font"), e);
        }
    }

    private void addImage(byte[] content, @NonNull Document document) {
        document.add(new AreaBreak());
        ImageData imageData = ImageDataFactory.create(content);
        Image image = new Image(imageData);

        document.add(image);
    }

    private void addPdf(byte[] content, @NonNull Document document) {
        InputStream is = new ByteArrayInputStream(content);
        try (PdfDocument srcPdf = new PdfDocument(new PdfReader(is))) {
            PdfDocument pdfA = document.getPdfDocument();

            for (int i = 1; i <= srcPdf.getNumberOfPages(); i++) {
                document.add(new AreaBreak());
                PdfPage newPage = pdfA.getLastPage();
                PdfCanvas canvas = new PdfCanvas(newPage);

                PdfPage sourcePage = srcPdf.getPage(i);
                PdfFormXObject pageCopy = sourcePage.copyAsFormXObject(pdfA);
                canvas.addXObject(pageCopy);
            }
        } catch (IOException e) {
            throw new AppFailureException(AppFailureMessage.internalError("Error while merging PDFs"), e);
        }
    }

    private static PdfOutputIntent getOutputIntent() {
        InputStream iccStream = BasePrinter.class.getResourceAsStream(COLOR_PROFILE);
        return new PdfOutputIntent("Custom", "", null, COLOR_PROFILE_NAME, iccStream);
    }

    @NonNull
    private static String getTenantHeaderImage(@NonNull TenantEnum tenant) {
        return switch (tenant) {
            case BS -> BS_HEADER_IMAGE;
            case BE -> BE_HEADER_IMAGE;
            default -> throw new IllegalArgumentException("Tenant not supported");
        };
    }

    /**
     * @return percantage that the 'key' i.e. label can take of the full width, given the passed textWidth, there are
     * 3 'breakpoints' defined
     */
    private static float getKeyWidthPercentage(int textSize) {
        if (textSize > 43) {
            return 45;
        }
        if (textSize > 23) {
            return 37.5F;
        }
        return 20;
    }
}
