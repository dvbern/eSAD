import {NonNullableFormBuilder, Validators} from '@angular/forms';
import {EinladungJson} from '@dvb/data-access-backend';
import {AppValidators, BaseForm} from '@dvb/util-angular';
import {isNotNullish, Nullish} from '@dvb/util-shared';

type Config = {
  fb: NonNullableFormBuilder;
};

function makeForm(config: Config) {
  const fb = config.fb;
  return fb.group({
    name: fb.control<string>('', [Validators.required]),
  });
}

export class ConfirmEinladungRemoveForm extends BaseForm<ReturnType<typeof makeForm>, object> {

  constructor(config: Config) {
    super(() => makeForm(config));
  }

  resetForm(einladung: EinladungJson | Nullish): void {
    super.reset();
    this.group.controls.name.setValidators([Validators.required]); // always validate not null
    if (isNotNullish(einladung)) { // add name validator
      this.group.controls.name.addValidators(AppValidators.requireEquals(() => einladung.kind.data.name + ' ' + einladung.kind.data.vorname));
    }
  }

  valueOnValid(): object {
   return {};
  }
}
