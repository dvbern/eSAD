import { NonNullableFormBuilder, Validators } from '@angular/forms';
import { NichtGeprueftGrund, RequirementSetting, ValueEnhancedJson } from '@dvb/data-access-backend';
import { AppValidators, BaseForm } from '@dvb/util-angular';
import {ensurePropsFlat, isNotNullish, Nullish, requireValue, trimToUndefined} from '@dvb/util-shared';
import { DestroyRef } from '@angular/core';
import { checkIfArrayContainsCheckedValue, markAllMultiCheckboxesAsTouched, SELECT_TYPE } from './value-enhanced.util';

type Config = {
  fb: NonNullableFormBuilder;
  options: string[];
  destroyRef: DestroyRef;
  selectionType: SELECT_TYPE; // Dies koennte spaeter aus der AnlassConfig kommen
};

export type MultiValueSubFormType = ReturnType<typeof makeMultiValueSubForm>;

function makeForm(config: Config) {
  const group = config.fb.group({
    value: config.fb.control<string | undefined>(undefined), // single
    valueArray: config.fb.array<MultiValueSubFormType>([]), // multi
    nichtGeprueftGrund: config.fb.control<NichtGeprueftGrund | undefined>(undefined),
  });
  // Multi-Checkboxen: Wenn eines touched ist aber keines ausgewählt, müssen alle rot werden
  markAllMultiCheckboxesAsTouched(group.controls.valueArray, config.destroyRef);
  return group;
}

function makeMultiValueSubForm(fb: NonNullableFormBuilder, value: string) {
  return fb.group({
    resultat: fb.control<string>({ value, disabled: true }),
    isChecked: fb.control<boolean>(false),
  });
}

export class ValueEnhancedForm extends BaseForm<ReturnType<typeof makeForm>, ValueEnhancedJson> {
  constructor(protected config: Config) {
    super(() => makeForm(config));
  }

  resetForm(value: ValueEnhancedJson | Nullish): void {
    if (this.config.selectionType === 'single') {
      const valueToResetTo = value?.value?.length === 1 ? value.value[0] : undefined;
      this.group.controls.value.reset(valueToResetTo);
    } else {
      this.group.controls.value.reset(undefined);
      this.resetMultiValueForm(value);
    }

    if (this.isNichtGeprueftChecked()) {
      this.group.controls.nichtGeprueftGrund.reset(value?.nichtGeprueftGrund);
    } else {
      this.group.controls.nichtGeprueftGrund.reset(undefined);
    }
  }

  private resetMultiValueForm(value: ValueEnhancedJson | Nullish): void {
    this.group.controls.valueArray.clear();
    for (const resultat of this.config.options) {
      const subForm = makeMultiValueSubForm(this.config.fb, resultat);
      subForm.controls.isChecked.reset(!!value?.value?.find(fv => fv === resultat)); //checked if value is present
      this.group.controls.valueArray.push(subForm);
    }
  }

  override valueOnValid(): ValueEnhancedJson {
    const rawValue = requireValue(this.group.getRawValue());
    const singleValue = rawValue.value;
    const multiValue = rawValue.valueArray.filter(i => i.isChecked).map(i => i.resultat);
    const allValues: string[] = [...new Set([...multiValue, singleValue].filter((s): s is string => isNotNullish(s)))];
    return ensurePropsFlat<ValueEnhancedJson>({
      value: allValues,
      nichtGeprueftGrund: trimToUndefined(rawValue.nichtGeprueftGrund),
    });
  }

  public setValidatorsForAnlassConfig(
    requirementSetting: RequirementSetting,
    nichtGeprueftGrundEnabled: boolean,
  ): void {
    if (requirementSetting === RequirementSetting.REQUIRED) {
      if (this.config.selectionType === 'single') {
        this.group.controls.value.setValidators(Validators.required);
      } else {
        this.group.controls.valueArray.addValidators(AppValidators.atLeastOneCheckedValidator());
      }
      if (nichtGeprueftGrundEnabled) {
        this.group.controls.nichtGeprueftGrund.addValidators(
          AppValidators.requiredIfValidator(() => {
            return this.isNichtGeprueftChecked();
          }),
        );
      }
    }
  }

  private isNichtGeprueftCheckedSingle(): boolean {
    return this.group.controls.value.value === 'NICHT_GEPRUEFT';
  }

  private isNichtGeprueftCheckedMulti(): boolean {
    return checkIfArrayContainsCheckedValue(this.group.controls.valueArray.controls, ['NICHT_GEPRUEFT']);
  }

  public isNichtGeprueftChecked(): boolean {
    return this.isNichtGeprueftCheckedSingle() || this.isNichtGeprueftCheckedMulti();
  }

  public getSelectionType(): SELECT_TYPE {
    return this.config.selectionType;
  }

  public getOptions(): string[] {
    return this.config.options;
  }
}
