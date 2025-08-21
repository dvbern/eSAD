import {NonNullableFormBuilder, Validators} from '@angular/forms';
import {Geschlecht, KontaktPersonSchuleFunktion, KontaktPersonSchuleSaveJson} from '@dvb/data-access-backend';
import {AppValidators, BaseForm} from '@dvb/util-angular';
import {ensureProps, requireValue} from '@dvb/util-shared';

type Config = {
  fb: NonNullableFormBuilder;
}

function makeForm(config: Config) {
  const fb = config.fb;
  const maxLength = AppValidators.dbMaxLength();

  return fb.group({
    name: fb.control<string>('', [Validators.required, maxLength]),
    vorname: fb.control<string | undefined>(undefined, [maxLength]),
    geschlecht: fb.control<Geschlecht | undefined>(undefined, [Validators.required]),
    funktion: fb.control<KontaktPersonSchuleFunktion | undefined>(undefined, [Validators.required]),
    telefon: fb.control<string | undefined>(undefined, [AppValidators.phoneValidator(), maxLength]),
    email: fb.control<string | undefined>(undefined, [AppValidators.emailValidator, maxLength]),
  });
}

export class KontaktPersonSchulenForm extends BaseForm<ReturnType<typeof makeForm>, KontaktPersonSchuleSaveJson> {
  constructor(config: Config) {
    super(() => makeForm(config));
  }

  override valueOnValid(): KontaktPersonSchuleSaveJson {
    const value = requireValue(this.group.getRawValue());
    return ensureProps<KontaktPersonSchuleSaveJson>({
      geschlecht: requireValue(value.geschlecht),
      funktion: requireValue(value.funktion),
      name: requireValue(value.name),
      vorname: value.vorname,
      email: value.email,
      telefon: value.telefon,
    })
  }
}
