package ch.dvbern.oss.hemed.esc.api.features.printer;

import ch.dvbern.dvbstarter.infrastructure.stereotypes.JaxMapper;
import ch.dvbern.dvbstarter.shared.types.id.ID;
import ch.dvbern.oss.hemed.esc.api.features.anlass.Anlass;
import ch.dvbern.oss.hemed.esc.api.features.anlass.anonymereintrag.AnonymerEintrag;
import ch.dvbern.oss.hemed.esc.api.features.kinder.Kind;
import ch.dvbern.oss.hemed.esc.api.shared.printer.PrintResultDTO;
import ch.dvbern.oss.hemed.esc.api.shared.printer.anlass.AnlassPrintService;
import ch.dvbern.oss.hemed.esc.api.shared.printer.anlass.AnlassPrintVariant;
import ch.dvbern.oss.hemed.esc.api.shared.printer.anonymer.AnonymerEintragPrintService;
import ch.dvbern.oss.hemed.esc.api.shared.printer.kinderdossier.KinderdossierPrintService;
import ch.dvbern.oss.hemed.esc.api.shared.printer.kinderdossier.KinderdossierPrintVariant;

import lombok.RequiredArgsConstructor;
import org.checkerframework.checker.nullness.qual.NonNull;

@JaxMapper
@RequiredArgsConstructor
public final class PrinterMapper {

    private final KinderdossierPrintService kinderdossierPrintService;
    private final AnlassPrintService anlassPrintService;
    private final AnonymerEintragPrintService anonymerEintragPrintService;

    @NonNull
    public PrintResultDTO printKinderdossier(@NonNull KinderdossierPrintVariant variant, @NonNull ID<Kind> kindId) {
        return kinderdossierPrintService.printKinderdossier(variant, kindId, null);
    }

    @NonNull
    public PrintResultDTO printKinderdossierForAnlass(
        @NonNull ID<Kind> kindId,
        @NonNull ID<Anlass> anlassId
    ) {
        return kinderdossierPrintService.printKinderdossier(KinderdossierPrintVariant.ANLAESSE, kindId, anlassId);
    }

    @NonNull
    public PrintResultDTO printAnlass(@NonNull ID<Anlass> anlassId, @NonNull AnlassPrintVariant variant) {
        return anlassPrintService.printAnlass(anlassId, variant);
    }

    @NonNull
    public PrintResultDTO printAnonymerEintrag(@NonNull ID<AnonymerEintrag> anonymerEintragId) {
        return anonymerEintragPrintService.printAnonymerEintrag(anonymerEintragId);
    }
}
