import {NonNullableFormBuilder, Validators} from '@angular/forms';
import {
  BenutzerOptionJson,
  FileDataJson,
  LabelJson,
  NotizSaveJson,
  NotizStatus,
  NotizTyp,
} from '@dvb/data-access-backend';
import {AppValidators, BaseForm} from '@dvb/util-angular';
import {ensurePropsFlat, Nullish, requireFilledIn, requireValue, trimToUndefined} from '@dvb/util-shared';
import {globalConstants} from '../constants/global-constants';

type Config = {
  fb: NonNullableFormBuilder;
  currentBenutzer: BenutzerOptionJson | undefined;
}

function makeForm(config: Config) {
  return config.fb.group({
    titel: config.fb.control<string>('', [Validators.required, AppValidators.dbMaxLength()]),
    bemerkungen: config.fb.control<string>('', AppValidators.textareaMaxLength5000()),
    status: config.fb.control<NotizStatus>(NotizStatus.ABGESCHLOSSEN, Validators.required),
    verantwortlicher: config.fb.control<BenutzerOptionJson | undefined>(config.currentBenutzer, Validators.required),
    anlassLink: config.fb.control<string>('', AppValidators.textareaMaxLength2000()),
    labels: config.fb.control<LabelJson[]>([]),
    files: config.fb.control<FileDataJson[]>([]),
    version: config.fb.control<number | Nullish>({value: undefined, disabled: true}),
    typ: config.fb.control<NotizTyp>({value: NotizTyp.DEFAULT, disabled: true}, Validators.required),
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
      anlassLink: trimToUndefined(value.anlassLink),
      labels: value.labels,
      files: value.files,
      version: value.version ?? globalConstants.entityPrePersistVersion,
      typ: value.typ,
    });
  }
}
