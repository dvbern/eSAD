import {DestroyRef, inject} from '@angular/core';
import {takeUntilDestroyed} from '@angular/core/rxjs-interop';
import {FormControl, FormGroup, NonNullableFormBuilder} from '@angular/forms';
import {AdresseJson, Land} from '@dvb/data-access-backend';
import {AppValidators, BaseForm} from '@dvb/util-angular';
import {requireValue, trimToUndefined} from '@dvb/util-shared';
import {distinctUntilChanged} from 'rxjs';
import {AdresseStore} from './store/adresse.store';

const VALIDATION_CH_PLZ_LENGTH = 4;

type AdresseFormType = {
  adresseZusatz: FormControl<string | undefined>;
  land: FormControl<Land | undefined>;
  ort: FormControl<string | undefined>;
  plz: FormControl<string | undefined>;
  strasseUndNr: FormControl<string | undefined>
}

type Config = {
  fb: NonNullableFormBuilder;
  destroyRef: DestroyRef;
  withLand: boolean;
  parentIdentifier: string;
};

function makeForm(config: Config) {
  const fb = config.fb;
  const maxLength = AppValidators.dbMaxLength();
  const adresseStore = inject(AdresseStore);

  const group = fb.group({
    strasseUndNr: fb.control<string | undefined>(undefined, [maxLength]),
    adresseZusatz: fb.control<string | undefined>(undefined, [maxLength]),
    plz: fb.control<string | undefined>(undefined, [AppValidators.plzValidator()]),
    ort: fb.control<string | undefined>(undefined, [maxLength, AppValidators.ortValidator()]),
    land: fb.control<Land | undefined>(Land.SCHWEIZ, []),
  });

  // add adresse validation if adress is ch
  group.controls.land.valueChanges.pipe(distinctUntilChanged(), takeUntilDestroyed(config.destroyRef))
    .subscribe({
      next: (land: Land | undefined) => {
        if (land === Land.SCHWEIZ) {
          setSwissValidatorsToAdresseGroup(group);
        } else {
          setDefaultValidatorsToAdresseGroup(group)
        }
      },
      error: console.error,
    });

  group.controls.plz.valueChanges.pipe(distinctUntilChanged(), takeUntilDestroyed(config.destroyRef))
    .subscribe({
      next: (searchString: string | undefined) => {
        if (searchString?.length === VALIDATION_CH_PLZ_LENGTH) {
          if (group.controls.plz.enabled) {
            // Sobald der Suchbegriff 4 Zeichen lang ist, rufen wir die Action auf
            adresseStore.onReadPlzData({ searchString, resultFor: config.parentIdentifier });
          }
        }
      },
      error: console.error,
    });

  return group;
}

function setSwissValidatorsToAdresseGroup(adresseGroup: FormGroup<AdresseFormType>) {
  adresseGroup.controls.plz.setValidators([AppValidators.plzValidator()]);
  adresseGroup.controls.ort.setValidators([AppValidators.dbMaxLength(), AppValidators.ortValidator()]);
}

function setDefaultValidatorsToAdresseGroup(adresseGroup: FormGroup<AdresseFormType>) {
  adresseGroup.controls.plz.setValidators([AppValidators.dbMaxLength()]);
  adresseGroup.controls.ort.setValidators([AppValidators.dbMaxLength()]);
}

export class AdresseForm extends BaseForm<ReturnType<typeof makeForm>, AdresseJson> {
  readonly withLand: boolean;
  readonly parentIdentifier: string;

  constructor(config: Config) {
    super(() => makeForm(config));
    this.withLand = config.withLand;
    this.parentIdentifier = config.parentIdentifier;
  }

  override valueOnValid(): AdresseJson {
    const value = requireValue(this.group.getRawValue());

    return {
      strasseUndNr: trimToUndefined(value.strasseUndNr),
      adresseZusatz: trimToUndefined(value.adresseZusatz),
      plz: trimToUndefined(value.plz),
      ort: trimToUndefined(value.ort),
      land: trimToUndefined(value.land),
    };
  }
}

