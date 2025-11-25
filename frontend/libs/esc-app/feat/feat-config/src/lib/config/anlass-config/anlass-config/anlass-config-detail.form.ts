import { DestroyRef } from '@angular/core';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { NonNullableFormBuilder, Validators } from '@angular/forms';
import { AnlassConfigSaveJson, AnlassTyp, PerzentileBmi, RequirementSetting } from '@dvb/data-access-backend';
import { ISODateString } from '@dvb/data-access-types';
import { AppValidators, BaseForm } from '@dvb/util-angular';
import {ensureProps, ensurePropsFlat, fromISODateString, isNotNullish, requireValue, today} from '@dvb/util-shared';
import { distinctUntilChanged } from 'rxjs';
import { globalConstants } from '@dvb/feat-shared';
import { AnlassConfigStore } from '../anlass-config.store';

type Config = {
  fb: NonNullableFormBuilder;
  destroyRef: DestroyRef;
  anlassConfigStore: AnlassConfigStore;
};

function makeForm(config: Config) {
  const fb = config.fb;

  const group = fb.group({
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
    checkEinwilligungVorhanden: fb.control<RequirementSetting>(RequirementSetting.NOT_VISIBLE, Validators.required),
    checkKontraindikationenAbgeklaert: fb.control<RequirementSetting>(
      RequirementSetting.NOT_VISIBLE,
      Validators.required,
    ),
    checkDoppelkontrolleImpfentscheidDurchgefuehrt: fb.control<RequirementSetting>(
      RequirementSetting.NOT_VISIBLE,
      Validators.required,
    ),
    untersuchungImpfungEmpfehlung: fb.control<RequirementSetting>(RequirementSetting.NOT_VISIBLE, Validators.required),
    untersuchungImpfungAchtung: fb.control<RequirementSetting>(RequirementSetting.NOT_VISIBLE, Validators.required),
    untersuchungImpfungBemerkungen: fb.control<RequirementSetting>(RequirementSetting.NOT_VISIBLE, Validators.required),
    sehenBrilleLinsen: fb.control<RequirementSetting>(RequirementSetting.NOT_VISIBLE, Validators.required),
    sehenFarbsehen: fb.control<RequirementSetting>(RequirementSetting.NOT_VISIBLE, Validators.required),
    sehenTafel: fb.control<RequirementSetting>(RequirementSetting.NOT_VISIBLE, Validators.required),
    sehenFernvisus: fb.control<RequirementSetting>(RequirementSetting.NOT_VISIBLE, Validators.required),
    sehenStereoTest: fb.control<RequirementSetting>(RequirementSetting.NOT_VISIBLE, Validators.required),
    sehenStrabismus: fb.control<RequirementSetting>(RequirementSetting.NOT_VISIBLE, Validators.required),
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
    hoerenAudiogrammBeurteilung: fb.control<RequirementSetting>(RequirementSetting.NOT_VISIBLE, Validators.required),
    hoerenAudiogrammZusaetzlicheAngaben: fb.control<RequirementSetting>(
      RequirementSetting.NOT_VISIBLE,
      Validators.required,
    ),
    hoerenAudiogrammEmpfehlung: fb.control<RequirementSetting>(RequirementSetting.NOT_VISIBLE, Validators.required),
    hoerenAudiogrammBemerkungen: fb.control<RequirementSetting>(RequirementSetting.NOT_VISIBLE, Validators.required),
    hoerenOtoskopie: fb.control<RequirementSetting>(RequirementSetting.NOT_VISIBLE, Validators.required),
    hoerenOtoskopieBemerkungen: fb.control<RequirementSetting>(RequirementSetting.NOT_VISIBLE, Validators.required),
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
    untersuchungEinbeinstandRechts: fb.control<RequirementSetting>(RequirementSetting.NOT_VISIBLE, Validators.required),
    untersuchungEinbeinstandLinks: fb.control<RequirementSetting>(RequirementSetting.NOT_VISIBLE, Validators.required),
    untersuchungEinbeinhuepfen: fb.control<RequirementSetting>(RequirementSetting.NOT_VISIBLE, Validators.required),
    untersuchungEinbeinhuepfenRechts: fb.control<RequirementSetting>(RequirementSetting.NOT_VISIBLE, Validators.required),
    untersuchungEinbeinhuepfenLinks: fb.control<RequirementSetting>(RequirementSetting.NOT_VISIBLE, Validators.required),
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
    untersuchungZahlenNachsprechen4: fb.control<RequirementSetting>(RequirementSetting.NOT_VISIBLE, Validators.required),
    untersuchungZahlenNachsprechen5: fb.control<RequirementSetting>(RequirementSetting.NOT_VISIBLE, Validators.required),
    untersuchungZeichnenKreis: fb.control<RequirementSetting>(RequirementSetting.NOT_VISIBLE, Validators.required),
    untersuchungZeichnenViereck: fb.control<RequirementSetting>(RequirementSetting.NOT_VISIBLE, Validators.required),
    untersuchungZeichnenDreieck: fb.control<RequirementSetting>(RequirementSetting.NOT_VISIBLE, Validators.required),
    untersuchungZeichnenMensch: fb.control<RequirementSetting>(RequirementSetting.NOT_VISIBLE, Validators.required),
    untersuchungZeichnenMenschPunkte: fb.control<RequirementSetting>(RequirementSetting.NOT_VISIBLE, Validators.required),
    untersuchungMotorik: fb.control<RequirementSetting>(RequirementSetting.NOT_VISIBLE, Validators.required),
    untersuchungMotorikPunkte: fb.control<RequirementSetting>(RequirementSetting.NOT_VISIBLE, Validators.required),
    untersuchungKognition: fb.control<RequirementSetting>(RequirementSetting.NOT_VISIBLE, Validators.required),
    untersuchungKognitionPunkte: fb.control<RequirementSetting>(RequirementSetting.NOT_VISIBLE, Validators.required),
    untersuchungSprache: fb.control<RequirementSetting>(RequirementSetting.NOT_VISIBLE, Validators.required),
    untersuchungSprachePunkte: fb.control<RequirementSetting>(RequirementSetting.NOT_VISIBLE, Validators.required),
    untersuchungMundZaehne: fb.control<RequirementSetting>(RequirementSetting.NOT_VISIBLE, Validators.required),
    untersuchungHaut: fb.control<RequirementSetting>(RequirementSetting.NOT_VISIBLE, Validators.required),
    untersuchungHerz: fb.control<RequirementSetting>(RequirementSetting.NOT_VISIBLE, Validators.required),
    untersuchungBlutdruck: fb.control<RequirementSetting>(RequirementSetting.NOT_VISIBLE, Validators.required),
    untersuchungVerhalten: fb.control<RequirementSetting>(RequirementSetting.NOT_VISIBLE, Validators.required),
    unterlagenEinverstaendnis: fb.control<RequirementSetting>(RequirementSetting.NOT_VISIBLE, Validators.required),
    unterlagenVerhaltensfragebogen: fb.control<RequirementSetting>(RequirementSetting.NOT_VISIBLE, Validators.required),
    untersuchungEmpfehlung: fb.control<RequirementSetting>(RequirementSetting.NOT_VISIBLE, Validators.required),
    untersuchungBemerkungen: fb.control<RequirementSetting>(RequirementSetting.NOT_VISIBLE, Validators.required),
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
  });

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

export class AnlassConfigForm extends BaseForm<ReturnType<typeof makeForm>, AnlassConfigSaveJson> {
  constructor(config: Config) {
    super(() => makeForm(config));
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
      checkEinwilligungVorhanden: requireValue(value.checkEinwilligungVorhanden),
      checkKontraindikationenAbgeklaert: requireValue(value.checkKontraindikationenAbgeklaert),
      checkDoppelkontrolleImpfentscheidDurchgefuehrt: requireValue(
        value.checkDoppelkontrolleImpfentscheidDurchgefuehrt,
      ),
      untersuchungImpfungEmpfehlung: requireValue(value.untersuchungImpfungEmpfehlung),
      untersuchungImpfungAchtung: requireValue(value.untersuchungImpfungAchtung),
      untersuchungImpfungBemerkungen: requireValue(value.untersuchungImpfungBemerkungen),
      sehenBrilleLinsen: requireValue(value.sehenBrilleLinsen),
      sehenFarbsehen: requireValue(value.sehenFarbsehen),
      sehenTafel: requireValue(value.sehenTafel),
      sehenFernvisus: requireValue(value.sehenFernvisus),
      sehenStereoTest: requireValue(value.sehenStereoTest),
      sehenStrabismus: requireValue(value.sehenStrabismus),
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
      hoerenAudiogrammBeurteilung: requireValue(value.hoerenAudiogrammBeurteilung),
      hoerenAudiogrammZusaetzlicheAngaben: requireValue(value.hoerenAudiogrammZusaetzlicheAngaben),
      hoerenAudiogrammEmpfehlung: requireValue(value.hoerenAudiogrammEmpfehlung),
      hoerenAudiogrammBemerkungen: requireValue(value.hoerenAudiogrammBemerkungen),
      hoerenOtoskopie: requireValue(value.hoerenOtoskopie),
      hoerenOtoskopieBemerkungen: requireValue(value.hoerenOtoskopieBemerkungen),
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
      untersuchungEinbeinstandRechts: requireValue(value.untersuchungEinbeinstandRechts),
      untersuchungEinbeinstandLinks: requireValue(value.untersuchungEinbeinstandLinks),
      untersuchungEinbeinhuepfen: requireValue(value.untersuchungEinbeinhuepfen),
      untersuchungEinbeinhuepfenRechts: requireValue(value.untersuchungEinbeinhuepfenRechts),
      untersuchungEinbeinhuepfenLinks: requireValue(value.untersuchungEinbeinhuepfenLinks),
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
      untersuchungMotorikPunkte: requireValue(value.untersuchungMotorikPunkte),
      untersuchungKognition: requireValue(value.untersuchungKognition),
      untersuchungKognitionPunkte: requireValue(value.untersuchungKognitionPunkte),
      untersuchungSprache: requireValue(value.untersuchungSprache),
      untersuchungSprachePunkte: requireValue(value.untersuchungSprachePunkte),
      untersuchungMundZaehne: requireValue(value.untersuchungMundZaehne),
      untersuchungHaut: requireValue(value.untersuchungHaut),
      untersuchungHerz: requireValue(value.untersuchungHerz),
      untersuchungBlutdruck: requireValue(value.untersuchungBlutdruck),
      untersuchungVerhalten: requireValue(value.untersuchungVerhalten),
      unterlagenEinverstaendnis: requireValue(value.unterlagenEinverstaendnis),
      unterlagenVerhaltensfragebogen: requireValue(value.unterlagenVerhaltensfragebogen),
      untersuchungEmpfehlung: requireValue(value.untersuchungEmpfehlung),
      untersuchungBemerkungen: requireValue(value.untersuchungBemerkungen),
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
