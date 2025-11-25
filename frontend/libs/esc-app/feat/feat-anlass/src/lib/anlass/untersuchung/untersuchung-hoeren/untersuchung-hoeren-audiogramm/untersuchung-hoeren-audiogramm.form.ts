import {DestroyRef, effect} from '@angular/core';
import {toSignal} from '@angular/core/rxjs-interop';
import {NonNullableFormBuilder, Validators} from '@angular/forms';
import {
  AnlassConfigJson,
  AudiogrammBeurteilung,
  AudiogrammSaveJson,
  HoerenEmpfehlung,
  HoerenZusaetzlicheAngaben,
  RequirementSetting,
  UntersuchungHoerenAudiogrammSaveJson,
} from '@dvb/data-access-backend';
import {globalConstants} from '@dvb/feat-shared';
import {AppValidators, BaseForm} from '@dvb/util-angular';
import {ensureProps, isNotNullish, Nullish, requireValue, trimToUndefined} from '@dvb/util-shared';
import {markAllMultiCheckboxesAsTouched} from '../../untersuchung.util';
import {AudiogrammForm} from './audiogramm/audiogramm-form';

type Config = {
  fb: NonNullableFormBuilder,
  destroyRef: DestroyRef,
  audiogrammForm: AudiogrammForm,
}

function makeAudiogrammBeurteilungSubForm(fb: NonNullableFormBuilder, value: AudiogrammBeurteilung) {
  return fb.group({
    resultat: fb.control<AudiogrammBeurteilung>({value, disabled: true}),
    isChecked: fb.control<boolean>(false),
    version: fb.control<number | Nullish>({value: undefined, disabled: true}),
  })
}

function makeZusaetzlicheAngabenSubForm(fb: NonNullableFormBuilder, value: HoerenZusaetzlicheAngaben) {
  return fb.group({
    resultat: fb.control<HoerenZusaetzlicheAngaben>({value, disabled: true}),
    isChecked: fb.control<boolean>(false),
  })
}

function makeEmpfehlungSubForm(fb: NonNullableFormBuilder, value: HoerenEmpfehlung) {
  return fb.group({
    resultat: fb.control<HoerenEmpfehlung>({value, disabled: true}),
    isChecked: fb.control<boolean>(false),
  });
}

export type AudiogrammBeurteilungSubFormType = ReturnType<typeof makeAudiogrammBeurteilungSubForm>
export type ZusaetzlicheAngabenSubFormType = ReturnType<typeof makeZusaetzlicheAngabenSubForm>
export type EmpfehlungSubFormType = ReturnType<typeof makeEmpfehlungSubForm>

function makeForm(config: Config) {
  const fb = config.fb;
  const group = fb.group({
    audiogramm: config.audiogrammForm.group,
    audiogrammBeurteilungArray: fb.array<AudiogrammBeurteilungSubFormType>([]),
    zusaetzlicheAngabenArray: fb.array<ZusaetzlicheAngabenSubFormType>([]),
    empfehlungArray: fb.array<EmpfehlungSubFormType>([]),
    bemerkungen: fb.control<string>('', AppValidators.textareaMaxLength2000()),
    version: fb.control<number | Nullish>({value: undefined, disabled: true}),
  });

  // Multi-Checkboxen: Wenn eines touched ist aber keines ausgewählt, müssen alle rot werden
  markAllMultiCheckboxesAsTouched(group.controls.audiogrammBeurteilungArray, config.destroyRef);
  markAllMultiCheckboxesAsTouched(group.controls.zusaetzlicheAngabenArray, config.destroyRef);
  markAllMultiCheckboxesAsTouched(group.controls.empfehlungArray, config.destroyRef);

  return group;
}

export class UntersuchungHoerenAudiogrammForm
  extends BaseForm<ReturnType<typeof makeForm>, UntersuchungHoerenAudiogrammSaveJson> {

  constructor(protected config: Config) {
    super(() => makeForm(config));
    const valueChanges = toSignal(this.group.controls.audiogrammBeurteilungArray.valueChanges);

    effect(() => {
      valueChanges();
      const anlassConfig = config.audiogrammForm.config.anlassConfigSig();
      if (isNotNullish(anlassConfig)) {
        const nichtGeprueftIsCheckedControl = this.group.controls.audiogrammBeurteilungArray.controls.find(r =>
          r.controls.resultat.value === AudiogrammBeurteilung.NICHT_GEPRUEFT && r.controls.isChecked.value);
        this.config.audiogrammForm.setValidators(anlassConfig, !!nichtGeprueftIsCheckedControl);
      }
    })
  }

  resetForm(value: UntersuchungHoerenAudiogrammSaveJson | Nullish): void {
    this.group.reset({...value});
    this.resetAudiogrammBeurteilung(value);
    this.resetZusaetzlicheAngaben(value);
    this.resetEmpfehlung(value);
    this.config.audiogrammForm.resetForm({links: value?.audiogrammLinks, rechts: value?.audiogrammRechts});
  }

  private resetAudiogrammBeurteilung(value: UntersuchungHoerenAudiogrammSaveJson | Nullish): void {
    this.group.controls.audiogrammBeurteilungArray.clear();
    for (const resultat of Object.values(AudiogrammBeurteilung)) {
      const subForm = makeAudiogrammBeurteilungSubForm(this.config.fb, resultat);
      subForm.controls.isChecked.reset(!!value?.audiogrammBeurteilung?.find(fv => fv === resultat));
      this.group.controls.audiogrammBeurteilungArray.push(subForm)
    }
  }

  private resetZusaetzlicheAngaben(value: UntersuchungHoerenAudiogrammSaveJson | Nullish): void {
    this.group.controls.zusaetzlicheAngabenArray.clear();
    for (const resultat of Object.values(HoerenZusaetzlicheAngaben)) {
      const subForm = makeZusaetzlicheAngabenSubForm(this.config.fb, resultat);
      subForm.controls.isChecked.reset(!!value?.zusaetzlicheAngaben?.find(fv => fv === resultat));
      this.group.controls.zusaetzlicheAngabenArray.push(subForm)
    }
  }

  private resetEmpfehlung(value: UntersuchungHoerenAudiogrammSaveJson | Nullish): void {
    this.group.controls.empfehlungArray.clear();
    for (const resultat of Object.values(HoerenEmpfehlung)) {
      const subForm = makeEmpfehlungSubForm(this.config.fb, resultat);
      subForm.controls.isChecked.reset(!!value?.empfehlung?.find(fv => fv === resultat));
      this.group.controls.empfehlungArray.push(subForm);
    }
  }

  public override valueOnValid(): UntersuchungHoerenAudiogrammSaveJson {
    const value = requireValue(this.group.getRawValue());
    const audiogrammRechts = this.config.audiogrammForm.valueOnValid().rechts;
    const audiogrammLinks = this.config.audiogrammForm.valueOnValid().links;
    return ensureProps<UntersuchungHoerenAudiogrammSaveJson>({
      audiogrammRechts: audiogrammRechts ? ensureProps<AudiogrammSaveJson>({
        dbHz250: audiogrammRechts?.dbHz250 ?? undefined,
        dbHz500: audiogrammRechts?.dbHz500 ?? undefined,
        dbHz1000: audiogrammRechts?.dbHz1000 ?? undefined,
        dbHz2000: audiogrammRechts?.dbHz2000 ?? undefined,
        dbHz4000: audiogrammRechts?.dbHz4000 ?? undefined,
        dbHz6000: audiogrammRechts?.dbHz6000 ?? undefined,
        dbHz8000: audiogrammRechts?.dbHz8000 ?? undefined,
        version: audiogrammRechts?.version ?? globalConstants.entityPrePersistVersion,
      }) : undefined,
      audiogrammLinks: audiogrammLinks ? ensureProps<AudiogrammSaveJson>({
        dbHz250: audiogrammLinks?.dbHz250 ?? undefined,
        dbHz500: audiogrammLinks?.dbHz500 ?? undefined,
        dbHz1000: audiogrammLinks?.dbHz1000 ?? undefined,
        dbHz2000: audiogrammLinks?.dbHz2000 ?? undefined,
        dbHz4000: audiogrammLinks?.dbHz4000 ?? undefined,
        dbHz6000: audiogrammLinks?.dbHz6000 ?? undefined,
        dbHz8000: audiogrammLinks?.dbHz8000 ?? undefined,
        version: audiogrammLinks?.version ?? globalConstants.entityPrePersistVersion,
      }) : undefined,
      audiogrammBeurteilung: value.audiogrammBeurteilungArray.filter(i => i.isChecked).map(i => i.resultat),
      zusaetzlicheAngaben: value.zusaetzlicheAngabenArray.filter(i => i.isChecked).map(i => i.resultat),
      empfehlung: value.empfehlungArray.filter(i => i.isChecked).map(i => i.resultat),
      bemerkungen: trimToUndefined(value.bemerkungen),
      version: value.version ?? globalConstants.entityPrePersistVersion,
    });
  }

  setValidators(config: AnlassConfigJson, data: UntersuchungHoerenAudiogrammSaveJson | undefined): void {

    const nichtGeprueftIsChecked = data?.audiogrammBeurteilung?.find(value => {
        return value === AudiogrammBeurteilung.NICHT_GEPRUEFT;
      },
    );
    // Audiogramm
    this.config.audiogrammForm.setValidators(config, !!nichtGeprueftIsChecked);
    // Audiogramm Beurteilung
    if (config.data.hoerenAudiogrammBeurteilung === RequirementSetting.REQUIRED) {
      this.group.controls.audiogrammBeurteilungArray.addValidators(AppValidators.atLeastOneCheckedValidator());
    }
    // Zusaetzliche Angaben
    if (config.data.hoerenAudiogrammZusaetzlicheAngaben === RequirementSetting.REQUIRED) {
      this.group.controls.zusaetzlicheAngabenArray.addValidators(AppValidators.atLeastOneCheckedValidator());
    }
    // Empfehlung
    if (config.data.hoerenAudiogrammEmpfehlung === RequirementSetting.REQUIRED) {
      this.group.controls.empfehlungArray.addValidators(AppValidators.atLeastOneCheckedValidator());
    }
    // Bemerkungen
    if (config.data.hoerenAudiogrammBemerkungen === RequirementSetting.REQUIRED) {
      this.group.controls.bemerkungen.addValidators(Validators.required);
    }
  }
}
