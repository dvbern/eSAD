import {DestroyRef} from '@angular/core';
import {takeUntilDestroyed} from '@angular/core/rxjs-interop';
import {FormArray, NonNullableFormBuilder, Validators} from '@angular/forms';
import {
  AnlassConfigJson,
  BrilleLinsenResultat,
  DistanzSehenResultat,
  FarbsehenResultat,
  InspektionKonvergenzResultat,
  MotilitaetResultat,
  RequirementSetting,
  SehenEmpfehlung,
  StereotestResultat,
  StrabismusAuftretenResultat,
  StrabismusAuspraegungResultat,
  StrabismusResultat,
  UntersuchungSehenBeurteilungSaveJson,
  VerwendetTafelResultat,
} from '@dvb/data-access-backend';
import {globalConstants} from '@dvb/feat-shared';
import {AppValidators, BaseForm} from '@dvb/util-angular';
import {ensureProps, Nullish, requireValue, trimToUndefined} from '@dvb/util-shared';
import {distinctUntilChanged} from 'rxjs';
import {markAllMultiCheckboxesAsTouched} from '../../untersuchung.util';

type Config = {
  fb: NonNullableFormBuilder,
  destroyRef: DestroyRef,
}

function makeDistanzSehenSubForm(fb: NonNullableFormBuilder, value: DistanzSehenResultat) {
  return fb.group({
    resultat: fb.control<DistanzSehenResultat>({value, disabled: true}),
    isChecked: fb.control<boolean>(false),
  });
}

function makeStrabismusSubForm(fb: NonNullableFormBuilder, value: StrabismusResultat) {
  return fb.group({
    resultat: fb.control<StrabismusResultat>({value, disabled: true}),
    isChecked: fb.control<boolean>(false),
  });
}

function makeEmpfehlungSubForm(fb: NonNullableFormBuilder, value: SehenEmpfehlung) {
  return fb.group({
    resultat: fb.control<SehenEmpfehlung>({value, disabled: true}),
    isChecked: fb.control<boolean>(false),
  });
}

export type DistanzSehenSubFormType = ReturnType<typeof makeDistanzSehenSubForm>
export type StrabismusSubFormType = ReturnType<typeof makeStrabismusSubForm>
export type EmpfehlungSubFormType = ReturnType<typeof makeEmpfehlungSubForm>

function makeForm(config: Config) {
  const fb = config.fb;
  const group = fb.group({
    brilleLinsen: fb.control<BrilleLinsenResultat | undefined>(undefined),
    stereotest: fb.control<StereotestResultat | undefined>(undefined),
    farbsehen: fb.control<FarbsehenResultat | undefined>(undefined),
    verwendeteTafel: fb.control<VerwendetTafelResultat | undefined>(undefined),
    fernvisusArray: fb.array<DistanzSehenSubFormType>([]),
    fernvisusRechts: fb.control<number | undefined>(undefined),
    fernvisusLinks: fb.control<number | undefined>(undefined),
    nahvisusArray: fb.array<DistanzSehenSubFormType>([]),
    nahvisusRechts: fb.control<number | undefined>(undefined),
    nahvisusLinks: fb.control<number | undefined>(undefined),
    motilitaet: fb.control<MotilitaetResultat | undefined>(undefined),
    strabismusArray: fb.array<StrabismusSubFormType>([]),
    strabismusAuspraegung: fb.control<StrabismusAuspraegungResultat | undefined>(undefined),
    strabismusAuftreten: fb.control<StrabismusAuftretenResultat | undefined>(undefined),
    inspektion: fb.control<InspektionKonvergenzResultat | undefined>(undefined),
    konvergenz: fb.control<InspektionKonvergenzResultat | undefined>(undefined),
    empfehlungArray: fb.array<EmpfehlungSubFormType>([]),
    bemerkungen: fb.control<string>('', AppValidators.textareaMaxLength2000()),
    version: fb.control<number | Nullish>({value: undefined, disabled: true}),
  });

  // Eine Eingabe aendert den required-Status eines anderen Feldes
  group.controls.fernvisusArray.valueChanges.pipe(distinctUntilChanged(), takeUntilDestroyed(config.destroyRef))
    .subscribe({
      next: _ => {
        group.controls.fernvisusRechts.updateValueAndValidity();
        group.controls.fernvisusLinks.updateValueAndValidity();
      },
      error: console.error,
    });
  group.controls.nahvisusArray.valueChanges.pipe(distinctUntilChanged(), takeUntilDestroyed(config.destroyRef))
    .subscribe({
      next: _ => {
        group.controls.nahvisusRechts.updateValueAndValidity();
        group.controls.nahvisusLinks.updateValueAndValidity();
      },
      error: console.error,
    });

  // Multi-Checkboxen: Wenn eines touched ist aber keines ausgewählt, müssen alle rot werden
  markAllMultiCheckboxesAsTouched(group.controls.fernvisusArray, config.destroyRef);
  markAllMultiCheckboxesAsTouched(group.controls.nahvisusArray, config.destroyRef);
  markAllMultiCheckboxesAsTouched(group.controls.strabismusArray, config.destroyRef);
  markAllMultiCheckboxesAsTouched(group.controls.empfehlungArray, config.destroyRef);

  return group;
}

export class UntersuchungSehenBeurteilungForm
  extends BaseForm<ReturnType<typeof makeForm>, UntersuchungSehenBeurteilungSaveJson> {

  constructor(protected config: Config) {
    super(() => makeForm(config));
  }

  resetForm(value: UntersuchungSehenBeurteilungSaveJson | undefined): void {
    super.reset(value);
    this.resetFernvisus(value);
    this.resetNahvisus(value);
    this.resetStrabismus(value);
    this.resetEmpfehlung(value);
  }

  private resetFernvisus(value: UntersuchungSehenBeurteilungSaveJson | Nullish): void {
    this.group.controls.fernvisusArray.clear();
    for (const resultat of Object.values(DistanzSehenResultat)) {
      const subForm = makeDistanzSehenSubForm(this.config.fb, resultat);
      subForm.controls.isChecked.reset(!!value?.fernvisus?.find(fv => fv === resultat));
      this.group.controls.fernvisusArray.push(subForm);
    }
  }

  private resetNahvisus(value: UntersuchungSehenBeurteilungSaveJson | Nullish): void {
    this.group.controls.nahvisusArray.clear();
    for (const resultat of Object.values(DistanzSehenResultat)) {
      const subForm = makeDistanzSehenSubForm(this.config.fb, resultat);
      subForm.controls.isChecked.reset(!!value?.nahvisus?.find(fv => fv === resultat));
      this.group.controls.nahvisusArray.push(subForm);
    }
  }

  private resetStrabismus(value: UntersuchungSehenBeurteilungSaveJson | Nullish): void {
    this.group.controls.strabismusArray.clear();
    for (const resultat of Object.values(StrabismusResultat)) {
      const subForm = makeStrabismusSubForm(this.config.fb, resultat);
      subForm.controls.isChecked.reset(!!value?.strabismus?.find(fv => fv === resultat));
      this.group.controls.strabismusArray.push(subForm);
    }
  }

  private resetEmpfehlung(value: UntersuchungSehenBeurteilungSaveJson | Nullish): void {
    this.group.controls.empfehlungArray.clear();
    for (const resultat of Object.values(SehenEmpfehlung)) {
      const subForm = makeEmpfehlungSubForm(this.config.fb, resultat);
      subForm.controls.isChecked.reset(!!value?.empfehlung?.find(fv => fv === resultat));
      this.group.controls.empfehlungArray.push(subForm);
    }
  }

  public override valueOnValid(): UntersuchungSehenBeurteilungSaveJson {
    const value = requireValue(this.group.getRawValue());
    return ensureProps<UntersuchungSehenBeurteilungSaveJson>({
      brilleLinsen: value.brilleLinsen,
      stereotest: value.stereotest,
      farbsehen: value.farbsehen,
      verwendeteTafel: value.verwendeteTafel,
      fernvisus: value.fernvisusArray.filter(i => i.isChecked).map(i => i.resultat),
      fernvisusRechts: value.fernvisusRechts,
      fernvisusLinks: value.fernvisusLinks,
      nahvisus: value.nahvisusArray.filter(i => i.isChecked).map(i => i.resultat),
      nahvisusRechts: value.nahvisusRechts,
      nahvisusLinks: value.nahvisusLinks,
      motilitaet: value.motilitaet,
      inspektion: value.inspektion,
      konvergenz: value.konvergenz,
      bemerkungen: trimToUndefined(value.bemerkungen),
      strabismus: value.strabismusArray.filter(i => i.isChecked).map(i => i.resultat),
      strabismusAuspraegung: value.strabismusAuspraegung,
      strabismusAuftreten: value.strabismusAuftreten,
      empfehlung: value.empfehlungArray.filter(i => i.isChecked).map(i => i.resultat),
      version: value.version ?? globalConstants.entityPrePersistVersion,
    });
  }

  setValidatorsForAnlassConfig(config: AnlassConfigJson): void {
    if (config.data.sehenBrilleLinsen === RequirementSetting.REQUIRED) {
      this.group.controls.brilleLinsen.addValidators(Validators.required);
    }
    if (config.data.sehenStereoTest === RequirementSetting.REQUIRED) {
      this.group.controls.stereotest.addValidators(Validators.required);
    }
    if (config.data.sehenFarbsehen === RequirementSetting.REQUIRED) {
      this.group.controls.farbsehen.addValidators(Validators.required);
    }
    if (config.data.sehenTafel === RequirementSetting.REQUIRED) {
      this.group.controls.verwendeteTafel.addValidators(Validators.required);
    }
    if (config.data.sehenFernvisus === RequirementSetting.REQUIRED) {
      this.group.controls.fernvisusArray.addValidators(AppValidators.atLeastOneCheckedValidator());
      this.group.controls.fernvisusRechts.addValidators(AppValidators.requiredIfValidator(() => {
        const formArray: FormArray<DistanzSehenSubFormType> = this.group.controls.fernvisusArray;
        const fernVisusNichtGeprueft = formArray.controls.find(i => i.controls.resultat.getRawValue()
          === DistanzSehenResultat.NICHT_GEPRUEFT);
        return !fernVisusNichtGeprueft?.value.isChecked;
      }));
      this.group.controls.fernvisusLinks.addValidators(AppValidators.requiredIfValidator(() => {
        const formArray: FormArray<DistanzSehenSubFormType> = this.group.controls.fernvisusArray;
        const fernVisusNichtGeprueft = formArray.controls.find(i => i.controls.resultat.getRawValue()
          === DistanzSehenResultat.NICHT_GEPRUEFT);
        return !fernVisusNichtGeprueft?.value.isChecked;
      }));
    }
    if (config.data.sehenNahvisus === RequirementSetting.REQUIRED) {
      this.group.controls.nahvisusArray.addValidators(AppValidators.atLeastOneCheckedValidator());
      this.group.controls.nahvisusRechts.addValidators(AppValidators.requiredIfValidator(() => {
        const formArray: FormArray<DistanzSehenSubFormType> = this.group.controls.nahvisusArray;
        const nahVisusNichtGeprueft = formArray.controls.find(i => i.controls.resultat.getRawValue()
          === DistanzSehenResultat.NICHT_GEPRUEFT);
        return !nahVisusNichtGeprueft?.value.isChecked;
      }));
      this.group.controls.nahvisusLinks.addValidators(AppValidators.requiredIfValidator(() => {
        const formArray: FormArray<DistanzSehenSubFormType> = this.group.controls.nahvisusArray;
        const nahVisusNichtGeprueft = formArray.controls.find(i => i.controls.resultat.getRawValue()
          === DistanzSehenResultat.NICHT_GEPRUEFT);
        return !nahVisusNichtGeprueft?.value.isChecked;
      }));
    }
    if (config.data.sehenStrabismus === RequirementSetting.REQUIRED) {
      this.group.controls.strabismusArray.addValidators(AppValidators.atLeastOneCheckedValidator());
      this.group.controls.strabismusAuspraegung.addValidators(Validators.required);
      this.group.controls.strabismusAuftreten.addValidators(Validators.required);
    }
    if (config.data.sehenMotilitaet === RequirementSetting.REQUIRED) {
      this.group.controls.motilitaet.addValidators(Validators.required);
    }
    if (config.data.sehenInspektion === RequirementSetting.REQUIRED) {
      this.group.controls.inspektion.addValidators(Validators.required);
    }
    if (config.data.sehenKonvergenz === RequirementSetting.REQUIRED) {
      this.group.controls.konvergenz.addValidators(Validators.required);
    }
    if (config.data.sehenEmpfehlung === RequirementSetting.REQUIRED) {
      this.group.controls.empfehlungArray.addValidators(AppValidators.atLeastOneCheckedValidator());
    }
    if (config.data.sehenBemerkungen === RequirementSetting.REQUIRED) {
      this.group.controls.bemerkungen.addValidators(Validators.required);
    }
  }
}
