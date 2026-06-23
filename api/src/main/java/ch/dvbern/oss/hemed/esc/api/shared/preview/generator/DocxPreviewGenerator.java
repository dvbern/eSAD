package ch.dvbern.oss.hemed.esc.api.shared.preview.generator;

import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

import ch.dvbern.oss.hemed.esc.api.shared.preview.PreviewGenerationException;
import ch.dvbern.oss.hemed.esc.api.shared.preview.PreviewGenerator;
import ch.dvbern.oss.hemed.esc.api.shared.preview.PreviewService;
import com.google.common.net.MediaType;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.xml.bind.JAXBElement;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.docx4j.Docx4J;
import org.docx4j.dml.wordprocessingDrawing.Anchor;
import org.docx4j.openpackaging.packages.WordprocessingMLPackage;
import org.docx4j.openpackaging.parts.WordprocessingML.FooterPart;
import org.docx4j.openpackaging.parts.WordprocessingML.HeaderPart;
import org.docx4j.openpackaging.parts.WordprocessingML.MainDocumentPart;
import org.docx4j.openpackaging.parts.relationships.RelationshipsPart;
import org.docx4j.relationships.Relationship;
import org.docx4j.wml.Body;
import org.docx4j.wml.CTMarkupRange;
import org.docx4j.wml.CTSimpleField;
import org.docx4j.wml.ContentAccessor;
import org.docx4j.wml.Drawing;
import org.docx4j.wml.FldChar;
import org.docx4j.wml.P;
import org.docx4j.wml.R;
import org.docx4j.wml.SectPr;
import org.docx4j.wml.Text;

@ApplicationScoped
@RequiredArgsConstructor
@Slf4j
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
            stripBookmarksAndAnchors(wordMLPackage);
            flattenSimpleFields(wordMLPackage);
            flattenComplexFields(wordMLPackage);
            byte[] pdfBytes = convertToPdf(wordMLPackage);

            // Delegate to PreviewService which routes to PdfPreviewGenerator (renders page 0 only)
            return previewService.generatePreviewImage(pdfBytes, PDF_MIME_TYPE);
        } catch (PreviewGenerationException e) {
            throw e;
        } catch (Exception e) {
            throw new PreviewGenerationException("Failed to generate preview from DOCX", e);
        }
    }

    /**
     * Removes problematic elements from the main document body as well as from every header and
     * footer part of the document. Currently this strips two kinds of nodes that consistently break
     * Apache FOP's PDF conversion (or PDFBox's rendering of the resulting PDF) of these documents:
     * <ul>
     *     <li>{@code w:bookmarkStart}/{@code w:bookmarkEnd} – bookmarks are not rendered visually,
     *     but FOP expands header/footer statics per page and would replicate any bookmark
     *     {@code id} (e.g. {@code Zusatzlogo}) once per page, causing
     *     {@code ValidationException: idNotUnique}.</li>
     *     <li>{@code wp:anchor} drawings (floating images such as background logos) – these break
     *     the conversion in two ways depending on where they appear and what image they reference:
     *     <ul>
     *         <li>In a header/footer they emit a {@code fo:block-container} with an empty
     *         {@code @bpda} attribute in FOP's area tree, which triggers
     *         {@code NumberFormatException: For input string: ""} in {@code FOPAreaTreeHelper} and
     *         breaks the header/footer height calculation.</li>
     *         <li>In the body a floating drawing referencing an image format FOP cannot embed
     *         cleanly (e.g. a {@code .wmf} vector image) makes FOP emit a malformed inline-image
     *         content stream; the conversion appears to succeed, but PDFBox then fails to render
     *         page 0 with {@code IOException: Expected operator 'ID' actual='In'}.</li>
     *     </ul>
     *     Inline ({@code wp:inline}) drawings are kept – they render fine; only anchored/floating
     *     drawings are removed.</li>
     * </ul>
     * Stripping these in the preview pipeline is safe because the preview only renders the first
     * page and the DOCX is never written back to disk.
     */
    void stripBookmarksAndAnchors(WordprocessingMLPackage pkg) {
        forEachContentPart(pkg, this::stripBookmarksAndAnchors);
    }

    void stripBookmarksAndAnchors(@Nullable Object node) {
        if (node != null) {
            if (node instanceof JAXBElement<?> je) {
                stripBookmarksAndAnchors(je.getValue());
                return;
            }
            if (node instanceof Drawing drawing) {
                // Drop floating (wp:anchor) drawings but keep inline ones: the visible inline image is
                // harmless for FOP, only anchored/floating drawings produce the empty @bpda problem.
                drawing.getAnchorOrInline().removeIf(Anchor.class::isInstance);
            }
            if (node instanceof ContentAccessor ca) {
                ca.getContent().removeIf(DocxPreviewGenerator::isBookmarkOrAnchorOnlyDrawing);
                for (Object child : new ArrayList<>(ca.getContent())) {
                    stripBookmarksAndAnchors(child);
                }
            }
        }
    }

    /**
     * Flattens every {@code w:fldSimple} (a {@link CTSimpleField}) in the main document body as well
     * as in all header/footer parts by replacing the field with its cached display runs.
     * <p>
     * docx4j evaluates field instructions during PDF conversion. Fields such as
     * {@code DOCPROPERTY "Department1.City"} reference custom document properties that are usually
     * not present in the package; for those docx4j throws a {@code FieldValueException}/
     * {@code TransformerException} from {@code DocPropertyResolver}, which aborts the whole
     * conversion. Because the preview only needs the rendered first page, we drop the field
     * instruction and keep the cached result text that Word already stored inside the field. This is
     * safe because the DOCX is never written back to disk.
     */
    private void flattenSimpleFields(WordprocessingMLPackage pkg) {
        forEachContentPart(pkg, this::flattenSimpleFields);
    }

    void flattenSimpleFields(@Nullable Object node) {
        if (node == null) {
            return;
        }
        if (node instanceof JAXBElement<?> je) {
            flattenSimpleFields(je.getValue());
            return;
        }
        if (node instanceof ContentAccessor ca) {
            // Build the flattened content in a new list instead of mutating the loop index: each
            // fldSimple is replaced by its cached display runs (which may themselves contain nested
            // fldSimple, hence the recursion on the cached runs before adding them).
            List<Object> content = ca.getContent();
            List<Object> flattened = new ArrayList<>(content.size());
            for (Object child : content) {
                Object value = (child instanceof JAXBElement<?> je) ? je.getValue() : child;
                if (value instanceof CTSimpleField field) {
                    // Replace the field with its cached display runs (drop the unresolvable instruction)
                    // and re-process them so nested fldSimple are flattened too.
                    for (Object cachedChild : new ArrayList<>(field.getContent())) {
                        flattenSimpleFields(cachedChild);
                    }
                    flattened.addAll(field.getContent());
                } else {
                    flattenSimpleFields(child);
                    flattened.add(child);
                }
            }
            content.clear();
            content.addAll(flattened);
        }
    }

    /**
     * Flattens every <em>complex</em> field in the main document body and all header/footer parts,
     * for the same reason as {@link #flattenSimpleFields(WordprocessingMLPackage)}: it removes the
     * unresolvable field instruction so docx4j has nothing to evaluate, keeping only the cached
     * result text.
     * <p>
     * Unlike a simple field ({@link CTSimpleField}), a complex field is encoded as a sequence of
     * runs: a {@code w:fldChar} of type {@code begin}, one or more {@code w:instrText} runs holding
     * the field code (e.g. {@code DOCPROPERTY "Department1.City"}), a {@code w:fldChar} of type
     * {@code separate}, the cached result runs, and a closing {@code w:fldChar} of type {@code end}.
     * Such fields may also be nested (e.g. an {@code IF} field wrapping a {@code DOCPROPERTY}), which
     * is common in documents produced by document-automation tools. We therefore drop every
     * {@code w:fldChar} marker and {@code w:instrText} instruction (at any nesting depth) and keep
     * the plain {@code w:t} runs.
     */
    private void flattenComplexFields(WordprocessingMLPackage pkg) {
        forEachContentPart(pkg, this::flattenComplexFields);
    }

    /**
     * Applies {@code action} to the main document part and to every header and footer part of the
     * package. The argument passed to {@code action} is a content-bearing JAXB element
     * ({@link MainDocumentPart}, {@code Hdr} or {@code Ftr}) that the recursive {@code (Object)}
     * pre-processing helpers know how to traverse. Shared by all pre-processing steps so the
     * main + header/footer fan-out lives in one place.
     */
    private static void forEachContentPart(WordprocessingMLPackage pkg, Consumer<Object> action) {
        MainDocumentPart main = pkg.getMainDocumentPart();
        if (main == null) {
            return;
        }
        action.accept(main);
        RelationshipsPart rels = main.getRelationshipsPart();
        if (rels == null) {
            return;
        }
        for (Relationship r : rels.getRelationships().getRelationship()) {
            Object part = rels.getPart(r);
            if (part instanceof HeaderPart hp) {
                action.accept(hp.getJaxbElement());
            } else if (part instanceof FooterPart fp) {
                action.accept(fp.getJaxbElement());
            }
        }
    }

    void flattenComplexFields(@Nullable Object node) {
        Object value = (node instanceof JAXBElement<?> je) ? je.getValue() : node;
        if (value instanceof R run) {
            flattenRunFieldItems(run);
        } else if (value instanceof ContentAccessor ca) {
            List<Object> content = ca.getContent();
            for (Object child : new ArrayList<>(content)) {
                flattenComplexFieldsInChild(content, child);
            }
        }
    }

    /**
     * Flattens a single child of a content list. Runs have their field-control items
     * ({@code w:fldChar}) and field instruction ({@code w:instrText}) dropped while keeping the
     * visible (cached) result text; a run left empty by this is removed entirely so no empty
     * leftover run remains. Any other child is recursed into.
     */
    private void flattenComplexFieldsInChild(List<Object> content, Object child) {
        Object value = (child instanceof JAXBElement<?> je) ? je.getValue() : child;
        if (value instanceof R run) {
            boolean hadFieldItems = flattenRunFieldItems(run);
            if (hadFieldItems && run.getContent().isEmpty()) {
                content.remove(child);
            }
        } else {
            flattenComplexFields(child);
        }
    }

    /**
     * Removes the field-control items ({@code w:fldChar}) and the field instruction
     * ({@code w:instrText}/{@code w:delInstrText}) from a single run.
     *
     * @return {@code true} if at least one such item was removed
     */
    private static boolean flattenRunFieldItems(R run) {
        return run.getContent().removeIf(DocxPreviewGenerator::isFieldControlItem);
    }

    private static boolean isFieldControlItem(Object o) {
        Object v = (o instanceof JAXBElement<?> je) ? je.getValue() : o;
        // w:fldChar (begin/separate/end markers of a complex field).
        if (v instanceof FldChar) {
            return true;
        }
        // w:instrText / w:delInstrText – the field instruction (e.g. DOCPROPERTY ...). It is encoded
        // as a Text wrapped in a JAXBElement whose local name is "instrText" (or "delInstrText"),
        // which is what distinguishes it from a normal w:t run.
        if (o instanceof JAXBElement<?> je && v instanceof Text) {
            String localPart = je.getName().getLocalPart();
            return "instrText".equals(localPart) || "delInstrText".equals(localPart);
        }
        return false;
    }

    /**
     * Converts the document to a PDF using docx4j/Apache FOP, keeping headers and footers whenever
     * possible and degrading gracefully when they cannot be rendered.
     * <p>
     * The normal conversion ({@link Docx4J#toPDF(WordprocessingMLPackage, java.io.OutputStream)})
     * runs an Apache-FOP area-tree pass to measure the header/footer region heights
     * ({@code LayoutMasterSetBuilder.fixExtents} → {@code FOPAreaTreeHelper.calculateHFExtents}) and
     * then adjusts every page master accordingly – this also corrects the body
     * ({@code region-body}) margins so the document content does not overlap the header/footer.
     * For headers/footers containing content FOP cannot measure (e.g. floating {@code w:framePr}
     * tables or anchored background logos), that pass emits a region with an empty {@code @bpda}
     * (block-progression-dimension) attribute, so {@code Integer.parseInt("")} throws
     * {@code NumberFormatException: For input string: ""} and the whole conversion fails.
     * <p>
     * We therefore try the full conversion first (full fidelity, headers/footers included). Only if
     * it fails do we fall back to dropping the header/footer references and converting again. The
     * fall-back must <em>not</em> simply skip the measurement pass: that pass is what fixes the body
     * region margins, so skipping it (e.g. via {@code FOSettings.setLayoutMasterSetCalculationInProgress})
     * leaves the body region collapsed and renders an essentially empty page. Removing the
     * references instead keeps the body layout intact and only the (unmeasurable) header/footer is
     * lost.
     * <p>
     * Both paths are safe because the preview only renders the (downscaled) first page and the DOCX
     * is never written back to disk.
     */
    private byte[] convertToPdf(WordprocessingMLPackage pkg) throws Exception {
        try {
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            Docx4J.toPDF(pkg, out);
            return out.toByteArray();
        } catch (Exception e) {
            // The most common cause is the FOP header/footer height measurement failing
            // (NumberFormatException: For input string: "") on documents whose headers/footers FOP
            // cannot measure. Drop the header/footer references and retry so the body content is
            // still rendered.
            LOG.warn("PDF conversion with headers/footers failed ({}); retrying without them",
                e.getMessage(), e);
            removeHeaderFooterReferences(pkg);
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            Docx4J.toPDF(pkg, out);
            return out.toByteArray();
        }
    }

    /**
     * Removes every {@code w:headerReference}/{@code w:footerReference} from all section properties
     * ({@code w:sectPr}) so that a subsequent conversion renders without any header or footer.
     * <p>
     * This is used as a fall-back only (see {@link #convertToPdf(WordprocessingMLPackage)}): without
     * the references, FOP no longer pulls the header/footer parts into the layout, so the failing
     * header/footer extent measurement never runs. The body content is unaffected. Safe because the
     * preview only renders the first page and the DOCX is never written back to disk.
     */
    void removeHeaderFooterReferences(WordprocessingMLPackage pkg) {
        MainDocumentPart main = pkg.getMainDocumentPart();
        if (main == null || main.getJaxbElement() == null) {
            return;
        }
        clearHeaderFooterReferences(main.getJaxbElement().getBody());
    }

    void clearHeaderFooterReferences(@Nullable Object node) {
        if (node == null) {
            return;
        }
        if (node instanceof JAXBElement<?> je) {
            clearHeaderFooterReferences(je.getValue());
            return;
        }
        if (node instanceof SectPr sectPr) {
            // Drop both w:headerReference and w:footerReference entries.
            sectPr.getEGHdrFtrReferences().clear();
            return;
        }
        if (node instanceof P paragraph && paragraph.getPPr() != null) {
            // A paragraph-level section break stores its own sectPr inside the paragraph properties.
            clearHeaderFooterReferences(paragraph.getPPr().getSectPr());
        }
        if (node instanceof Body body) {
            // The final/body-level sectPr is not part of the body content list, it is a sibling.
            clearHeaderFooterReferences(body.getSectPr());
        }
        if (node instanceof ContentAccessor ca) {
            for (Object child : new ArrayList<>(ca.getContent())) {
                clearHeaderFooterReferences(child);
            }
        }
    }

    private static boolean isBookmarkOrAnchorOnlyDrawing(Object o) {
        Object v = (o instanceof JAXBElement<?> je) ? je.getValue() : o;
        // CTBookmark extends CTBookmarkRange extends CTMarkupRange, so CTMarkupRange covers both
        // w:bookmarkStart and w:bookmarkEnd.
        if (v instanceof CTMarkupRange) {
            return true;
        }
        // Remove a Drawing entirely if all of its children are wp:anchor entries; if the Drawing
        // also wraps a wp:inline image we keep it (the anchored children are already stripped in
        // stripBookmarksAndAnchors) so the inline picture stays visible in the preview.
        if (v instanceof Drawing drawing) {
            return !drawing.getAnchorOrInline().isEmpty()
                && drawing.getAnchorOrInline().stream().allMatch(Anchor.class::isInstance);
        }
        return false;
    }
}
