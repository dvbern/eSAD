import {NonNullableFormBuilder, Validators} from '@angular/forms';
import {BenutzerOptionJson, FileDataJson, LabelJson, NotizSaveJson, NotizStatus} from '@dvb/data-access-backend';
import {AppValidators, BaseForm} from '@dvb/util-angular';
import {ensurePropsFlat, requireFilledIn, requireValue, trimToUndefined} from '@dvb/util-shared';

type Config = {
  fb: NonNullableFormBuilder;
  currentBenutzer: BenutzerOptionJson | undefined;
}

function makeForm(config: Config) {
  return config.fb.group({
    titel: config.fb.control<string>('', [Validators.required, AppValidators.dbMaxLength()]),
    bemerkungen: config.fb.control<string>('', AppValidators.textareaMaxLength()),
    status: config.fb.control<NotizStatus>(NotizStatus.ABGESCHLOSSEN, Validators.required),
    verantwortlicher: config.fb.control<BenutzerOptionJson | undefined>(config.currentBenutzer, Validators.required),
    labels: config.fb.control<LabelJson[]>([]),
    files: config.fb.control<FileDataJson[]>([]),
  });
}

export class NotizenForm extends BaseForm<ReturnType<typeof makeForm>, NotizSaveJson> {

  constructor(protected config: Config) {
    super(() => makeForm(config));
  }

  override valueOnValid(): NotizSaveJson {
    const value = requireValue(this.group.getRawValue());
    return ensurePropsFlat<NotizSaveJson>({
      titel: requireFilledIn(value.titel),
      bemerkungen: trimToUndefined(value.bemerkungen),
      status: requireFilledIn(value.status),
      verantwortlicher: requireValue(value.verantwortlicher),
      labels: value.labels,
      files: value.files,
    });
  }
}
