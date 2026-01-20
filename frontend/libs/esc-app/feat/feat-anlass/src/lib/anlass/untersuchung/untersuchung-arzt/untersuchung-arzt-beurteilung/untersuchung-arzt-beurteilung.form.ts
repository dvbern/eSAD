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

import {DestroyRef} from '@angular/core';
import {takeUntilDestroyed} from '@angular/core/rxjs-interop';
import {NonNullableFormBuilder, Validators} from '@angular/forms';
import {
  AnlassConfigJson,
  Aussprache,
  BallFangen,
  BallWerfen,
  Bewegungsapparat,
  Blutdruck,
  Einbeinhuepfen,
  Einbeinstand,
  Einverstaendniserklaerung,
  FigurNachmalen,
  Fingeropposition,
  Hampelmann,
  Haut,
  Herzauskultation,
  Kognition,
  Lesefit,
  Motorik,
  MundAndZaehne,
  NameSchreiben,
  RequirementSetting,
  Sprachfaehigkeit,
  Stifthaltung,
  UntersuchungArztBeurteilungSaveJson,
  Verhalten,
  Verhaltensfragebogen,
  ZahlenNachsprechen,
  Zeichnen,
  ZeichnenMensch,
} from '@dvb/data-access-backend';
import {globalConstants} from '@dvb/feat-shared';
import {AppValidators, BaseForm} from '@dvb/util-angular';
import {ensureProps, Nullish, requireValue, trimToUndefined} from '@dvb/util-shared';
import {distinctUntilChanged} from 'rxjs';
import {AnlassStore} from '../../../anlass.store';
import {checkIfArrayContainsCheckedValue, markAllMultiCheckboxesAsTouched} from '../../untersuchung.util';

type Config = {
  fb: NonNullableFormBuilder,
  destroyRef: DestroyRef,
  anlassStore?: AnlassStore,
}

export type EinverstaendniserklaerungSubFormType = ReturnType<typeof makeEinverstaendniserklaerungSubForm>
export type SprachfaehigkeitMultiValueSubFormType = ReturnType<typeof makeSprachfaehigkeitMultiValueSubForm>
export type BewegungsapparatSubFormType = ReturnType<typeof makeBewegungsapparatSubForm>
export type FingeroppositionSubFormType = ReturnType<typeof makeFingeroppositionSubForm>
export type MundAndZaehneSubFormType = ReturnType<typeof makeMundAndZaehneSubForm>

function makeForm(config: Config) {
  const fb = config.fb;
  const group = fb.group({
    einverstaendniserklaerungArray: fb.array<EinverstaendniserklaerungSubFormType>([]),
    verhalten: fb.control<Verhalten | undefined>(undefined),
    verhaltensfragebogen: fb.control<Verhaltensfragebogen | undefined>(undefined),
    blutdruck: fb.control<Blutdruck | undefined>(undefined),
    blutdruckSystolisch: fb.control<number | undefined>(undefined),
    blutdruckDiastolisch: fb.control<number | undefined>(undefined),
    herzauskultation: fb.control<Herzauskultation | undefined>(undefined),
    herzauskultationBemerkung: fb.control<string>('', AppValidators.textareaMaxLength2000()),
    haut: fb.control<Haut | undefined>(undefined),
    hautBemerkung: fb.control<string>('', AppValidators.textareaMaxLength2000()),
    mundAndZaehneArray: fb.array<MundAndZaehneSubFormType>([]),
    mundAndZaehneBemerkung: fb.control<string>('', AppValidators.textareaMaxLength2000()),
    sprachfaehigkeitSingleValue: fb.control<Sprachfaehigkeit | undefined>(undefined),
    sprachfaehigkeitMultiValueArray: fb.array<SprachfaehigkeitMultiValueSubFormType>([]),
    sprachfaehigkeitBemerkung: fb.control<string>('', AppValidators.textareaMaxLength2000()),
    sprachfaehigkeitPunkte: fb.control<number | undefined>(undefined,
      [AppValidators.nonNegativNumber(), AppValidators.oneDecimalPlace()]),
    motorik: fb.control<Motorik | undefined>(undefined),
    motorikPunkte: fb.control<number | undefined>(undefined,
      [AppValidators.nonNegativNumber(), AppValidators.oneDecimalPlace()]),
    kognition: fb.control<Kognition | undefined>(undefined),
    kognitionPunkte: fb.control<number | undefined>(undefined,
      [AppValidators.nonNegativNumber(), AppValidators.oneDecimalPlace()]),
    bewegungsapparatArray: fb.array<BewegungsapparatSubFormType>([]),
    bewegungsapparatBemerkung: fb.control<string>('', AppValidators.textareaMaxLength2000()),
    einbeinstand: fb.control<Einbeinstand | undefined>(undefined),
    einbeinstandRechts: fb.control<Einbeinstand | undefined>(undefined),
    einbeinstandLinks: fb.control<Einbeinstand | undefined>(undefined),
    einbeinstandBemerkung: fb.control<string>('', AppValidators.textareaMaxLength2000()),
    einbeinhuepfen: fb.control<Einbeinhuepfen | undefined>(undefined),
    einbeinhuepfenRechts: fb.control<Einbeinhuepfen | undefined>(undefined),
    einbeinhuepfenLinks: fb.control<Einbeinhuepfen | undefined>(undefined),
    einbeinhuepfenBemerkung: fb.control<string>('', AppValidators.textareaMaxLength2000()),
    fingeroppositionArray: fb.array<FingeroppositionSubFormType>([]),
    fingeroppositionBemerkung: fb.control<string>('', AppValidators.textareaMaxLength2000()),
    hampelmann: fb.control<Hampelmann | undefined>(undefined),
    ballFangen: fb.control<BallFangen | undefined>(undefined),
    ballWerfen: fb.control<BallWerfen | undefined>(undefined),
    nameSchreiben: fb.control<NameSchreiben | undefined>(undefined),
    figurNachmalen: fb.control<FigurNachmalen | undefined>(undefined),
    stifthaltung: fb.control<Stifthaltung | undefined>(undefined),
    aussprache: fb.control<Aussprache | undefined>(undefined),
    lesefit: fb.control<Lesefit | undefined>(undefined),
    lesefitPunkte: fb.control<number | undefined>(undefined,
      [AppValidators.nonNegativNumber(), AppValidators.oneDecimalPlace()]),
    zahlenNachsprechen4: fb.control<ZahlenNachsprechen | undefined>(undefined),
    zahlenNachsprechen5: fb.control<ZahlenNachsprechen | undefined>(undefined),
    zeichnenKreis: fb.control<Zeichnen | undefined>(undefined),
    zeichnenViereck: fb.control<Zeichnen | undefined>(undefined),
    zeichnenDreieck: fb.control<Zeichnen | undefined>(undefined),
    zeichnenMensch: fb.control<ZeichnenMensch | undefined>(undefined),
    zeichnenMenschPunkte: fb.control<number | undefined>(undefined,
      [AppValidators.nonNegativNumber(), AppValidators.oneDecimalPlace()]),
    version: fb.control<number | Nullish>({value: undefined, disabled: true}),
  });

  // Eine Eingabe aendert den required-Status eines anderen Feldes
  group.controls.blutdruck.valueChanges.pipe(distinctUntilChanged(), takeUntilDestroyed(config.destroyRef))
    .subscribe({
      next: _ => {
        group.controls.blutdruckSystolisch.updateValueAndValidity();
        group.controls.blutdruckDiastolisch.updateValueAndValidity();
      },
      error: console.error,
    });

  // Multi-Checkboxen: Wenn eines touched ist aber keines ausgewählt, müssen alle rot werden
  markAllMultiCheckboxesAsTouched(group.controls.einverstaendniserklaerungArray, config.destroyRef);
  markAllMultiCheckboxesAsTouched(group.controls.mundAndZaehneArray, config.destroyRef);
  markAllMultiCheckboxesAsTouched(group.controls.sprachfaehigkeitMultiValueArray, config.destroyRef);
  markAllMultiCheckboxesAsTouched(group.controls.bewegungsapparatArray, config.destroyRef);
  markAllMultiCheckboxesAsTouched(group.controls.fingeroppositionArray, config.destroyRef);

  return group;
}

function makeEinverstaendniserklaerungSubForm(fb: NonNullableFormBuilder, value: Einverstaendniserklaerung) {
  return fb.group({
    resultat: fb.control<Einverstaendniserklaerung>({value, disabled: true}),
    isChecked: fb.control<boolean>(false),
  })
}

function makeSprachfaehigkeitMultiValueSubForm(fb: NonNullableFormBuilder, value: Sprachfaehigkeit) {
  return fb.group({
    resultat: fb.control<Sprachfaehigkeit>({value, disabled: true}),
    isChecked: fb.control<boolean>(false),
  })
}

function makeBewegungsapparatSubForm(fb: NonNullableFormBuilder, value: Bewegungsapparat) {
  return fb.group({
    resultat: fb.control<Bewegungsapparat>({value, disabled: true}),
    isChecked: fb.control<boolean>(false),
  })
}

function makeFingeroppositionSubForm(fb: NonNullableFormBuilder, value: Fingeropposition) {
  return fb.group({
    resultat: fb.control<Fingeropposition>({value, disabled: true}),
    isChecked: fb.control<boolean>(false),
  })
}

function makeMundAndZaehneSubForm(fb: NonNullableFormBuilder, value: MundAndZaehne) {
  return fb.group({
    resultat: fb.control<MundAndZaehne>({value, disabled: true}),
    isChecked: fb.control<boolean>(false),
  })
}

export class UntersuchungArztBeurteilungForm
  extends BaseForm<ReturnType<typeof makeForm>, UntersuchungArztBeurteilungSaveJson> {

  constructor(protected config: Config) {
    super(() => makeForm(config));
  }

  resetForm(value: UntersuchungArztBeurteilungSaveJson | Nullish): void {
    this.group.reset({
      ...value,
    });
    this.resetEinverstaendniserklaerung(value);
    this.resetSprachfaehigkeit(value);
    this.resetBewegungsapparat(value);
    this.resetFingeropposition(value);
    this.resetMundAndZaehne(value);
  }

  private resetEinverstaendniserklaerung(value: UntersuchungArztBeurteilungSaveJson | Nullish): void {
    this.group.controls.einverstaendniserklaerungArray.clear();
    for (const resultat of Object.values(Einverstaendniserklaerung)) {
      const subForm = makeEinverstaendniserklaerungSubForm(this.config.fb, resultat);
      subForm.controls.isChecked.reset(!!value?.einverstaendniserklaerung?.find(fv => fv === resultat));
      this.group.controls.einverstaendniserklaerungArray.push(subForm)
    }
  }

  private resetSprachfaehigkeit(value: UntersuchungArztBeurteilungSaveJson | Nullish): void {
    this.group.controls.sprachfaehigkeitMultiValueArray.clear();
    const valuesSupported = this.config.anlassStore?.untersuchungSprachfaehigkeitValues();
    if (valuesSupported) {
      for (const resultat of valuesSupported) {
        const subForm = makeSprachfaehigkeitMultiValueSubForm(this.config.fb, resultat);
        subForm.controls.isChecked.reset(!!value?.sprachfaehigkeitMultiValue?.find(fv => fv === resultat));
        this.group.controls.sprachfaehigkeitMultiValueArray.push(subForm)
      }
    }
  }

  private resetBewegungsapparat(value: UntersuchungArztBeurteilungSaveJson | Nullish): void {
    this.group.controls.bewegungsapparatArray.clear();
    for (const resultat of Object.values(Bewegungsapparat)) {
      const subForm = makeBewegungsapparatSubForm(this.config.fb, resultat);
      subForm.controls.isChecked.reset(!!value?.bewegungsapparat?.find(fv => fv === resultat));
      this.group.controls.bewegungsapparatArray.push(subForm)
    }
  }

  private resetFingeropposition(value: UntersuchungArztBeurteilungSaveJson | Nullish): void {
    this.group.controls.fingeroppositionArray.clear();
    for (const resultat of Object.values(Fingeropposition)) {
      const subForm = makeFingeroppositionSubForm(this.config.fb, resultat);
      subForm.controls.isChecked.reset(!!value?.fingeropposition?.find(fv => fv === resultat));
      this.group.controls.fingeroppositionArray.push(subForm)
    }
  }

  private resetMundAndZaehne(value: UntersuchungArztBeurteilungSaveJson | Nullish): void {
    this.group.controls.mundAndZaehneArray.clear();
    for (const resultat of Object.values(MundAndZaehne)) {
      const subForm = makeMundAndZaehneSubForm(this.config.fb, resultat);
      subForm.controls.isChecked.reset(!!value?.mundAndZaehne?.find(fv => fv === resultat));
      this.group.controls.mundAndZaehneArray.push(subForm)
    }
  }

  public override valueOnValid(): UntersuchungArztBeurteilungSaveJson {
    const value = requireValue(this.group.getRawValue());
    return ensureProps<UntersuchungArztBeurteilungSaveJson>({
      einverstaendniserklaerung: value.einverstaendniserklaerungArray.filter(i => i.isChecked).map(i => i.resultat),
      verhaltensfragebogen: value.verhaltensfragebogen,
      verhalten: value.verhalten,
      blutdruck: value.blutdruck,
      blutdruckSystolisch: value.blutdruckSystolisch,
      blutdruckDiastolisch: value.blutdruckDiastolisch,
      herzauskultation: value.herzauskultation,
      herzauskultationBemerkung: trimToUndefined(value.herzauskultationBemerkung),
      haut: value.haut,
      hautBemerkung: trimToUndefined(value.hautBemerkung),
      mundAndZaehne: value.mundAndZaehneArray.filter(i => i.isChecked).map(i => i.resultat),
      mundAndZaehneBemerkung: trimToUndefined(value.mundAndZaehneBemerkung),
      sprachfaehigkeitSingleValue: value.sprachfaehigkeitSingleValue,
      sprachfaehigkeitMultiValue: value.sprachfaehigkeitMultiValueArray.filter(i => i.isChecked).map(i => i.resultat),
      sprachfaehigkeitBemerkung: trimToUndefined(value.sprachfaehigkeitBemerkung),
      sprachfaehigkeitPunkte: value.sprachfaehigkeitPunkte,
      motorik: value.motorik,
      motorikPunkte: value.motorikPunkte,
      kognition: value.kognition,
      kognitionPunkte: value.kognitionPunkte,
      bewegungsapparat: value.bewegungsapparatArray.filter(i => i.isChecked).map(i => i.resultat),
      bewegungsapparatBemerkung: trimToUndefined(value.bewegungsapparatBemerkung),
      einbeinstand: value.einbeinstand,
      einbeinstandRechts: value.einbeinstandRechts,
      einbeinstandLinks: value.einbeinstandLinks,
      einbeinstandBemerkung: trimToUndefined(value.einbeinstandBemerkung),
      einbeinhuepfen: value.einbeinhuepfen,
      einbeinhuepfenRechts: value.einbeinhuepfenRechts,
      einbeinhuepfenLinks: value.einbeinhuepfenLinks,
      einbeinhuepfenBemerkung: trimToUndefined(value.einbeinhuepfenBemerkung),
      fingeropposition: value.fingeroppositionArray.filter(i => i.isChecked).map(i => i.resultat),
      fingeroppositionBemerkung: trimToUndefined(value.fingeroppositionBemerkung),
      hampelmann: value.hampelmann,
      ballFangen: value.ballFangen,
      ballWerfen: value.ballWerfen,
      nameSchreiben: value.nameSchreiben,
      figurNachmalen: value.figurNachmalen,
      stifthaltung: value.stifthaltung,
      aussprache: value.aussprache,
      lesefit: value.lesefit,
      lesefitPunkte: value.lesefitPunkte,
      zahlenNachsprechen4: value.zahlenNachsprechen4,
      zahlenNachsprechen5: value.zahlenNachsprechen5,
      zeichnenKreis: value.zeichnenKreis,
      zeichnenViereck: value.zeichnenViereck,
      zeichnenDreieck: value.zeichnenDreieck,
      zeichnenMensch: value.zeichnenMensch,
      zeichnenMenschPunkte: value.zeichnenMenschPunkte,
      version: value.version ?? globalConstants.entityPrePersistVersion,
    });
  }


  public setValidatorsForAnlassConfig(anlassConfig: AnlassConfigJson): void {
    if (anlassConfig.data.unterlagenEinverstaendnis === RequirementSetting.REQUIRED) {
      this.group.controls.einverstaendniserklaerungArray.addValidators(AppValidators.atLeastOneCheckedValidator());
    }
    if (anlassConfig.data.unterlagenVerhaltensfragebogen === RequirementSetting.REQUIRED) {
      this.group.controls.verhaltensfragebogen.addValidators(Validators.required);
    }
    if (anlassConfig.data.untersuchungBewegungsapparat === RequirementSetting.REQUIRED) {
      this.group.controls.bewegungsapparatArray.addValidators(AppValidators.atLeastOneCheckedValidator());
      this.group.controls.bewegungsapparatBemerkung.addValidators(AppValidators.requiredIfValidator(() => {
        return this.isBewegungsapparatAnderesChecked();
      }));
    }
    if (anlassConfig.data.untersuchungEinbeinstand === RequirementSetting.REQUIRED) {
      this.group.controls.einbeinstand.addValidators(Validators.required);
      this.group.controls.einbeinstandBemerkung.addValidators(AppValidators.requiredIfValidator(() => {
        return this.isEinbeinstandAnderesChecked();
      }));
    }
    if (anlassConfig.data.untersuchungEinbeinstandRechtsLinks === RequirementSetting.REQUIRED) {
      this.group.controls.einbeinstandRechts.addValidators(Validators.required);
      this.group.controls.einbeinstandLinks.addValidators(Validators.required);
    }
    if (anlassConfig.data.untersuchungEinbeinhuepfen === RequirementSetting.REQUIRED) {
      this.group.controls.einbeinhuepfen.addValidators(Validators.required);
      this.group.controls.einbeinhuepfenBemerkung.addValidators(AppValidators.requiredIfValidator(() => {
        return this.isEinbeinhuepfenAnderesChecked();
      }));
    }
    if (anlassConfig.data.untersuchungEinbeinhuepfenRechtsLinks === RequirementSetting.REQUIRED) {
      this.group.controls.einbeinhuepfenRechts.addValidators(Validators.required);
      this.group.controls.einbeinhuepfenLinks.addValidators(Validators.required);
    }
    if (anlassConfig.data.untersuchungFingeropposition === RequirementSetting.REQUIRED) {
      this.group.controls.fingeroppositionArray.addValidators(AppValidators.atLeastOneCheckedValidator());
      this.group.controls.fingeroppositionBemerkung.addValidators(AppValidators.requiredIfValidator(() => {
        return this.isFingeroppositionAnderesChecked();
      }));
    }
    if (anlassConfig.data.untersuchungMotorik === RequirementSetting.REQUIRED) {
      this.group.controls.motorik.addValidators(Validators.required);
    }
    if (anlassConfig.data.untersuchungMotorikPunkte === RequirementSetting.REQUIRED) {
      this.group.controls.motorikPunkte.addValidators(Validators.required);
    }
    if (anlassConfig.data.untersuchungKognition === RequirementSetting.REQUIRED) {
      this.group.controls.kognition.addValidators(Validators.required);
    }
    if (anlassConfig.data.untersuchungKognitionPunkte === RequirementSetting.REQUIRED) {
      this.group.controls.kognitionPunkte.addValidators(Validators.required);
    }
    if (anlassConfig.data.untersuchungSprachfaehigkeitSingleValue === RequirementSetting.REQUIRED) {
      this.group.controls.sprachfaehigkeitSingleValue.addValidators(Validators.required);
    }
    if (anlassConfig.data.untersuchungSprachfaehigkeitMultiValue === RequirementSetting.REQUIRED) {
      this.group.controls.sprachfaehigkeitMultiValueArray.addValidators(AppValidators.atLeastOneCheckedValidator());
      this.group.controls.sprachfaehigkeitBemerkung.addValidators(AppValidators.requiredIfValidator(() => {
        return this.isSprachfaehigkeitMultiValueAnderesOrArtikulationsfehlerChecked();
      }));
    }
    if (anlassConfig.data.untersuchungSprachePunkte === RequirementSetting.REQUIRED) {
      this.group.controls.sprachfaehigkeitPunkte.addValidators(Validators.required);
    }
    if (anlassConfig.data.untersuchungMundZaehne === RequirementSetting.REQUIRED) {
      this.group.controls.mundAndZaehneArray.addValidators(AppValidators.atLeastOneCheckedValidator());
      this.group.controls.mundAndZaehneBemerkung.addValidators(AppValidators.requiredIfValidator(() => {
        return this.isMundAndZaehneAnderesChecked();
      }));
    }
    if (anlassConfig.data.untersuchungHaut === RequirementSetting.REQUIRED) {
      this.group.controls.haut.addValidators(Validators.required);
      this.group.controls.hautBemerkung.addValidators(AppValidators.requiredIfValidator(() => {
        return this.isHautAnderesChecked();
      }));
    }
    if (anlassConfig.data.untersuchungHerz === RequirementSetting.REQUIRED) {
      this.group.controls.herzauskultation.addValidators(Validators.required);
      this.group.controls.herzauskultationBemerkung.addValidators(AppValidators.requiredIfValidator(() => {
        return this.isHerzauskultationAnderesChecked();
      }));
    }
    if (anlassConfig.data.untersuchungBlutdruck === RequirementSetting.REQUIRED) {
      this.group.controls.blutdruck.addValidators(Validators.required);
      this.group.controls.blutdruckSystolisch.addValidators(AppValidators.requiredIfValidator(() => {
        return this.group.controls.blutdruck.value !== Blutdruck.NICHT_GEPRUEFT;
      }));
      this.group.controls.blutdruckDiastolisch.addValidators(AppValidators.requiredIfValidator(() => {
        return this.group.controls.blutdruck.value !== Blutdruck.NICHT_GEPRUEFT;
      }));
    }
    if (anlassConfig.data.untersuchungVerhalten === RequirementSetting.REQUIRED) {
      this.group.controls.verhalten.addValidators(Validators.required);
    }
    if (anlassConfig.data.untersuchungHampelmann === RequirementSetting.REQUIRED) {
      this.group.controls.hampelmann.addValidators(Validators.required);
    }
    if (anlassConfig.data.untersuchungBallFangen === RequirementSetting.REQUIRED) {
      this.group.controls.ballFangen.addValidators(Validators.required);
    }
    if (anlassConfig.data.untersuchungBallWerfen === RequirementSetting.REQUIRED) {
      this.group.controls.ballWerfen.addValidators(Validators.required);
    }
    if (anlassConfig.data.untersuchungNameSchreiben === RequirementSetting.REQUIRED) {
      this.group.controls.nameSchreiben.addValidators(Validators.required);
    }
    if (anlassConfig.data.untersuchungFigurNachmalen === RequirementSetting.REQUIRED) {
      this.group.controls.figurNachmalen.addValidators(Validators.required);
    }
    if (anlassConfig.data.untersuchungStifthaltung === RequirementSetting.REQUIRED) {
      this.group.controls.stifthaltung.addValidators(Validators.required);
    }
    if (anlassConfig.data.untersuchungAussprache === RequirementSetting.REQUIRED) {
      this.group.controls.aussprache.addValidators(Validators.required);
    }
    if (anlassConfig.data.untersuchungLesefit === RequirementSetting.REQUIRED) {
      this.group.controls.lesefit.addValidators(Validators.required);
    }
    if (anlassConfig.data.untersuchungLesefitPunkte === RequirementSetting.REQUIRED) {
      this.group.controls.lesefitPunkte.addValidators(Validators.required);
    }
    if (anlassConfig.data.untersuchungZahlenNachsprechen4 === RequirementSetting.REQUIRED) {
      this.group.controls.zahlenNachsprechen4.addValidators(Validators.required);
    }
    if (anlassConfig.data.untersuchungZahlenNachsprechen5 === RequirementSetting.REQUIRED) {
      this.group.controls.zahlenNachsprechen5.addValidators(Validators.required);
    }
    if (anlassConfig.data.untersuchungZeichnenKreis === RequirementSetting.REQUIRED) {
      this.group.controls.zeichnenKreis.addValidators(Validators.required);
    }
    if (anlassConfig.data.untersuchungZeichnenViereck === RequirementSetting.REQUIRED) {
      this.group.controls.zeichnenViereck.addValidators(Validators.required);
    }
    if (anlassConfig.data.untersuchungZeichnenDreieck === RequirementSetting.REQUIRED) {
      this.group.controls.zeichnenDreieck.addValidators(Validators.required);
    }
    if (anlassConfig.data.untersuchungZeichnenMensch === RequirementSetting.REQUIRED) {
      this.group.controls.zeichnenMensch.addValidators(Validators.required);
    }
    if (anlassConfig.data.untersuchungZeichnenMenschPunkte === RequirementSetting.REQUIRED) {
      this.group.controls.zeichnenMenschPunkte.addValidators(Validators.required);
    }
  }

  public isSprachfaehigkeitMultiValueAnderesOrArtikulationsfehlerChecked(): boolean {
    return checkIfArrayContainsCheckedValue(this.group.controls.sprachfaehigkeitMultiValueArray.controls,
      [Sprachfaehigkeit.ANDERES, Sprachfaehigkeit.ARTIKULATIONSFEHLER]);
  }

  public isBewegungsapparatAnderesChecked(): boolean {
    return checkIfArrayContainsCheckedValue(this.group.controls.bewegungsapparatArray.controls,
      [Bewegungsapparat.ANDERES]);
  }

  public isFingeroppositionAnderesChecked(): boolean {
    return checkIfArrayContainsCheckedValue(this.group.controls.fingeroppositionArray.controls,
      [Fingeropposition.ANDERES]);
  }

  public isMundAndZaehneAnderesChecked(): boolean {
    return checkIfArrayContainsCheckedValue(this.group.controls.mundAndZaehneArray.controls, [MundAndZaehne.ANDERES]);
  }

  public isEinbeinstandAnderesChecked(): boolean {
    return this.group.controls.einbeinstand.value === Einbeinstand.ANDERES;
  }

  public isEinbeinhuepfenAnderesChecked(): boolean {
    return this.group.controls.einbeinhuepfen.value === Einbeinhuepfen.ANDERES;
  }

  public isHerzauskultationAnderesChecked(): boolean {
    return this.group.controls.herzauskultation.value === Herzauskultation.ANDERES;
  }

  public isHautAnderesChecked(): boolean {
    return this.group.controls.haut.value === Haut.ANDERES;
  }
}
