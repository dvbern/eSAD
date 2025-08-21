import {DestroyRef} from '@angular/core';
import {NonNullableFormBuilder} from '@angular/forms';
import {BenutzerOptionJson, UntersuchungImpfungenDoppelkontrolleSaveJson} from '@dvb/data-access-backend';
import {ISODateString} from '@dvb/data-access-types';
import {AppValidators, BaseForm, DetailMode} from '@dvb/util-angular';
import {ensureProps, isNotNullish, Nullish, requireValue} from '@dvb/util-shared';

type Config = {
  fb: NonNullableFormBuilder;
  destroyRef: DestroyRef;
};

function makeForm(config: Config) {
  const fb = config.fb;
  const group = fb.group({
    // Der User von Visum 1 kann nie geändert werden, es ist immer entweder schon gesetzt oder der aktuelle Benutzer
    visumPerson1: fb.control<BenutzerOptionJson | undefined>({ value: undefined, disabled: true }),
    visumDatum1: fb.control<ISODateString | undefined>(undefined),
    visumPerson2: fb.control<BenutzerOptionJson | undefined>(undefined),
    visumDatum2: fb.control<ISODateString | undefined>(undefined),
  });
  group.controls.visumPerson2.setValidators([
    AppValidators.requiredIfValidator(() => !!group.controls.visumDatum1.value),
  ]);
  return group;
}

export class UntersuchungDoppelkontrolleImpfentscheidForm extends BaseForm<
  ReturnType<typeof makeForm>,
  UntersuchungImpfungenDoppelkontrolleSaveJson
> {
  constructor(protected config: Config) {
    super(() => makeForm(config));
  }

  resetForm(value: UntersuchungImpfungenDoppelkontrolleSaveJson | undefined, loggedInUser: BenutzerOptionJson | Nullish): void {
    super.reset(value, DetailMode.EDIT);
    // Falls Visum 1 nicht gesetzt -> den aktuellen Benutzer als Visum 1 setzen
    if (!value?.visumPerson1 && isNotNullish(loggedInUser)) {
      this.group.controls.visumPerson1.reset(loggedInUser);
    }
    // Solange Visum 1 nicht gegeben wurde, kann Visum 2 nicht erfasst werden
    if (value?.visumDatum1) {
      this.group.controls.visumPerson2.enable();
    } else {
      this.group.controls.visumPerson2.disable();
    }
  }

  valueOnValid(): UntersuchungImpfungenDoppelkontrolleSaveJson {
    const value = requireValue(this.group.getRawValue());
    return ensureProps<UntersuchungImpfungenDoppelkontrolleSaveJson>({
      visumPerson1: value.visumPerson1,
      visumDatum1: value.visumDatum1,
      visumPerson2: value.visumPerson2,
      visumDatum2: value.visumDatum2,
    });
  }
}
