import {OrganisationJson} from "@dvb/data-access-backend";
import {NonNullableFormBuilder} from "@angular/forms";
import {DestroyRef} from "@angular/core";
import {BaseForm, FilterCombinationMethod, FilterMethod, onTextInput, TableModel} from "@dvb/util-angular";

export interface OrganisationFilterForm {
  verantwortlicherArzt: string;
}

type Config = {
  fb: NonNullableFormBuilder;
  destroyRef: DestroyRef;
  paginatedTable: TableModel<OrganisationJson>;
};

function makeForm(config: Config) {
  const fb = config.fb;
  const group = fb.group({
    verantwortlicherArzt: fb.control<string>(''),
  });

  onTextInput(group.controls.verantwortlicherArzt, config.destroyRef).subscribe({
    next: verantwortlicherArzt => {
      config.paginatedTable.onFilterChanged({
        filterValue: verantwortlicherArzt ?? '',
        filterMethod: FilterMethod.SUBSTRING,
        filterCombinationMethod: FilterCombinationMethod.AND,
        filterName: 'verantwortlicherArztFullname',
      })
    },
    error: console.error,
  });

  return group;
}

export class OrganisationListFilterForm extends BaseForm<ReturnType<typeof makeForm>, OrganisationFilterForm> {
  constructor(config: Config) {
    super(() => makeForm(config));
  }

  override valueOnValid(): OrganisationFilterForm {
    return this.group.getRawValue();
  }
}
