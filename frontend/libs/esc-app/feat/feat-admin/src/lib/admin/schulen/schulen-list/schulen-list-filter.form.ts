import {DestroyRef} from '@angular/core';
import {takeUntilDestroyed} from '@angular/core/rxjs-interop';
import {NonNullableFormBuilder} from '@angular/forms';
import {EntityAktivStatus, EscadaKlasseStufe, SchuleJson} from '@dvb/data-access-backend';
import {BaseForm, FilterCombinationMethod, FilterMethod, onTextInput, TableModel} from '@dvb/util-angular';
import {isNullish} from '@dvb/util-shared';
import {distinctUntilChanged} from 'rxjs';

export interface KlassenListFilter {
  klassenstufen: EscadaKlasseStufe[];
  schulhaus: string;
  schulstandort: string;
  schulkreis: string;
  zustaendigArzt: string;
  zustaendigMPA: string;
  schuleAktivStatus: EntityAktivStatus | undefined;
}

type Config = {
  fb: NonNullableFormBuilder;
  destroyRef: DestroyRef;
  paginatedTable: TableModel<SchuleJson>;
};

function makeForm(config: Config) {
  const fb = config.fb;
  const group = fb.group({
    klassenstufen: fb.control<EscadaKlasseStufe[]>([]),
    schulhaus: fb.control<string>(''),
    schulstandort: fb.control<string>(''),
    schulkreis: fb.control<string>(''),
    zustaendigArzt: fb.control<string>(''),
    zustaendigMPA: fb.control<string>(''),
    schuleAktivStatus: fb.control<EntityAktivStatus | undefined>(undefined),
  });

  group.controls.klassenstufen.valueChanges.pipe(distinctUntilChanged(), takeUntilDestroyed(config.destroyRef))
    .subscribe({
      next: klassenstufen => {
        config.paginatedTable.onFilterChanged({
          filterValue: klassenstufen.map(as => as.toString()).join(' ') ?? '',
          filterMethod: FilterMethod.ENUM,
          filterCombinationMethod: FilterCombinationMethod.OR,
          filterName: 'klassenstufen',
        });
      },
      error: console.error,
    });

  onTextInput(group.controls.schulhaus, config.destroyRef).subscribe({
    next: schulhaus => {
      config.paginatedTable.onFilterChanged({
        filterValue: schulhaus ?? '',
        filterMethod: FilterMethod.SUBSTRING,
        filterCombinationMethod: FilterCombinationMethod.AND,
        filterName: 'data.schulhaus',
      });
    },
    error: console.error,
  });

  onTextInput(group.controls.schulstandort, config.destroyRef).subscribe({
    next: schulstandort => {
      config.paginatedTable.onFilterChanged({
        filterValue: schulstandort ?? '',
        filterMethod: FilterMethod.SUBSTRING,
        filterCombinationMethod: FilterCombinationMethod.AND,
        filterName: 'data.schulstandort',
      });
    },
    error: console.error,
  });

  onTextInput(group.controls.schulkreis, config.destroyRef).subscribe({
    next: schulkreis => {
      config.paginatedTable.onFilterChanged({
        filterValue: schulkreis ?? '',
        filterMethod: FilterMethod.SUBSTRING,
        filterCombinationMethod: FilterCombinationMethod.AND,
        filterName: 'data.schulkreis',
      });
    },
    error: console.error,
  });

  onTextInput(group.controls.zustaendigArzt, config.destroyRef).subscribe({
    next: zustaendigArzt => {
      config.paginatedTable.onFilterChanged({
        filterValue: zustaendigArzt ?? '',
        filterMethod: FilterMethod.SUBSTRING,
        filterCombinationMethod: FilterCombinationMethod.AND,
        filterName: 'data.zustaendigArzt.fullName',
      });
    },
    error: console.error,
  });

  onTextInput(group.controls.zustaendigMPA, config.destroyRef).subscribe({
    next: zustaendigMPA => {
      config.paginatedTable.onFilterChanged({
        filterValue: zustaendigMPA ?? '',
        filterMethod: FilterMethod.SUBSTRING,
        filterCombinationMethod: FilterCombinationMethod.AND,
        filterName: 'data.zustaendigMPA.fullName',
      });
    },
    error: console.error,
  });

  group.controls.schuleAktivStatus.valueChanges.pipe(distinctUntilChanged(), takeUntilDestroyed(config.destroyRef))
    .subscribe({
      next: schuleAktivStatus => {
        const filterValue = isNullish(schuleAktivStatus) ? '' : schuleAktivStatus;
        config.paginatedTable.onFilterChanged({
          filterValue: filterValue,
          filterMethod: FilterMethod.ENUM,
          filterCombinationMethod: FilterCombinationMethod.AND,
          filterName: 'data.schuleAktivStatus',
        });
      },
      error: console.error,
    });

  return group;
}

export class SchulenListFilterForm extends BaseForm<ReturnType<typeof makeForm>, KlassenListFilter> {
  constructor(config: Config) {
    super(() => makeForm(config));
  }

  valueOnValid(): KlassenListFilter {
    return this.group.getRawValue();
  }
}
