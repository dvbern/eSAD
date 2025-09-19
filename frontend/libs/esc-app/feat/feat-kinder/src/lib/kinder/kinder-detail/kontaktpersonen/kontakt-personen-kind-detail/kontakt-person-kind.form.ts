import {DestroyRef} from '@angular/core';
import {NonNullableFormBuilder, Validators} from '@angular/forms';
import {Geschlecht, KontaktPersonKindBeziehung, KontaktPersonKindSaveJson} from '@dvb/data-access-backend';
import {AdresseForm, globalConstants} from '@dvb/feat-shared';
import {AppValidators, BaseForm} from '@dvb/util-angular';
import {ensurePropsFlat, Nullish, requireValue} from '@dvb/util-shared';

type Config = {
  fb: NonNullableFormBuilder;
  adresseForm: AdresseForm;
  destroyRef: DestroyRef;
}

function makeForm(config: Config) {
  const fb = config.fb;
  const maxLength = AppValidators.dbMaxLength();

  const group = fb.group({
    name: fb.control<string>('', [Validators.required, maxLength]),
    vorname: fb.control<string | undefined>(undefined, [maxLength]),
    geschlecht: fb.control<Geschlecht | undefined>(undefined, [Validators.required]),
    beziehung: fb.control<KontaktPersonKindBeziehung | undefined>(undefined, []),
    adresse: config.adresseForm.group,
    telefon: fb.control<string | undefined>(undefined, [AppValidators.phoneValidator(), maxLength]),
    email: fb.control<string | undefined>(undefined, [AppValidators.emailValidator, maxLength]),
    korrespondenz: fb.control<boolean>(false, []),
    version: fb.control<number | Nullish>({value: undefined, disabled: true}),
  });

  group.updateValueAndValidity();
  return group;
}

export class KontaktPersonKindForm extends BaseForm<ReturnType<typeof makeForm>, KontaktPersonKindSaveJson> {
  readonly adresseForm: AdresseForm;

  constructor(config: Config) {
    super(() => makeForm(config));
    this.adresseForm = config.adresseForm;
  }

  override valueOnValid(): KontaktPersonKindSaveJson {
    const value = requireValue(this.group.getRawValue());
    return ensurePropsFlat<KontaktPersonKindSaveJson>({
      name: requireValue(value.name),
      vorname: value.vorname,
      geschlecht: requireValue(value.geschlecht),
      beziehung: value.beziehung,
      adresse: this.adresseForm.valueOnValid(),
      email: value.email,
      telefon: value.telefon,
      korrespondenz: value.korrespondenz,
      version: value.version ?? globalConstants.entityPrePersistVersion,
    })
  }
}
