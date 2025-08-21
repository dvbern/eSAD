package ch.dvbern.oss.hemed.esc.api.shared.docmerger;

import java.io.InputStream;

import ch.dvbern.dvbstarter.infrastructure.errorhandling.AppFailureException;
import ch.dvbern.dvbstarter.infrastructure.errorhandling.AppFailureMessage;
import ch.dvbern.lib.doctemplate.common.DocTemplateException;
import ch.dvbern.lib.doctemplate.common.MergeSource;
import ch.dvbern.lib.doctemplate.docx.DOCXMergeEngine;
import org.checkerframework.checker.nullness.qual.NonNull;


public final class DocMergerUtil {
    private DocMergerUtil() {
    }

    public static byte[] merge(@NonNull InputStream docTemplateInputStream, @NonNull MergeSource mergeSource) {
        DOCXMergeEngine docxME = new DOCXMergeEngine("ESC");

        try {
            return docxME.getDocument(docTemplateInputStream, mergeSource);
        } catch (DocTemplateException e) {
            throw new AppFailureException(AppFailureMessage.internalError("DocMerging failed: "), e);
        }
    }
}
