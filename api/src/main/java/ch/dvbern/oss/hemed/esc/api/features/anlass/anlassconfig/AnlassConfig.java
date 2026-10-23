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
import java.math.BigDecimal;
import java.util.EnumSet;
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

import ch.dvbern.dvbstarter.infrastructure.db.DbConst;
import ch.dvbern.oss.hemed.esc.api.features.anlass.aerztlicheuntersuchung.types.Einbeinhuepfen;
import ch.dvbern.oss.hemed.esc.api.features.anlass.aerztlicheuntersuchung.types.Einbeinstand;
import ch.dvbern.oss.hemed.esc.api.features.anlass.aerztlicheuntersuchung.types.Motorik;
import ch.dvbern.oss.hemed.esc.api.features.anlass.aerztlicheuntersuchung.types.Sprachfaehigkeit;
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
import ch.dvbern.oss.hemed.esc.api.shared.abstractentity.AbstractEntity;
import ch.dvbern.oss.hemed.esc.api.shared.types.DateRange;
import ch.dvbern.oss.hemed.esc.api.shared.types.IgnoreForIdClassTest;
import ch.dvbern.oss.hemed.esc.api.shared.util.EnumSetUtil;
import ch.dvbern.oss.hemed.esc.api.shared.validator.CheckAnlassConfigImpfungKrankheitenUniqueness;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.hibernate.envers.Audited;

@Entity
@Table(
    uniqueConstraints = {
        @UniqueConstraint(name = "uc_anlassconfig_typ_bezeichnung", columnNames = { "anlasstyp", "bezeichnung" })
    }
)
@Audited
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@CheckAnlassConfigImpfungKrankheitenUniqueness
public class AnlassConfig extends AbstractEntity<AnlassConfig> {

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

    @NotNull
    @Column(nullable = false)
    private boolean showImpfeinverstaendnis;

    // Ausdrucke

    @NotNull
    @Column(nullable = false)
    private boolean showAnlassPrintImpfen;

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

    // Vorabklaerungen

    @NotNull
    @NonNull
    @Column(nullable = false, length = DbConst.DB_ENUM_LENGTH)
    @Enumerated(EnumType.STRING)
    private RequirementSetting vorabklaerungenFragebogenStatus;

    @NotNull
    @NonNull
    @Column(nullable = false, length = DbConst.DB_ENUM_LENGTH)
    @Enumerated(EnumType.STRING)
    private RequirementSetting vorabklaerungenEinverstaendnisImpfungen;

    @NotNull
    @NonNull
    @Column(nullable = false, length = DbConst.DB_ENUM_LENGTH)
    @Enumerated(EnumType.STRING)
    private RequirementSetting vorabklaerungenChronischeKrankheiten;

    // Impfungen

    @NotNull
    @Column(nullable = false)
    private boolean showImpfungenHeaderImpfausweisUnvollstaendig;

    @NotNull
    @Column(nullable = false)
    private boolean showImpfungenHeaderImpfausweisNichtVorhanden;

    @NotNull
    @Column(nullable = false)
    private boolean showImpfungenHeaderImpfausweisZweiPlusEinsSchema;

    @NotNull
    @Column(nullable = false)
    private boolean showImpfungenHeaderImpfausweisUpload;

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
    private RequirementSetting impfungenMassnahmeBemerkungen;

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

    @Nullable
    @Size(max = DbConst.DB_ENUM_LIST_LENGTH)
    @Column(nullable = false, length = DbConst.DB_ENUM_LIST_LENGTH)
    @Getter(AccessLevel.NONE)
    @Setter(AccessLevel.NONE)
    private String untersuchungImpfungEmpfehlungValues;

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

    @Nullable
    @Size(max = DbConst.DB_ENUM_LIST_LENGTH)
    @Column(nullable = false, length = DbConst.DB_ENUM_LIST_LENGTH)
    @Getter(AccessLevel.NONE)
    @Setter(AccessLevel.NONE)
    private String untersuchungImpfungKrankheitenLinksValues;

    @Nullable
    @Size(max = DbConst.DB_ENUM_LIST_LENGTH)
    @Column(nullable = false, length = DbConst.DB_ENUM_LIST_LENGTH)
    @Getter(AccessLevel.NONE)
    @Setter(AccessLevel.NONE)
    private String untersuchungImpfungKrankheitenRechtsValues;

    // ImpfungenDataMinimal

    @NotNull
    @NonNull
    @Column(nullable = false, length = DbConst.DB_ENUM_LENGTH)
    @Enumerated(EnumType.STRING)
    private RequirementSetting impfungenDataMinimalImpfausweisGeprueft;

    @NotNull
    @NonNull
    @Column(nullable = false, length = DbConst.DB_ENUM_LENGTH)
    @Enumerated(EnumType.STRING)
    private RequirementSetting impfungenDataMinimalImmunsuppression;

    @NotNull
    @NonNull
    @Column(nullable = false, length = DbConst.DB_ENUM_LENGTH)
    @Enumerated(EnumType.STRING)
    private RequirementSetting impfungenDataMinimalMasernImpfstatus;

    @NotNull
    @NonNull
    @Column(nullable = false, length = DbConst.DB_ENUM_LENGTH)
    @Enumerated(EnumType.STRING)
    private RequirementSetting impfungenDataMinimalMasernAnzahlImpfungen;

    @NotNull
    @NonNull
    @Column(nullable = false, length = DbConst.DB_ENUM_LENGTH)
    @Enumerated(EnumType.STRING)
    private RequirementSetting impfungenDataMinimalHpvImpfstatus;

    @NotNull
    @NonNull
    @Column(nullable = false, length = DbConst.DB_ENUM_LENGTH)
    @Enumerated(EnumType.STRING)
    private RequirementSetting impfungenDataMinimalHpvAnzahlImpfungen;

    @NotNull
    @NonNull
    @Column(nullable = false, length = DbConst.DB_ENUM_LENGTH)
    @Enumerated(EnumType.STRING)
    private RequirementSetting impfungenDataMinimalImpfempfehlung;

    @NotNull
    @NonNull
    @Column(nullable = false, length = DbConst.DB_ENUM_LENGTH)
    @Enumerated(EnumType.STRING)
    private RequirementSetting impfungenDataMinimalImpfungDurchgefuehrt;

    // ImpfungenNurUnvollstaendige

    @NotNull
    @NonNull
    @Column(nullable = false, length = DbConst.DB_ENUM_LENGTH)
    @Enumerated(EnumType.STRING)
    private RequirementSetting impfungenNurUnvollstaendige;

    // Sehen

    @NotNull
    @NonNull
    @Column(nullable = false, length = DbConst.DB_ENUM_LENGTH)
    @Enumerated(EnumType.STRING)
    private RequirementSetting sehenBrilleLinsen;

    @Nullable
    @Size(max = DbConst.DB_ENUM_LIST_LENGTH)
    @Column(nullable = true, length = DbConst.DB_ENUM_LIST_LENGTH)
    @Getter(AccessLevel.NONE)
    @Setter(AccessLevel.NONE)
    private String sehenBrilleLinsenValues;

    @Nullable
    @Enumerated(EnumType.STRING)
    @Column(length = DbConst.DB_ENUM_LENGTH)
    private BrilleLinsenResultat sehenBrilleLinsenDefaultValue;

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

    @Nullable
    @Column
    private BigDecimal sehenFernvisusDefaultValue;

    @NotNull
    @NonNull
    @Column(nullable = false, length = DbConst.DB_ENUM_LENGTH)
    @Enumerated(EnumType.STRING)
    private RequirementSetting sehenFernvisusAugeEinzeln;

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

    @Nullable
    @Size(max = DbConst.DB_ENUM_LIST_LENGTH)
    @Column(nullable = true, length = DbConst.DB_ENUM_LIST_LENGTH)
    @Getter(AccessLevel.NONE)
    @Setter(AccessLevel.NONE)
    private String sehenStrabismusValues;

    @NotNull
    @NonNull
    @Column(nullable = false, length = DbConst.DB_ENUM_LENGTH)
    @Enumerated(EnumType.STRING)
    private RequirementSetting sehenStrabismusAuspraegung;

    @NotNull
    @NonNull
    @Column(nullable = false, length = DbConst.DB_ENUM_LENGTH)
    @Enumerated(EnumType.STRING)
    private RequirementSetting sehenStrabismusAuftreten;

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

    @Nullable
    @Enumerated(EnumType.STRING)
    @Column(length = DbConst.DB_ENUM_LENGTH)
    private Dezibel hoerenAudiogrammDefaultValue;

    // Required will only be checked in Validator if hoerenAudiogramm is required
    @NotNull
    @NonNull
    @Column(nullable = false, length = DbConst.DB_ENUM_LENGTH)
    @Enumerated(EnumType.STRING)
    private RequirementSetting hoerenAudiogramm250Hz;

    @Nullable
    @Enumerated(EnumType.STRING)
    @Column(length = DbConst.DB_ENUM_LENGTH)
    private Dezibel hoerenAudiogramm250HzDefaultValue;

    // Required will only be checked in Validator if hoerenAudiogramm is required
    @NotNull
    @NonNull
    @Column(nullable = false, length = DbConst.DB_ENUM_LENGTH)
    @Enumerated(EnumType.STRING)
    private RequirementSetting hoerenAudiogramm8000Hz;

    @Nullable
    @Enumerated(EnumType.STRING)
    @Column(length = DbConst.DB_ENUM_LENGTH)
    private Dezibel hoerenAudiogramm8000HzDefaultValue;

    @NotNull
    @NonNull
    @Column(nullable = false, length = DbConst.DB_ENUM_LENGTH)
    @Enumerated(EnumType.STRING)
    private RequirementSetting hoerenAudiogrammSimpel;

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

    // Hoeren - Hoerhilfe
    @NotNull
    @NonNull
    @Column(nullable = false, length = DbConst.DB_ENUM_LENGTH)
    @Enumerated(EnumType.STRING)
    private RequirementSetting hoerenHoerhilfe;

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
    private RequirementSetting groesseGewichtGroessePerzentileSingleValue;

    @NotNull
    @NonNull
    @Column(nullable = false, length = DbConst.DB_ENUM_LENGTH)
    @Enumerated(EnumType.STRING)
    private RequirementSetting groesseGewichtGroessePerzentileMultiValue;

    @Nullable
    @Size(max = DbConst.DB_ENUM_LIST_LENGTH)
    @Column(nullable = true, length = DbConst.DB_ENUM_LIST_LENGTH)
    @Getter(AccessLevel.NONE)
    @Setter(AccessLevel.NONE)
    private String groesseGewichtGroessePerzentileValues;

    @NotNull
    @NonNull
    @Column(nullable = false, length = DbConst.DB_ENUM_LENGTH)
    @Enumerated(EnumType.STRING)
    private RequirementSetting groesseGewichtGewichtPerzentileSingleValue;

    @NotNull
    @NonNull
    @Column(nullable = false, length = DbConst.DB_ENUM_LENGTH)
    @Enumerated(EnumType.STRING)
    private RequirementSetting groesseGewichtGewichtPerzentileMultiValue;

    @Nullable
    @Size(max = DbConst.DB_ENUM_LIST_LENGTH)
    @Column(nullable = true, length = DbConst.DB_ENUM_LIST_LENGTH)
    @Getter(AccessLevel.NONE)
    @Setter(AccessLevel.NONE)
    private String groesseGewichtGewichtPerzentileValues;

    @NotNull
    @NonNull
    @Column(nullable = false, length = DbConst.DB_ENUM_LENGTH)
    @Enumerated(EnumType.STRING)
    private RequirementSetting groesseGewichtBmiPerzentileSingleValue;

    @NotNull
    @NonNull
    @Column(nullable = false, length = DbConst.DB_ENUM_LENGTH)
    @Enumerated(EnumType.STRING)
    private RequirementSetting groesseGewichtBmiPerzentileMultiValue;

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
    private RequirementSetting untersuchungEinbeinstandRechtsLinks;

    @Nullable
    @Size(max = DbConst.DB_ENUM_LIST_LENGTH)
    @Column(nullable = true, length = DbConst.DB_ENUM_LIST_LENGTH)
    @Getter(AccessLevel.NONE)
    @Setter(AccessLevel.NONE)
    private String untersuchungEinbeinstandValues;

    @NotNull
    @NonNull
    @Column(nullable = false, length = DbConst.DB_ENUM_LENGTH)
    @Enumerated(EnumType.STRING)
    private RequirementSetting untersuchungEinbeinhuepfen;

    @NotNull
    @NonNull
    @Column(nullable = false, length = DbConst.DB_ENUM_LENGTH)
    @Enumerated(EnumType.STRING)
    private RequirementSetting untersuchungEinbeinhuepfenRechtsLinks;

    @Nullable
    @Size(max = DbConst.DB_ENUM_LIST_LENGTH)
    @Column(nullable = true, length = DbConst.DB_ENUM_LIST_LENGTH)
    @Getter(AccessLevel.NONE)
    @Setter(AccessLevel.NONE)
    private String untersuchungEinbeinhuepfenValues;

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

    @Nullable
    @Size(max = DbConst.DB_ENUM_LIST_LENGTH)
    @Column(nullable = true, length = DbConst.DB_ENUM_LIST_LENGTH)
    @Getter(AccessLevel.NONE)
    @Setter(AccessLevel.NONE)
    private String untersuchungMotorikValues;

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
    private RequirementSetting untersuchungSprachfaehigkeitSingleValue;

    @NotNull
    @NonNull
    @Column(nullable = false, length = DbConst.DB_ENUM_LENGTH)
    @Enumerated(EnumType.STRING)
    private RequirementSetting untersuchungSprachfaehigkeitMultiValue;

    @Nullable
    @Size(max = DbConst.DB_ENUM_LIST_LENGTH)
    @Column(nullable = true, length = DbConst.DB_ENUM_LIST_LENGTH)
    @Getter(AccessLevel.NONE)
    @Setter(AccessLevel.NONE)
    private String untersuchungSprachfaehigkeitValues;

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

    // Untersuchung Sprache

    @NotNull
    @NonNull
    @Column(nullable = false, length = DbConst.DB_ENUM_LENGTH)
    @Enumerated(EnumType.STRING)
    private RequirementSetting untersuchungSpracheNachsprechen;

    @NotNull
    @NonNull
    @Column(nullable = false, length = DbConst.DB_ENUM_LENGTH)
    @Enumerated(EnumType.STRING)
    private RequirementSetting untersuchungSpracheWortschatz;

    @NotNull
    @NonNull
    @Column(nullable = false, length = DbConst.DB_ENUM_LENGTH)
    @Enumerated(EnumType.STRING)
    private RequirementSetting untersuchungSpracheSprachverstaendnis;

    @NotNull
    @NonNull
    @Column(nullable = false, length = DbConst.DB_ENUM_LENGTH)
    @Enumerated(EnumType.STRING)
    private RequirementSetting untersuchungSpracheBemerkungen;

    // Bewegungsapparat

    @NotNull
    @NonNull
    @Column(nullable = false, length = DbConst.DB_ENUM_LENGTH)
    @Enumerated(EnumType.STRING)
    private RequirementSetting bewegungsapparatSkoliose;

    @NotNull
    @NonNull
    @Column(nullable = false, length = DbConst.DB_ENUM_LENGTH)
    @Enumerated(EnumType.STRING)
    private RequirementSetting bewegungsapparatHaltungsschwaeche;

    @NotNull
    @NonNull
    @Column(nullable = false, length = DbConst.DB_ENUM_LENGTH)
    @Enumerated(EnumType.STRING)
    private RequirementSetting bewegungsapparatVerkuerzungHamstringmuskulatur;

    @NotNull
    @NonNull
    @Column(nullable = false, length = DbConst.DB_ENUM_LENGTH)
    @Enumerated(EnumType.STRING)
    private RequirementSetting bewegungsapparatBemerkung;

    // Untersuchung Schulrelevante Beeinträchtigung

    @NotNull
    @NonNull
    @Column(nullable = false, length = DbConst.DB_ENUM_LENGTH)
    @Enumerated(EnumType.STRING)
    private RequirementSetting untersuchungBeeintraechtigungFreiesGehen;

    @NotNull
    @NonNull
    @Column(nullable = false, length = DbConst.DB_ENUM_LENGTH)
    @Enumerated(EnumType.STRING)
    private RequirementSetting untersuchungBeeintraechtigungOhneStuetzradVelofahren;

    @NotNull
    @NonNull
    @Column(nullable = false, length = DbConst.DB_ENUM_LENGTH)
    @Enumerated(EnumType.STRING)
    private RequirementSetting untersuchungBeeintraechtigungGrafomotorik;

    @NotNull
    @NonNull
    @Column(nullable = false, length = DbConst.DB_ENUM_LENGTH)
    @Enumerated(EnumType.STRING)
    private RequirementSetting untersuchungBeeintraechtigungFeinmotorik;

    @NotNull
    @NonNull
    @Column(nullable = false, length = DbConst.DB_ENUM_LENGTH)
    @Enumerated(EnumType.STRING)
    private RequirementSetting untersuchungBeeintraechtigungGrobmotorik;

    @NotNull
    @NonNull
    @Column(nullable = false, length = DbConst.DB_ENUM_LENGTH)
    @Enumerated(EnumType.STRING)
    private RequirementSetting untersuchungBeeintraechtigungGleichgewicht;

    @NotNull
    @NonNull
    @Column(nullable = false, length = DbConst.DB_ENUM_LENGTH)
    @Enumerated(EnumType.STRING)
    private RequirementSetting untersuchungBeeintraechtigungBemerkung;

    // Abschluss

    @NotNull
    @NonNull
    @Column(nullable = false, length = DbConst.DB_ENUM_LENGTH)
    @Enumerated(EnumType.STRING)
    private RequirementSetting abschlussUeberweisungErfolgt;

    @NotNull
    @NonNull
    @Column(nullable = false, length = DbConst.DB_ENUM_LENGTH)
    @Enumerated(EnumType.STRING)
    private RequirementSetting abschlussAerztlicheValidierungErforderlich;

    @NotNull
    @NonNull
    @Column(nullable = false, length = DbConst.DB_ENUM_LENGTH)
    @Enumerated(EnumType.STRING)
    private RequirementSetting abschlussBeratungDurchgefuehrt;

    @NotNull
    @NonNull
    @Column(nullable = false, length = DbConst.DB_ENUM_LENGTH)
    @Enumerated(EnumType.STRING)
    private RequirementSetting abschlussBeurteilungEmpfohleneMassnahmen;

    @NotNull
    @Column(nullable = false)
    private boolean showCardAbschlussSignatur;

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

    // groesseGewichtGroessePerzentileValues
    public void setGroesseGewichtGroessePerzentileValues(
        @Nullable EnumSet<PerzentileGroesse> groesseGewichtGroessePerzentileValues) {
        this.groesseGewichtGroessePerzentileValues = EnumSetUtil.toString(groesseGewichtGroessePerzentileValues);
    }

    @NonNull
    public EnumSet<PerzentileGroesse> getGroesseGewichtGroessePerzentileValues() {
        return EnumSetUtil.fromString(PerzentileGroesse.class, groesseGewichtGroessePerzentileValues);
    }

    // groesseGewichtGewichtPerzentileValues
    public void setGroesseGewichtGewichtPerzentileValues(
        @Nullable EnumSet<PerzentileGewicht> groesseGewichtGewichtPerzentileValues) {
        this.groesseGewichtGewichtPerzentileValues = EnumSetUtil.toString(groesseGewichtGewichtPerzentileValues);
    }

    @NonNull
    public EnumSet<PerzentileGewicht> getGroesseGewichtGewichtPerzentileValues() {
        return EnumSetUtil.fromString(PerzentileGewicht.class, groesseGewichtGewichtPerzentileValues);
    }

    // groesseGewichtBmiPerzentileValues
    public void setGroesseGewichtBmiPerzentileValues(
        @Nullable EnumSet<PerzentileBmi> groesseGewichtBmiPerzentileValues) {
        this.groesseGewichtBmiPerzentileValues = EnumSetUtil.toString(groesseGewichtBmiPerzentileValues);
    }

    @NonNull
    public EnumSet<PerzentileBmi> getGroesseGewichtBmiPerzentileValues() {
        return EnumSetUtil.fromString(PerzentileBmi.class, groesseGewichtBmiPerzentileValues);
    }

    // untersuchungEinbeinstandValues
    public void setUntersuchungEinbeinstandValues(@Nullable EnumSet<Einbeinstand> untersuchungEinbeinstandValues) {
        this.untersuchungEinbeinstandValues = EnumSetUtil.toString(untersuchungEinbeinstandValues);
    }

    @NonNull
    public EnumSet<Einbeinstand> getUntersuchungEinbeinstandValues() {
        return EnumSetUtil.fromString(Einbeinstand.class, untersuchungEinbeinstandValues);
    }

    // untersuchungEinbeinhuepfenValues
    public void setUntersuchungEinbeinhuepfenValues(
        @Nullable EnumSet<Einbeinhuepfen> untersuchungEinbeinhuepfenValues) {
        this.untersuchungEinbeinhuepfenValues = EnumSetUtil.toString(untersuchungEinbeinhuepfenValues);
    }

    @NonNull
    public EnumSet<Einbeinhuepfen> getUntersuchungEinbeinhuepfenValues() {
        return EnumSetUtil.fromString(Einbeinhuepfen.class, untersuchungEinbeinhuepfenValues);
    }

    // untersuchungMotorikValues
    public void setUntersuchungMotorikValues(@Nullable EnumSet<Motorik> untersuchungMotorikValues) {
        this.untersuchungMotorikValues = EnumSetUtil.toString(untersuchungMotorikValues);
    }

    @NonNull
    public EnumSet<Motorik> getUntersuchungMotorikValues() {
        return EnumSetUtil.fromString(Motorik.class, untersuchungMotorikValues);
    }

    // untersuchungSprachfaehigkeitValues
    public void setUntersuchungSprachfaehigkeitValues(@Nullable EnumSet<Sprachfaehigkeit> untersuchungSpracheValues) {
        this.untersuchungSprachfaehigkeitValues = EnumSetUtil.toString(untersuchungSpracheValues);
    }

    @NonNull
    public EnumSet<Sprachfaehigkeit> getUntersuchungSprachfaehigkeitValues() {
        return EnumSetUtil.fromString(Sprachfaehigkeit.class, untersuchungSprachfaehigkeitValues);
    }

    // Impfungen Krankheiten Links
    public void setUntersuchungImpfungKrankheitenLinksValues(
        @NonNull EnumSet<Krankheit> untersuchungImpfungKrankheitenLinksValues) {
        this.untersuchungImpfungKrankheitenLinksValues =
            EnumSetUtil.toString(untersuchungImpfungKrankheitenLinksValues);
    }

    @NonNull
    public EnumSet<Krankheit> getUntersuchungImpfungKrankheitenLinksValues() {
        return EnumSetUtil.fromString(Krankheit.class, untersuchungImpfungKrankheitenLinksValues);
    }

    // Impfungen Krankheiten Rechts
    public void setUntersuchungImpfungKrankheitenRechtsValues(
        @NonNull EnumSet<Krankheit> untersuchungImpfungKrankheitenRechtsValues) {
        this.untersuchungImpfungKrankheitenRechtsValues =
            EnumSetUtil.toString(untersuchungImpfungKrankheitenRechtsValues);
    }

    @NonNull
    public EnumSet<Krankheit> getUntersuchungImpfungKrankheitenRechtsValues() {
        return EnumSetUtil.fromString(Krankheit.class, untersuchungImpfungKrankheitenRechtsValues);
    }

    // Sehen Brille Linsen
    public void setSehenBrilleLinsenValues(
        @NonNull EnumSet<BrilleLinsenResultat> sehenBrilleLinsen) {
        this.sehenBrilleLinsenValues =
            EnumSetUtil.toString(sehenBrilleLinsen);
    }

    @NonNull
    public EnumSet<BrilleLinsenResultat> getSehenBrilleLinsenValues() {
        return EnumSetUtil.fromString(BrilleLinsenResultat.class, sehenBrilleLinsenValues);
    }

    // Sehen Strabismus
    public void setSehenStrabismusValues(
        @NonNull EnumSet<StrabismusResultat> sehenStrabismus) {
        this.sehenStrabismusValues =
            EnumSetUtil.toString(sehenStrabismus);
    }

    @NonNull
    public EnumSet<StrabismusResultat> getSehenStrabismusValues() {
        return EnumSetUtil.fromString(StrabismusResultat.class, sehenStrabismusValues);
    }

    // Impfungen Empfehlung Values
    public void setUntersuchungImpfungEmpfehlungValues(
        @NonNull EnumSet<ImpfungenMassnahmenEmpfehlung> untersuchungImpfungEmpfehlungValues) {
        this.untersuchungImpfungEmpfehlungValues =
            EnumSetUtil.toString(untersuchungImpfungEmpfehlungValues);
    }

    @NonNull
    public EnumSet<ImpfungenMassnahmenEmpfehlung> getUntersuchungImpfungEmpfehlungValues() {
        return EnumSetUtil.fromString(ImpfungenMassnahmenEmpfehlung.class, untersuchungImpfungEmpfehlungValues);
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this)
            .append("id", getId().getId())
            .append("anlassTyp", anlassTyp)
            .append("gueltigkeitsZeitraum", gueltigkeitsZeitraum)
            .append("bezeichnung", bezeichnung)
            .toString();
    }
}
