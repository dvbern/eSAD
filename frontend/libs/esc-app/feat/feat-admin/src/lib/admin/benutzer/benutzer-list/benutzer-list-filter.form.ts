import {DestroyRef} from '@angular/core';
import {takeUntilDestroyed} from '@angular/core/rxjs-interop';
import {NonNullableFormBuilder} from '@angular/forms';
import {BenutzerJson, FachRolle} from '@dvb/data-access-backend';
import {BaseForm, FilterCombinationMethod, FilterMethod, onTextInput, TableModel} from '@dvb/util-angular';
import {isNullish} from '@dvb/util-shared';
import {distinctUntilChanged} from 'rxjs';

type Config = {
  fb: NonNullableFormBuilder;
  destroyRef: DestroyRef;
  paginatedTable: TableModel<BenutzerJson>;
};

function makeForm(config: Config) {
  const fb = config.fb;
  const group = fb.group({
    fullName: fb.control<string>(''),
    email: fb.control<string>(''),
    aktiv: fb.control<boolean | null>(null),
    rolle: fb.control<FachRolle | null>(null),
  });

  onTextInput(group.controls.fullName, config.destroyRef).subscribe({
    next: fullName => {
      config.paginatedTable.onFilterChanged({
        filterValue: fullName ?? '',
        filterMethod: FilterMethod.SUBSTRING,
        filterCombinationMethod: FilterCombinationMethod.AND,
        filterName: 'data.fullName',
      });
    },
    error: console.error,
  });

  onTextInput(group.controls.email, config.destroyRef).subscribe({
    next: email => {
      config.paginatedTable.onFilterChanged({
        filterValue: email ?? '',
        filterMethod: FilterMethod.SUBSTRING,
        filterCombinationMethod: FilterCombinationMethod.AND,
        filterName: 'data.email',
      });
    },
    error: console.error,
  });

  group.controls.aktiv.valueChanges.pipe(distinctUntilChanged(), takeUntilDestroyed(config.destroyRef)).subscribe({
    next: aktiv => {
      const filterValue = isNullish(aktiv) ? '' : '' + aktiv;
      config.paginatedTable.onFilterChanged({
        filterValue: filterValue,
        filterMethod: FilterMethod.BOOL,
        filterCombinationMethod: FilterCombinationMethod.AND,
        filterName: 'data.aktiv',
      });
    },
    error: console.error,
  });

  group.controls.rolle.valueChanges.pipe(distinctUntilChanged(), takeUntilDestroyed(config.destroyRef)).subscribe({
    next: rolle => {
      config.paginatedTable.onFilterChanged({
        filterValue: rolle ?? '',
        filterMethod: FilterMethod.ENUM,
        filterCombinationMethod: FilterCombinationMethod.OR,
        filterName: 'data.fachRollen',
      });
    },
    error: console.error,
  });

  return group;
}

export class BenutzerListFilterForm extends BaseForm<
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
