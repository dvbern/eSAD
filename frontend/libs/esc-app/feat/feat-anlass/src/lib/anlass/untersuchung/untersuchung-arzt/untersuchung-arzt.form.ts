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
  Bewegungsapparat,
  Blutdruck,
  Einbeinhuepfen,
  Einbeinstand,
  Fingeropposition,
  Haut,
  Herzauskultation,
  Motorik,
  MundAndZaehne,
  RequirementSetting,
  Sprachfaehigkeit,
  UntersuchungArztSaveJson,
  Verhalten,
} from '@dvb/data-access-backend';
import {AppValidators, BaseForm} from '@dvb/util-angular';
import {ensureProps, Nullish, requireValue, trimToUndefined} from '@dvb/util-shared';
import {distinctUntilChanged} from 'rxjs';
import {checkIfArrayContainsCheckedValue, markAllMultiCheckboxesAsTouched} from '../untersuchung.util';

type Config = {
  fb: NonNullableFormBuilder,
  destroyRef: DestroyRef,
}
export type SprachfaehigkeitSubFormType = ReturnType<typeof makeSprachfaehigkeitSubForm>
export type BewegungsapparatSubFormType = ReturnType<typeof makeBewegungsapparatSubForm>
export type FingeroppositionSubFormType = ReturnType<typeof makeFingeroppositionSubForm>
export type MundAndZaehneSubFormType = ReturnType<typeof makeMundAndZaehneSubForm>

function makeForm(config: Config) {
  const fb = config.fb;
  const group = fb.group({
    verhalten: fb.control<Verhalten | undefined>(undefined),
    blutdruck: fb.control<Blutdruck | undefined>(undefined),
    blutdruckSystolisch: fb.control<number | undefined>(undefined),
    blutdruckDiastolisch: fb.control<number | undefined>(undefined),
    herzauskultation: fb.control<Herzauskultation | undefined>(undefined),
    herzauskultationBemerkung: fb.control<string>('', AppValidators.textareaMaxLength()),
    haut: fb.control<Haut | undefined>(undefined),
    hautBemerkung: fb.control<string>('', AppValidators.textareaMaxLength()),
    mundAndZaehneArray: fb.array<MundAndZaehneSubFormType>([]),
    mundAndZaehneBemerkung: fb.control<string>('', AppValidators.textareaMaxLength()),
    sprachfaehigkeitArray: fb.array<SprachfaehigkeitSubFormType>([]),
    sprachfaehigkeitBemerkung: fb.control<string>('', AppValidators.textareaMaxLength()),
    motorik: fb.control<Motorik | undefined>(undefined),
    bewegungsapparatArray: fb.array<BewegungsapparatSubFormType>([]),
    bewegungsapparatBemerkung: fb.control<string>('', AppValidators.textareaMaxLength()),
    einbeinstand: fb.control<Einbeinstand | undefined>(undefined),
    einbeinstandBemerkung: fb.control<string>('', AppValidators.textareaMaxLength()),
    einbeinhuepfen: fb.control<Einbeinhuepfen | undefined>(undefined),
    einbeinhuepfenBemerkung: fb.control<string>('', AppValidators.textareaMaxLength()),
    fingeroppositionArray: fb.array<FingeroppositionSubFormType>([]),
    fingeroppositionBemerkung: fb.control<string>('', AppValidators.textareaMaxLength()),
    bemerkungen: fb.control<string>('', AppValidators.textareaMaxLength()),
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
  markAllMultiCheckboxesAsTouched(group.controls.mundAndZaehneArray, config.destroyRef);
  markAllMultiCheckboxesAsTouched(group.controls.sprachfaehigkeitArray, config.destroyRef);
  markAllMultiCheckboxesAsTouched(group.controls.bewegungsapparatArray, config.destroyRef);
  markAllMultiCheckboxesAsTouched(group.controls.fingeroppositionArray, config.destroyRef);

  return group;
}

function makeSprachfaehigkeitSubForm(fb: NonNullableFormBuilder, value: Sprachfaehigkeit) {
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

export class UntersuchungArztForm extends BaseForm<ReturnType<typeof makeForm>, UntersuchungArztSaveJson> {

  constructor(protected config: Config) {
    super(() => makeForm(config));
  }

  resetForm(value: UntersuchungArztSaveJson | Nullish): void {
    this.group.reset({
      ...value,
    });
    this.resetSprachfaehigkeit(value);
    this.resetBewegungsapparat(value);
    this.resetFingeropposition(value);
    this.resetMundAndZaehne(value);
  }

  private resetSprachfaehigkeit(value: UntersuchungArztSaveJson | Nullish): void {
    this.group.controls.sprachfaehigkeitArray.clear();
    for (const resultat of Object.values(Sprachfaehigkeit)) {
      const subForm = makeSprachfaehigkeitSubForm(this.config.fb, resultat);
      subForm.controls.isChecked.reset(!!value?.sprachfaehigkeit?.find(fv => fv === resultat));
      this.group.controls.sprachfaehigkeitArray.push(subForm)
    }
  }

  private resetBewegungsapparat(value: UntersuchungArztSaveJson | Nullish): void {
    this.group.controls.bewegungsapparatArray.clear();
    for (const resultat of Object.values(Bewegungsapparat)) {
      const subForm = makeBewegungsapparatSubForm(this.config.fb, resultat);
      subForm.controls.isChecked.reset(!!value?.bewegungsapparat?.find(fv => fv === resultat));
      this.group.controls.bewegungsapparatArray.push(subForm)
    }
  }

  private resetFingeropposition(value: UntersuchungArztSaveJson | Nullish): void {
    this.group.controls.fingeroppositionArray.clear();
    for (const resultat of Object.values(Fingeropposition)) {
      const subForm = makeFingeroppositionSubForm(this.config.fb, resultat);
      subForm.controls.isChecked.reset(!!value?.fingeropposition?.find(fv => fv === resultat));
      this.group.controls.fingeroppositionArray.push(subForm)
    }
  }

  private resetMundAndZaehne(value: UntersuchungArztSaveJson | Nullish): void {
    this.group.controls.mundAndZaehneArray.clear();
    for (const resultat of Object.values(MundAndZaehne)) {
      const subForm = makeMundAndZaehneSubForm(this.config.fb, resultat);
      subForm.controls.isChecked.reset(!!value?.mundAndZaehne?.find(fv => fv === resultat));
      this.group.controls.mundAndZaehneArray.push(subForm)
    }
  }

  public override valueOnValid(): UntersuchungArztSaveJson {
    const value = requireValue(this.group.getRawValue());
    return ensureProps<UntersuchungArztSaveJson>({
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
      sprachfaehigkeit: value.sprachfaehigkeitArray.filter(i => i.isChecked).map(i => i.resultat),
      sprachfaehigkeitBemerkung: trimToUndefined(value.sprachfaehigkeitBemerkung),
      motorik: value.motorik,
      bewegungsapparat: value.bewegungsapparatArray.filter(i => i.isChecked).map(i => i.resultat),
      bewegungsapparatBemerkung: trimToUndefined(value.bewegungsapparatBemerkung),
      einbeinstand: value.einbeinstand,
      einbeinstandBemerkung: trimToUndefined(value.einbeinstandBemerkung),
      einbeinhuepfen: value.einbeinhuepfen,
      einbeinhuepfenBemerkung: trimToUndefined(value.einbeinhuepfenBemerkung),
      fingeropposition: value.fingeroppositionArray.filter(i => i.isChecked).map(i => i.resultat),
      fingeroppositionBemerkung: trimToUndefined(value.fingeroppositionBemerkung),
      bemerkungen: trimToUndefined(value.bemerkungen)
    });
  }

  public setValidatorsForAnlassConfig(anlassConfig: AnlassConfigJson): void {
    if (anlassConfig.untersuchungBewegungsapparat === RequirementSetting.REQUIRED) {
      this.group.controls.bewegungsapparatArray.addValidators(AppValidators.atLeastOneCheckedValidator());
      this.group.controls.bewegungsapparatBemerkung.addValidators(AppValidators.requiredIfValidator(() => {
        return this.isBewegungsapparatAnderesChecked();
      }));
    }
    if (anlassConfig.untersuchungEinbeinstand === RequirementSetting.REQUIRED) {
      this.group.controls.einbeinstand.addValidators(Validators.required);
      this.group.controls.einbeinstandBemerkung.addValidators(AppValidators.requiredIfValidator(() => {
        return this.isEinbeinstandAnderesChecked();
      }));
    }
    if (anlassConfig.untersuchungEinbeinhuepfen === RequirementSetting.REQUIRED) {
      this.group.controls.einbeinhuepfen.addValidators(Validators.required);
      this.group.controls.einbeinhuepfenBemerkung.addValidators(AppValidators.requiredIfValidator(() => {
        return this.isEinbeinhuepfenAnderesChecked();
      }));
    }
    if (anlassConfig.untersuchungFingeropposition === RequirementSetting.REQUIRED) {
      this.group.controls.fingeroppositionArray.addValidators(AppValidators.atLeastOneCheckedValidator());
      this.group.controls.fingeroppositionBemerkung.addValidators(AppValidators.requiredIfValidator(() => {
        return this.isFingeroppositionAnderesChecked();
      }));
    }
    if (anlassConfig.untersuchungMotorik === RequirementSetting.REQUIRED) {
      this.group.controls.motorik.addValidators(Validators.required);
    }
    if (anlassConfig.untersuchungSprache === RequirementSetting.REQUIRED) {
      this.group.controls.sprachfaehigkeitArray.addValidators(AppValidators.atLeastOneCheckedValidator());
      this.group.controls.sprachfaehigkeitBemerkung.addValidators(AppValidators.requiredIfValidator(() => {
        return this.isSprachfaehigkeitAnderesOrArtikulationsfehlerChecked();
      }));
    }
    if (anlassConfig.untersuchungMundZaehne === RequirementSetting.REQUIRED) {
      this.group.controls.mundAndZaehneArray.addValidators(AppValidators.atLeastOneCheckedValidator());
      this.group.controls.mundAndZaehneBemerkung.addValidators(AppValidators.requiredIfValidator(() => {
        return this.isMundAndZaehneAnderesChecked();
      }));
    }
    if (anlassConfig.untersuchungHaut === RequirementSetting.REQUIRED) {
      this.group.controls.haut.addValidators(Validators.required);
      this.group.controls.hautBemerkung.addValidators(AppValidators.requiredIfValidator(() => {
        return this.isHautAnderesChecked();
      }));
    }
    if (anlassConfig.untersuchungHerz === RequirementSetting.REQUIRED) {
      this.group.controls.herzauskultation.addValidators(Validators.required);
      this.group.controls.herzauskultationBemerkung.addValidators(AppValidators.requiredIfValidator(() => {
        return this.isHerzauskultationAnderesChecked();
      }));
    }
    if (anlassConfig.untersuchungBlutdruck === RequirementSetting.REQUIRED) {
      this.group.controls.blutdruck.addValidators(Validators.required);
      this.group.controls.blutdruckSystolisch.addValidators(AppValidators.requiredIfValidator(() => {
        return this.group.controls.blutdruck.value !== Blutdruck.NICHT_GEPRUEFT;
      }));
      this.group.controls.blutdruckDiastolisch.addValidators(AppValidators.requiredIfValidator(() => {
        return this.group.controls.blutdruck.value !== Blutdruck.NICHT_GEPRUEFT;
      }));
    }
    if (anlassConfig.untersuchungVerhalten === RequirementSetting.REQUIRED) {
      this.group.controls.verhalten.addValidators(Validators.required);
    }
    if (anlassConfig.untersuchungBemerkungen === RequirementSetting.REQUIRED) {
      this.group.controls.bemerkungen.addValidators(Validators.required);
    }
  }

  public isSprachfaehigkeitAnderesOrArtikulationsfehlerChecked(): boolean {
    return checkIfArrayContainsCheckedValue(this.group.controls.sprachfaehigkeitArray.controls, [Sprachfaehigkeit.ANDERES, Sprachfaehigkeit.ARTIKULATIONSFEHLER]);
  }

  public isBewegungsapparatAnderesChecked(): boolean {
    return checkIfArrayContainsCheckedValue(this.group.controls.bewegungsapparatArray.controls, [Bewegungsapparat.ANDERES]);
  }

  public isFingeroppositionAnderesChecked(): boolean {
    return checkIfArrayContainsCheckedValue(this.group.controls.fingeroppositionArray.controls, [Fingeropposition.ANDERES]);
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
