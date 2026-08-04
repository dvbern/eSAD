/*
 *
 * Copyright (C) 2025 DV Bern AG, Switzerland
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package ch.dvbern.oss.hemed.esc.api.shared.printer.kinderdossier;

import java.util.Comparator;
import java.util.List;
import java.util.Objects;

import ch.dvbern.oss.commons.i18nl10n.I18nMessage;
import ch.dvbern.oss.hemed.esc.api.features.anlass.Anlass;
import ch.dvbern.oss.hemed.esc.api.features.anlass.anlassconfig.AnlassConfig;
import ch.dvbern.oss.hemed.esc.api.features.anlass.anlassconfig.AnlassConfigUtil;
import ch.dvbern.oss.hemed.esc.api.features.anlass.durchfuehrung.UntersuchungDurchfuehrung;
import ch.dvbern.oss.hemed.esc.api.features.anlass.einladung.Einladung;
import ch.dvbern.oss.hemed.esc.api.features.anlass.einladung.types.EinladungStatus;
import ch.dvbern.oss.hemed.esc.api.features.anlass.kind.UntersuchungKindDaten;
import ch.dvbern.oss.hemed.esc.api.features.anlass.untersuchung.Untersuchung;
import ch.dvbern.oss.hemed.esc.api.shared.printer.BasePrinter;
import ch.dvbern.oss.hemed.esc.api.shared.printer.PDFAPrintUtil;
import ch.dvbern.oss.hemed.esc.api.shared.printer.untersuchung.UntersuchungPrinter;
import ch.dvbern.oss.hemed.esc.api.shared.util.AnlassTitelUtil;

import com.itextpdf.layout.Document;
import com.itextpdf.layout.element.AreaBreak;
import org.checkerframework.checker.nullness.qual.NonNull;

import static ch.dvbern.oss.hemed.esc.api.shared.printer.PrintUtil.trimToPlaceholder;
import static ch.dvbern.oss.hemed.esc.api.shared.printer.kinderdossier.KinderdossierPrintUtil.isAnyVisible;

public final class AnlassUntersuchungPrinter {

    private AnlassUntersuchungPrinter() {
        // static and hidden so we don't get tempted to add services in here ;)
    }

    public static void addAllAnlass(
        @NonNull List<AnlassUntersuchungPrintDTO> anlassPrintDTOS,
        @NonNull BasePrinter basePrinter,
        @NonNull Document document
    ) {
        List<AnlassUntersuchungPrintDTO> sortedList = anlassPrintDTOS.stream()
            .sorted(Comparator.comparing(o -> o.getAnlass().getDatum()))
            .toList()
            .reversed();

        for (AnlassUntersuchungPrintDTO item : sortedList) {
            addAnlassdaten(item, basePrinter, document);
            if (item.getEffectiveEinladungStatus() == EinladungStatus.TEILNAHME_BESTAETIGT) {
                Anlass anlass = item.getAnlass();
                AnlassConfig anlassConfig = anlass.getAnlassConfig();
                Untersuchung untersuchung = item.getUntersuchung();
                addUntersuchungImpfungen(item, basePrinter, document);
                UntersuchungPrinter.addUntersuchungSehen(anlassConfig, untersuchung, basePrinter, document);
                UntersuchungPrinter.addUntersuchungHoerenAudiogramm(anlassConfig, untersuchung, basePrinter, document);
                UntersuchungPrinter.addUntersuchungHoerenOtoskopie(anlassConfig, untersuchung, basePrinter, document);
                UntersuchungPrinter.addUntersuchungGroesseGewicht(anlassConfig, untersuchung, basePrinter, document);
                UntersuchungPrinter.adduntersuchungTriage(anlassConfig, untersuchung, basePrinter, document);
                UntersuchungPrinter.addLaeusekontrolle(anlassConfig, untersuchung, basePrinter, document);
                UntersuchungPrinter.addUntersuchungArzt(anlassConfig, untersuchung, basePrinter, document);
                UntersuchungPrinter.addUntersuchungSprache(anlassConfig, untersuchung, basePrinter, document);
                UntersuchungPrinter.addBewegungsapparat(anlassConfig, untersuchung, basePrinter, document);
                UntersuchungPrinter.addUntersuchungSchulrelevanteBeeintraechtigung(anlassConfig, untersuchung, basePrinter, document);
                UntersuchungPrinter.addUntersuchungImpfungenMassnahmen(
                    anlassConfig,
                    untersuchung,
                    item.getFolgeImpfAnlassList(),
                    basePrinter,
                    document);
                UntersuchungPrinter.addUntersuchungMassnahmen(anlassConfig, untersuchung, basePrinter, document);
            }
        }
    }

    private static void addAnlassdaten(
        @NonNull AnlassUntersuchungPrintDTO item,
        @NonNull BasePrinter basePrinter,
        @NonNull Document document
    ) {
        Anlass anlass = item.getAnlass();
        Untersuchung untersuchung = item.getUntersuchung();
        AnlassConfig anlassConfig = anlass.getAnlassConfig();
        if (!(isAnyVisible(anlassConfig.getDatenUntersuchung())
            || anlassConfig.isShowDatenSchule())) {
            return;
        }
        document.add(new AreaBreak());

        boolean anonymous = item.isAnonymous();

        basePrinter.addH2Title(
            I18nMessage.of(
                "printer.kinderdossier.anlass.subtitle.anlass.label",
                "date",
                trimToPlaceholder(anlass.getDatum()),
                "title",
                anlassTitle(anlass, basePrinter)),
            document);

        // For anonymous prints there is no Kind/Dossier: render the child's hash directly under the
        // Anlasstitel instead of any personal identifier.
        if (anonymous) {
            basePrinter.addKeyValue(
                "printer.kinderdossier.anlass.hash",
                Objects.requireNonNull(item.getHash()),
                document);
        }

        basePrinter.addH3Title(
            I18nMessage.of("printer.kinderdossier.anlass.subtitle.anlassdaten.label"),
            document);

        basePrinter.addKeyValue(
            "printer.kinderdossier.anlass.anlass-typ",
            anlassConfig.getAnlassTyp(),
            document);

        // Skip school/class data for anonymous prints: it would leak organization context and the
        // underlying data is resolved via the (absent) Dossier for anonymous entries.
        if (anlassConfig.isShowDatenSchule() && !anonymous) {
            Einladung schuleEinladung = Objects.requireNonNull(item.getEinladung());
            basePrinter.addKeyValue(
                "printer.kinderdossier.anlass.schule",
                KinderdossierPrintUtil.extractUntersuchungKindDaten(
                    schuleEinladung,
                    daten -> String.format(
                        "%s %s",
                        trimToPlaceholder(daten.getSchulstandort()),
                        trimToPlaceholder(daten.getSchulhaus()))),
                document);

            basePrinter.addKeyValue(
                "printer.kinderdossier.anlass.klasse",
                KinderdossierPrintUtil.extractUntersuchungKindDaten(schuleEinladung, UntersuchungKindDaten::getKlasse),
                document);
        }

        EinladungStatus einladungStatus = item.getEffectiveEinladungStatus();
        basePrinter.addKeyValue(
            "printer.kinderdossier.anlass.einladungStatus",
            basePrinter.translateValue(einladungStatus),
            document);

        if (einladungStatus == EinladungStatus.TEILNAHME_ABGESAGT && item.getEinladung() != null) {
            basePrinter.addKeyValue(
                "printer.kinderdossier.anlass.grundAbmeldung",
                item.getEinladung().getGrundAbmeldung(),
                document);
        }

        if (anlassConfig.getDatenUntersuchung().isVisible()) {
            basePrinter.addKeyValue(
                "printer.kinderdossier.anlass.nachkontrolle",
                KinderdossierPrintUtil.extractUntersuchungDurchfuehrungBooleanFn(
                    untersuchung,
                    UntersuchungDurchfuehrung::isNachkontrolle),
                document);
            basePrinter.addKeyValue(
                "printer.kinderdossier.anlass.nachzugler",
                KinderdossierPrintUtil.extractUntersuchungDurchfuehrungBooleanFn(
                    untersuchung,
                    UntersuchungDurchfuehrung::isNachzuegler),
                document);
            basePrinter.addKeyValue(
                "printer.kinderdossier.anlass.neueintritt",
                KinderdossierPrintUtil.extractUntersuchungDurchfuehrungBooleanFn(
                    untersuchung,
                    UntersuchungDurchfuehrung::isNeueintritt),
                document);

            basePrinter.addKeyValue(
                "printer.kinderdossier.anlass.arzt",
                trimToPlaceholder(anlass.getArzt()),
                document);
            basePrinter.addKeyValue(
                "printer.kinderdossier.anlass.mpa",
                trimToPlaceholder(anlass.getMpa()),
                document);
        }

        basePrinter.addKeyValue(
            "printer.kinderdossier.anlass.weiterePerson",
            trimToPlaceholder(anlass.getWeiterePerson()),
            document);

        basePrinter.addKeyValue(
            "printer.kinderdossier.anlass.durchfuehrung.arzt",
            KinderdossierPrintUtil.extractUntersuchungDurchfuehrung(
                untersuchung,
                u -> trimToPlaceholder(u.getArzt())),
            document);
        basePrinter.addKeyValue(
            "printer.kinderdossier.anlass.durchfuehrung.mpa",
            KinderdossierPrintUtil.extractUntersuchungDurchfuehrung(
                untersuchung,
                u -> trimToPlaceholder(u.getMpa())),
            document);
        basePrinter.addKeyValue(
            "printer.kinderdossier.anlass.durchfuehrung.bemerkungen",
            KinderdossierPrintUtil.extractUntersuchungDurchfuehrung(
                untersuchung,
                u -> trimToPlaceholder(u.getBemerkungen())),
            document);
    }

    private static void addUntersuchungImpfungen(
        @NonNull AnlassUntersuchungPrintDTO anlassPrintDTO,
        @NonNull BasePrinter basePrinter,
        @NonNull Document document
    ) {
        AnlassConfig anlassConfig = anlassPrintDTO.getAnlass().getAnlassConfig();
        if (!isAnyVisible(
            anlassConfig.getImpfungenUebersicht(),
            anlassConfig.getImpfungenListe()
        )
            && !AnlassConfigUtil.showCardImpfungenChecklist(anlassConfig)
            && !AnlassConfigUtil.showCardImpfungDataMinimal(anlassConfig)
        ) {
            return;
        }

        Untersuchung untersuchung = anlassPrintDTO.getUntersuchung();

        basePrinter.addH3Title(
            I18nMessage.of("printer.kinderdossier.anlass.subtitle.ergebnisse.label"),
            document);

        if (AnlassConfigUtil.showCardImpfungenHeader(anlassConfig)) {
            KinderdossierUntersuchungImpfungenUtil.printImpfungenHeader(anlassConfig, untersuchung, basePrinter, document);
        }
        if (AnlassConfigUtil.showCardImpfungDataMinimal(anlassConfig)) {
            KinderdossierUntersuchungImpfungenUtil.printImpfungenDataMinimal(anlassConfig, untersuchung, basePrinter, document);
        }
        if (anlassConfig.getImpfungenUebersicht().isVisible()) {
            KinderdossierUntersuchungImpfungenUtil.printTableImpfstatus(
                anlassPrintDTO.getImpfstatusList(),
                basePrinter,
                document);
        }
        if (AnlassConfigUtil.showCardImpfungenChecklist(anlassConfig)) {
            KinderdossierUntersuchungImpfungenUtil.printImpfungenCheckliste(
                anlassConfig,
                untersuchung,
                basePrinter,
                document);
        }
        if (anlassConfig.isShowImpfungenDoppelkontrolle()) {
            KinderdossierUntersuchungImpfungenUtil.printImpfungenDoppelkontrolle(untersuchung, basePrinter, document);
        }
        if (anlassConfig.getImpfungenListe().isVisible()) {
            KinderdossierUntersuchungImpfungenUtil.printImpfungenTableImpfliste(
                anlassPrintDTO.getImpfungInternList(),
                basePrinter,
                document);
        }
    }

    @NonNull
    private static String anlassTitle(@NonNull Anlass anlass, @NonNull BasePrinter basePrinter) {
        String title = AnlassTitelUtil.getFullAnlassTitel(
            anlass.getTitel(),
            anlass.getSchuljahr(),
            basePrinter.translateValue(anlass.getAnlassConfig().getAnlassTyp()));
        return PDFAPrintUtil.sanitizeForPDFA(title);
    }
}
