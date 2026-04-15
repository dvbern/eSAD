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

package ch.dvbern.oss.hemed.esc.api.features.anlass.anlassconfig;

import java.util.Arrays;

import ch.dvbern.oss.hemed.esc.api.features.anlass.anlassconfig.types.RequirementSetting;
import lombok.experimental.UtilityClass;
import org.checkerframework.checker.nullness.qual.NonNull;

@UtilityClass
public final class AnlassConfigUtil {

    public static boolean showCardImpfungenHeader(@NonNull AnlassConfig config) {
        return isAnyVisible(
            config.isShowImpfungenHeaderImpfausweisNichtVorhanden(),
            config.isShowImpfungenHeaderImpfausweisUnvollstaendig(),
            config.isShowImpfungenHeaderImpfausweisZweiPlusEinsSchema(),
            config.isShowImpfungenHeaderImpfausweisUpload()
        );
    }

    public static boolean showCardImpfungDataMinimal(@NonNull AnlassConfig config) {
        return isAnyVisible(
            config.getImpfungenDataMinimalHpvImpfstatus(),
            config.getImpfungenDataMinimalImmunsuppression(),
            config.getImpfungenDataMinimalMasernImpfstatus(),
            config.getImpfungenDataMinimalMasernAnzahlImpfungen(),
            config.getImpfungenDataMinimalHpvImpfstatus(),
            config.getImpfungenDataMinimalHpvAnzahlImpfungen(),
            config.getImpfungenDataMinimalImpfempfehlung(),
            config.getImpfungenDataMinimalImpfungDurchgefuehrt()
        );
    }

    public static boolean showCardImpfungenChecklist(@NonNull AnlassConfig config) {
        return isAnyVisible(
            config.getCheckEinwilligungVorhanden(),
            config.getCheckKontraindikationenAbgeklaert(),
            config.getCheckDoppelkontrolleImpfentscheidDurchgefuehrt(),
            config.getUntersuchungImpfungBemerkungen()
        );
    }

    public static boolean isImpfungenMassnahmenVisible(@NonNull AnlassConfig config) {
        return isAnyVisible(
                config.getUntersuchungImpfungEmpfehlung(),
                config.getUntersuchungImpfungAchtung()
            )
            || config.isImpfungenMassnahmeKeineMassnahmen()
            || config.isImpfungenMassnahmeGruppenFolgetermin()
            || config.isImpfungenMassnahmeImpfenFolgetermin()
            || config.isImpfungenMassnahmeEinzelFolgetermin()
            || config.isImpfungenMassnahmeInfoKinderarzt()
            || config.isImpfungenMassnahmeBeratungMPA()
            || config.isImpfungenMassnahmeInfomaterialAbgegeben()
            || config.isImpfungenMassnahmeInfoEltern()
            || config.isImpfungenMassnahmeRueckfrageKinderarzt();
    }

    public static boolean showTabMassnahmen(@NonNull AnlassConfig config) {
        return isAnyVisible(
            isMassnahmenBeurteilungVisible(config),
            isMassnahmenPathologischeBefundeVisible(config)
        );
    }

    public static boolean isMassnahmenPathologischeBefundeVisible(@NonNull AnlassConfig config) {
        return isAnyVisible(
            config.isShowMassnahmenPathologischeBefunde()
        );
    }

    public static boolean isMassnahmenBeurteilungVisible(@NonNull AnlassConfig config) {
        return isAnyVisible(
            config.isShowMassnahmenKeine(),
            config.isShowMassnahmenBeratungMpa(),
            config.isShowMassnahmenBeratungSchulAerztin(),
            config.isShowMassnahmenBereitsBehandelt(),
            config.isShowMassnahmenInfomaterial(),
            config.isShowMassnahmenInfoEltern(),
            config.isShowMassnahmenUeberweisungHausarzt(),
            config.isShowMassnahmenUeberweisungAugenarzt(),
            config.isShowMassnahmenNachkontrolleSaed(),
            config.isShowMassnahmenAnderes()
        );
    }

    public static boolean showTabSprache(@NonNull AnlassConfig config) {
        return isAnyVisible(
            config.getUntersuchungSpracheNachsprechen(),
            config.getUntersuchungSpracheWortschatz(),
            config.getUntersuchungSpracheSprachverstaendnis(),
            config.getUntersuchungSpracheBemerkungen()
        );
    }

    public static boolean showTabBewegungsapparat(@NonNull AnlassConfig config) {
        return isAnyVisible(
            config.getBewegungsapparatSkoliose(),
            config.getBewegungsapparatHaltungsschwaeche(),
            config.getBewegungsapparatVerkuerzungHamstringmuskulatur(),
            config.getBewegungsapparatBemerkung()
        );
    }

    public static boolean showTabSchulrelevanteBeeintraechtigung(@NonNull AnlassConfig config) {
        return isAnyVisible(
            config.getUntersuchungBeeintraechtigungFreiesGehen(),
            config.getUntersuchungBeeintraechtigungOhneStuetzradVelofahren(),
            config.getUntersuchungBeeintraechtigungGrafomotorik(),
            config.getUntersuchungBeeintraechtigungFeinmotorik(),
            config.getUntersuchungBeeintraechtigungGrobmotorik(),
            config.getUntersuchungBeeintraechtigungGleichgewicht(),
            config.getUntersuchungBeeintraechtigungBemerkung()
        );
    }

    public static boolean showTabLaeusekontrolle(@NonNull AnlassConfig config) {
        return isAnyVisible(
            config.getLaeusekontrolleBeurteilung(),
            config.getLaeusekontrolleEmpfehlung(),
            config.getLaeusekontrolleBemerkungen()
        );
    }

    public static boolean showTabTriage(@NonNull AnlassConfig config) {
        return isAnyVisible(
            config.isShowTriageTriage(),
            config.isShowTriageKindWeiterleiten()
        );
    }

    public static boolean showTabHoeren(@NonNull AnlassConfig config) {
        return isAnyVisible(
            isHoerenAudiogrammVisible(config),
            isHoerenOtoskopieVisible(config)
        );
    }

    public static boolean isHoerenOtoskopieVisible(@NonNull AnlassConfig config) {
        return isAnyVisible(
            config.getHoerenOtoskopie(),
            config.getHoerenOtoskopieBemerkungen());
    }

    public static boolean isHoerenAudiogrammVisible(@NonNull AnlassConfig config) {
        return isAnyVisible(
            config.getHoerenAudiogramm(),
            config.getHoerenAudiogramm250Hz(),
            config.getHoerenAudiogramm8000Hz(),
            config.getHoerenAudiogrammSimpel(),
            config.getHoerenHoerhilfe(),
            config.getHoerenAudiogrammBeurteilung(),
            config.getHoerenAudiogrammZusaetzlicheAngaben(),
            config.getHoerenAudiogrammBemerkungen());
    }

    public static boolean showTabSehen(@NonNull AnlassConfig config) {
        return isAnyVisible(
            isSehenBeurteilungVisible(config),
            isSehenBemerkungenFragebogenVisible(config),
            isSehenMassnahmenVisible(config)
        );
    }

    public static boolean showCardAbschlussAbschluss(@NonNull AnlassConfig config) {
        return isAnyVisible(
            config.getAbschlussUeberweisungErfolgt(),
            config.getAbschlussAerztlicheValidierungErforderlich(),
            config.getAbschlussAerztlicheValidierungErforderlich(),
            config.getAbschlussBeurteilungEmpfohleneMassnahmen()
        );
    }

    public static boolean showTabAbschluss(@NonNull AnlassConfig config) {
        return isAnyVisible(
            AnlassConfigUtil.showCardAbschlussAbschluss(config),
            AnlassConfigUtil.isSignaturVisible(config)
        );
    }

    public static boolean isSignaturVisible(@NonNull AnlassConfig config) {
        return isAnyVisible(
            config.isShowCardAbschlussSignatur()
        );
    }

    public static boolean isSehenMassnahmenVisible(@NonNull AnlassConfig config) {
        return isAnyVisible(
            config.getSehenMassnahmen()
        );
    }

    public static boolean isSehenBemerkungenFragebogenVisible(@NonNull AnlassConfig config) {
        return isAnyVisible(
            config.getSehenBemerkungenFragebogen()
        );
    }

    public static boolean isSehenBeurteilungVisible(@NonNull AnlassConfig config) {
        return isAnyVisible(
            config.getSehenBrilleLinsen(),
            config.getSehenFarbsehen(),
            config.getSehenTafel(),
            config.getSehenFernvisus(),
            config.getSehenFernvisusAugeEinzeln(),
            config.getSehenStereoTest(),
            config.getSehenStrabismus(),
            config.getSehenStrabismusAuftreten(),
            config.getSehenStrabismusAuspraegung(),
            config.getSehenInspektion(),
            config.getSehenKonvergenz(),
            config.getSehenMotilitaet(),
            config.getSehenNahvisus(),
            config.getSehenBemerkungen());
    }

    public static boolean showTabDaten(@NonNull AnlassConfig config) {
        return config.isShowDatenPersonendaten()
            || config.isShowDatenPersonendatenEsadId()
            || isAnyVisible(config.getDatenUntersuchung())
            || config.isShowDatenSchule();
    }

    public static boolean showTabVorabklaerungen(@NonNull AnlassConfig config) {
        return isAnyVisible(
            config.getVorabklaerungenFragebogenStatus(),
            config.getVorabklaerungenEinverstaendnisImpfungen(),
            config.getVorabklaerungenChronischeKrankheiten());
    }

    public static boolean showTabGroesseGewicht(@NonNull AnlassConfig config) {
        return isAnyVisible(
            config.getGroesseGewichtGroesse(),
            config.getGroesseGewichtGewicht(),
            config.getGroesseGewichtBmi(),
            config.getGroesseGewichtGroessePerzentile(),
            config.getGroesseGewichtGewichtPerzentile(),
            config.getGroesseGewichtBmiPerzentile(),
            config.getGroesseGewichtEmpfehlung(),
            config.getGroesseGewichtBemerkungen());
    }

    public static boolean showTabArztUntersuchung(@NonNull AnlassConfig config) {
        return isAnyVisible(
            isArztUntersuchungBeurteilungVisible(config),
            isArztUntersuchungBemerkungVisible(config)
        );
    }

    public static boolean isArztUntersuchungBeurteilungVisible(@NonNull AnlassConfig config) {
        return isAnyVisible(
            config.getUntersuchungBewegungsapparat(),
            config.getUntersuchungEinbeinstand(),
            config.getUntersuchungEinbeinstandRechtsLinks(),
            config.getUntersuchungEinbeinhuepfen(),
            config.getUntersuchungEinbeinhuepfenRechtsLinks(),
            config.getUntersuchungFingeropposition(),
            config.getUntersuchungHampelmann(),
            config.getUntersuchungBallFangen(),
            config.getUntersuchungBallWerfen(),
            config.getUntersuchungNameSchreiben(),
            config.getUntersuchungFigurNachmalen(),
            config.getUntersuchungStifthaltung(),
            config.getUntersuchungAussprache(),
            config.getUntersuchungLesefit(),
            config.getUntersuchungLesefitPunkte(),
            config.getUntersuchungZahlenNachsprechen4(),
            config.getUntersuchungZahlenNachsprechen5(),
            config.getUntersuchungZeichnenKreis(),
            config.getUntersuchungZeichnenViereck(),
            config.getUntersuchungZeichnenDreieck(),
            config.getUntersuchungZeichnenMensch(),
            config.getUntersuchungZeichnenMenschPunkte(),
            config.getUntersuchungKognition(),
            config.getUntersuchungKognitionPunkte(),
            config.getUntersuchungMotorik(),
            config.getUntersuchungMotorikPunkte(),
            config.getUntersuchungSprachfaehigkeitSingleValue(),
            config.getUntersuchungSprachfaehigkeitMultiValue(),
            config.getUntersuchungSprachePunkte(),
            config.getUntersuchungMundZaehne(),
            config.getUntersuchungHaut(),
            config.getUntersuchungHerz(),
            config.getUntersuchungBlutdruck(),
            config.getUntersuchungVerhalten(),
            config.getUnterlagenEinverstaendnis(),
            config.getUnterlagenVerhaltensfragebogen());
    }

    public static boolean isArztUntersuchungBemerkungVisible(@NonNull AnlassConfig config) {
        return isAnyVisible(
            config.getUntersuchungBemerkungen());
    }

    public static boolean isAnyVisible(@NonNull RequirementSetting... settings) {
        return Arrays.stream(settings).anyMatch(RequirementSetting::isVisible);
    }

    public static boolean isAnyVisible(Boolean... settings) {
        return Arrays.stream(settings).anyMatch(Boolean::booleanValue);
    }
}
