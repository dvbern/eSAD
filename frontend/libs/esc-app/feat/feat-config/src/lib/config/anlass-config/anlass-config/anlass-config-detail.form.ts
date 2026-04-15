import {DestroyRef} from '@angular/core';
import {takeUntilDestroyed} from '@angular/core/rxjs-interop';
import {NonNullableFormBuilder, ValidatorFn, Validators} from '@angular/forms';
import {
  AnlassConfigSaveJson,
  AnlassTyp, BrilleLinsenResultat,
  Einbeinhuepfen,
  Einbeinstand,
  ImpfungenMassnahmenEmpfehlung,
  Krankheit,
  Motorik,
  PerzentileBmi,
  RequirementSetting,
  Sprachfaehigkeit, StrabismusResultat,
} from '@dvb/data-access-backend';
import {ISODateString} from '@dvb/data-access-types';
import {AppValidatorKeys, AppValidators, BaseForm} from '@dvb/util-angular';
import {ensurePropsFlat, fromISODateString, isNotNullish, requireValue, today} from '@dvb/util-shared';
import {distinctUntilChanged} from 'rxjs';
import {globalConstants} from '@dvb/feat-shared';
import {AnlassConfigStore} from '../anlass-config.store';

type Config = {
  fb: NonNullableFormBuilder;
  destroyRef: DestroyRef;
  anlassConfigStore: AnlassConfigStore;
};

function makeForm(config: Config) {
  const fb = config.fb;

  const group = fb.group(
    {
      anlassTyp: fb.control<AnlassTyp | undefined>(undefined, Validators.required),
      datumVon: fb.control<ISODateString | undefined>(undefined, [Validators.required]),
      datumBis: fb.control<ISODateString | undefined>(undefined, [Validators.required]),
      bezeichnung: fb.control<string | undefined>(undefined, Validators.required),
      showImpfausweis: fb.control<boolean>(false),
      showFragebogen: fb.control<boolean>(false),
      showAnmeldungBestaetigung: fb.control<boolean>(false),
      showDatenPersonendaten: fb.control<boolean>(false, [Validators.required]),
      showDatenPersonendatenEsadId: fb.control<boolean>(false, [Validators.required]),
      datenUntersuchung: fb.control<RequirementSetting>(RequirementSetting.NOT_VISIBLE, Validators.required),
      showDatenSchule: fb.control<boolean>(false, [Validators.required]),
      datenBemerkung: fb.control<RequirementSetting>(RequirementSetting.NOT_VISIBLE, Validators.required),
      vorabklaerungenFragebogenStatus: fb.control<RequirementSetting>(RequirementSetting.NOT_VISIBLE, Validators.required),
      vorabklaerungenEinverstaendnisImpfungen: fb.control<RequirementSetting>(RequirementSetting.NOT_VISIBLE, Validators.required),
      vorabklaerungenChronischeKrankheiten: fb.control<RequirementSetting>(RequirementSetting.NOT_VISIBLE, Validators.required),
      showImpfungenHeaderImpfausweisUnvollstaendig: fb.control<boolean>(false, [Validators.required]),
      showImpfungenHeaderImpfausweisNichtVorhanden: fb.control<boolean>(false, [Validators.required]),
      showImpfungenHeaderImpfausweisZweiPlusEinsSchema: fb.control<boolean>(false, [Validators.required]),
      showImpfungenHeaderImpfausweisUpload: fb.control<boolean>(false, [Validators.required]),
      impfungenUebersicht: fb.control<RequirementSetting>(RequirementSetting.NOT_VISIBLE, Validators.required),
      impfungenListe: fb.control<RequirementSetting>(RequirementSetting.NOT_VISIBLE, Validators.required),
      showImpfungenDoppelkontrolle: fb.control<boolean>(false, [Validators.required]),
      impfungenMassnahmeKeineMassnahmen: fb.control<boolean>(false, [Validators.required]),
      impfungenMassnahmeGruppenFolgetermin: fb.control<boolean>(false, [Validators.required]),
      impfungenMassnahmeImpfenFolgetermin: fb.control<boolean>(false, [Validators.required]),
      impfungenMassnahmeEinzelFolgetermin: fb.control<boolean>(false, [Validators.required]),
      impfungenMassnahmeInfoKinderarzt: fb.control<boolean>(false, [Validators.required]),
      impfungenMassnahmeBeratungMPA: fb.control<boolean>(false, [Validators.required]),
      impfungenMassnahmeInfomaterialAbgegeben: fb.control<boolean>(false, [Validators.required]),
      impfungenMassnahmeInfoEltern: fb.control<boolean>(false, [Validators.required]),
      impfungenMassnahmeRueckfrageKinderarzt: fb.control<boolean>(false, [Validators.required]),
      impfungenMassnahmeBemerkungen: fb.control<RequirementSetting>(
        RequirementSetting.NOT_VISIBLE,
        Validators.required,
      ),
      checkEinwilligungVorhanden: fb.control<RequirementSetting>(RequirementSetting.NOT_VISIBLE, Validators.required),
      checkKontraindikationenAbgeklaert: fb.control<RequirementSetting>(
        RequirementSetting.NOT_VISIBLE,
        Validators.required,
      ),
      checkDoppelkontrolleImpfentscheidDurchgefuehrt: fb.control<RequirementSetting>(
        RequirementSetting.NOT_VISIBLE,
        Validators.required,
      ),
      untersuchungImpfungEmpfehlung: fb.control<RequirementSetting>(
        RequirementSetting.NOT_VISIBLE,
        Validators.required,
      ),
      untersuchungImpfungEmpfehlungValues: fb.control<ImpfungenMassnahmenEmpfehlung[]>([]),
      untersuchungImpfungAchtung: fb.control<RequirementSetting>(RequirementSetting.NOT_VISIBLE, Validators.required),
      untersuchungImpfungBemerkungen: fb.control<RequirementSetting>(
        RequirementSetting.NOT_VISIBLE,
        Validators.required,
      ),
      untersuchungImpfungKrankheitenLinksValues: fb.control<Krankheit[]>([]),
      untersuchungImpfungKrankheitenRechtsValues: fb.control<Krankheit[]>([]),
      impfungenDataMinimalImpfausweisGeprueft: fb.control<RequirementSetting>(
        RequirementSetting.NOT_VISIBLE,
        Validators.required,
      ),
      impfungenDataMinimalImmunsuppression: fb.control<RequirementSetting>(
        RequirementSetting.NOT_VISIBLE,
        Validators.required,
      ),
      impfungenDataMinimalMasernImpfstatus: fb.control<RequirementSetting>(
        RequirementSetting.NOT_VISIBLE,
        Validators.required,
      ),
      impfungenDataMinimalMasernAnzahlImpfungen: fb.control<RequirementSetting>(
        RequirementSetting.NOT_VISIBLE,
        Validators.required,
      ),
      impfungenDataMinimalHpvImpfstatus: fb.control<RequirementSetting>(
        RequirementSetting.NOT_VISIBLE,
        Validators.required,
      ),
      impfungenDataMinimalHpvAnzahlImpfungen: fb.control<RequirementSetting>(
        RequirementSetting.NOT_VISIBLE,
        Validators.required,
      ),
      impfungenDataMinimalImpfempfehlung: fb.control<RequirementSetting>(
        RequirementSetting.NOT_VISIBLE,
        Validators.required,
      ),
      impfungenDataMinimalImpfungDurchgefuehrt: fb.control<RequirementSetting>(
        RequirementSetting.NOT_VISIBLE,
        Validators.required,
      ),
      sehenBrilleLinsen: fb.control<RequirementSetting>(RequirementSetting.NOT_VISIBLE, Validators.required),
      sehenBrilleLinsenValues: fb.control<BrilleLinsenResultat[]>([]),
      sehenFarbsehen: fb.control<RequirementSetting>(RequirementSetting.NOT_VISIBLE, Validators.required),
      sehenTafel: fb.control<RequirementSetting>(RequirementSetting.NOT_VISIBLE, Validators.required),
      sehenFernvisus: fb.control<RequirementSetting>(RequirementSetting.NOT_VISIBLE, Validators.required),
      sehenFernvisusAugeEinzeln: fb.control<RequirementSetting>(RequirementSetting.NOT_VISIBLE, Validators.required),
      sehenStereoTest: fb.control<RequirementSetting>(RequirementSetting.NOT_VISIBLE, Validators.required),
      sehenStrabismus: fb.control<RequirementSetting>(RequirementSetting.NOT_VISIBLE, Validators.required),
      sehenStrabismusValues: fb.control<StrabismusResultat[]>([]),
      sehenStrabismusAuspraegung: fb.control<RequirementSetting>(RequirementSetting.NOT_VISIBLE, Validators.required),
      sehenStrabismusAuftreten: fb.control<RequirementSetting>(RequirementSetting.NOT_VISIBLE, Validators.required),
      sehenInspektion: fb.control<RequirementSetting>(RequirementSetting.NOT_VISIBLE, Validators.required),
      sehenKonvergenz: fb.control<RequirementSetting>(RequirementSetting.NOT_VISIBLE, Validators.required),
      sehenMotilitaet: fb.control<RequirementSetting>(RequirementSetting.NOT_VISIBLE, Validators.required),
      sehenNahvisus: fb.control<RequirementSetting>(RequirementSetting.NOT_VISIBLE, Validators.required),
      sehenEmpfehlung: fb.control<RequirementSetting>(RequirementSetting.NOT_VISIBLE, Validators.required),
      sehenBemerkungen: fb.control<RequirementSetting>(RequirementSetting.NOT_VISIBLE, Validators.required),
      sehenBemerkungenFragebogen: fb.control<RequirementSetting>(RequirementSetting.NOT_VISIBLE, Validators.required),
      sehenMassnahmen: fb.control<RequirementSetting>(RequirementSetting.NOT_VISIBLE, Validators.required),
      hoerenAudiogramm: fb.control<RequirementSetting>(RequirementSetting.NOT_VISIBLE, Validators.required),
      hoerenAudiogramm250Hz: fb.control<RequirementSetting>(RequirementSetting.NOT_VISIBLE, Validators.required),
      hoerenAudiogramm8000Hz: fb.control<RequirementSetting>(RequirementSetting.NOT_VISIBLE, Validators.required),
      hoerenAudiogrammSimpel: fb.control<RequirementSetting>(RequirementSetting.NOT_VISIBLE, Validators.required),
      hoerenAudiogrammBeurteilung: fb.control<RequirementSetting>(RequirementSetting.NOT_VISIBLE, Validators.required),
      hoerenAudiogrammZusaetzlicheAngaben: fb.control<RequirementSetting>(
        RequirementSetting.NOT_VISIBLE,
        Validators.required,
      ),
      hoerenAudiogrammEmpfehlung: fb.control<RequirementSetting>(RequirementSetting.NOT_VISIBLE, Validators.required),
      hoerenAudiogrammBemerkungen: fb.control<RequirementSetting>(RequirementSetting.NOT_VISIBLE, Validators.required),
      hoerenOtoskopie: fb.control<RequirementSetting>(RequirementSetting.NOT_VISIBLE, Validators.required),
      hoerenOtoskopieBemerkungen: fb.control<RequirementSetting>(RequirementSetting.NOT_VISIBLE, Validators.required),
      hoerenHoerhilfe: fb.control<RequirementSetting>(RequirementSetting.NOT_VISIBLE, Validators.required),
      groesseGewichtGroesse: fb.control<RequirementSetting>(RequirementSetting.NOT_VISIBLE, Validators.required),
      groesseGewichtGewicht: fb.control<RequirementSetting>(RequirementSetting.NOT_VISIBLE, Validators.required),
      groesseGewichtBmi: fb.control<RequirementSetting>(RequirementSetting.NOT_VISIBLE, Validators.required),
      groesseGewichtGroessePerzentile: fb.control<RequirementSetting>(
        RequirementSetting.NOT_VISIBLE,
        Validators.required,
      ),
      groesseGewichtGewichtPerzentile: fb.control<RequirementSetting>(
        RequirementSetting.NOT_VISIBLE,
        Validators.required,
      ),
      groesseGewichtBmiPerzentile: fb.control<RequirementSetting>(RequirementSetting.NOT_VISIBLE, Validators.required),
      groesseGewichtBmiPerzentileValues: fb.control<PerzentileBmi[]>([]),
      groesseGewichtEmpfehlung: fb.control<RequirementSetting>(RequirementSetting.NOT_VISIBLE, Validators.required),
      groesseGewichtBemerkungen: fb.control<RequirementSetting>(RequirementSetting.NOT_VISIBLE, Validators.required),
      showTriageTriage: fb.control<boolean>(false, [Validators.required]),
      showTriageKindWeiterleiten: fb.control<boolean>(false, [Validators.required]),
      laeusekontrolleBeurteilung: fb.control<RequirementSetting>(RequirementSetting.NOT_VISIBLE, Validators.required),
      laeusekontrolleEmpfehlung: fb.control<RequirementSetting>(RequirementSetting.NOT_VISIBLE, Validators.required),
      laeusekontrolleBemerkungen: fb.control<RequirementSetting>(RequirementSetting.NOT_VISIBLE, Validators.required),
      untersuchungBewegungsapparat: fb.control<RequirementSetting>(RequirementSetting.NOT_VISIBLE, Validators.required),
      untersuchungEinbeinstand: fb.control<RequirementSetting>(RequirementSetting.NOT_VISIBLE, Validators.required),
      untersuchungEinbeinstandRechtsLinks: fb.control<RequirementSetting>(
        RequirementSetting.NOT_VISIBLE,
        Validators.required,
      ),
      untersuchungEinbeinstandValues: fb.control<Einbeinstand[]>([]),
      untersuchungEinbeinhuepfen: fb.control<RequirementSetting>(RequirementSetting.NOT_VISIBLE, Validators.required),
      untersuchungEinbeinhuepfenRechtsLinks: fb.control<RequirementSetting>(
        RequirementSetting.NOT_VISIBLE,
        Validators.required,
      ),
      untersuchungEinbeinhuepfenValues: fb.control<Einbeinhuepfen[]>([]),
      untersuchungFingeropposition: fb.control<RequirementSetting>(RequirementSetting.NOT_VISIBLE, Validators.required),
      untersuchungHampelmann: fb.control<RequirementSetting>(RequirementSetting.NOT_VISIBLE, Validators.required),
      untersuchungBallFangen: fb.control<RequirementSetting>(RequirementSetting.NOT_VISIBLE, Validators.required),
      untersuchungBallWerfen: fb.control<RequirementSetting>(RequirementSetting.NOT_VISIBLE, Validators.required),
      untersuchungNameSchreiben: fb.control<RequirementSetting>(RequirementSetting.NOT_VISIBLE, Validators.required),
      untersuchungFigurNachmalen: fb.control<RequirementSetting>(RequirementSetting.NOT_VISIBLE, Validators.required),
      untersuchungStifthaltung: fb.control<RequirementSetting>(RequirementSetting.NOT_VISIBLE, Validators.required),
      untersuchungAussprache: fb.control<RequirementSetting>(RequirementSetting.NOT_VISIBLE, Validators.required),
      untersuchungLesefit: fb.control<RequirementSetting>(RequirementSetting.NOT_VISIBLE, Validators.required),
      untersuchungLesefitPunkte: fb.control<RequirementSetting>(RequirementSetting.NOT_VISIBLE, Validators.required),
      untersuchungZahlenNachsprechen4: fb.control<RequirementSetting>(
        RequirementSetting.NOT_VISIBLE,
        Validators.required,
      ),
      untersuchungZahlenNachsprechen5: fb.control<RequirementSetting>(
        RequirementSetting.NOT_VISIBLE,
        Validators.required,
      ),
      untersuchungZeichnenKreis: fb.control<RequirementSetting>(RequirementSetting.NOT_VISIBLE, Validators.required),
      untersuchungZeichnenViereck: fb.control<RequirementSetting>(RequirementSetting.NOT_VISIBLE, Validators.required),
      untersuchungZeichnenDreieck: fb.control<RequirementSetting>(RequirementSetting.NOT_VISIBLE, Validators.required),
      untersuchungZeichnenMensch: fb.control<RequirementSetting>(RequirementSetting.NOT_VISIBLE, Validators.required),
      untersuchungZeichnenMenschPunkte: fb.control<RequirementSetting>(
        RequirementSetting.NOT_VISIBLE,
        Validators.required,
      ),
      untersuchungMotorik: fb.control<RequirementSetting>(RequirementSetting.NOT_VISIBLE, Validators.required),
      untersuchungMotorikValues: fb.control<Motorik[]>([]),
      untersuchungMotorikPunkte: fb.control<RequirementSetting>(RequirementSetting.NOT_VISIBLE, Validators.required),
      untersuchungKognition: fb.control<RequirementSetting>(RequirementSetting.NOT_VISIBLE, Validators.required),
      untersuchungKognitionPunkte: fb.control<RequirementSetting>(RequirementSetting.NOT_VISIBLE, Validators.required),
      untersuchungSprachfaehigkeitSingleValue: fb.control<RequirementSetting>(
        RequirementSetting.NOT_VISIBLE,
        Validators.required,
      ),
      untersuchungSprachfaehigkeitMultiValue: fb.control<RequirementSetting>(
        RequirementSetting.NOT_VISIBLE,
        Validators.required,
      ),
      untersuchungSprachfaehigkeitValues: fb.control<Sprachfaehigkeit[]>([]),
      untersuchungSprachfaehigkeitPunkte: fb.control<RequirementSetting>(
        RequirementSetting.NOT_VISIBLE,
        Validators.required,
      ),
      untersuchungMundZaehne: fb.control<RequirementSetting>(RequirementSetting.NOT_VISIBLE, Validators.required),
      untersuchungHaut: fb.control<RequirementSetting>(RequirementSetting.NOT_VISIBLE, Validators.required),
      untersuchungHerz: fb.control<RequirementSetting>(RequirementSetting.NOT_VISIBLE, Validators.required),
      untersuchungBlutdruck: fb.control<RequirementSetting>(RequirementSetting.NOT_VISIBLE, Validators.required),
      untersuchungVerhalten: fb.control<RequirementSetting>(RequirementSetting.NOT_VISIBLE, Validators.required),
      unterlagenEinverstaendnis: fb.control<RequirementSetting>(RequirementSetting.NOT_VISIBLE, Validators.required),
      unterlagenVerhaltensfragebogen: fb.control<RequirementSetting>(
        RequirementSetting.NOT_VISIBLE,
        Validators.required,
      ),
      untersuchungEmpfehlung: fb.control<RequirementSetting>(RequirementSetting.NOT_VISIBLE, Validators.required),
      untersuchungBemerkungen: fb.control<RequirementSetting>(RequirementSetting.NOT_VISIBLE, Validators.required),
      untersuchungSpracheNachsprechen: fb.control<RequirementSetting>(
        RequirementSetting.NOT_VISIBLE,
        Validators.required,
      ),
      untersuchungSpracheWortschatz: fb.control<RequirementSetting>(
        RequirementSetting.NOT_VISIBLE,
        Validators.required,
      ),
      untersuchungSpracheSprachverstaendnis: fb.control<RequirementSetting>(
        RequirementSetting.NOT_VISIBLE,
        Validators.required,
      ),
      untersuchungSpracheBemerkungen: fb.control<RequirementSetting>(
        RequirementSetting.NOT_VISIBLE,
        Validators.required,
      ),
      untersuchungBeeintraechtigungFreiesGehen: fb.control<RequirementSetting>(
        RequirementSetting.NOT_VISIBLE,
        Validators.required,
      ),
      untersuchungBeeintraechtigungOhneStuetzradVelofahren: fb.control<RequirementSetting>(
        RequirementSetting.NOT_VISIBLE,
        Validators.required,
      ),
      untersuchungBeeintraechtigungGrafomotorik: fb.control<RequirementSetting>(
        RequirementSetting.NOT_VISIBLE,
        Validators.required,
      ),
      untersuchungBeeintraechtigungFeinmotorik: fb.control<RequirementSetting>(
        RequirementSetting.NOT_VISIBLE,
        Validators.required,
      ),
      untersuchungBeeintraechtigungGrobmotorik: fb.control<RequirementSetting>(
        RequirementSetting.NOT_VISIBLE,
        Validators.required,
      ),
      untersuchungBeeintraechtigungGleichgewicht: fb.control<RequirementSetting>(
        RequirementSetting.NOT_VISIBLE,
        Validators.required,
      ),
      untersuchungBeeintraechtigungBemerkung: fb.control<RequirementSetting>(
        RequirementSetting.NOT_VISIBLE,
        Validators.required,
      ),
      bewegungsapparatSkoliose: fb.control<RequirementSetting>(RequirementSetting.NOT_VISIBLE, Validators.required),
      bewegungsapparatHaltungsschwaeche: fb.control<RequirementSetting>(
        RequirementSetting.NOT_VISIBLE,
        Validators.required,
      ),
      bewegungsapparatVerkuerzungHamstringmuskulatur: fb.control<RequirementSetting>(
        RequirementSetting.NOT_VISIBLE,
        Validators.required,
      ),
      bewegungsapparatBemerkung: fb.control<RequirementSetting>(RequirementSetting.NOT_VISIBLE, Validators.required),
      abschlussUeberweisungErfolgt: fb.control<RequirementSetting>(RequirementSetting.NOT_VISIBLE, Validators.required),
      abschlussAerztlicheValidierungErforderlich: fb.control<RequirementSetting>(RequirementSetting.NOT_VISIBLE,
        Validators.required),
      abschlussBeratungDurchgefuehrt: fb.control<RequirementSetting>(RequirementSetting.NOT_VISIBLE,
        Validators.required),
      abschlussBeurteilungEmpfohleneMassnahmen: fb.control<RequirementSetting>(RequirementSetting.NOT_VISIBLE,
        Validators.required),
      showCardAbschlussSignatur: fb.control<boolean>(false, [Validators.required]),
      showMassnahmenPathologischeBefunde: fb.control<boolean>(false, [Validators.required]),
      showMassnahmenKeine: fb.control<boolean>(false, [Validators.required]),
      showMassnahmenBeratungMpa: fb.control<boolean>(false, [Validators.required]),
      showMassnahmenBeratungSchulAerztin: fb.control<boolean>(false, [Validators.required]),
      showMassnahmenBereitsBehandelt: fb.control<boolean>(false, [Validators.required]),
      showMassnahmenInfomaterial: fb.control<boolean>(false, [Validators.required]),
      showMassnahmenInfoEltern: fb.control<boolean>(false, [Validators.required]),
      showMassnahmenUeberweisungHausarzt: fb.control<boolean>(false, [Validators.required]),
      showMassnahmenUeberweisungAugenarzt: fb.control<boolean>(false, [Validators.required]),
      showMassnahmenNachkontrolleSaed: fb.control<boolean>(false, [Validators.required]),
      showMassnahmenAnderes: fb.control<boolean>(false, [Validators.required]),
    },
    {validators: [einmalAuswahlKrankheiten()]},
  );

  group.controls.anlassTyp.valueChanges.pipe(distinctUntilChanged(), takeUntilDestroyed(config.destroyRef)).subscribe({
    next: anlassTyp => {
      if (isNotNullish(anlassTyp)) {
        config.anlassConfigStore.calculateMinDatumVon(anlassTyp);
        const minStartDate = config.anlassConfigStore.minDatumVon();
        group.controls.datumVon.setValidators([
          Validators.required,
          AppValidators.nonPastDates(today()),
          AppValidators.minDate(fromISODateString(minStartDate)),
        ]);
        group.controls.datumVon.updateValueAndValidity();
      }
    },
    error: console.error,
  });

  return group;
}

function einmalAuswahlKrankheiten(): ValidatorFn {
  return group => {
    const krankheitenLinks: Krankheit[] = group.get('untersuchungImpfungKrankheitenLinksValues')?.value;
    const krankheitenRechts: Krankheit[] = group.get('untersuchungImpfungKrankheitenRechtsValues')?.value;

    if (krankheitenLinks.length === 0 || krankheitenRechts.length === 0) {
      return null;
    }

    const uberschneidungen = krankheitenLinks.filter(krankheit => krankheitenRechts.includes(krankheit));
    if (uberschneidungen.length > 0) {
      return {
        [AppValidatorKeys.einmalAuswahlKrankheiten]: true,
      };
    }

    return null;
  };
}

export class AnlassConfigForm extends BaseForm<ReturnType<typeof makeForm>, AnlassConfigSaveJson> {
  constructor(config: Config) {
    super(() => makeForm(config));
  }

  setBasis(anlassConfig: AnlassConfigSaveJson | undefined) {
    this.group.reset({
      ...anlassConfig,
      // Die nachfolgenden Werte sollten nicht überschrieben werden.
      anlassTyp: this.group.controls.anlassTyp.value,
      datumVon: this.group.controls.datumVon.value,
      datumBis: this.group.controls.datumBis.value,
      bezeichnung: this.group.controls.bezeichnung.value,
    });
  }

  override valueOnValid(): AnlassConfigSaveJson {
    const value = requireValue(this.group.getRawValue());
    return ensurePropsFlat<AnlassConfigSaveJson>({
      anlassTyp: requireValue(value.anlassTyp),
      datumVon: requireValue(value.datumVon),
      datumBis: requireValue(value.datumBis),
      bezeichnung: requireValue(value.bezeichnung),
      showImpfausweis: requireValue(value.showImpfausweis),
      showFragebogen: requireValue(value.showFragebogen),
      showAnmeldungBestaetigung: requireValue(value.showAnmeldungBestaetigung),
      showDatenPersonendaten: requireValue(value.showDatenPersonendaten),
      showDatenPersonendatenEsadId: requireValue(value.showDatenPersonendatenEsadId),
      datenUntersuchung: requireValue(value.datenUntersuchung),
      showDatenSchule: requireValue(value.showDatenSchule),
      datenBemerkung: requireValue(value.datenBemerkung),
      vorabklaerungenFragebogenStatus: requireValue(value.vorabklaerungenFragebogenStatus),
      vorabklaerungenEinverstaendnisImpfungen: requireValue(value.vorabklaerungenEinverstaendnisImpfungen),
      vorabklaerungenChronischeKrankheiten: requireValue(value.vorabklaerungenChronischeKrankheiten),
      showImpfungenHeaderImpfausweisUnvollstaendig: requireValue(value.showImpfungenHeaderImpfausweisUnvollstaendig),
      showImpfungenHeaderImpfausweisNichtVorhanden: requireValue(value.showImpfungenHeaderImpfausweisNichtVorhanden),
      showImpfungenHeaderImpfausweisZweiPlusEinsSchema: requireValue(
        value.showImpfungenHeaderImpfausweisZweiPlusEinsSchema,
      ),
      showImpfungenHeaderImpfausweisUpload: requireValue(value.showImpfungenHeaderImpfausweisUpload),
      impfungenUebersicht: requireValue(value.impfungenUebersicht),
      impfungenListe: requireValue(value.impfungenListe),
      showImpfungenDoppelkontrolle: requireValue(value.showImpfungenDoppelkontrolle),
      impfungenMassnahmeKeineMassnahmen: requireValue(value.impfungenMassnahmeKeineMassnahmen),
      impfungenMassnahmeGruppenFolgetermin: requireValue(value.impfungenMassnahmeGruppenFolgetermin),
      impfungenMassnahmeImpfenFolgetermin: requireValue(value.impfungenMassnahmeImpfenFolgetermin),
      impfungenMassnahmeEinzelFolgetermin: requireValue(value.impfungenMassnahmeEinzelFolgetermin),
      impfungenMassnahmeInfoKinderarzt: requireValue(value.impfungenMassnahmeInfoKinderarzt),
      impfungenMassnahmeBeratungMPA: requireValue(value.impfungenMassnahmeBeratungMPA),
      impfungenMassnahmeInfomaterialAbgegeben: requireValue(value.impfungenMassnahmeInfomaterialAbgegeben),
      impfungenMassnahmeInfoEltern: requireValue(value.impfungenMassnahmeInfoEltern),
      impfungenMassnahmeRueckfrageKinderarzt: requireValue(value.impfungenMassnahmeRueckfrageKinderarzt),
      impfungenMassnahmeBemerkungen: requireValue(value.impfungenMassnahmeBemerkungen),
      checkEinwilligungVorhanden: requireValue(value.checkEinwilligungVorhanden),
      checkKontraindikationenAbgeklaert: requireValue(value.checkKontraindikationenAbgeklaert),
      checkDoppelkontrolleImpfentscheidDurchgefuehrt: requireValue(
        value.checkDoppelkontrolleImpfentscheidDurchgefuehrt,
      ),
      untersuchungImpfungEmpfehlung: requireValue(value.untersuchungImpfungEmpfehlung),
      untersuchungImpfungEmpfehlungValues: value.untersuchungImpfungEmpfehlungValues,
      untersuchungImpfungAchtung: requireValue(value.untersuchungImpfungAchtung),
      untersuchungImpfungBemerkungen: requireValue(value.untersuchungImpfungBemerkungen),
      untersuchungImpfungKrankheitenLinksValues: value.untersuchungImpfungKrankheitenLinksValues,
      untersuchungImpfungKrankheitenRechtsValues: value.untersuchungImpfungKrankheitenRechtsValues,
      impfungenDataMinimalImpfausweisGeprueft: requireValue(value.impfungenDataMinimalImpfausweisGeprueft),
      impfungenDataMinimalImmunsuppression: requireValue(value.impfungenDataMinimalImmunsuppression),
      impfungenDataMinimalMasernImpfstatus: requireValue(value.impfungenDataMinimalMasernImpfstatus),
      impfungenDataMinimalMasernAnzahlImpfungen: requireValue(value.impfungenDataMinimalMasernAnzahlImpfungen),
      impfungenDataMinimalHpvImpfstatus: requireValue(value.impfungenDataMinimalHpvImpfstatus),
      impfungenDataMinimalHpvAnzahlImpfungen: requireValue(value.impfungenDataMinimalHpvAnzahlImpfungen),
      impfungenDataMinimalImpfempfehlung: requireValue(value.impfungenDataMinimalImpfempfehlung),
      impfungenDataMinimalImpfungDurchgefuehrt: requireValue(value.impfungenDataMinimalImpfungDurchgefuehrt),
      sehenBrilleLinsen: requireValue(value.sehenBrilleLinsen),
      sehenBrilleLinsenValues: requireValue(value.sehenBrilleLinsenValues),
      sehenFarbsehen: requireValue(value.sehenFarbsehen),
      sehenTafel: requireValue(value.sehenTafel),
      sehenFernvisus: requireValue(value.sehenFernvisus),
      sehenFernvisusAugeEinzeln: requireValue(value.sehenFernvisusAugeEinzeln),
      sehenStereoTest: requireValue(value.sehenStereoTest),
      sehenStrabismus: requireValue(value.sehenStrabismus),
      sehenStrabismusValues: requireValue(value.sehenStrabismusValues),
      sehenStrabismusAuftreten: requireValue(value.sehenStrabismusAuftreten),
      sehenStrabismusAuspraegung: requireValue(value.sehenStrabismusAuspraegung),
      sehenInspektion: requireValue(value.sehenInspektion),
      sehenKonvergenz: requireValue(value.sehenKonvergenz),
      sehenMotilitaet: requireValue(value.sehenMotilitaet),
      sehenNahvisus: requireValue(value.sehenNahvisus),
      sehenEmpfehlung: requireValue(value.sehenEmpfehlung),
      sehenBemerkungen: requireValue(value.sehenBemerkungen),
      sehenBemerkungenFragebogen: requireValue(value.sehenBemerkungenFragebogen),
      sehenMassnahmen: requireValue(value.sehenMassnahmen),
      hoerenAudiogramm: requireValue(value.hoerenAudiogramm),
      hoerenAudiogramm250Hz: requireValue(value.hoerenAudiogramm250Hz),
      hoerenAudiogramm8000Hz: requireValue(value.hoerenAudiogramm8000Hz),
      hoerenAudiogrammSimpel: requireValue(value.hoerenAudiogrammSimpel),
      hoerenAudiogrammBeurteilung: requireValue(value.hoerenAudiogrammBeurteilung),
      hoerenAudiogrammZusaetzlicheAngaben: requireValue(value.hoerenAudiogrammZusaetzlicheAngaben),
      hoerenAudiogrammEmpfehlung: requireValue(value.hoerenAudiogrammEmpfehlung),
      hoerenAudiogrammBemerkungen: requireValue(value.hoerenAudiogrammBemerkungen),
      hoerenOtoskopie: requireValue(value.hoerenOtoskopie),
      hoerenOtoskopieBemerkungen: requireValue(value.hoerenOtoskopieBemerkungen),
      hoerenHoerhilfe: requireValue(value.hoerenHoerhilfe),
      groesseGewichtGroesse: requireValue(value.groesseGewichtGroesse),
      groesseGewichtGewicht: requireValue(value.groesseGewichtGewicht),
      groesseGewichtBmi: requireValue(value.groesseGewichtBmi),
      groesseGewichtGroessePerzentile: requireValue(value.groesseGewichtGroessePerzentile),
      groesseGewichtGewichtPerzentile: requireValue(value.groesseGewichtGewichtPerzentile),
      groesseGewichtBmiPerzentile: requireValue(value.groesseGewichtBmiPerzentile),
      groesseGewichtBmiPerzentileValues: value.groesseGewichtBmiPerzentileValues,
      groesseGewichtEmpfehlung: requireValue(value.groesseGewichtEmpfehlung),
      groesseGewichtBemerkungen: requireValue(value.groesseGewichtBemerkungen),
      showTriageTriage: requireValue(value.showTriageTriage),
      showTriageKindWeiterleiten: requireValue(value.showTriageKindWeiterleiten),
      laeusekontrolleBeurteilung: requireValue(value.laeusekontrolleBeurteilung),
      laeusekontrolleEmpfehlung: requireValue(value.laeusekontrolleEmpfehlung),
      laeusekontrolleBemerkungen: requireValue(value.laeusekontrolleBemerkungen),
      untersuchungBewegungsapparat: requireValue(value.untersuchungBewegungsapparat),
      untersuchungEinbeinstand: requireValue(value.untersuchungEinbeinstand),
      untersuchungEinbeinstandRechtsLinks: requireValue(value.untersuchungEinbeinstandRechtsLinks),
      untersuchungEinbeinstandValues: value.untersuchungEinbeinstandValues,
      untersuchungEinbeinhuepfen: requireValue(value.untersuchungEinbeinhuepfen),
      untersuchungEinbeinhuepfenRechtsLinks: requireValue(value.untersuchungEinbeinhuepfenRechtsLinks),
      untersuchungEinbeinhuepfenValues: requireValue(value.untersuchungEinbeinhuepfenValues),
      untersuchungFingeropposition: requireValue(value.untersuchungFingeropposition),
      untersuchungHampelmann: requireValue(value.untersuchungHampelmann),
      untersuchungBallFangen: requireValue(value.untersuchungBallFangen),
      untersuchungBallWerfen: requireValue(value.untersuchungBallWerfen),
      untersuchungNameSchreiben: requireValue(value.untersuchungNameSchreiben),
      untersuchungFigurNachmalen: requireValue(value.untersuchungFigurNachmalen),
      untersuchungStifthaltung: requireValue(value.untersuchungStifthaltung),
      untersuchungAussprache: requireValue(value.untersuchungAussprache),
      untersuchungLesefit: requireValue(value.untersuchungLesefit),
      untersuchungLesefitPunkte: requireValue(value.untersuchungLesefitPunkte),
      untersuchungZahlenNachsprechen4: requireValue(value.untersuchungZahlenNachsprechen4),
      untersuchungZahlenNachsprechen5: requireValue(value.untersuchungZahlenNachsprechen5),
      untersuchungZeichnenKreis: requireValue(value.untersuchungZeichnenKreis),
      untersuchungZeichnenViereck: requireValue(value.untersuchungZeichnenViereck),
      untersuchungZeichnenDreieck: requireValue(value.untersuchungZeichnenDreieck),
      untersuchungZeichnenMensch: requireValue(value.untersuchungZeichnenMensch),
      untersuchungZeichnenMenschPunkte: requireValue(value.untersuchungZeichnenMenschPunkte),
      untersuchungMotorik: requireValue(value.untersuchungMotorik),
      untersuchungMotorikValues: value.untersuchungMotorikValues,
      untersuchungMotorikPunkte: requireValue(value.untersuchungMotorikPunkte),
      untersuchungKognition: requireValue(value.untersuchungKognition),
      untersuchungKognitionPunkte: requireValue(value.untersuchungKognitionPunkte),
      untersuchungSprachfaehigkeitSingleValue: requireValue(value.untersuchungSprachfaehigkeitSingleValue),
      untersuchungSprachfaehigkeitMultiValue: requireValue(value.untersuchungSprachfaehigkeitMultiValue),
      untersuchungSprachfaehigkeitValues: requireValue(value.untersuchungSprachfaehigkeitValues),
      untersuchungSprachePunkte: requireValue(value.untersuchungSprachfaehigkeitPunkte),
      untersuchungMundZaehne: requireValue(value.untersuchungMundZaehne),
      untersuchungHaut: requireValue(value.untersuchungHaut),
      untersuchungHerz: requireValue(value.untersuchungHerz),
      untersuchungBlutdruck: requireValue(value.untersuchungBlutdruck),
      untersuchungVerhalten: requireValue(value.untersuchungVerhalten),
      unterlagenEinverstaendnis: requireValue(value.unterlagenEinverstaendnis),
      unterlagenVerhaltensfragebogen: requireValue(value.unterlagenVerhaltensfragebogen),
      untersuchungEmpfehlung: requireValue(value.untersuchungEmpfehlung),
      untersuchungBemerkungen: requireValue(value.untersuchungBemerkungen),
      untersuchungSpracheNachsprechen: requireValue(value.untersuchungSpracheNachsprechen),
      untersuchungSpracheWortschatz: requireValue(value.untersuchungSpracheWortschatz),
      untersuchungSpracheSprachverstaendnis: requireValue(value.untersuchungSpracheSprachverstaendnis),
      untersuchungSpracheBemerkungen: requireValue(value.untersuchungSpracheBemerkungen),
      untersuchungBeeintraechtigungFreiesGehen: requireValue(value.untersuchungBeeintraechtigungFreiesGehen),
      untersuchungBeeintraechtigungOhneStuetzradVelofahren: requireValue(
        value.untersuchungBeeintraechtigungOhneStuetzradVelofahren,
      ),
      untersuchungBeeintraechtigungGrafomotorik: requireValue(value.untersuchungBeeintraechtigungGrafomotorik),
      untersuchungBeeintraechtigungFeinmotorik: requireValue(value.untersuchungBeeintraechtigungFeinmotorik),
      untersuchungBeeintraechtigungGrobmotorik: requireValue(value.untersuchungBeeintraechtigungGrobmotorik),
      untersuchungBeeintraechtigungGleichgewicht: requireValue(value.untersuchungBeeintraechtigungGleichgewicht),
      untersuchungBeeintraechtigungBemerkung: requireValue(value.untersuchungBeeintraechtigungBemerkung),
      bewegungsapparatSkoliose: requireValue(value.bewegungsapparatSkoliose),
      bewegungsapparatHaltungsschwaeche: requireValue(value.bewegungsapparatHaltungsschwaeche),
      bewegungsapparatVerkuerzungHamstringmuskulatur: requireValue(
        value.bewegungsapparatVerkuerzungHamstringmuskulatur,
      ),
      bewegungsapparatBemerkung: requireValue(value.bewegungsapparatBemerkung),
      abschlussUeberweisungErfolgt: requireValue(value.abschlussUeberweisungErfolgt),
      abschlussAerztlicheValidierungErforderlich: requireValue(value.abschlussAerztlicheValidierungErforderlich),
      abschlussBeratungDurchgefuehrt: requireValue(value.abschlussBeratungDurchgefuehrt),
      abschlussBeurteilungEmpfohleneMassnahmen: requireValue(value.abschlussBeurteilungEmpfohleneMassnahmen),
      showCardAbschlussSignatur: requireValue(value.showCardAbschlussSignatur),
      showMassnahmenPathologischeBefunde: requireValue(value.showMassnahmenPathologischeBefunde),
      showMassnahmenKeine: requireValue(value.showMassnahmenKeine),
      showMassnahmenBeratungMpa: requireValue(value.showMassnahmenBeratungMpa),
      showMassnahmenBeratungSchulAerztin: requireValue(value.showMassnahmenBeratungSchulAerztin),
      showMassnahmenBereitsBehandelt: requireValue(value.showMassnahmenBereitsBehandelt),
      showMassnahmenInfomaterial: requireValue(value.showMassnahmenInfomaterial),
      showMassnahmenInfoEltern: requireValue(value.showMassnahmenInfoEltern),
      showMassnahmenUeberweisungHausarzt: requireValue(value.showMassnahmenUeberweisungHausarzt),
      showMassnahmenUeberweisungAugenarzt: requireValue(value.showMassnahmenUeberweisungAugenarzt),
      showMassnahmenNachkontrolleSaed: requireValue(value.showMassnahmenNachkontrolleSaed),
      showMassnahmenAnderes: requireValue(value.showMassnahmenAnderes),
    });
  }

  disableFields(selectedIsNew: boolean): void {
    this.group.controls.datumBis.disable();
    if (selectedIsNew) {
      this.group.controls.datumBis.setValue(globalConstants.endOfTime);
    } else {
      this.group.controls.anlassTyp.disable();
      this.group.controls.datumVon.disable();
    }
  }
}
