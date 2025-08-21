import {NonNullableFormBuilder} from '@angular/forms';
import {BaseForm} from '@dvb/util-angular';
import {ensureProps} from '@dvb/util-shared';

export interface KinderImmunitaetJson {
  immunitaetMasern: boolean;
  immunitaetVarizellen: boolean;
}

function makeForm(fb: NonNullableFormBuilder) {
  return fb.group({
    immunitaetMasern: fb.control<boolean>(false),
    immunitaetVarizellen: fb.control<boolean>(false),
  });
}

export class KinderImmunitaetForm extends BaseForm<ReturnType<typeof makeForm>, KinderImmunitaetJson> {
  constructor(fb: NonNullableFormBuilder) {
    super(() => makeForm(fb));
  }

  valueOnValid(): KinderImmunitaetJson {
    const value = this.group.getRawValue();
    return ensureProps<KinderImmunitaetJson>({
      immunitaetMasern: value.immunitaetMasern,
      immunitaetVarizellen: value.immunitaetVarizellen,
    });
  }
}
