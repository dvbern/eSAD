import {NonNullableFormBuilder, Validators} from '@angular/forms';
import {AppLanguages, BriefvorlageSaveJson, BriefvorlageType} from '@dvb/data-access-backend';
import {AppValidators, BaseForm} from '@dvb/util-angular';
import {ensurePropsFlat, requireFilledIn, requireValue} from '@dvb/util-shared';

type Config = {
  fb: NonNullableFormBuilder
}

function makeForm(config: Config) {
  const fb = config.fb;

  return fb.group({
    name: fb.control<string>('', [Validators.required, AppValidators.dbMaxLength()]),
    type: fb.control<BriefvorlageType | undefined>(undefined, [Validators.required]),
    sprache: fb.control<AppLanguages>(AppLanguages.DE, [Validators.required]),
  });
}

export class BriefvorlageForm extends BaseForm<ReturnType<typeof makeForm>, BriefvorlageSaveJson> {

  constructor(protected config: Config) {
    super(() => makeForm(config));
  }

  override valueOnValid(): BriefvorlageSaveJson {
    const value = requireValue(this.group.getRawValue());
    return ensurePropsFlat<BriefvorlageSaveJson>({
      name: requireFilledIn(value.name),
      type: requireFilledIn(value.type),
      sprache: requireFilledIn(value.sprache),
    });
  }
}
