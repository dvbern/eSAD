package ch.dvbern.oss.hemed.esc.api.features.anlass.json;

import ch.dvbern.dvbstarter.infrastructure.stereotypes.JaxDTO;
import ch.dvbern.dvbstarter.shared.types.id.ID;
import ch.dvbern.oss.hemed.esc.api.features.anlass.AnlassConfig;
import ch.dvbern.oss.hemed.esc.api.features.anlass.types.AnlassTyp;
import ch.dvbern.oss.hemed.esc.api.features.anlass.types.RequirementSetting;
import ch.dvbern.oss.hemed.esc.api.shared.types.DateRange;
import lombok.AllArgsConstructor;
import lombok.Value;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.eclipse.microprofile.openapi.annotations.media.Schema;

@Value
@AllArgsConstructor
@JaxDTO
public class AnlassConfigJson {

    @NonNull
    @Schema(required = true)
    private final ID<AnlassConfig> id;

    @NonNull
    @Schema(required = true)
    private AnlassTyp anlassTyp;

    @NonNull
    @Schema(required = true)
    private DateRange gueltigkeitsZeitraum;

    // Daten

    @Schema(required = true)
    private boolean showTabDaten;

    @Schema(required = true)
    private boolean showDatenPersonendaten;

    @NonNull
    @Schema(required = true)
    private RequirementSetting datenUntersuchung;

    @Schema(required = true)
    private boolean showDatenSchule;

    // Impfungen

    @Schema(required = true)
    private boolean showTabImpfungen;

    @NonNull
    @Schema(required = true)
    private RequirementSetting impfungenUebersicht;

    @NonNull
    @Schema(required = true)
    private RequirementSetting impfungenListe;

    @Schema(required = true)
    private boolean impfungenMassnahmeFolgetermin;

    @Schema(required = true)
    private boolean impfungenMassnahmeEinzelFolgetermin;

    @Schema(required = true)
    private boolean impfungenMassnahmeInfoKinderarzt;

    @Schema(required = true)
    private boolean impfungenMassnahmeBeratungMPA;

    @Schema(required = true)
    private boolean impfungenMassnahmeInfomaterialAbgegeben;

    @Schema(required = true)
    private boolean impfungenMassnahmeInfoEltern;

    @Schema(required = true)
    private boolean impfungenMassnahmeRueckfrageKinderarzt;

    @Schema(required = true)
    private boolean showChecklist;

    @NonNull
    @Schema(required = true)
    private RequirementSetting checkEinwilligungVorhanden;

    @NonNull
    @Schema(required = true)
    private RequirementSetting checkKontraindikationenAbgeklaert;

    @NonNull
    @Schema(required = true)
    private RequirementSetting checkDoppelkontrolleImpfentscheidDurchgefuehrt;

    @NonNull
    @Schema(required = true)
    private RequirementSetting untersuchungImpfungBemerkungen;

    // Sehen

    @Schema(required = true)
    private boolean showTabSehen;

    @Schema(required = true)
    private boolean showTabSehenBeurteilung;

    @NonNull
    @Schema(required = true)
    private RequirementSetting sehenBrilleLinsen;

    @NonNull
    @Schema(required = true)
    private RequirementSetting sehenFarbsehen;

    @NonNull
    @Schema(required = true)
    private RequirementSetting sehenTafel;

    @NonNull
    @Schema(required = true)
    private RequirementSetting sehenFernvisus;

    @NonNull
    @Schema(required = true)
    private RequirementSetting sehenStereoTest;

    @NonNull
    @Schema(required = true)
    private RequirementSetting sehenStrabismus;

    @NonNull
    @Schema(required = true)
    private RequirementSetting sehenInspektion;

    @NonNull
    @Schema(required = true)
    private RequirementSetting sehenKonvergenz;

    @NonNull
    @Schema(required = true)
    private RequirementSetting sehenMotilitaet;

    @NonNull
    @Schema(required = true)
    private RequirementSetting sehenNahvisus;

    @NonNull
    @Schema(required = true)
    private RequirementSetting sehenBemerkungen;

    @Schema(required = true)
    private boolean showTabSehenBemerkungenFragenbogen;

    @NonNull
    @Schema(required = true)
    private RequirementSetting sehenBemerkungenFragebogen;

    @Schema(required = true)
    private boolean showTabSehenMassnahmen;

    @NonNull
    @Schema(required = true)
    private RequirementSetting sehenMassnahmen;

    // Hoeren

    @Schema(required = true)
    private boolean showTabHoeren;

    // Hoeren - Audiogramm

    @Schema(required = true)
    private boolean showTabHoerenAudiogramm;

    @NonNull
    @Schema(required = true)
    private RequirementSetting hoerenAudiogramm;

    @NonNull
    @Schema(required = true)
    private RequirementSetting hoerenAudiogramm250Hz;

    @NonNull
    @Schema(required = true)
    private RequirementSetting hoerenAudiogramm8000Hz;

    @NonNull
    @Schema(required = true)
    private RequirementSetting hoerenAudiogrammBeurteilung;

    @NonNull
    @Schema(required = true)
    private RequirementSetting hoerenAudiogrammZusaetzlicheAngaben;

    @NonNull
    @Schema(required = true)
    private RequirementSetting hoerenAudiogrammBemerkungen;

    // Hoeren - Otoskopie

    @Schema(required = true)
    private boolean showTabHoerenOtoskopie;

    @NonNull
    @Schema(required = true)
    private RequirementSetting hoerenOtoskopie;

    @NonNull
    @Schema(required = true)
    private RequirementSetting hoerenOtoskopieBemerkungen;

    // Groesse und Gewicht

    @Schema(required = true)
    private boolean showTabGroesseGewicht;

    @NonNull
    @Schema(required = true)
    private RequirementSetting groesseGewichtGroesse;

    @NonNull
    @Schema(required = true)
    private RequirementSetting groesseGewichtGewicht;

    @NonNull
    @Schema(required = true)
    private RequirementSetting groesseGewichtBmi;

    @NonNull
    @Schema(required = true)
    private RequirementSetting groesseGewichtGroessePerzentile;

    @NonNull
    @Schema(required = true)
    private RequirementSetting groesseGewichtGewichtPerzentile;

    @NonNull
    @Schema(required = true)
    private RequirementSetting groesseGewichtBmiPerzentile;

    @NonNull
    @Schema(required = true)
    private RequirementSetting groesseGewichtBemerkungen;

    // Triage

    @Schema(required = true)
    private boolean showTabTriage;

    @Schema(required = true)
    private boolean showTriageTriage;

    @Schema(required = true)
    private boolean showTriageKindWeiterleiten;

    // Aerztliche Untersuchung

    @Schema(required = true)
    private boolean showTabUntersuchung;

    @Schema(required = true)
    private boolean showTabUntersuchungBeurteilung;

    @Schema(required = true)
    private boolean showTabUntersuchungBemerkung;

    @NonNull
    @Schema(required = true)
    private RequirementSetting untersuchungBewegungsapparat;

    @NonNull
    @Schema(required = true)
    private RequirementSetting untersuchungEinbeinstand;

    @NonNull
    @Schema(required = true)
    private RequirementSetting untersuchungEinbeinhuepfen;

    @NonNull
    @Schema(required = true)
    private RequirementSetting untersuchungFingeropposition;

    @NonNull
    @Schema(required = true)
    private RequirementSetting untersuchungMotorik;

    @NonNull
    @Schema(required = true)
    private RequirementSetting untersuchungSprache;

    @NonNull
    @Schema(required = true)
    private RequirementSetting untersuchungMundZaehne;

    @NonNull
    @Schema(required = true)
    private RequirementSetting untersuchungHaut;

    @NonNull
    @Schema(required = true)
    private RequirementSetting untersuchungHerz;

    @NonNull
    @Schema(required = true)
    private RequirementSetting untersuchungBlutdruck;

    @NonNull
    @Schema(required = true)
    private RequirementSetting untersuchungVerhalten;

    @NonNull
    @Schema(required = true)
    private RequirementSetting untersuchungBemerkungen;

    // Massnahmen

    @Schema(required = true)
    private boolean showTabMassnahmen;

    @Schema(required = true)
    private boolean showMassnahmenPathologischeBefunde;

    @Schema(required = true)
    private boolean showMassnahmenKeine;

    @Schema(required = true)
    private boolean showMassnahmenBeratungMpa;

    @Schema(required = true)
    private boolean showMassnahmenBeratungSchulAerztin;

    @Schema(required = true)
    private boolean showMassnahmenBereitsBehandelt;

    @Schema(required = true)
    private boolean showMassnahmenInfomaterial;

    @Schema(required = true)
    private boolean showMassnahmenInfoEltern;

    @Schema(required = true)
    private boolean showMassnahmenUeberweisungHausarzt;

    @Schema(required = true)
    private boolean showMassnahmenUeberweisungAugenarzt;

    @Schema(required = true)
    private boolean showMassnahmenNachkontrolleSaed;

    @Schema(required = true)
    private boolean showMassnahmenAnderes;
}
