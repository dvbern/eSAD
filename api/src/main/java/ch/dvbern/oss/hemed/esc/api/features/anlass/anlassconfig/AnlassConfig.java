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

import java.io.Serial;
import java.util.EnumSet;

import ch.dvbern.dvbstarter.infrastructure.db.DbConst;
import ch.dvbern.oss.hemed.esc.api.features.anlass.groessegewicht.types.PerzentileBmi;
import ch.dvbern.oss.hemed.esc.api.features.anlass.types.AnlassTyp;
import ch.dvbern.oss.hemed.esc.api.features.anlass.anlassconfig.types.RequirementSetting;
import ch.dvbern.oss.hemed.esc.api.shared.abstractentity.AbstractTenantEntity;
import ch.dvbern.oss.hemed.esc.api.shared.types.DateRange;
import ch.dvbern.oss.hemed.esc.api.shared.types.IgnoreForIdClassTest;
import ch.dvbern.oss.hemed.esc.api.shared.util.EnumSetUtil;
import jakarta.persistence.Column;
import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.hibernate.envers.Audited;

@Entity
@Table(
    uniqueConstraints = {
        @UniqueConstraint(name = "uc_anlassconfig_typ_bezeichnung", columnNames = {"anlasstyp", "bezeichnung"})
    }
)
@Audited
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class AnlassConfig extends AbstractTenantEntity<AnlassConfig> {

    @Serial
    private static final long serialVersionUID = 5285023727443911252L;

    @NotNull
    @NonNull
    @Column(nullable = false, length = DbConst.DB_ENUM_LENGTH)
    @Enumerated(EnumType.STRING)
    private AnlassTyp anlassTyp;

    @NotNull
    @NonNull
    @Valid
    @Embedded
    @Column(nullable = false)
    private DateRange gueltigkeitsZeitraum;

    @NotNull
    @NonNull
    @Column(nullable = false, length = DbConst.DB_DEFAULT_MAX_LENGTH)
    private String bezeichnung;

    // Einladung Dateien

    @NotNull
    @Column(nullable = false)
    private boolean showImpfausweis;

    @NotNull
    @Column(nullable = false)
    private boolean showFragebogen;

    @NotNull
    @Column(nullable = false)
    private boolean showAnmeldungBestaetigung;

    // Daten

    @NotNull
    @Column(nullable = false)
    private boolean showDatenPersonendaten;

    @NotNull
    @Column(nullable = false)
    @IgnoreForIdClassTest
    private boolean showDatenPersonendatenEsadId;

    @NotNull
    @NonNull
    @Column(nullable = false, length = DbConst.DB_ENUM_LENGTH)
    @Enumerated(EnumType.STRING)
    private RequirementSetting datenUntersuchung;

    @NotNull
    @Column(nullable = false)
    private boolean showDatenSchule;

    @NotNull
    @NonNull
    @Column(nullable = false, length = DbConst.DB_ENUM_LENGTH)
    @Enumerated(EnumType.STRING)
    private RequirementSetting datenBemerkung;

    // Impfungen

    @NotNull
    @NonNull
    @Column(nullable = false, length = DbConst.DB_ENUM_LENGTH)
    @Enumerated(EnumType.STRING)
    private RequirementSetting impfungenUebersicht;

    @NotNull
    @NonNull
    @Column(nullable = false, length = DbConst.DB_ENUM_LENGTH)
    @Enumerated(EnumType.STRING)
    private RequirementSetting impfungenListe;

    @NotNull
    @Column(nullable = false)
    private boolean showImpfungenDoppelkontrolle;

    @NotNull
    @Column(nullable = false)
    private boolean impfungenMassnahmeKeineMassnahmen;

    @NotNull
    @Column(nullable = false)
    private boolean impfungenMassnahmeGruppenFolgetermin;

    @NotNull
    @Column(nullable = false)
    private boolean impfungenMassnahmeImpfenFolgetermin;

    @NotNull
    @Column(nullable = false)
    private boolean impfungenMassnahmeEinzelFolgetermin;

    @NotNull
    @Column(nullable = false)
    private boolean impfungenMassnahmeInfoKinderarzt;

    @NotNull
    @Column(nullable = false)
    private boolean impfungenMassnahmeBeratungMPA;

    @NotNull
    @Column(nullable = false)
    private boolean impfungenMassnahmeInfomaterialAbgegeben;

    @NotNull
    @Column(nullable = false)
    private boolean impfungenMassnahmeInfoEltern;

    @NotNull
    @Column(nullable = false)
    private boolean impfungenMassnahmeRueckfrageKinderarzt;

    @NotNull
    @NonNull
    @Column(nullable = false, length = DbConst.DB_ENUM_LENGTH)
    @Enumerated(EnumType.STRING)
    private RequirementSetting checkEinwilligungVorhanden;

    @NotNull
    @NonNull
    @Column(nullable = false, length = DbConst.DB_ENUM_LENGTH)
    @Enumerated(EnumType.STRING)
    private RequirementSetting checkKontraindikationenAbgeklaert;

    @NotNull
    @NonNull
    @Column(nullable = false, length = DbConst.DB_ENUM_LENGTH)
    @Enumerated(EnumType.STRING)
    private RequirementSetting checkDoppelkontrolleImpfentscheidDurchgefuehrt;

    @NotNull
    @NonNull
    @Column(nullable = false, length = DbConst.DB_ENUM_LENGTH)
    @Enumerated(EnumType.STRING)
    private RequirementSetting untersuchungImpfungEmpfehlung;

    @NotNull
    @NonNull
    @Column(nullable = false, length = DbConst.DB_ENUM_LENGTH)
    @Enumerated(EnumType.STRING)
    private RequirementSetting untersuchungImpfungAchtung;

    @NotNull
    @NonNull
    @Column(nullable = false, length = DbConst.DB_ENUM_LENGTH)
    @Enumerated(EnumType.STRING)
    private RequirementSetting untersuchungImpfungBemerkungen;

    // Sehen

    @NotNull
    @NonNull
    @Column(nullable = false, length = DbConst.DB_ENUM_LENGTH)
    @Enumerated(EnumType.STRING)
    private RequirementSetting sehenBrilleLinsen;

    @NotNull
    @NonNull
    @Column(nullable = false, length = DbConst.DB_ENUM_LENGTH)
    @Enumerated(EnumType.STRING)
    private RequirementSetting sehenFarbsehen;

    @NotNull
    @NonNull
    @Column(nullable = false, length = DbConst.DB_ENUM_LENGTH)
    @Enumerated(EnumType.STRING)
    private RequirementSetting sehenTafel;

    @NotNull
    @NonNull
    @Column(nullable = false, length = DbConst.DB_ENUM_LENGTH)
    @Enumerated(EnumType.STRING)
    private RequirementSetting sehenFernvisus;

    @NotNull
    @NonNull
    @Column(nullable = false, length = DbConst.DB_ENUM_LENGTH)
    @Enumerated(EnumType.STRING)
    private RequirementSetting sehenStereoTest;

    @NotNull
    @NonNull
    @Column(nullable = false, length = DbConst.DB_ENUM_LENGTH)
    @Enumerated(EnumType.STRING)
    private RequirementSetting sehenStrabismus;

    @NotNull
    @NonNull
    @Column(nullable = false, length = DbConst.DB_ENUM_LENGTH)
    @Enumerated(EnumType.STRING)
    private RequirementSetting sehenInspektion;

    @NotNull
    @NonNull
    @Column(nullable = false, length = DbConst.DB_ENUM_LENGTH)
    @Enumerated(EnumType.STRING)
    private RequirementSetting sehenKonvergenz;

    @NotNull
    @NonNull
    @Column(nullable = false, length = DbConst.DB_ENUM_LENGTH)
    @Enumerated(EnumType.STRING)
    private RequirementSetting sehenMotilitaet;

    @NotNull
    @NonNull
    @Column(nullable = false, length = DbConst.DB_ENUM_LENGTH)
    @Enumerated(EnumType.STRING)
    private RequirementSetting sehenNahvisus;

    @NotNull
    @NonNull
    @Column(nullable = false, length = DbConst.DB_ENUM_LENGTH)
    @Enumerated(EnumType.STRING)
    private RequirementSetting sehenEmpfehlung;

    @NotNull
    @NonNull
    @Column(nullable = false, length = DbConst.DB_ENUM_LENGTH)
    @Enumerated(EnumType.STRING)
    private RequirementSetting sehenBemerkungen;

    @NotNull
    @NonNull
    @Column(nullable = false, length = DbConst.DB_ENUM_LENGTH)
    @Enumerated(EnumType.STRING)
    private RequirementSetting sehenBemerkungenFragebogen;

    @NotNull
    @NonNull
    @Column(nullable = false, length = DbConst.DB_ENUM_LENGTH)
    @Enumerated(EnumType.STRING)
    private RequirementSetting sehenMassnahmen;

    // Hoeren - Audiogramm

    @NotNull
    @NonNull
    @Column(nullable = false, length = DbConst.DB_ENUM_LENGTH)
    @Enumerated(EnumType.STRING)
    private RequirementSetting hoerenAudiogramm;

    // Required will only be checked in Validator if hoerenAudiogramm is required
    @NotNull
    @NonNull
    @Column(nullable = false, length = DbConst.DB_ENUM_LENGTH)
    @Enumerated(EnumType.STRING)
    private RequirementSetting hoerenAudiogramm250Hz;

    // Required will only be checked in Validator if hoerenAudiogramm is required
    @NotNull
    @NonNull
    @Column(nullable = false, length = DbConst.DB_ENUM_LENGTH)
    @Enumerated(EnumType.STRING)
    private RequirementSetting hoerenAudiogramm8000Hz;

    @NotNull
    @NonNull
    @Column(nullable = false, length = DbConst.DB_ENUM_LENGTH)
    @Enumerated(EnumType.STRING)
    private RequirementSetting hoerenAudiogrammBeurteilung;

    @NotNull
    @NonNull
    @Column(nullable = false, length = DbConst.DB_ENUM_LENGTH)
    @Enumerated(EnumType.STRING)
    private RequirementSetting hoerenAudiogrammZusaetzlicheAngaben;

    @NotNull
    @NonNull
    @Column(nullable = false, length = DbConst.DB_ENUM_LENGTH)
    @Enumerated(EnumType.STRING)
    private RequirementSetting hoerenAudiogrammEmpfehlung;

    @NotNull
    @NonNull
    @Column(nullable = false, length = DbConst.DB_ENUM_LENGTH)
    @Enumerated(EnumType.STRING)
    private RequirementSetting hoerenAudiogrammBemerkungen;

    // Hoeren - Otoskopie

    @NotNull
    @NonNull
    @Column(nullable = false, length = DbConst.DB_ENUM_LENGTH)
    @Enumerated(EnumType.STRING)
    private RequirementSetting hoerenOtoskopie;

    @NotNull
    @NonNull
    @Column(nullable = false, length = DbConst.DB_ENUM_LENGTH)
    @Enumerated(EnumType.STRING)
    private RequirementSetting hoerenOtoskopieBemerkungen;

    // Groesse und Gewicht

    @NotNull
    @NonNull
    @Column(nullable = false, length = DbConst.DB_ENUM_LENGTH)
    @Enumerated(EnumType.STRING)
    private RequirementSetting groesseGewichtGroesse;

    @NotNull
    @NonNull
    @Column(nullable = false, length = DbConst.DB_ENUM_LENGTH)
    @Enumerated(EnumType.STRING)
    private RequirementSetting groesseGewichtGewicht;

    @NotNull
    @NonNull
    @Column(nullable = false, length = DbConst.DB_ENUM_LENGTH)
    @Enumerated(EnumType.STRING)
    private RequirementSetting groesseGewichtBmi;

    @NotNull
    @NonNull
    @Column(nullable = false, length = DbConst.DB_ENUM_LENGTH)
    @Enumerated(EnumType.STRING)
    private RequirementSetting groesseGewichtGroessePerzentile;

    @NotNull
    @NonNull
    @Column(nullable = false, length = DbConst.DB_ENUM_LENGTH)
    @Enumerated(EnumType.STRING)
    private RequirementSetting groesseGewichtGewichtPerzentile;

    @NotNull
    @NonNull
    @Column(nullable = false, length = DbConst.DB_ENUM_LENGTH)
    @Enumerated(EnumType.STRING)
    private RequirementSetting groesseGewichtBmiPerzentile;

    @Nullable
    @Size(max = DbConst.DB_ENUM_LIST_LENGTH)
    @Column(nullable = true, length = DbConst.DB_ENUM_LIST_LENGTH)
    @Getter(AccessLevel.NONE)
    @Setter(AccessLevel.NONE)
    private String groesseGewichtBmiPerzentileValues;

    @NotNull
    @NonNull
    @Column(nullable = false, length = DbConst.DB_ENUM_LENGTH)
    @Enumerated(EnumType.STRING)
    private RequirementSetting groesseGewichtEmpfehlung;

    @NotNull
    @NonNull
    @Column(nullable = false, length = DbConst.DB_ENUM_LENGTH)
    @Enumerated(EnumType.STRING)
    private RequirementSetting groesseGewichtBemerkungen;

    // Triage

    @NotNull
    @Column(nullable = false)
    private boolean showTriageTriage;

    @NotNull
    @Column(nullable = false)
    private boolean showTriageKindWeiterleiten;

    // Läusekontrolle

    @NotNull
    @NonNull
    @Column(nullable = false, length = DbConst.DB_ENUM_LENGTH)
    @Enumerated(EnumType.STRING)
    private RequirementSetting laeusekontrolleBeurteilung;

    @NotNull
    @NonNull
    @Column(nullable = false, length = DbConst.DB_ENUM_LENGTH)
    @Enumerated(EnumType.STRING)
    private RequirementSetting laeusekontrolleEmpfehlung;

    @NotNull
    @NonNull
    @Column(nullable = false, length = DbConst.DB_ENUM_LENGTH)
    @Enumerated(EnumType.STRING)
    private RequirementSetting laeusekontrolleBemerkungen;

    // Aerztliche Untersuchung

    @NotNull
    @NonNull
    @Column(nullable = false, length = DbConst.DB_ENUM_LENGTH)
    @Enumerated(EnumType.STRING)
    private RequirementSetting untersuchungBewegungsapparat;

    @NotNull
    @NonNull
    @Column(nullable = false, length = DbConst.DB_ENUM_LENGTH)
    @Enumerated(EnumType.STRING)
    private RequirementSetting untersuchungEinbeinstand;

    @NotNull
    @NonNull
    @Column(nullable = false, length = DbConst.DB_ENUM_LENGTH)
    @Enumerated(EnumType.STRING)
    private RequirementSetting untersuchungEinbeinstandRechts;

    @NotNull
    @NonNull
    @Column(nullable = false, length = DbConst.DB_ENUM_LENGTH)
    @Enumerated(EnumType.STRING)
    private RequirementSetting untersuchungEinbeinstandLinks;

    @NotNull
    @NonNull
    @Column(nullable = false, length = DbConst.DB_ENUM_LENGTH)
    @Enumerated(EnumType.STRING)
    private RequirementSetting untersuchungEinbeinhuepfen;

    @NotNull
    @NonNull
    @Column(nullable = false, length = DbConst.DB_ENUM_LENGTH)
    @Enumerated(EnumType.STRING)
    private RequirementSetting untersuchungEinbeinhuepfenRechts;

    @NotNull
    @NonNull
    @Column(nullable = false, length = DbConst.DB_ENUM_LENGTH)
    @Enumerated(EnumType.STRING)
    private RequirementSetting untersuchungEinbeinhuepfenLinks;

    @NotNull
    @NonNull
    @Column(nullable = false, length = DbConst.DB_ENUM_LENGTH)
    @Enumerated(EnumType.STRING)
    private RequirementSetting untersuchungFingeropposition;

    @NotNull
    @NonNull
    @Column(nullable = false, length = DbConst.DB_ENUM_LENGTH)
    @Enumerated(EnumType.STRING)
    private RequirementSetting untersuchungHampelmann;

    @NotNull
    @NonNull
    @Column(nullable = false, length = DbConst.DB_ENUM_LENGTH)
    @Enumerated(EnumType.STRING)
    private RequirementSetting untersuchungBallFangen;

    @NotNull
    @NonNull
    @Column(nullable = false, length = DbConst.DB_ENUM_LENGTH)
    @Enumerated(EnumType.STRING)
    private RequirementSetting untersuchungBallWerfen;

    @NotNull
    @NonNull
    @Column(nullable = false, length = DbConst.DB_ENUM_LENGTH)
    @Enumerated(EnumType.STRING)
    private RequirementSetting untersuchungNameSchreiben;

    @NotNull
    @NonNull
    @Column(nullable = false, length = DbConst.DB_ENUM_LENGTH)
    @Enumerated(EnumType.STRING)
    private RequirementSetting untersuchungFigurNachmalen;

    @NotNull
    @NonNull
    @Column(nullable = false, length = DbConst.DB_ENUM_LENGTH)
    @Enumerated(EnumType.STRING)
    private RequirementSetting untersuchungStifthaltung;

    @NotNull
    @NonNull
    @Column(nullable = false, length = DbConst.DB_ENUM_LENGTH)
    @Enumerated(EnumType.STRING)
    private RequirementSetting untersuchungAussprache;

    @NotNull
    @NonNull
    @Column(nullable = false, length = DbConst.DB_ENUM_LENGTH)
    @Enumerated(EnumType.STRING)
    private RequirementSetting untersuchungLesefit;

    @NotNull
    @NonNull
    @Column(nullable = false, length = DbConst.DB_ENUM_LENGTH)
    @Enumerated(EnumType.STRING)
    private RequirementSetting untersuchungLesefitPunkte;

    @NotNull
    @NonNull
    @Column(nullable = false, length = DbConst.DB_ENUM_LENGTH)
    @Enumerated(EnumType.STRING)
    private RequirementSetting untersuchungZahlenNachsprechen4;

    @NotNull
    @NonNull
    @Column(nullable = false, length = DbConst.DB_ENUM_LENGTH)
    @Enumerated(EnumType.STRING)
    private RequirementSetting untersuchungZahlenNachsprechen5;

    @NotNull
    @NonNull
    @Column(nullable = false, length = DbConst.DB_ENUM_LENGTH)
    @Enumerated(EnumType.STRING)
    private RequirementSetting untersuchungZeichnenKreis;

    @NotNull
    @NonNull
    @Column(nullable = false, length = DbConst.DB_ENUM_LENGTH)
    @Enumerated(EnumType.STRING)
    private RequirementSetting untersuchungZeichnenViereck;

    @NotNull
    @NonNull
    @Column(nullable = false, length = DbConst.DB_ENUM_LENGTH)
    @Enumerated(EnumType.STRING)
    private RequirementSetting untersuchungZeichnenDreieck;

    @NotNull
    @NonNull
    @Column(nullable = false, length = DbConst.DB_ENUM_LENGTH)
    @Enumerated(EnumType.STRING)
    private RequirementSetting untersuchungZeichnenMensch;

    @NotNull
    @NonNull
    @Column(nullable = false, length = DbConst.DB_ENUM_LENGTH)
    @Enumerated(EnumType.STRING)
    private RequirementSetting untersuchungZeichnenMenschPunkte;

    @NotNull
    @NonNull
    @Column(nullable = false, length = DbConst.DB_ENUM_LENGTH)
    @Enumerated(EnumType.STRING)
    private RequirementSetting untersuchungMotorik;

    @NotNull
    @NonNull
    @Column(nullable = false, length = DbConst.DB_ENUM_LENGTH)
    @Enumerated(EnumType.STRING)
    private RequirementSetting untersuchungMotorikPunkte;

    @NotNull
    @NonNull
    @Column(nullable = false, length = DbConst.DB_ENUM_LENGTH)
    @Enumerated(EnumType.STRING)
    private RequirementSetting untersuchungKognition;

    @NotNull
    @NonNull
    @Column(nullable = false, length = DbConst.DB_ENUM_LENGTH)
    @Enumerated(EnumType.STRING)
    private RequirementSetting untersuchungKognitionPunkte;

    @NotNull
    @NonNull
    @Column(nullable = false, length = DbConst.DB_ENUM_LENGTH)
    @Enumerated(EnumType.STRING)
    private RequirementSetting untersuchungSprache;

    @NotNull
    @NonNull
    @Column(nullable = false, length = DbConst.DB_ENUM_LENGTH)
    @Enumerated(EnumType.STRING)
    private RequirementSetting untersuchungSprachePunkte;

    @NotNull
    @NonNull
    @Column(nullable = false, length = DbConst.DB_ENUM_LENGTH)
    @Enumerated(EnumType.STRING)
    private RequirementSetting untersuchungMundZaehne;

    @NotNull
    @NonNull
    @Column(nullable = false, length = DbConst.DB_ENUM_LENGTH)
    @Enumerated(EnumType.STRING)
    private RequirementSetting untersuchungHaut;

    @NotNull
    @NonNull
    @Column(nullable = false, length = DbConst.DB_ENUM_LENGTH)
    @Enumerated(EnumType.STRING)
    private RequirementSetting untersuchungHerz;

    @NotNull
    @NonNull
    @Column(nullable = false, length = DbConst.DB_ENUM_LENGTH)
    @Enumerated(EnumType.STRING)
    private RequirementSetting untersuchungBlutdruck;

    @NotNull
    @NonNull
    @Column(nullable = false, length = DbConst.DB_ENUM_LENGTH)
    @Enumerated(EnumType.STRING)
    private RequirementSetting untersuchungVerhalten;

    @NotNull
    @NonNull
    @Column(nullable = false, length = DbConst.DB_ENUM_LENGTH)
    @Enumerated(EnumType.STRING)
    private RequirementSetting unterlagenEinverstaendnis;

    @NotNull
    @NonNull
    @Column(nullable = false, length = DbConst.DB_ENUM_LENGTH)
    @Enumerated(EnumType.STRING)
    private RequirementSetting unterlagenVerhaltensfragebogen;

    @NotNull
    @NonNull
    @Column(nullable = false, length = DbConst.DB_ENUM_LENGTH)
    @Enumerated(EnumType.STRING)
    private RequirementSetting untersuchungEmpfehlung;

    @NotNull
    @NonNull
    @Column(nullable = false, length = DbConst.DB_ENUM_LENGTH)
    @Enumerated(EnumType.STRING)
    private RequirementSetting untersuchungBemerkungen;

    // Massnahmen

    @NotNull
    @Column(nullable = false)
    private boolean showMassnahmenPathologischeBefunde;

    @NotNull
    @Column(nullable = false)
    private boolean showMassnahmenKeine;

    @NotNull
    @Column(nullable = false)
    private boolean showMassnahmenBeratungMpa;

    @NotNull
    @Column(nullable = false)
    private boolean showMassnahmenBeratungSchulAerztin;

    @NotNull
    @Column(nullable = false)
    private boolean showMassnahmenBereitsBehandelt;

    @NotNull
    @Column(nullable = false)
    private boolean showMassnahmenInfomaterial;

    @NotNull
    @Column(nullable = false)
    private boolean showMassnahmenInfoEltern;

    @NotNull
    @Column(nullable = false)
    private boolean showMassnahmenUeberweisungHausarzt;

    @NotNull
    @Column(nullable = false)
    private boolean showMassnahmenUeberweisungAugenarzt;

    @NotNull
    @Column(nullable = false)
    private boolean showMassnahmenNachkontrolleSaed;

    @NotNull
    @Column(nullable = false)
    private boolean showMassnahmenAnderes;


    // groesseGewichtBmiPerzentileValues
    public void setGroesseGewichtBmiPerzentileValues(@Nullable EnumSet<PerzentileBmi> groesseGewichtBmiPerzentileValues) {
        this.groesseGewichtBmiPerzentileValues = EnumSetUtil.toString(groesseGewichtBmiPerzentileValues);
    }

    @NonNull
    public EnumSet<PerzentileBmi> getGroesseGewichtBmiPerzentileValues() {
        return EnumSetUtil.fromString(PerzentileBmi.class, groesseGewichtBmiPerzentileValues);
    }
}
