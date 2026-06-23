package ch.dvbern.oss.hemed.esc.api.shared.preview.generator;

import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.util.ArrayList;

import ch.dvbern.oss.hemed.esc.api.shared.preview.PreviewGenerationException;
import ch.dvbern.oss.hemed.esc.api.shared.preview.PreviewService;
import jakarta.xml.bind.JAXBElement;
import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.rendering.PDFRenderer;
import org.apache.pdfbox.text.PDFTextStripper;
import org.docx4j.dml.wordprocessingDrawing.Anchor;
import org.docx4j.dml.wordprocessingDrawing.Inline;
import org.docx4j.openpackaging.packages.WordprocessingMLPackage;
import org.docx4j.openpackaging.parts.WordprocessingML.FooterPart;
import org.docx4j.openpackaging.parts.WordprocessingML.HeaderPart;
import org.docx4j.openpackaging.parts.WordprocessingML.MainDocumentPart;
import org.docx4j.openpackaging.parts.relationships.RelationshipsPart;
import org.docx4j.relationships.Relationship;
import org.docx4j.wml.Body;
import org.docx4j.wml.CTBookmark;
import org.docx4j.wml.CTMarkupRange;
import org.docx4j.wml.CTSimpleField;
import org.docx4j.wml.ContentAccessor;
import org.docx4j.wml.Drawing;
import org.docx4j.wml.FldChar;
import org.docx4j.wml.Ftr;
import org.docx4j.wml.Hdr;
import org.docx4j.wml.ObjectFactory;
import org.docx4j.wml.P;
import org.docx4j.wml.R;
import org.docx4j.wml.STFldCharType;
import org.docx4j.wml.Text;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
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

    @Test
    void generates_preview_from_docx_with_docproperty_fields() throws Exception {
        // Regression test: DOCPROPERTY simple fields (e.g. Author, Title) caused a crash in
        // DocPropertyHandler.toString() because docx4j throws TransformerException for any
        // unresolvable DOCPROPERTY field. The fix pre-processes the document to flatten these
        // fields to their cached display runs before PDF conversion.
        ObjectFactory factory = new ObjectFactory();

        WordprocessingMLPackage wordMLPackage = WordprocessingMLPackage.createPackage();
        MainDocumentPart mdp = wordMLPackage.getMainDocumentPart();

        // Build a paragraph containing a DOCPROPERTY field: <w:fldSimple w:instr=" DOCPROPERTY Author ">
        //   <w:r><w:t>John Doe</w:t></w:r>
        // </w:fldSimple>
        CTSimpleField fld = factory.createCTSimpleField();
        fld.setInstr(" DOCPROPERTY Author ");
        R run = factory.createR();
        Text text = factory.createText();
        text.setValue("John Doe");
        run.getContent().add(text);
        fld.getContent().add(run);

        P paragraph = factory.createP();
        paragraph.getContent().add(factory.createPFldSimple(fld));
        mdp.getContent().add(paragraph);

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        wordMLPackage.save(out);

        try (InputStream docxStream = new ByteArrayInputStream(out.toByteArray())) {
            BufferedImage image = generator.generatePreview(docxStream);

            assertThat(image).isNotNull();
            assertThat(image.getWidth()).isGreaterThan(0);
            assertThat(image.getHeight()).isGreaterThan(0);
        }
    }

    @Test
    void generates_preview_from_real_problematic_docx() throws Exception {
        // Regression test reproducing the original production failures with this real-world DOCX
        // (api/src/test/resources/PreviewTest/1a_Augen_KG_Notter-Nina_26-05-2026_manualbreak.docx): it contains DOCPROPERTY
        // fields referencing custom document properties that are not present in the package
        // (FieldValueException/TransformerException "No value found for DOCPROPERTY ...") and
        // headers/footers FOP cannot measure (anchored logo + floating w:framePr table) which
        // produced "NumberFormatException: For input string: \"\"" in FOPAreaTreeHelper. The fix
        // tries the full conversion first (keeping headers/footers) and only on failure falls back
        // to removing the header/footer references and converting again, so the body content is
        // always rendered (DocxPreviewGenerator#convertToPdf). We make sure the preview still
        // renders an image for this document.
        try (InputStream docxStream =
            getClass().getResourceAsStream("/PreviewTest/1a_Augen_KG_Notter-Nina_26-05-2026_manualbreak.docx")) {
            assertThat(docxStream).isNotNull();

            BufferedImage image = generator.generatePreview(docxStream);

            assertThat(image).isNotNull();
            assertThat(image.getWidth()).isGreaterThan(0);
            assertThat(image.getHeight()).isGreaterThan(0);
        }
    }

    @Test
    void renders_body_content_for_real_problematic_docx() throws Exception {
        // Regression test for "the whole content is basically missing": an earlier attempt skipped
        // FOP's header/footer extent-measurement pass to dodge the NumberFormatException, but that
        // pass is also what fixes the body (region-body) margins, so the rendered page collapsed and
        // the document text disappeared. This test renders the real fixture and asserts the produced
        // PDF actually contains the body text, not just that some (possibly blank) image came back.
        byte[] pdfBytes = capturePdfBytes("/PreviewTest/1a_Augen_KG_Notter-Nina_26-05-2026_manualbreak.docx");

        String pdfText;
        try (PDDocument document = Loader.loadPDF(pdfBytes)) {
            assertThat(document.getNumberOfPages()).isGreaterThan(0);
            pdfText = new PDFTextStripper().getText(document);
        }

        // The body must not be empty: a collapsed body region would yield (almost) no extractable
        // text. We assert a sensible amount of visible text survived the conversion.
        assertThat(pdfText.replaceAll("\\s", ""))
            .as("rendered PDF must contain the document body text (content must not be missing)")
            .hasSizeGreaterThan(50);
    }

    /**
     * Renders the given DOCX resource through {@link DocxPreviewGenerator#generatePreview} and
     * returns the PDF bytes that were handed to the (mocked) {@link PreviewService}. This lets us
     * inspect what was actually rendered instead of only checking the returned dummy image.
     */
    private byte[] capturePdfBytes(String resource) throws Exception {
        try (InputStream docxStream = getClass().getResourceAsStream(resource)) {
            assertThat(docxStream).isNotNull();
            generator.generatePreview(docxStream);
        }
        ArgumentCaptor<byte[]> pdfCaptor = ArgumentCaptor.forClass(byte[].class);
        Mockito.verify(previewService)
            .generatePreviewImage(pdfCaptor.capture(), Mockito.eq("application/pdf"));
        return pdfCaptor.getValue();
    }

    @Test
    void renders_docx_with_floating_wmf_image_in_body() throws Exception {
        // Regression test for "The docx4j generation produces something that the pdf lib can not
        // handle": the fixture 1a_Augen_KG_Notter-Nina_26-05-2026_manualbreak.docx embeds a floating
        // (wp:anchor) WMF vector image (media/image1.wmf, a "Baselstab" background logo) in the
        // document BODY. FOP cannot embed the WMF cleanly and emits a malformed inline-image content
        // stream; docx4j's toPDF appears to succeed, but PDFBox then fails to render page 0 with
        // "IOException: Expected operator 'ID' actual='In'" (which surfaced as an HTTP 500).
        //
        // The fix strips floating/anchored drawings from the body too (not only from
        // headers/footers, see DocxPreviewGenerator#stripBookmarksAndAnchors). This test renders the
        // produced PDF through PDFBox (exactly what PdfPreviewGenerator does) and asserts it parses
        // and rasterises without throwing.
        byte[] pdfBytes = capturePdfBytes("/PreviewTest/1a_Augen_KG_Notter-Nina_26-05-2026_manualbreak.docx");

        try (PDDocument document = Loader.loadPDF(pdfBytes)) {
            assertThat(document.getNumberOfPages()).isGreaterThan(0);
            // Rendering page 0 is what previously threw "Expected operator 'ID' actual='In'".
            BufferedImage page = new PDFRenderer(document).renderImageWithDPI(0, 100);
            assertThat(page).isNotNull();
        }
    }

    @Test
    void stripBookmarksAndAnchors_removes_floating_drawings_from_body() throws Exception {
        // The body floating WMF drawing is what makes FOP emit the malformed inline image. Verify the
        // pre-processing actually removes every anchored (wp:anchor) drawing from the body while the
        // (model-level) check is independent of FOP/PDFBox behaviour.
        WordprocessingMLPackage pkg;
        try (InputStream docxStream = getClass()
            .getResourceAsStream("/PreviewTest/1a_Augen_KG_Notter-Nina_26-05-2026_manualbreak.docx")) {
            assertThat(docxStream).isNotNull();
            pkg = WordprocessingMLPackage.load(docxStream);
        }

        // Sanity check: the fixture really does contain a floating drawing in the body up-front.
        assertThat(countAnchoredDrawings(pkg.getMainDocumentPart().getJaxbElement().getBody()))
            .as("fixture is expected to contain a floating (wp:anchor) drawing in the body")
            .isGreaterThan(0);

        generator.stripBookmarksAndAnchors(pkg);

        assertThat(countAnchoredDrawings(pkg.getMainDocumentPart().getJaxbElement().getBody()))
            .as("no floating (wp:anchor) drawing must remain in the body after stripping")
            .isZero();
    }

    @Test
    void flatten_removes_all_docproperty_fields_from_real_problematic_docx() throws Exception {
        // The DOCPROPERTY errors are caught and only logged by docx4j, so the previous test would
        // pass even if a single field were left unresolved. This test instead verifies the actual
        // fix: after the pre-processing steps run, the document model must contain neither simple
        // fields (w:fldSimple) nor complex-field control markers (w:fldChar) nor field instructions
        // (w:instrText). Without these, docx4j has nothing left to evaluate and the
        // "No value found for DOCPROPERTY ..." errors can no longer occur.
        WordprocessingMLPackage pkg;
        try (InputStream docxStream =
            getClass().getResourceAsStream("/PreviewTest/1a_Augen_KG_Notter-Nina_26-05-2026_manualbreak.docx")) {
            assertThat(docxStream).isNotNull();
            pkg = WordprocessingMLPackage.load(docxStream);
        }

        // Sanity check: the test fixture really does contain the problematic fields up-front.
        assertThat(countFieldNodes(pkg))
            .as("fixture is expected to contain DOCPROPERTY/complex fields to reproduce the problem")
            .isGreaterThan(0);

        flattenAllParts(pkg);

        assertThat(countFieldNodes(pkg))
            .as("no fldSimple/fldChar/instrText must remain after flattening")
            .isZero();
    }

    @Test
    void preprocessing_keeps_header_and_footer_references_for_real_problematic_docx() throws Exception {
        // The current fix tries the full conversion (headers/footers included) first and only removes
        // the header/footer references as a fall-back when that conversion fails. The pre-processing
        // steps that always run (bookmark/anchor stripping, field flattening) must therefore NOT
        // remove any header/footer references, so a "clean" document keeps its headers/footers.
        WordprocessingMLPackage pkg;
        try (InputStream docxStream =
            getClass().getResourceAsStream("/PreviewTest/1a_Augen_KG_Notter-Nina_26-05-2026_manualbreak.docx")) {
            assertThat(docxStream).isNotNull();
            pkg = WordprocessingMLPackage.load(docxStream);
        }

        int referencesBefore = countHeaderFooterReferences(pkg);
        // Sanity check: the fixture really does reference headers/footers up-front.
        assertThat(referencesBefore)
            .as("fixture is expected to reference headers/footers")
            .isGreaterThan(0);

        // The pre-processing steps that run before conversion must not remove header/footer refs.
        generator.stripBookmarksAndAnchors(pkg.getMainDocumentPart().getJaxbElement().getBody());
        generator.flattenSimpleFields(pkg.getMainDocumentPart());
        generator.flattenComplexFields(pkg.getMainDocumentPart());

        assertThat(countHeaderFooterReferences(pkg))
            .as("header/footer references must be kept by pre-processing (only the fall-back removes them)")
            .isEqualTo(referencesBefore);
    }

    @Test
    void removeHeaderFooterReferences_removes_all_header_and_footer_references() throws Exception {
        // The conversion fall-back (used only when the full conversion with headers/footers fails)
        // drops every w:headerReference/w:footerReference so FOP no longer pulls the unmeasurable
        // header/footer parts into the layout. This guards that fall-back step.
        WordprocessingMLPackage pkg;
        try (InputStream docxStream =
            getClass().getResourceAsStream("/PreviewTest/1a_Augen_KG_Notter-Nina_26-05-2026_manualbreak.docx")) {
            assertThat(docxStream).isNotNull();
            pkg = WordprocessingMLPackage.load(docxStream);
        }

        // Sanity check: the fixture really does reference headers/footers up-front.
        assertThat(countHeaderFooterReferences(pkg))
            .as("fixture is expected to reference headers/footers to reproduce the problem")
            .isGreaterThan(0);

        generator.removeHeaderFooterReferences(pkg);

        assertThat(countHeaderFooterReferences(pkg))
            .as("no w:headerReference/w:footerReference must remain after removal")
            .isZero();
    }

    @Test
    void flattenComplexFields_removes_fldchar_and_instrtext_keeps_result_text() {
        // A complex field is a run sequence: fldChar(begin) instrText(DOCPROPERTY ...)
        // fldChar(separate) <result run> fldChar(end). flattenComplexFields must drop every fldChar
        // and instrText but keep the cached result text run so the preview still shows it.
        ObjectFactory factory = new ObjectFactory();
        P paragraph = factory.createP();

        paragraph.getContent().add(runWithFldChar(STFldCharType.BEGIN));
        paragraph.getContent().add(runWithInstrText(" DOCPROPERTY \"Department1.City\" "));
        paragraph.getContent().add(runWithFldChar(STFldCharType.SEPARATE));
        R resultRun = runWithText("Bern");
        paragraph.getContent().add(resultRun);
        paragraph.getContent().add(runWithFldChar(STFldCharType.END));

        generator.flattenComplexFields(paragraph);

        // Only the result run survives, and it still carries its visible text.
        assertThat(paragraph.getContent()).containsExactly(resultRun);
        assertThat(resultRun.getContent())
            .singleElement()
            .isInstanceOfSatisfying(Text.class, t -> assertThat(t.getValue()).isEqualTo("Bern"));
    }

    @Test
    void flattenComplexFields_handles_nested_fields() {
        // Document-automation tools nest fields (e.g. IF wrapping DOCPROPERTY). All fldChar/instrText
        // items must be removed regardless of nesting depth.
        ObjectFactory factory = new ObjectFactory();
        P paragraph = factory.createP();

        paragraph.getContent().add(runWithFldChar(STFldCharType.BEGIN));
        paragraph.getContent().add(runWithInstrText(" IF "));
        paragraph.getContent().add(runWithFldChar(STFldCharType.BEGIN));
        paragraph.getContent().add(runWithInstrText(" DOCPROPERTY \"Contactperson.Department\" "));
        paragraph.getContent().add(runWithFldChar(STFldCharType.SEPARATE));
        paragraph.getContent().add(runWithText("Schul\u00e4rztlicher Dienst"));
        paragraph.getContent().add(runWithFldChar(STFldCharType.END));
        paragraph.getContent().add(runWithFldChar(STFldCharType.END));

        generator.flattenComplexFields(paragraph);

        assertThat(countFieldNodes(paragraph)).isZero();
    }

    // ---------------------------------------------------------------------
    // Tests for stripBookmarksAndAnchors – the helper that removes problematic
    // elements (bookmarks, anchored/floating drawings) from the body and from header/footer
    // content before FOP renders the PDF. See the Javadoc on
    // DocxPreviewGenerator#stripBookmarksAndAnchors for context.
    // ---------------------------------------------------------------------

    @Test
    void stripBookmarksAndAnchors_removes_bookmark_start_and_end_from_header_content() {
        // Bookmarks in a header trigger FOP's "ValidationException: idNotUnique" because the
        // header static is replicated per page. The strip helper must remove them entirely.
        ObjectFactory factory = new ObjectFactory();
        Hdr header = new Hdr();

        CTBookmark bookmarkStart = factory.createCTBookmark();
        bookmarkStart.setName("Zusatzlogo");
        bookmarkStart.setId(java.math.BigInteger.ZERO);
        CTMarkupRange bookmarkEnd = factory.createCTMarkupRange();
        bookmarkEnd.setId(java.math.BigInteger.ZERO);

        header.getContent().add(factory.createHdrBookmarkStart(bookmarkStart));
        header.getContent().add(factory.createHdrBookmarkEnd(bookmarkEnd));

        generator.stripBookmarksAndAnchors(header);

        assertThat(header.getContent()).isEmpty();
    }

    @Test
    void stripBookmarksAndAnchors_removes_anchor_drawing_from_footer_content() {
        // Floating wp:anchor drawings (typical: background logos) cause FOP's FOPAreaTreeHelper
        // to read an empty @bpda and produce a NumberFormatException. They must be stripped from
        // header/footer parts before PDF conversion.
        ObjectFactory factory = new ObjectFactory();
        Ftr footer = new Ftr();

        Drawing anchorOnlyDrawing = factory.createDrawing();
        anchorOnlyDrawing.getAnchorOrInline().add(new Anchor());

        P paragraph = factory.createP();
        R run = factory.createR();
        run.getContent().add(anchorOnlyDrawing);
        paragraph.getContent().add(run);
        footer.getContent().add(paragraph);

        generator.stripBookmarksAndAnchors(footer);

        // The anchor-only drawing is removed wholesale; the wrapping paragraph and run remain
        // because they may still contain other content in real documents.
        assertThat(footer.getContent()).containsExactly(paragraph);
        assertThat(run.getContent()).isEmpty();
    }

    @Test
    void stripBookmarksAndAnchors_keeps_inline_drawing() {
        // Inline drawings (wp:inline) flow with the text and don't trigger the FOP @bpda bug.
        // They must therefore be preserved so the visible inline image stays in the preview.
        ObjectFactory factory = new ObjectFactory();
        Hdr header = new Hdr();

        Drawing inlineDrawing = factory.createDrawing();
        inlineDrawing.getAnchorOrInline().add(new Inline());

        P paragraph = factory.createP();
        R run = factory.createR();
        run.getContent().add(inlineDrawing);
        paragraph.getContent().add(run);
        header.getContent().add(paragraph);

        generator.stripBookmarksAndAnchors(header);

        assertThat(run.getContent()).containsExactly(inlineDrawing);
        assertThat(inlineDrawing.getAnchorOrInline())
            .hasSize(1)
            .allMatch(Inline.class::isInstance);
    }

    @Test
    void stripBookmarksAndAnchors_keeps_inline_when_drawing_has_both_anchor_and_inline() {
        // A mixed drawing (anchor + inline) must keep the inline part: only the anchored children
        // are dropped, the drawing itself stays so the inline picture remains visible.
        ObjectFactory factory = new ObjectFactory();
        Hdr header = new Hdr();

        Drawing mixedDrawing = factory.createDrawing();
        Inline inline = new Inline();
        Anchor anchor = new Anchor();
        mixedDrawing.getAnchorOrInline().add(anchor);
        mixedDrawing.getAnchorOrInline().add(inline);

        P paragraph = factory.createP();
        R run = factory.createR();
        run.getContent().add(mixedDrawing);
        paragraph.getContent().add(run);
        header.getContent().add(paragraph);

        generator.stripBookmarksAndAnchors(header);

        assertThat(run.getContent()).containsExactly(mixedDrawing);
        assertThat(mixedDrawing.getAnchorOrInline()).containsExactly(inline);
    }

    @Test
    void stripBookmarksAndAnchors_handles_jaxb_element_wrapping() {
        // In real-world DOCX content, w:bookmarkStart is unmarshalled as JAXBElement<CTBookmark>.
        // The strip helper must look through the JAXBElement wrapper to recognise the bookmark.
        ObjectFactory factory = new ObjectFactory();
        Hdr header = new Hdr();

        CTBookmark bookmark = factory.createCTBookmark();
        bookmark.setName("Logo");
        bookmark.setId(java.math.BigInteger.ONE);
        JAXBElement<CTBookmark> jaxbBookmark = factory.createPBookmarkStart(bookmark);

        P paragraph = factory.createP();
        paragraph.getContent().add(jaxbBookmark);
        header.getContent().add(paragraph);

        generator.stripBookmarksAndAnchors(header);

        assertThat(paragraph.getContent()).isEmpty();
    }

    @Test
    void stripBookmarksAndAnchors_processes_nested_paragraph_content() {
        // Headers/footers typically nest content inside paragraphs and runs. The strip helper
        // must recurse so that bookmarks and anchors deep in the structure are also removed.
        ObjectFactory factory = new ObjectFactory();
        Hdr header = new Hdr();

        CTBookmark bookmark = factory.createCTBookmark();
        bookmark.setName("Nested");
        bookmark.setId(java.math.BigInteger.TEN);
        Drawing anchorDrawing = factory.createDrawing();
        anchorDrawing.getAnchorOrInline().add(new Anchor());

        R run = factory.createR();
        Text text = factory.createText();
        text.setValue("visible text");
        run.getContent().add(text);
        run.getContent().add(anchorDrawing);

        P paragraph = factory.createP();
        paragraph.getContent().add(factory.createPBookmarkStart(bookmark));
        paragraph.getContent().add(run);
        header.getContent().add(paragraph);

        generator.stripBookmarksAndAnchors(header);

        // The bookmark inside the paragraph and the anchor drawing inside the run are gone,
        // but the visible text run survives.
        assertThat(paragraph.getContent()).containsExactly(run);
        assertThat(run.getContent()).containsExactly(text);
    }

    @Test
    void stripBookmarksAndAnchors_keeps_unrelated_content() {
        // Sanity check: ordinary text content in a header/footer must not be touched.
        ObjectFactory factory = new ObjectFactory();
        Hdr header = new Hdr();

        P paragraph = factory.createP();
        R run = factory.createR();
        Text text = factory.createText();
        text.setValue("page header");
        run.getContent().add(text);
        paragraph.getContent().add(run);
        header.getContent().add(paragraph);

        generator.stripBookmarksAndAnchors(header);

        assertThat(header.getContent()).containsExactly(paragraph);
        assertThat(paragraph.getContent()).containsExactly(run);
        assertThat(run.getContent()).containsExactly(text);
    }

    @Test
    void stripBookmarksAndAnchors_ignores_null_node() {
        // Defensive: a null node must be tolerated without throwing.
        generator.stripBookmarksAndAnchors((Object) null);
    }

    // ---------------------------------------------------------------------
    // Test helpers
    // ---------------------------------------------------------------------

    /** Runs both flattening steps over the main part and every header/footer part. */
    private void flattenAllParts(WordprocessingMLPackage pkg) {
        forEachContentPart(pkg, part -> {
            generator.flattenSimpleFields(part);
            generator.flattenComplexFields(part);
        });
    }

    private static void forEachContentPart(WordprocessingMLPackage pkg, java.util.function.Consumer<Object> action) {
        MainDocumentPart main = pkg.getMainDocumentPart();
        action.accept(main);
        RelationshipsPart rels = main.getRelationshipsPart();
        for (Relationship r : rels.getRelationships().getRelationship()) {
            Object part = rels.getPart(r);
            if (part instanceof HeaderPart hp) {
                action.accept(hp.getJaxbElement());
            } else if (part instanceof FooterPart fp) {
                action.accept(fp.getJaxbElement());
            }
        }
    }

    private static int countHeaderFooterReferences(WordprocessingMLPackage pkg) {
        int count = 0;
        Body body = pkg.getMainDocumentPart().getJaxbElement().getBody();
        if (body.getSectPr() != null) {
            count += body.getSectPr().getEGHdrFtrReferences().size();
        }
        for (Object child : new ArrayList<>(body.getContent())) {
            Object value = (child instanceof JAXBElement<?> je) ? je.getValue() : child;
            if (value instanceof P paragraph
                && paragraph.getPPr() != null
                && paragraph.getPPr().getSectPr() != null) {
                count += paragraph.getPPr().getSectPr().getEGHdrFtrReferences().size();
            }
        }
        return count;
    }

    private static int countFieldNodes(WordprocessingMLPackage pkg) {
        int count = countFieldNodes(pkg.getMainDocumentPart());
        RelationshipsPart rels = pkg.getMainDocumentPart().getRelationshipsPart();
        for (Relationship r : rels.getRelationships().getRelationship()) {
            Object part = rels.getPart(r);
            if (part instanceof HeaderPart hp) {
                count += countFieldNodes(hp.getJaxbElement());
            } else if (part instanceof FooterPart fp) {
                count += countFieldNodes(fp.getJaxbElement());
            }
        }
        return count;
    }

    /** Counts w:fldSimple, w:fldChar and w:instrText/w:delInstrText nodes anywhere below {@code node}. */
    private static int countFieldNodes(Object node) {
        if (node == null) {
            return 0;
        }
        if (node instanceof JAXBElement<?> je) {
            int self = "instrText".equals(je.getName().getLocalPart())
                || "delInstrText".equals(je.getName().getLocalPart()) ? 1 : 0;
            return self + countFieldNodes(je.getValue());
        }
        int count = 0;
        if (node instanceof CTSimpleField || node instanceof FldChar) {
            count++;
        }
        if (node instanceof ContentAccessor ca) {
            for (Object child : new ArrayList<>(ca.getContent())) {
                count += countFieldNodes(child);
            }
        }
        return count;
    }

    /** Counts floating (wp:anchor) drawings anywhere below {@code node}. */
    private static int countAnchoredDrawings(Object node) {
        if (node == null) {
            return 0;
        }
        if (node instanceof JAXBElement<?> je) {
            return countAnchoredDrawings(je.getValue());
        }
        int count = 0;
        if (node instanceof Drawing drawing) {
            count += (int) drawing.getAnchorOrInline().stream().filter(Anchor.class::isInstance).count();
        }
        if (node instanceof ContentAccessor ca) {
            for (Object child : new ArrayList<>(ca.getContent())) {
                count += countAnchoredDrawings(child);
            }
        }
        return count;
    }

    private static R runWithFldChar(STFldCharType type) {
        ObjectFactory factory = new ObjectFactory();
        R run = factory.createR();
        FldChar fldChar = factory.createFldChar();
        fldChar.setFldCharType(type);
        run.getContent().add(fldChar);
        return run;
    }

    private static R runWithInstrText(String instruction) {
        ObjectFactory factory = new ObjectFactory();
        R run = factory.createR();
        Text text = factory.createText();
        text.setValue(instruction);
        run.getContent().add(factory.createRInstrText(text));
        return run;
    }

    private static R runWithText(String value) {
        ObjectFactory factory = new ObjectFactory();
        R run = factory.createR();
        Text text = factory.createText();
        text.setValue(value);
        run.getContent().add(text);
        return run;
    }
}
