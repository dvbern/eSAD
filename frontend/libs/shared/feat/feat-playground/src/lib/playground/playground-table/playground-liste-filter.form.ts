import {DestroyRef} from '@angular/core';
import {NonNullableFormBuilder} from '@angular/forms';
import {BaseForm, FilterCombinationMethod, FilterMethod, onTextInput, TableModel} from '@dvb/util-angular';
import {DummyItem} from './playground-table.component';

type Config = {
  fb: NonNullableFormBuilder;
  destroyRef: DestroyRef;
  dummyItemTable: TableModel<DummyItem>;
};

function makeForm(config: Config) {
  const fb = config.fb;
  const group = fb.group({
    name: fb.control<string>(''),
    strasse: fb.control<string>(''),
    nummer: fb.control<string>(''),
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

  onTextInput(group.controls.strasse, config.destroyRef).subscribe({
    next: strasse => {
      config.dummyItemTable.onFilterChanged({
        filterValue: strasse ?? '',
        filterMethod: FilterMethod.SUBSTRING,
        filterCombinationMethod: FilterCombinationMethod.AND,
        filterName: 'adresse.strasse',
      });
    },
    error: console.error,
  });

  onTextInput(group.controls.nummer, config.destroyRef).subscribe({
    next: nummer => {
      config.dummyItemTable.onFilterChanged({
        filterValue: nummer ?? '',
        filterMethod: FilterMethod.SUBSTRING,
        filterCombinationMethod: FilterCombinationMethod.AND,
        filterName: 'nummer',
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
