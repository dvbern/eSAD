import {NonNullableFormBuilder, Validators} from '@angular/forms';
import {BenutzerOptionJson, EntityAktivStatus, QuartierOptionJson, SchuleSaveJson} from '@dvb/data-access-backend';
import {AdresseForm} from '@dvb/feat-shared';
import {AppValidators, BaseForm} from '@dvb/util-angular';
import {ensurePropsFlat, requireValue, trimToUndefined} from '@dvb/util-shared';

type Config = {
  fb: NonNullableFormBuilder;
  adresseForm: AdresseForm;
}

function makeForm(config: Config) {
  const fb = config.fb;
  const maxLength = AppValidators.dbMaxLength();

  return fb.group({
    schulhaus: fb.control<string>('', [Validators.required, maxLength]),
    schulstandort: fb.control<string | undefined>(undefined, [maxLength]),
    schulkreis: fb.control<string | undefined>(undefined, [maxLength]),
    quartier: fb.control<QuartierOptionJson | undefined>(undefined, []),
    schulhausnummer: fb.control<string | undefined>(undefined, [
      AppValidators.numberOrNumericChars(),
      Validators.maxLength(3),
    ]),
    adresse: config.adresseForm.group,
    email: fb.control<string | undefined>(undefined, [maxLength, AppValidators.emailValidator()]),
    telefon: fb.control<string | undefined>(undefined, [maxLength, AppValidators.phoneValidator()]),
    bemerkungen: fb.control<string | undefined>(undefined, maxLength),
    zustaendigArzt: fb.control<BenutzerOptionJson | undefined>(undefined, []),
    zustaendigMPA: fb.control<BenutzerOptionJson | undefined>(undefined, []),
    schuleAktivStatus: fb.control<EntityAktivStatus>(EntityAktivStatus.AKTIV, Validators.required),
  });
}

export class SchulenStammdatenForm extends BaseForm<ReturnType<typeof makeForm>, SchuleSaveJson> {
  readonly adresseForm: AdresseForm;

  constructor(config: Config) {
    super(() => makeForm(config));
    this.adresseForm = config.adresseForm;
  }

  override valueOnValid(): SchuleSaveJson {
    const value = requireValue(this.group.getRawValue());
    // TODO ESC-322 validate plz and ort belong together
    return ensurePropsFlat<SchuleSaveJson>({
      schulhaus: requireValue(value.schulhaus),
      schulstandort: trimToUndefined(value.schulstandort),
      schulkreis: trimToUndefined(value.schulkreis),
      quartier: value.quartier,
      schulhausnummer: trimToUndefined(value.schulhausnummer),
      adresse: this.adresseForm.valueOnValid(),
      email: trimToUndefined(value.email),
      telefon: trimToUndefined(value.telefon),
      bemerkungen: trimToUndefined(value.bemerkungen),
      zustaendigArzt: value.zustaendigArzt,
      zustaendigMPA: value.zustaendigMPA,
      schuleAktivStatus: requireValue(value.schuleAktivStatus),
    });
  }
}
