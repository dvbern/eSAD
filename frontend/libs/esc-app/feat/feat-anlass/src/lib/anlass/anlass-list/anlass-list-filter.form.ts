import {DestroyRef} from '@angular/core';
import {takeUntilDestroyed} from '@angular/core/rxjs-interop';
import {NonNullableFormBuilder} from '@angular/forms';
import {AnlassStatus} from '@dvb/data-access-backend';
import {BackendEntityId, ISODateString} from '@dvb/data-access-types';
import {BaseForm, FilterCombinationMethod, FilterMethod, onTextInput, TableModel} from '@dvb/util-angular';
import {distinctUntilChanged} from 'rxjs';
import {AnlassJsonEnhanced} from '../anlass.util';

export interface AnlassFilterForm {
  fullTitle: string;
  anlassStatus: AnlassStatus[];
  datum: ISODateString | null;
  arzt: BackendEntityId[];
  mpa: BackendEntityId[];
  weiterePerson: BackendEntityId[];
}

type Config = {
  fb: NonNullableFormBuilder;
  destroyRef: DestroyRef;
  paginatedTable: TableModel<AnlassJsonEnhanced>;
};

function makeForm(config: Config) {
  const fb = config.fb;
  const group = fb.group({
    fullTitle: fb.control<string>(''),
    anlassStatus: fb.control<AnlassStatus[]>([]),
    datum: fb.control<ISODateString | null>(null),
    arzt: fb.control<BackendEntityId[]>([]),
    mpa: fb.control<BackendEntityId[]>([]),
    weiterePerson: fb.control<BackendEntityId[]>([]),
  });

  onTextInput(group.controls.fullTitle, config.destroyRef).subscribe({
    next: titel => {
      config.paginatedTable.onFilterChanged({
        filterValue: titel ?? '',
        filterMethod: FilterMethod.SUBSTRING,
        filterCombinationMethod: FilterCombinationMethod.AND,
        filterName: 'fullTitle',
      });
    },
    error: console.error,
  });

  group.controls.anlassStatus.valueChanges.pipe(distinctUntilChanged(), takeUntilDestroyed(config.destroyRef))
    .subscribe({
      next: anlassStatus => {
        config.paginatedTable.onFilterChanged({
          filterValue: anlassStatus.map(as => as.toString()).join(' ') ?? '',
          filterMethod: FilterMethod.ENUM,
          filterCombinationMethod: FilterCombinationMethod.OR,
          filterName: 'data.anlassStatus',
        });
      },
      error: console.error,
    });

  group.controls.datum.valueChanges.pipe(distinctUntilChanged(), takeUntilDestroyed(config.destroyRef))
    .subscribe({
      next: datum => {
        config.paginatedTable.onFilterChanged({
          filterValue: datum ?? '',
          filterMethod: FilterMethod.DATE,
          filterCombinationMethod: FilterCombinationMethod.SINGLE,
          filterName: 'data.datum',
        });
      },
      error: console.error,
    });

  onTextInput(group.controls.arzt, config.destroyRef).subscribe({
    next: aerzte => {
      config.paginatedTable.onFilterChanged({
        filterValue: aerzte.join(' ') ?? '',
        filterMethod: FilterMethod.SUBSTRING,
        filterCombinationMethod: FilterCombinationMethod.OR,
        filterName: 'data.arzt.id',
      });
    },
    error: console.error,
  });

  group.controls.mpa.valueChanges.pipe(distinctUntilChanged(), takeUntilDestroyed(config.destroyRef)).subscribe({
    next: mpas => {
      config.paginatedTable.onFilterChanged({
        filterValue: mpas.join(' ') ?? '',
        filterMethod: FilterMethod.ENUM,
        filterCombinationMethod: FilterCombinationMethod.OR,
        filterName: 'data.mpa.id',
      });
    },
    error: console.error,
  });

  onTextInput(group.controls.weiterePerson, config.destroyRef).subscribe({
    next: weiterePersonen => {
      config.paginatedTable.onFilterChanged({
        filterValue: weiterePersonen.join(' ') ?? '',
        filterMethod: FilterMethod.SUBSTRING,
        filterCombinationMethod: FilterCombinationMethod.OR,
        filterName: 'data.weiterePerson.id',
      });
    },
    error: console.error,
  });

  return group;
}

export class AnlassListFilterForm extends BaseForm<
  ReturnType<typeof makeForm>,
  AnlassFilterForm
> {
  constructor(config: Config) {
    super(() => makeForm(config));
  }

  valueOnValid(): AnlassFilterForm {
    return this.group.getRawValue();
  }
}
