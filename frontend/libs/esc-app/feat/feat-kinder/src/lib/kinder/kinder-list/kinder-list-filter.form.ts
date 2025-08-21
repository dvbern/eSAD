import {DestroyRef} from '@angular/core';
import {takeUntilDestroyed} from '@angular/core/rxjs-interop';
import {NonNullableFormBuilder} from '@angular/forms';
import {SearchSummaryJson} from '@dvb/data-access-backend';
import {ISODateString} from '@dvb/data-access-types';
import {BaseForm, FilterCombinationMethod, FilterMethod, onTextInput, TableModel} from '@dvb/util-angular';
import {distinctUntilChanged} from 'rxjs';

export interface KinderFilterForm {
  schule: string[];
  klasse: string;
  klassenstufe: string[];
  geschlecht: string | null
  name: string;
  vorname: string;
  geburtsdatum: ISODateString | null;
  esadId: string;
}

type Config = {
  fb: NonNullableFormBuilder;
  destroyRef: DestroyRef;
  paginatedTable: TableModel<SearchSummaryJson>;
};

function makeForm(config: Config) {
  const fb = config.fb;
  const group = fb.group({
    schule: fb.control<string[]>([]),
    klasse: fb.control<string>(''),
    klassenstufe: fb.control<string[]>([]),
    geschlecht: fb.control<string | null>(null),
    name: fb.control<string>(''),
    vorname: fb.control<string>(''),
    geburtsdatum: fb.control<ISODateString | null>(null),
    esadId: fb.control<string>(''),
  });

  group.controls.schule.valueChanges.pipe(distinctUntilChanged(), takeUntilDestroyed(config.destroyRef))
    .subscribe({
      next: schule => {
        config.paginatedTable.onFilterChanged({
          filterValue: schule?.map(s => `"${s}"`).join(' ') ?? '',
          filterMethod: FilterMethod.ENUM,
          filterCombinationMethod: FilterCombinationMethod.OR,
          filterName: 'schule.id',
        });
      },
      error: console.error,
    });

  onTextInput(group.controls.klasse, config.destroyRef).subscribe({
    next: klasse => {
      config.paginatedTable.onFilterChanged({
        filterValue: klasse ?? '',
        filterMethod: FilterMethod.SUBSTRING,
        filterCombinationMethod: FilterCombinationMethod.AND,
        filterName: 'klasse.bezeichnung',
      });
    },
    error: console.error,
  });

  group.controls.klassenstufe.valueChanges.pipe(distinctUntilChanged(), takeUntilDestroyed(config.destroyRef))
    .subscribe({
      next: klassenstufe => {
        config.paginatedTable.onFilterChanged({
          filterValue: klassenstufe?.join(' ') ?? '',
          filterMethod: FilterMethod.ENUM,
          filterCombinationMethod: FilterCombinationMethod.OR,
          filterName: 'klassenStufe',
        });
      },
      error: console.error,
    });

  group.controls.geschlecht.valueChanges.pipe(distinctUntilChanged(), takeUntilDestroyed(config.destroyRef)).subscribe({
    next: geschlecht => {
      config.paginatedTable.onFilterChanged({
        filterValue: geschlecht ?? '',
        filterMethod: FilterMethod.ENUM,
        filterCombinationMethod: FilterCombinationMethod.OR,
        filterName: 'geschlecht',
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
        filterName: 'name',
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
        filterName: 'vorname',
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
          filterCombinationMethod: FilterCombinationMethod.SINGLE,
          filterName: 'geburtsdatum',
        });
      },
      error: console.error,
    });

  onTextInput(group.controls.esadId, config.destroyRef).subscribe({
    next: esadId => {
      config.paginatedTable.onFilterChanged({
        filterValue: esadId ?? '',
        filterMethod: FilterMethod.SUBSTRING,
        filterCombinationMethod: FilterCombinationMethod.AND,
        filterName: 'esadId',
      });
    },
    error: console.error,
  });

  return group;
}

export class KinderListFilterForm extends BaseForm<ReturnType<typeof makeForm>, KinderFilterForm> {
  constructor(config: Config) {
    super(() => makeForm(config));
  }

  valueOnValid(): KinderFilterForm {
    return this.group.getRawValue();
  }
}
