import {NonNullableFormBuilder, Validators} from '@angular/forms';
import {Geschlecht, LehrpersonSaveJson} from '@dvb/data-access-backend';
import {globalConstants} from '@dvb/feat-shared';
import {AppValidators, BaseForm} from '@dvb/util-angular';
import {ensureProps, Nullish, requireValue} from '@dvb/util-shared';

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
    telefon: fb.control<string | undefined>(undefined, [AppValidators.phoneValidator(), maxLength]),
    email: fb.control<string | undefined>(undefined, [AppValidators.emailValidator(), maxLength]),
    version: fb.control<number | Nullish>({value: undefined, disabled: true}),
  });
}

export class LehrpersonForm extends BaseForm<ReturnType<typeof makeForm>, LehrpersonSaveJson> {

  constructor(config: Config) {
    super(() => makeForm(config));
  }

  override valueOnValid(): LehrpersonSaveJson {
    const value = requireValue(this.group.getRawValue());
    return ensureProps<LehrpersonSaveJson>({
      name: requireValue(value.name),
      vorname: value.vorname,
      geschlecht: requireValue(value.geschlecht),
      email: value.email,
      telefon: value.telefon,
      version: value.version ?? globalConstants.entityPrePersistVersion,
    })
  }
}
