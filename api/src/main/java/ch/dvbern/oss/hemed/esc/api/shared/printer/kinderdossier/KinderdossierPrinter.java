package ch.dvbern.oss.hemed.esc.api.shared.printer.kinderdossier;

import java.io.OutputStream;

import ch.dvbern.dvbstarter.infrastructure.stereotypes.NoTransactionService;
import ch.dvbern.oss.commons.i18nl10n.I18nMessage;
import ch.dvbern.oss.hemed.esc.api.features.kinder.Kind;
import ch.dvbern.oss.hemed.esc.api.shared.config.service.ConfigService;
import ch.dvbern.oss.hemed.esc.api.shared.fileio.FileStoreService;
import ch.dvbern.oss.hemed.esc.api.shared.printer.BasePrinter;
import ch.dvbern.oss.hemed.esc.api.shared.printer.PrintUtil;
import ch.dvbern.oss.hemed.esc.api.shared.settings.TenantSettingsService;

import com.itextpdf.layout.Document;
import lombok.experimental.UtilityClass;
import org.checkerframework.checker.nullness.qual.NonNull;

@NoTransactionService
@UtilityClass
public final class KinderdossierPrinter {
    public static void print(
        @NonNull KinderdossierPrintDTO dto,
        @NonNull OutputStream outputStream,
        @NonNull FileStoreService fileStoreService,
        @NonNull TenantSettingsService tenantSettingsService,
        @NonNull ConfigService configService
    ) {
        BasePrinter basePrinter = new BasePrinter(dto.getLanguage(), dto.getTenant(), fileStoreService);

        Kind kind = dto.getKind();
        Document document = basePrinter.getDocument(outputStream);
        basePrinter.addHeader(document, tenantSettingsService.isTenantHeaderInExportsEnabled());
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

        KinderdossierStammdatenPrinter.addStammdaten(
            kind,
            dto.getKontaktPersonKindList(),
            basePrinter,
            document,
            tenantSettingsService,
            configService);

        switch (dto.getVariant()) {
        case NOTIZEN -> KinderdossierNotizenPrinter.addAllNotiz(dto.getNotizenList(), basePrinter, document);
        case ANLAESSE -> AnlassUntersuchungPrinter.addAllAnlass(dto.getAnlassDTOList(), basePrinter, document);
        case GESAMTES -> {
            KinderdossierNotizenPrinter.addAllNotiz(dto.getNotizenList(), basePrinter, document);
            AnlassUntersuchungPrinter.addAllAnlass(dto.getAnlassDTOList(), basePrinter, document);
        }
        }

        document.close();
    }
}
