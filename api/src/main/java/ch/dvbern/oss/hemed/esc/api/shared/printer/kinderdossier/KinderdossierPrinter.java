package ch.dvbern.oss.hemed.esc.api.shared.printer.kinderdossier;

import java.io.OutputStream;

import ch.dvbern.dvbstarter.infrastructure.stereotypes.NoTransactionService;
import ch.dvbern.oss.commons.i18nl10n.I18nMessage;
import ch.dvbern.oss.hemed.esc.api.features.kinder.Kind;
import ch.dvbern.oss.hemed.esc.api.shared.fileio.FileStoreService;
import ch.dvbern.oss.hemed.esc.api.shared.printer.BasePrinter;
import ch.dvbern.oss.hemed.esc.api.shared.printer.PrintUtil;
import com.itextpdf.layout.Document;
import org.checkerframework.checker.nullness.qual.NonNull;

@NoTransactionService
public final class KinderdossierPrinter {

    private KinderdossierPrinter() {
        // static and hidden so we don't get tempted to add services in here ;)
    }

    public static void print(
        @NonNull KinderdossierPrintDTO dto,
        @NonNull OutputStream outputStream,
        @NonNull FileStoreService fileStoreService
    ) {
        BasePrinter basePrinter = new BasePrinter(dto.getLanguage(), dto.getTenant(), fileStoreService);

        Kind kind = dto.getKind();
        Document document = basePrinter.getDocument(outputStream);
        basePrinter.addHeader(document);
        basePrinter.addH1Title(
            I18nMessage.of(
                "printer.kinderdossier.title",
                "nameVorname",
                kind.getNameVorname(),
                "geburtsdatum",
                PrintUtil.trimToPlaceholder(kind.getGeburtsdatum())),
            document);
        basePrinter.addPaginationFooter(
            I18nMessage.of(
                "printer.kinderdossier.footer",
                "nameVorname",
                kind.getNameVorname(),
                "geburtsdatum",
                PrintUtil.trimToPlaceholder(kind.getGeburtsdatum()),
                "now",
                PrintUtil.trimToPlaceholder(dto.getNow())),
            document);

        KinderdossierStammdatenPrinter.addStammdaten(kind, dto.getKontaktPersonKindList(), basePrinter, document);

        switch (dto.getVariant()) {
        case NOTIZEN -> KinderdossierNotizenPrinter.addAllNotiz(dto.getNotizenList(), basePrinter, document);
        case ANLAESSE -> KinderdossierAnlassPrinter.addAllAnlass(dto.getAnlassDTOList(), basePrinter, document);
        case GESAMTES -> {
            KinderdossierNotizenPrinter.addAllNotiz(dto.getNotizenList(), basePrinter, document);
            KinderdossierAnlassPrinter.addAllAnlass(dto.getAnlassDTOList(), basePrinter, document);
        }
        }

        document.close();
    }
}
