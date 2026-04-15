import {NonNullableFormBuilder, Validators} from '@angular/forms';
import {DestroyRef} from '@angular/core';
import {
  AnlassConfigJson,
  RequirementSetting,
  UntersuchungBewegungsapparatSaveJson,
} from '@dvb/data-access-backend';
import {AppValidators, BaseForm} from '@dvb/util-angular';
import {ensurePropsFlat, Nullish, requireValue} from '@dvb/util-shared';
import {globalConstants, ValueEnhancedForm} from '@dvb/feat-shared';

type Config = {
  fb: NonNullableFormBuilder;
  skolioseValueEnhancedForm: ValueEnhancedForm;
  haltungsschwaecheValueEnhancedForm: ValueEnhancedForm;
  verkuerzungHamstringmuskulaturValueEnhancedForm: ValueEnhancedForm;
  destroyRef: DestroyRef;
};

function makeForm(config: Config) {
  const fb = config.fb;
  return fb.group({
    skoliose: config.skolioseValueEnhancedForm.group,
    haltungsschwaeche: config.haltungsschwaecheValueEnhancedForm.group,
    verkuerzungHamstringmuskulatur: config.verkuerzungHamstringmuskulaturValueEnhancedForm.group,
    bemerkungen: fb.control<string>('', AppValidators.textareaMaxLength2000()),
    version: fb.control<number | Nullish>({value: undefined, disabled: true}),
  })
}

export class UntersuchungBewegungsapparatForm extends BaseForm<
  ReturnType<typeof makeForm>,
  UntersuchungBewegungsapparatSaveJson
> {
  readonly skolioseSingleValueForm: ValueEnhancedForm;
  readonly haltungsschwaecheSingleValueForm: ValueEnhancedForm;
  readonly verkuerzungHamstringmuskulaturSingleValueForm: ValueEnhancedForm;

  constructor(protected config: Config) {
    super(() => makeForm(config));
    this.skolioseSingleValueForm = config.skolioseValueEnhancedForm;
    this.haltungsschwaecheSingleValueForm = config.haltungsschwaecheValueEnhancedForm;
    this.verkuerzungHamstringmuskulaturSingleValueForm = config.verkuerzungHamstringmuskulaturValueEnhancedForm;
  }

  resetForm(value: UntersuchungBewegungsapparatSaveJson | undefined): void {
    super.reset(value);
    this.skolioseSingleValueForm.resetForm(value?.skoliose);
    this.haltungsschwaecheSingleValueForm.resetForm(value?.haltungsschwaeche);
    this.verkuerzungHamstringmuskulaturSingleValueForm.resetForm(value?.verkuerzungHamstringmuskulatur);
  }

  valueOnValid(): UntersuchungBewegungsapparatSaveJson {
    const value = requireValue(this.group.getRawValue());
    return ensurePropsFlat<UntersuchungBewegungsapparatSaveJson>(
      {
        skoliose: this.skolioseSingleValueForm.valueOnValid(),
        haltungsschwaeche: this.haltungsschwaecheSingleValueForm.valueOnValid(),
        verkuerzungHamstringmuskulatur: this.verkuerzungHamstringmuskulaturSingleValueForm.valueOnValid(),
        bemerkungen: value.bemerkungen,
        version: value.version ?? globalConstants.entityPrePersistVersion
      }
    );
  }

  setValidators(config: AnlassConfigJson, nichtGeprueftGrundEnabled: boolean): void {
    // Skoliose
    this.skolioseSingleValueForm.setValidatorsForAnlassConfig(
      config.data.bewegungsapparatSkoliose,
      nichtGeprueftGrundEnabled,
    );

    // Haltungsschwaeche
    this.haltungsschwaecheSingleValueForm.setValidatorsForAnlassConfig(
      config.data.bewegungsapparatHaltungsschwaeche,
      nichtGeprueftGrundEnabled,
    );

    // Verkürzung Hamstringmuskulatur
    this.verkuerzungHamstringmuskulaturSingleValueForm.setValidatorsForAnlassConfig(
      config.data.bewegungsapparatVerkuerzungHamstringmuskulatur,
      nichtGeprueftGrundEnabled,
    );

    // Bemerkungen
    if (config.data.bewegungsapparatBemerkung === RequirementSetting.REQUIRED) {
      this.group.controls.bemerkungen.addValidators(Validators.required);
    }
  }
}
