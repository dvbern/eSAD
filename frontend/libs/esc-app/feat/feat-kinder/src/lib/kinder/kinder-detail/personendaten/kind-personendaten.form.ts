import {DestroyRef} from '@angular/core';
import {NonNullableFormBuilder, Validators} from '@angular/forms';
import {
  BenutzerOptionJson,
  EntityAktivStatus,
  Geschlecht,
  KindSaveJson,
  Klassenstufe,
  KlasseOptionJson,
  Land,
  SchuleOptionJson,
} from '@dvb/data-access-backend';
import {ISODateString} from '@dvb/data-access-types';
import {AdresseForm, BenutzerStore, globalConstants, KindSaveJsonEnhanced, TenantSettingsStore} from '@dvb/feat-shared';
import {AppValidators, BaseForm, DetailMode} from '@dvb/util-angular';
import {ensurePropsFlat, Nullish, requireFilledIn, requireValue, trimToUndefined} from '@dvb/util-shared';
import {KinderSummaryStore} from '../../kinder-summary.store';

type Config = {
  fb: NonNullableFormBuilder;
  kinderStore: KinderSummaryStore;
  tenantSettingsStore: TenantSettingsStore;
  benutzerStore: BenutzerStore;
  destroyRef: DestroyRef;
  adresseForm: AdresseForm;
};

function makeForm(config: Config) {
  const fb = config.fb;
  const maxLength = AppValidators.dbMaxLength();

  const group = fb.group({
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
    zustaendigArzt: fb.control<BenutzerOptionJson | undefined>(undefined),
    zustaendigMPA: fb.control<BenutzerOptionJson | undefined>(undefined),
  });

  // Arzt ist bei dezentralen Mandanten zwingend
  group.controls.zustaendigArzt.setValidators([AppValidators.requiredIfValidator(() => !config.tenantSettingsStore.zentraleOrganisationEnabled())]);

  return group;
}

export class KindPersonendatenForm extends BaseForm<ReturnType<typeof makeForm>, KindSaveJsonEnhanced> {
  readonly adresseForm: AdresseForm;

  constructor(private readonly config: Config) {
    super(() => makeForm(config));
    this.adresseForm = config.adresseForm;
  }

  resetForm(
    kindSaveJson: KindSaveJson,
    zustaendigArzt: BenutzerOptionJson | undefined,
    zustaendigMPA: BenutzerOptionJson | undefined,
    mode: DetailMode
  ): void {
    const enhancedKind = this.enhanceKind(kindSaveJson, zustaendigArzt, zustaendigMPA);
    this.reset(enhancedKind, mode);
    this.group.controls.zustaendigArzt.reset(zustaendigArzt);
    this.group.controls.zustaendigMPA.reset(zustaendigMPA);
    // Arzt und MPA nur fuer aktuell Verantwortliche Person editierbar
    if (!this.config.benutzerStore.isZustaendigForKind(enhancedKind)) {
      this.group.controls.zustaendigArzt.disable();
      this.group.controls.zustaendigMPA.disable();
    }
  }

  override valueOnValid(): KindSaveJsonEnhanced {
    const value = requireValue(this.group.getRawValue());
    // TODO ESC-322 validate plz and ort belong together
    const kindSaveJson = ensurePropsFlat<KindSaveJson>({
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
    return this.enhanceKind(kindSaveJson, value.zustaendigArzt, value.zustaendigMPA);
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

  private enhanceKind(
    kind: KindSaveJson,
    zustaendigArzt: BenutzerOptionJson | undefined,
    zustaendigMPA: BenutzerOptionJson | undefined
  ): KindSaveJsonEnhanced {
    return {
      ...kind,
      zustaendigArzt: zustaendigArzt,
      zustaendigMPA: zustaendigMPA,
    }
  }
}

