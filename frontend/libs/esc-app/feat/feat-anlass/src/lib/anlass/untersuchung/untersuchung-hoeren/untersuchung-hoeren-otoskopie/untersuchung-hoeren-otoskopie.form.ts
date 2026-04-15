import {DestroyRef} from '@angular/core';
import {NonNullableFormBuilder, Validators} from '@angular/forms';
import {
  AnlassConfigJson,
  OtoskopieResultat,
  RequirementSetting,
  UntersuchungHoerenOtoskopieSaveJson,
} from '@dvb/data-access-backend';
import {globalConstants, ValueEnhancedForm} from '@dvb/feat-shared';
import {AppValidators, BaseForm} from '@dvb/util-angular';
import {ensurePropsFlat, Nullish, requireValue, trimToUndefined} from '@dvb/util-shared';
import {checkIfArrayContainsCheckedValue} from '../../untersuchung.util';

type Config = {
  fb: NonNullableFormBuilder,
  destroyRef: DestroyRef,
  otoskopieRechtsValueEnhancedForm: ValueEnhancedForm;
  otoskopieLinksValueEnhancedForm: ValueEnhancedForm;
}

function makeForm(config: Config) {
  const fb = config.fb;
  return fb.group({
    otoskopieRechts: config.otoskopieRechtsValueEnhancedForm.group,
    otoskopieRechtsResultatAndere: fb.control<string>('', AppValidators.textareaMaxLength2000()),
    otoskopieLinks: config.otoskopieLinksValueEnhancedForm.group,
    otoskopieLinksResultatAndere: fb.control<string>('', AppValidators.textareaMaxLength2000()),
    bemerkungen: fb.control<string>('', AppValidators.textareaMaxLength2000()),
    version: fb.control<number | Nullish>({value: undefined, disabled: true}),
  });
}

export class UntersuchungHoerenOtoskopieForm
  extends BaseForm<ReturnType<typeof makeForm>, UntersuchungHoerenOtoskopieSaveJson> {

  readonly otoskopieRechtsValueEnhancedForm: ValueEnhancedForm;
  readonly otoskopieLinksValueEnhancedForm: ValueEnhancedForm;

  constructor(protected config: Config) {
    super(() => makeForm(config));
    this.otoskopieRechtsValueEnhancedForm = config.otoskopieRechtsValueEnhancedForm;
    this.otoskopieLinksValueEnhancedForm = config.otoskopieLinksValueEnhancedForm;
  }

  resetForm(value: UntersuchungHoerenOtoskopieSaveJson | undefined): void {
    super.reset(value);
    this.otoskopieRechtsValueEnhancedForm.resetForm(value?.otoskopieRechts);
    this.otoskopieLinksValueEnhancedForm.resetForm(value?.otoskopieLinks);
  }

  public override valueOnValid(): UntersuchungHoerenOtoskopieSaveJson {
    const value = requireValue(this.group.getRawValue());
    return ensurePropsFlat<UntersuchungHoerenOtoskopieSaveJson>({
      otoskopieRechts: this.otoskopieRechtsValueEnhancedForm.valueOnValid(),
      otoskopieRechtsResultatAndere: value.otoskopieRechtsResultatAndere,
      otoskopieLinks: this.otoskopieLinksValueEnhancedForm.valueOnValid(),
      otoskopieLinksResultatAndere: value.otoskopieLinksResultatAndere,
      bemerkungen: trimToUndefined(value.bemerkungen),
      version: value.version ?? globalConstants.entityPrePersistVersion,
    });
  }

  setValidators(config: AnlassConfigJson, nichtGeprueftGrundEnabled: boolean): void {
    // Otoskopie
    this.otoskopieRechtsValueEnhancedForm.setValidatorsForAnlassConfig(
      config.data.hoerenOtoskopie,
      nichtGeprueftGrundEnabled,
    );
    this.otoskopieLinksValueEnhancedForm.setValidatorsForAnlassConfig(
      config.data.hoerenOtoskopie,
      nichtGeprueftGrundEnabled,
    );

    if (config.data.hoerenOtoskopie === RequirementSetting.REQUIRED) {
      this.group.controls.otoskopieRechtsResultatAndere.addValidators(AppValidators.requiredIfValidator(() => {
        return this.isHoerenOtroskopieRechtsResultatAndereChecked()
      }));
      this.group.controls.otoskopieLinksResultatAndere.addValidators(AppValidators.requiredIfValidator(() => {
        return this.isHoerenOtroskopieLinksResultatAndereChecked();
      }));
    }
    // Bemerkungen
    if (config.data.hoerenOtoskopieBemerkungen === RequirementSetting.REQUIRED) {
      this.group.controls.bemerkungen.addValidators(Validators.required);
    }
  }

  public isHoerenOtroskopieRechtsResultatAndereChecked(): boolean {
    return checkIfArrayContainsCheckedValue(this.otoskopieRechtsValueEnhancedForm.group.controls.valueArray.controls,
      [OtoskopieResultat.ANDERES]);
  }

  public isHoerenOtroskopieLinksResultatAndereChecked(): boolean {
    return checkIfArrayContainsCheckedValue(this.otoskopieLinksValueEnhancedForm.group.controls.valueArray.controls,
      [OtoskopieResultat.ANDERES]);
  }
}
