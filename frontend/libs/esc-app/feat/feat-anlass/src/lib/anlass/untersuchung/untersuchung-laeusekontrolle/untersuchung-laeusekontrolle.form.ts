import { DestroyRef } from '@angular/core';
import { NonNullableFormBuilder, Validators } from '@angular/forms';
import {
  AnlassConfigJson,
  LaeusekontrolleBeurteilung,
  RequirementSetting,
  UntersuchungLaeusekontrolleSaveJson,
} from '@dvb/data-access-backend';
import { globalConstants } from '@dvb/feat-shared';
import { AppValidators, BaseForm } from '@dvb/util-angular';
import { ensurePropsFlat, Nullish, requireValue } from '@dvb/util-shared';
import { markAllMultiCheckboxesAsTouched } from '../untersuchung.util';

type Config = {
  fb: NonNullableFormBuilder;
  destroyRef: DestroyRef;
};

function makeLaeusekontrolleBeurteilungSubForm(fb: NonNullableFormBuilder, value: LaeusekontrolleBeurteilung) {
  return fb.group({
    resultat: fb.control<LaeusekontrolleBeurteilung>({ value, disabled: true }),
    isChecked: fb.control<boolean>(false),
  });
}

export type LaeusekontrolleBeurteilungSubFormType = ReturnType<typeof makeLaeusekontrolleBeurteilungSubForm>;

function makeForm(config: Config) {
  const fb = config.fb;
  const group = fb.group({
    beurteilungArray: fb.array<LaeusekontrolleBeurteilungSubFormType>([]),
    bemerkungen: fb.control<string>('', AppValidators.textareaMaxLength2000()),
    version: fb.control<number | Nullish>({ value: undefined, disabled: true }),
  });
  // Multi-Checkboxen: Wenn eines touched ist aber keines ausgewählt, müssen alle rot werden
  markAllMultiCheckboxesAsTouched(group.controls.beurteilungArray, config.destroyRef);
  return group;
}

export class UntersuchungLaeusekontrolleForm extends BaseForm<
  ReturnType<typeof makeForm>,
  UntersuchungLaeusekontrolleSaveJson
> {
  constructor(protected config: Config) {
    super(() => makeForm(config));
  }

  resetForm(value: UntersuchungLaeusekontrolleSaveJson | Nullish): void {
    this.group.reset({ ...value });
    this.resetBeurteilung(value);
  }

  private resetBeurteilung(value: UntersuchungLaeusekontrolleSaveJson | Nullish): void {
    this.group.controls.beurteilungArray.clear();
    for (const resultat of Object.values(LaeusekontrolleBeurteilung)) {
      const subForm = makeLaeusekontrolleBeurteilungSubForm(this.config.fb, resultat);
      subForm.controls.isChecked.reset(!!value?.beurteilung?.find(fv => fv === resultat));
      this.group.controls.beurteilungArray.push(subForm);
    }
  }

  public override valueOnValid(): UntersuchungLaeusekontrolleSaveJson {
    const value = requireValue(this.group.getRawValue());
    return ensurePropsFlat<UntersuchungLaeusekontrolleSaveJson>({
      beurteilung: value.beurteilungArray.filter(i => i.isChecked).map(i => i.resultat),
      bemerkungen: value.bemerkungen,
      version: value.version ?? globalConstants.entityPrePersistVersion,
    });
  }

  setValidators(config: AnlassConfigJson): void {
    // Beurteilung
    if (config.data.laeusekontrolleBeurteilung === RequirementSetting.REQUIRED) {
      this.group.controls.beurteilungArray.addValidators(AppValidators.atLeastOneCheckedValidator());
    }
    // Bemerkungen
    if (config.data.laeusekontrolleBemerkungen === RequirementSetting.REQUIRED) {
      this.group.controls.bemerkungen.addValidators(Validators.required);
    }
  }
}
