import {DestroyRef} from '@angular/core';
import {takeUntilDestroyed} from '@angular/core/rxjs-interop';
import {NonNullableFormBuilder} from '@angular/forms';
import {EntityAktivStatus} from '@dvb/data-access-backend';
import {KlasseJsonEnhanced} from '@dvb/feat-shared';
import {BaseForm, FilterCombinationMethod, FilterMethod, onTextInput, TableModel} from '@dvb/util-angular';
import {isNullish} from '@dvb/util-shared';
import {distinctUntilChanged} from 'rxjs';

export interface KlassenListFilter {
  schule: string;
  bezeichnung: string;
  klassenstufe: string;
  klasseSchulart: string;
  lehrperson: string;
  klasseAktivStatus: EntityAktivStatus | undefined;
}

type Config = {
  fb: NonNullableFormBuilder;
  destroyRef: DestroyRef;
  paginatedTable: TableModel<KlasseJsonEnhanced>;
};

function makeForm(config: Config) {
  const fb = config.fb;
  const group = fb.group({
    schule: fb.control<string>(''),
    bezeichnung: fb.control<string>(''),
    klassenstufe: fb.control<string>(''),
    klasseSchulart: fb.control<string>(''),
    lehrperson: fb.control<string>(''),
    klasseAktivStatus: fb.control<EntityAktivStatus | undefined>(undefined),
  });

  onTextInput(group.controls.schule, config.destroyRef).subscribe({
    next: schule => {
      config.paginatedTable.onFilterChanged({
        filterValue: schule ?? '',
        filterMethod: FilterMethod.SUBSTRING,
        filterCombinationMethod: FilterCombinationMethod.AND,
        filterName: 'data.schule.bezeichnung',
      });
    },
    error: console.error,
  });

  onTextInput(group.controls.bezeichnung, config.destroyRef).subscribe({
    next: bezeichnung => {
      config.paginatedTable.onFilterChanged({
        filterValue: bezeichnung ?? '',
        filterMethod: FilterMethod.SUBSTRING,
        filterCombinationMethod: FilterCombinationMethod.AND,
        filterName: 'data.bezeichnung',
      });
    },
    error: console.error,
  });

  onTextInput(group.controls.klassenstufe, config.destroyRef).subscribe({
    next: klassenstufe => {
      config.paginatedTable.onFilterChanged({
        filterValue: klassenstufe ?? '',
        filterMethod: FilterMethod.SUBSTRING,
        filterCombinationMethod: FilterCombinationMethod.AND,
        filterName: 'data.klassenstufe',
      });
    },
    error: console.error,
  });

  onTextInput(group.controls.klasseSchulart, config.destroyRef).subscribe({
    next: klasseSchulart => {
      config.paginatedTable.onFilterChanged({
        filterValue: klasseSchulart ?? '',
        filterMethod: FilterMethod.SUBSTRING,
        filterCombinationMethod: FilterCombinationMethod.AND,
        filterName: 'data.klasseSchulart',
      });
    },
    error: console.error,
  });

  onTextInput(group.controls.lehrperson, config.destroyRef).subscribe({
    next: lehrperson => {
      config.paginatedTable.onFilterChanged({
        filterValue: lehrperson ?? '',
        filterMethod: FilterMethod.SUBSTRING,
        filterCombinationMethod: FilterCombinationMethod.AND,
        filterName: 'lehrpersonenName',
      });
    },
    error: console.error,
  });

  group.controls.klasseAktivStatus.valueChanges.pipe(distinctUntilChanged(), takeUntilDestroyed(config.destroyRef))
    .subscribe({
      next: klasseAktivStatus => {
        const filterValue = isNullish(klasseAktivStatus) ? '' : klasseAktivStatus;
        config.paginatedTable.onFilterChanged({
          filterValue: filterValue,
          filterMethod: FilterMethod.ENUM,
          filterCombinationMethod: FilterCombinationMethod.AND,
          filterName: 'data.klasseAktivStatus',
        });
      },
      error: console.error,
    });

  return group;
}

export class KlassenListFilterForm extends BaseForm<ReturnType<typeof makeForm>, KlassenListFilter> {
  constructor(config: Config) {
    super(() => makeForm(config));
  }

  valueOnValid(): KlassenListFilter {
    return this.group.getRawValue();
  }
}
