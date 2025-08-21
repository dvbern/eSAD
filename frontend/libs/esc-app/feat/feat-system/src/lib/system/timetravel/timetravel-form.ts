import {inject} from '@angular/core';
import {NonNullableFormBuilder, Validators} from '@angular/forms';
import {ISODateString} from '@dvb/data-access-types';
import {BaseForm} from '@dvb/util-angular';
import {ensureProps, requireValue} from '@dvb/util-shared';

function makeForm() {
  const fb = inject(NonNullableFormBuilder);

  const group = fb.group({
    date: fb.control<ISODateString | undefined>(undefined, [Validators.required]),
  });

  return group;
}

export type TimetravelOutputType = {
  date: ISODateString;
}

export class TimetravelForm extends BaseForm<ReturnType<typeof makeForm>, TimetravelOutputType> {
  constructor() {
    super(() => makeForm());
  }

  public override valueOnValid(): TimetravelOutputType {
    const value = requireValue(this.group.getRawValue());
    const result = ensureProps<TimetravelOutputType>({
      date: requireValue(value.date),
    });
    return result;
  }
}
