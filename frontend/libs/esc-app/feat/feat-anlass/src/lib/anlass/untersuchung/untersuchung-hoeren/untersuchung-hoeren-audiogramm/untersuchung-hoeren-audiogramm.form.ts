import {DestroyRef, effect} from '@angular/core';
import {toSignal} from '@angular/core/rxjs-interop';
import {NonNullableFormBuilder, Validators} from '@angular/forms';
import {
  AnlassConfigJson,
  AudiogrammBeurteilung,
  AudiogrammSaveJson,
  RequirementSetting,
  UntersuchungHoerenAudiogrammSaveJson,
} from '@dvb/data-access-backend';
import {globalConstants, ValueEnhancedForm} from '@dvb/feat-shared';
import {AppValidators, BaseForm} from '@dvb/util-angular';
import {ensureProps, ensurePropsFlat, isNotNullish, Nullish, requireValue, trimToUndefined} from '@dvb/util-shared';
import {AudiogrammForm} from './audiogramm/audiogramm-form';

type Config = {
  fb: NonNullableFormBuilder,
  destroyRef: DestroyRef,
  audiogrammForm: AudiogrammForm,
  audiogrammSimpelValueEnhancedForm: ValueEnhancedForm;
  hoerhilfeValueEnhancedForm: ValueEnhancedForm;
  audiogrammBeurteilungValueEnhancedForm: ValueEnhancedForm;
  zusaetzlicheAngabenValueEnhancedForm: ValueEnhancedForm;
  empfehlungValueEnhancedForm: ValueEnhancedForm;
}

function makeForm(config: Config) {
  const fb = config.fb;
  return fb.group({
    audiogramm: config.audiogrammForm.group,
    audiogrammSimpel: config.audiogrammSimpelValueEnhancedForm.group,
    hoerhilfe: config.hoerhilfeValueEnhancedForm.group,
    audiogrammBeurteilung: config.audiogrammBeurteilungValueEnhancedForm.group,
    zusaetzlicheAngaben: config.zusaetzlicheAngabenValueEnhancedForm.group,
    empfehlung: config.empfehlungValueEnhancedForm.group,
    bemerkungen: fb.control<string>('', AppValidators.textareaMaxLength2000()),
    version: fb.control<number | Nullish>({value: undefined, disabled: true}),
  });
}

export class UntersuchungHoerenAudiogrammForm
  extends BaseForm<ReturnType<typeof makeForm>, UntersuchungHoerenAudiogrammSaveJson> {
  readonly audiogrammSimpelValueEnhancedForm: ValueEnhancedForm;
  readonly hoerhilfeValueEnhancedForm: ValueEnhancedForm;
  readonly audiogrammBeurteilungValueEnhancedForm: ValueEnhancedForm;
  readonly zusaetzlicheAngabenValueEnhancedForm: ValueEnhancedForm;
  readonly empfehlungValueEnhancedForm: ValueEnhancedForm;

  constructor(protected config: Config) {
    super(() => makeForm(config));
    const valueChanges = toSignal(this.group.controls.audiogrammBeurteilung.valueChanges);

    this.audiogrammSimpelValueEnhancedForm = config.audiogrammSimpelValueEnhancedForm;
    this.hoerhilfeValueEnhancedForm = config.hoerhilfeValueEnhancedForm;
    this.audiogrammBeurteilungValueEnhancedForm= config.audiogrammBeurteilungValueEnhancedForm;
    this.zusaetzlicheAngabenValueEnhancedForm= config.zusaetzlicheAngabenValueEnhancedForm;
    this.empfehlungValueEnhancedForm= config.empfehlungValueEnhancedForm;

    effect(() => {
      valueChanges();
      const anlassConfig = config.audiogrammForm.config.anlassConfigSig();
      if (isNotNullish(anlassConfig)) {
        const nichtGeprueftIsCheckedControl = this.audiogrammBeurteilungValueEnhancedForm.group.controls.valueArray.controls.find(r =>
          r.controls.resultat.value === AudiogrammBeurteilung.NICHT_GEPRUEFT && r.controls.isChecked.value);
        this.config.audiogrammForm.setValidators(anlassConfig, !!nichtGeprueftIsCheckedControl);
      }
    })
  }

  resetForm(value: UntersuchungHoerenAudiogrammSaveJson | undefined): void {
    super.reset(value);
    this.hoerhilfeValueEnhancedForm.resetForm(value?.hoerhilfe);
    this.audiogrammSimpelValueEnhancedForm.resetForm(value?.audiogrammSimpel);
    this.config.audiogrammForm.resetForm({links: value?.audiogrammLinks, rechts: value?.audiogrammRechts});
    this.audiogrammBeurteilungValueEnhancedForm.resetForm(value?.audiogrammBeurteilung);
    this.zusaetzlicheAngabenValueEnhancedForm.resetForm(value?.zusaetzlicheAngaben);
    this.empfehlungValueEnhancedForm.resetForm(value?.empfehlung);
  }

  public override valueOnValid(): UntersuchungHoerenAudiogrammSaveJson {
    const value = requireValue(this.group.getRawValue());
    const audiogrammRechts = this.config.audiogrammForm.valueOnValid().rechts;
    const audiogrammLinks = this.config.audiogrammForm.valueOnValid().links;
    return ensurePropsFlat<UntersuchungHoerenAudiogrammSaveJson>({
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
      audiogrammBeurteilung: this.audiogrammBeurteilungValueEnhancedForm.valueOnValid(),
      zusaetzlicheAngaben: this.zusaetzlicheAngabenValueEnhancedForm.valueOnValid(),
      empfehlung: this.empfehlungValueEnhancedForm.valueOnValid(),
      bemerkungen: trimToUndefined(value.bemerkungen),
      hoerhilfe: this.hoerhilfeValueEnhancedForm.valueOnValid(),
      audiogrammSimpel: this.audiogrammSimpelValueEnhancedForm.valueOnValid(),
      version: value.version ?? globalConstants.entityPrePersistVersion,
    });
  }

  setValidators(config: AnlassConfigJson, data: UntersuchungHoerenAudiogrammSaveJson | undefined, nichtGeprueftGrundEnabled: boolean): void {

    const nichtGeprueftIsChecked = data?.audiogrammBeurteilung.value?.find(value => {
        return value === AudiogrammBeurteilung.NICHT_GEPRUEFT;
      },
    );
    // Audiogramm
    this.config.audiogrammForm.setValidators(config, !!nichtGeprueftIsChecked);
    // Audiogramm Beurteilung
    this.audiogrammBeurteilungValueEnhancedForm.setValidatorsForAnlassConfig(
      config.data.hoerenAudiogrammBeurteilung,
      nichtGeprueftGrundEnabled,
    );
    // Zusaetzliche Angaben
    this.zusaetzlicheAngabenValueEnhancedForm.setValidatorsForAnlassConfig(
      config.data.hoerenAudiogrammZusaetzlicheAngaben,
      nichtGeprueftGrundEnabled,
    );
    // Empfehlung
    this.empfehlungValueEnhancedForm.setValidatorsForAnlassConfig(
      config.data.hoerenAudiogrammEmpfehlung,
      nichtGeprueftGrundEnabled,
    );
    // Bemerkungen
    if (config.data.hoerenAudiogrammBemerkungen === RequirementSetting.REQUIRED) {
      this.group.controls.bemerkungen.addValidators(Validators.required);
    }

    this.audiogrammSimpelValueEnhancedForm.setValidatorsForAnlassConfig(
      config.data.hoerenAudiogrammSimpel,
      nichtGeprueftGrundEnabled
    );

    this.hoerhilfeValueEnhancedForm.setValidatorsForAnlassConfig(
      config.data.hoerenHoerhilfe,
      nichtGeprueftGrundEnabled
    );
  }
}
