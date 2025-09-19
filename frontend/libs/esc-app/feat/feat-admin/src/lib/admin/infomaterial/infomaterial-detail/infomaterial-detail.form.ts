import {NonNullableFormBuilder, ValidatorFn, Validators} from '@angular/forms';
import {InfomaterialSaveJson} from '@dvb/data-access-backend';
import {globalConstants, InfomaterialStore, TenantSettingsStore} from '@dvb/feat-shared';
import {AppValidatorKeys, AppValidators, BaseForm} from '@dvb/util-angular';
import {ensureProps, isEqualCaseInsensitive, Nullish} from '@dvb/util-shared';

type Config = {
  fb: NonNullableFormBuilder;
  tenantSettingsStore: TenantSettingsStore;
  infomaterialStore: InfomaterialStore;
}

function makeForm(config: Config) {
  const fb = config.fb;
  const maxLength = AppValidators.dbMaxLength();
  const group = fb.group({
    name: fb.group({
      de: fb.control<string | Nullish>(undefined),
      fr: fb.control<string | Nullish>(undefined),
    }),
    aktiv: fb.control<boolean>(true),
    version: fb.control<number | Nullish>({value: undefined, disabled: true}),
  });
  group.setValidators([uniqueInfomaterial(config.infomaterialStore)]);
  if (config.tenantSettingsStore.supportsLanguageDe()) {
    group.controls.name.controls.de.setValidators([Validators.required, maxLength]);
  }
  if (config.tenantSettingsStore.supportsLanguageFr()) {
    group.controls.name.controls.fr.setValidators([Validators.required, maxLength]);
  }
  return group;
}

export class InfomaterialForm extends BaseForm<ReturnType<typeof makeForm>, InfomaterialSaveJson> {
  constructor(config: Config) {
    super(() => makeForm(config));
  }

  override valueOnValid(): InfomaterialSaveJson {
    const valuesFromForm = this.group.getRawValue();
    const nameDe = valuesFromForm.name.de ?? '';
    const nameFr = valuesFromForm.name.fr ?? '';
    return ensureProps<InfomaterialSaveJson>({
      name: {
        de: nameDe,
        fr: nameFr,
      },
      aktiv: valuesFromForm.aktiv,
      version: valuesFromForm.version ?? globalConstants.entityPrePersistVersion,
    });
  }
}

function uniqueInfomaterial(infomaterialStore: InfomaterialStore): ValidatorFn {
  return control => {
    const existingInfomaterial = infomaterialStore.entities();
    const editedId = infomaterialStore.selectedEntityId();
    const uniqueViolated = existingInfomaterial
      .filter(infomaterial => infomaterial.id !== editedId)
      .map(infomaterial => infomaterial.data)
      .some(infomaterial => (isEqualCaseInsensitive(infomaterial.name.de, control.value.name.de)
          || isEqualCaseInsensitive(infomaterial.name.fr, control.value.name.fr)));

    if (!uniqueViolated) {
      return null;
    }
    return {
      [AppValidatorKeys.unique]: {},
    }
  };
}
