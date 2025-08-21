import {NonNullableFormBuilder, ValidatorFn, Validators} from '@angular/forms';
import {LabelSaveJson, LabelTyp} from '@dvb/data-access-backend';
import {LabelsStore} from '@dvb/feat-shared';
import {TenantSettingsStore} from '@dvb/feat-shared';
import {AppValidatorKeys, AppValidators, BaseForm} from '@dvb/util-angular';
import {ensureProps, isEqualCaseInsensitive, Nullish, requireValue} from '@dvb/util-shared';

type Config = {
  fb: NonNullableFormBuilder;
  tenantSettingsStore: TenantSettingsStore;
  labelsStore: LabelsStore;
};

function makeForm(config: Config) {
  const fb = config.fb;
  const maxLength = AppValidators.dbMaxLength();
  const group = fb.group({
    name: fb.group({
      de: fb.control<string | Nullish>(undefined),
      fr: fb.control<string | Nullish>(undefined),
    }),
    labelTyp: fb.control<LabelTyp | Nullish>(undefined, [Validators.required]),
    autoLabel: fb.control<boolean>(false),
    disabled: fb.control<boolean>(false),
    aktiv: fb.control<boolean>(true),
    version: fb.control<number | Nullish>({value: undefined, disabled: true}),
  });
  group.setValidators([uniqueLabel(config.labelsStore)]); // checks if label is unique
  if (config.tenantSettingsStore.supportsLanguageDe()) {
    group.controls.name.controls.de.setValidators([Validators.required, maxLength]);
  }
  if (config.tenantSettingsStore.supportsLanguageFr()) {
    group.controls.name.controls.fr.setValidators([Validators.required, maxLength]);
  }
  group.controls.autoLabel.disable();
  return group;
}

export class LabelDetailForm extends BaseForm<ReturnType<typeof makeForm>, LabelSaveJson> {
  constructor(config: Config) {
    super(() => makeForm(config));
  }

  override valueOnValid(): LabelSaveJson {
    const valuesFromForm = this.group.getRawValue();
    const nameDe = valuesFromForm.name.de ?? '';
    const nameFr = valuesFromForm.name.fr ?? '';
    return ensureProps<LabelSaveJson>({
      name: {
        de: nameDe,
        fr: nameFr,
      },
      labelTyp: requireValue(valuesFromForm.labelTyp),
      autoLabel: valuesFromForm.autoLabel,
      disabled: valuesFromForm.disabled,
      aktiv: valuesFromForm.aktiv,
      version: valuesFromForm.version ?? 0,
    });
  }
}

function uniqueLabel(labelStore: LabelsStore): ValidatorFn {
  return control => {
    const existingLabels = labelStore.entities();
    const editedId = labelStore.selectedEntityId();
    const uniqueViolated = existingLabels
      .filter(label => label.id !== editedId) // editing a label is allowed
      .map(label => label.data)
      .some(label => (isEqualCaseInsensitive(label.name.de, control.value.name.de)
          || isEqualCaseInsensitive(label.name.fr, control.value.name.fr))
        && label.labelTyp === control.value.labelTyp,
      );
    if (!uniqueViolated) {
      return null;
    }
    return {
      [AppValidatorKeys.unique]: {},
    }
  };
}
