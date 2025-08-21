import { inject } from '@angular/core';
import { NonNullableFormBuilder, Validators } from '@angular/forms';
import { ISODateString } from '@dvb/data-access-types';
import { BaseForm } from '@dvb/util-angular';
import { ensureProps, requireFilledIn, requireValue } from '@dvb/util-shared';
import {Color, PlaygroundFormOutputType } from './playground-form.types';

function makeForm() {
  const fb = inject(NonNullableFormBuilder);

  const group = fb.group({
    typeahead: fb.control<string>('', [Validators.required]),
    captcha: fb.control<string>('', [Validators.required]),
    text: fb.control<string>('', [Validators.required]),
    email: fb.control<string>('', [Validators.email]),
    date: fb.control<ISODateString | null>(null, [Validators.required]),
    time: fb.control<`${number}:${number}` | null>(null, [Validators.required]),
    radios: fb.control<Color | null>(null, [Validators.required]),
    radios1: fb.control<Color | null>(null, [Validators.required]),
    radios2: fb.control<Color | null>(null, [Validators.required]),
    checkbox: fb.control<boolean>(false, [Validators.required, Validators.requiredTrue]),
    select: fb.control<string | null>(null, [Validators.required]),
    multiSelect: fb.control<string[] | null>(['5', 'zwei'], [Validators.required]),
    textarea: fb.control<string>('', [Validators.required, Validators.minLength(3), Validators.maxLength(5)]),

    typeahead2: fb.control<string>('', []),
    text2: fb.control<string>('', []),
    text3: fb.control<string>('', []),
    text4: fb.control<string>('', []),
    select2: fb.control<string | null>(null, []),
  });

  return group;
}

export class PlaygroundBorderedFormInputForm extends BaseForm<ReturnType<typeof makeForm>, PlaygroundFormOutputType> {
  constructor() {
    super(() => makeForm());
  }

  valueOnValid(): PlaygroundFormOutputType {
    const value = requireValue(this.group.getRawValue());

    const result = ensureProps<PlaygroundFormOutputType>({
      typeahead: requireFilledIn(value.typeahead),
      captcha: value.captcha,
      text: requireFilledIn(value.text),
      email: value.email,
      date: requireFilledIn(value.date),
      time: requireFilledIn(value.time),
      radios: requireFilledIn(value.radios),
      radios1: requireFilledIn(value.radios1),
      radios2: requireFilledIn(value.radios2),
      checkbox: requireFilledIn(value.checkbox),
      select: requireFilledIn(value.select),
      textarea: requireFilledIn(value.textarea),

      typeahead2: value.typeahead2,
      text2: value.text2,
      text3: value.text3,
      text4: value.text4,
      select2: value.select2,

      multiSelect: value.multiSelect ?? [],
    });

    return result;
  }
}
