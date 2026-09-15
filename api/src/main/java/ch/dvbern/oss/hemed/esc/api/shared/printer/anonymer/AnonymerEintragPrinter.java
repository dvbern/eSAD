package ch.dvbern.oss.hemed.esc.api.shared.printer.anonymer;

import java.io.OutputStream;

import ch.dvbern.dvbstarter.infrastructure.stereotypes.NoTransactionService;
import ch.dvbern.oss.commons.i18nl10n.I18nMessage;
import ch.dvbern.oss.hemed.esc.api.shared.fileio.FileStoreService;
import ch.dvbern.oss.hemed.esc.api.shared.printer.BasePrinter;
import ch.dvbern.oss.hemed.esc.api.shared.printer.PrintUtil;
import ch.dvbern.oss.hemed.esc.api.shared.printer.kinderdossier.AnlassUntersuchungPrinter;

import com.itextpdf.layout.Document;
import lombok.experimental.UtilityClass;
import org.checkerframework.checker.nullness.qual.NonNull;

@NoTransactionService
@UtilityClass
public final class AnonymerEintragPrinter {

    public static void print(
        @NonNull AnonymerEintragPrintDTO dto,
        @NonNull OutputStream outputStream,
        @NonNull FileStoreService fileStoreService,
        boolean showHeader
    ) {
        BasePrinter basePrinter = new BasePrinter(dto.getLanguage(), dto.getTenant(), fileStoreService);

        Document document = basePrinter.getDocument(outputStream);
        basePrinter.addHeader(document, showHeader);
        basePrinter.addH1Title(
            I18nMessage.of(
                "printer.anonymer-eintrag.title",
                "hash",
                dto.getHash()),
            document);
        basePrinter.addPaginationFooter(
            I18nMessage.of(
                "printer.anonymer-eintrag.footer",
                "hash",
                dto.getHash(),
                "now",
                PrintUtil.trimToPlaceholder(dto.getNow())),
            document);

        AnlassUntersuchungPrinter.addAllAnlass(dto.getAnlassDTOList(), basePrinter, document);

        document.close();
    }
}
