import {DestroyRef} from '@angular/core';
import {NonNullableFormBuilder} from '@angular/forms';
import {AppValidators, BaseForm} from '@dvb/util-angular';
import {ensureProps, Nullish, requireValue, trimToUndefined} from '@dvb/util-shared';
import {BemerkungenSaveJson} from './bemerkungen-save-json';

type Config = {
  fb: NonNullableFormBuilder,
  destroyRef: DestroyRef,
}

function makeForm(config: Config) {
  const fb = config.fb;
  const group = fb.group({
    arztInfo: fb.control<string | Nullish>('', AppValidators.dbMaxLength()),
    mpaInfo: fb.control<string | Nullish>('', AppValidators.dbMaxLength()),
    weiterePersonInfo: fb.control<string | Nullish>('', AppValidators.dbMaxLength()),
  });
  return group;
}

export class BemerkungenForm extends BaseForm<ReturnType<typeof makeForm>, BemerkungenSaveJson> {
  constructor(config: Config) {
    super(() => makeForm(config));
  }

  override valueOnValid(): BemerkungenSaveJson {
    const value = requireValue(this.group.getRawValue());
    return ensureProps<BemerkungenSaveJson>({
      arztInfo: trimToUndefined(value.arztInfo),
      mpaInfo: trimToUndefined(value.mpaInfo),
      weiterePersonInfo: trimToUndefined(value.weiterePersonInfo),
    });
  }
}
