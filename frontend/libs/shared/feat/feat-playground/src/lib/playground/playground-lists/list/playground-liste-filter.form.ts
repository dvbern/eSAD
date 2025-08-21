import {DestroyRef} from '@angular/core';
import {NonNullableFormBuilder} from '@angular/forms';
import {BaseForm, FilterCombinationMethod, FilterMethod, onTextInput, TableModel} from '@dvb/util-angular';
import {MyDummyItem} from './playground-liste.store';

type Config = {
  fb: NonNullableFormBuilder;
  destroyRef: DestroyRef;
  dummyItemTable: TableModel<MyDummyItem>;
};

function makeForm(config: Config) {
  const fb = config.fb;
  const group = fb.group({
    name: fb.control<string>(''),
    vorname: fb.control<string>(''),
  });

  onTextInput(group.controls.name, config.destroyRef).subscribe({
    next: name => {
      config.dummyItemTable.onFilterChanged({
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
      config.dummyItemTable.onFilterChanged({
        filterValue: vorname ?? '',
        filterMethod: FilterMethod.SUBSTRING,
        filterCombinationMethod: FilterCombinationMethod.AND,
        filterName: 'vorname',
      });
    },
    error: console.error,
  });


  return group;
}

export class PlaygroundListeFilterForm extends BaseForm<
  ReturnType<typeof makeForm>,
  // eslint-disable-next-line @typescript-eslint/no-explicit-any
  any
> {
  constructor(config: Config) {
    super(() => makeForm(config));
  }

  // eslint-disable-next-line @typescript-eslint/no-empty-function
  valueOnValid(): void {
  }
}
