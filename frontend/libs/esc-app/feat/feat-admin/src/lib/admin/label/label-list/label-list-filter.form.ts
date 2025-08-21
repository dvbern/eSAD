import {DestroyRef} from '@angular/core';
import {takeUntilDestroyed} from '@angular/core/rxjs-interop';
import {NonNullableFormBuilder} from '@angular/forms';
import {LabelJson, LabelTyp} from '@dvb/data-access-backend';
import {BaseForm, FilterCombinationMethod, FilterMethod, onTextInput, TableModel} from '@dvb/util-angular';
import {isNullish} from '@dvb/util-shared';
import {distinctUntilChanged} from 'rxjs';

export interface LabelFilterForm {
  nameDe: string;
  nameFr: string;
  typ: LabelTyp | null;
  autoLabel: boolean | null;
  aktiv: boolean | null;
}

type Config = {
  fb: NonNullableFormBuilder;
  destroyRef: DestroyRef;
  paginatedTable: TableModel<LabelJson>;
};

function makeForm(config: Config) {
  const fb = config.fb;
  const group = fb.group({
    nameDe: fb.control<string>(''),
    nameFr: fb.control<string>(''),
    typ: fb.control<LabelTyp | null>(null),
    autoLabel: fb.control<boolean | null>(null),
    aktiv: fb.control<boolean | null>(null),
  });

  onTextInput(group.controls.nameDe, config.destroyRef).subscribe({
    next: nameDe => {
      config.paginatedTable.onFilterChanged({
        filterValue: nameDe ?? '',
        filterMethod: FilterMethod.SUBSTRING,
        filterCombinationMethod: FilterCombinationMethod.AND,
        filterName: 'data.name.de',
      });
    },
    error: console.error,
  });

  onTextInput(group.controls.nameFr, config.destroyRef).subscribe({
    next: nameFr => {
      config.paginatedTable.onFilterChanged({
        filterValue: nameFr ?? '',
        filterMethod: FilterMethod.SUBSTRING,
        filterCombinationMethod: FilterCombinationMethod.AND,
        filterName: 'data.name.fr',
      });
    },
    error: console.error,
  });

  group.controls.typ.valueChanges.pipe(distinctUntilChanged(), takeUntilDestroyed(config.destroyRef)).subscribe({
    next: typ => {
      config.paginatedTable.onFilterChanged({
        filterValue: typ ?? '',
        filterMethod: FilterMethod.ENUM,
        filterCombinationMethod: FilterCombinationMethod.OR,
        filterName: 'data.labelTyp',
      });
    },
    error: console.error,
  });

  group.controls.autoLabel.valueChanges.pipe(distinctUntilChanged(), takeUntilDestroyed(config.destroyRef)).subscribe({
    next: autoLabel => {
      const filterValue = isNullish(autoLabel) ? '' : '' + autoLabel;
      config.paginatedTable.onFilterChanged({
        filterValue: filterValue,
        filterMethod: FilterMethod.BOOL,
        filterCombinationMethod: FilterCombinationMethod.AND,
        filterName: 'data.autoLabel',
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

export class LabelListFilterForm extends BaseForm<ReturnType<typeof makeForm>, LabelFilterForm> {
  constructor(config: Config) {
    super(() => makeForm(config));
  }

  valueOnValid(): LabelFilterForm {
    return this.group.getRawValue();
  }
}
