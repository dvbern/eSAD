package ch.dvbern.oss.hemed.esc.api.features.anlass;

import java.io.Serial;

import ch.dvbern.dvbstarter.infrastructure.db.DbConst;
import ch.dvbern.oss.hemed.esc.api.features.anlass.types.AnlassTyp;
import ch.dvbern.oss.hemed.esc.api.features.anlass.types.RequirementSetting;
import ch.dvbern.oss.hemed.esc.api.shared.abstractentity.AbstractTenantEntity;
import ch.dvbern.oss.hemed.esc.api.shared.types.DateRange;
import jakarta.persistence.Column;
import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Table;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.hibernate.envers.Audited;

@Entity
@Table
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

    // Daten

    @NotNull
    @Column(nullable = false)
    private boolean showDatenPersonendaten;

    @NotNull
    @NonNull
    @Column(nullable = false, length = DbConst.DB_ENUM_LENGTH)
    @Enumerated(EnumType.STRING)
    private RequirementSetting datenUntersuchung;

    @NotNull
    @Column(nullable = false)
    private boolean showDatenSchule;

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
    private boolean impfungenMassnahmeFolgetermin;

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
    private boolean impfungenMassnahmeInformationenAbgegeben;

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
    private RequirementSetting sehenBemerkungen;

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
    private RequirementSetting untersuchungEinbeinhuepfen;

    @NotNull
    @NonNull
    @Column(nullable = false, length = DbConst.DB_ENUM_LENGTH)
    @Enumerated(EnumType.STRING)
    private RequirementSetting untersuchungFingeropposition;

    @NotNull
    @NonNull
    @Column(nullable = false, length = DbConst.DB_ENUM_LENGTH)
    @Enumerated(EnumType.STRING)
    private RequirementSetting untersuchungMotorik;

    @NotNull
    @NonNull
    @Column(nullable = false, length = DbConst.DB_ENUM_LENGTH)
    @Enumerated(EnumType.STRING)
    private RequirementSetting untersuchungSprache;

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

    public boolean areBothHoerenVisible() {
        boolean audiogrammVisible = false;
        boolean otoskopieVisible = false;
        if (getHoerenAudiogramm() != RequirementSetting.NOT_VISIBLE
            || getHoerenAudiogramm250Hz() != RequirementSetting.NOT_VISIBLE
            || getHoerenAudiogramm8000Hz() != RequirementSetting.NOT_VISIBLE
            || getHoerenAudiogrammBeurteilung() != RequirementSetting.NOT_VISIBLE
            || getHoerenAudiogrammZusaetzlicheAngaben() != RequirementSetting.NOT_VISIBLE
            || getHoerenAudiogrammBemerkungen() != RequirementSetting.NOT_VISIBLE) {
            audiogrammVisible = true;
        }
        if (getHoerenOtoskopie() != RequirementSetting.NOT_VISIBLE
            || getHoerenOtoskopieBemerkungen() != RequirementSetting.NOT_VISIBLE) {
            otoskopieVisible = true;
        }
        return audiogrammVisible && otoskopieVisible;
    }

}
