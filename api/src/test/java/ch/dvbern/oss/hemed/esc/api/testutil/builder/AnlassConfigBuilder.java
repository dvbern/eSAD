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

package ch.dvbern.oss.hemed.esc.api.testutil.builder;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.EnumSet;
import java.util.function.Consumer;

import ch.dvbern.dvbstarter.shared.clock.AppClock;
import ch.dvbern.dvbstarter.shared.clock.FixedClockProvider;
import ch.dvbern.dvbstarter.shared.types.id.ID;
import ch.dvbern.oss.hemed.esc.api.features.anlass.aerztlicheuntersuchung.types.Einbeinhuepfen;
import ch.dvbern.oss.hemed.esc.api.features.anlass.aerztlicheuntersuchung.types.Einbeinstand;
import ch.dvbern.oss.hemed.esc.api.features.anlass.aerztlicheuntersuchung.types.Motorik;
import ch.dvbern.oss.hemed.esc.api.features.anlass.aerztlicheuntersuchung.types.Sprachfaehigkeit;
import ch.dvbern.oss.hemed.esc.api.features.anlass.anlassconfig.AnlassConfig;
import ch.dvbern.oss.hemed.esc.api.features.anlass.anlassconfig.AnlassConfigMapper;
import ch.dvbern.oss.hemed.esc.api.features.anlass.anlassconfig.json.AnlassConfigJson;
import ch.dvbern.oss.hemed.esc.api.features.anlass.anlassconfig.types.RequirementSetting;
import ch.dvbern.oss.hemed.esc.api.features.anlass.groessegewicht.types.PerzentileBmi;
import ch.dvbern.oss.hemed.esc.api.features.anlass.groessegewicht.types.PerzentileGewicht;
import ch.dvbern.oss.hemed.esc.api.features.anlass.groessegewicht.types.PerzentileGroesse;
import ch.dvbern.oss.hemed.esc.api.features.anlass.hoeren.types.Dezibel;
import ch.dvbern.oss.hemed.esc.api.features.anlass.massnahmen.types.ImpfungenMassnahmenEmpfehlung;
import ch.dvbern.oss.hemed.esc.api.features.anlass.sehen.types.BrilleLinsenResultat;
import ch.dvbern.oss.hemed.esc.api.features.anlass.sehen.types.StrabismusResultat;
import ch.dvbern.oss.hemed.esc.api.features.anlass.types.AnlassTyp;
import ch.dvbern.oss.hemed.esc.api.features.impfstoff.types.Krankheit;
import ch.dvbern.oss.hemed.esc.api.shared.types.DateRange;
import ch.dvbern.oss.hemed.esc.api.shared.util.EnumSetUtil;
import ch.dvbern.oss.hemed.esc.api.testutil.TestUtil;
import ch.dvbern.oss.hemed.esc.api.testutil.mapper.AnlassConfigJsonSpecMapper;
import ch.dvbern.oss.hemed.esc.generated.restassured.dto.AnlassConfigJsonSpec;

import org.assertj.core.annotation.CanIgnoreReturnValue;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;

public class AnlassConfigBuilder {

    private AnlassConfig anlassConfig;

    private AnlassConfigBuilder() {
        this.anlassConfig = defaultAnlassConfig();
        TestUtil.setEntityInfo(anlassConfig);
    }

    @NonNull
    public static AnlassConfig defaultAnlassConfig() {
        return new AnlassConfig(
            AnlassTyp.KLASSE_4,
            DateRange.ofYearContaining(LocalDate.now()),
            TestUtil.getRandomString("Default"),
            true,            // showImpfausweis
            true,                           // showFragebogen
            true,                           // showAnmeldungBestaetigung
            true,                           // showImpfeinverständnis
            true,                           // showAnlassPrintImpfen
            true,                           // showDatenPersonendaten
            true,                           // showDatenPersonendatenEsadId
            RequirementSetting.OPTIONAL,    // datenUntersuchung
            true,                           // showDatenSchule
            RequirementSetting.OPTIONAL,    // datenBemerkung
            RequirementSetting.OPTIONAL,    // vorabklaerungenFragebogenStatus
            RequirementSetting.OPTIONAL,    // vorabklaerungenEinverstaendnisImpfungen
            RequirementSetting.OPTIONAL,    // vorabklaerungenChronischeKrankheiten
            true,                           // showImpfungenHeaderImpfausweisUnvollstaendig
            true,                           // showImpfungenHeaderImpfausweisNichtVorhanden
            true,                           // showImpfungenHeaderImpfausweisZweiPlusEinsSchema
            true,                           // showImpfungenHeaderImpfausweisUpload
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
            RequirementSetting.OPTIONAL,    // impfungenMassnahmeBemerkungen
            RequirementSetting.OPTIONAL,    // checkEinwilligungVorhanden
            RequirementSetting.OPTIONAL,    // checkKontraindikationenAbgeklaert
            RequirementSetting.OPTIONAL,    // checkDoppelkontrolleImpfentscheidDurchgefuehrt
            RequirementSetting.OPTIONAL,    // untersuchungImpfungEmpfehlung
            EnumSetUtil.toString(EnumSet.allOf(ImpfungenMassnahmenEmpfehlung.class)), //
            // untersuchungImpfungEmpfehlungValues
            RequirementSetting.OPTIONAL,    // untersuchungImpfungAchtung
            RequirementSetting.OPTIONAL,    // untersuchungImpfungBemerkungen
            EnumSetUtil.toString(EnumSet.allOf(Krankheit.class)), //untersuchungImpfungKrankheitLinksValues
            EnumSetUtil.toString(EnumSet.noneOf(Krankheit.class)), //untersuchungImpfungKrankheitRechtsValues
            RequirementSetting.OPTIONAL,    // impfungenDataMinimalImpfausweisGeprueft
            RequirementSetting.OPTIONAL,    // impfungenDataMinimalImmunsuppression
            RequirementSetting.OPTIONAL,    // impfungenDataMinimalMasernImpfstatus
            RequirementSetting.OPTIONAL,    // impfungenDataMinimalMasernAnzahlImpfungen
            RequirementSetting.OPTIONAL,    // impfungenDataMinimalHpvImpfstatus
            RequirementSetting.OPTIONAL,    // impfungenDataMinimalHpvAnzahlImpfungen
            RequirementSetting.OPTIONAL,    // impfungenDataMinimalImpfempfehlung
            RequirementSetting.OPTIONAL,    // impfungenDataMinimalImpfungDurchgefuehrt
            RequirementSetting.OPTIONAL,    // impfungenNurUnvollstaendige
            RequirementSetting.OPTIONAL,    // sehenBrilleLinsen
            EnumSetUtil.toString(EnumSet.noneOf(BrilleLinsenResultat.class)), //sehenBrilleLinsenValues
            BrilleLinsenResultat.OHNE_KEINE, // sehenBrilleLinsenDefaultValue
            RequirementSetting.OPTIONAL,    // sehenFarbsehen
            RequirementSetting.OPTIONAL,    // sehenTafel
            RequirementSetting.OPTIONAL,    // sehenFernvisus
            BigDecimal.valueOf(1.0),        // sehenFernvisusDefaultValue
            RequirementSetting.OPTIONAL,    // sehenFernvisusAugeEinzeln
            RequirementSetting.OPTIONAL,    // sehenStereoTest
            RequirementSetting.OPTIONAL,    // sehenStrabismus
            EnumSetUtil.toString(EnumSet.allOf(StrabismusResultat.class)),// sehenStrabismusValues
            RequirementSetting.OPTIONAL,    // sehenStrabismusAuspraegung
            RequirementSetting.OPTIONAL,    // sehenStrabismusAuftreten
            RequirementSetting.OPTIONAL,    // sehenInspektion
            RequirementSetting.OPTIONAL,    // sehenKonvergenz
            RequirementSetting.OPTIONAL,    // sehenMotilitaet
            RequirementSetting.OPTIONAL,    // sehenNahvisus
            RequirementSetting.OPTIONAL,    // sehenEmpfehlung
            RequirementSetting.OPTIONAL,    // sehenBemerkungen
            RequirementSetting.OPTIONAL,    // sehenBemerkungenFragebogen
            RequirementSetting.OPTIONAL,    // sehenMassnahmen
            RequirementSetting.OPTIONAL,    // hoerenAudiogramm
            Dezibel.DB20,                   // hoerenAudiogrammDefaultValue
            RequirementSetting.OPTIONAL,    // hoerenAudiogramm250Hz
            Dezibel.DB20,                   // hoerenAudiogramm250HzDefaultValue
            RequirementSetting.OPTIONAL,    // hoerenAudiogramm8000Hz
            Dezibel.DB20,                   // hoerenAudiogramm8000HzDefaultValue
            RequirementSetting.OPTIONAL,    // hoerenAudiogrammSimpel
            RequirementSetting.OPTIONAL,    // hoerenAudiogrammBeurteilung
            RequirementSetting.OPTIONAL,    // hoerenAudiogrammZusaetzlicheAngaben
            RequirementSetting.OPTIONAL,    // hoerenAudiogrammEmpfehlung
            RequirementSetting.OPTIONAL,    // hoerenAudiogrammBemerkungen
            RequirementSetting.OPTIONAL,    // hoerenHoerhilfe
            RequirementSetting.OPTIONAL,    // hoerenOtoskopie
            RequirementSetting.OPTIONAL,    // hoerenOtoskopieBemerkungen
            RequirementSetting.OPTIONAL,    // groesseGewichtGroesse
            RequirementSetting.OPTIONAL,    // groesseGewichtGewicht
            RequirementSetting.OPTIONAL,    // groesseGewichtBmi
            RequirementSetting.OPTIONAL,    // groesseGewichtGroessePerzentileSingleValue
            RequirementSetting.OPTIONAL,    // groesseGewichtGroessePerzentileMultiValue
            EnumSetUtil.toString(EnumSet.allOf(PerzentileGroesse.class)),// groesseGewichtGroessePerzentileValues
            RequirementSetting.OPTIONAL,    // groesseGewichtGewichtPerzentileSingleValue
            RequirementSetting.OPTIONAL,    // groesseGewichtGewichtPerzentileMultiValue
            EnumSetUtil.toString(EnumSet.allOf(PerzentileGewicht.class)),// groesseGewichtGewichtPerzentileValues
            RequirementSetting.OPTIONAL,    // groesseGewichtBmiPerzentileSingleValue
            RequirementSetting.OPTIONAL,    // groesseGewichtBmiPerzentileMultiValue
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
            EnumSetUtil.toString(EnumSet.allOf(Einbeinstand.class)),    // untersuchungEinbeinstandValues
            RequirementSetting.OPTIONAL,    // untersuchungEinbeinhuepfen
            RequirementSetting.OPTIONAL,    // untersuchungEinbeinhuepfenRechtsLinks
            EnumSetUtil.toString(EnumSet.allOf(Einbeinhuepfen.class)),    // untersuchungEinbeinhuepfenValues
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
            EnumSetUtil.toString(EnumSet.allOf(Motorik.class)),    // untersuchungMotorikValues
            RequirementSetting.OPTIONAL,    // untersuchungMotorikPunkte
            RequirementSetting.OPTIONAL,    // untersuchungKognition
            RequirementSetting.OPTIONAL,    // untersuchungKognitionPunkte
            RequirementSetting.OPTIONAL,    // untersuchungSprachfaehigkeitSingleValue
            RequirementSetting.OPTIONAL,    // untersuchungSprachfaehigkeitMultiValue
            EnumSetUtil.toString(EnumSet.allOf(Sprachfaehigkeit.class)),    // untersuchungSprachfaehigkeitValues
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
            RequirementSetting.OPTIONAL,    // untersuchungSpracheNachsprechen
            RequirementSetting.OPTIONAL,    // untersuchungSpracheWortschatz
            RequirementSetting.OPTIONAL,    // untersuchungSpracheSprachverstaendnis
            RequirementSetting.OPTIONAL,    // untersuchungSpracheBemerkungen
            RequirementSetting.OPTIONAL,    // bewegungsapparatSkoliose
            RequirementSetting.OPTIONAL,    // bewegungsapparatHaltungsschwaeche
            RequirementSetting.OPTIONAL,    // bewegungsapparatVerkuerzungHamstringmuskulatur
            RequirementSetting.OPTIONAL,    // bewegungsapparatBemerkung
            RequirementSetting.OPTIONAL,    // untersuchungBeeintraechtigungFreiesGehen
            RequirementSetting.OPTIONAL,    // untersuchungBeeintraechtigungOhneStuetzradVelofahren
            RequirementSetting.OPTIONAL,    // untersuchungBeeintraechtigungGrafomotorik
            RequirementSetting.OPTIONAL,    // untersuchungBeeintraechtigungFeinmotorik
            RequirementSetting.OPTIONAL,    // untersuchungBeeintraechtigungGrobmotorik
            RequirementSetting.OPTIONAL,    // untersuchungBeeintraechtigungGleichgewicht
            RequirementSetting.OPTIONAL,    // untersuchungBeeintraechtigungBemerkung
            RequirementSetting.OPTIONAL,    // abschlussUeberweisungErfolgt
            RequirementSetting.OPTIONAL,    // abschlussAerztlicheValidierungErforderlich
            RequirementSetting.OPTIONAL,    // abschlussBeratungDurchgefuehrt
            RequirementSetting.OPTIONAL,    // abschlussBeurteilungEmpfohleneMassnahmen
            true,                           // signatur
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

    @NonNull
    public static AnlassConfig allSameRequirementSetting(RequirementSetting requirementSetting) {
        boolean showBooleans = requirementSetting != RequirementSetting.NOT_VISIBLE;
        return new AnlassConfig(
            AnlassTyp.KLASSE_4,
            DateRange.ofYearContaining(LocalDate.now()),
            TestUtil.getRandomString("Default"),
            showBooleans,                   // showImpfausweis
            showBooleans,                   // showFragebogen
            showBooleans,                   // showAnmeldungBestaetigung
            showBooleans,                   // showImpfeinverständnis
            showBooleans,                   // showAnlassPrintImpfen
            showBooleans,                   // showDatenPersonendaten
            showBooleans,                   // showDatenPersonendatenEsadId
            requirementSetting,             // datenUntersuchung
            showBooleans,                   // showDatenSchule
            requirementSetting,             // datenBemerkung
            requirementSetting,             // vorabklaerungenFragebogenStatus
            requirementSetting,             // vorabklaerungenEinverstaendnisImpfungen
            requirementSetting,             // vorabklaerungenChronischeKrankheiten
            showBooleans,                   // showImpfungenHeaderImpfausweisUnvollstaendig
            showBooleans,                   // showImpfungenHeaderImpfausweisNichtVorhanden
            showBooleans,                   // showImpfungenHeaderImpfausweisZweiPlusEinsSchema
            showBooleans,                   // showImpfungenHeaderImpfausweisUpload
            requirementSetting,             // impfungenUebersicht
            requirementSetting,             // impfungenListe
            showBooleans,                   // showImpfungenDoppelkontrolle
            showBooleans,                   // impfungenMassnahmeKeineMassnahmen
            showBooleans,                   // impfungenMassnahmeGruppenFolgetermin
            showBooleans,                   // impfungenMassnahmeImpfenFolgetermin
            showBooleans,                   // impfungenMassnahmeEinzelFolgetermin
            showBooleans,                   // impfungenMassnahmeInfoKinderarzt
            showBooleans,                   // impfungenMassnahmeBeratungMPA
            showBooleans,                   // impfungenMassnahmeInfomaterialAbgegeben
            showBooleans,                   // impfungenMassnahmeInfoEltern
            showBooleans,                   // impfungenMassnahmeRueckfrageKinderarzt
            requirementSetting,             // impfungenMassnahmeBemerkungen
            requirementSetting,             // checkEinwilligungVorhanden
            requirementSetting,             // checkKontraindikationenAbgeklaert
            requirementSetting,             // checkDoppelkontrolleImpfentscheidDurchgefuehrt
            requirementSetting,             // untersuchungImpfungEmpfehlung
            EnumSetUtil.toString(EnumSet.allOf(ImpfungenMassnahmenEmpfehlung.class)), //
            // untersuchungImpfungEmpfehlungValues
            requirementSetting,             // untersuchungImpfungAchtung
            requirementSetting,             // untersuchungImpfungBemerkungen
            EnumSetUtil.toString(EnumSet.allOf(Krankheit.class)),   // untersuchungImpfungKrankheitenLinksValues
            EnumSetUtil.toString(EnumSet.noneOf(Krankheit.class)),   // untersuchungImpfungKrankheitenRechtsValues
            requirementSetting,             // impfungenDataMinimalImpfausweisGeprueft
            requirementSetting,             // impfungenDataMinimalImmunsuppression
            requirementSetting,             // impfungenDataMinimalMasernImpfstatus
            requirementSetting,             // impfungenDataMinimalMasernAnzahlImpfungen
            requirementSetting,             // impfungenDataMinimalHpvImpfstatus
            requirementSetting,             // impfungenDataMinimalHpvAnzahlImpfungen
            requirementSetting,             // impfungenDataMinimalImpfempfehlung
            requirementSetting,             // impfungenDataMinimalImpfungDurchgefuehrt
            requirementSetting,             // impfungenNurUnvollstaendige
            requirementSetting,             // sehenBrilleLinsen
            EnumSetUtil.toString(EnumSet.noneOf(BrilleLinsenResultat.class)),   // sehenBrilleLinsenValues
            BrilleLinsenResultat.OHNE_KEINE, // sehenBrilleLinsenDefaultValue
            requirementSetting,             // sehenFarbsehen
            requirementSetting,             // sehenTafel
            requirementSetting,             // sehenFernvisus
            BigDecimal.valueOf(1.0),        // sehenFernvisusDefaultValue
            requirementSetting,             // sehenFernvisusAugeEinzeln
            requirementSetting,             // sehenStereoTest
            requirementSetting,             // sehenStrabismus
            EnumSetUtil.toString(EnumSet.allOf(StrabismusResultat.class)),// sehenStrabismusValues
            requirementSetting,             // sehenStrabismusAuspraegung
            requirementSetting,             // sehenStrabismusAuftreten
            requirementSetting,             // sehenInspektion
            requirementSetting,             // sehenKonvergenz
            requirementSetting,             // sehenMotilitaet
            requirementSetting,             // sehenNahvisus
            requirementSetting,             // sehenEmpfehlung
            requirementSetting,             // sehenBemerkungen
            requirementSetting,             // sehenBemerkungenFragebogen
            requirementSetting,             // sehenMassnahmen
            requirementSetting,             // hoerenAudiogramm
            Dezibel.DB20,                   // hoerenAudiogrammDefaultValue
            requirementSetting,             // hoerenAudiogramm250Hz
            Dezibel.DB20,                   // hoerenAudiogramm250HzDefaultValue
            requirementSetting,             // hoerenAudiogramm8000Hz
            Dezibel.DB20,                   // hoerenAudiogramm8000HzDefaultValue
            requirementSetting,             // hoerenAudiogrammSimpel
            requirementSetting,             // hoerenAudiogrammBeurteilung
            requirementSetting,             // hoerenAudiogrammZusaetzlicheAngaben
            requirementSetting,             // hoerenAudiogrammEmpfehlung
            requirementSetting,             // hoerenAudiogrammBemerkungen
            requirementSetting,             // hoerenHoerhilfe
            requirementSetting,             // hoerenOtoskopie
            requirementSetting,             // hoerenOtoskopieBemerkungen
            requirementSetting,             // groesseGewichtGroesse
            requirementSetting,             // groesseGewichtGewicht
            requirementSetting,             // groesseGewichtBmi
            requirementSetting,             // groesseGewichtGroessePerzentileSingleValue
            requirementSetting,             // groesseGewichtGroessePerzentileMultiValue
            EnumSetUtil.toString(EnumSet.allOf(PerzentileGroesse.class)),// groesseGewichtGroessePerzentileValues
            requirementSetting,             // groesseGewichtGewichtPerzentileSingleValue
            requirementSetting,             // groesseGewichtGewichtPerzentileMultiValue
            EnumSetUtil.toString(EnumSet.allOf(PerzentileGewicht.class)),// groesseGewichtGewichtPerzentileValues
            requirementSetting,             // groesseGewichtBmiPerzentileSingleValue
            requirementSetting,             // groesseGewichtBmiPerzentileMultiValue
            EnumSetUtil.toString(EnumSet.allOf(PerzentileBmi.class)),// groesseGewichtBmiPerzentileValues
            requirementSetting,             // groesseGewichtEmpfehlung
            requirementSetting,             // groesseGewichtBemerkungen
            showBooleans,                   // showTriageTriage
            showBooleans,                   // showTriageKindWeiterleiten
            requirementSetting,             // laeusekontrolleBeurteilung
            requirementSetting,             // laeusekontrolleEmpfehlung
            requirementSetting,             // laeusekontrolleBemerkungen
            requirementSetting,             // untersuchungBewegungsapparat
            requirementSetting,             // untersuchungEinbeinstand
            requirementSetting,             // untersuchungEinbeinstandRechtsLinks
            EnumSetUtil.toString(EnumSet.allOf(Einbeinstand.class)),// untersuchungEinbeinstandValues
            requirementSetting,             // untersuchungEinbeinhuepfen
            requirementSetting,             // untersuchungEinbeinhuepfenRechtsLinks
            EnumSetUtil.toString(EnumSet.allOf(Einbeinhuepfen.class)),// untersuchungEinbeinhuepfenValues
            requirementSetting,             // untersuchungFingeropposition
            requirementSetting,             // untersuchungHampelmann
            requirementSetting,             // untersuchungBallFangen
            requirementSetting,             // untersuchungBallWerfen
            requirementSetting,             // untersuchungNameSchreiben
            requirementSetting,             // untersuchungFigurNachmalen
            requirementSetting,             // untersuchungStifthaltung
            requirementSetting,             // untersuchungAussprache
            requirementSetting,             // untersuchungLesefit
            requirementSetting,             // untersuchungLesefitPunkte
            requirementSetting,             // untersuchungZahlenNachsprechen4
            requirementSetting,             // untersuchungZahlenNachsprechen5
            requirementSetting,             // untersuchungZeichnenKreis
            requirementSetting,             // untersuchungZeichnenViereck
            requirementSetting,             // untersuchungZeichnenDreieck
            requirementSetting,             // untersuchungZeichnenMensch
            requirementSetting,             // untersuchungZeichnenMenschPunkte
            requirementSetting,             // untersuchungMotorik
            EnumSetUtil.toString(EnumSet.allOf(Motorik.class)), // untersuchungMotorikValues
            requirementSetting,             // untersuchungMotorikPunkte
            requirementSetting,             // untersuchungKognition
            requirementSetting,             // untersuchungKognitionPunkte
            requirementSetting,             // untersuchungSprachfaehigkeitSingleValue
            requirementSetting,             // untersuchungSprachfaehigkeitMultiValue
            EnumSetUtil.toString(EnumSet.allOf(Sprachfaehigkeit.class)), // untersuchungSprachfaehigkeitValues
            requirementSetting,             // untersuchungSprachePunkte
            requirementSetting,             // untersuchungMundZaehne
            requirementSetting,             // untersuchungHaut
            requirementSetting,             // untersuchungHerz
            requirementSetting,             // untersuchungBlutdruck
            requirementSetting,             // untersuchungVerhalten
            requirementSetting,             // unterlagenEinverstaendnis
            requirementSetting,             // unterlagenVerhaltensfragebogen
            requirementSetting,             // untersuchungEmpfehlung
            requirementSetting,             // untersuchungBemerkungen
            requirementSetting,             // untersuchungSpracheNachsprechen
            requirementSetting,             // untersuchungSpracheWortschatz
            requirementSetting,             // untersuchungSpracheSprachverstaendnis
            requirementSetting,             // untersuchungSpracheBemerkungen
            requirementSetting,             // bewegungsapparatSkoliose
            requirementSetting,             // bewegungsapparatHaltungsschwaeche
            requirementSetting,             // bewegungsapparatVerkuerzungHamstringmuskulatur
            requirementSetting,             // bewegungsapparatBemerkung
            requirementSetting,             // untersuchungBeeintraechtigungFreiesGehen
            requirementSetting,             // untersuchungBeeintraechtigungOhneStuetzradVelofahren
            requirementSetting,             // untersuchungBeeintraechtigungGrafomotorik
            requirementSetting,             // untersuchungBeeintraechtigungFeinmotorik
            requirementSetting,             // untersuchungBeeintraechtigungGrobmotorik
            requirementSetting,             // untersuchungBeeintraechtigungGleichgewicht
            requirementSetting,             // untersuchungBeeintraechtigungBemerkung
            requirementSetting,             // abschlussUeberweisungErfolgt
            requirementSetting,             // abschlussAerztlicheValidierungErforderlich
            requirementSetting,             // abschlussBeratungDurchgefuehrt
            requirementSetting,             // abschlussBeurteilungEmpfohleneMassnahmen
            showBooleans,                   // signatur
            showBooleans,                   // showMassnahmenPathologischeBefunde
            showBooleans,                   // showMassnahmenKeine
            showBooleans,                   // showMassnahmenBeratungMpa
            showBooleans,                   // showMassnahmenBeratungSchulAerztin
            showBooleans,                   // showMassnahmenBereitsBehandelt
            showBooleans,                   // showMassnahmenInfomaterial
            showBooleans,                   // showMassnahmenInfoEltern
            showBooleans,                   // showMassnahmenUeberweisungHausarzt
            showBooleans,                   // showMassnahmenUeberweisungAugenarzt
            showBooleans,                   // showMassnahmenNachkontrolleSaed
            showBooleans                    // showMassnahmenAnderes
        );
    }

    @NonNull
    @CanIgnoreReturnValue
    public AnlassConfigBuilder forAnlassConfig(@NonNull AnlassConfig existingAnlassConfig) {
        this.anlassConfig = existingAnlassConfig;
        return this;
    }

    @NonNull
    @CanIgnoreReturnValue
    public AnlassConfigBuilder withID(@NonNull ID<AnlassConfig> id) {
        this.anlassConfig.setId(id);
        return this;
    }

    @NonNull
    @CanIgnoreReturnValue
    public AnlassConfigBuilder withAnlassTyp(@NonNull AnlassTyp anlassTyp) {
        this.anlassConfig.setAnlassTyp(anlassTyp);
        return this;
    }

    @NonNull
    @CanIgnoreReturnValue
    public AnlassConfigBuilder withKrankheitenLinks(@NonNull EnumSet<Krankheit> krankheiten) {
        this.anlassConfig.setUntersuchungImpfungKrankheitenLinksValues(krankheiten);
        return this;
    }

    @NonNull
    @CanIgnoreReturnValue
    public AnlassConfigBuilder withKrankheitenRechts(@NonNull EnumSet<Krankheit> krankheiten) {
        this.anlassConfig.setUntersuchungImpfungKrankheitenRechtsValues(krankheiten);
        return this;
    }

    @NonNull
    @CanIgnoreReturnValue
    public AnlassConfigBuilder withGueltigkeitsZeitraum(@NonNull LocalDate datumVon, @NonNull LocalDate datumBis) {
        this.anlassConfig.setGueltigkeitsZeitraum(DateRange.of(datumVon, datumBis));
        return this;
    }

    @NonNull
    @CanIgnoreReturnValue
    public AnlassConfigBuilder withImpfungenUebersicht(@NonNull RequirementSetting setting) {
        this.anlassConfig.setImpfungenUebersicht(setting);
        return this;
    }

    @NonNull
    @CanIgnoreReturnValue
    public AnlassConfigBuilder withSehenTafel(@NonNull RequirementSetting setting) {
        this.anlassConfig.setSehenTafel(setting);
        return this;
    }

    @NonNull
    @CanIgnoreReturnValue
    public AnlassConfigBuilder withGroesseGewichtBmiPerzentileValues(
        @Nullable EnumSet<PerzentileBmi> values
    ) {
        this.anlassConfig.setGroesseGewichtBmiPerzentileValues(values);
        return this;
    }

    @NonNull
    @CanIgnoreReturnValue
    public AnlassConfigBuilder withGroesseGewichtGroessePerzentileValues(
        @Nullable EnumSet<PerzentileGroesse> values
    ) {
        this.anlassConfig.setGroesseGewichtGroessePerzentileValues(values);
        return this;
    }

    @NonNull
    @CanIgnoreReturnValue
    public AnlassConfigBuilder withGroesseGewichtGewichtPerzentileValues(
        @Nullable EnumSet<PerzentileGewicht> values
    ) {
        this.anlassConfig.setGroesseGewichtGewichtPerzentileValues(values);
        return this;
    }

    @NonNull
    @CanIgnoreReturnValue
    public AnlassConfigBuilder withImpfungenMassnahmenEmpfehlungValues(
        @NonNull EnumSet<ImpfungenMassnahmenEmpfehlung> values
    ) {
        this.anlassConfig.setUntersuchungImpfungEmpfehlungValues(values);
        return this;
    }

    @NonNull
    @CanIgnoreReturnValue
    public AnlassConfigBuilder withShowFragebogen(boolean showFragebogen) {
        this.anlassConfig.setShowFragebogen(showFragebogen);
        return this;
    }

    @NonNull
    @CanIgnoreReturnValue
    public AnlassConfigBuilder withShowImpfausweis(boolean showImpfausweis) {
        this.anlassConfig.setShowImpfausweis(showImpfausweis);
        return this;
    }

    @NonNull
    @CanIgnoreReturnValue
    public AnlassConfigBuilder withShowImpfeinverstaendnis(boolean showImpfeinverstaendnis) {
        this.anlassConfig.setShowImpfeinverstaendnis(showImpfeinverstaendnis);
        return this;
    }

    @NonNull
    @CanIgnoreReturnValue
    public AnlassConfigBuilder withShowAnlassPrintImpfen(boolean showAnlassPrintImpfen) {
        this.anlassConfig.setShowAnlassPrintImpfen(showAnlassPrintImpfen);
        return this;
    }

    @NonNull
    @CanIgnoreReturnValue
    public AnlassConfigBuilder withShowAnmeldungBestaetigung(boolean showAnmeldungBestaetigung) {
        this.anlassConfig.setShowAnmeldungBestaetigung(showAnmeldungBestaetigung);
        return this;
    }

    @NonNull
    public static AnlassConfig create(@NonNull Consumer<AnlassConfigBuilder> block) {
        AnlassConfigBuilder builder = new AnlassConfigBuilder();
        block.accept(builder);
        return builder.anlassConfig;
    }

    @NonNull
    public static AnlassConfigJson createJax(@NonNull Consumer<AnlassConfigBuilder> block) {
        AnlassConfig anlassConfig = create(block);
        final FixedClockProvider clockProvider = FixedClockProvider.now();
        final AppClock appClock = new AppClock(clockProvider);
        LocalDate date = appClock.getToday();
        return AnlassConfigMapper.toJson(anlassConfig, date);
    }

    @NonNull
    public static AnlassConfigJsonSpec createJaxSpec(@NonNull Consumer<AnlassConfigBuilder> block) {
        AnlassConfigJson json = createJax(block);
        return AnlassConfigJsonSpecMapper.INSTANCE.anlassConfigJsonToAnlassConfigJsonSpec(json);
    }
}

