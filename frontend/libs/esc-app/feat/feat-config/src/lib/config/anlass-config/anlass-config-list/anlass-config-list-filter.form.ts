import { DestroyRef } from '@angular/core';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { NonNullableFormBuilder } from '@angular/forms';
import { AnlassConfigJson, AnlassConfigStatus, AnlassTyp } from '@dvb/data-access-backend';
import { ISODateString } from '@dvb/data-access-types';
import { BaseForm, FilterCombinationMethod, FilterMethod, onTextInput, TableModel } from '@dvb/util-angular';
import { distinctUntilChanged } from 'rxjs';
import { isNullish } from '@dvb/util-shared';

export interface AnlassConfigFilterForm {
  anlassTyp: AnlassTyp[];
  bezeichnung: string | null;
  datumVon: ISODateString | null;
  datumBis: ISODateString | null;
  anlassConfigStatus: AnlassConfigStatus | null;
  anlassConfigVisible: boolean | null;
}

type Config = {
  fb: NonNullableFormBuilder;
  destroyRef: DestroyRef;
  paginatedTable: TableModel<AnlassConfigJson>;
};

function makeForm(config: Config) {
  const fb = config.fb;
  const group = fb.group({
    anlassTyp: fb.control<AnlassTyp[]>([]),
    bezeichnung: fb.control<string | null>(null),
    datumVon: fb.control<ISODateString | null>(null),
    datumBis: fb.control<ISODateString | null>(null),
    anlassConfigStatus: fb.control<AnlassConfigStatus | null>(null),
    anlassConfigVisible: fb.control<boolean | null>(null),
  });

  group.controls.anlassTyp.valueChanges.pipe(distinctUntilChanged(), takeUntilDestroyed(config.destroyRef)).subscribe({
    next: anlassTyp => {
      config.paginatedTable.onFilterChanged({
        filterValue: anlassTyp.map(typ => typ.toString()).join(' ') ?? '',
        filterMethod: FilterMethod.ENUM,
        filterCombinationMethod: FilterCombinationMethod.OR,
        filterName: 'data.anlassTyp',
      });
    },
    error: console.error,
  });

  group.controls.bezeichnung.valueChanges.pipe(distinctUntilChanged(), takeUntilDestroyed(config.destroyRef)).subscribe({
    next: bezeichnung => {
      config.paginatedTable.onFilterChanged({
        filterValue: bezeichnung ?? '',
        filterMethod: FilterMethod.SUBSTRING,
        filterCombinationMethod: FilterCombinationMethod.SINGLE,
        filterName: 'data.bezeichnung',
      });
    },
    error: console.error,
  });

  group.controls.datumVon.valueChanges.pipe(distinctUntilChanged(), takeUntilDestroyed(config.destroyRef)).subscribe({
    next: datumVon => {
      config.paginatedTable.onFilterChanged({
        filterValue: datumVon ?? '',
        filterMethod: FilterMethod.DATE,
        filterCombinationMethod: FilterCombinationMethod.SINGLE,
        filterName: 'data.datumVon',
      });
    },
    error: console.error,
  });

  group.controls.datumBis.valueChanges.pipe(distinctUntilChanged(), takeUntilDestroyed(config.destroyRef)).subscribe({
    next: datumBis => {
      config.paginatedTable.onFilterChanged({
        filterValue: datumBis ?? '',
        filterMethod: FilterMethod.DATE,
        filterCombinationMethod: FilterCombinationMethod.SINGLE,
        filterName: 'data.datumBis',
      });
    },
    error: console.error,
  });

  group.controls.anlassConfigStatus.valueChanges
    .pipe(distinctUntilChanged(), takeUntilDestroyed(config.destroyRef))
    .subscribe({
      next: anlassConfigStatus => {
        config.paginatedTable.onFilterChanged({
          filterValue: anlassConfigStatus ?? '',
          filterMethod: FilterMethod.ENUM,
          filterCombinationMethod: FilterCombinationMethod.OR,
          filterName: 'anlassConfigStatus',
        });
      },
      error: console.error,
    });

  onTextInput(group.controls.anlassConfigVisible, config.destroyRef).subscribe({
    next: anlassConfigVisible => {
      const filterValue = isNullish(anlassConfigVisible) ? '' : '' + anlassConfigVisible;
      config.paginatedTable.onFilterChanged({
        filterValue: filterValue,
        filterMethod: FilterMethod.BOOL,
        filterCombinationMethod: FilterCombinationMethod.OR,
        filterName: 'anlassConfigVisible',
      });
    },
    error: console.error,
  });

  return group;
}

export class AnlassConfigListFilterForm extends BaseForm<ReturnType<typeof makeForm>, AnlassConfigFilterForm> {
  constructor(config: Config) {
    super(() => makeForm(config));
  }

  valueOnValid(): AnlassConfigFilterForm {
    return this.group.getRawValue();
  }
}
