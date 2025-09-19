import {DestroyRef} from '@angular/core';
import {NonNullableFormBuilder, ValidatorFn, Validators} from '@angular/forms';
import {ImpfstoffChargeSaveJson} from '@dvb/data-access-backend';
import {globalConstants} from '@dvb/feat-shared';
import {AppValidatorKeys, AppValidators, BaseForm} from '@dvb/util-angular';
import {ensureProps, Nullish, requireFilledIn} from '@dvb/util-shared';
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
    version: fb.control<number | Nullish>({value: undefined, disabled: true}),
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
      version: value.version ?? globalConstants.entityPrePersistVersion,
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
