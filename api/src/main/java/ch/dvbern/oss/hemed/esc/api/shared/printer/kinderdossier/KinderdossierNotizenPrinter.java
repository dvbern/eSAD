package ch.dvbern.oss.hemed.esc.api.shared.printer.kinderdossier;

import java.util.Comparator;
import java.util.List;

import ch.dvbern.oss.commons.i18nl10n.I18nMessage;
import ch.dvbern.oss.hemed.esc.api.features.notizen.Notiz;
import ch.dvbern.oss.hemed.esc.api.shared.abstractentity.AbstractEntity;
import ch.dvbern.oss.hemed.esc.api.shared.fileio.FileData;
import ch.dvbern.oss.hemed.esc.api.shared.printer.BasePrinter;
import ch.dvbern.oss.hemed.esc.api.shared.printer.PrintUtil;
import com.itextpdf.layout.Document;
import org.checkerframework.checker.nullness.qual.NonNull;

public final class KinderdossierNotizenPrinter {

    private KinderdossierNotizenPrinter() {
        // static and hidden so we don't get tempted to add services in here ;)
    }

    public static void addAllNotiz(
        @NonNull List<Notiz> notizes,
        @NonNull BasePrinter basePrinter,
        @NonNull Document document
    ) {
        for (Notiz notiz : notizes) {

            basePrinter.addH2Title(
                I18nMessage.of(
                    "printer.kinderdossier.notizen.subtitle.notiz-vom.label",
                    "date",
                    PrintUtil.formatDate(notiz.getTimestampErstellt()),
                    "title",
                    notiz.getTitel()),
                document
            );
            basePrinter.addKeyValue("printer.kinderdossier.notiz.status.label", notiz.getStatus(), document);
            basePrinter.addKeyValue(
                "printer.kinderdossier.notizen.subtitle.responsible-person.label",
                notiz.getVerantwortlicher().getNameVorname(),
                document);

            basePrinter.addVerticalKeyValue(
                "printer.kinderdossier.notiz.status.bemerkung",
                PrintUtil.trimToPlaceholder(notiz.getBemerkungen()),
                document);

            if (notiz.getFiles().isEmpty()) {
                basePrinter.addValue(
                    I18nMessage.of("printer.kinderdossier.notizen.subtitle.files.label.keine"),
                    false,
                    document);
            } else {
                basePrinter.addValue(
                    I18nMessage.of("printer.kinderdossier.notizen.subtitle.files.label"),
                    false,
                    document);

                List<FileData> files = notiz.getFiles()
                    .stream()
                    .sorted(Comparator.comparing(AbstractEntity::getTimestampErstellt))
                    .toList()
                    .reversed();

                List<I18nMessage> filesMessages = files.stream()
                    .map(file -> I18nMessage.of(
                        "printer.kinderdossier.notizen.subtitle.upload-date.label",
                        "filename",
                        file.getCalculatedFileName(),
                        "date",
                        PrintUtil.formatDate(file.getTimestampErstellt())))
                    .toList();
                basePrinter.addValueList(filesMessages, document);
            }
        }
    }
}
