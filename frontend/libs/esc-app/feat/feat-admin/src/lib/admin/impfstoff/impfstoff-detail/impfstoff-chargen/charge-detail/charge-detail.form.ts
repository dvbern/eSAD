import {DestroyRef} from '@angular/core';
import {NonNullableFormBuilder, ValidatorFn, Validators} from '@angular/forms';
import {ImpfstoffChargeSaveJson} from '@dvb/data-access-backend';
import {AppValidatorKeys, AppValidators, BaseForm} from '@dvb/util-angular';
import {ensureProps, requireFilledIn} from '@dvb/util-shared';
import {ChargenStore} from '../chargen.store';

type Config = {
  fb: NonNullableFormBuilder,
  destroyRef: DestroyRef,
  chargenStore: ChargenStore,
}

function makeForm(config: Config) {
  const { fb } = config;
  return fb.group({
    aktiv: fb.control<boolean | undefined>(undefined, [Validators.required]),
    name: fb.control<string>('', [Validators.required, AppValidators.dbMaxLength(), uniqueCharge(config.chargenStore)]),
  });
}

export class ChargeDetailForm
  extends BaseForm<ReturnType<typeof makeForm>, ImpfstoffChargeSaveJson> {
  constructor(config: Config) {
    super(() => makeForm(config));
  }

  valueOnValid(): ImpfstoffChargeSaveJson {
    const value = this.group.getRawValue();
    return ensureProps<ImpfstoffChargeSaveJson>({
      name: value.name,
      aktiv: requireFilledIn(value.aktiv),
    });
  }
}

function uniqueCharge(chargenStore: ChargenStore): ValidatorFn {

  return control => {
    const existingChargen = chargenStore.entities();
    const editedId = chargenStore.selectedEntityId();
    const uniqueViolated = existingChargen
      .filter(impfstoff => impfstoff.id !== editedId)
      .some(charge => charge.data.name.toLowerCase() === control.value?.toLowerCase(),
      );

    if (!uniqueViolated) {
      return null;
    }
    return {
      [AppValidatorKeys.unique]: {},
    }
  };
}
