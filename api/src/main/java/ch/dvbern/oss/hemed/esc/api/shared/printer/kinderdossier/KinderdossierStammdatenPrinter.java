package ch.dvbern.oss.hemed.esc.api.shared.printer.kinderdossier;

import java.util.List;

import ch.dvbern.oss.commons.i18nl10n.I18nMessage;
import ch.dvbern.oss.hemed.esc.api.features.kinder.Kind;
import ch.dvbern.oss.hemed.esc.api.features.kinder.kontakt_person_kind.KontaktPersonKind;
import ch.dvbern.oss.hemed.esc.api.features.schule.Schule;
import ch.dvbern.oss.hemed.esc.api.features.schule.klasse.Klasse;
import ch.dvbern.oss.hemed.esc.api.shared.printer.BasePrinter;
import com.itextpdf.layout.Document;
import org.checkerframework.checker.nullness.qual.NonNull;

import static ch.dvbern.oss.hemed.esc.api.shared.printer.PrintUtil.PLACEHOLDER;

public final class KinderdossierStammdatenPrinter {

    private KinderdossierStammdatenPrinter() {
        // static and hidden so we don't get tempted to add services in here ;)
    }

    public static void addStammdaten(
        @NonNull Kind kind,
        @NonNull List<KontaktPersonKind> kontaktPersonKindList,
        @NonNull BasePrinter basePrinter,
        @NonNull Document document
    ) {
        addPersonalien(kind, basePrinter, document);

        addKontaktangaben(kind, basePrinter, document);

        addKontaktPersonKindList(kontaktPersonKindList, basePrinter, document);

        addSchuleKlasse(kind, basePrinter, document);
    }

    private static void addKontaktangaben(
        @NonNull Kind kind,
        @NonNull BasePrinter basePrinter,
        @NonNull Document document
    ) {
        basePrinter.addH2Title(
            I18nMessage.of("printer.kinderdossier.stammdaten.subtitle.kontaktangaben.label"),
            document);
        basePrinter.addKeyValue("printer.kinderdossier.stammdaten.email.label", kind.getEmail(), document);
        basePrinter.addKeyValue("printer.kinderdossier.stammdaten.telefon.label", kind.getTelefon(), document);
        basePrinter.addKeyValue("printer.kinderdossier.stammdaten.wohntyp.label", kind.getWohntyp(), document);
        basePrinter.addKeyValue("printer.kinderdossier.stammdaten.adresse.label", kind.getAdresse(), document);

        basePrinter.addH2Title(
            I18nMessage.of("printer.kinderdossier.stammdaten.subtitle.kontaktpersonen.label"),
            document);
    }

    private static void addPersonalien(
        @NonNull Kind kind,
        @NonNull BasePrinter basePrinter,
        @NonNull Document document
    ) {
        basePrinter.addH2Title(
            I18nMessage.of("printer.kinderdossier.stammdaten.subtitle.personalien.label"),
            document);
        basePrinter.addKeyValue("printer.kinderdossier.stammdaten.edokid-id.label", kind.getEsadId(), document);
        basePrinter.addKeyValue(
            "printer.kinderdossier.stammdaten.ahv-nummer.label",
            kind.getSozialversicherungsNummer(),
            document);
        basePrinter.addKeyValue(
            "printer.kinderdossier.stammdaten.geschlecht.label",
            kind.getGeschlecht(),
            document);
        basePrinter.addKeyValue(
            "printer.kinderdossier.stammdaten.nationalitaet.label",
            kind.getNationalitaet(),
            document);
        basePrinter.addKeyValue("printer.kinderdossier.stammdaten.erstsprache.label", kind.getErstsprache(), document);
    }

    private static void addKontaktPersonKindList(
        @NonNull List<KontaktPersonKind> kontaktPersonKindList,
        @NonNull BasePrinter basePrinter,
        @NonNull Document document
    ) {
        if (kontaktPersonKindList.isEmpty()) {
            basePrinter.addValue(I18nMessage.of("printer.table.no-entries"), false, document);
        } else {
            kontaktPersonKindList.forEach(kontaktPerson -> addKontaktPerson(kontaktPerson, basePrinter, document));
        }
    }

    private static void addKontaktPerson(
        @NonNull KontaktPersonKind kontaktPerson,
        @NonNull BasePrinter basePrinter,
        @NonNull Document document
    ) {
        basePrinter.addValue(KinderdossierPrintUtil.getNameWithAnrede(kontaktPerson, basePrinter), true, document);
        basePrinter.addKeyValue(
            "printer.kinderdossier.stammdaten.beziehung.label",
            kontaktPerson.getBeziehung(),
            document);
        basePrinter.addKeyValue(
            "printer.kinderdossier.stammdaten.adresse.label",
            kontaktPerson.getAdresse(),
            document);
        basePrinter.addKeyValue("printer.kinderdossier.stammdaten.email.label", kontaktPerson.getEmail(), document);
        basePrinter.addKeyValue("printer.kinderdossier.stammdaten.telefon.label", kontaktPerson.getTelefon(), document);
    }

    private static void addSchuleKlasse(
        @NonNull Kind kind,
        @NonNull BasePrinter basePrinter,
        @NonNull Document document
    ) {
        basePrinter.addH2Title(
            I18nMessage.of("printer.kinderdossier.stammdaten.subtitle.schule-klasse.label"),
            document);
        basePrinter.addKeyValue(
            "printer.kinderdossier.stammdaten.schulhaus.label",
            KinderdossierPrintUtil.extractKindSchuleDaten(kind, Schule::getSchulhaus),
            document);
        basePrinter.addKeyValue(
            "printer.kinderdossier.stammdaten.schulstandort.label",
            KinderdossierPrintUtil.extractKindSchuleDaten(kind, Schule::getSchulstandort),
            document);

        basePrinter.addKeyValue(
            "printer.kinderdossier.stammdaten.klassenbezeichnung.label",
            KinderdossierPrintUtil.extractKindKlasseDaten(kind, Klasse::getBezeichnung),
            document);

        if (kind.getKlasse() != null) {
            basePrinter.addKeyValue(
                "printer.kinderdossier.stammdaten.klassenstufe.label",
                kind.getKlasse().getKlassenstufe(),
                document);
        } else {
            basePrinter.addKeyValue(
                "printer.kinderdossier.stammdaten.klassenstufe.label",
                PLACEHOLDER,
                document);
        }

        basePrinter.addKeyValue(
            "printer.kinderdossier.stammdaten.klassentyp.label",
            KinderdossierPrintUtil.extractKindKlasseDaten(kind, Klasse::getKlasseSchulart),
            document);
    }
}
