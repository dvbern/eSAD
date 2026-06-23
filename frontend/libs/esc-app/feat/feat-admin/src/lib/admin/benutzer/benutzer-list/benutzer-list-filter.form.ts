import {DestroyRef} from '@angular/core';
import {takeUntilDestroyed} from '@angular/core/rxjs-interop';
import {NonNullableFormBuilder} from '@angular/forms';
import {EntityAktivStatus, FachRolle} from '@dvb/data-access-backend';
import {BaseForm, FilterCombinationMethod, FilterMethod, onTextInput, TableModel} from '@dvb/util-angular';
import {distinctUntilChanged, startWith} from 'rxjs';
import {BenutzerEnhancedJson} from "@dvb/feat-shared";

type Config = {
  fb: NonNullableFormBuilder;
  destroyRef: DestroyRef;
  paginatedTable: TableModel<BenutzerEnhancedJson>;
};

function makeForm(config: Config) {
  const fb = config.fb;
  const group = fb.group({
    name: fb.control<string>(''),
    vorname: fb.control<string>(''),
    email: fb.control<string>(''),
    benutzerAktivStatus: fb.control<EntityAktivStatus | null>(EntityAktivStatus.AKTIV),
    rolle: fb.control<FachRolle | null>(null),
    organisation: fb.control<string | null>(null),
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

  onTextInput(group.controls.vorname, config.destroyRef).subscribe({
    next: vorname => {
      config.paginatedTable.onFilterChanged({
        filterValue: vorname ?? '',
        filterMethod: FilterMethod.SUBSTRING,
        filterCombinationMethod: FilterCombinationMethod.AND,
        filterName: 'data.vorname',
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

  onTextInput(group.controls.organisation, config.destroyRef).subscribe({
    next: organisation => {
      config.paginatedTable.onFilterChanged({
        filterValue: organisation ?? '',
        filterMethod: FilterMethod.SUBSTRING,
        filterCombinationMethod: FilterCombinationMethod.AND,
        filterName: 'data.organisationRollen.organisation.verantwortlicherArztFullname',
      });
    },
    error: console.error,
  });

  group.controls.benutzerAktivStatus.valueChanges.pipe(
    startWith(group.controls.benutzerAktivStatus.value),
    distinctUntilChanged(),
    takeUntilDestroyed(config.destroyRef)
  )
    .subscribe({
      next: benutzerAktivStatus => {
        config.paginatedTable.onFilterChanged({
          filterValue: benutzerAktivStatus ?? '',
          filterMethod: FilterMethod.ENUM,
          filterCombinationMethod: FilterCombinationMethod.AND,
          filterName: 'data.benutzerAktivStatus',
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
        filterName: 'fachRollen',
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
