import { DestroyRef } from '@angular/core';
import { NonNullableFormBuilder, Validators } from '@angular/forms';
import {
  AnlassConfigJson,
  RequirementSetting,
  UntersuchungSpracheSaveJson,
} from '@dvb/data-access-backend';
import { globalConstants, ValueEnhancedForm } from '@dvb/feat-shared';
import { AppValidators, BaseForm } from '@dvb/util-angular';
import { ensurePropsFlat, Nullish, requireValue, trimToUndefined } from '@dvb/util-shared';

type Config = {
  fb: NonNullableFormBuilder;
  nachsprechenValueEnhancedForm: ValueEnhancedForm;
  wortschatzValueEnhancedForm: ValueEnhancedForm;
  sprachverstaendnisValueEnhancedForm: ValueEnhancedForm;
  destroyRef: DestroyRef;
};

function makeForm(config: Config) {
  const fb = config.fb;
  return fb.group({
    nachsprechen: config.nachsprechenValueEnhancedForm.group,
    wortschatz: config.wortschatzValueEnhancedForm.group,
    sprachverstaendnis: config.sprachverstaendnisValueEnhancedForm.group,
    bemerkungen: fb.control<string>('', AppValidators.textareaMaxLength2000()),
    version: fb.control<number | Nullish>({ value: undefined, disabled: true }),
  });
}

export class UntersuchungSpracheForm extends BaseForm<ReturnType<typeof makeForm>, UntersuchungSpracheSaveJson> {
  readonly nachsprechenSingleValueForm: ValueEnhancedForm;
  readonly wortschatzSingleValueForm: ValueEnhancedForm;
  readonly sprachverstaendnisSingleValueForm: ValueEnhancedForm;
  constructor(protected config: Config) {
    super(() => makeForm(config));
    this.nachsprechenSingleValueForm = config.nachsprechenValueEnhancedForm;
    this.wortschatzSingleValueForm = config.wortschatzValueEnhancedForm;
    this.sprachverstaendnisSingleValueForm = config.sprachverstaendnisValueEnhancedForm;
  }

  resetForm(value: UntersuchungSpracheSaveJson | undefined): void {
    super.reset(value);
    this.nachsprechenSingleValueForm.resetForm(value?.nachsprechen);
    this.wortschatzSingleValueForm.resetForm(value?.wortschatz);
    this.sprachverstaendnisSingleValueForm.resetForm(value?.sprachverstaendnis);
  }

  public override valueOnValid(): UntersuchungSpracheSaveJson {
    const value = requireValue(this.group.getRawValue());
    return ensurePropsFlat<UntersuchungSpracheSaveJson>({
      nachsprechen: this.nachsprechenSingleValueForm.valueOnValid(),
      wortschatz: this.wortschatzSingleValueForm.valueOnValid(),
      sprachverstaendnis: this.sprachverstaendnisSingleValueForm.valueOnValid(),
      bemerkungen: trimToUndefined(value.bemerkungen),
      version: value.version ?? globalConstants.entityPrePersistVersion,
    });
  }

  setValidatorsForAnlassConfig(config: AnlassConfigJson, nichtGeprueftGrundEnabled: boolean): void {
    this.nachsprechenSingleValueForm.setValidatorsForAnlassConfig(
      config.data.untersuchungSpracheNachsprechen,
      nichtGeprueftGrundEnabled,
    );
    this.wortschatzSingleValueForm.setValidatorsForAnlassConfig(
      config.data.untersuchungSpracheWortschatz,
      nichtGeprueftGrundEnabled,
    );
    this.sprachverstaendnisSingleValueForm.setValidatorsForAnlassConfig(
      config.data.untersuchungSpracheSprachverstaendnis,
      nichtGeprueftGrundEnabled,
    );
    if (config.data.untersuchungSpracheBemerkungen === RequirementSetting.REQUIRED) {
      this.group.controls.bemerkungen.addValidators(Validators.required);
    }
  }
}
