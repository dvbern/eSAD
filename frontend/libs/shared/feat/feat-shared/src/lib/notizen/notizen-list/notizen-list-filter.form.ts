import { DestroyRef } from '@angular/core';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { NonNullableFormBuilder } from '@angular/forms';
import { BenutzerOptionJson, LabelJson, NotizStatus } from '@dvb/data-access-backend';
import { ISODateString } from '@dvb/data-access-types';
import {BaseForm, FilterCombinationMethod, FilterMethod, TableModel} from '@dvb/util-angular';
import { distinctUntilChanged } from 'rxjs';
import { NotizJsonEnhanced } from '../notiz-json-enhanced';

export interface NotizFilterForm {
  erstelltAm: ISODateString | null,
  titel: string | null,
  label: LabelJson[],
  status: NotizStatus[],
  verantwortlichePerson: BenutzerOptionJson[]
}

type Config = {
  fb: NonNullableFormBuilder,
  destroyRef: DestroyRef,
  paginatedTable: TableModel<NotizJsonEnhanced>
};

function makeForm(config: Config) {
  const fb = config.fb;
  const group = fb.group({
    erstelltAm: fb.control<ISODateString | null>(null),
    titel: fb.control<string | null>(null),
    label: fb.control<LabelJson[]>([]),
    status: fb.control<NotizStatus[]>([]),
    verantwortlichePerson: fb.control<BenutzerOptionJson[]>([]),
  });

  group.controls.erstelltAm.valueChanges.pipe(distinctUntilChanged(), takeUntilDestroyed(config.destroyRef))
    .subscribe({
    next: datum => {
      config.paginatedTable.onFilterChanged({
        filterValue: datum ?? '',
        filterMethod: FilterMethod.DATE,
        filterCombinationMethod: FilterCombinationMethod.AND,
        filterName: 'entityInfo.timestampErstellt',
      });
    },
    error: console.error,
  });

  group.controls.titel.valueChanges.pipe(distinctUntilChanged(), takeUntilDestroyed(config.destroyRef))
    .subscribe({
    next: titel => {
      config.paginatedTable.onFilterChanged({
        filterValue: titel ?? '',
        filterMethod: FilterMethod.SUBSTRING,
        filterCombinationMethod: FilterCombinationMethod.AND,
        filterName: 'data.titel',
      });
    },
    error: console.error,
  });

  group.controls.label.valueChanges.pipe(distinctUntilChanged(), takeUntilDestroyed(config.destroyRef))
    .subscribe({
    next: labels => {
      config.paginatedTable.onFilterChanged({
        filterValue: labels.map(label => label.id).join(' ') ?? '',
        filterMethod: FilterMethod.ENUM,
        filterCombinationMethod: FilterCombinationMethod.OR,
        filterName: 'labelIds',
      });
    },
    error: console.error,
  });

  group.controls.status.valueChanges.pipe(distinctUntilChanged(), takeUntilDestroyed(config.destroyRef))
    .subscribe({
    next: notizStatus => {
      config.paginatedTable.onFilterChanged({
        filterValue: notizStatus.map(status => status.toString()).join(' ') ?? '',
        filterMethod: FilterMethod.ENUM,
        filterCombinationMethod: FilterCombinationMethod.OR,
        filterName: 'data.status',
      });
    },
    error: console.error,
  });

  group.controls.verantwortlichePerson.valueChanges.pipe(distinctUntilChanged(), takeUntilDestroyed(config.destroyRef))
    .subscribe({
    next: verantwortlicher => {
      config.paginatedTable.onFilterChanged({
        filterValue: verantwortlicher.join(' ') ?? '',
        filterMethod: FilterMethod.SUBSTRING,
        filterCombinationMethod: FilterCombinationMethod.OR,
        filterName: 'data.verantwortlicher.id',
      });
    },
    error: console.error,
  });

  group.updateValueAndValidity();
  return group;
}

export class NotizenListFilterForm extends BaseForm<ReturnType<typeof makeForm>, NotizFilterForm> {
  constructor(config: Config) {
    super(() => makeForm(config));
  }

  valueOnValid(): NotizFilterForm {
    return this.group.getRawValue();
  }
}
