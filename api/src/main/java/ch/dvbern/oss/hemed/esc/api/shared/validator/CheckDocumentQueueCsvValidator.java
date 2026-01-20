package ch.dvbern.oss.hemed.esc.api.shared.validator;

import ch.dvbern.oss.hemed.esc.api.shared.documentqueue.DocumentQueue;
import ch.dvbern.oss.hemed.esc.api.shared.fileio.FileTyp;
import org.checkerframework.checker.nullness.qual.NonNull;

public class CheckDocumentQueueCsvValidator
    extends AbstractValidator<CheckDocumentQueueCsv, DocumentQueue> {

    @Override
    public boolean isEntityValid(@NonNull DocumentQueue documentQueue) {
        if (documentQueue.getType() == FileTyp.CSV_REPORT) {
            return documentQueue.getDatumVon() != null
                && documentQueue.getDatumBis() != null;
        }
        return true;
    }
}
