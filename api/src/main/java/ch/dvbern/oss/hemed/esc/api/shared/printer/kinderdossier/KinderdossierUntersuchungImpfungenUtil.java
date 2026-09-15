package ch.dvbern.oss.hemed.esc.api.shared.printer.kinderdossier;

import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

import ch.dvbern.oss.commons.i18nl10n.I18nMessage;
import ch.dvbern.oss.hemed.esc.api.features.anlass.anlassconfig.AnlassConfig;
import ch.dvbern.oss.hemed.esc.api.features.anlass.impfung.UntersuchungImpfstatusKrankheit;
import ch.dvbern.oss.hemed.esc.api.features.anlass.impfung.UntersuchungImpfungIntern;
import ch.dvbern.oss.hemed.esc.api.features.anlass.impfung.UntersuchungImpfungKrankheitImmunitaet;
import ch.dvbern.oss.hemed.esc.api.features.anlass.impfung.UntersuchungImpfungen;
import ch.dvbern.oss.hemed.esc.api.features.anlass.impfung.types.Immunsuppression;
import ch.dvbern.oss.hemed.esc.api.features.anlass.impfung.types.ImpfStatus;
import ch.dvbern.oss.hemed.esc.api.features.anlass.impfung.types.ImpfStatusMinimal;
import ch.dvbern.oss.hemed.esc.api.features.anlass.impfung.types.ImpfausweisGeprueft;
import ch.dvbern.oss.hemed.esc.api.features.anlass.impfung.types.ImpfempfehlungMinimal;
import ch.dvbern.oss.hemed.esc.api.features.anlass.impfung.types.ImpfschutzGegeben;
import ch.dvbern.oss.hemed.esc.api.features.anlass.impfung.types.ImpfungDurchgefuehrtMinimal;
import ch.dvbern.oss.hemed.esc.api.features.anlass.untersuchung.Untersuchung;
import ch.dvbern.oss.hemed.esc.api.shared.printer.BasePrinter;
import ch.dvbern.oss.hemed.esc.api.shared.util.ValueEnhancedUtil;

import com.itextpdf.layout.Document;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;

import static ch.dvbern.oss.hemed.esc.api.shared.printer.PrintUtil.trimToPlaceholder;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class KinderdossierUntersuchungImpfungenUtil {

    public static void printImpfungenHeader(
        @NonNull AnlassConfig config,
        @Nullable Untersuchung untersuchung,
        @NonNull BasePrinter basePrinter,
        @NonNull Document document
    ) {
        if (config.isShowImpfungenHeaderImpfausweisUnvollstaendig()) {
            basePrinter.addKeyValue(
                "printer.kinderdossier.anlass.impfausweisUnvollstaendig",
                KinderdossierPrintUtil.extractUntersuchungImpfungenBooleanFn(
                    untersuchung,
                    UntersuchungImpfungen::isImpfausweisUnvollstaendig),
                document);
        }
        if (config.isShowImpfungenHeaderImpfausweisNichtVorhanden()) {
            basePrinter.addKeyValue(
                "printer.kinderdossier.anlass.impfausweisNichtVorhanden",
                KinderdossierPrintUtil.extractUntersuchungImpfungenBooleanFn(
                    untersuchung,
                    UntersuchungImpfungen::isImpfausweisNichtVorhanden),
                document);
        }
        if (config.isShowImpfungenHeaderImpfausweisZweiPlusEinsSchema()) {
            basePrinter.addKeyValue(
                "printer.kinderdossier.anlass.impfausweisZweiPlusEinsSchema",
                KinderdossierPrintUtil.extractUntersuchungImpfungenBooleanFn(
                    untersuchung,
                    UntersuchungImpfungen::isImpfausweisZweiPlusEinsSchema),
                document);
        }
    }

    public static void printImpfungenDataMinimal(
        @NonNull AnlassConfig config,
        @Nullable Untersuchung untersuchung,
        @NonNull BasePrinter basePrinter,
        @NonNull Document document
    ) {
        if (config.getImpfungenDataMinimalImpfausweisGeprueft().isVisible()) {
            basePrinter.addKeyValue(
                "printer.kinderdossier.anlass.impfungenDataMinimal.impfausweisGeprueft.label",
                KinderdossierPrintUtil.extractUntersuchungImpfungenDataMinimal(
                    untersuchung,
                    d -> basePrinter.translateValue(ValueEnhancedUtil.getEnumValueOrNull(
                        d.getImpfausweisGeprueft(),
                        ImpfausweisGeprueft.class))),
                document);
        }
        if (config.getImpfungenDataMinimalImmunsuppression().isVisible()) {
            basePrinter.addKeyValue(
                "printer.kinderdossier.anlass.impfungenDataMinimal.immunsuppression.label",
                KinderdossierPrintUtil.extractUntersuchungImpfungenDataMinimal(
                    untersuchung,
                    d -> basePrinter.translateValue(ValueEnhancedUtil.getEnumValueOrNull(
                        d.getImmunsuppression(),
                        Immunsuppression.class))),
                document);
        }
        if (config.getImpfungenDataMinimalMasernImpfstatus().isVisible()) {
            basePrinter.addKeyValue(
                "printer.kinderdossier.anlass.impfungenDataMinimal.masernImpfstatus.label",
                KinderdossierPrintUtil.extractUntersuchungImpfungenDataMinimal(
                    untersuchung,
                    d -> basePrinter.translateValue(ValueEnhancedUtil.getEnumValueOrNull(
                        d.getMasernImpfstatus(),
                        ImpfStatusMinimal.class))),
                document);
        }
        if (config.getImpfungenDataMinimalMasernAnzahlImpfungen().isVisible()) {
            basePrinter.addKeyValue(
                "printer.kinderdossier.anlass.impfungenDataMinimal.masernAnzahlImpfungen.label",
                KinderdossierPrintUtil.extractUntersuchungImpfungenDataMinimal(
                    untersuchung,
                    d -> trimToPlaceholder(d.getMasernAnzahlImpfungen())),
                document);
        }
        if (config.getImpfungenDataMinimalHpvImpfstatus().isVisible()) {
            basePrinter.addKeyValue(
                "printer.kinderdossier.anlass.impfungenDataMinimal.hpvImpfstatus.label",
                KinderdossierPrintUtil.extractUntersuchungImpfungenDataMinimal(
                    untersuchung,
                    d -> basePrinter.translateValue(ValueEnhancedUtil.getEnumValueOrNull(
                        d.getHpvImpfstatus(),
                        ImpfStatusMinimal.class))),
                document);
        }
        if (config.getImpfungenDataMinimalHpvAnzahlImpfungen().isVisible()) {
            basePrinter.addKeyValue(
                "printer.kinderdossier.anlass.impfungenDataMinimal.hpvAnzahlImpfungen.label",
                KinderdossierPrintUtil.extractUntersuchungImpfungenDataMinimal(
                    untersuchung,
                    d -> trimToPlaceholder(d.getHpvAnzahlImpfungen())),
                document);
        }
        if (config.getImpfungenDataMinimalImpfempfehlung().isVisible()) {
            basePrinter.addKeyValue(
                "printer.kinderdossier.anlass.impfungenDataMinimal.impfempfehlung.label",
                KinderdossierPrintUtil.extractUntersuchungImpfungenDataMinimal(
                    untersuchung,
                    d -> basePrinter.translateValue(ValueEnhancedUtil.getEnumValueOrNull(
                        d.getImpfempfehlung(),
                        ImpfempfehlungMinimal.class))),
                document);
        }
        if (config.getImpfungenDataMinimalImpfungDurchgefuehrt().isVisible()) {
            basePrinter.addKeyValue(
                "printer.kinderdossier.anlass.impfungenDataMinimal.impfungDurchgefuehrt.label",
                KinderdossierPrintUtil.extractUntersuchungImpfungenDataMinimal(
                    untersuchung,
                    d -> basePrinter.translateValue(ValueEnhancedUtil.getEnumValueOrNull(
                        d.getImpfungDurchgefuehrt(),
                        ImpfungDurchgefuehrtMinimal.class))),
                document);
        }
    }

    public static void printTableImpfstatus(
        @NonNull List<UntersuchungImpfstatusKrankheit> untersuchungImpfstatusKrankheitList,
        @NonNull BasePrinter basePrinter,
        @NonNull Document document
    ) {
        basePrinter.addH3Title(I18nMessage.of("printer.kinderdossier.anlass.table.impfstatus.label"), document);

        basePrinter.addTable(
            List.of(
                I18nMessage.of("printer.kinderdossier.anlass.table.impfstatus.column.krankheit.label"),
                I18nMessage.of("printer.kinderdossier.anlass.table.impfstatus.column.anzahl.label"),
                I18nMessage.of("printer.kinderdossier.anlass.table.impfstatus.column.datum.label"),
                I18nMessage.of("printer.kinderdossier.anlass.table.impfstatus.column.impfschutz.label"),
                I18nMessage.of("printer.kinderdossier.anlass.table.impfstatus.column.impfempfehlung.label"),
                I18nMessage.of("printer.kinderdossier.anlass.table.impfstatus.column.impf.label")),
            untersuchungImpfstatusKrankheitList.stream()
                .sorted(Comparator.comparing(UntersuchungImpfstatusKrankheit::getKrankheit))
                .map(untersuchungImpfstatusKrankheit -> List.of(
                    basePrinter.translateValue(untersuchungImpfstatusKrankheit.getKrankheit()),
                    trimToPlaceholder(untersuchungImpfstatusKrankheit.getAnzahlImpfungen()),
                    trimToPlaceholder(untersuchungImpfstatusKrankheit.getDatumLetzteImpfung(), "MM/yyyy"),
                    untersuchungImpfstatusKrankheit.getImpfschutzGegeben() == ImpfschutzGegeben.KEINE_ANGABE ?
                        "-" :
                        basePrinter.translateValue(untersuchungImpfstatusKrankheit.getImpfschutzGegeben() == ImpfschutzGegeben.JA),
                    basePrinter.translateValue(untersuchungImpfstatusKrankheit.isImpfempfehlung()),
                    basePrinter.translateValue(untersuchungImpfstatusKrankheit.isImpfentscheid())))
                .toList(),
            document
        );
    }

    public static void printImpfungenTableImpfliste(
        @NonNull List<UntersuchungImpfungIntern> impfungInternList,
        @NonNull BasePrinter basePrinter,
        @NonNull Document document
    ) {
        basePrinter.addH3Title(I18nMessage.of("printer.kinderdossier.anlass.table.impfliste.label"), document);

        basePrinter.addTable(
            List.of(
                I18nMessage.of("printer.kinderdossier.anlass.table.impfliste.column.impfstoff.label"),
                I18nMessage.of("printer.kinderdossier.anlass.table.impfliste.column.indikation.label"),
                I18nMessage.of("printer.kinderdossier.anlass.table.impfliste.column.datum.label"),
                I18nMessage.of("printer.kinderdossier.anlass.table.impfliste.column.charge.label"),
                I18nMessage.of("printer.kinderdossier.anlass.table.impfliste.column.erstellt-durch.label"),
                I18nMessage.of("printer.kinderdossier.anlass.table.impfliste.column.verabreicht-durch.label"),
                I18nMessage.of("printer.kinderdossier.anlass.table.impfliste.column.impfschutz-krankheit.label"),
                I18nMessage.of("printer.kinderdossier.anlass.table.impfliste.column.impfung.label")),
            impfungInternList.stream()
                .map(untersuchungImpfungIntern -> List.of(
                    untersuchungImpfungIntern.getImpfstoff().getName(),
                    untersuchungImpfungIntern.getImpfstoff().getKrankheiten().stream()
                        .sorted()
                        .map(basePrinter::translateValue)
                        .collect(Collectors.joining(", ")),
                    trimToPlaceholder(untersuchungImpfungIntern.getDatumImpfung()),
                    KinderdossierPrintUtil.getImpfungInternChargeName(untersuchungImpfungIntern),
                    trimToPlaceholder(untersuchungImpfungIntern.getUserErfasst()),
                    trimToPlaceholder(untersuchungImpfungIntern.getUserDurchgefuehrt()),
                    untersuchungImpfungIntern.getKrankheitenImmunitaeten()
                        .stream()
                        .sorted(Comparator.comparing(UntersuchungImpfungKrankheitImmunitaet::getKrankheit))
                        .map(item -> String.format(
                            "%s: %s",
                            basePrinter.translateValue(item.getKrankheit()),
                            basePrinter.translateValue(item.isImmunitaetGenuegend())))
                        .collect(Collectors.joining("\n ")),
                    basePrinter.translateValue(untersuchungImpfungIntern.getImpfStatus() == ImpfStatus.DURCHGEFUEHRT))
                )
                .toList(),
            document
        );
    }

    public static void printImpfungenCheckliste(
        @NonNull AnlassConfig anlassConfig,
        @Nullable Untersuchung untersuchung,
        @NonNull BasePrinter basePrinter,
        @NonNull Document document
    ) {
        if (anlassConfig.getCheckEinwilligungVorhanden().isVisible()) {
            basePrinter.addKeyValue(
                "printer.kinderdossier.anlass.checkEinwilligungVorhanden",
                KinderdossierPrintUtil.extractUntersuchungImpfungenBooleanFn(
                    untersuchung,
                    UntersuchungImpfungen::isCheckEinwilligungVorhanden),
                document);
        }
        if (anlassConfig.getCheckKontraindikationenAbgeklaert().isVisible()) {
            basePrinter.addKeyValue(
                "printer.kinderdossier.anlass.checkKontraindikationenAbgeklaert",
                KinderdossierPrintUtil.extractUntersuchungImpfungenBooleanFn(
                    untersuchung,
                    UntersuchungImpfungen::isCheckKontraindikationenAbgeklaert),
                document);
        }
        if (anlassConfig.getCheckDoppelkontrolleImpfentscheidDurchgefuehrt().isVisible()) {
            basePrinter.addKeyValue(
                "printer.kinderdossier.anlass.checkDoppelkontrolleImpfentscheidDurchgefuehrt",
                KinderdossierPrintUtil.extractUntersuchungImpfungenBooleanFn(
                    untersuchung,
                    UntersuchungImpfungen::isCheckDoppelkontrolleImpfentscheidDurchgefuehrt),
                document);
        }
        // Bemerkung wird nicht gedruckt
    }

    public static void printImpfungenDoppelkontrolle(
        @Nullable Untersuchung untersuchung,
        @NonNull BasePrinter basePrinter,
        @NonNull Document document
    ) {
        basePrinter.addKeyValue(
            "printer.kinderdossier.anlass.doppelkontrolle.visum1",
            KinderdossierPrintUtil.extractUntersuchungImpfungen(
                untersuchung,
                ui -> trimToPlaceholder(ui.getVisum1() != null ? ui.getVisum1().getPerson() : null)),
            document);
        basePrinter.addKeyValue(
            "printer.kinderdossier.anlass.doppelkontrolle.visum2",
            KinderdossierPrintUtil.extractUntersuchungImpfungen(
                untersuchung,
                ui -> trimToPlaceholder(ui.getVisum2() != null ? ui.getVisum2().getPerson() : null)),
            document);
    }
}
