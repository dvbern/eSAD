import {DestroyRef} from '@angular/core';
import {NonNullableFormBuilder, Validators} from '@angular/forms';
import {
  EntityAktivStatus,
  Geschlecht,
  KindSaveJson,
  Klassenstufe,
  KlasseOptionJson,
  Land,
  SchuleOptionJson,
} from '@dvb/data-access-backend';
import {ISODateString} from '@dvb/data-access-types';
import {AdresseForm, globalConstants} from '@dvb/feat-shared';
import {AppValidators, BaseForm} from '@dvb/util-angular';
import {ensurePropsFlat, Nullish, requireFilledIn, requireValue, trimToUndefined} from '@dvb/util-shared';
import {KinderSummaryStore} from '../../kinder-summary.store';

type Config = {
  fb: NonNullableFormBuilder;
  kinderStore: KinderSummaryStore;
  destroyRef: DestroyRef;
  adresseForm: AdresseForm;
};

function makeForm(config: Config) {
  const fb = config.fb;
  const maxLength = AppValidators.dbMaxLength();

  return fb.group({
    sozialversicherungsNummer: fb.control<string | undefined>(undefined,
      [AppValidators.ahvNummerValdiator(), maxLength]),
    name: fb.control<string>('', [Validators.required, maxLength]),
    vorname: fb.control<string>('', [Validators.required, maxLength]),
    geschlecht: fb.control<Geschlecht | undefined>(undefined, [Validators.required]),
    geburtsdatum: fb.control<ISODateString | undefined>(undefined,
      [Validators.required, AppValidators.geburtsdatumKindValidator(new Date())]),
    nationalitaet: fb.control<Land | undefined>(undefined, []),
    erstsprache: fb.control<string | undefined>(undefined, [maxLength]),
    wohntyp: fb.control<string | undefined>(undefined, [maxLength]),
    adresse: config.adresseForm.group,
    email: fb.control<string | undefined>(undefined, AppValidators.emailValidator()),
    telefon: fb.control<string | undefined>(undefined, AppValidators.phoneValidator()),
    schule: fb.control<SchuleOptionJson | undefined>(undefined, []),
    klasse: fb.control<KlasseOptionJson | undefined>(undefined, []),
    klassenstufe: fb.control<Klassenstufe | undefined>(undefined, []),
    kindAktivStatus: fb.control<EntityAktivStatus>(EntityAktivStatus.AKTIV, Validators.required),
    version: fb.control<number | Nullish>({value: undefined, disabled: true}),
  });
}

export class KindPersonendatenForm extends BaseForm<ReturnType<typeof makeForm>, KindSaveJson> {
  readonly adresseForm: AdresseForm;

  constructor(config: Config) {
    super(() => makeForm(config));
    this.adresseForm = config.adresseForm;
  }

  override valueOnValid(): KindSaveJson {
    const value = requireValue(this.group.getRawValue());
    // TODO ESC-322 validate plz and ort belong together
    return ensurePropsFlat<KindSaveJson>({
      sozialversicherungsNummer: trimToUndefined(value.sozialversicherungsNummer),
      name: requireFilledIn(value.name),
      vorname: requireFilledIn(value.vorname),
      geschlecht: requireFilledIn(value.geschlecht),
      geburtsdatum: requireFilledIn(value.geburtsdatum),
      nationalitaet: trimToUndefined(value.nationalitaet),
      erstsprache: trimToUndefined(value.erstsprache),
      wohntyp: trimToUndefined(value.wohntyp),
      adresse: this.adresseForm.valueOnValid(),
      email: trimToUndefined(value.email),
      telefon: trimToUndefined(value.telefon),
      schule: value.schule ?? undefined,
      klasse: value.klasse ?? undefined,
      klassenstufe: trimToUndefined(value.klassenstufe),
      alter: '',
      kindAktivStatus: requireFilledIn(value.kindAktivStatus),
      version: value.version ?? globalConstants.entityPrePersistVersion,
    });
  }

  disableEscadaFields(): void {
    this.group.controls.sozialversicherungsNummer.disable();
    this.group.controls.name.disable();
    this.group.controls.vorname.disable();
    this.group.controls.geschlecht.disable();
    this.group.controls.geburtsdatum.disable();
    this.group.controls.nationalitaet.disable();
    this.group.controls.erstsprache.disable();
    this.group.controls.email.disable();
    this.group.controls.telefon.disable();
    this.group.controls.wohntyp.disable();
    this.group.controls.adresse.controls.strasseUndNr.disable();
    this.group.controls.adresse.controls.adresseZusatz.disable();
    this.group.controls.adresse.controls.plz.disable();
    this.group.controls.adresse.controls.ort.disable();
    this.group.controls.adresse.controls.land.disable();
  }

}

