import {DestroyRef} from '@angular/core';
import {takeUntilDestroyed} from '@angular/core/rxjs-interop';
import {NonNullableFormBuilder, Validators} from '@angular/forms';
import {
  AnlassArt,
  AnlassConfigJson,
  AnlassJson,
  AnlassSaveJson,
  AnlassStatus,
  AnlassTyp,
  BenutzerOptionJson,
} from '@dvb/data-access-backend';
import {ISODateString} from '@dvb/data-access-types';
import {globalConstants, KlassenOptionsStore, TimetravelStore} from '@dvb/feat-shared';
import {AppValidators, BaseForm, DetailMode} from '@dvb/util-angular';
import {
  ensureProps,
  getDateTimeFromDateAndTime,
  getSchoolYearFromISODateString,
  getTimeFromISODateTimeString,
  isNotNullish,
  ISOTime,
  Nullish,
  requireFilledIn,
  requireValue,
  trimToUndefined,
} from '@dvb/util-shared';
import {toDate} from 'date-fns';
import {distinctUntilChanged} from 'rxjs';
import {AnlassKinderStore} from '../../anlass-kinder.store';

type Config = {
  fb: NonNullableFormBuilder,
  destroyRef: DestroyRef,
  klassenOptionsStore: KlassenOptionsStore,
  anlassKinderStore: AnlassKinderStore,
  anlassConfig: AnlassConfigJson | Nullish,
  timetravelStore: TimetravelStore
}

function makeForm(config: Config) {
  const fb = config.fb;

  const group = fb.group({
    titel: fb.control<string>('', [Validators.required, AppValidators.dbMaxLength()]),
    datum: fb.control<ISODateString | undefined>(undefined, [Validators.required]),
    schuljahr: fb.control<number | undefined>(undefined, [Validators.required]),
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
    version: fb.control<number | Nullish>({value: undefined, disabled: true}),
  }, {
    validators: [AppValidators.zeitBisIsAfterZeitVon('zeitVon', 'zeitBis')],
  });

  // Times must be set in tandem
  const handleTimeInputsEmpty = () => {
    if (group.controls.zeitVon.value?.length === 0 && group.controls.zeitBis.value?.length === 0) {
      group.controls.zeitVon.updateValueAndValidity();
      group.controls.zeitBis.updateValueAndValidity();
    }
  };

  group.controls.datum.valueChanges.pipe(distinctUntilChanged(), takeUntilDestroyed(config.destroyRef)).subscribe({
    next: datum => {
      const year = getSchoolYearFromISODateString(datum);
      if (isNotNullish(year)) {
        group.controls.schuljahr.setValue(year);
      }
    },
    error: console.error,
  });

  group.controls.zeitVon.setValidators([AppValidators.requiredIfValidator(() => !!group.controls.zeitBis.value)]);
  group.controls.zeitBis.setValidators([AppValidators.requiredIfValidator(() => !!group.controls.zeitVon.value)]);
  group.controls.zeitVon
    .valueChanges.pipe(distinctUntilChanged(), takeUntilDestroyed(config.destroyRef)).subscribe({
    next: () => handleTimeInputsEmpty(),
    error: console.error,
  });
  group.controls.zeitBis
    .valueChanges.pipe(distinctUntilChanged(), takeUntilDestroyed(config.destroyRef)).subscribe({
    next: () => handleTimeInputsEmpty(),
    error: console.error,
  });

  return group;
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
    });
    if (!anlass) {
      // Bei neuen Anlaessen muss das Datum in der Zukunft liegen
      this.group.controls.datum.addValidators(AppValidators.nonPastDates(this.config.timetravelStore.currentDate()));
    }
    // Validation of anlassdatum according to config
    if (anlass) {
      const anlassConfig = anlass.anlassConfig;
      const von = toDate(anlassConfig.gueltigkeitsZeitraum.datumVon);
      const bis = toDate(anlassConfig.gueltigkeitsZeitraum.datumBis);
      this.group.controls.datum.addValidators([AppValidators.dateRange(von, bis)]);
    }
  }

  override valueOnValid(): AnlassSaveJson {
    const value = requireValue(this.group.getRawValue());
    return ensureProps<AnlassSaveJson>({
      anlassTyp: requireFilledIn(value.anlassTyp),
      titel: requireFilledIn(value.titel),
      anlassStatus: requireFilledIn(value.anlassStatus),
      datum: requireFilledIn(value.datum),
      schuljahr: requireFilledIn(value.schuljahr),
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
