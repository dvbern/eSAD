import {DestroyRef} from '@angular/core';
import {takeUntilDestroyed} from '@angular/core/rxjs-interop';
import {NonNullableFormBuilder} from '@angular/forms';
import {Geschlecht, KindJson, Klassenstufe, KlasseOptionJson, SchuleOptionJson} from '@dvb/data-access-backend';
import {ISODateString} from '@dvb/data-access-types';
import {KlassenOptionsStore} from '@dvb/feat-shared';
import {BaseForm, FilterCombinationMethod, FilterMethod, onTextInput, TableModel} from '@dvb/util-angular';
import {distinctUntilChanged} from 'rxjs';
import {AnlassKinderStore} from '../../../anlass-kinder.store';

type Config = {
  fb: NonNullableFormBuilder;
  destroyRef: DestroyRef;
  paginatedTable: TableModel<KindJson>;
  anlassKinderStore: AnlassKinderStore;
  klassenOptionsStore: KlassenOptionsStore;
};

function makeForm(config: Config) {
  const fb = config.fb;
  const group = fb.group({
    schule: fb.control<SchuleOptionJson | undefined>(undefined),
    klasse: fb.control<KlasseOptionJson | undefined>(undefined),

    klassenstufe: fb.control<Klassenstufe | undefined>(undefined),
    name: fb.control<string>(''),
    vorname: fb.control<string>(''),
    geschlecht: fb.control<Geschlecht | undefined>(undefined),
    geburtsdatum: fb.control<ISODateString | undefined>(undefined),
  });

  group.controls.schule
    .valueChanges.pipe(distinctUntilChanged(), takeUntilDestroyed(config.destroyRef)).subscribe({
    next: (schule: SchuleOptionJson | undefined) => {
      if (schule) {
        config.klassenOptionsStore.loadKlasseOptions({schuleId: schule.id, onlyActive: true});
        group.controls.klasse.reset();
        config.anlassKinderStore.loadKinderBySchuleOrKlasse({schuleId: schule.id});
      } else {
        config.klassenOptionsStore.resetKlasseOptions();
        config.anlassKinderStore.resetKinderListe();
      }
    },
    error: console.error,
  });

  group.controls.klasse.valueChanges.pipe(distinctUntilChanged(), takeUntilDestroyed(config.destroyRef)).subscribe({
    next: (klasse: KlasseOptionJson | undefined) => {
      if (klasse) {
        config.anlassKinderStore.loadKinderBySchuleOrKlasse({klasseId: klasse?.id});
      } else {
        const schuleId = group.controls.schule.value?.id;
        if (schuleId) {
          config.anlassKinderStore.loadKinderBySchuleOrKlasse({schuleId});
        }
      }
    },
    error: console.error,
  });

  group.controls.klassenstufe.valueChanges.pipe(distinctUntilChanged(), takeUntilDestroyed(config.destroyRef))
    .subscribe({
      next: klassenstufe => {
        config.paginatedTable.onFilterChanged({
          filterValue: klassenstufe ?? '',
          filterMethod: FilterMethod.ENUM,
          filterCombinationMethod: FilterCombinationMethod.AND,
          filterName: 'data.klassenstufe',
        });
      },
      error: console.error,
    });

  onTextInput(group.controls.name, config.destroyRef).subscribe({
    next: name => {
      config.paginatedTable.onFilterChanged({
        filterValue: name ?? '',
        filterMethod: FilterMethod.SUBSTRING,
        filterCombinationMethod: FilterCombinationMethod.AND,
        filterName: 'data.name',
      });
    },
    error: console.error,
  });

  onTextInput(group.controls.vorname, config.destroyRef).subscribe({
    next: vorname => {
      config.paginatedTable.onFilterChanged({
        filterValue: vorname ?? '',
        filterMethod: FilterMethod.SUBSTRING,
        filterCombinationMethod: FilterCombinationMethod.AND,
        filterName: 'data.vorname',
      });
    },
    error: console.error,
  });

  group.controls.geschlecht.valueChanges.pipe(distinctUntilChanged(), takeUntilDestroyed(config.destroyRef)).subscribe({
    next: geschlecht => {
      config.paginatedTable.onFilterChanged({
        filterValue: geschlecht ?? '',
        filterMethod: FilterMethod.ENUM,
        filterCombinationMethod: FilterCombinationMethod.AND,
        filterName: 'data.geschlecht',
      });
    },
    error: console.error,
  });

  group.controls.geburtsdatum.valueChanges.pipe(distinctUntilChanged(), takeUntilDestroyed(config.destroyRef))
    .subscribe({
      next: geburtsdatum => {
        config.paginatedTable.onFilterChanged({
          filterValue: geburtsdatum ?? '',
          filterMethod: FilterMethod.DATE,
          filterCombinationMethod: FilterCombinationMethod.AND,
          filterName: 'data.geburtsdatum',
        });
      },
      error: console.error,
    });

  return group;
}

export class KinderFilterForm extends BaseForm<
  ReturnType<typeof makeForm>,
  // eslint-disable-next-line @typescript-eslint/no-explicit-any
  any
> {
  constructor(config: Config) {
    super(() => makeForm(config));
  }

  valueOnValid(): void {
    return;
  }
}
