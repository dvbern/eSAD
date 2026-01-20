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

package ch.dvbern.oss.hemed.esc.api.shared.validator;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.Month;
import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;

import ch.dvbern.dvbstarter.shared.types.emailaddress.EmailAddress;
import ch.dvbern.dvbstarter.shared.types.phonenumber.NormalizedPhoneNumber;
import ch.dvbern.oss.hemed.esc.api.features.anlass.Anlass;
import ch.dvbern.oss.hemed.esc.api.features.anlass.einladung.Einladung;
import ch.dvbern.oss.hemed.esc.api.features.anlass.untersuchung.Untersuchung;
import ch.dvbern.oss.hemed.esc.api.features.anlass.impfung.UntersuchungImpfstatusKrankheit;
import ch.dvbern.oss.hemed.esc.api.features.anlass.impfung.UntersuchungImpfungIntern;
import ch.dvbern.oss.hemed.esc.api.features.anlass.impfung.UntersuchungImpfungen;
import ch.dvbern.oss.hemed.esc.api.features.anlass.sehen.UntersuchungSehenBemerkungFragebogen;
import ch.dvbern.oss.hemed.esc.api.features.anlass.sehen.UntersuchungSehenBeurteilung;
import ch.dvbern.oss.hemed.esc.api.features.anlass.aerztlicheuntersuchung.types.Einbeinhuepfen;
import ch.dvbern.oss.hemed.esc.api.features.anlass.aerztlicheuntersuchung.types.Einbeinstand;
import ch.dvbern.oss.hemed.esc.api.features.anlass.aerztlicheuntersuchung.types.Motorik;
import ch.dvbern.oss.hemed.esc.api.features.anlass.aerztlicheuntersuchung.types.Sprachfaehigkeit;
import ch.dvbern.oss.hemed.esc.api.features.anlass.anlassconfig.AnlassConfig;
import ch.dvbern.oss.hemed.esc.api.features.anlass.groessegewicht.types.PerzentileBmi;
import ch.dvbern.oss.hemed.esc.api.features.anlass.groessegewicht.types.PerzentileGewicht;
import ch.dvbern.oss.hemed.esc.api.features.anlass.groessegewicht.types.PerzentileGroesse;
import ch.dvbern.oss.hemed.esc.api.features.anlass.groessegewicht.UntersuchungGroesseGewicht;
import ch.dvbern.oss.hemed.esc.api.features.anlass.hoeren.Audiogramm;
import ch.dvbern.oss.hemed.esc.api.features.anlass.hoeren.types.AudiogrammBeurteilung;
import ch.dvbern.oss.hemed.esc.api.features.anlass.hoeren.types.Dezibel;
import ch.dvbern.oss.hemed.esc.api.features.anlass.hoeren.types.HoerenZusaetzlicheAngaben;
import ch.dvbern.oss.hemed.esc.api.features.anlass.hoeren.types.OtoskopieResultat;
import ch.dvbern.oss.hemed.esc.api.features.anlass.hoeren.UntersuchungHoerenAudiogramm;
import ch.dvbern.oss.hemed.esc.api.features.anlass.hoeren.UntersuchungHoerenOtoskopie;
import ch.dvbern.oss.hemed.esc.api.features.anlass.massnahmen.UntersuchungMassnahmen;
import ch.dvbern.oss.hemed.esc.api.features.anlass.types.AnlassArt;
import ch.dvbern.oss.hemed.esc.api.features.anlass.types.AnlassStatus;
import ch.dvbern.oss.hemed.esc.api.features.anlass.types.AnlassTyp;
import ch.dvbern.oss.hemed.esc.api.features.anlass.sehen.types.BrilleLinsenResultat;
import ch.dvbern.oss.hemed.esc.api.features.anlass.sehen.types.DistanzSehenResultat;
import ch.dvbern.oss.hemed.esc.api.features.anlass.sehen.types.FarbsehenResultat;
import ch.dvbern.oss.hemed.esc.api.features.anlass.hoeren.types.HoerenEmpfehlung;
import ch.dvbern.oss.hemed.esc.api.features.anlass.sehen.types.InspektionKonvergenzResultat;
import ch.dvbern.oss.hemed.esc.api.features.anlass.sehen.types.MotilitaetResultat;
import ch.dvbern.oss.hemed.esc.api.features.anlass.anlassconfig.types.RequirementSetting;
import ch.dvbern.oss.hemed.esc.api.features.anlass.sehen.types.StereotestResultat;
import ch.dvbern.oss.hemed.esc.api.features.anlass.sehen.types.StrabismusAuftretenResultat;
import ch.dvbern.oss.hemed.esc.api.features.anlass.sehen.types.StrabismusAuspraegungResultat;
import ch.dvbern.oss.hemed.esc.api.features.anlass.sehen.types.StrabismusResultat;
import ch.dvbern.oss.hemed.esc.api.features.anlass.sehen.types.VerwendetTafelResultat;
import ch.dvbern.oss.hemed.esc.api.features.arzt.Arzt;
import ch.dvbern.oss.hemed.esc.api.features.arzt.types.Fachrichtung;
import ch.dvbern.oss.hemed.esc.api.features.arzt.types.HpvTeilnahme;
import ch.dvbern.oss.hemed.esc.api.features.arzt.types.Organisation;
import ch.dvbern.oss.hemed.esc.api.features.impfstoff.Impfstoff;
import ch.dvbern.oss.hemed.esc.api.features.impfstoff.types.Krankheit;
import ch.dvbern.oss.hemed.esc.api.features.kinder.Dossier;
import ch.dvbern.oss.hemed.esc.api.features.kinder.Kind;
import ch.dvbern.oss.hemed.esc.api.features.kinder.Nachkontrolle;
import ch.dvbern.oss.hemed.esc.api.features.kinder.kontakt_person_kind.KontaktPersonKind;
import ch.dvbern.oss.hemed.esc.api.features.schule.Schule;
import ch.dvbern.oss.hemed.esc.api.features.schule.klasse.Klasse;
import ch.dvbern.oss.hemed.esc.api.features.schule.quartier.Quartier;
import ch.dvbern.oss.hemed.esc.api.features.schule.types.Klassenstufe;
import ch.dvbern.oss.hemed.esc.api.shared.anlass.anlassflags.AnlassFlags;
import ch.dvbern.oss.hemed.esc.api.shared.auth.FachRolle;
import ch.dvbern.oss.hemed.esc.api.shared.benutzer.Benutzer;
import ch.dvbern.oss.hemed.esc.api.shared.person.Adresse;
import ch.dvbern.oss.hemed.esc.api.shared.person.Geschlecht;
import ch.dvbern.oss.hemed.esc.api.shared.person.Land;
import ch.dvbern.oss.hemed.esc.api.shared.types.DateRange;
import ch.dvbern.oss.hemed.esc.api.shared.types.DateTimeRange;
import ch.dvbern.oss.hemed.esc.api.shared.types.EntityAktivStatus;
import ch.dvbern.oss.hemed.esc.api.shared.util.EnumSetUtil;
import org.apache.commons.text.RandomStringGenerator;
import org.apache.commons.text.RandomStringGenerator.Builder;
import org.checkerframework.checker.nullness.qual.NonNull;

/**
 * This class is used to create Testdata that will be used to fill all Properties of a Kind / Kind with an Anlass.
 * Those will then be used to create Testbriefe
 */
public final class MergeSourceDummyDataCreator {

    private MergeSourceDummyDataCreator() {
    }

    @NonNull
    private static Benutzer getTestBenutzer() {
        return new Benutzer(
            null,
            EmailAddress.fromString("julien.schularzt@mailbucket.dvbern.ch"),
            "Dr.",
            "Schularzt",
            "Julien",
            NormalizedPhoneNumber.parse("+41 76 123 45 67"),
            null,
            EnumSetUtil.toString(EnumSet.of(FachRolle.SCHULAERZTIN)),
            Collections.emptySet(),
            true,
            null
        );
    }

    @NonNull
    private static Benutzer getTestMPA() {
        return new Benutzer(
            null,
            EmailAddress.fromString("monika.mpa@mailbucket.dvbern.ch"),
            "Med. Praxisassistentin",
            "Mate",
            "Monika",
            NormalizedPhoneNumber.parse("+41 76 123 45 67"),
            null,
            EnumSetUtil.toString(EnumSet.of(FachRolle.MPA)),
            Collections.emptySet(),
            true,
            null
        );
    }

    @NonNull
    private static Schule getTestSchule() {
        return new Schule(
            null,
            "Dreirosen",
            "Nussbaumstrasse 21",
            "Volksschulen",
            new Quartier("GBO"),
            "452",
            null,
            EmailAddress.fromString("elementaire@efdb.ch"),
            NormalizedPhoneNumber.parse("+41 79 765 43 21"),
            "Séction élémentaire",
            getTestBenutzer(),
            getTestMPA(),
            EntityAktivStatus.INAKTIV // Inkaktiv because this is a dummy schule
        );
    }

    @NonNull
    private static Klasse getTestKlasse() {
        return new Klasse(
            null,
            getTestSchule(),
            new ArrayList<>(),
            "Klasse 4a",
            Klassenstufe.PRIMAR_KLASSE_4,
            "Sek A",
            EntityAktivStatus.INAKTIV // Inkaktiv because this is a dummy schule
        );
    }

    @NonNull
    public static Kind getTestKind() {
        Adresse adresse = new Adresse(
            "Bahnhofstrasse 10",
            "c/o Muster",
            "3000",
            "Bern",
            Land.SCHWEIZ
        );

        return new Kind(
            null,
            null,
            null,
            "Notter",
            "Nina",
            "Notter",
            "Nina",
            Geschlecht.WEIBLICH,
            LocalDate.of(2015, 3, 15),
            Land.SCHWEIZ,
            "Deutsch/Chinesisch",
            "Eltern",
            adresse,
            EmailAddress.fromString("nina.notter@mailbucket.dvbern.ch"),
            NormalizedPhoneNumber.parse("+41 79 123 67 45"),
            getTestSchule(),
            getTestKlasse(),
            EntityAktivStatus.AKTIV);
    }

    @NonNull
    public static Dossier getTestDossier(@NonNull Kind kind) {
        Dossier dossier = Dossier.forKind(kind);
        dossier.setZustaendigArzt(getTestBenutzer());
        return dossier;
    }

    @NonNull
    public static Nachkontrolle getTestNachkontrolle(@NonNull Dossier dossier) {
        return new Nachkontrolle(
            dossier,
            new AnlassFlags(true, true, true, true, true, true)
        );
    }

    @NonNull
    private static Impfstoff createTestImpfstoff(@NonNull String name, @NonNull List<Krankheit> krankheiten) {
        Impfstoff impfstoff = new Impfstoff();
        impfstoff.setName(name);
        impfstoff.setKrankheiten(krankheiten);
        return impfstoff;
    }

    @NonNull
    public static Anlass getTestAnlass() {
        LocalDate anlassDate = LocalDate.of(2024, 2, 14);
        int schuljahr = anlassDate.isAfter(LocalDate.of(anlassDate.getYear(), Month.AUGUST, 1))
            ? anlassDate.getYear() : anlassDate.getYear() - 1;
        return new Anlass(
            getAnlassConfig(),
            getRandomString("Test-Anlass"),
            AnlassStatus.IN_PLANUNG,
            anlassDate,
            schuljahr,
            DateTimeRange.of(anlassDate.atTime(10, 14), anlassDate.atTime(12, 30)),
            AnlassArt.GRUPPEN_ANLASS,
            getTestBenutzer(),
            null,
            getTestMPA(),
            null,
            null,
            null
        );
    }

    @NonNull
    private static Einladung createDefaultEinladung(@NonNull Anlass anlass, @NonNull Dossier dossier) {
        return Einladung.create(anlass, dossier);
    }

    @NonNull
    private static UntersuchungImpfungIntern getTestUntersuchungImpfungIntern(
        @NonNull Impfstoff impfstoff,
        @NonNull UntersuchungImpfungen untersuchungImpfungen,
        @NonNull LocalDate datumImpfung
    ) {
        UntersuchungImpfungIntern untersuchungImpfungIntern = new UntersuchungImpfungIntern();
        untersuchungImpfungIntern.setImpfstoff(impfstoff);
        untersuchungImpfungIntern.setUntersuchungImpfungen(untersuchungImpfungen);
        untersuchungImpfungIntern.setDatumImpfung(datumImpfung);
        return untersuchungImpfungIntern;
    }

    @NonNull
    public static List<UntersuchungImpfungIntern> getTestImpfungList(@NonNull Dossier dossier, @NonNull Anlass anlass) {
        List<UntersuchungImpfungIntern> impfungList = new ArrayList<>();

        Einladung einl = MergeSourceDummyDataCreator.createDefaultEinladung(anlass, dossier);
        Untersuchung untersuchung = Untersuchung.forEinladung(einl);
        UntersuchungImpfungen untersuchungImpfungen = UntersuchungImpfungen.forUntersuchung(untersuchung);
        impfungList.add(getTestUntersuchungImpfungIntern(
            createTestImpfstoff(
                "Boostrix",
                List.of(Krankheit.DIPHTERIE, Krankheit.TETANUS, Krankheit.PERTUSSIS)),
            untersuchungImpfungen,
            LocalDate.of(2025, 3, 13)));
        impfungList.add(getTestUntersuchungImpfungIntern(
            createTestImpfstoff(
                "VARIVAX",
                List.of(Krankheit.VARIZELLEN)),
            untersuchungImpfungen,
            LocalDate.of(2025, 3, 13)));
        impfungList.add(getTestUntersuchungImpfungIntern(
            createTestImpfstoff(
                "VARIVAX",
                List.of(Krankheit.VARIZELLEN)),
            untersuchungImpfungen,
            LocalDate.of(2025, 4, 20)));

        return impfungList;
    }

    @NonNull
    public static List<UntersuchungImpfstatusKrankheit> getTestImpfStatusList() {
        List<UntersuchungImpfstatusKrankheit> impfstatusList = new ArrayList<>();

        for (Krankheit krankheit : Krankheit.values()) {
            UntersuchungImpfstatusKrankheit untersuchungImpfstatusKrankheit = new UntersuchungImpfstatusKrankheit();
            untersuchungImpfstatusKrankheit.setKrankheit(krankheit);
            untersuchungImpfstatusKrankheit.setAnzahlImpfungen(3);
            untersuchungImpfstatusKrankheit.setDatumLetzteImpfung(LocalDate.of(2025, 5, 20));

            impfstatusList.add(untersuchungImpfstatusKrankheit);
        }

        return impfstatusList;
    }

    @NonNull
    public static Arzt getTestArzt() {
        Adresse adresse = new Adresse(
            "Bahnhofstrasse 10",
            "c/o Muster",
            "3000",
            "Bern",
            Land.SCHWEIZ
        );
        return new Arzt(
            "Dr. Med.",
            Fachrichtung.ANDERES,
            Organisation.NATUERLICHE_PERSON,
            "Armin", "Arzt", adresse,
            NormalizedPhoneNumber.parse("+41 79 123 45 67"),
            EmailAddress.fromString("elementaire@efdb.ch"),
            "Eine Ärztliche bemerkung",
            HpvTeilnahme.JA
        );
    }

    @NonNull
    public static KontaktPersonKind getTestKontaktPersonKind(@NonNull Kind kind) {
        Set<Kind> kinder = Set.of(kind);
        return new KontaktPersonKind(
            null,
            kinder,
            "Kontaktperson",
            "Karin",
            Geschlecht.WEIBLICH,
            null,
            null,
            null,
            null,
            true
        );
    }

    @NonNull
    public static Untersuchung getTestUntersuchung() {
        Untersuchung untersuchung = new Untersuchung();
        UntersuchungSehenBeurteilung untersuchungSehenBeurteilung = new UntersuchungSehenBeurteilung();
        untersuchungSehenBeurteilung.setBrilleLinsen(BrilleLinsenResultat.MIT);
        untersuchungSehenBeurteilung.setStereotest(StereotestResultat.NORMAL);
        untersuchungSehenBeurteilung.setFarbsehen(FarbsehenResultat.NORMAL);
        untersuchungSehenBeurteilung.setVerwendeteTafel(VerwendetTafelResultat.E_TAFEL);
        untersuchungSehenBeurteilung.setFernvisus(EnumSet.of(
            DistanzSehenResultat.NORMAL,
            DistanzSehenResultat.LINKS_REDUZIERT));
        untersuchungSehenBeurteilung.setFernvisusRechts(BigDecimal.valueOf(100));
        untersuchungSehenBeurteilung.setFernvisusLinks(BigDecimal.valueOf(200));
        untersuchungSehenBeurteilung.setNahvisus(EnumSet.of(
            DistanzSehenResultat.NORMAL,
            DistanzSehenResultat.RECHTS_REDUZIERT));
        untersuchungSehenBeurteilung.setNahvisusRechts(BigDecimal.valueOf(300));
        untersuchungSehenBeurteilung.setNahvisusLinks(BigDecimal.valueOf(400));
        untersuchungSehenBeurteilung.setStrabismus(EnumSet.of(StrabismusResultat.ESO, StrabismusResultat.HYPER));
        untersuchungSehenBeurteilung.setStrabismusAuspraegung(
            StrabismusAuspraegungResultat.INTERMITTIEREND);
        untersuchungSehenBeurteilung.setStrabismusAuftreten(
            StrabismusAuftretenResultat.RECHTS);
        untersuchungSehenBeurteilung.setInspektion(InspektionKonvergenzResultat.AUFFAELLIG);
        untersuchungSehenBeurteilung.setKonvergenz(InspektionKonvergenzResultat.NICHT_GEPRUEFT);
        untersuchungSehenBeurteilung.setMotilitaet(MotilitaetResultat.NORMAL);

        untersuchung.setUntersuchungSehenBemerkungFragebogen(new UntersuchungSehenBemerkungFragebogen(
            untersuchung,
            "Meine Fragebon Bemerkung für sehen"));
        untersuchung.setUntersuchungSehenBeurteilung(untersuchungSehenBeurteilung);
        untersuchung.setUntersuchungHoerenAudiogramm(getTestUntersuchungHoerenAudiogramm(untersuchung));
        untersuchung.setUntersuchungHoerenOtoskopie(getTestUntersuchungHoerenOtoskopie(untersuchung));
        untersuchung.setUntersuchungMassnahmen(new UntersuchungMassnahmen());
        untersuchung.setUntersuchungGroesseGewicht(getTestUntersuchungGroesseGewicht());
        return untersuchung;
    }

    @NonNull
    private static UntersuchungHoerenAudiogramm getTestUntersuchungHoerenAudiogramm(
        @NonNull Untersuchung untersuchung
    ) {
        UntersuchungHoerenAudiogramm hoerenAudiogramm = new UntersuchungHoerenAudiogramm();
        Audiogramm audiogrammRechts = new Audiogramm(
            Dezibel.DB20,
            Dezibel.DB40,
            Dezibel.DB10,
            Dezibel.DB50,
            Dezibel.MORE_THAN_DB60,
            Dezibel.DB60,
            Dezibel.DB30);
        Audiogramm audiogrammLinks = new Audiogramm(
            Dezibel.DB30,
            Dezibel.DB60,
            Dezibel.DB10,
            Dezibel.DB10,
            Dezibel.MORE_THAN_DB60,
            Dezibel.DB20,
            Dezibel.DB20);

        hoerenAudiogramm.setAudiogrammRechts(audiogrammRechts);
        hoerenAudiogramm.setAudiogrammLinks(audiogrammLinks);
        hoerenAudiogramm.setAudiogrammBeurteilung(EnumSet.of(
            AudiogrammBeurteilung.NORMAL,
            AudiogrammBeurteilung.LINKS_REDUZIERT,
            AudiogrammBeurteilung.RECHTS_REDUZIERT));
        hoerenAudiogramm.setZusaetzlicheAngaben(EnumSet.of(
            HoerenZusaetzlicheAngaben.ERKAELTUNG,
            HoerenZusaetzlicheAngaben.HOERHILFE));
        hoerenAudiogramm.setEmpfehlung(EnumSet.of(
            HoerenEmpfehlung.UEBERPRUEFUNG_ARZT));
        hoerenAudiogramm.setBemerkungen("Test Bemerkung für Audiogramm");
        hoerenAudiogramm.setUntersuchung(untersuchung);
        return hoerenAudiogramm;
    }

    @NonNull
    private static UntersuchungHoerenOtoskopie getTestUntersuchungHoerenOtoskopie(@NonNull Untersuchung untersuchung) {
        UntersuchungHoerenOtoskopie hoerenOtoskopie = new UntersuchungHoerenOtoskopie();
        hoerenOtoskopie.setOtoskopieRechts(EnumSet.of(
            OtoskopieResultat.NORMAL,
            OtoskopieResultat.SEROMUKOTYMPANON));
        hoerenOtoskopie.setOtoskopieLinks(EnumSet.of(
            OtoskopieResultat.ANDERES,
            OtoskopieResultat.TROMMELFELL_CERUMEN));
        hoerenOtoskopie.setBemerkungen("Test Bemerkung für Otoskopie");
        hoerenOtoskopie.setUntersuchung(untersuchung);

        return hoerenOtoskopie;
    }

    @NonNull
    private static UntersuchungGroesseGewicht getTestUntersuchungGroesseGewicht() {
        UntersuchungGroesseGewicht untersuchungGroesseGewicht = new UntersuchungGroesseGewicht();
        untersuchungGroesseGewicht.setGroesse(150);
        untersuchungGroesseGewicht.setGewicht(BigDecimal.valueOf(70));
        untersuchungGroesseGewicht.setBmi(BigDecimal.valueOf(31.1));
        untersuchungGroesseGewicht.setGroessePerzentile(PerzentileGroesse.NORMAL);
        untersuchungGroesseGewicht.setGewichtPerzentile(PerzentileGewicht.PERZENTILE_HOCH);
        untersuchungGroesseGewicht.setBmiPerzentile(PerzentileBmi.PERZENTILE_HOCH);
        return untersuchungGroesseGewicht;
    }

    @NonNull
    private static String getRandomString(@NonNull String prefix) {
        char[][] pairs = { { 'a', 'z' }, { 'A', 'Z' } };
        RandomStringGenerator generator = new Builder()
            .withinRange(pairs)
            .get();
        return prefix + '_' + generator.generate(10);
    }

    @NonNull
    private static AnlassConfig getAnlassConfig() {
        return new AnlassConfig(
            AnlassTyp.KLASSE_4,
            DateRange.ofFullRange(),
            "Default",
            true,            // showImpfausweis
            true,                           // showFragebogen
            true,                           // showAnmeldungBestaetigung
            true,                           // showDatenPersonendaten
            true,                           // showDatenPersonendatenEsadId
            RequirementSetting.OPTIONAL,    // datenUntersuchung
            true,                           // showDatenSchule
            RequirementSetting.OPTIONAL,    // datenBemerkung
            RequirementSetting.OPTIONAL,    // impfungenUebersicht
            RequirementSetting.OPTIONAL,    // impfungenListe
            true,                           // showImpfungenDoppelkontrolle
            true,                           // impfungenMassnahmeKeineMassnahmen
            true,                           // impfungenMassnahmeGruppenFolgetermin
            true,                           // impfungenMassnahmeImpfenFolgetermin
            true,                           // impfungenMassnahmeEinzelFolgetermin
            true,                           // impfungenMassnahmeInfoKinderarzt
            true,                           // impfungenMassnahmeBeratungMPA
            true,                           // impfungenMassnahmeInfomaterialAbgegeben
            true,                           // impfungenMassnahmeInfoEltern
            true,                           // impfungenMassnahmeRueckfrageKinderarzt
            RequirementSetting.OPTIONAL,    // checkEinwilligungVorhanden
            RequirementSetting.OPTIONAL,    // checkKontraindikationenAbgeklaert
            RequirementSetting.OPTIONAL,    // checkDoppelkontrolleImpfentscheidDurchgefuehrt
            RequirementSetting.OPTIONAL,    // untersuchungImpfungEmpfehlung
            RequirementSetting.OPTIONAL,    // untersuchungImpfungAchtung
            RequirementSetting.OPTIONAL,    // untersuchungImpfungBemerkungen
            EnumSetUtil.toString(EnumSet.allOf(Krankheit.class)), // untersuchungImpfungKrankheitenLinksValues
            EnumSetUtil.toString(EnumSet.allOf(Krankheit.class)), // untersuchungImpfungKrankheitenRechtsValues
            RequirementSetting.OPTIONAL,    // sehenBrilleLinsen
            RequirementSetting.OPTIONAL,    // sehenFarbsehen
            RequirementSetting.OPTIONAL,    // sehenTafel
            RequirementSetting.OPTIONAL,    // sehenFernvisus
            RequirementSetting.OPTIONAL,    // sehenStereoTest
            RequirementSetting.OPTIONAL,    // sehenStrabismus
            RequirementSetting.OPTIONAL,    // sehenInspektion
            RequirementSetting.OPTIONAL,    // sehenKonvergenz
            RequirementSetting.OPTIONAL,    // sehenMotilitaet
            RequirementSetting.OPTIONAL,    // sehenNahvisus
            RequirementSetting.OPTIONAL,    // sehenEmpfehlung
            RequirementSetting.OPTIONAL,    // sehenBemerkungen
            RequirementSetting.OPTIONAL,    // sehenBemerkungenFragebogen
            RequirementSetting.OPTIONAL,    // sehenMassnahmen
            RequirementSetting.OPTIONAL,    // hoerenAudiogramm
            RequirementSetting.OPTIONAL,    // hoerenAudiogramm250Hz
            RequirementSetting.OPTIONAL,    // hoerenAudiogramm8000Hz
            RequirementSetting.OPTIONAL,    // hoerenAudiogrammBeurteilung
            RequirementSetting.OPTIONAL,    // hoerenAudiogrammZusaetzlicheAngaben
            RequirementSetting.OPTIONAL,    // hoerenAudiogrammEmpfehlung
            RequirementSetting.OPTIONAL,    // hoerenAudiogrammBemerkungen
            RequirementSetting.OPTIONAL,    // hoerenOtoskopie
            RequirementSetting.OPTIONAL,    // hoerenOtoskopieBemerkungen
            RequirementSetting.OPTIONAL,    // groesseGewichtGroesse
            RequirementSetting.OPTIONAL,    // groesseGewichtGewicht
            RequirementSetting.OPTIONAL,    // groesseGewichtBmi
            RequirementSetting.OPTIONAL,    // groesseGewichtGroessePerzentile
            RequirementSetting.OPTIONAL,    // groesseGewichtGewichtPerzentile
            RequirementSetting.OPTIONAL,    // groesseGewichtBmiPerzentile
            EnumSetUtil.toString(EnumSet.allOf(PerzentileBmi.class)),// groesseGewichtBmiPerzentileValues
            RequirementSetting.OPTIONAL,    // groesseGewichtEmpfehlung
            RequirementSetting.OPTIONAL,    // groesseGewichtBemerkungen
            true,                           // showTriageTriage
            true,                           // showTriageKindWeiterleiten
            RequirementSetting.OPTIONAL,    // laeusekontrolleBeurteilung
            RequirementSetting.OPTIONAL,    // laeusekontrolleEmpfehlung
            RequirementSetting.OPTIONAL,    // laeusekontrolleBemerkungen
            RequirementSetting.OPTIONAL,    // untersuchungBewegungsapparat
            RequirementSetting.OPTIONAL,    // untersuchungEinbeinstand
            RequirementSetting.OPTIONAL,    // untersuchungEinbeinstandRechtsLinks
            EnumSetUtil.toString(EnumSet.allOf(Einbeinstand.class)),// untersuchungEinbeinstandValues
            RequirementSetting.OPTIONAL,    // untersuchungEinbeinhuepfen
            RequirementSetting.OPTIONAL,    // untersuchungEinbeinhuepfenRechtsLinks
            EnumSetUtil.toString(EnumSet.allOf(Einbeinhuepfen.class)),// untersuchungEinbeinhuepfenValues
            RequirementSetting.OPTIONAL,    // untersuchungFingeropposition
            RequirementSetting.OPTIONAL,    // untersuchungHampelmann
            RequirementSetting.OPTIONAL,    // untersuchungBallFangen
            RequirementSetting.OPTIONAL,    // untersuchungBallWerfen
            RequirementSetting.OPTIONAL,    // untersuchungNameSchreiben
            RequirementSetting.OPTIONAL,    // untersuchungFigurNachmalen
            RequirementSetting.OPTIONAL,    // untersuchungStifthaltung
            RequirementSetting.OPTIONAL,    // untersuchungAussprache
            RequirementSetting.OPTIONAL,    // untersuchungLesefit
            RequirementSetting.OPTIONAL,    // untersuchungLesefitPunkte
            RequirementSetting.OPTIONAL,    // untersuchungZahlenNachsprechen4
            RequirementSetting.OPTIONAL,    // untersuchungZahlenNachsprechen5
            RequirementSetting.OPTIONAL,    // untersuchungZeichnenKreis
            RequirementSetting.OPTIONAL,    // untersuchungZeichnenViereck
            RequirementSetting.OPTIONAL,    // untersuchungZeichnenDreieck
            RequirementSetting.OPTIONAL,    // untersuchungZeichnenMensch
            RequirementSetting.OPTIONAL,    // untersuchungZeichnenMenschPunkte
            RequirementSetting.OPTIONAL,    // untersuchungMotorik
            EnumSetUtil.toString(EnumSet.allOf(Motorik.class)),// untersuchungMotorikValues
            RequirementSetting.OPTIONAL,    // untersuchungMotorikPunkte
            RequirementSetting.OPTIONAL,    // untersuchungKognition
            RequirementSetting.OPTIONAL,    // untersuchungKognitionPunkte
            RequirementSetting.OPTIONAL,    // untersuchungSprachfaehigkeitSingleValue
            RequirementSetting.OPTIONAL,    // untersuchungSprachfaehigkeitMultiValue
            EnumSetUtil.toString(EnumSet.allOf(Sprachfaehigkeit.class)),// untersuchungSprachfeahigkeitValues
            RequirementSetting.OPTIONAL,    // untersuchungSprachePunkte
            RequirementSetting.OPTIONAL,    // untersuchungMundZaehne
            RequirementSetting.OPTIONAL,    // untersuchungHaut
            RequirementSetting.OPTIONAL,    // untersuchungHerz
            RequirementSetting.OPTIONAL,    // untersuchungBlutdruck
            RequirementSetting.OPTIONAL,    // untersuchungVerhalten
            RequirementSetting.OPTIONAL,    // unterlagenEinverstaendnis
            RequirementSetting.OPTIONAL,    // unterlagenVerhaltensfragebogen
            RequirementSetting.OPTIONAL,    // untersuchungEmpfehlung
            RequirementSetting.OPTIONAL,    // untersuchungBemerkungen
            true,                           // showMassnahmenPathologischeBefunde
            true,                           // showMassnahmenKeine
            true,                           // showMassnahmenBeratungMpa
            true,                           // showMassnahmenBeratungSchulAerztin
            true,                           // showMassnahmenBereitsBehandelt
            true,                           // showMassnahmenInfomaterial
            true,                           // showMassnahmenInfoEltern
            true,                           // showMassnahmenUeberweisungHausarzt
            true,                           // showMassnahmenUeberweisungAugenarzt
            true,                           // showMassnahmenNachkontrolleSaed
            true                            // showMassnahmenAnderes
        );
    }
}
