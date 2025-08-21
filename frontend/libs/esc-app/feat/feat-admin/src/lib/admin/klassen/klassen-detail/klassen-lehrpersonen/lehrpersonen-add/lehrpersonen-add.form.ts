import {NonNullableFormBuilder, Validators} from '@angular/forms';
import {LehrpersonOptionJson} from '@dvb/data-access-backend';
import {BaseForm} from '@dvb/util-angular';
import {requireValue} from '@dvb/util-shared';

type Config = {
  fb: NonNullableFormBuilder;
}

function makeForm(config: Config) {
  const fb = config.fb;

  return fb.group({
    lehrperson: fb.control<LehrpersonOptionJson | undefined>(undefined, [Validators.required]),
  });
}

export class LehrpersonenAddForm extends BaseForm<ReturnType<typeof makeForm>, LehrpersonOptionJson> {

  constructor(config: Config) {
    super(() => makeForm(config));
  }

  override valueOnValid(): LehrpersonOptionJson {
    const value = requireValue(this.group.getRawValue());
    return requireValue(value.lehrperson);
  }
}
