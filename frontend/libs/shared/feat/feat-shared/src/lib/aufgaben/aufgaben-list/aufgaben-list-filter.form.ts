import {DestroyRef} from '@angular/core';
import {takeUntilDestroyed} from '@angular/core/rxjs-interop';
import {NonNullableFormBuilder} from '@angular/forms';
import {AufgabeStatus, BenutzerOptionJson, LabelJson} from '@dvb/data-access-backend';
import {ISODateString} from '@dvb/data-access-types';
import {BaseForm, FilterCombinationMethod, FilterMethod, onTextInput, TableModel} from '@dvb/util-angular';
import {isNullish} from '@dvb/util-shared';
import {distinctUntilChanged} from 'rxjs';
import {AufgabeJsonEnhanced} from '../../admin-shared/aufgaben.store';

export interface AufgabenListFilter {
  faelligBis: ISODateString | null;
  aufgabeStatus: AufgabeStatus[];
  label: LabelJson[];
  titel: string;
  erstelltDurch: BenutzerOptionJson[];
  empfaenger: BenutzerOptionJson[];
  kind: string;
  hasAnhang: boolean | null
}

type Config = {
  fb: NonNullableFormBuilder;
  destroyRef: DestroyRef;
  paginatedTable: TableModel<AufgabeJsonEnhanced>;
};

function makeForm(config: Config) {
  const fb = config.fb;
  const group = fb.group({
    faelligBis: fb.control<ISODateString | null>(null),
    aufgabeStatus: fb.control<AufgabeStatus[]>([AufgabeStatus.OFFEN, AufgabeStatus.IN_BEARBEITUNG]),
    label: fb.control<LabelJson[]>([]),
    titel: fb.control<string>(''),
    erstelltDurch: fb.control<BenutzerOptionJson[]>([]),
    empfaenger: fb.control<BenutzerOptionJson[]>([]),
    kind: fb.control<string>(''),
    hasAnhang: fb.control<boolean | null>(null),
  });

  group.controls.faelligBis.valueChanges.pipe(distinctUntilChanged(), takeUntilDestroyed(config.destroyRef))
    .subscribe({
      next: datum => {
        config.paginatedTable.onFilterChanged({
          filterValue: datum ?? '',
          filterMethod: FilterMethod.DATE,
          filterCombinationMethod: FilterCombinationMethod.AND,
          filterName: 'data.faelligBis',
        });
      },
      error: console.error,
    });

  group.controls.aufgabeStatus.valueChanges.pipe(distinctUntilChanged(), takeUntilDestroyed(config.destroyRef))
    .subscribe({
      next: aufgabeStatus => {
        config.paginatedTable.onFilterChanged({
          filterValue: aufgabeStatus.map(as => as.toString()).join(' ') ?? '',
          filterMethod: FilterMethod.ENUM,
          filterCombinationMethod: FilterCombinationMethod.OR,
          filterName: 'data.status',
        });
      },
      error: console.error,
    });

  group.controls.label.valueChanges.pipe(distinctUntilChanged(), takeUntilDestroyed(config.destroyRef)).subscribe({
    next: labels => {
      config.paginatedTable.onFilterChanged({
        filterValue: labels.map(label => label.id).join(' ') ?? '',
        filterMethod: FilterMethod.ENUM, // works for ids too
        filterCombinationMethod: FilterCombinationMethod.OR,
        filterName: 'labelIds',
      });
    },
    error: console.error,
  });

  onTextInput(group.controls.titel, config.destroyRef).subscribe({
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

  onTextInput(group.controls.erstelltDurch, config.destroyRef).subscribe({
    next: benutzer => {
      config.paginatedTable.onFilterChanged({
        filterValue: benutzer.map(benutzer => benutzer.id).join(' ') ?? '',
        filterMethod: FilterMethod.SUBSTRING,
        filterCombinationMethod: FilterCombinationMethod.OR,
        filterName: 'data.userErfasst.id',
      });
    },
    error: console.error,
  });

  onTextInput(group.controls.empfaenger, config.destroyRef).subscribe({
    next: empfaenger => {
      config.paginatedTable.onFilterChanged({
        filterValue: empfaenger.map(empfaenger => empfaenger.id).join(' ') ?? '',
        filterMethod: FilterMethod.SUBSTRING,
        filterCombinationMethod: FilterCombinationMethod.OR,
        filterName: 'data.empfaenger.id',
      });
    },
    error: console.error,
  });

  onTextInput(group.controls.kind, config.destroyRef).subscribe({
    next: kind => {
      config.paginatedTable.onFilterChanged({
        filterValue: kind ?? '',
        filterMethod: FilterMethod.SUBSTRING,
        filterCombinationMethod: FilterCombinationMethod.AND,
        filterName: 'kindFullName',
      });
    },
    error: console.error,
  });

  group.controls.hasAnhang.valueChanges.pipe(distinctUntilChanged(), takeUntilDestroyed(config.destroyRef)).subscribe({
    next: hasAnhang => {
      const filterValue = isNullish(hasAnhang) ? '' : '' + hasAnhang;
      config.paginatedTable.onFilterChanged({
        filterValue: filterValue,
        filterMethod: FilterMethod.BOOL,
        filterCombinationMethod: FilterCombinationMethod.AND,
        filterName: 'hasAnhang',
      });
    },
    error: console.error,
  });

  group.controls.aufgabeStatus.updateValueAndValidity();
  return group;
}

export class AufgabeListFilterForm extends BaseForm<ReturnType<typeof makeForm>, AufgabenListFilter> {
  constructor(config: Config) {
    super(() => makeForm(config));
  }

  setEmpfaenger(benutzer: BenutzerOptionJson): void {
    this.group.controls.empfaenger.reset([benutzer]);
  }

  valueOnValid(): AufgabenListFilter {
    return this.group.getRawValue();
  }
}
