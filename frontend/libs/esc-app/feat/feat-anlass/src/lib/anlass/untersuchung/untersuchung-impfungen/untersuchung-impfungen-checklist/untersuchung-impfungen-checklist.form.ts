import {DestroyRef} from '@angular/core';
import {NonNullableFormBuilder, Validators} from '@angular/forms';
import {AnlassConfigJson, RequirementSetting, UntersuchungImpfungenChecklisteSaveJson} from '@dvb/data-access-backend';
import {AppValidators, BaseForm, DetailMode} from '@dvb/util-angular';
import {ensureProps, requireValue} from '@dvb/util-shared';

type Config = {
  fb: NonNullableFormBuilder;
  destroyRef: DestroyRef;
};

function makeForm(config: Config) {
  const fb = config.fb;
  return fb.group({
    checkEinwilligungVorhanden: fb.control(false),
    checkKontraindikationenAbgeklaert: fb.control(false),
    checkDoppelkontrolleImpfentscheidDurchgefuehrt: fb.control(false),
    bemerkungen: fb.control<string>('', AppValidators.textareaMaxLength()),
  });
}

export class UntersuchungImpfungenChecklistForm extends BaseForm<
  ReturnType<typeof makeForm>,
  UntersuchungImpfungenChecklisteSaveJson
> {
  constructor(protected config: Config) {
    super(() => makeForm(config));
  }

  resetForm(value: UntersuchungImpfungenChecklisteSaveJson | undefined): void {
    super.reset(value, DetailMode.EDIT);
  }

  setValidatorsForAnlassConfig(config: AnlassConfigJson): void {
    if (config.checkEinwilligungVorhanden === RequirementSetting.REQUIRED) {
      this.group.controls.checkEinwilligungVorhanden.addValidators(Validators.requiredTrue);
    }
    if (config.checkKontraindikationenAbgeklaert === RequirementSetting.REQUIRED) {
      this.group.controls.checkKontraindikationenAbgeklaert.addValidators(Validators.requiredTrue);
    }
    if (config.checkDoppelkontrolleImpfentscheidDurchgefuehrt === RequirementSetting.REQUIRED) {
      this.group.controls.checkDoppelkontrolleImpfentscheidDurchgefuehrt.addValidators(Validators.requiredTrue);
    }
    if (config.untersuchungImpfungBemerkungen === RequirementSetting.REQUIRED) {
      this.group.controls.bemerkungen.addValidators(Validators.required);
    }
  }

  override valueOnValid(): UntersuchungImpfungenChecklisteSaveJson {
    const value = requireValue(this.group.getRawValue());
    return ensureProps<UntersuchungImpfungenChecklisteSaveJson>({
      checkEinwilligungVorhanden: value.checkEinwilligungVorhanden,
      checkKontraindikationenAbgeklaert: value.checkKontraindikationenAbgeklaert,
      checkDoppelkontrolleImpfentscheidDurchgefuehrt: value.checkDoppelkontrolleImpfentscheidDurchgefuehrt,
      bemerkungen: value.bemerkungen,
    });
  }
}
