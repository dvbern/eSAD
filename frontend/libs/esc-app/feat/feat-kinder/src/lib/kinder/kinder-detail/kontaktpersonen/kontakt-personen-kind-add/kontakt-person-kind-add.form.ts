import {NonNullableFormBuilder, Validators} from '@angular/forms';
import {KontaktPersonKindOptionJson} from '@dvb/data-access-backend';
import {BaseForm} from '@dvb/util-angular';
import {requireValue} from '@dvb/util-shared';

type Config = {
  fb: NonNullableFormBuilder;
}

function makeForm(config: Config) {
  const fb = config.fb;
  return fb.group({
    kontaktPerson: fb.control<KontaktPersonKindOptionJson | undefined>(undefined, [Validators.required]),
  });
}

export class KontaktPersonKindAddForm extends BaseForm<ReturnType<typeof makeForm>, KontaktPersonKindOptionJson> {

  constructor(config: Config) {
    super(() => makeForm(config));
  }

  valueOnValid(): KontaktPersonKindOptionJson {
    const value = requireValue(this.group.getRawValue());
    return requireValue(value.kontaktPerson);
  }
}
