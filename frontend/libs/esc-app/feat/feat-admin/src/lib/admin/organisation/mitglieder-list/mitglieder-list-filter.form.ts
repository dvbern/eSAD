import {FachRolle} from "@dvb/data-access-backend";
import {NonNullableFormBuilder} from "@angular/forms";
import {DestroyRef} from "@angular/core";
import {BaseForm, FilterCombinationMethod, FilterMethod, onTextInput, TableModel} from "@dvb/util-angular";
import {distinctUntilChanged} from "rxjs";
import {takeUntilDestroyed} from "@angular/core/rxjs-interop";
import {BenutzerEnhancedJson} from "@dvb/feat-shared";

export interface OrganisationMitgliederFilter {
  vorname: string;
  nachname: string;
  email: string;
  fachRollen: FachRolle[]
}

type Config = {
  fb: NonNullableFormBuilder;
  destroyRef: DestroyRef;
  paginatedTable: TableModel<BenutzerEnhancedJson>
}

function makeForm(config: Config) {
  const fb = config.fb;
  const group = fb.group({
    vorname: fb.control<string>(''),
    nachname: fb.control<string>(''),
    email: fb.control<string>(''),
    fachRollen: fb.control<FachRolle[]>([]),
  });

  onTextInput(group.controls.vorname, config.destroyRef).subscribe({
    next: vorname => {
      config.paginatedTable.onFilterChanged({
        filterValue: vorname ?? '',
        filterMethod: FilterMethod.SUBSTRING,
        filterCombinationMethod: FilterCombinationMethod.AND,
        filterName: 'data.vorname',
      })
    }
  });

  onTextInput(group.controls.nachname, config.destroyRef).subscribe({
    next: nachname => {
      config.paginatedTable.onFilterChanged({
        filterValue: nachname ?? '',
        filterMethod: FilterMethod.SUBSTRING,
        filterCombinationMethod: FilterCombinationMethod.AND,
        filterName: 'data.name',
      })
    }
  });

  onTextInput(group.controls.email, config.destroyRef).subscribe({
    next: email => {
      config.paginatedTable.onFilterChanged({
        filterValue: email ?? '',
        filterMethod: FilterMethod.SUBSTRING,
        filterCombinationMethod: FilterCombinationMethod.AND,
        filterName: 'data.email',
      })
    }
  });

  group.controls.fachRollen.valueChanges.pipe(distinctUntilChanged(), takeUntilDestroyed(config.destroyRef))
    .subscribe({
      next: fachRollen => {
        config.paginatedTable.onFilterChanged({
          filterValue: fachRollen.map(as => as.toString()).join(' ') ?? '',
          filterMethod: FilterMethod.ENUM,
          filterCombinationMethod: FilterCombinationMethod.OR,
          filterName: 'fachRollen',
        });
      },
      error: console.error,
    });

  return group;
}

export class OrganisationMitgliederListFilterForm extends BaseForm<
  ReturnType<typeof makeForm>,
  OrganisationMitgliederFilter
>{
  constructor(config: Config) {
    super(() => makeForm(config));
  }

  valueOnValid(): OrganisationMitgliederFilter {
    return this.group.getRawValue();
  }
}
