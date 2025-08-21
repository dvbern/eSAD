import {DestroyRef} from '@angular/core';
import {NonNullableFormBuilder} from '@angular/forms';
import {DuplikatJson, DuplikatStatus} from '@dvb/data-access-backend';
import {BaseForm, FilterCombinationMethod, FilterMethod, onTextInput, TableModel} from '@dvb/util-angular';

type Config = {
  fb: NonNullableFormBuilder;
  destroyRef: DestroyRef;
  paginatedTable: TableModel<DuplikatJson>;
};

function makeForm(config: Config) {
  const fb = config.fb;
  const group = fb.group({
    duplikatStatus: fb.control<DuplikatStatus | null>(null),
  });

  onTextInput(group.controls.duplikatStatus, config.destroyRef).subscribe({
    next: duplikatStatus => {
      config.paginatedTable.onFilterChanged({
        filterValue: duplikatStatus ?? '',
        filterMethod: FilterMethod.ENUM,
        filterCombinationMethod: FilterCombinationMethod.OR,
        filterName: 'duplikatStatus',
      });
    },
    error: console.error,
  });

  return group;
}

export class DuplikatListFilterForm extends BaseForm<
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
