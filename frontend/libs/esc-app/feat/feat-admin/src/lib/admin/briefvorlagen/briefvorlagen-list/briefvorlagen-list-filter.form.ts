import {DestroyRef} from '@angular/core';
import {NonNullableFormBuilder} from '@angular/forms';
import {BenutzerOptionJson, BriefvorlageJson} from '@dvb/data-access-backend';
import {BaseForm, FilterCombinationMethod, FilterMethod, onTextInput, TableModel} from '@dvb/util-angular';

type Config = {
  fb: NonNullableFormBuilder;
  destroyRef: DestroyRef;
  paginatedTable: TableModel<BriefvorlageJson>;
};

function makeForm(config: Config) {
  const fb = config.fb;
  const group = fb.group({
    name: fb.control<string>(''),
    vorlageTyp: fb.control<string>(''),
    sprache: fb.control<string>(''),
    erstelltDurch: fb.control<BenutzerOptionJson[]>([]),
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

  onTextInput(group.controls.vorlageTyp, config.destroyRef).subscribe({
    next: vorlageTyp => {
      config.paginatedTable.onFilterChanged({
        filterValue: vorlageTyp ?? '',
        filterMethod: FilterMethod.SUBSTRING,
        filterCombinationMethod: FilterCombinationMethod.AND,
        filterName: 'data.type',
      });
    },
    error: console.error,
  });

  onTextInput(group.controls.sprache, config.destroyRef).subscribe({
    next: sprache => {
      config.paginatedTable.onFilterChanged({
        filterValue: sprache ?? '',
        filterMethod: FilterMethod.ENUM,
        filterCombinationMethod: FilterCombinationMethod.OR,
        filterName: 'data.sprache',
      });
    },
    error: console.error,
  });

  onTextInput(group.controls.erstelltDurch, config.destroyRef).subscribe({
    next: erstelltDurch => {
      config.paginatedTable.onFilterChanged({
        filterValue: erstelltDurch.map(person => person.id).join(' ') ?? '',
        filterMethod: FilterMethod.SUBSTRING,
        filterCombinationMethod: FilterCombinationMethod.OR,
        filterName: 'entityInfo.userErstellt',
      });
    },
    error: console.error,
  });

  return group;
}

export class BriefvorlagenListFilterForm extends BaseForm<
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
