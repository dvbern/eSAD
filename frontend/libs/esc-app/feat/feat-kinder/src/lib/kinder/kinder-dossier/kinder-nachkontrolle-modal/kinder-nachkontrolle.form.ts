import {NonNullableFormBuilder} from '@angular/forms';
import {BaseForm} from '@dvb/util-angular';
import {AnlassFlagsJson} from '@dvb/data-access-backend';
import {ensureProps} from '@dvb/util-shared';

function makeForm(fb: NonNullableFormBuilder) {
  return fb.group({
    nachkontrolleSehen: fb.control<boolean>(false),
    nachkontrolleHoeren: fb.control<boolean>(false),
    nachkontrolleGroesseGewicht: fb.control<boolean>(false),
    nachkontrolleSprache: fb.control<boolean>(false),
    nachkontrolleMotorik: fb.control<boolean>(false),
    nachkontrolleAnderes: fb.control<boolean>(false)
  });
}

export class KinderNachkontrolleForm extends BaseForm<ReturnType<typeof makeForm>, AnlassFlagsJson> {

  constructor(fb: NonNullableFormBuilder) {
    super(() => makeForm(fb));
  }

  override valueOnValid(): AnlassFlagsJson {
    const value = this.group.getRawValue();
    return ensureProps<AnlassFlagsJson>({
      nachkontrolleSehen: value.nachkontrolleSehen,
      nachkontrolleHoeren: value.nachkontrolleHoeren,
      nachkontrolleGroesseGewicht: value.nachkontrolleGroesseGewicht,
      nachkontrolleSprache: value.nachkontrolleSprache,
      nachkontrolleMotorik: value.nachkontrolleMotorik,
      nachkontrolleAnderes: value.nachkontrolleAnderes
    });
  }
}
