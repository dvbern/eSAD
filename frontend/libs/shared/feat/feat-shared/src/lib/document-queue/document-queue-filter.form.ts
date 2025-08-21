import {DestroyRef} from '@angular/core';
import {takeUntilDestroyed} from '@angular/core/rxjs-interop';
import {NonNullableFormBuilder} from '@angular/forms';
import {DocumentQueueDisplayJson} from '@dvb/data-access-backend';
import {BaseForm, FilterCombinationMethod, FilterMethod, TableModel} from '@dvb/util-angular';
import {distinctUntilChanged} from 'rxjs';

type Config = {
  fb: NonNullableFormBuilder;
  destroyRef: DestroyRef;
  paginatedTable: TableModel<DocumentQueueDisplayJson>;
};

function makeForm(config: Config) {
  const fb = config.fb;
  const group = fb.group({
    type: fb.control<string>(''),
    status: fb.control<string>(''),
    timestampErstellt: fb.control<string>(''),
    timestampResult: fb.control<string>(''),
    fileName: fb.control<string>(''),
    name: fb.control<string>(''),
  });

  group.controls.type.valueChanges.pipe(distinctUntilChanged(), takeUntilDestroyed(config.destroyRef)).subscribe({
    next: type => {
      config.paginatedTable.onFilterChanged({
        filterValue: type ?? '',
        filterMethod: FilterMethod.ENUM,
        filterCombinationMethod: FilterCombinationMethod.OR,
        filterName: 'type',
      });
    },
    error: console.error,
  });

  group.controls.status.valueChanges
    .pipe(distinctUntilChanged(), takeUntilDestroyed(config.destroyRef))
    .subscribe({
      next: status => {
        config.paginatedTable.onFilterChanged({
          filterValue: status ?? '',
          filterMethod: FilterMethod.ENUM,
          filterCombinationMethod: FilterCombinationMethod.OR,
          filterName: 'status',
        });
      },
      error: console.error,
    });

  group.controls.timestampErstellt.valueChanges
    .pipe(distinctUntilChanged(), takeUntilDestroyed(config.destroyRef))
    .subscribe({
      next: klassenstufe => {
        config.paginatedTable.onFilterChanged({
          filterValue: klassenstufe ?? '',
          filterMethod: FilterMethod.DATE,
          filterCombinationMethod: FilterCombinationMethod.AND,
          filterName: 'entityInfo.timestampErstellt',
        });
      },
      error: console.error,
    });

  group.controls.timestampResult.valueChanges.pipe(distinctUntilChanged(), takeUntilDestroyed(config.destroyRef))
    .subscribe({
      next: timestampResult => {
        config.paginatedTable.onFilterChanged({
          filterValue: timestampResult ?? '',
          filterMethod: FilterMethod.DATE,
          filterCombinationMethod: FilterCombinationMethod.AND,
          filterName: 'timestampResult',
        });
      },
      error: console.error,
    });

  group.controls.fileName.valueChanges.pipe(distinctUntilChanged(), takeUntilDestroyed(config.destroyRef)).subscribe({
    next: fileName => {
      config.paginatedTable.onFilterChanged({
        filterValue: fileName ?? '',
        filterMethod: FilterMethod.SUBSTRING,
        filterCombinationMethod: FilterCombinationMethod.AND,
        filterName: 'fileName',
      });
    },
    error: console.error,
  });

  group.controls.name.valueChanges.pipe(distinctUntilChanged(), takeUntilDestroyed(config.destroyRef)).subscribe({
    next: name => {
      config.paginatedTable.onFilterChanged({
        filterValue: name ?? '',
        filterMethod: FilterMethod.ENUM,
        filterCombinationMethod: FilterCombinationMethod.AND,
        filterName: 'documentQueueParams.spssReportType',
      });
    },
    error: console.error,
  });

  return group;
}

export class DocumentQueueFilterForm extends BaseForm<
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
