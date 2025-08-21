import {DestroyRef} from '@angular/core';
import {NonNullableFormBuilder, Validators} from '@angular/forms';
import {
    AnlassConfigJson,
    OtoskopieResultat,
    RequirementSetting,
    UntersuchungHoerenOtoskopieSaveJson,
} from '@dvb/data-access-backend';
import {AppValidators, BaseForm} from '@dvb/util-angular';
import {ensureProps, Nullish, requireValue, trimToUndefined} from '@dvb/util-shared';
import {checkIfArrayContainsCheckedValue, markAllMultiCheckboxesAsTouched} from '../../untersuchung.util';

type Config = {
  fb: NonNullableFormBuilder,
  destroyRef: DestroyRef,
}

function makeOtoskopieResultatSubForm(fb: NonNullableFormBuilder, value: OtoskopieResultat) {
  return fb.group({
    resultat: fb.control<OtoskopieResultat>({value, disabled: true}),
    isChecked: fb.control<boolean>(false),
  })
}

export type OtoskopieResultatSubFormType = ReturnType<typeof makeOtoskopieResultatSubForm>

function makeForm(config: Config) {
  const fb = config.fb;
  const group = fb.group({
    otoskopieRechtsArray: fb.array<OtoskopieResultatSubFormType>([]),
    otoskopieRechtsResultatAndere: fb.control<string>('', AppValidators.textareaMaxLength()),
    otoskopieLinksArray: fb.array<OtoskopieResultatSubFormType>([]),
    otoskopieLinksResultatAndere: fb.control<string>('', AppValidators.textareaMaxLength()),
    bemerkungen: fb.control<string>('', AppValidators.textareaMaxLength()),
  });

  // Multi-Checkboxen: Wenn eines touched ist aber keines ausgewählt, müssen alle rot werden
  markAllMultiCheckboxesAsTouched(group.controls.otoskopieRechtsArray, config.destroyRef);
  markAllMultiCheckboxesAsTouched(group.controls.otoskopieLinksArray, config.destroyRef);

  return group;
}

export class UntersuchungHoerenOtoskopieForm
    extends BaseForm<ReturnType<typeof makeForm>, UntersuchungHoerenOtoskopieSaveJson> {

  constructor(protected config: Config) {
    super(() => makeForm(config));
  }

  resetForm(value: UntersuchungHoerenOtoskopieSaveJson | Nullish): void {
    this.group.reset({...value});
    this.resetOtoskopieRechts(value);
    this.resetOtoskopieLinks(value);
  }

  private resetOtoskopieRechts(value: UntersuchungHoerenOtoskopieSaveJson | Nullish): void {
    this.group.controls.otoskopieRechtsArray.clear();
    for (const resultat of Object.values(OtoskopieResultat)) {
      const subForm = makeOtoskopieResultatSubForm(this.config.fb, resultat);
      subForm.controls.isChecked.reset(!!value?.otoskopieRechts?.find(fv => fv === resultat));
      this.group.controls.otoskopieRechtsArray.push(subForm)
    }
  }

  private resetOtoskopieLinks(value: UntersuchungHoerenOtoskopieSaveJson | Nullish): void {
    this.group.controls.otoskopieLinksArray.clear();
    for (const resultat of Object.values(OtoskopieResultat)) {
      const subForm = makeOtoskopieResultatSubForm(this.config.fb, resultat);
      subForm.controls.isChecked.reset(!!value?.otoskopieLinks?.find(fv => fv === resultat));
      this.group.controls.otoskopieLinksArray.push(subForm)
    }
  }

  public override valueOnValid(): UntersuchungHoerenOtoskopieSaveJson {
    const value = requireValue(this.group.getRawValue());
    return ensureProps<UntersuchungHoerenOtoskopieSaveJson>({
      otoskopieRechts: value.otoskopieRechtsArray.filter(i => i.isChecked).map(i => i.resultat),
      otoskopieRechtsResultatAndere: value.otoskopieRechtsResultatAndere,
      otoskopieLinks: value.otoskopieLinksArray.filter(i => i.isChecked).map(i => i.resultat),
      otoskopieLinksResultatAndere: value.otoskopieLinksResultatAndere,
      bemerkungen: trimToUndefined(value.bemerkungen),
    });
  }

  setValidators(config: AnlassConfigJson): void {
    // Otoskopie
    if (config.hoerenOtoskopie === RequirementSetting.REQUIRED) {
      this.group.controls.otoskopieRechtsArray.addValidators(AppValidators.atLeastOneCheckedValidator());
      this.group.controls.otoskopieRechtsResultatAndere.addValidators(AppValidators.requiredIfValidator(() => {
        return this.isHoerenOtroskopieRechtsResultatAndereChecked()
      }));
      this.group.controls.otoskopieLinksArray.addValidators(AppValidators.atLeastOneCheckedValidator());
      this.group.controls.otoskopieLinksResultatAndere.addValidators(AppValidators.requiredIfValidator(() => {
        return this.isHoerenOtroskopieLinksResultatAndereChecked();
      }));
    }
    // Bemerkungen
    if (config.hoerenOtoskopieBemerkungen === RequirementSetting.REQUIRED) {
      this.group.controls.bemerkungen.addValidators(Validators.required);
    }
  }

  public isHoerenOtroskopieRechtsResultatAndereChecked(): boolean {
    return checkIfArrayContainsCheckedValue(this.group.controls.otoskopieRechtsArray.controls,
        [OtoskopieResultat.ANDERES]);
  }

  public isHoerenOtroskopieLinksResultatAndereChecked(): boolean {
    return checkIfArrayContainsCheckedValue(this.group.controls.otoskopieLinksArray.controls,
        [OtoskopieResultat.ANDERES]);
  }
}
