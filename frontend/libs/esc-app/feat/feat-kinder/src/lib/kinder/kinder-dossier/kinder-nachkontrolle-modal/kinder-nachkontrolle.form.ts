import {NonNullableFormBuilder} from '@angular/forms';
import {AnlassFlagsJson, NachkontrolleSaveJson} from '@dvb/data-access-backend';
import {globalConstants} from '@dvb/feat-shared';
import {BaseForm} from '@dvb/util-angular';
import {ensureProps, Nullish} from '@dvb/util-shared';

function makeForm(fb: NonNullableFormBuilder) {
  return fb.group({
    nachkontrolleSehen: fb.control<boolean>(false),
    nachkontrolleHoeren: fb.control<boolean>(false),
    nachkontrolleGroesseGewicht: fb.control<boolean>(false),
    nachkontrolleSprache: fb.control<boolean>(false),
    nachkontrolleMotorik: fb.control<boolean>(false),
    nachkontrolleAnderes: fb.control<boolean>(false),
    version: fb.control<number | Nullish>({value: undefined, disabled: true}),
  });
}

export class KinderNachkontrolleForm extends BaseForm<ReturnType<typeof makeForm>, NachkontrolleSaveJson> {

  constructor(fb: NonNullableFormBuilder) {
    super(() => makeForm(fb));
  }

  resetForm(einladung: NachkontrolleSaveJson): void {
    this.group.reset(einladung.anlassFlags);
    this.group.controls.version.reset(einladung.version);
  }

  override valueOnValid(): NachkontrolleSaveJson {
    const value = this.group.getRawValue();
    const anlassFlags = ensureProps<AnlassFlagsJson>({
      nachkontrolleSehen: value.nachkontrolleSehen,
      nachkontrolleHoeren: value.nachkontrolleHoeren,
      nachkontrolleGroesseGewicht: value.nachkontrolleGroesseGewicht,
      nachkontrolleSprache: value.nachkontrolleSprache,
      nachkontrolleMotorik: value.nachkontrolleMotorik,
      nachkontrolleAnderes: value.nachkontrolleAnderes
    });
    return ensureProps<NachkontrolleSaveJson>({
      anlassFlags: anlassFlags,
      version: value.version ?? globalConstants.entityPrePersistVersion
    });
  }
}
