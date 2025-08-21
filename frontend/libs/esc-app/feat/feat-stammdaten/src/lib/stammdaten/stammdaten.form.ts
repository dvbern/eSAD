import { DestroyRef, inject } from '@angular/core';
import { NonNullableFormBuilder, Validators } from '@angular/forms';
import { ISODateString } from '@dvb/data-access-types';
import { BaseForm } from '@dvb/util-angular';
import { ensureProps, requireFilledIn, requireValue, trimToUndefined } from '@dvb/util-shared';
import { Geschlecht, MyStammdatenDTO } from './fake-stammdaten.service';

type Config = {
  fb: NonNullableFormBuilder;
  destroyRef: DestroyRef;
};

function makeForm(config: Config) {
  return config.fb.group({
    name: config.fb.control('', [Validators.required]),
    vorname: config.fb.control('', [Validators.required]),
    geburtsDatum: config.fb.control<ISODateString | ''>(''),
    geschlecht: config.fb.control<Geschlecht | ''>('', [Validators.required]),
  });
}

export class StammdatenForm extends BaseForm<ReturnType<typeof makeForm>, MyStammdatenDTO> {
  constructor(config: Config = { fb: inject(NonNullableFormBuilder), destroyRef: inject(DestroyRef) }) {
    super(() => makeForm(config));
  }

  valueOnValid(): MyStammdatenDTO {
    const value = requireValue(this.group.getRawValue());
    return ensureProps<MyStammdatenDTO>({
      name: value.name,
      vorname: value.vorname,
      geburtsDatum: trimToUndefined(value.geburtsDatum),
      geschlecht: requireFilledIn(value.geschlecht),
    });
  }
}
