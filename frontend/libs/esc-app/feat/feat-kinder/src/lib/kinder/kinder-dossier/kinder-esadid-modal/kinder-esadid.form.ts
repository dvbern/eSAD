import {NonNullableFormBuilder, Validators} from '@angular/forms';
import {EsadIdJson} from '@dvb/data-access-backend';
import {AppValidators, BaseForm} from '@dvb/util-angular';
import {requireFilledIn} from '@dvb/util-shared';
import {formatEntryAsEsadId} from './kinder-esadid.util';

function makeForm(fb: NonNullableFormBuilder) {
  return fb.group({
    esadId: fb.control<string>('', [Validators.required, AppValidators.esadIdValidator()]),
  });
}

export class KinderEsadidForm extends BaseForm<ReturnType<typeof makeForm>, EsadIdJson> {
  constructor(fb: NonNullableFormBuilder) {
    super(() => makeForm(fb));
  }

  valueOnValid(): EsadIdJson {
    const value = this.group.getRawValue();
    return {
      esadId: formatEntryAsEsadId(requireFilledIn(value.esadId)),
    }
  }
}

