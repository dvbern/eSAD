import {NonNullableFormBuilder, Validators} from '@angular/forms';
import {KontaktPersonSchuleOptionJson} from '@dvb/data-access-backend';
import {BaseForm} from '@dvb/util-angular';
import {requireValue} from '@dvb/util-shared';

type Config = {
  fb: NonNullableFormBuilder;
};

function makeForm(config: Config) {
  const fb = config.fb;
  return fb.group({
    kontaktPerson: fb.control<KontaktPersonSchuleOptionJson | undefined>(undefined, [Validators.required]),
  });
}

export class SchulenDetailKontaktpersonenAddForm
  extends BaseForm<ReturnType<typeof makeForm>, KontaktPersonSchuleOptionJson> {

  constructor(config: Config) {
    super(() => makeForm(config));
  }

  valueOnValid(): KontaktPersonSchuleOptionJson {
    const value = requireValue(this.group.getRawValue());
    return requireValue(value.kontaktPerson);
  }
}
