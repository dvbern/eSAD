import {inject} from '@angular/core';
import {NonNullableFormBuilder, Validators} from '@angular/forms';
import {BaseForm} from '@dvb/util-angular';
import {ensureProps, requireValue} from '@dvb/util-shared';

function makeForm() {
  const fb = inject(NonNullableFormBuilder);

  const group = fb.group({
    text1: fb.control<string>('', [Validators.required]),
    text2: fb.control<string>('', [Validators.required]),
  });

  return group;
}

export type PlaygroundEditDialogFormOutputType = {
  text1: string;
  text2: string;
}

export class PlaygroundEditDialogForm extends BaseForm<ReturnType<typeof makeForm>, PlaygroundEditDialogFormOutputType> {
  constructor() {
    super(() => makeForm());
  }

  public override valueOnValid(): PlaygroundEditDialogFormOutputType {
    const value = requireValue(this.group.getRawValue());
    const result = ensureProps<PlaygroundEditDialogFormOutputType>({
      text1: value.text1,
      text2: value.text2,
    });
    return result;
  }
}
