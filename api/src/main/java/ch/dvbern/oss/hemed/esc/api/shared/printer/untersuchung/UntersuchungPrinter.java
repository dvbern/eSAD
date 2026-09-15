package ch.dvbern.oss.hemed.esc.api.shared.printer.untersuchung;

import java.util.List;
import java.util.stream.Collectors;

import ch.dvbern.dvbstarter.shared.i18n.datetime.DateUtil;
import ch.dvbern.oss.commons.i18nl10n.I18nMessage;
import ch.dvbern.oss.hemed.esc.api.features.anlass.aerztlicheuntersuchung.UntersuchungArztBemerkung;
import ch.dvbern.oss.hemed.esc.api.features.anlass.aerztlicheuntersuchung.UntersuchungArztBeurteilung;
import ch.dvbern.oss.hemed.esc.api.features.anlass.anlassconfig.AnlassConfig;
import ch.dvbern.oss.hemed.esc.api.features.anlass.anlassconfig.AnlassConfigUtil;
import ch.dvbern.oss.hemed.esc.api.features.anlass.bewegungsapparat.UntersuchungBewegungsapparat;
import ch.dvbern.oss.hemed.esc.api.features.anlass.bewegungsapparat.types.Haltungsschwaeche;
import ch.dvbern.oss.hemed.esc.api.features.anlass.bewegungsapparat.types.Skoliose;
import ch.dvbern.oss.hemed.esc.api.features.anlass.bewegungsapparat.types.VerkuerzungHamstringmuskulatur;
import ch.dvbern.oss.hemed.esc.api.features.anlass.groessegewicht.UntersuchungGroesseGewicht;
import ch.dvbern.oss.hemed.esc.api.features.anlass.groessegewicht.types.PerzentileBmi;
import ch.dvbern.oss.hemed.esc.api.features.anlass.groessegewicht.types.PerzentileGewicht;
import ch.dvbern.oss.hemed.esc.api.features.anlass.groessegewicht.types.PerzentileGroesse;
import ch.dvbern.oss.hemed.esc.api.features.anlass.hoeren.Audiogramm;
import ch.dvbern.oss.hemed.esc.api.features.anlass.hoeren.UntersuchungHoerenAudiogramm;
import ch.dvbern.oss.hemed.esc.api.features.anlass.hoeren.UntersuchungHoerenOtoskopie;
import ch.dvbern.oss.hemed.esc.api.features.anlass.hoeren.types.AudiogrammBeurteilung;
import ch.dvbern.oss.hemed.esc.api.features.anlass.hoeren.types.AudiogrammSimpel;
import ch.dvbern.oss.hemed.esc.api.features.anlass.hoeren.types.HoerenEmpfehlung;
import ch.dvbern.oss.hemed.esc.api.features.anlass.hoeren.types.HoerenHoerhilfe;
import ch.dvbern.oss.hemed.esc.api.features.anlass.hoeren.types.HoerenZusaetzlicheAngaben;
import ch.dvbern.oss.hemed.esc.api.features.anlass.hoeren.types.OtoskopieResultat;
import ch.dvbern.oss.hemed.esc.api.features.anlass.json.AnlassWithDateTimeJson;
import ch.dvbern.oss.hemed.esc.api.features.anlass.massnahmen.UntersuchungImpfungenMassnahmen;
import ch.dvbern.oss.hemed.esc.api.features.anlass.massnahmen.UntersuchungMassnahmen;
import ch.dvbern.oss.hemed.esc.api.features.anlass.schulrelevantebeeintraechtigung.UntersuchungSchulrelevanteBeeintraechtigung;
import ch.dvbern.oss.hemed.esc.api.features.anlass.schulrelevantebeeintraechtigung.types.Feinmotorik;
import ch.dvbern.oss.hemed.esc.api.features.anlass.schulrelevantebeeintraechtigung.types.FreiesGehen;
import ch.dvbern.oss.hemed.esc.api.features.anlass.schulrelevantebeeintraechtigung.types.GleichgewichtEinbeinstand;
import ch.dvbern.oss.hemed.esc.api.features.anlass.schulrelevantebeeintraechtigung.types.Grafomotorik;
import ch.dvbern.oss.hemed.esc.api.features.anlass.schulrelevantebeeintraechtigung.types.GrobmotorikEinbeinhuepfen;
import ch.dvbern.oss.hemed.esc.api.features.anlass.schulrelevantebeeintraechtigung.types.OhneStuetzradVelofahren;
import ch.dvbern.oss.hemed.esc.api.features.anlass.sehen.UntersuchungSehenBeurteilung;
import ch.dvbern.oss.hemed.esc.api.features.anlass.sehen.types.BrilleLinsenResultat;
import ch.dvbern.oss.hemed.esc.api.features.anlass.sehen.types.DistanzSehenAugeEinzelnResultat;
import ch.dvbern.oss.hemed.esc.api.features.anlass.sehen.types.DistanzSehenResultat;
import ch.dvbern.oss.hemed.esc.api.features.anlass.sehen.types.FarbsehenResultat;
import ch.dvbern.oss.hemed.esc.api.features.anlass.sehen.types.InspektionKonvergenzResultat;
import ch.dvbern.oss.hemed.esc.api.features.anlass.sehen.types.MotilitaetResultat;
import ch.dvbern.oss.hemed.esc.api.features.anlass.sehen.types.SehenEmpfehlung;
import ch.dvbern.oss.hemed.esc.api.features.anlass.sehen.types.StereotestResultat;
import ch.dvbern.oss.hemed.esc.api.features.anlass.sehen.types.StrabismusAuftretenResultat;
import ch.dvbern.oss.hemed.esc.api.features.anlass.sehen.types.StrabismusAuspraegungResultat;
import ch.dvbern.oss.hemed.esc.api.features.anlass.sehen.types.StrabismusResultat;
import ch.dvbern.oss.hemed.esc.api.features.anlass.sehen.types.VerwendetTafelResultat;
import ch.dvbern.oss.hemed.esc.api.features.anlass.sprache.UntersuchungSprache;
import ch.dvbern.oss.hemed.esc.api.features.anlass.sprache.types.Nachsprechen;
import ch.dvbern.oss.hemed.esc.api.features.anlass.sprache.types.Sprachverstaendnis;
import ch.dvbern.oss.hemed.esc.api.features.anlass.sprache.types.Wortschatz;
import ch.dvbern.oss.hemed.esc.api.features.anlass.triage.UntersuchungTriage;
import ch.dvbern.oss.hemed.esc.api.features.anlass.untersuchung.Untersuchung;
import ch.dvbern.oss.hemed.esc.api.shared.anlass.anlassflags.AnlassFlags;
import ch.dvbern.oss.hemed.esc.api.shared.printer.BasePrinter;
import ch.dvbern.oss.hemed.esc.api.shared.printer.PrintUtil;
import ch.dvbern.oss.hemed.esc.api.shared.printer.PrintUtilRecord;
import ch.dvbern.oss.hemed.esc.api.shared.printer.kinderdossier.KinderdossierPrintUtil;
import ch.dvbern.oss.hemed.esc.api.shared.util.ValueEnhancedUtil;

import com.itextpdf.layout.Document;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;

import static ch.dvbern.oss.hemed.esc.api.shared.printer.PrintUtil.trimToPlaceholder;
import static ch.dvbern.oss.hemed.esc.api.shared.printer.kinderdossier.KinderdossierPrintUtil.isAnyVisible;

public final class UntersuchungPrinter {
    private static final String BEMERKUNG_KEY = "printer.kinderdossier.anlass.bemerkungen";

    private UntersuchungPrinter() {
        // static and hidden so we don't get tempted to add services in here ;)
    }

    public static void addUntersuchungSehen(
        @NonNull AnlassConfig anlassConfig,
        @Nullable Untersuchung untersuchung,
        @NonNull BasePrinter basePrinter,
        @NonNull Document document
    ) {
        if (!AnlassConfigUtil.isSehenBeurteilungVisible(anlassConfig)) {
            return;
        }

        basePrinter.addH3Title(
            I18nMessage.of("printer.kinderdossier.anlass.sehen"),
            document);

        List<PrintUtilRecord<UntersuchungSehenBeurteilung>> printerList = List.of(
            new PrintUtilRecord<>(
                "printer.kinderdossier.anlass.brilleLinsen",
                anlassConfig.getSehenBrilleLinsen().isVisible(),
                untersuchungSehen -> basePrinter.translateValue(ValueEnhancedUtil.getEnumValueOrNull(
                    untersuchungSehen.getBrilleLinsen(),
                    BrilleLinsenResultat.class))),
            new PrintUtilRecord<>(
                "printer.kinderdossier.anlass.stereotest",
                anlassConfig.getSehenStereoTest().isVisible(),
                untersuchungSehen -> basePrinter.translateValue(ValueEnhancedUtil.getEnumValueOrNull(
                    untersuchungSehen.getStereotest(),
                    StereotestResultat.class))),
            new PrintUtilRecord<>(
                "printer.kinderdossier.anlass.farbsehen",
                anlassConfig.getSehenFarbsehen().isVisible(),
                untersuchungSehen -> basePrinter.translateValue(ValueEnhancedUtil.getEnumValueOrNull(
                    untersuchungSehen.getFarbsehen(),
                    FarbsehenResultat.class))),
            new PrintUtilRecord<>(
                "printer.kinderdossier.anlass.verwendeteTafel",
                anlassConfig.getSehenTafel().isVisible(),
                untersuchungSehen -> basePrinter.translateValue(ValueEnhancedUtil.getEnumValueOrNull(
                    untersuchungSehen.getVerwendeteTafel(),
                    VerwendetTafelResultat.class))),
            new PrintUtilRecord<>(
                "printer.kinderdossier.anlass.fernvisus",
                anlassConfig.getSehenFernvisus().isVisible(),
                untersuchungSehen -> ValueEnhancedUtil.getEnumSetOrEmpty(
                        untersuchungSehen.getFernvisus(),
                        DistanzSehenResultat.class)
                    .stream()
                    .map(basePrinter::translateValue)
                    .collect(Collectors.joining(", "))),
            new PrintUtilRecord<>(
                "printer.kinderdossier.anlass.fernvisusRechts",
                anlassConfig.getSehenFernvisus().isVisible(),
                untersuchungSehen -> trimToPlaceholder(untersuchungSehen.getFernvisusRechts())),
            new PrintUtilRecord<>(
                "printer.kinderdossier.anlass.fernvisusLinks",
                anlassConfig.getSehenFernvisus().isVisible(),
                untersuchungSehen -> trimToPlaceholder(untersuchungSehen.getFernvisusLinks())),
            new PrintUtilRecord<>(
                "printer.kinderdossier.anlass.fernvisusAugeLinks",
                anlassConfig.getSehenFernvisusAugeEinzeln().isVisible(),
                untersuchungSehen -> basePrinter.translateValue(ValueEnhancedUtil.getEnumValueOrNull(
                    untersuchungSehen.getFernvisusAugeLinks(),
                    DistanzSehenAugeEinzelnResultat.class))),
            new PrintUtilRecord<>(
                "printer.kinderdossier.anlass.fernvisusAugeRechts",
                anlassConfig.getSehenFernvisusAugeEinzeln().isVisible(),
                untersuchungSehen -> basePrinter.translateValue(ValueEnhancedUtil.getEnumValueOrNull(
                    untersuchungSehen.getFernvisusAugeRechts(),
                    DistanzSehenAugeEinzelnResultat.class))),
            new PrintUtilRecord<>(
                "printer.kinderdossier.anlass.nahvisus",
                anlassConfig.getSehenNahvisus().isVisible(),
                untersuchungSehen -> ValueEnhancedUtil.getEnumSetOrEmpty(
                        untersuchungSehen.getNahvisus(),
                        DistanzSehenResultat.class)
                    .stream()
                    .map(basePrinter::translateValue)
                    .collect(Collectors.joining(", "))),
            new PrintUtilRecord<>(
                "printer.kinderdossier.anlass.nahvisusRechts",
                anlassConfig.getSehenNahvisus().isVisible(),
                untersuchungSehen -> trimToPlaceholder(untersuchungSehen.getNahvisusRechts())),
            new PrintUtilRecord<>(
                "printer.kinderdossier.anlass.nahvisusLinks",
                anlassConfig.getSehenNahvisus().isVisible(),
                untersuchungSehen -> trimToPlaceholder(untersuchungSehen.getNahvisusLinks())),
            new PrintUtilRecord<>(
                "printer.kinderdossier.anlass.motilitaet",
                anlassConfig.getSehenMotilitaet().isVisible(),
                untersuchungSehen -> basePrinter.translateValue(ValueEnhancedUtil.getEnumValueOrNull(
                    untersuchungSehen.getMotilitaet(),
                    MotilitaetResultat.class))),
            new PrintUtilRecord<>(
                "printer.kinderdossier.anlass.strabismus",
                anlassConfig.getSehenStrabismus().isVisible(),
                untersuchungSehen -> ValueEnhancedUtil.getEnumSetOrEmpty(
                        untersuchungSehen.getStrabismus(),
                        StrabismusResultat.class)
                    .stream()
                    .map(basePrinter::translateValue)
                    .collect(Collectors.joining(", "))),
            new PrintUtilRecord<>(
                "printer.kinderdossier.anlass.strabismusAuspraegung",
                anlassConfig.getSehenStrabismusAuspraegung().isVisible(),
                untersuchungSehen -> basePrinter.translateValue(ValueEnhancedUtil.getEnumValueOrNull(
                    untersuchungSehen.getStrabismusAuspraegung(),
                    StrabismusAuspraegungResultat.class))),
            new PrintUtilRecord<>(
                "printer.kinderdossier.anlass.strabismusAuftreten",
                anlassConfig.getSehenStrabismusAuftreten().isVisible(),
                untersuchungSehen -> basePrinter.translateValue(ValueEnhancedUtil.getEnumValueOrNull(
                    untersuchungSehen.getStrabismusAuftreten(),
                    StrabismusAuftretenResultat.class))),
            new PrintUtilRecord<>(
                "printer.kinderdossier.anlass.inspektion",
                anlassConfig.getSehenInspektion().isVisible(),
                untersuchungSehen -> basePrinter.translateValue(ValueEnhancedUtil.getEnumValueOrNull(
                    untersuchungSehen.getInspektion(),
                    InspektionKonvergenzResultat.class))),
            new PrintUtilRecord<>(
                "printer.kinderdossier.anlass.konvergenz",
                anlassConfig.getSehenKonvergenz().isVisible(),
                untersuchungSehen -> basePrinter.translateValue(ValueEnhancedUtil.getEnumValueOrNull(
                    untersuchungSehen.getKonvergenz(),
                    InspektionKonvergenzResultat.class))),
            new PrintUtilRecord<>(
                "printer.kinderdossier.anlass.sehenEmpfehlung",
                anlassConfig.getSehenEmpfehlung().isVisible(),
                untersuchungSehen -> ValueEnhancedUtil.getEnumSetOrEmpty(
                        untersuchungSehen.getEmpfehlung(),
                        SehenEmpfehlung.class)
                    .stream()
                    .map(basePrinter::translateValue)
                    .collect(Collectors.joining(", "))),
            new PrintUtilRecord<>(
                BEMERKUNG_KEY,
                anlassConfig.getSehenBemerkungen().isVisible(),
                UntersuchungSehenBeurteilung::getBemerkungen)
        );

        printerList.stream()
            .filter(PrintUtilRecord::isVisible)
            .forEach(printerRecord ->
                basePrinter.addKeyValue(
                    printerRecord.titleKey(),
                    KinderdossierPrintUtil.extractUntersuchungSehen(
                        untersuchung,
                        printerRecord.stringTransformer()
                    ),
                    document
                )
            );
    }

    public static void addUntersuchungHoerenAudiogramm(
        @NonNull AnlassConfig anlassConfig,
        @Nullable Untersuchung untersuchung,
        @NonNull BasePrinter basePrinter,
        @NonNull Document document
    ) {
        if (!AnlassConfigUtil.isHoerenAudiogrammVisible(anlassConfig)) {
            return;
        }
        basePrinter.addH3Title(I18nMessage.of("printer.kinderdossier.anlass.hoeren.audiogramm"), document);

        if (anlassConfig.getHoerenAudiogramm().isVisible()) {
            // audiogrammRechts
            basePrinter.addKeyValue("printer.kinderdossier.anlass.table.audiogrammRechts.label", "", document);
            Audiogramm audiogrammRechts =
                KinderdossierPrintUtil.extractAudiogramm(
                    untersuchung,
                    UntersuchungHoerenAudiogramm::getAudiogrammRechts);
            addAudiogramTable(audiogrammRechts, basePrinter, document);

            // audiogrammLinks
            basePrinter.addKeyValue("printer.kinderdossier.anlass.table.audiogrammLinks.label", "", document);
            Audiogramm audiogrammLinks =
                KinderdossierPrintUtil.extractAudiogramm(
                    untersuchung,
                    UntersuchungHoerenAudiogramm::getAudiogrammLinks);
            addAudiogramTable(audiogrammLinks, basePrinter, document);
        }

        List<PrintUtilRecord<UntersuchungHoerenAudiogramm>> printerList = List.of(
            new PrintUtilRecord<>(
                "printer.kinderdossier.anlass.audiogramm.audiogrammSimpel",
                anlassConfig.getHoerenAudiogrammSimpel().isVisible(),
                untersuchungHoerenAudiogramm -> basePrinter.translateValue(ValueEnhancedUtil.getEnumValueOrNull(
                    untersuchungHoerenAudiogramm.getAudiogrammSimpel(),
                    AudiogrammSimpel.class))),
            new PrintUtilRecord<>(
                "printer.kinderdossier.anlass.audiogramm.hoerhilfe",
                anlassConfig.getHoerenHoerhilfe().isVisible(),
                untersuchungHoerenAudiogramm -> basePrinter.translateValue(ValueEnhancedUtil.getEnumValueOrNull(
                    untersuchungHoerenAudiogramm.getHoerhilfe(),
                    HoerenHoerhilfe.class))),
            new PrintUtilRecord<>(
                "printer.kinderdossier.anlass.audiogramm.audiogrammBeurteilung",
                anlassConfig.getHoerenAudiogrammBeurteilung().isVisible(),
                untersuchungHoerenAudiogramm -> ValueEnhancedUtil.getEnumSetOrEmpty(
                        untersuchungHoerenAudiogramm.getAudiogrammBeurteilung(),
                        AudiogrammBeurteilung.class)
                    .stream()
                    .map(basePrinter::translateValue)
                    .collect(Collectors.joining(", "))),
            new PrintUtilRecord<>(
                "printer.kinderdossier.anlass.audiogramm.zusaetzlicheAngaben",
                anlassConfig.getHoerenAudiogrammZusaetzlicheAngaben().isVisible(),
                untersuchungHoerenAudiogramm ->
                    ValueEnhancedUtil.getEnumSetOrEmpty(
                            untersuchungHoerenAudiogramm.getZusaetzlicheAngaben(),
                            HoerenZusaetzlicheAngaben.class)
                        .stream()
                        .map(basePrinter::translateValue)
                        .collect(Collectors.joining(", "))),
            new PrintUtilRecord<>(
                "printer.kinderdossier.anlass.audiogramm.empfehlungen",
                anlassConfig.getHoerenAudiogrammEmpfehlung().isVisible(),
                untersuchungHoeren -> ValueEnhancedUtil.getEnumSetOrEmpty(
                        untersuchungHoeren.getEmpfehlung(),
                        HoerenEmpfehlung.class)
                    .stream()
                    .map(basePrinter::translateValue)
                    .collect(Collectors.joining(", "))),
            new PrintUtilRecord<>(
                "printer.kinderdossier.anlass.audiogramm.bemerkungen",
                anlassConfig.getHoerenAudiogrammBemerkungen().isVisible(),
                UntersuchungHoerenAudiogramm::getBemerkungen)
        );

        printerList.stream()
            .filter(PrintUtilRecord::isVisible)
            .forEach(printerRecord ->
                basePrinter.addKeyValue(
                    printerRecord.titleKey(),
                    KinderdossierPrintUtil.extractUntersuchungHoerenAudiogramm(
                        untersuchung,
                        printerRecord.stringTransformer()
                    ),
                    document
                )
            );
    }

    public static void addUntersuchungHoerenOtoskopie(
        @NonNull AnlassConfig anlassConfig,
        @Nullable Untersuchung untersuchung,
        @NonNull BasePrinter basePrinter,
        @NonNull Document document
    ) {
        if (!AnlassConfigUtil.isHoerenOtoskopieVisible(anlassConfig)) {
            return;
        }
        basePrinter.addH3Title(I18nMessage.of("printer.kinderdossier.anlass.hoeren.otoskopie"), document);

        List<PrintUtilRecord<UntersuchungHoerenOtoskopie>> printerList = List.of(
            new PrintUtilRecord<>(
                "printer.kinderdossier.anlass.otoskopie.otoskopieRechts",
                anlassConfig.getHoerenOtoskopie().isVisible(),
                untersuchungHoeren -> ValueEnhancedUtil.getEnumSetOrEmpty(
                        untersuchungHoeren.getOtoskopieRechts(),
                        OtoskopieResultat.class)
                    .stream()
                    .map(basePrinter::translateValue)
                    .collect(Collectors.joining(", "))),
            new PrintUtilRecord<>(
                "printer.kinderdossier.anlass.otoskopie.otoskopieRechtsResultatAndere",
                anlassConfig.getHoerenOtoskopie().isVisible(),
                UntersuchungHoerenOtoskopie::getOtoskopieRechtsResultatAndere),
            new PrintUtilRecord<>(
                "printer.kinderdossier.anlass.otoskopie.otoskopieLinks",
                anlassConfig.getHoerenOtoskopie().isVisible(),
                untersuchungHoeren ->
                    ValueEnhancedUtil.getEnumSetOrEmpty(untersuchungHoeren.getOtoskopieLinks(), OtoskopieResultat.class)
                        .stream()
                        .map(basePrinter::translateValue)
                        .collect(Collectors.joining(", "))),
            new PrintUtilRecord<>(
                "printer.kinderdossier.anlass.otoskopie.otoskopieLinksResultatAndere",
                anlassConfig.getHoerenOtoskopie().isVisible(),
                UntersuchungHoerenOtoskopie::getOtoskopieLinksResultatAndere),
            new PrintUtilRecord<>(
                "printer.kinderdossier.anlass.otoskopie.bemerkungen",
                anlassConfig.getHoerenOtoskopieBemerkungen().isVisible(),
                UntersuchungHoerenOtoskopie::getBemerkungen)
        );

        printerList.stream()
            .filter(PrintUtilRecord::isVisible)
            .forEach(printerRecord ->
                basePrinter.addKeyValue(
                    printerRecord.titleKey(),
                    KinderdossierPrintUtil.extractUntersuchungHoerenOtoskopie(
                        untersuchung,
                        printerRecord.stringTransformer()
                    ),
                    document
                )
            );
    }

    public static void addUntersuchungGroesseGewicht(
        @NonNull AnlassConfig anlassConfig,
        @Nullable Untersuchung untersuchung,
        @NonNull BasePrinter basePrinter,
        @NonNull Document document
    ) {
        if (!AnlassConfigUtil.showTabGroesseGewicht(anlassConfig)) {
            return;
        }
        basePrinter.addH3Title(I18nMessage.of("printer.kinderdossier.anlass.grosse"), document);

        List<PrintUtilRecord<UntersuchungGroesseGewicht>> printerList = List.of(
            new PrintUtilRecord<>(
                "printer.kinderdossier.anlass.alter",
                !anlassConfig.getAnlassTyp().isAnonym(),
                groesseGewicht -> PrintUtil.trimToPlaceholder(groesseGewicht.getAlter())),
            new PrintUtilRecord<>(
                "printer.kinderdossier.anlass.groesse",
                anlassConfig.getGroesseGewichtGroesse().isVisible(),
                groesseGewicht -> PrintUtil.trimToPlaceholder(groesseGewicht.getGroesse())),
            new PrintUtilRecord<>(
                "printer.kinderdossier.anlass.groessePerzentile",
                anlassConfig.getGroesseGewichtGroessePerzentileSingleValue().isVisible(),
                groesseGewicht -> basePrinter.translateValue(ValueEnhancedUtil.getEnumValueOrNull(
                    groesseGewicht.getGroessePerzentileSingleValue(),
                    PerzentileGroesse.class))),
            new PrintUtilRecord<>(
                "printer.kinderdossier.anlass.gewicht",
                anlassConfig.getGroesseGewichtGewicht().isVisible(),
                groesseGewicht -> PrintUtil.trimToPlaceholder(groesseGewicht.getGewicht())),
            new PrintUtilRecord<>(
                "printer.kinderdossier.anlass.gewichtPerzentile",
                anlassConfig.getGroesseGewichtGewichtPerzentileSingleValue().isVisible(),
                groesseGewicht -> basePrinter.translateValue(ValueEnhancedUtil.getEnumValueOrNull(
                    groesseGewicht.getGewichtPerzentileSingleValue(),
                    PerzentileGewicht.class))),
            new PrintUtilRecord<>(
                "printer.kinderdossier.anlass.bmi",
                anlassConfig.getGroesseGewichtBmi().isVisible(),
                groesseGewicht -> PrintUtil.trimToPlaceholder(groesseGewicht.getBmi())),
            new PrintUtilRecord<>(
                "printer.kinderdossier.anlass.bmiPerzentile",
                anlassConfig.getGroesseGewichtBmiPerzentileSingleValue().isVisible(),
                groesseGewicht -> basePrinter.translateValue(ValueEnhancedUtil.getEnumValueOrNull(
                    groesseGewicht.getBmiPerzentileSingleValue(),
                    PerzentileBmi.class))),
            new PrintUtilRecord<>(
                BEMERKUNG_KEY,
                anlassConfig.getGroesseGewichtBemerkungen().isVisible(),
                UntersuchungGroesseGewicht::getBemerkungen)
        );

        printerList.stream()
            .filter(PrintUtilRecord::isVisible)
            .forEach(printerRecord ->
                basePrinter.addKeyValue(
                    printerRecord.titleKey(),
                    KinderdossierPrintUtil.extractUntersuchungGroesseGewicht(
                        untersuchung,
                        printerRecord.stringTransformer()
                    ),
                    document
                )
            );
    }

    public static void addBewegungsapparat(
        @NonNull AnlassConfig anlassConfig,
        @Nullable Untersuchung untersuchung,
        @NonNull BasePrinter basePrinter,
        @NonNull Document document
    ) {
        if (!AnlassConfigUtil.showTabBewegungsapparat(anlassConfig)) {
            return;
        }
        basePrinter.addH3Title(I18nMessage.of("printer.kinderdossier.anlass.bewegungsapparat.titel"), document);

        List<PrintUtilRecord<UntersuchungBewegungsapparat>> printerList = List.of(
            new PrintUtilRecord<>(
                "printer.kinderdossier.anlass.skoliose",
                anlassConfig.getBewegungsapparatSkoliose().isVisible(),
                bewegungsapparat -> basePrinter.translateValue(ValueEnhancedUtil.getEnumValueOrNull(
                    bewegungsapparat.getSkoliose(),
                    Skoliose.class))),
            new PrintUtilRecord<>(
                "printer.kinderdossier.anlass.haltungsschwaeche",
                anlassConfig.getBewegungsapparatHaltungsschwaeche().isVisible(),
                bewegungsapparat -> basePrinter.translateValue(ValueEnhancedUtil.getEnumValueOrNull(
                    bewegungsapparat.getHaltungsschwaeche(),
                    Haltungsschwaeche.class))),
            new PrintUtilRecord<>(
                "printer.kinderdossier.anlass.verkuerzungHamstringmuskulatur",
                anlassConfig.getBewegungsapparatVerkuerzungHamstringmuskulatur().isVisible(),
                bewegungsapparat -> basePrinter.translateValue(ValueEnhancedUtil.getEnumValueOrNull(
                    bewegungsapparat.getVerkuerzungHamstringmuskulatur(),
                    VerkuerzungHamstringmuskulatur.class))),
            new PrintUtilRecord<>(
                BEMERKUNG_KEY,
                anlassConfig.getBewegungsapparatBemerkung().isVisible(),
                UntersuchungBewegungsapparat::getBemerkungen)
        );

        printerList.stream()
            .filter(PrintUtilRecord::isVisible)
            .forEach(printerRecord ->
                basePrinter.addKeyValue(
                    printerRecord.titleKey(),
                    KinderdossierPrintUtil.extractUntersuchungBewegungsapparat(
                        untersuchung,
                        printerRecord.stringTransformer()
                    ),
                    document
                )
            );
    }

    /* acceptable complexity */
    @SuppressWarnings("java:S3776")
    public static void addUntersuchungArzt(
        @NonNull AnlassConfig anlassConfig,
        @Nullable Untersuchung untersuchung,
        @NonNull BasePrinter basePrinter,
        @NonNull Document document
    ) {
        if (!AnlassConfigUtil.isArztUntersuchungBeurteilungVisible(anlassConfig)) {
            return;
        }
        basePrinter.addH3Title(I18nMessage.of("printer.kinderdossier.anlass.untersuchungArzt"), document);

        List<PrintUtilRecord<UntersuchungArztBeurteilung>> printerList = List.of(
            new PrintUtilRecord<>(
                "printer.kinderdossier.anlass.einverstaendniserklaerung",
                anlassConfig.getUnterlagenEinverstaendnis().isVisible(),
                untersuchungArzt -> untersuchungArzt.getEinverstaendniserklaerung()
                    .stream()
                    .map(basePrinter::translateValue)
                    .collect(Collectors.joining(", "))),
            new PrintUtilRecord<>(
                "printer.kinderdossier.anlass.verhaltenfragebogen",
                anlassConfig.getUnterlagenVerhaltensfragebogen().isVisible(),
                untersuchungArzt -> basePrinter.translateValue(untersuchungArzt.getVerhaltensfragebogen())),
            new PrintUtilRecord<>(
                "printer.kinderdossier.anlass.verhalten",
                anlassConfig.getUntersuchungVerhalten().isVisible(),
                untersuchungArzt -> basePrinter.translateValue(untersuchungArzt.getVerhalten())),
            new PrintUtilRecord<>(
                "printer.kinderdossier.anlass.blutdruck",
                anlassConfig.getUntersuchungBlutdruck().isVisible(),
                untersuchungArzt -> basePrinter.translateValue(untersuchungArzt.getBlutdruck())),
            new PrintUtilRecord<>(
                "printer.kinderdossier.anlass.blutdruckSystolisch",
                anlassConfig.getUntersuchungBlutdruck().isVisible(),
                untersuchungArzt -> PrintUtil.trimToPlaceholder(untersuchungArzt.getBlutdruckSystolisch())),
            new PrintUtilRecord<>(
                "printer.kinderdossier.anlass.blutdruckDiastolisch",
                anlassConfig.getUntersuchungBlutdruck().isVisible(),
                untersuchungArzt -> PrintUtil.trimToPlaceholder(untersuchungArzt.getBlutdruckDiastolisch())),
            new PrintUtilRecord<>(
                "printer.kinderdossier.anlass.herzauskultation",
                anlassConfig.getUntersuchungHerz().isVisible(),
                untersuchungArzt -> basePrinter.translateValue(untersuchungArzt.getHerzauskultation())),
            new PrintUtilRecord<>(
                "printer.kinderdossier.anlass.herzauskultationBemerkung",
                anlassConfig.getUntersuchungHerz().isVisible(),
                UntersuchungArztBeurteilung::getHerzauskultationBemerkung),
            new PrintUtilRecord<>(
                "printer.kinderdossier.anlass.haut",
                anlassConfig.getUntersuchungHaut().isVisible(),
                untersuchungArzt -> basePrinter.translateValue(untersuchungArzt.getHaut())),
            new PrintUtilRecord<>(
                "printer.kinderdossier.anlass.hautBemerkung",
                anlassConfig.getUntersuchungHaut().isVisible(),
                UntersuchungArztBeurteilung::getHautBemerkung),
            new PrintUtilRecord<>(
                "printer.kinderdossier.anlass.mundAndZaehne",
                anlassConfig.getUntersuchungMundZaehne().isVisible(),
                untersuchungArzt -> untersuchungArzt.getMundAndZaehne()
                    .stream()
                    .map(basePrinter::translateValue)
                    .collect(Collectors.joining(", "))),
            new PrintUtilRecord<>(
                "printer.kinderdossier.anlass.mundAndZaehneBemerkung",
                anlassConfig.getUntersuchungMundZaehne().isVisible(),
                UntersuchungArztBeurteilung::getMundAndZaehneBemerkung),
            new PrintUtilRecord<>(
                "printer.kinderdossier.anlass.sprachfaehigkeit",
                anlassConfig.getUntersuchungSprachfaehigkeitMultiValue().isVisible(),
                untersuchungArzt -> untersuchungArzt.getSprachfaehigkeitMultiValue()
                    .stream()
                    .map(basePrinter::translateValue)
                    .collect(Collectors.joining(", "))),
            new PrintUtilRecord<>(
                "printer.kinderdossier.anlass.sprachfaehigkeitBemerkung",
                anlassConfig.getUntersuchungSprachfaehigkeitMultiValue().isVisible(),
                UntersuchungArztBeurteilung::getSprachfaehigkeitBemerkung),
            new PrintUtilRecord<>(
                "printer.kinderdossier.anlass.sprachfaehigkeit",
                anlassConfig.getUntersuchungSprachfaehigkeitSingleValue().isVisible(),
                untersuchungArzt -> basePrinter.translateValue(untersuchungArzt.getSprachfaehigkeitSingleValue())),
            new PrintUtilRecord<>(
                "printer.kinderdossier.anlass.bewegungsapparat",
                anlassConfig.getUntersuchungBewegungsapparat().isVisible(),
                untersuchungArzt -> untersuchungArzt.getBewegungsapparat()
                    .stream()
                    .map(basePrinter::translateValue)
                    .collect(Collectors.joining(", "))),
            new PrintUtilRecord<>(
                "printer.kinderdossier.anlass.bewegungsapparatBemerkung",
                anlassConfig.getUntersuchungBewegungsapparat().isVisible(),
                UntersuchungArztBeurteilung::getBewegungsapparatBemerkung),
            new PrintUtilRecord<>(
                "printer.kinderdossier.anlass.einbeinstand",
                anlassConfig.getUntersuchungEinbeinstand().isVisible(),
                untersuchungArzt -> basePrinter.translateValue(untersuchungArzt.getEinbeinstand())),
            new PrintUtilRecord<>(
                "printer.kinderdossier.anlass.einbeinstandBemerkung",
                anlassConfig.getUntersuchungEinbeinstand().isVisible(),
                UntersuchungArztBeurteilung::getEinbeinhuepfenBemerkung),
            new PrintUtilRecord<>(
                "printer.kinderdossier.anlass.einbeinstand.links",
                anlassConfig.getUntersuchungEinbeinstandRechtsLinks().isVisible(),
                untersuchungArzt -> basePrinter.translateValue(untersuchungArzt.getEinbeinstandLinks())),
            new PrintUtilRecord<>(
                "printer.kinderdossier.anlass.einbeinstand.rechts",
                anlassConfig.getUntersuchungEinbeinstandRechtsLinks().isVisible(),
                untersuchungArzt -> basePrinter.translateValue(untersuchungArzt.getEinbeinstandRechts())),
            new PrintUtilRecord<>(
                "printer.kinderdossier.anlass.einbeinhuepfen",
                anlassConfig.getUntersuchungEinbeinhuepfen().isVisible(),
                untersuchungArzt -> basePrinter.translateValue(untersuchungArzt.getEinbeinhuepfen())),
            new PrintUtilRecord<>(
                "printer.kinderdossier.anlass.einbeinhuepfenBemerkung",
                anlassConfig.getUntersuchungEinbeinhuepfen().isVisible(),
                UntersuchungArztBeurteilung::getEinbeinhuepfenBemerkung),
            new PrintUtilRecord<>(
                "printer.kinderdossier.anlass.einbeinhuepfen.links",
                anlassConfig.getUntersuchungEinbeinhuepfenRechtsLinks().isVisible(),
                untersuchungArzt -> basePrinter.translateValue(untersuchungArzt.getEinbeinhuepfenLinks())),
            new PrintUtilRecord<>(
                "printer.kinderdossier.anlass.einbeinhuepfen.rechts",
                anlassConfig.getUntersuchungEinbeinhuepfenRechtsLinks().isVisible(),
                untersuchungArzt -> basePrinter.translateValue(untersuchungArzt.getEinbeinhuepfenRechts())),
            new PrintUtilRecord<>(
                "printer.kinderdossier.anlass.fingeropposition",
                anlassConfig.getUntersuchungFingeropposition().isVisible(),
                untersuchungArzt -> untersuchungArzt.getFingeropposition()
                    .stream()
                    .map(basePrinter::translateValue)
                    .collect(Collectors.joining(", "))),
            new PrintUtilRecord<>(
                "printer.kinderdossier.anlass.fingeroppositionBemerkung",
                anlassConfig.getUntersuchungFingeropposition().isVisible(),
                UntersuchungArztBeurteilung::getFingeroppositionBemerkung),
            new PrintUtilRecord<>(
                "printer.kinderdossier.anlass.hampelmann",
                anlassConfig.getUntersuchungHampelmann().isVisible(),
                untersuchungArzt -> basePrinter.translateValue(untersuchungArzt.getHampelmann())),
            new PrintUtilRecord<>(
                "printer.kinderdossier.anlass.ballFangen",
                anlassConfig.getUntersuchungBallFangen().isVisible(),
                untersuchungArzt -> basePrinter.translateValue(untersuchungArzt.getBallFangen())),
            new PrintUtilRecord<>(
                "printer.kinderdossier.anlass.ballWerfen",
                anlassConfig.getUntersuchungBallWerfen().isVisible(),
                untersuchungArzt -> basePrinter.translateValue(untersuchungArzt.getBallWerfen())),
            new PrintUtilRecord<>(
                "printer.kinderdossier.anlass.nameSchreiben",
                anlassConfig.getUntersuchungNameSchreiben().isVisible(),
                untersuchungArzt -> basePrinter.translateValue(untersuchungArzt.getNameSchreiben())),
            new PrintUtilRecord<>(
                "printer.kinderdossier.anlass.figurNachmalen",
                anlassConfig.getUntersuchungFigurNachmalen().isVisible(),
                untersuchungArzt -> basePrinter.translateValue(untersuchungArzt.getFigurNachmalen())),
            new PrintUtilRecord<>(
                "printer.kinderdossier.anlass.stifthaltung",
                anlassConfig.getUntersuchungStifthaltung().isVisible(),
                untersuchungArzt -> basePrinter.translateValue(untersuchungArzt.getStifthaltung())),
            new PrintUtilRecord<>(
                "printer.kinderdossier.anlass.aussprache",
                anlassConfig.getUntersuchungAussprache().isVisible(),
                untersuchungArzt -> basePrinter.translateValue(untersuchungArzt.getAussprache())),
            new PrintUtilRecord<>(
                "printer.kinderdossier.anlass.lesefitPunkte",
                anlassConfig.getUntersuchungLesefitPunkte().isVisible(),
                untersuchungArzt -> PrintUtil.trimToPlaceholder(untersuchungArzt.getLesefitPunkte())),
            new PrintUtilRecord<>(
                "printer.kinderdossier.anlass.lesefit",
                anlassConfig.getUntersuchungLesefit().isVisible(),
                untersuchungArzt -> basePrinter.translateValue(untersuchungArzt.getLesefit())),
            new PrintUtilRecord<>(
                "printer.kinderdossier.anlass.zahlenNachsprechen4",
                anlassConfig.getUntersuchungZahlenNachsprechen4().isVisible(),
                untersuchungArzt -> basePrinter.translateValue(untersuchungArzt.getZahlenNachsprechen4())),
            new PrintUtilRecord<>(
                "printer.kinderdossier.anlass.zahlenNachsprechen5",
                anlassConfig.getUntersuchungZahlenNachsprechen5().isVisible(),
                untersuchungArzt -> basePrinter.translateValue(untersuchungArzt.getZahlenNachsprechen5())),
            new PrintUtilRecord<>(
                "printer.kinderdossier.anlass.zeichnenKreis",
                anlassConfig.getUntersuchungZeichnenKreis().isVisible(),
                untersuchungArzt -> basePrinter.translateValue(untersuchungArzt.getZeichnenKreis())),
            new PrintUtilRecord<>(
                "printer.kinderdossier.anlass.zeichnenViereck",
                anlassConfig.getUntersuchungZeichnenViereck().isVisible(),
                untersuchungArzt -> basePrinter.translateValue(untersuchungArzt.getZeichnenViereck())),
            new PrintUtilRecord<>(
                "printer.kinderdossier.anlass.zeichnenDreieck",
                anlassConfig.getUntersuchungZeichnenDreieck().isVisible(),
                untersuchungArzt -> basePrinter.translateValue(untersuchungArzt.getZeichnenDreieck())),
            new PrintUtilRecord<>(
                "printer.kinderdossier.anlass.zeichnenMenschPunkte",
                anlassConfig.getUntersuchungZeichnenMenschPunkte().isVisible(),
                untersuchungArzt -> PrintUtil.trimToPlaceholder(untersuchungArzt.getZeichnenMenschPunkte())),
            new PrintUtilRecord<>(
                "printer.kinderdossier.anlass.zeichnenMensch",
                anlassConfig.getUntersuchungZeichnenMensch().isVisible(),
                untersuchungArzt -> basePrinter.translateValue(untersuchungArzt.getZeichnenMensch())),
            new PrintUtilRecord<>(
                "printer.kinderdossier.anlass.motorikPunkte",
                anlassConfig.getUntersuchungMotorikPunkte().isVisible(),
                untersuchungArzt -> PrintUtil.trimToPlaceholder(untersuchungArzt.getMotorikPunkte())),
            new PrintUtilRecord<>(
                "printer.kinderdossier.anlass.motorik",
                anlassConfig.getUntersuchungMotorik().isVisible(),
                untersuchungArzt -> basePrinter.translateValue(untersuchungArzt.getMotorik())),
            new PrintUtilRecord<>(
                "printer.kinderdossier.anlass.sprachePunkte",
                anlassConfig.getUntersuchungSprachePunkte().isVisible(),
                untersuchungArzt -> PrintUtil.trimToPlaceholder(untersuchungArzt.getSprachfaehigkeitPunkte())),
            new PrintUtilRecord<>(
                "printer.kinderdossier.anlass.spracheBeurteilung",
                anlassConfig.getUntersuchungSprachfaehigkeitSingleValue().isVisible(),
                untersuchungArzt -> basePrinter.translateValue(untersuchungArzt.getSprachfaehigkeitSingleValue())),
            new PrintUtilRecord<>(
                "printer.kinderdossier.anlass.kognitionPunkte",
                anlassConfig.getUntersuchungKognitionPunkte().isVisible(),
                untersuchungArzt -> PrintUtil.trimToPlaceholder(untersuchungArzt.getKognitionPunkte())),
            new PrintUtilRecord<>(
                "printer.kinderdossier.anlass.kognition",
                anlassConfig.getUntersuchungKognition().isVisible(),
                untersuchungArzt -> basePrinter.translateValue(untersuchungArzt.getKognition()))
        );

        printerList.stream()
            .filter(PrintUtilRecord::isVisible)
            .forEach(printerRecord ->
                basePrinter.addKeyValue(
                    printerRecord.titleKey(),
                    KinderdossierPrintUtil.extractUntersuchungArzt(
                        untersuchung,
                        printerRecord.stringTransformer()
                    ),
                    document
                )
            );

        if (anlassConfig.getUntersuchungBemerkungen().isVisible()) {
            basePrinter.addVerticalKeyValue(
                BEMERKUNG_KEY,
                KinderdossierPrintUtil.extractUntersuchungArztBemerkung(
                    untersuchung,
                    UntersuchungArztBemerkung::getBemerkungen),
                document);
        }
    }

    public static void addUntersuchungSprache(
        @NonNull AnlassConfig anlassConfig,
        @Nullable Untersuchung untersuchung,
        @NonNull BasePrinter basePrinter,
        @NonNull Document document
    ) {
        if (!AnlassConfigUtil.showTabSprache(anlassConfig)) {
            return;
        }
        basePrinter.addH3Title(I18nMessage.of("printer.kinderdossier.anlass.sprache"), document);

        List<PrintUtilRecord<UntersuchungSprache>> printerList = List.of(
            new PrintUtilRecord<>(
                "printer.kinderdossier.anlass.sprache.nachsprechen",
                anlassConfig.getUntersuchungSpracheNachsprechen().isVisible(),
                sprache -> basePrinter.translateValue(ValueEnhancedUtil.getEnumValueOrNull(
                    sprache.getNachsprechen(),
                    Nachsprechen.class))),
            new PrintUtilRecord<>(
                "printer.kinderdossier.anlass.sprache.wortschatz",
                anlassConfig.getUntersuchungSpracheWortschatz().isVisible(),
                sprache -> basePrinter.translateValue(ValueEnhancedUtil.getEnumValueOrNull(
                    sprache.getWortschatz(),
                    Wortschatz.class))),
            new PrintUtilRecord<>(
                "printer.kinderdossier.anlass.sprache.sprachverstaendnis",
                anlassConfig.getUntersuchungSpracheSprachverstaendnis().isVisible(),
                sprache -> basePrinter.translateValue(ValueEnhancedUtil.getEnumValueOrNull(
                    sprache.getSprachverstaendnis(),
                    Sprachverstaendnis.class))),
            new PrintUtilRecord<>(
                BEMERKUNG_KEY,
                anlassConfig.getUntersuchungSpracheBemerkungen().isVisible(),
                UntersuchungSprache::getBemerkungen)
        );

        printerList.stream()
            .filter(PrintUtilRecord::isVisible)
            .forEach(printerRecord ->
                basePrinter.addKeyValue(
                    printerRecord.titleKey(),
                    KinderdossierPrintUtil.extractUntersuchungSprache(
                        untersuchung,
                        printerRecord.stringTransformer()
                    ),
                    document
                )
            );
    }

    public static void addUntersuchungSchulrelevanteBeeintraechtigung(
        @NonNull AnlassConfig anlassConfig,
        @Nullable Untersuchung untersuchung,
        @NonNull BasePrinter basePrinter,
        @NonNull Document document
    ) {
        if (!isAnyVisible(
            anlassConfig.getUntersuchungBeeintraechtigungFreiesGehen(),
            anlassConfig.getUntersuchungBeeintraechtigungOhneStuetzradVelofahren(),
            anlassConfig.getUntersuchungBeeintraechtigungGrafomotorik(),
            anlassConfig.getUntersuchungBeeintraechtigungFeinmotorik(),
            anlassConfig.getUntersuchungBeeintraechtigungGrobmotorik(),
            anlassConfig.getUntersuchungBeeintraechtigungGleichgewicht(),
            anlassConfig.getUntersuchungBeeintraechtigungBemerkung()
        )) {
            return;
        }

        basePrinter.addH3Title(
            I18nMessage.of("printer.kinderdossier.anlass.schulrelevanteBeeintraechtigung"),
            document);

        List<PrintUtilRecord<UntersuchungSchulrelevanteBeeintraechtigung>> printerList = List.of(
            new PrintUtilRecord<>(
                "printer.kinderdossier.anlass.freiesGehen",
                anlassConfig.getUntersuchungBeeintraechtigungFreiesGehen().isVisible(),
                beeintraechtigung -> {
                    if (beeintraechtigung.getAnzahlMonateFreiesGehen() != null) {
                        return PrintUtil.trimToPlaceholder(DateUtil.calculateAlterDuodezimal(beeintraechtigung.getAnzahlMonateFreiesGehen()));
                    }
                    return basePrinter.translateValue(ValueEnhancedUtil.getEnumValueOrNull(
                        beeintraechtigung.getFreiesGehen(),
                        FreiesGehen.class)
                    );
                }),
            new PrintUtilRecord<>(
                "printer.kinderdossier.anlass.ohneStuetzradVelofahren",
                anlassConfig.getUntersuchungBeeintraechtigungOhneStuetzradVelofahren().isVisible(),
                beeintraechtigung -> basePrinter.translateValue(ValueEnhancedUtil.getEnumValueOrNull(
                    beeintraechtigung.getOhneStuetzradVelofahren(),
                    OhneStuetzradVelofahren.class))),
            new PrintUtilRecord<>(
                "printer.kinderdossier.anlass.grafomotorik",
                anlassConfig.getUntersuchungBeeintraechtigungGrafomotorik().isVisible(),
                beeintraechtigung -> basePrinter.translateValue(ValueEnhancedUtil.getEnumValueOrNull(
                    beeintraechtigung.getGrafomotorik(),
                    Grafomotorik.class))),
            new PrintUtilRecord<>(
                "printer.kinderdossier.anlass.feinmotorik",
                anlassConfig.getUntersuchungBeeintraechtigungFeinmotorik().isVisible(),
                beeintraechtigung -> basePrinter.translateValue(ValueEnhancedUtil.getEnumValueOrNull(
                    beeintraechtigung.getFeinmotorik(),
                    Feinmotorik.class))),
            new PrintUtilRecord<>(
                "printer.kinderdossier.anlass.grobmotorik",
                anlassConfig.getUntersuchungBeeintraechtigungGrobmotorik().isVisible(),
                beeintraechtigung -> basePrinter.translateValue(ValueEnhancedUtil.getEnumValueOrNull(
                    beeintraechtigung.getGrobmotorikEinbeinhuepfen(),
                    GrobmotorikEinbeinhuepfen.class))),
            new PrintUtilRecord<>(
                "printer.kinderdossier.anlass.gleichgewicht",
                anlassConfig.getUntersuchungBeeintraechtigungGleichgewicht().isVisible(),
                beeintraechtigung -> basePrinter.translateValue(ValueEnhancedUtil.getEnumValueOrNull(
                    beeintraechtigung.getGleichgewichtEinbeinstand(),
                    GleichgewichtEinbeinstand.class))),
            new PrintUtilRecord<>(
                BEMERKUNG_KEY,
                anlassConfig.getUntersuchungBeeintraechtigungBemerkung().isVisible(),
                UntersuchungSchulrelevanteBeeintraechtigung::getBemerkungen)
        );

        printerList.stream()
            .filter(PrintUtilRecord::isVisible)
            .forEach(printerRecord ->
                basePrinter.addKeyValue(
                    printerRecord.titleKey(),
                    KinderdossierPrintUtil.extractUntersuchungSchulrelevanteBeeintraechtigung(
                        untersuchung,
                        printerRecord.stringTransformer()
                    ),
                    document
                )
            );
    }

    public static void addUntersuchungImpfungenMassnahmen(
        @NonNull AnlassConfig anlassConfig,
        @Nullable Untersuchung untersuchung,
        @NonNull List<AnlassWithDateTimeJson> listeImpfFolgetermine,
        @NonNull BasePrinter basePrinter,
        @NonNull Document document
    ) {
        if (!(AnlassConfigUtil.isImpfungenMassnahmenVisible(anlassConfig))) {
            return;
        }

        basePrinter.addH3Title(I18nMessage.of("printer.kinderdossier.anlass.massnahmen"), document);

        // keineMassnahmen
        if (anlassConfig.isImpfungenMassnahmeKeineMassnahmen()) {
            basePrinter.addKeyValue(
                "printer.kinderdossier.anlass.keineImpfMassnahmen",
                KinderdossierPrintUtil.extractUntersuchungImpfungenMassnahmenBooleanFn(
                    untersuchung,
                    UntersuchungImpfungenMassnahmen::isKeineMassnahmen),
                document);
        }

        // impfberatungMpaDurchgefuehrt
        if (anlassConfig.isImpfungenMassnahmeBeratungMPA()) {
            basePrinter.addKeyValue(
                "printer.kinderdossier.anlass.impfberatungMpaDurchgefuehrt",
                KinderdossierPrintUtil.extractUntersuchungImpfungenMassnahmenBooleanFn(
                    untersuchung,
                    UntersuchungImpfungenMassnahmen::isImpfberatungMpaDurchgefuehrt),
                document);
        }

        // informationEltern
        if (anlassConfig.isImpfungenMassnahmeInfoEltern()) {
            basePrinter.addKeyValue(
                "printer.kinderdossier.anlass.informationImpfenEltern",
                KinderdossierPrintUtil.extractUntersuchungImpfungenMassnahmen(
                    untersuchung,
                    untersuchungImpfungenMassnahmen -> basePrinter.translateValue(untersuchungImpfungenMassnahmen.getInformationEltern())),
                document);
        }

        // informationHausarzt
        if (anlassConfig.isImpfungenMassnahmeInfoKinderarzt()) {
            basePrinter.addKeyValue(
                "printer.kinderdossier.anlass.informationHausarzt",
                KinderdossierPrintUtil.extractUntersuchungImpfungenMassnahmenBooleanFn(
                    untersuchung,
                    UntersuchungImpfungenMassnahmen::isInformationHausarzt),
                document);
        }

        // rueckfrageHausarzt
        if (anlassConfig.isImpfungenMassnahmeRueckfrageKinderarzt()) {
            basePrinter.addKeyValue(
                "printer.kinderdossier.anlass.rueckfrageHausarzt",
                KinderdossierPrintUtil.extractUntersuchungImpfungenMassnahmenBooleanFn(
                    untersuchung,
                    UntersuchungImpfungenMassnahmen::isRueckfrageHausarzt),
                document);
        }

        if (anlassConfig.getUntersuchungImpfungEmpfehlung().isVisible()) {
            basePrinter.addKeyValue(
                "printer.kinderdossier.anlass.impfungEmpfehlung",
                KinderdossierPrintUtil.extractUntersuchungImpfungenMassnahmen(
                    untersuchung,
                    untersuchungImpfungenMassnahmen -> untersuchungImpfungenMassnahmen.getEmpfehlung().stream()
                        .map(basePrinter::translateValue)
                        .collect(Collectors.joining(", "))),
                document);
        }

        if (anlassConfig.getUntersuchungImpfungAchtung().isVisible()) {
            basePrinter.addKeyValue(
                "printer.kinderdossier.anlass.impfungAchtung",
                KinderdossierPrintUtil.extractUntersuchungImpfungenMassnahmen(
                    untersuchung,
                    untersuchungImpfungenMassnahmen -> PrintUtil.trimToPlaceholder(untersuchungImpfungenMassnahmen.getAchtung())),
                document);
        }

        if (anlassConfig.isImpfungenMassnahmeImpfenFolgetermin()) {
            basePrinter.addKeyValue(
                "printer.kinderdossier.anlass.impfenFolgetermin",
                KinderdossierPrintUtil.extractImpfungenFolgetermin(
                    listeImpfFolgetermine,
                    folgetermin -> folgetermin.getTitel()
                        + ' '
                        + trimToPlaceholder(folgetermin.getDatum())
                        + getZeitraum(folgetermin)
                ),
                document);
        }

        if (anlassConfig.isImpfungenMassnahmeGruppenFolgetermin()) {
            basePrinter.addKeyValue(
                "printer.kinderdossier.anlass.vormerkenGruppenFolgetermin",
                KinderdossierPrintUtil.extractUntersuchungImpfungenMassnahmenBooleanFn(
                    untersuchung,
                    UntersuchungImpfungenMassnahmen::isVormerkenFuerGruppenFolgetermin),
                document);
        }

        if (anlassConfig.isImpfungenMassnahmeInfomaterialAbgegeben()) {
            basePrinter.addKeyValue(
                "printer.kinderdossier.anlass.infomaterialAbgegeben",
                KinderdossierPrintUtil.extractUntersuchungImpfungenMassnahmenBooleanFn(
                    untersuchung,
                    UntersuchungImpfungenMassnahmen::isInfomaterialAbgegeben),
                document);
        }

        // bemerkungen
        basePrinter.addVerticalKeyValue(
            BEMERKUNG_KEY,
            KinderdossierPrintUtil.extractUntersuchungImpfungenMassnahmen(
                untersuchung,
                UntersuchungImpfungenMassnahmen::getBemerkungen),
            document);
    }

    public static String getZeitraum(AnlassWithDateTimeJson anlass) {
        if (anlass.getZeitVon() == null) {
            return "";
        }
        return ' ' + PrintUtil.formatLocalDateTime(anlass.getZeitVon())
            + " - "
            + PrintUtil.formatLocalDateTime(anlass.getZeitBis());
    }

    public static void addUntersuchungMassnahmen(
        @NonNull AnlassConfig anlassConfig,
        @Nullable Untersuchung untersuchung,
        @NonNull BasePrinter basePrinter,
        @NonNull Document document
    ) {
        if (!AnlassConfigUtil.isMassnahmenBeurteilungVisible(anlassConfig)) {
            return;
        }

        basePrinter.addH3Title(I18nMessage.of("printer.kinderdossier.anlass.generelleMassnahmen"), document);

        // keineMassnahmen
        if (anlassConfig.isShowMassnahmenKeine()) {
            basePrinter.addKeyValue(
                "printer.kinderdossier.anlass.keineMassnahmen",
                KinderdossierPrintUtil.extractUntersuchungMassnahmenBooleanFn(
                    untersuchung,
                    UntersuchungMassnahmen::isKeineMassnahmen),
                document);
        }

        //beratungMPA
        if (anlassConfig.isShowMassnahmenBeratungMpa()) {
            basePrinter.addKeyValue(
                "printer.kinderdossier.anlass.beratungMPA",
                KinderdossierPrintUtil.extractUntersuchungMassnahmenBooleanFn(
                    untersuchung,
                    UntersuchungMassnahmen::isBeratungMPA),
                document);
        }

        //beratungSchulAerztin
        if (anlassConfig.isShowMassnahmenBeratungSchulAerztin()) {
            basePrinter.addKeyValue(
                "printer.kinderdossier.anlass.beratungSchulAerztin",
                KinderdossierPrintUtil.extractUntersuchungMassnahmenBooleanFn(
                    untersuchung,
                    UntersuchungMassnahmen::isBeratungSchulAerztin),
                document);
        }

        // anderes
        if (anlassConfig.isShowMassnahmenAnderes()) {
            basePrinter.addKeyValue(
                "printer.kinderdossier.anlass.anderes",
                KinderdossierPrintUtil.extractUntersuchungMassnahmenBooleanFn(
                    untersuchung,
                    UntersuchungMassnahmen::isAnderes),
                document);

            // anderesBemerkung
            basePrinter.addKeyValue(
                "printer.kinderdossier.anlass.anderesBemerkung",
                KinderdossierPrintUtil.extractUntersuchungMassnahmen(
                    untersuchung,
                    untersuchungMassnahmen -> PrintUtil.trimToPlaceholder(untersuchungMassnahmen.getAnderesBemerkung())),
                document);
        }

        if (anlassConfig.isShowMassnahmenBereitsBehandelt()) {
            // Bereits Behandelt init table
            addBereitsBehandeltTable(untersuchung, basePrinter, document);

            // bereitsBehandeltAnderesBemerkung
            basePrinter.addKeyValue(
                "printer.kinderdossier.anlass.bereitsBehandeltAnderesBemerkung",
                KinderdossierPrintUtil.extractUntersuchungMassnahmen(
                    untersuchung,
                    UntersuchungMassnahmen::getBereitsBehandeltAnderesBemerkung),
                document);
        }

        // informationEltern
        if (anlassConfig.isShowMassnahmenInfoEltern()) {
            basePrinter.addKeyValue(
                "printer.kinderdossier.anlass.informationenEltern",
                KinderdossierPrintUtil.extractUntersuchungMassnahmen(
                    untersuchung,
                    untersuchungMassnahmen
                        -> basePrinter.translateValue(untersuchungMassnahmen.getInformationEltern())),
                document);
        }

        // abgegebenesInfomaterial
        if (anlassConfig.isShowMassnahmenInfomaterial()) {
            basePrinter.addKeyValue(
                "printer.kinderdossier.anlass.abgegebenesInfomaterial",
                KinderdossierPrintUtil.extractUntersuchungMassnahmen(
                    untersuchung,
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
                    untersuchung,
                    UntersuchungMassnahmen::isUeberweisungHausarzt),
                document);
        }

        // ueberweisungAugenarzt
        if (anlassConfig.isShowMassnahmenUeberweisungAugenarzt()) {
            basePrinter.addKeyValue(
                "printer.kinderdossier.anlass.ueberweisungAugenarzt",
                KinderdossierPrintUtil.extractUntersuchungMassnahmenBooleanFn(
                    untersuchung,
                    UntersuchungMassnahmen::isUeberweisungAugenarzt),
                document);
        }

        // bemerkungen
        basePrinter.addVerticalKeyValue(
            BEMERKUNG_KEY,
            KinderdossierPrintUtil.extractUntersuchungMassnahmen(untersuchung, UntersuchungMassnahmen::getBemerkungen),
            document);

        // nachkontrolle
        if (anlassConfig.isShowMassnahmenNachkontrolleSaed()) {
            addNachkontrolleTable(untersuchung, basePrinter, document);
        }
    }

    private static void addNachkontrolleTable(
        @Nullable Untersuchung untersuchung,
        @NonNull BasePrinter basePrinter,
        @NonNull Document document) {
        basePrinter.addKeyValue(
            "printer.kinderdossier.anlass.nachkontrolle",
            "",
            document);

        AnlassFlags nachkontrolle =
            KinderdossierPrintUtil.extractAnlassFlags(untersuchung, UntersuchungMassnahmen::getNachkontrolle);

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
        @Nullable Untersuchung untersuchung,
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
                            untersuchung,
                            UntersuchungMassnahmen::isBereitsBehandeltSehen)),
                    basePrinter.translateValue(
                        KinderdossierPrintUtil.extractUntersuchungMassnahmenBooleanFn(
                            untersuchung,
                            UntersuchungMassnahmen::isBereitsBehandeltHoeren)),
                    basePrinter.translateValue(
                        KinderdossierPrintUtil.extractUntersuchungMassnahmenBooleanFn(
                            untersuchung,
                            UntersuchungMassnahmen::isBereitsBehandeltGroesseGewicht)),
                    basePrinter.translateValue(
                        KinderdossierPrintUtil.extractUntersuchungMassnahmenBooleanFn(
                            untersuchung,
                            UntersuchungMassnahmen::isBereitsBehandeltSprache)),
                    basePrinter.translateValue(
                        KinderdossierPrintUtil.extractUntersuchungMassnahmenBooleanFn(
                            untersuchung,
                            UntersuchungMassnahmen::isBereitsBehandeltMotorik)),
                    basePrinter.translateValue(
                        KinderdossierPrintUtil.extractUntersuchungMassnahmenBooleanFn(
                            untersuchung,
                            UntersuchungMassnahmen::isBereitsBehandeltAnderes)
                    )
                )
            ),
            document,
            true);
    }

    public static void addLaeusekontrolle(
        @NonNull AnlassConfig anlassConfig,
        @Nullable Untersuchung untersuchung,
        @NonNull BasePrinter basePrinter,
        @NonNull Document document
    ) {
        if (!AnlassConfigUtil.showTabLaeusekontrolle(anlassConfig)) {
            return;
        }

        if (untersuchung != null
            && untersuchung.getUntersuchungLaeusekontrolle() != null) {

            basePrinter.addH3Title(
                I18nMessage.of("printer.kinderdossier.anlass.subtitle.laeusekontrolle.label"),
                document);

            // Beurteilung
            if (anlassConfig.getLaeusekontrolleBeurteilung().isVisible()) {
                basePrinter.addVerticalKeyValue(
                    "printer.kinderdossier.anlass.laeusekontrolle.beurteilung",
                    KinderdossierPrintUtil.extractUntersuchungLaeusekontrolle(
                        untersuchung,
                        untersuchungHoeren -> untersuchungHoeren.getBeurteilung().stream()
                            .map(basePrinter::translateValue)
                            .collect(Collectors.joining(", "))),
                    document);
            }

            // Bemerkung
            if (anlassConfig.getLaeusekontrolleBemerkungen().isVisible()) {
                basePrinter.addKeyValue(
                    "printer.kinderdossier.anlass.laeusekontrolle.bemerkungen",
                    KinderdossierPrintUtil.extractUntersuchungLaeusekontrolle(
                        untersuchung,
                        u -> trimToPlaceholder(u.getBemerkungen())),
                    document);
            }
        }
    }

    public static void adduntersuchungTriage(
        @NonNull AnlassConfig anlassConfig,
        @Nullable Untersuchung untersuchung,
        @NonNull BasePrinter basePrinter,
        @NonNull Document document
    ) {
        if (
            !(anlassConfig.isShowTriageTriage()) ||
                untersuchung == null ||
                untersuchung.getUntersuchungTriage() == null
        ) {
            return;
        }

        basePrinter.addH3Title(I18nMessage.of("printer.kinderdossier.anlass.subtitle.triage.label"), document);
        UntersuchungTriage untersuchungTriage = untersuchung.getUntersuchungTriage();

        List<PrintUtilRecord<UntersuchungTriage>> printerList = List.of(
            new PrintUtilRecord<>(
                "printer.kinderdossier.anlass.triage.triage-su.label",
                untersuchungTriage.isTriageSu(),
                t -> trimToPlaceholder(t.getTriageSuBemerkungen())),
            new PrintUtilRecord<>(
                "printer.kinderdossier.anlass.triage.triage-mpa.label",
                untersuchungTriage.isTriageMpa(),
                t -> trimToPlaceholder(t.getTriageMpaBemerkungen())),
            new PrintUtilRecord<>(
                "printer.kinderdossier.anlass.triage.befund-su.label",
                untersuchungTriage.isBefundSu(),
                t -> trimToPlaceholder(t.getBefundSuBemerkungen())),
            new PrintUtilRecord<>(
                "printer.kinderdossier.anlass.triage.wunsch-schueler.label",
                untersuchungTriage.isWunschSchueler(),
                t -> trimToPlaceholder(t.getWunschSchuelerBemerkungen())),
            new PrintUtilRecord<>(
                "printer.kinderdossier.anlass.triage.vorinformation-lehrperson.label",
                untersuchungTriage.isVorinformationLehrperson(),
                t -> trimToPlaceholder(t.getVorinformationLehrpersonBemerkungen()))
        );

        printerList.stream()
            .filter(PrintUtilRecord::isVisible)
            .forEach(printerRecord ->
                basePrinter.addVerticalKeyValue(
                    printerRecord.titleKey(),
                    printerRecord.stringTransformer().apply(untersuchungTriage),
                    document)
            );
    }

    public static void addAudiogramTable(
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
