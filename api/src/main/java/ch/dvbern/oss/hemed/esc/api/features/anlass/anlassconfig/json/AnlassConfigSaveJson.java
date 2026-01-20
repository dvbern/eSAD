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

package ch.dvbern.oss.hemed.esc.api.features.anlass.anlassconfig.json;

import java.time.LocalDate;
import java.util.List;

import ch.dvbern.dvbstarter.infrastructure.stereotypes.JaxDTO;
import ch.dvbern.oss.hemed.esc.api.features.anlass.aerztlicheuntersuchung.types.Einbeinhuepfen;
import ch.dvbern.oss.hemed.esc.api.features.anlass.aerztlicheuntersuchung.types.Einbeinstand;
import ch.dvbern.oss.hemed.esc.api.features.anlass.aerztlicheuntersuchung.types.Motorik;
import ch.dvbern.oss.hemed.esc.api.features.anlass.aerztlicheuntersuchung.types.Sprachfaehigkeit;
import ch.dvbern.oss.hemed.esc.api.features.anlass.groessegewicht.types.PerzentileBmi;
import ch.dvbern.oss.hemed.esc.api.features.anlass.types.AnlassTyp;
import ch.dvbern.oss.hemed.esc.api.features.anlass.anlassconfig.types.RequirementSetting;
import ch.dvbern.oss.hemed.esc.api.features.impfstoff.types.Krankheit;
import ch.dvbern.oss.hemed.esc.api.shared.types.IgnoreForIdClassTest;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Value;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.eclipse.microprofile.openapi.annotations.media.Schema;

@Value
@AllArgsConstructor
@JaxDTO
public class AnlassConfigSaveJson {

    @NotNull
    @NonNull
    @Schema(required = true)
    private AnlassTyp anlassTyp;

    @NotNull
    @NonNull
    @Schema(required = true)
    private LocalDate datumVon;

    @NotNull
    @NonNull
    @Schema(required = true)
    private LocalDate datumBis;

    @NotNull
    @NonNull
    @Schema(required = true)
    private String bezeichnung;

    // Einladung Dateien

    @NotNull
    @Schema(required = true)
    private boolean showImpfausweis;

    @NotNull
    @Schema(required = true)
    private boolean showFragebogen;

    @NotNull
    @Schema(required = true)
    private boolean showAnmeldungBestaetigung;

    // Daten

    @NotNull
    @Schema(required = true)
    private boolean showDatenPersonendaten;

    @NotNull
    @Schema(required = true)
    @IgnoreForIdClassTest
    private boolean showDatenPersonendatenEsadId;

    @NotNull
    @NonNull
    @Schema(required = true)
    private RequirementSetting datenUntersuchung;

    @NotNull
    @Schema(required = true)
    private boolean showDatenSchule;

    @NotNull
    @NonNull
    @Schema(required = true)
    private RequirementSetting datenBemerkung;

    // Impfungen

    @NotNull
    @NonNull
    @Schema(required = true)
    private RequirementSetting impfungenUebersicht;

    @NotNull
    @NonNull
    @Schema(required = true)
    private RequirementSetting impfungenListe;

    @NotNull
    @Schema(required = true)
    private boolean showImpfungenDoppelkontrolle;

    @NotNull
    @Schema(required = true)
    private boolean impfungenMassnahmeKeineMassnahmen;

    @NotNull
    @Schema(required = true)
    private boolean impfungenMassnahmeGruppenFolgetermin;

    @NotNull
    @Schema(required = true)
    private boolean impfungenMassnahmeImpfenFolgetermin;

    @NotNull
    @Schema(required = true)
    private boolean impfungenMassnahmeEinzelFolgetermin;

    @NotNull
    @Schema(required = true)
    private boolean impfungenMassnahmeInfoKinderarzt;

    @NotNull
    @Schema(required = true)
    private boolean impfungenMassnahmeBeratungMPA;

    @NotNull
    @Schema(required = true)
    private boolean impfungenMassnahmeInfomaterialAbgegeben;

    @NotNull
    @Schema(required = true)
    private boolean impfungenMassnahmeInfoEltern;

    @NotNull
    @Schema(required = true)
    private boolean impfungenMassnahmeRueckfrageKinderarzt;

    @NotNull
    @NonNull
    @Schema(required = true)
    private RequirementSetting checkEinwilligungVorhanden;

    @NotNull
    @NonNull
    @Schema(required = true)
    private RequirementSetting checkKontraindikationenAbgeklaert;

    @NotNull
    @NonNull
    @Schema(required = true)
    private RequirementSetting checkDoppelkontrolleImpfentscheidDurchgefuehrt;

    @NotNull
    @NonNull
    @Schema(required = true)
    private RequirementSetting untersuchungImpfungEmpfehlung;

    @NotNull
    @NonNull
    @Schema(required = true)
    private RequirementSetting untersuchungImpfungAchtung;

    @NotNull
    @NonNull
    @Schema(required = true)
    private RequirementSetting untersuchungImpfungBemerkungen;

    @NotNull
    @NonNull
    @Schema(required = true)
    private List<Krankheit> untersuchungImpfungKrankheitenLinksValues;

    @NotNull
    @NonNull
    @Schema(required = true)
    private List<Krankheit> untersuchungImpfungKrankheitenRechtsValues;

    // Sehen

    @NotNull
    @NonNull
    @Schema(required = true)
    private RequirementSetting sehenBrilleLinsen;

    @NotNull
    @NonNull
    @Schema(required = true)
    private RequirementSetting sehenFarbsehen;

    @NotNull
    @NonNull
    @Schema(required = true)
    private RequirementSetting sehenTafel;

    @NotNull
    @NonNull
    @Schema(required = true)
    private RequirementSetting sehenFernvisus;

    @NotNull
    @NonNull
    @Schema(required = true)
    private RequirementSetting sehenStereoTest;

    @NotNull
    @NonNull
    @Schema(required = true)
    private RequirementSetting sehenStrabismus;

    @NotNull
    @NonNull
    @Schema(required = true)
    private RequirementSetting sehenInspektion;

    @NotNull
    @NonNull
    @Schema(required = true)
    private RequirementSetting sehenKonvergenz;

    @NotNull
    @NonNull
    @Schema(required = true)
    private RequirementSetting sehenMotilitaet;

    @NotNull
    @NonNull
    @Schema(required = true)
    private RequirementSetting sehenNahvisus;

    @NotNull
    @NonNull
    @Schema(required = true)
    private RequirementSetting sehenEmpfehlung;

    @NotNull
    @NonNull
    @Schema(required = true)
    private RequirementSetting sehenBemerkungen;

    @NotNull
    @NonNull
    @Schema(required = true)
    private RequirementSetting sehenBemerkungenFragebogen;

    @NotNull
    @NonNull
    @Schema(required = true)
    private RequirementSetting sehenMassnahmen;

    // Hoeren - Audiogramm

    @NotNull
    @NonNull
    @Schema(required = true)
    private RequirementSetting hoerenAudiogramm;

    @NotNull
    @NonNull
    @Schema(required = true)
    private RequirementSetting hoerenAudiogramm250Hz;

    @NotNull
    @NonNull
    @Schema(required = true)
    private RequirementSetting hoerenAudiogramm8000Hz;

    @NotNull
    @NonNull
    @Schema(required = true)
    private RequirementSetting hoerenAudiogrammBeurteilung;

    @NotNull
    @NonNull
    @Schema(required = true)
    private RequirementSetting hoerenAudiogrammZusaetzlicheAngaben;

    @NotNull
    @NonNull
    @Schema(required = true)
    private RequirementSetting hoerenAudiogrammEmpfehlung;

    @NotNull
    @NonNull
    @Schema(required = true)
    private RequirementSetting hoerenAudiogrammBemerkungen;

    // Hoeren - Otoskopie

    @NotNull
    @NonNull
    @Schema(required = true)
    private RequirementSetting hoerenOtoskopie;

    @NotNull
    @NonNull
    @Schema(required = true)
    private RequirementSetting hoerenOtoskopieBemerkungen;

    // Groesse und Gewicht

    @NotNull
    @NonNull
    @Schema(required = true)
    private RequirementSetting groesseGewichtGroesse;

    @NotNull
    @NonNull
    @Schema(required = true)
    private RequirementSetting groesseGewichtGewicht;

    @NotNull
    @NonNull
    @Schema(required = true)
    private RequirementSetting groesseGewichtBmi;

    @NotNull
    @NonNull
    @Schema(required = true)
    private RequirementSetting groesseGewichtGroessePerzentile;

    @NotNull
    @NonNull
    @Schema(required = true)
    private RequirementSetting groesseGewichtGewichtPerzentile;

    @NotNull
    @NonNull
    @Schema(required = true)
    private RequirementSetting groesseGewichtBmiPerzentile;

    @NotNull
    @NonNull
    @Schema(required = true)
    private List<PerzentileBmi> groesseGewichtBmiPerzentileValues;

    @NotNull
    @NonNull
    @Schema(required = true)
    private RequirementSetting groesseGewichtEmpfehlung;

    @NotNull
    @NonNull
    @Schema(required = true)
    private RequirementSetting groesseGewichtBemerkungen;

    // Triage

    @NotNull
    @Schema(required = true)
    private boolean showTriageTriage;

    @NotNull
    @Schema(required = true)
    private boolean showTriageKindWeiterleiten;

    // Läusekontrolle

    @NotNull
    @NonNull
    @Schema(required = true)
    private RequirementSetting laeusekontrolleBeurteilung;

    @NotNull
    @NonNull
    @Schema(required = true)
    private RequirementSetting laeusekontrolleEmpfehlung;

    @NotNull
    @NonNull
    @Schema(required = true)
    private RequirementSetting laeusekontrolleBemerkungen;

    // Aerztliche Untersuchung

    @NotNull
    @NonNull
    @Schema(required = true)
    private RequirementSetting untersuchungBewegungsapparat;

    @NotNull
    @NonNull
    @Schema(required = true)
    private RequirementSetting untersuchungEinbeinstand;

    @NotNull
    @NonNull
    @Schema(required = true)
    private RequirementSetting untersuchungEinbeinstandRechtsLinks;

    @NotNull
    @NonNull
    @Schema(required = true)
    private List<Einbeinstand> untersuchungEinbeinstandValues;

    @NotNull
    @NonNull
    @Schema(required = true)
    private RequirementSetting untersuchungEinbeinhuepfen;

    @NotNull
    @NonNull
    @Schema(required = true)
    private RequirementSetting untersuchungEinbeinhuepfenRechtsLinks;

    @NotNull
    @NonNull
    @Schema(required = true)
    private List<Einbeinhuepfen> untersuchungEinbeinhuepfenValues;

    @NotNull
    @NonNull
    @Schema(required = true)
    private RequirementSetting untersuchungFingeropposition;

    @NotNull
    @NonNull
    @Schema(required = true)
    private RequirementSetting untersuchungHampelmann;

    @NotNull
    @NonNull
    @Schema(required = true)
    private RequirementSetting untersuchungBallFangen;

    @NotNull
    @NonNull
    @Schema(required = true)
    private RequirementSetting untersuchungBallWerfen;

    @NotNull
    @NonNull
    @Schema(required = true)
    private RequirementSetting untersuchungNameSchreiben;

    @NotNull
    @NonNull
    @Schema(required = true)
    private RequirementSetting untersuchungFigurNachmalen;

    @NotNull
    @NonNull
    @Schema(required = true)
    private RequirementSetting untersuchungStifthaltung;

    @NotNull
    @NonNull
    @Schema(required = true)
    private RequirementSetting untersuchungAussprache;

    @NotNull
    @NonNull
    @Schema(required = true)
    private RequirementSetting untersuchungLesefit;

    @NotNull
    @NonNull
    @Schema(required = true)
    private RequirementSetting untersuchungLesefitPunkte;

    @NotNull
    @NonNull
    @Schema(required = true)
    private RequirementSetting untersuchungZahlenNachsprechen4;

    @NotNull
    @NonNull
    @Schema(required = true)
    private RequirementSetting untersuchungZahlenNachsprechen5;

    @NotNull
    @NonNull
    @Schema(required = true)
    private RequirementSetting untersuchungZeichnenKreis;

    @NotNull
    @NonNull
    @Schema(required = true)
    private RequirementSetting untersuchungZeichnenViereck;

    @NotNull
    @NonNull
    @Schema(required = true)
    private RequirementSetting untersuchungZeichnenDreieck;

    @NotNull
    @NonNull
    @Schema(required = true)
    private RequirementSetting untersuchungZeichnenMensch;

    @NotNull
    @NonNull
    @Schema(required = true)
    private RequirementSetting untersuchungZeichnenMenschPunkte;

    @NotNull
    @NonNull
    @Schema(required = true)
    private RequirementSetting untersuchungMotorik;

    @NotNull
    @NonNull
    @Schema(required = true)
    private List<Motorik> untersuchungMotorikValues;

    @NotNull
    @NonNull
    @Schema(required = true)
    private RequirementSetting untersuchungMotorikPunkte;

    @NotNull
    @NonNull
    @Schema(required = true)
    private RequirementSetting untersuchungKognition;

    @NotNull
    @NonNull
    @Schema(required = true)
    private RequirementSetting untersuchungKognitionPunkte;

    @NotNull
    @NonNull
    @Schema(required = true)
    private RequirementSetting untersuchungSprachfaehigkeitSingleValue;

    @NotNull
    @NonNull
    @Schema(required = true)
    private RequirementSetting untersuchungSprachfaehigkeitMultiValue;

    @NotNull
    @NonNull
    @Schema(required = true)
    private List<Sprachfaehigkeit> untersuchungSprachfaehigkeitValues;

    @NotNull
    @NonNull
    @Schema(required = true)
    private RequirementSetting untersuchungSprachePunkte;

    @NotNull
    @NonNull
    @Schema(required = true)
    private RequirementSetting untersuchungMundZaehne;

    @NotNull
    @NonNull
    @Schema(required = true)
    private RequirementSetting untersuchungHaut;

    @NotNull
    @NonNull
    @Schema(required = true)
    private RequirementSetting untersuchungHerz;

    @NotNull
    @NonNull
    @Schema(required = true)
    private RequirementSetting untersuchungBlutdruck;

    @NotNull
    @NonNull
    @Schema(required = true)
    private RequirementSetting untersuchungVerhalten;

    @NotNull
    @NonNull
    @Schema(required = true)
    private RequirementSetting unterlagenEinverstaendnis;

    @NotNull
    @NonNull
    @Schema(required = true)
    private RequirementSetting unterlagenVerhaltensfragebogen;

    @NotNull
    @NonNull
    @Schema(required = true)
    private RequirementSetting untersuchungEmpfehlung;

    @NotNull
    @NonNull
    @Schema(required = true)
    private RequirementSetting untersuchungBemerkungen;

    // Massnahmen

    @NotNull
    @Schema(required = true)
    private boolean showMassnahmenPathologischeBefunde;

    @NotNull
    @Schema(required = true)
    private boolean showMassnahmenKeine;

    @NotNull
    @Schema(required = true)
    private boolean showMassnahmenBeratungMpa;

    @NotNull
    @Schema(required = true)
    private boolean showMassnahmenBeratungSchulAerztin;

    @NotNull
    @Schema(required = true)
    private boolean showMassnahmenBereitsBehandelt;

    @NotNull
    @Schema(required = true)
    private boolean showMassnahmenInfomaterial;

    @NotNull
    @Schema(required = true)
    private boolean showMassnahmenInfoEltern;

    @NotNull
    @Schema(required = true)
    private boolean showMassnahmenUeberweisungHausarzt;

    @NotNull
    @Schema(required = true)
    private boolean showMassnahmenUeberweisungAugenarzt;

    @NotNull
    @Schema(required = true)
    private boolean showMassnahmenNachkontrolleSaed;

    @NotNull
    @Schema(required = true)
    private boolean showMassnahmenAnderes;

}
