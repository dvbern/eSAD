import {NonNullableFormBuilder, ValidatorFn, Validators} from '@angular/forms';
import {EntityAktivStatus, ImpfstoffSaveJson, Krankheit} from '@dvb/data-access-backend';
import {globalConstants, ImpfstoffeChargenStore} from '@dvb/feat-shared';
import {AppValidatorKeys, AppValidators, BaseForm} from '@dvb/util-angular';
import {ensureProps, Nullish, requireValue} from '@dvb/util-shared';

type Config = {
  fb: NonNullableFormBuilder;
  impfstoffeChargenStore: ImpfstoffeChargenStore
};

function makeForm(config: Config) {
  const fb = config.fb;

  return fb.group({
    name: fb.control<string>('',
      [Validators.required, AppValidators.dbMaxLength(), uniqueImpfstoff(config.impfstoffeChargenStore)]),
    krankheiten: fb.control<Krankheit[]>([], [Validators.required]),
    impfstoffAktivStatus: fb.control<EntityAktivStatus>(EntityAktivStatus.AKTIV, Validators.required),
    version: fb.control<number | Nullish>({value: undefined, disabled: true}),
  });
}

export class ImpfstoffDatenForm extends BaseForm<ReturnType<typeof makeForm>, ImpfstoffSaveJson> {
  constructor(config: Config) {
    super(() => makeForm(config));
  }

  override valueOnValid(): ImpfstoffSaveJson {
    const value = this.group.getRawValue();
    const name = value.name;

    return ensureProps<ImpfstoffSaveJson>({
      name: requireValue(name),
      krankheiten: value.krankheiten,
      impfstoffAktivStatus: requireValue(value.impfstoffAktivStatus),
      version: value.version ?? globalConstants.entityPrePersistVersion,
    });
  }
}

function uniqueImpfstoff(impfstoffeChargenStore: ImpfstoffeChargenStore): ValidatorFn {

  return control => {
    const existingImpfstoff = impfstoffeChargenStore.entities();
    const editedId = impfstoffeChargenStore.selectedEntityId();
    const uniqueViolated = existingImpfstoff
      .filter(impfstoff => impfstoff.id !== editedId)
      .some(impfstoff => impfstoff.data.name.toLowerCase() === control.value?.toLowerCase(),
      );

    if (!uniqueViolated) {
      return null;
    }
    return {
      [AppValidatorKeys.unique]: {},
    }
  };
}

