package ch.dvbern.oss.hemed.esc.api.features.anlass;

import ch.dvbern.dvbstarter.infrastructure.stereotypes.JaxMapper;
import ch.dvbern.oss.hemed.esc.api.features.anlass.json.AnlassConfigJson;
import ch.dvbern.oss.hemed.esc.api.features.anlass.types.RequirementSetting;
import org.checkerframework.checker.nullness.qual.NonNull;

@JaxMapper
public final class AnlassConfigMapper {

    private AnlassConfigMapper() {
    }

    @NonNull
    public static AnlassConfigJson toJson(@NonNull AnlassConfig config) {
        boolean isHoerenAudiogrammVisible = isAnyVisible(
            config.getHoerenAudiogramm(),
            config.getHoerenAudiogramm250Hz(),
            config.getHoerenAudiogramm8000Hz(),
            config.getHoerenAudiogrammBeurteilung(),
            config.getHoerenAudiogrammBemerkungen());
        boolean isHoerenOtoskopieVisible = isAnyVisible(
            config.getHoerenOtoskopie(),
            config.getHoerenOtoskopieBemerkungen());

        return new AnlassConfigJson(
            config.getId(),
            config.getAnlassTyp(),
            config.getGueltigkeitsZeitraum(),
            // Daten
            config.isShowDatenPersonendaten()
                || isAnyVisible(config.getDatenUntersuchung())
                || config.isShowDatenSchule(),
            config.isShowDatenPersonendaten(),
            config.getDatenUntersuchung(),
            config.isShowDatenSchule(),
            // Impfungen
            isAnyVisible(
                config.getImpfungenUebersicht(),
                config.getImpfungenListe())
                || config.isImpfungenMassnahmeFolgetermin()
                || config.isImpfungenMassnahmeEinzelFolgetermin()
                || config.isImpfungenMassnahmeInfoKinderarzt()
                || config.isImpfungenMassnahmeBeratungMPA()
                || config.isImpfungenMassnahmeInformationenAbgegeben()
                || config.isImpfungenMassnahmeInfoEltern()
                || config.isImpfungenMassnahmeRueckfrageKinderarzt(),
            config.getImpfungenUebersicht(),
            config.getImpfungenListe(),
            config.isImpfungenMassnahmeFolgetermin(),
            config.isImpfungenMassnahmeEinzelFolgetermin(),
            config.isImpfungenMassnahmeInfoKinderarzt(),
            config.isImpfungenMassnahmeBeratungMPA(),
            config.isImpfungenMassnahmeInformationenAbgegeben(),
            config.isImpfungenMassnahmeInfoEltern(),
            config.isImpfungenMassnahmeRueckfrageKinderarzt(),
            isAnyVisible(
                config.getCheckEinwilligungVorhanden(),
                config.getCheckKontraindikationenAbgeklaert(),
                config.getCheckDoppelkontrolleImpfentscheidDurchgefuehrt(),
                config.getUntersuchungImpfungBemerkungen()
            ),
            config.getCheckEinwilligungVorhanden(),
            config.getCheckKontraindikationenAbgeklaert(),
            config.getCheckDoppelkontrolleImpfentscheidDurchgefuehrt(),
            config.getUntersuchungImpfungBemerkungen(),
            // Sehen
            isAnyVisible(
                config.getSehenBrilleLinsen(),
                config.getSehenFarbsehen(),
                config.getSehenTafel(),
                config.getSehenFernvisus(),
                config.getSehenStereoTest(),
                config.getSehenStrabismus(),
                config.getSehenInspektion(),
                config.getSehenKonvergenz(),
                config.getSehenMotilitaet(),
                config.getSehenNahvisus(),
                config.getSehenBemerkungen(),
                config.getSehenMassnahmen()),
            config.getSehenBrilleLinsen(),
            config.getSehenFarbsehen(),
            config.getSehenTafel(),
            config.getSehenFernvisus(),
            config.getSehenStereoTest(),
            config.getSehenStrabismus(),
            config.getSehenInspektion(),
            config.getSehenKonvergenz(),
            config.getSehenMotilitaet(),
            config.getSehenNahvisus(),
            config.getSehenBemerkungen(),
            config.getSehenMassnahmen(),
            // Hoeren
            isAnyVisible(isHoerenAudiogrammVisible, isHoerenOtoskopieVisible),
            // Hoeren Audiogramm
            isHoerenAudiogrammVisible,
            config.getHoerenAudiogramm(),
            config.getHoerenAudiogramm250Hz(),
            config.getHoerenAudiogramm8000Hz(),
            config.getHoerenAudiogrammBeurteilung(),
            config.getHoerenAudiogrammZusaetzlicheAngaben(),
            config.getHoerenAudiogrammBemerkungen(),
            // Hoeren Otoskopie
            isHoerenOtoskopieVisible,
            config.getHoerenOtoskopie(),
            config.getHoerenOtoskopieBemerkungen(),
            // GroesseGewicht
            isAnyVisible(
                config.getGroesseGewichtGroesse(),
                config.getGroesseGewichtGewicht(),
                config.getGroesseGewichtBmi(),
                config.getGroesseGewichtGroessePerzentile(),
                config.getGroesseGewichtGewichtPerzentile(),
                config.getGroesseGewichtBmiPerzentile(),
                config.getGroesseGewichtBemerkungen()),
            config.getGroesseGewichtGroesse(),
            config.getGroesseGewichtGewicht(),
            config.getGroesseGewichtBmi(),
            config.getGroesseGewichtGroessePerzentile(),
            config.getGroesseGewichtGewichtPerzentile(),
            config.getGroesseGewichtBmiPerzentile(),
            config.getGroesseGewichtBemerkungen(),
            // Triage
            isAnyVisible(
                config.isShowTriageTriage(),
                config.isShowTriageKindWeiterleiten()
            ),
            config.isShowTriageTriage(),
            config.isShowTriageKindWeiterleiten(),
            // Untersuchung
            isAnyVisible(
                config.getUntersuchungBewegungsapparat(),
                config.getUntersuchungEinbeinstand(),
                config.getUntersuchungEinbeinhuepfen(),
                config.getUntersuchungFingeropposition(),
                config.getUntersuchungMotorik(),
                config.getUntersuchungSprache(),
                config.getUntersuchungMundZaehne(),
                config.getUntersuchungHaut(),
                config.getUntersuchungHerz(),
                config.getUntersuchungBlutdruck(),
                config.getUntersuchungVerhalten(),
                config.getUntersuchungBemerkungen()),
            config.getUntersuchungBewegungsapparat(),
            config.getUntersuchungEinbeinstand(),
            config.getUntersuchungEinbeinhuepfen(),
            config.getUntersuchungFingeropposition(),
            config.getUntersuchungMotorik(),
            config.getUntersuchungSprache(),
            config.getUntersuchungMundZaehne(),
            config.getUntersuchungHaut(),
            config.getUntersuchungHerz(),
            config.getUntersuchungBlutdruck(),
            config.getUntersuchungVerhalten(),
            config.getUntersuchungBemerkungen(),
            // Massnahmen
            isAnyVisible(
                config.isShowMassnahmenPathologischeBefunde(),
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
            ),
            config.isShowMassnahmenPathologischeBefunde(),
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

    private static boolean isAnyVisible(@NonNull RequirementSetting... settings) {
        for (RequirementSetting setting : settings) {
            if (setting.isVisible()) {
                return true;
            }
        }
        return false;
    }

    private static boolean isAnyVisible(boolean... settings) {
        for (boolean setting : settings) {
            if (setting) {
                return true;
            }
        }
        return false;
    }
}
