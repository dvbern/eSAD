import {DestroyRef} from '@angular/core';
import {takeUntilDestroyed} from '@angular/core/rxjs-interop';
import {NonNullableFormBuilder} from '@angular/forms';
import {ImpfstoffChargeJson} from '@dvb/data-access-backend';
import {BaseForm, FilterCombinationMethod, FilterMethod, onTextInput, TableModel} from '@dvb/util-angular';
import {isNullish} from '@dvb/util-shared';
import {distinctUntilChanged} from 'rxjs';

type Config = {
  fb: NonNullableFormBuilder;
  destroyRef: DestroyRef;
  paginatedTable: TableModel<ImpfstoffChargeJson>;
};

function makeForm(config: Config) {
  const fb = config.fb;
  const group = fb.group({
    name: fb.control<string>(''),
    aktiv: fb.control<boolean | null>(null),
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

  return group;
}

export class ChargeListFilterForm extends BaseForm<
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
