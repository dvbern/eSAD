import {DestroyRef} from '@angular/core';
import {takeUntilDestroyed} from '@angular/core/rxjs-interop';
import {NonNullableFormBuilder, Validators} from '@angular/forms';
import {
  AnlassArt,
  AnlassConfigJson,
  AnlassConfigOptionJson,
  AnlassJson,
  AnlassSaveJson,
  AnlassStatus,
  AnlassTyp,
  BenutzerOptionJson,
} from '@dvb/data-access-backend';
import {ISODateString} from '@dvb/data-access-types';
import {
  AnlassConfigOptionsStore,
  BenutzerStore,
  globalConstants,
  KlassenOptionsStore,
  TenantSettingsStore,
  TimetravelStore
} from '@dvb/feat-shared';
import {AppValidators, BaseForm, DetailMode} from '@dvb/util-angular';
import {
  ensurePropsFlat,
  getDateTimeFromDateAndTime,
  getSchoolYearFromISODateString,
  getTimeFromISODateTimeString,
  isNotNullish,
  isNullish,
  ISOTime,
  Nullish,
  requireFilledIn,
  requireValue,
  trimToUndefined,
} from '@dvb/util-shared';
import {toDate} from 'date-fns';
import {combineLatest, distinctUntilChanged, filter, map} from 'rxjs';
import {AnlassKinderStore} from '../../anlass-kinder.store';

type Config = {
  fb: NonNullableFormBuilder;
  destroyRef: DestroyRef;
  klassenOptionsStore: KlassenOptionsStore;
  anlassKinderStore: AnlassKinderStore;
  anlassConfig: AnlassConfigJson | Nullish;
  timetravelStore: TimetravelStore;
  anlassConfigOptionsStore: AnlassConfigOptionsStore;
  tenantSettingsStore: TenantSettingsStore;
  benutzerStore: BenutzerStore;
  isNew: boolean;
};

function makeForm(config: Config) {
  const fb = config.fb;

  const group = fb.group(
    {
      titel: fb.control<string>('', [Validators.required, AppValidators.dbMaxLength()]),
      datum: fb.control<ISODateString | undefined>(undefined, [Validators.required]),
      // Wird benötigt zum sicherstellen, dass beim Datumswechsel nicht in ein anderes Schuljahr gewechselt wird. Da
      // Schuljahr individuell wechselbar ist können wir uns nicht darauf verlassen.
      originalDatum: fb.control<ISODateString | undefined>({ value: undefined, disabled: true }),
      schuljahr: fb.control<number | undefined>(undefined, [Validators.required]),
      anlassConfigOption: fb.control<AnlassConfigOptionJson | undefined>(undefined, Validators.required),
      anlassArt: fb.control<AnlassArt | undefined>(undefined, Validators.required),
      anlassTyp: fb.control<AnlassTyp | undefined>(undefined, Validators.required),
      zeitVon: fb.control<ISOTime | undefined>(undefined),
      zeitBis: fb.control<ISOTime | undefined>(undefined),
      arzt: fb.control<BenutzerOptionJson | undefined>(undefined),
      mpa: fb.control<BenutzerOptionJson | undefined>(undefined),
      weiterePerson: fb.control<BenutzerOptionJson | undefined>(undefined),
      anlassStatus: fb.control<AnlassStatus>(AnlassStatus.IN_PLANUNG, Validators.required),
      arztInfo: fb.control<string>('', AppValidators.dbMaxLength()),
      mpaInfo: fb.control<string>('', AppValidators.dbMaxLength()),
      weiterePersonInfo: fb.control<string>('', AppValidators.dbMaxLength()),
      version: fb.control<number | Nullish>({ value: undefined, disabled: true }),
    },
    {
      validators: [
        AppValidators.zeitBisIsAfterZeitVon('zeitVon', 'zeitBis'),
        AppValidators.datumStaysInSameSchuljahr(config.isNew, 'datum', 'originalDatum'),
      ],
    },
  );

  // Arzt ist bei dezentralen Mandanten zwingend
  group.controls.arzt.setValidators([AppValidators.requiredIfValidator(() => !config.tenantSettingsStore.zentraleOrganisationEnabled())]);

  group.controls.zeitVon.setValidators([AppValidators.requiredIfValidator(() => !!group.controls.zeitBis.value)]);
  group.controls.zeitBis.setValidators([AppValidators.requiredIfValidator(() => !!group.controls.zeitVon.value)]);

  subscribeToFormChanges(group, config);

  return group;
}

function subscribeToFormChanges(group: ReturnType<typeof makeForm>, config: Config) {
  if (config.isNew) {
    combineLatest([
      group.controls.anlassTyp.valueChanges.pipe(distinctUntilChanged()),
      group.controls.datum.valueChanges.pipe(distinctUntilChanged(), map(getSchoolYearFromISODateString)),
    ])
      .pipe(takeUntilDestroyed(config.destroyRef), filter(isNotNullish))
      .subscribe({
        next: () => {
          const datum = group.controls.datum.value;
          const anlassTyp = group.controls.anlassTyp.value;
          const option = config.anlassConfigOptionsStore.getDefaultOptionForAnlassTyp(anlassTyp, datum);
          group.controls.anlassConfigOption.setValue(option);
        },
        error: console.error,
      });
  }

  group.controls.datum.valueChanges
    .pipe(
      distinctUntilChanged(),
      map(getSchoolYearFromISODateString),
      filter(isNotNullish),
      takeUntilDestroyed(config.destroyRef),
    )
    .subscribe({
      next: year => {
        group.controls.schuljahr.setValue(year);
      },
      error: console.error,
    });

  combineLatest([
    group.controls.zeitVon.valueChanges.pipe(distinctUntilChanged()),
    group.controls.zeitBis.valueChanges.pipe(distinctUntilChanged()),
  ]).subscribe({
    next: () => {
      if (group.controls.zeitVon.value?.length === 0 && group.controls.zeitBis.value?.length === 0) {
        group.controls.zeitVon.updateValueAndValidity();
        group.controls.zeitBis.updateValueAndValidity();
      }
    },
    error: console.error,
  });
}

export class AnlassDatenForm extends BaseForm<ReturnType<typeof makeForm>, AnlassSaveJson> {
  constructor(protected config: Config) {
    super(() => makeForm(config));
  }

  resetForm(anlass: AnlassJson | Nullish, mode: DetailMode): void {
    super.reset(anlass?.data, mode);
    // Converting time values
    this.group.reset({
      ...anlass?.data,
      zeitVon: getTimeFromISODateTimeString(anlass?.data.zeitVon),
      zeitBis: getTimeFromISODateTimeString(anlass?.data.zeitBis),
      originalDatum: anlass?.data.datum,
    });
    if (!anlass) {
      // Bei neuen Anlaessen muss das Datum in der Zukunft liegen
      this.group.controls.datum.addValidators(AppValidators.nonPastDates(this.config.timetravelStore.currentDate()));
    }
    // Validation of anlassdatum according to config
    if (anlass) {
      const anlassConfig = anlass.anlassConfig;
      const von = toDate(anlassConfig.data.datumVon);
      const bis = toDate(anlassConfig.data.datumBis);
      this.group.controls.datum.addValidators([AppValidators.dateRange(von, bis)]);
    }
    // Arzt und MPA nur fuer aktuell Verantwortliche Person editierbar, oder wenn noch nicht gesetzt
    if (isNullish(anlass?.data.arzt) || this.config.benutzerStore.isZustaendigForAnlass(anlass)) {
      this.group.controls.arzt.enable();
    } else {
      this.group.controls.arzt.disable();
    }
    if (isNullish(anlass?.data.mpa) || this.config.benutzerStore.isZustaendigForAnlass(anlass)) {
      this.group.controls.mpa.enable();
    } else {
      this.group.controls.mpa.disable();
    }
  }

  override valueOnValid(): AnlassSaveJson {
    const value = requireValue(this.group.getRawValue());
    return ensurePropsFlat<AnlassSaveJson>({
      anlassTyp: requireFilledIn(value.anlassTyp),
      titel: requireFilledIn(value.titel),
      anlassStatus: requireFilledIn(value.anlassStatus),
      datum: requireFilledIn(value.datum),
      schuljahr: requireFilledIn(value.schuljahr),
      anlassConfigOption: requireFilledIn(value.anlassConfigOption),
      zeitVon: getDateTimeFromDateAndTime(requireFilledIn(value.datum), value.zeitVon),
      zeitBis: getDateTimeFromDateAndTime(requireFilledIn(value.datum), value.zeitBis),
      anlassArt: requireFilledIn(value.anlassArt),
      arzt: value.arzt,
      arztInfo: trimToUndefined(value.arztInfo),
      mpa: value.mpa,
      mpaInfo: trimToUndefined(value.mpaInfo),
      weiterePerson: value.weiterePerson,
      weiterePersonInfo: trimToUndefined(value.weiterePersonInfo),
      version: value.version ?? globalConstants.entityPrePersistVersion,
    });
  }
}
