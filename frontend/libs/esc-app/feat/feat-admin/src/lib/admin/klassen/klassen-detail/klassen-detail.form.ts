import {NonNullableFormBuilder, Validators} from '@angular/forms';
import {EntityAktivStatus, Klassenstufe, KlasseSaveJson, SchuleOptionJson} from '@dvb/data-access-backend';
import {AppValidators, BaseForm} from '@dvb/util-angular';
import {ensureProps, requireValue} from '@dvb/util-shared';

type Config = {
  fb: NonNullableFormBuilder;
};

function makeForm(config: Config) {
  const fb = config.fb;
  const maxLength = AppValidators.dbMaxLength();
  return fb.group({
    schule: fb.control<SchuleOptionJson | undefined>(undefined, Validators.required),
    bezeichnung: fb.control<string>('', [Validators.required, maxLength]),
    klassenstufe: fb.control<Klassenstufe | undefined>(undefined, Validators.required),
    klasseSchulart: fb.control<string | undefined>(undefined, Validators.required),
    klasseAktivStatus: fb.control<EntityAktivStatus>(EntityAktivStatus.AKTIV, Validators.required),
  });
}

export class KlassenDetailForm extends BaseForm<ReturnType<typeof makeForm>, KlasseSaveJson> {
  constructor(config: Config) {
    super(() => makeForm(config));
  }

  override valueOnValid(): KlasseSaveJson {
    const value = requireValue(this.group.getRawValue());
    return ensureProps<KlasseSaveJson>({
      schule: requireValue(value.schule),
      bezeichnung: requireValue(value.bezeichnung),
      klassenstufe: requireValue(value.klassenstufe),
      klasseSchulart: requireValue(value.klasseSchulart),
      klasseAktivStatus: requireValue(value.klasseAktivStatus),
    });
  }
}
