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
import java.util.stream.Collectors;

import ch.dvbern.oss.commons.i18nl10n.I18nMessage;
import ch.dvbern.oss.hemed.esc.api.features.anlass.Anlass;
import ch.dvbern.oss.hemed.esc.api.features.anlass.einladung.Einladung;
import ch.dvbern.oss.hemed.esc.api.features.anlass.durchfuehrung.UntersuchungDurchfuehrung;
import ch.dvbern.oss.hemed.esc.api.features.anlass.impfung.UntersuchungImpfstatusKrankheit;
import ch.dvbern.oss.hemed.esc.api.features.anlass.impfung.UntersuchungImpfungIntern;
import ch.dvbern.oss.hemed.esc.api.features.anlass.impfung.UntersuchungImpfungen;
import ch.dvbern.oss.hemed.esc.api.features.anlass.kind.UntersuchungKindDaten;
import ch.dvbern.oss.hemed.esc.api.features.anlass.sehen.UntersuchungSehenBeurteilung;
import ch.dvbern.oss.hemed.esc.api.features.anlass.triage.UntersuchungTriage;
import ch.dvbern.oss.hemed.esc.api.features.anlass.aerztlicheuntersuchung.UntersuchungArztBemerkung;
import ch.dvbern.oss.hemed.esc.api.features.anlass.aerztlicheuntersuchung.UntersuchungArztBeurteilung;
import ch.dvbern.oss.hemed.esc.api.features.anlass.anlassconfig.AnlassConfig;
import ch.dvbern.oss.hemed.esc.api.features.anlass.groessegewicht.UntersuchungGroesseGewicht;
import ch.dvbern.oss.hemed.esc.api.features.anlass.hoeren.Audiogramm;
import ch.dvbern.oss.hemed.esc.api.features.anlass.hoeren.UntersuchungHoerenAudiogramm;
import ch.dvbern.oss.hemed.esc.api.features.anlass.hoeren.UntersuchungHoerenOtoskopie;
import ch.dvbern.oss.hemed.esc.api.features.anlass.massnahmen.UntersuchungImpfungenMassnahmen;
import ch.dvbern.oss.hemed.esc.api.features.anlass.massnahmen.UntersuchungMassnahmen;
import ch.dvbern.oss.hemed.esc.api.features.anlass.einladung.types.EinladungStatus;
import ch.dvbern.oss.hemed.esc.api.features.anlass.impfung.types.ImpfStatus;
import ch.dvbern.oss.hemed.esc.api.shared.anlass.anlassflags.AnlassFlags;
import ch.dvbern.oss.hemed.esc.api.shared.printer.BasePrinter;
import ch.dvbern.oss.hemed.esc.api.shared.printer.PDFAPrintUtil;
import ch.dvbern.oss.hemed.esc.api.shared.printer.PrintUtil;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.element.AreaBreak;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;

import static ch.dvbern.oss.hemed.esc.api.shared.printer.PrintUtil.trimToPlaceholder;
import static ch.dvbern.oss.hemed.esc.api.shared.printer.kinderdossier.KinderdossierPrintUtil.isAnyVisible;

public final class KinderdossierAnlassPrinter {
    private static final String BEMERKUNG_KEY = "printer.kinderdossier.anlass.bemerkungen";

    private KinderdossierAnlassPrinter() {
        // static and hidden so we don't get tempted to add services in here ;)
    }

    public static void addAllAnlass(
        @NonNull List<KinderdossierAnlassPrintDTO> anlassPrintDTOS,
        @NonNull BasePrinter basePrinter,
        @NonNull Document document
    ) {
        List<KinderdossierAnlassPrintDTO> sortedList = anlassPrintDTOS.stream()
            .sorted(Comparator.comparing(o -> o.getEinladung().getAnlass().getDatum()))
            .toList()
            .reversed();

        for (KinderdossierAnlassPrintDTO item : sortedList) {
            addAnlassdaten(item.getEinladung(), basePrinter, document);
            if (item.getEinladung().getEinladungStatus() == EinladungStatus.TEILNAHME_BESTAETIGT) {
                addImpungenUntersuchung(item, basePrinter, document);
                addUntersuchungSehen(item.getEinladung(), basePrinter, document);
                addUntersuchungHoerenAudiogramm(item.getEinladung(), basePrinter, document);
                addUntersuchungHoerenOtoskopie(item.getEinladung(), basePrinter, document);
                addUntersuchungGroesseGewicht(item.getEinladung(), basePrinter, document);
                adduntersuchungTriage(item.getEinladung(), basePrinter, document);
                addUntersuchungArzt(item.getEinladung(), basePrinter, document);
                addUntersuchungImpfungenMassnahmen(item.getEinladung(), basePrinter, document);
                addUntersuchungMassnahmen(item.getEinladung(), basePrinter, document);
            }
        }
    }

    private static void addUntersuchungSehen(
        @NonNull Einladung einladung,
        @NonNull BasePrinter basePrinter,
        @NonNull Document document
    ) {
        AnlassConfig anlassConfig = einladung.getAnlass().getAnlassConfig();
        if (!isAnyVisible(
            anlassConfig.getSehenBrilleLinsen(),
            anlassConfig.getSehenFarbsehen(),
            anlassConfig.getSehenTafel(),
            anlassConfig.getSehenFernvisus(),
            anlassConfig.getSehenStereoTest(),
            anlassConfig.getSehenStrabismus(),
            anlassConfig.getSehenInspektion(),
            anlassConfig.getSehenKonvergenz(),
            anlassConfig.getSehenMotilitaet(),
            anlassConfig.getSehenNahvisus(),
            anlassConfig.getSehenBemerkungen())) {
            return;
        }

        basePrinter.addH3Title(
            I18nMessage.of("printer.kinderdossier.anlass.sehen"),
            document);

        // brilleLinsen
        if (anlassConfig.getSehenBrilleLinsen().isVisible()) {
            basePrinter.addKeyValue(
                "printer.kinderdossier.anlass.brilleLinsen",
                KinderdossierPrintUtil.extractUntersuchungSehen(
                    einladung,
                    untersuchungSehen -> basePrinter.translateValue(untersuchungSehen.getBrilleLinsen())),
                document);
        }

        // stereotest
        if (anlassConfig.getSehenStereoTest().isVisible()) {
            basePrinter.addKeyValue(
                "printer.kinderdossier.anlass.stereotest",
                KinderdossierPrintUtil.extractUntersuchungSehen(
                    einladung,
                    untersuchungSehen -> basePrinter.translateValue(untersuchungSehen.getStereotest())),
                document);
        }

        // farbsehen
        if (anlassConfig.getSehenFarbsehen().isVisible()) {
            basePrinter.addKeyValue(
                "printer.kinderdossier.anlass.farbsehen",
                KinderdossierPrintUtil.extractUntersuchungSehen(
                    einladung,
                    untersuchungSehen -> basePrinter.translateValue(untersuchungSehen.getFarbsehen())),
                document);
        }

        // verwendeteTafel
        if (anlassConfig.getSehenTafel().isVisible()) {
            basePrinter.addKeyValue(
                "printer.kinderdossier.anlass.verwendeteTafel",
                KinderdossierPrintUtil.extractUntersuchungSehen(
                    einladung,
                    untersuchungSehen -> basePrinter.translateValue(untersuchungSehen.getVerwendeteTafel())),
                document);
        }

        if (anlassConfig.getSehenFernvisus().isVisible()) {
            // fernvisus
            basePrinter.addKeyValue(
                "printer.kinderdossier.anlass.fernvisus",
                KinderdossierPrintUtil.extractUntersuchungSehen(
                    einladung,
                    untersuchungSehen -> untersuchungSehen.getFernvisus().stream()
                        .map(basePrinter::translateValue)
                        .collect(Collectors.joining(", "))),
                document);

            // fernvisusRechts
            basePrinter.addKeyValue(
                "printer.kinderdossier.anlass.fernvisusRechts",
                KinderdossierPrintUtil.extractUntersuchungSehen(
                    einladung,
                    untersuchungSehen -> PrintUtil.trimToPlaceholder(untersuchungSehen.getFernvisusRechts())),
                document);

            // fernvisusLinks
            basePrinter.addKeyValue(
                "printer.kinderdossier.anlass.fernvisusLinks",
                KinderdossierPrintUtil.extractUntersuchungSehen(
                    einladung,
                    untersuchungSehen -> PrintUtil.trimToPlaceholder(untersuchungSehen.getFernvisusLinks())),
                document);
        }

        if (anlassConfig.getSehenNahvisus().isVisible()) {
            // nahvisus
            basePrinter.addKeyValue(
                "printer.kinderdossier.anlass.nahvisus",
                KinderdossierPrintUtil.extractUntersuchungSehen(
                    einladung,
                    untersuchungSehen -> untersuchungSehen.getNahvisus().stream()
                        .map(basePrinter::translateValue)
                        .collect(Collectors.joining(", "))),
                document);

            // nahvisusRechts
            basePrinter.addKeyValue(
                "printer.kinderdossier.anlass.nahvisusRechts",
                KinderdossierPrintUtil.extractUntersuchungSehen(
                    einladung,
                    untersuchungSehen -> PrintUtil.trimToPlaceholder(untersuchungSehen.getNahvisusRechts())),
                document);

            // nahvisusLinks
            basePrinter.addKeyValue(
                "printer.kinderdossier.anlass.nahvisusLinks",
                KinderdossierPrintUtil.extractUntersuchungSehen(
                    einladung,
                    untersuchungSehen -> PrintUtil.trimToPlaceholder(untersuchungSehen.getNahvisusLinks())),
                document);
        }

        // motilitaet
        if (anlassConfig.getSehenMotilitaet().isVisible()) {
            basePrinter.addKeyValue(
                "printer.kinderdossier.anlass.motilitaet",
                KinderdossierPrintUtil.extractUntersuchungSehen(
                    einladung,
                    untersuchungSehen -> basePrinter.translateValue(untersuchungSehen.getMotilitaet())),
                document);
        }

        if (anlassConfig.getSehenStrabismus().isVisible()) {
            // strabismus
            basePrinter.addKeyValue(
                "printer.kinderdossier.anlass.strabismus",
                KinderdossierPrintUtil.extractUntersuchungSehen(
                    einladung,
                    untersuchungSehen -> untersuchungSehen.getStrabismus().stream()
                        .map(basePrinter::translateValue)
                        .collect(Collectors.joining(", "))),
                document);

            // strabismusAuspraegung
            basePrinter.addKeyValue(
                "printer.kinderdossier.anlass.strabismusAuspraegung",
                KinderdossierPrintUtil.extractUntersuchungSehen(
                    einladung,
                    untersuchungSehen -> basePrinter.translateValue(untersuchungSehen.getStrabismusAuspraegung())),
                document);

            // strabismusAuftreten
            basePrinter.addKeyValue(
                "printer.kinderdossier.anlass.strabismusAuftreten",
                KinderdossierPrintUtil.extractUntersuchungSehen(
                    einladung,
                    untersuchungSehen -> basePrinter.translateValue(untersuchungSehen.getStrabismusAuftreten())),
                document);
        }

        // inspektion
        if (anlassConfig.getSehenInspektion().isVisible()) {
            basePrinter.addKeyValue(
                "printer.kinderdossier.anlass.inspektion",
                KinderdossierPrintUtil.extractUntersuchungSehen(
                    einladung,
                    untersuchungSehen -> basePrinter.translateValue(untersuchungSehen.getInspektion())),
                document);
        }

        // konvergenz
        if (anlassConfig.getSehenKonvergenz().isVisible()) {
            basePrinter.addKeyValue(
                "printer.kinderdossier.anlass.konvergenz",
                KinderdossierPrintUtil.extractUntersuchungSehen(
                    einladung,
                    untersuchungSehen -> basePrinter.translateValue(untersuchungSehen.getKonvergenz())),
                document);
        }

        // bemerkungen
        if (anlassConfig.getSehenBemerkungen().isVisible()) {
            basePrinter.addVerticalKeyValue(
                BEMERKUNG_KEY,
                KinderdossierPrintUtil.extractUntersuchungSehen(
                    einladung,
                    UntersuchungSehenBeurteilung::getBemerkungen),
                document);
        }
    }

    private static void addUntersuchungHoerenAudiogramm(
        @NonNull Einladung einladung,
        @NonNull BasePrinter basePrinter,
        @NonNull Document document
    ) {
        AnlassConfig anlassConfig = einladung.getAnlass().getAnlassConfig();
        if (!isAnyVisible(
            anlassConfig.getHoerenAudiogramm(),
            anlassConfig.getHoerenAudiogrammBeurteilung(),
            anlassConfig.getHoerenAudiogrammZusaetzlicheAngaben(),
            anlassConfig.getHoerenAudiogrammBemerkungen())
        ) {
            return;
        }
        basePrinter.addH3Title(I18nMessage.of("printer.kinderdossier.anlass.hoeren.audiogramm"), document);

        if (anlassConfig.getHoerenAudiogramm().isVisible()) {
            // audiogrammRechts
            basePrinter.addKeyValue("printer.kinderdossier.anlass.table.audiogrammRechts.label", "", document);
            Audiogramm audiogrammRechts =
                KinderdossierPrintUtil.extractAudiogramm(einladung, UntersuchungHoerenAudiogramm::getAudiogrammRechts);
            addAudiogramTable(audiogrammRechts, basePrinter, document);

            // audiogrammLinks
            basePrinter.addKeyValue("printer.kinderdossier.anlass.table.audiogrammLinks.label", "", document);
            Audiogramm audiogrammLinks =
                KinderdossierPrintUtil.extractAudiogramm(einladung, UntersuchungHoerenAudiogramm::getAudiogrammLinks);
            addAudiogramTable(audiogrammLinks, basePrinter, document);
        }

        // audiogrammBeurteilung
        if (anlassConfig.getHoerenAudiogrammBeurteilung().isVisible()) {
            basePrinter.addKeyValue(
                "printer.kinderdossier.anlass.audiogramm.audiogrammBeurteilung",
                KinderdossierPrintUtil.extractUntersuchungHoerenAudiogramm(
                    einladung,
                    untersuchungHoeren -> untersuchungHoeren.getAudiogrammBeurteilung()
                        .stream()
                        .map(basePrinter::translateValue)
                        .collect(Collectors.joining(", "))),
                document);
        }

        // zusaetzlicheAngaben
        if (anlassConfig.getHoerenAudiogrammZusaetzlicheAngaben().isVisible()) {
            basePrinter.addKeyValue(
                "printer.kinderdossier.anlass.audiogramm.zusaetzlicheAngaben",
                KinderdossierPrintUtil.extractUntersuchungHoerenAudiogramm(
                    einladung,
                    untersuchungHoeren -> untersuchungHoeren.getZusaetzlicheAngaben()
                        .stream()
                        .map(basePrinter::translateValue)
                        .collect(Collectors.joining(", "))),
                document);
        }

        // bemerkungen
        if (anlassConfig.getHoerenAudiogrammBemerkungen().isVisible()) {
            basePrinter.addVerticalKeyValue(
                "printer.kinderdossier.anlass.audiogramm.bemerkungen",
                KinderdossierPrintUtil.extractUntersuchungHoerenAudiogramm(
                    einladung,
                    UntersuchungHoerenAudiogramm::getBemerkungen),
                document);
        }
    }

    private static void addUntersuchungHoerenOtoskopie(
        @NonNull Einladung einladung,
        @NonNull BasePrinter basePrinter,
        @NonNull Document document
    ) {
        AnlassConfig anlassConfig = einladung.getAnlass().getAnlassConfig();
        if (!isAnyVisible(
            anlassConfig.getHoerenOtoskopie(),
            anlassConfig.getHoerenOtoskopieBemerkungen())
        ) {
            return;
        }
        basePrinter.addH3Title(I18nMessage.of("printer.kinderdossier.anlass.hoeren.otoskopie"), document);

        // otoskopieRechts
        if (anlassConfig.getHoerenOtoskopie().isVisible()) {
            basePrinter.addKeyValue(
                "printer.kinderdossier.anlass.otoskopie.otoskopieRechts",
                KinderdossierPrintUtil.extractUntersuchungHoerenOtoskopie(
                    einladung,
                    untersuchungHoeren -> untersuchungHoeren.getOtoskopieRechts()
                        .stream()
                        .map(basePrinter::translateValue)
                        .collect(Collectors.joining(", "))),
                document);

            // otoskopieRechtsResultatAndere
            basePrinter.addKeyValue(
                "printer.kinderdossier.anlass.otoskopie.otoskopieRechtsResultatAndere",
                KinderdossierPrintUtil.extractUntersuchungHoerenOtoskopie(
                    einladung,
                    UntersuchungHoerenOtoskopie::getOtoskopieRechtsResultatAndere),
                document);

            // otoskopieLinks
            basePrinter.addKeyValue(
                "printer.kinderdossier.anlass.otoskopie.otoskopieLinks",
                KinderdossierPrintUtil.extractUntersuchungHoerenOtoskopie(
                    einladung,
                    untersuchungHoeren -> untersuchungHoeren.getOtoskopieLinks()
                        .stream()
                        .map(basePrinter::translateValue)
                        .collect(Collectors.joining(", "))),
                document);

            // otoskopieLinksResultatAndere
            basePrinter.addKeyValue(
                "printer.kinderdossier.anlass.otoskopie.otoskopieLinksResultatAndere",
                KinderdossierPrintUtil.extractUntersuchungHoerenOtoskopie(
                    einladung,
                    UntersuchungHoerenOtoskopie::getOtoskopieLinksResultatAndere),
                document);
        }

        // bemerkungen
        if (anlassConfig.getHoerenOtoskopieBemerkungen().isVisible()) {
            basePrinter.addVerticalKeyValue(
                "printer.kinderdossier.anlass.otoskopie.bemerkungen",
                KinderdossierPrintUtil.extractUntersuchungHoerenOtoskopie(
                    einladung,
                    UntersuchungHoerenOtoskopie::getBemerkungen),
                document);
        }
    }

    private static void addUntersuchungGroesseGewicht(
        @NonNull Einladung einladung,
        @NonNull BasePrinter basePrinter,
        @NonNull Document document
    ) {
        AnlassConfig anlassConfig = einladung.getAnlass().getAnlassConfig();
        if (!isAnyVisible(
            anlassConfig.getGroesseGewichtGroesse(),
            anlassConfig.getGroesseGewichtGewicht(),
            anlassConfig.getGroesseGewichtBmi(),
            anlassConfig.getGroesseGewichtGroessePerzentile(),
            anlassConfig.getGroesseGewichtGewichtPerzentile(),
            anlassConfig.getGroesseGewichtBmiPerzentile(),
            anlassConfig.getGroesseGewichtBemerkungen())) {
            return;
        }
        basePrinter.addH3Title(I18nMessage.of("printer.kinderdossier.anlass.grosse"), document);

        // alter
        basePrinter.addKeyValue(
            "printer.kinderdossier.anlass.alter",
            KinderdossierPrintUtil.extractUntersuchungGroesseGewicht(
                einladung,
                groesseGewicht -> PrintUtil.trimToPlaceholder(groesseGewicht.getAlter())),
            document);

        // groesse
        if (anlassConfig.getGroesseGewichtGroesse().isVisible()) {
            basePrinter.addKeyValue(
                "printer.kinderdossier.anlass.groesse",
                KinderdossierPrintUtil.extractUntersuchungGroesseGewicht(
                    einladung,
                    groesseGewicht -> PrintUtil.trimToPlaceholder(groesseGewicht.getGroesse())),
                document);
        }

        // groessePerzentile
        if (anlassConfig.getGroesseGewichtGroessePerzentile().isVisible()) {
            basePrinter.addKeyValue(
                "printer.kinderdossier.anlass.groessePerzentile",
                KinderdossierPrintUtil.extractUntersuchungGroesseGewicht(
                    einladung,
                    groesseGewicht -> basePrinter.translateValue(groesseGewicht.getGroessePerzentile())),
                document);
        }

        // gewicht
        if (anlassConfig.getGroesseGewichtGewicht().isVisible()) {
            basePrinter.addKeyValue(
                "printer.kinderdossier.anlass.gewicht",
                KinderdossierPrintUtil.extractUntersuchungGroesseGewicht(
                    einladung,
                    groesseGewicht -> PrintUtil.trimToPlaceholder(groesseGewicht.getGewicht())),
                document);
        }

        // gewichtPerzentile
        if (anlassConfig.getGroesseGewichtGewichtPerzentile().isVisible()) {
            basePrinter.addKeyValue(
                "printer.kinderdossier.anlass.gewichtPerzentile",
                KinderdossierPrintUtil.extractUntersuchungGroesseGewicht(
                    einladung,
                    groesseGewicht -> basePrinter.translateValue(groesseGewicht.getGewichtPerzentile())),
                document);
        }

        // bmi
        if (anlassConfig.getGroesseGewichtBmi().isVisible()) {
            basePrinter.addKeyValue(
                "printer.kinderdossier.anlass.bmi",
                KinderdossierPrintUtil.extractUntersuchungGroesseGewicht(
                    einladung,
                    groesseGewicht -> PrintUtil.trimToPlaceholder(groesseGewicht.getBmi())),
                document);
        }

        // bmiPerzentile
        if (anlassConfig.getGroesseGewichtBmiPerzentile().isVisible()) {
            basePrinter.addKeyValue(
                "printer.kinderdossier.anlass.bmiPerzentile",
                KinderdossierPrintUtil.extractUntersuchungGroesseGewicht(
                    einladung,
                    groesseGewicht -> basePrinter.translateValue(groesseGewicht.getBmiPerzentile())),
                document);
        }

        // bemerkungen
        if (anlassConfig.getGroesseGewichtBemerkungen().isVisible()) {
            basePrinter.addVerticalKeyValue(
                BEMERKUNG_KEY,
                KinderdossierPrintUtil.extractUntersuchungGroesseGewicht(
                    einladung,
                    UntersuchungGroesseGewicht::getBemerkungen),
                document);
        }
    }

    private static void addUntersuchungArzt(
        @NonNull Einladung einladung,
        @NonNull BasePrinter basePrinter,
        @NonNull Document document
    ) {
        AnlassConfig anlassConfig = einladung.getAnlass().getAnlassConfig();
        if (!isAnyVisible(
            anlassConfig.getUntersuchungBewegungsapparat(),
            anlassConfig.getUntersuchungEinbeinstand(),
            anlassConfig.getUntersuchungEinbeinhuepfen(),
            anlassConfig.getUntersuchungFingeropposition(),
            anlassConfig.getUntersuchungMotorik(),
            anlassConfig.getUntersuchungSprache(),
            anlassConfig.getUntersuchungMundZaehne(),
            anlassConfig.getUntersuchungHaut(),
            anlassConfig.getUntersuchungHerz(),
            anlassConfig.getUntersuchungBlutdruck(),
            anlassConfig.getUntersuchungVerhalten(),
            anlassConfig.getUntersuchungBemerkungen())) {
            return;
        }
        basePrinter.addH3Title(I18nMessage.of("printer.kinderdossier.anlass.untersuchungArzt"), document);

        // verhalten
        if (anlassConfig.getUntersuchungVerhalten().isVisible()) {
            basePrinter.addKeyValue(
                "printer.kinderdossier.anlass.verhalten",
                KinderdossierPrintUtil.extractUntersuchungArzt(
                    einladung,
                    untersuchungArzt -> basePrinter.translateValue(untersuchungArzt.getVerhalten())),
                document);
        }

        // blutdruck
        if (anlassConfig.getUntersuchungBlutdruck().isVisible()) {
            basePrinter.addKeyValue(
                "printer.kinderdossier.anlass.blutdruck",
                KinderdossierPrintUtil.extractUntersuchungArzt(
                    einladung,
                    untersuchungArzt -> basePrinter.translateValue(untersuchungArzt.getBlutdruck())),
                document);

            // blutdruckSystolisch
            basePrinter.addKeyValue(
                "printer.kinderdossier.anlass.blutdruckSystolisch",
                KinderdossierPrintUtil.extractUntersuchungArzt(
                    einladung,
                    untersuchungArzt -> PrintUtil.trimToPlaceholder(untersuchungArzt.getBlutdruckSystolisch())),
                document);

            // blutdruckDiastolisch
            basePrinter.addKeyValue(
                "printer.kinderdossier.anlass.blutdruckDiastolisch",
                KinderdossierPrintUtil.extractUntersuchungArzt(
                    einladung,
                    untersuchungArzt -> PrintUtil.trimToPlaceholder(untersuchungArzt.getBlutdruckDiastolisch())),
                document);
        }

        // herzauskultation
        if (anlassConfig.getUntersuchungHerz().isVisible()) {
            basePrinter.addKeyValue(
                "printer.kinderdossier.anlass.herzauskultation",
                KinderdossierPrintUtil.extractUntersuchungArzt(
                    einladung,
                    untersuchungArzt -> basePrinter.translateValue(untersuchungArzt.getHerzauskultation())),
                document);

            // herzauskultationBemerkung
            basePrinter.addKeyValue(
                "printer.kinderdossier.anlass.herzauskultationBemerkung",
                KinderdossierPrintUtil.extractUntersuchungArzt(
                    einladung,
                    UntersuchungArztBeurteilung::getHerzauskultationBemerkung),
                document);
        }

        // haut
        if (anlassConfig.getUntersuchungHaut().isVisible()) {
            basePrinter.addKeyValue(
                "printer.kinderdossier.anlass.haut",
                KinderdossierPrintUtil.extractUntersuchungArzt(
                    einladung,
                    untersuchungArzt -> basePrinter.translateValue(untersuchungArzt.getHaut())),
                document);

            // hautBemerkung
            basePrinter.addKeyValue(
                "printer.kinderdossier.anlass.hautBemerkung",
                KinderdossierPrintUtil.extractUntersuchungArzt(
                    einladung,
                    UntersuchungArztBeurteilung::getHautBemerkung),
                document);
        }

        // mundAndZaehne
        if (anlassConfig.getUntersuchungMundZaehne().isVisible()) {
            basePrinter.addKeyValue(
                "printer.kinderdossier.anlass.mundAndZaehne",
                KinderdossierPrintUtil.extractUntersuchungArzt(
                    einladung,
                    untersuchungArzt -> untersuchungArzt.getMundAndZaehne()
                        .stream()
                        .map(basePrinter::translateValue)
                        .collect(Collectors.joining(", "))),
                document);

            // mundAndZaehneBemerkung
            basePrinter.addKeyValue(
                "printer.kinderdossier.anlass.mundAndZaehneBemerkung",
                KinderdossierPrintUtil.extractUntersuchungArzt(
                    einladung,
                    UntersuchungArztBeurteilung::getMundAndZaehneBemerkung),
                document);
        }

        // sprachfaehigkeit
        if (anlassConfig.getUntersuchungSprache().isVisible()) {
            basePrinter.addKeyValue(
                "printer.kinderdossier.anlass.sprachfaehigkeit",
                KinderdossierPrintUtil.extractUntersuchungArzt(
                    einladung,
                    untersuchungArzt -> untersuchungArzt.getSprachfaehigkeit()
                        .stream()
                        .map(basePrinter::translateValue)
                        .collect(Collectors.joining(", "))),
                document);

            // sprachfaehigkeitBemerkung
            basePrinter.addKeyValue(
                "printer.kinderdossier.anlass.sprachfaehigkeitBemerkung",
                KinderdossierPrintUtil.extractUntersuchungArzt(
                    einladung,
                    UntersuchungArztBeurteilung::getSprachfaehigkeitBemerkung),
                document);
        }

        // motorik
        if (anlassConfig.getUntersuchungMotorik().isVisible()) {
            basePrinter.addKeyValue(
                "printer.kinderdossier.anlass.motorik",
                KinderdossierPrintUtil.extractUntersuchungArzt(
                    einladung,
                    untersuchungArzt -> basePrinter.translateValue(untersuchungArzt.getMotorik())),
                document);
        }

        // bewegungsapparat
        if (anlassConfig.getUntersuchungBewegungsapparat().isVisible()) {
            basePrinter.addKeyValue(
                "printer.kinderdossier.anlass.bewegungsapparat",
                KinderdossierPrintUtil.extractUntersuchungArzt(
                    einladung,
                    untersuchungArzt -> untersuchungArzt.getBewegungsapparat()
                        .stream()
                        .map(basePrinter::translateValue)
                        .collect(Collectors.joining(", "))),
                document);

            // bewegungsapparatBemerkung
            basePrinter.addKeyValue(
                "printer.kinderdossier.anlass.bewegungsapparatBemerkung",
                KinderdossierPrintUtil.extractUntersuchungArzt(
                    einladung,
                    UntersuchungArztBeurteilung::getBewegungsapparatBemerkung),
                document);
        }

        // einbeinstand
        if (anlassConfig.getUntersuchungEinbeinstand().isVisible()) {
            basePrinter.addKeyValue(
                "printer.kinderdossier.anlass.einbeinstand",
                KinderdossierPrintUtil.extractUntersuchungArzt(
                    einladung,
                    untersuchungArzt -> basePrinter.translateValue(untersuchungArzt.getEinbeinstand())),
                document);

            // einbeinstandBemerkung
            basePrinter.addKeyValue(
                "printer.kinderdossier.anlass.einbeinstandBemerkung",
                KinderdossierPrintUtil.extractUntersuchungArzt(
                    einladung,
                    UntersuchungArztBeurteilung::getEinbeinhuepfenBemerkung),
                document);
        }

        // einbeinhuepfen
        if (anlassConfig.getUntersuchungEinbeinhuepfen().isVisible()) {
            basePrinter.addKeyValue(
                "printer.kinderdossier.anlass.einbeinhuepfen",
                KinderdossierPrintUtil.extractUntersuchungArzt(
                    einladung,
                    untersuchungArzt -> basePrinter.translateValue(untersuchungArzt.getEinbeinhuepfen())),
                document);

            // einbeinhuepfenBemerkung
            basePrinter.addKeyValue(
                "printer.kinderdossier.anlass.einbeinhuepfenBemerkung",
                KinderdossierPrintUtil.extractUntersuchungArzt(
                    einladung,
                    UntersuchungArztBeurteilung::getEinbeinhuepfenBemerkung),
                document);
        }

        // fingeropposition
        if (anlassConfig.getUntersuchungFingeropposition().isVisible()) {
            basePrinter.addKeyValue(
                "printer.kinderdossier.anlass.fingeropposition",
                KinderdossierPrintUtil.extractUntersuchungArzt(
                    einladung,
                    untersuchungArzt -> untersuchungArzt.getFingeropposition()
                        .stream()
                        .map(basePrinter::translateValue)
                        .collect(Collectors.joining(", "))),
                document);

            // fingeroppositionBemerkung
            basePrinter.addKeyValue(
                "printer.kinderdossier.anlass.fingeroppositionBemerkung",
                KinderdossierPrintUtil.extractUntersuchungArzt(
                    einladung,
                    UntersuchungArztBeurteilung::getFingeroppositionBemerkung),
                document);
        }

        // bemerkungen
        if (anlassConfig.getUntersuchungBemerkungen().isVisible()) {
            basePrinter.addVerticalKeyValue(
                BEMERKUNG_KEY,
                KinderdossierPrintUtil.extractUntersuchungArztBemerkung(
                    einladung,
                    UntersuchungArztBemerkung::getBemerkungen),
                document);
        }
    }

    private static void addUntersuchungImpfungenMassnahmen(
        @NonNull Einladung einladung,
        @NonNull BasePrinter basePrinter,
        @NonNull Document document
    ) {
        AnlassConfig anlassConfig = einladung.getAnlass().getAnlassConfig();
        if (!(anlassConfig.isImpfungenMassnahmeInfoKinderarzt()
            || anlassConfig.isImpfungenMassnahmeBeratungMPA()
            || anlassConfig.isImpfungenMassnahmeInfoEltern()
            || anlassConfig.isImpfungenMassnahmeRueckfrageKinderarzt())) {
            return;
        }

        basePrinter.addH3Title(I18nMessage.of("printer.kinderdossier.anlass.massnahmen"), document);

        // keineMassnahmen
        basePrinter.addKeyValue(
            "printer.kinderdossier.anlass.keineImpfMassnahmen",
            KinderdossierPrintUtil.extractUntersuchungImpfungenMassnahmenBooleanFn(
                einladung,
                UntersuchungImpfungenMassnahmen::isKeineMassnahmen),
            document);

        // impfberatungMpaDurchgefuehrt
        if (anlassConfig.isImpfungenMassnahmeBeratungMPA()) {
            basePrinter.addKeyValue(
                "printer.kinderdossier.anlass.impfberatungMpaDurchgefuehrt",
                KinderdossierPrintUtil.extractUntersuchungImpfungenMassnahmenBooleanFn(
                    einladung,
                    UntersuchungImpfungenMassnahmen::isImpfberatungMpaDurchgefuehrt),
                document);
        }

        // informationEltern
        if (anlassConfig.isImpfungenMassnahmeInfoEltern()) {
            basePrinter.addKeyValue(
                "printer.kinderdossier.anlass.informationImpfenEltern",
                KinderdossierPrintUtil.extractUntersuchungImpfungenMassnahmen(
                    einladung,
                    untersuchungImpfungenMassnahmen -> basePrinter.translateValue(untersuchungImpfungenMassnahmen.getInformationEltern())),
                document);
        }

        // informationHausarzt
        if (anlassConfig.isImpfungenMassnahmeInfoKinderarzt()) {
            basePrinter.addKeyValue(
                "printer.kinderdossier.anlass.informationHausarzt",
                KinderdossierPrintUtil.extractUntersuchungImpfungenMassnahmenBooleanFn(
                    einladung,
                    UntersuchungImpfungenMassnahmen::isInformationHausarzt),
                document);
        }

        // rueckfrageHausarzt
        if (anlassConfig.isImpfungenMassnahmeRueckfrageKinderarzt()) {
            basePrinter.addKeyValue(
                "printer.kinderdossier.anlass.rueckfrageHausarzt",
                KinderdossierPrintUtil.extractUntersuchungImpfungenMassnahmenBooleanFn(
                    einladung,
                    UntersuchungImpfungenMassnahmen::isRueckfrageHausarzt),
                document);
        }

        // bemerkungen
        basePrinter.addVerticalKeyValue(
            BEMERKUNG_KEY,
            KinderdossierPrintUtil.extractUntersuchungImpfungenMassnahmen(
                einladung,
                UntersuchungImpfungenMassnahmen::getBemerkungen),
            document);
    }

    private static void addUntersuchungMassnahmen(
        @NonNull Einladung einladung,
        @NonNull BasePrinter basePrinter,
        @NonNull Document document
    ) {
        AnlassConfig anlassConfig = einladung.getAnlass().getAnlassConfig();
        if (noMassnahmeConfigured(anlassConfig)) {
            return;
        }

        basePrinter.addH3Title(I18nMessage.of("printer.kinderdossier.anlass.generelleMassnahmen"), document);

        // keineMassnahmen
        if (anlassConfig.isShowMassnahmenKeine()) {
            basePrinter.addKeyValue(
                "printer.kinderdossier.anlass.keineMassnahmen",
                KinderdossierPrintUtil.extractUntersuchungMassnahmenBooleanFn(
                    einladung,
                    UntersuchungMassnahmen::isKeineMassnahmen),
                document);
        }

        //beratungMPA
        if (anlassConfig.isShowMassnahmenBeratungMpa()) {
            basePrinter.addKeyValue(
                "printer.kinderdossier.anlass.beratungMPA",
                KinderdossierPrintUtil.extractUntersuchungMassnahmenBooleanFn(
                    einladung,
                    UntersuchungMassnahmen::isBeratungMPA),
                document);
        }

        //beratungSchulAerztin
        if (anlassConfig.isShowMassnahmenBeratungSchulAerztin()) {
            basePrinter.addKeyValue(
                "printer.kinderdossier.anlass.beratungSchulAerztin",
                KinderdossierPrintUtil.extractUntersuchungMassnahmenBooleanFn(
                    einladung,
                    UntersuchungMassnahmen::isBeratungSchulAerztin),
                document);
        }

        // anderes
        if (anlassConfig.isShowMassnahmenAnderes()) {
            basePrinter.addKeyValue(
                "printer.kinderdossier.anlass.anderes",
                KinderdossierPrintUtil.extractUntersuchungMassnahmenBooleanFn(
                    einladung,
                    UntersuchungMassnahmen::isAnderes),
                document);

            // anderesBemerkung
            basePrinter.addKeyValue(
                "printer.kinderdossier.anlass.anderesBemerkung",
                KinderdossierPrintUtil.extractUntersuchungMassnahmen(
                    einladung,
                    untersuchungMassnahmen -> PrintUtil.trimToPlaceholder(untersuchungMassnahmen.getAnderesBemerkung())),
                document);
        }

        if (anlassConfig.isShowMassnahmenBereitsBehandelt()) {
            // Bereits Behandelt init table
            addBereitsBehandeltTable(einladung, basePrinter, document);

            // bereitsBehandeltAnderesBemerkung
            basePrinter.addKeyValue(
                "printer.kinderdossier.anlass.bereitsBehandeltAnderesBemerkung",
                KinderdossierPrintUtil.extractUntersuchungMassnahmen(
                    einladung,
                    UntersuchungMassnahmen::getBereitsBehandeltAnderesBemerkung),
                document);
        }

        // informationEltern
        if (anlassConfig.isShowMassnahmenInfoEltern()) {
            basePrinter.addKeyValue(
                "printer.kinderdossier.anlass.informationenEltern",
                KinderdossierPrintUtil.extractUntersuchungMassnahmen(
                    einladung,
                    untersuchungMassnahmen
                        -> basePrinter.translateValue(untersuchungMassnahmen.getInformationEltern())),
                document);
        }

        // abgegebenesInfomaterial
        if (anlassConfig.isShowMassnahmenInfomaterial()) {
            basePrinter.addKeyValue(
                "printer.kinderdossier.anlass.abgegebenesInfomaterial",
                KinderdossierPrintUtil.extractUntersuchungMassnahmen(
                    einladung,
                    untersuchungMassnahmen -> untersuchungMassnahmen.getAbgegebenesInfomaterial()
                        .stream()
                        .map(item -> basePrinter.getTranslatedString(item.getName()))
                        .collect(Collectors.joining(", "))),
                document);
        }

        // ueberweisungHausarzt
        if (anlassConfig.isShowMassnahmenUeberweisungHausarzt()) {
            basePrinter.addKeyValue(
                "printer.kinderdossier.anlass.ueberweisungHausarzt",
                KinderdossierPrintUtil.extractUntersuchungMassnahmenBooleanFn(
                    einladung,
                    UntersuchungMassnahmen::isUeberweisungHausarzt),
                document);
        }

        // ueberweisungAugenarzt
        if (anlassConfig.isShowMassnahmenUeberweisungAugenarzt()) {
            basePrinter.addKeyValue(
                "printer.kinderdossier.anlass.ueberweisungAugenarzt",
                KinderdossierPrintUtil.extractUntersuchungMassnahmenBooleanFn(
                    einladung,
                    UntersuchungMassnahmen::isUeberweisungAugenarzt),
                document);
        }

        // bemerkungen
        basePrinter.addVerticalKeyValue(
            BEMERKUNG_KEY,
            KinderdossierPrintUtil.extractUntersuchungMassnahmen(einladung, UntersuchungMassnahmen::getBemerkungen),
            document);

        // nachkontrolle
        if (anlassConfig.isShowMassnahmenNachkontrolleSaed()) {
            addNachkontrolleTable(einladung, basePrinter, document);
        }
    }

    private static boolean noMassnahmeConfigured(AnlassConfig anlassConfig) {
        return !(anlassConfig.isShowMassnahmenKeine()
            || anlassConfig.isShowMassnahmenBeratungMpa()
            || anlassConfig.isShowMassnahmenBereitsBehandelt()
            || anlassConfig.isShowMassnahmenInfomaterial()
            || anlassConfig.isShowMassnahmenInfoEltern()
            || anlassConfig.isShowMassnahmenUeberweisungHausarzt()
            || anlassConfig.isShowMassnahmenUeberweisungAugenarzt()
            || anlassConfig.isShowMassnahmenNachkontrolleSaed()
            || anlassConfig.isShowMassnahmenAnderes());
    }

    private static void addNachkontrolleTable(
        @NonNull Einladung einladung,
        @NonNull BasePrinter basePrinter,
        @NonNull Document document) {
        basePrinter.addKeyValue(
            "printer.kinderdossier.anlass.nachkontrolle",
            "",
            document);

        AnlassFlags nachkontrolle =
            KinderdossierPrintUtil.extractAnlassFlags(einladung, UntersuchungMassnahmen::getNachkontrolle);

        basePrinter.addTable(
            List.of(
                I18nMessage.of("printer.kinderdossier.anlass.table.sehen.label"),
                I18nMessage.of("printer.kinderdossier.anlass.table.hoeren.label"),
                I18nMessage.of("printer.kinderdossier.anlass.table.grosse.label"),
                I18nMessage.of("printer.kinderdossier.anlass.table.sprache.label"),
                I18nMessage.of("printer.kinderdossier.anlass.table.motorik.label"),
                I18nMessage.of("printer.kinderdossier.anlass.table.anderer.label")),
            List.of(List.of(
                basePrinter.translateValue(nachkontrolle == null ?
                    null :
                    nachkontrolle.isNachkontrolleSehen()),
                basePrinter.translateValue(nachkontrolle == null ? null : nachkontrolle.isNachkontrolleHoeren()),
                basePrinter.translateValue(nachkontrolle == null ?
                    null :
                    nachkontrolle.isNachkontrolleGroesseGewicht()),
                basePrinter.translateValue(nachkontrolle == null ? null : nachkontrolle.isNachkontrolleSprache()),
                basePrinter.translateValue(nachkontrolle == null ? null : nachkontrolle.isNachkontrolleMotorik()),
                basePrinter.translateValue(nachkontrolle == null ? null : nachkontrolle.isNachkontrolleAnderes()))),
            document,
            true);
    }

    private static void addBereitsBehandeltTable(
        @NonNull Einladung einladung,
        @NonNull BasePrinter basePrinter,
        @NonNull Document document) {
        basePrinter.addKeyValue(
            "printer.kinderdossier.anlass.bereitsBehandelt",
            "",
            document);
        basePrinter.addTable(
            List.of(
                I18nMessage.of("printer.kinderdossier.anlass.bereitsBehandeltSehen"),
                I18nMessage.of("printer.kinderdossier.anlass.bereitsBehandeltHoeren"),
                I18nMessage.of("printer.kinderdossier.anlass.bereitsBehandeltGroesseGewicht"),
                I18nMessage.of("printer.kinderdossier.anlass.bereitsBehandeltSprache"),
                I18nMessage.of("printer.kinderdossier.anlass.bereitsBehandeltMotorik"),
                I18nMessage.of("printer.kinderdossier.anlass.bereitsBehandeltAnderes")),
            List.of(
                List.of(
                    basePrinter.translateValue(
                        KinderdossierPrintUtil.extractUntersuchungMassnahmenBooleanFn(
                            einladung,
                            UntersuchungMassnahmen::isBereitsBehandeltSehen)),
                    basePrinter.translateValue(
                        KinderdossierPrintUtil.extractUntersuchungMassnahmenBooleanFn(
                            einladung,
                            UntersuchungMassnahmen::isBereitsBehandeltHoeren)),
                    basePrinter.translateValue(
                        KinderdossierPrintUtil.extractUntersuchungMassnahmenBooleanFn(
                            einladung,
                            UntersuchungMassnahmen::isBereitsBehandeltGroesseGewicht)),
                    basePrinter.translateValue(
                        KinderdossierPrintUtil.extractUntersuchungMassnahmenBooleanFn(
                            einladung,
                            UntersuchungMassnahmen::isBereitsBehandeltSprache)),
                    basePrinter.translateValue(
                        KinderdossierPrintUtil.extractUntersuchungMassnahmenBooleanFn(
                            einladung,
                            UntersuchungMassnahmen::isBereitsBehandeltMotorik)),
                    basePrinter.translateValue(
                        KinderdossierPrintUtil.extractUntersuchungMassnahmenBooleanFn(
                            einladung,
                            UntersuchungMassnahmen::isBereitsBehandeltAnderes)
                    )
                )
            ),
            document,
            true);
    }

    private static void addAnlassdaten(
        @NonNull Einladung einladung,
        @NonNull BasePrinter basePrinter,
        @NonNull Document document
    ) {
        AnlassConfig anlassConfig = einladung.getAnlass().getAnlassConfig();
        if (!(isAnyVisible(anlassConfig.getDatenUntersuchung())
            || anlassConfig.isShowDatenSchule())) {
            return;
        }
        document.add(new AreaBreak());

        basePrinter.addH2Title(
            I18nMessage.of(
                "printer.kinderdossier.anlass.subtitle.anlass.label",
                "date",
                trimToPlaceholder(einladung.getAnlass().getDatum()),
                "title",
                anlassTitle(einladung.getAnlass(), basePrinter)),
            document);
        basePrinter.addH3Title(
            I18nMessage.of("printer.kinderdossier.anlass.subtitle.anlassdaten.label"),
            document);

        basePrinter.addKeyValue(
            "printer.kinderdossier.anlass.anlass-typ",
            einladung.getAnlass().getAnlassConfig().getAnlassTyp(),
            document);

        if (anlassConfig.isShowDatenSchule()) {
            basePrinter.addKeyValue(
                "printer.kinderdossier.anlass.schule",
                KinderdossierPrintUtil.extractUntersuchungKindDaten(
                    einladung,
                    daten -> String.format(
                        "%s %s",
                        trimToPlaceholder(daten.getSchulstandort()),
                        trimToPlaceholder(daten.getSchulhaus()))),
                document);

            basePrinter.addKeyValue(
                "printer.kinderdossier.anlass.klasse",
                KinderdossierPrintUtil.extractUntersuchungKindDaten(einladung, UntersuchungKindDaten::getKlasse),
                document);
        }

        basePrinter.addKeyValue(
            "printer.kinderdossier.anlass.einladungStatus",
            basePrinter.translateValue(einladung.getEinladungStatus()),
            document);

        if (einladung.getEinladungStatus() == EinladungStatus.TEILNAHME_ABGESAGT) {
            basePrinter.addKeyValue(
                "printer.kinderdossier.anlass.grundAbmeldung",
                einladung.getGrundAbmeldung(),
                document);
        }

        if (anlassConfig.getDatenUntersuchung().isVisible()) {
            basePrinter.addKeyValue(
                "printer.kinderdossier.anlass.nachkontrolle",
                KinderdossierPrintUtil.extractUntersuchungDurchfuehrungBooleanFn(
                    einladung,
                    UntersuchungDurchfuehrung::isNachkontrolle),
                document);
            basePrinter.addKeyValue(
                "printer.kinderdossier.anlass.nachzugler",
                KinderdossierPrintUtil.extractUntersuchungDurchfuehrungBooleanFn(
                    einladung,
                    UntersuchungDurchfuehrung::isNachzuegler),
                document);
            basePrinter.addKeyValue(
                "printer.kinderdossier.anlass.neueintritt",
                KinderdossierPrintUtil.extractUntersuchungDurchfuehrungBooleanFn(
                    einladung,
                    UntersuchungDurchfuehrung::isNeueintritt),
                document);

            basePrinter.addKeyValue(
                "printer.kinderdossier.anlass.arzt",
                trimToPlaceholder(einladung.getAnlass().getArzt()),
                document);
            basePrinter.addKeyValue(
                "printer.kinderdossier.anlass.mpa",
                trimToPlaceholder(einladung.getAnlass().getMpa()),
                document);
        }

        basePrinter.addKeyValue(
            "printer.kinderdossier.anlass.weiterePerson",
            trimToPlaceholder(einladung.getAnlass().getWeiterePerson()),
            document);

        basePrinter.addKeyValue(
            "printer.kinderdossier.anlass.durchfuehrung.arzt",
            KinderdossierPrintUtil.extractUntersuchungDurchfuehrung(
                einladung,
                u -> trimToPlaceholder(u.getArzt())),
            document);
        basePrinter.addKeyValue(
            "printer.kinderdossier.anlass.durchfuehrung.mpa",
            KinderdossierPrintUtil.extractUntersuchungDurchfuehrung(
                einladung,
                u -> trimToPlaceholder(u.getMpa())),
            document);
    }

    private static void addImpungenUntersuchung(
        @NonNull KinderdossierAnlassPrintDTO anlassPrintDTO,
        @NonNull BasePrinter basePrinter,
        @NonNull Document document
    ) {
        AnlassConfig anlassConfig = anlassPrintDTO.getEinladung().getAnlass().getAnlassConfig();
        if (!isAnyVisible(
            anlassConfig.getImpfungenUebersicht(),
            anlassConfig.getImpfungenListe(),
            anlassConfig.getCheckKontraindikationenAbgeklaert(),
            anlassConfig.getCheckDoppelkontrolleImpfentscheidDurchgefuehrt(),
            anlassConfig.getCheckEinwilligungVorhanden())) {
            return;
        }

        Einladung einladung = anlassPrintDTO.getEinladung();

        basePrinter.addH3Title(
            I18nMessage.of("printer.kinderdossier.anlass.subtitle.ergebnisse.label"),
            document);
        basePrinter.addKeyValue(
            "printer.kinderdossier.anlass.impfausweisUnvollstaendig",
            KinderdossierPrintUtil.extractUntersuchungImpfungenBooleanFn(
                einladung,
                UntersuchungImpfungen::isImpfausweisUnvollstaendig),
            document);
        basePrinter.addKeyValue(
            "printer.kinderdossier.anlass.impfausweisNichtVorhanden",
            KinderdossierPrintUtil.extractUntersuchungImpfungenBooleanFn(
                einladung,
                UntersuchungImpfungen::isImpfausweisNichtVorhanden),
            document);
        basePrinter.addKeyValue(
            "printer.kinderdossier.anlass.impfausweisZweiPlusEinsSchema",
            KinderdossierPrintUtil.extractUntersuchungImpfungenBooleanFn(
                einladung,
                UntersuchungImpfungen::isImpfausweisZweiPlusEinsSchema),
            document);

        if (anlassConfig.getImpfungenUebersicht().isVisible()) {
            addTableImpfstatus(anlassPrintDTO.getImpfstatusList(), basePrinter, document);
        }

        if (anlassConfig.getCheckEinwilligungVorhanden().isVisible()) {
            basePrinter.addKeyValue(
                "printer.kinderdossier.anlass.checkEinwilligungVorhanden",
                KinderdossierPrintUtil.extractUntersuchungImpfungenBooleanFn(
                    einladung,
                    UntersuchungImpfungen::isCheckEinwilligungVorhanden),
                document);
        }

        if (anlassConfig.getCheckKontraindikationenAbgeklaert().isVisible()) {
            basePrinter.addKeyValue(
                "printer.kinderdossier.anlass.checkKontraindikationenAbgeklaert",
                KinderdossierPrintUtil.extractUntersuchungImpfungenBooleanFn(
                    einladung,
                    UntersuchungImpfungen::isCheckKontraindikationenAbgeklaert),
                document);
        }

        if (anlassConfig.getCheckDoppelkontrolleImpfentscheidDurchgefuehrt().isVisible()) {
            basePrinter.addKeyValue(
                "printer.kinderdossier.anlass.checkDoppelkontrolleImpfentscheidDurchgefuehrt",
                KinderdossierPrintUtil.extractUntersuchungImpfungenBooleanFn(
                    einladung,
                    UntersuchungImpfungen::isCheckDoppelkontrolleImpfentscheidDurchgefuehrt),
                document);
        }

        basePrinter.addKeyValue(
            "printer.kinderdossier.anlass.doppelkontrolle.visum1",
            KinderdossierPrintUtil.extractUntersuchungImpfungen(
                einladung,
                ui -> trimToPlaceholder(ui.getVisum1() != null ? ui.getVisum1().getPerson() : null)),
            document);
        basePrinter.addKeyValue(
            "printer.kinderdossier.anlass.doppelkontrolle.visum2",
            KinderdossierPrintUtil.extractUntersuchungImpfungen(
                einladung,
                ui -> trimToPlaceholder(ui.getVisum2() != null ? ui.getVisum2().getPerson() : null)),
            document);

        if (anlassConfig.getImpfungenListe().isVisible()) {
            addTableImpfliste(anlassPrintDTO.getImpfungInternList(), basePrinter, document);
        }
    }

    private static void adduntersuchungTriage(
        @NonNull Einladung einladung,
        @NonNull BasePrinter basePrinter,
        @NonNull Document document
    ) {
        AnlassConfig anlassConfig = einladung.getAnlass().getAnlassConfig();
        if (!(anlassConfig.isShowTriageTriage())) {
            return;
        }

        if (einladung.getUntersuchung() != null && einladung.getUntersuchung().getUntersuchungTriage() != null) {
            basePrinter.addH3Title(I18nMessage.of("printer.kinderdossier.anlass.subtitle.triage.label"), document);

            UntersuchungTriage untersuchungTriage = einladung.getUntersuchung().getUntersuchungTriage();

            addTriageRemark(
                "triage-su",
                untersuchungTriage.isTriageSu(),
                untersuchungTriage.getTriageSuBemerkungen(),
                basePrinter,
                document);
            addTriageRemark(
                "triage-mpa",
                untersuchungTriage.isTriageMpa(),
                untersuchungTriage.getTriageMpaBemerkungen(),
                basePrinter,
                document);
            addTriageRemark(
                "befund-su",
                untersuchungTriage.isBefundSu(),
                untersuchungTriage.getBefundSuBemerkungen(),
                basePrinter,
                document);
            addTriageRemark(
                "wunsch-schueler",
                untersuchungTriage.isWunschSchueler(),
                untersuchungTriage.getWunschSchuelerBemerkungen(),
                basePrinter,
                document);
            addTriageRemark(
                "vorinformation-lehrperson",
                untersuchungTriage.isVorinformationLehrperson(),
                untersuchungTriage.getVorinformationLehrpersonBemerkungen(),
                basePrinter,
                document);
        }
    }

    private static void addTableImpfstatus(
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
                .map(untersuchungImpfstatusKrankheit -> List.of(
                    basePrinter.translateValue(untersuchungImpfstatusKrankheit.getKrankheit()),
                    trimToPlaceholder(untersuchungImpfstatusKrankheit.getAnzahlImpfungen()),
                    trimToPlaceholder(untersuchungImpfstatusKrankheit.getDatumLetzteImpfung(), "MM/yyyy"),
                    basePrinter.translateValue(untersuchungImpfstatusKrankheit.isImpfschutzGegeben()),
                    basePrinter.translateValue(untersuchungImpfstatusKrankheit.isImpfempfehlung()),
                    basePrinter.translateValue(untersuchungImpfstatusKrankheit.isImpfentscheid())))
                .toList(),
            document
        );
    }

    private static void addTableImpfliste(
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
                        .map(basePrinter::translateValue)
                        .collect(Collectors.joining(", ")),
                    trimToPlaceholder(untersuchungImpfungIntern.getDatumImpfung()),
                    KinderdossierPrintUtil.getImpfungInternChargeName(untersuchungImpfungIntern),
                    trimToPlaceholder(untersuchungImpfungIntern.getUserErfasst()),
                    trimToPlaceholder(untersuchungImpfungIntern.getUserDurchgefuehrt()),
                    untersuchungImpfungIntern.getKrankheitenImmunitaeten()
                        .stream()
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

    private static void addTriageRemark(
        @NonNull String labelKeySuffix,
        boolean isTriage,
        @Nullable String bemerkungen,
        @NonNull BasePrinter basePrinter,
        @NonNull Document document
    ) {
        if (isTriage) {
            basePrinter.addVerticalKeyValue(
                "printer.kinderdossier.anlass.triage." + labelKeySuffix + ".label",
                trimToPlaceholder(bemerkungen),
                document
            );
        }
    }

    @NonNull
    private static String anlassTitle(@NonNull Anlass anlass, @NonNull BasePrinter basePrinter) {
        String title = String.format(
            "%d/%d %s %s",
            anlass.getSchuljahr(),
            (anlass.getSchuljahr() + 1) % 100,
            basePrinter.translateValue(anlass.getAnlassConfig().getAnlassTyp()),
            anlass.getTitel());
        return PDFAPrintUtil.sanitizeForPDFA(title);
    }

    private static void addAudiogramTable(
        @Nullable Audiogramm audiogramm,
        @NonNull BasePrinter basePrinter,
        @NonNull Document document
    ) {
        basePrinter.addTable(
            List.of(
                I18nMessage.of("printer.kinderdossier.anlass.table.dbHz250.label"),
                I18nMessage.of("printer.kinderdossier.anlass.table.dbHz500.label"),
                I18nMessage.of("printer.kinderdossier.anlass.table.dbHz1000.label"),
                I18nMessage.of("printer.kinderdossier.anlass.table.dbHz2000.label"),
                I18nMessage.of("printer.kinderdossier.anlass.table.dbHz4000.label"),
                I18nMessage.of("printer.kinderdossier.anlass.table.dbHz6000.label"),
                I18nMessage.of("printer.kinderdossier.anlass.table.dbHz8000.label")
            ), List.of(
                List.of(
                    basePrinter.translateValue(audiogramm == null ? null : audiogramm.getDbHz250()),
                    basePrinter.translateValue(audiogramm == null ? null : audiogramm.getDbHz500()),
                    basePrinter.translateValue(audiogramm == null ? null : audiogramm.getDbHz1000()),
                    basePrinter.translateValue(audiogramm == null ? null : audiogramm.getDbHz2000()),
                    basePrinter.translateValue(audiogramm == null ? null : audiogramm.getDbHz4000()),
                    basePrinter.translateValue(audiogramm == null ? null : audiogramm.getDbHz6000()),
                    basePrinter.translateValue(audiogramm == null ? null : audiogramm.getDbHz8000())
                )
            ), document);
    }
}
