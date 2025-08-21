import {DestroyRef} from '@angular/core';
import {takeUntilDestroyed} from '@angular/core/rxjs-interop';
import {NonNullableFormBuilder} from '@angular/forms';
import {EntityAktivStatus, ImpfstoffJson} from '@dvb/data-access-backend';
import {BaseForm, FilterCombinationMethod, FilterMethod, onTextInput, TableModel} from '@dvb/util-angular';
import {distinctUntilChanged} from 'rxjs';

export interface ImpfstoffFilterForm {
  name: string;
  impfstoffAktivStatus: EntityAktivStatus | undefined;
}

type Config = {
  fb: NonNullableFormBuilder;
  destroyRef: DestroyRef;
  paginatedTable: TableModel<ImpfstoffJson>;
};

function makeForm(config: Config) {
  const fb = config.fb;
  const group = fb.group({
    name: fb.control<string>(''),
    impfstoffAktivStatus: fb.control<EntityAktivStatus | undefined>(undefined),
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

  group.controls.impfstoffAktivStatus.valueChanges.pipe(distinctUntilChanged(), takeUntilDestroyed(config.destroyRef))
    .subscribe({
      next: impfstoffAktivStatus => {
        const filterValue = impfstoffAktivStatus ?? '';
        config.paginatedTable.onFilterChanged({
          filterValue: filterValue,
          filterMethod: FilterMethod.ENUM,
          filterCombinationMethod: FilterCombinationMethod.AND,
          filterName: 'data.impfstoffAktivStatus',
        });
      },
      error: console.error,
    });

  return group;
}

export class ImpfstoffListFilterForm extends BaseForm<ReturnType<typeof makeForm>, ImpfstoffFilterForm> {
  constructor(config: Config) {
    super(() => makeForm(config));
  }

  valueOnValid(): ImpfstoffFilterForm {
    return this.group.getRawValue();
  }
}
