import {DestroyRef} from '@angular/core';
import {takeUntilDestroyed} from '@angular/core/rxjs-interop';
import {FormArray, NonNullableFormBuilder, Validators} from '@angular/forms';
import {
  AnlassConfigJson,
  BrilleLinsenResultat,
  DistanzSehenResultat,
  FarbsehenResultat,
  InfomaterialJson,
  InformationEltern,
  InspektionKonvergenzResultat,
  MotilitaetResultat,
  RequirementSetting,
  StereotestResultat,
  StrabismusAuftretenResultat,
  StrabismusAuspraegungResultat,
  StrabismusResultat,
  UntersuchungSehenMassnahmenSaveJson,
  UntersuchungSehenSaveJson,
  VerwendetTafelResultat,
} from '@dvb/data-access-backend';
import {AppValidators, BaseForm} from '@dvb/util-angular';
import {ensureProps, Nullish, requireValue, trimToUndefined} from '@dvb/util-shared';
import {distinctUntilChanged} from 'rxjs';
import {markAllMultiCheckboxesAsTouched} from '../untersuchung.util';

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

export type DistanzSehenSubFormType = ReturnType<typeof makeDistanzSehenSubForm>
export type StrabismusSubFormType = ReturnType<typeof makeStrabismusSubForm>


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
    bemerkungen: fb.control<string>('', AppValidators.textareaMaxLength()),

    keineMassnahmen: fb.control<boolean>(false),
    bereitsBehandeltSehen: fb.control<boolean>(false),
    ueberweisungAugenarzt: fb.control<boolean>(false),
    nachkontrolleSehen: fb.control<boolean>(false),
    abgegebenesInfomaterial: fb.control<InfomaterialJson[]>([]),
    informationEltern: fb.control<InformationEltern | undefined>(undefined),
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

  return group;
}

export class UntersuchungSehenForm extends BaseForm<ReturnType<typeof makeForm>, UntersuchungSehenSaveJson> {

  constructor(protected config: Config) {
    super(() => makeForm(config));
  }

  resetForm(value: UntersuchungSehenSaveJson | Nullish): void {
    this.group.reset({
      ...value,
      keineMassnahmen: value?.massnahmen?.keineMassnahmen,
      bereitsBehandeltSehen: value?.massnahmen?.bereitsBehandeltSehen,
      ueberweisungAugenarzt: value?.massnahmen?.ueberweisungAugenarzt,
      nachkontrolleSehen: value?.massnahmen?.nachkontrolleSehen,
      abgegebenesInfomaterial: value?.massnahmen?.abgegebenesInfomaterial,
      informationEltern: value?.massnahmen?.informationEltern,
    });
    this.resetFernvisus(value);
    this.resetNahvisus(value);
    this.resetStrabismus(value);
  }

  private resetFernvisus(value: UntersuchungSehenSaveJson | Nullish): void {
    this.group.controls.fernvisusArray.clear();
    for (const resultat of Object.values(DistanzSehenResultat)) {
      const subForm = makeDistanzSehenSubForm(this.config.fb, resultat);
      subForm.controls.isChecked.reset(!!value?.fernvisus?.find(fv => fv === resultat));
      this.group.controls.fernvisusArray.push(subForm);
    }
  }

  private resetNahvisus(value: UntersuchungSehenSaveJson | Nullish): void {
    this.group.controls.nahvisusArray.clear();
    for (const resultat of Object.values(DistanzSehenResultat)) {
      const subForm = makeDistanzSehenSubForm(this.config.fb, resultat);
      subForm.controls.isChecked.reset(!!value?.nahvisus?.find(fv => fv === resultat));
      this.group.controls.nahvisusArray.push(subForm);
    }
  }

  private resetStrabismus(value: UntersuchungSehenSaveJson | Nullish): void {
    this.group.controls.strabismusArray.clear();
    for (const resultat of Object.values(StrabismusResultat)) {
      const subForm = makeStrabismusSubForm(this.config.fb, resultat);
      subForm.controls.isChecked.reset(!!value?.strabismus?.find(fv => fv === resultat));
      this.group.controls.strabismusArray.push(subForm);
    }
  }


  public override valueOnValid(withMassnahmen = false): UntersuchungSehenSaveJson {
    const value = requireValue(this.group.getRawValue());
    const massnahmen = ensureProps<UntersuchungSehenMassnahmenSaveJson>({
      keineMassnahmen: value.keineMassnahmen,
      bereitsBehandeltSehen: value.bereitsBehandeltSehen,
      // @ts-expect-error scheint er nicht zu erkennen
      abgegebenesInfomaterial: value.abgegebenesInfomaterial ?? [],
      informationEltern: value.informationEltern,
      ueberweisungAugenarzt: value.ueberweisungAugenarzt,
      nachkontrolleSehen: value.nachkontrolleSehen,
    });
    return ensureProps<UntersuchungSehenSaveJson>({
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
      massnahmen: withMassnahmen ? massnahmen : undefined,
    });
  }

  setValidatorsForAnlassConfig(config: AnlassConfigJson): void {
    if (config.sehenBrilleLinsen === RequirementSetting.REQUIRED) {
      this.group.controls.brilleLinsen.addValidators(Validators.required);
    }
    if (config.sehenStereoTest === RequirementSetting.REQUIRED) {
      this.group.controls.stereotest.addValidators(Validators.required);
    }
    if (config.sehenFarbsehen === RequirementSetting.REQUIRED) {
      this.group.controls.farbsehen.addValidators(Validators.required);
    }
    if (config.sehenTafel === RequirementSetting.REQUIRED) {
      this.group.controls.verwendeteTafel.addValidators(Validators.required);
    }
    if (config.sehenFernvisus === RequirementSetting.REQUIRED) {
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
    if (config.sehenNahvisus === RequirementSetting.REQUIRED) {
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
    if (config.sehenStrabismus === RequirementSetting.REQUIRED) {
      this.group.controls.strabismusArray.addValidators(AppValidators.atLeastOneCheckedValidator());
      this.group.controls.strabismusAuspraegung.addValidators(Validators.required);
      this.group.controls.strabismusAuftreten.addValidators(Validators.required);
    }
    if (config.sehenMotilitaet === RequirementSetting.REQUIRED) {
      this.group.controls.motilitaet.addValidators(Validators.required);
    }
    if (config.sehenInspektion === RequirementSetting.REQUIRED) {
      this.group.controls.inspektion.addValidators(Validators.required);
    }
    if (config.sehenKonvergenz === RequirementSetting.REQUIRED) {
      this.group.controls.konvergenz.addValidators(Validators.required);
    }
    if (config.sehenBemerkungen === RequirementSetting.REQUIRED) {
      this.group.controls.bemerkungen.addValidators(Validators.required);
    }
  }
}
