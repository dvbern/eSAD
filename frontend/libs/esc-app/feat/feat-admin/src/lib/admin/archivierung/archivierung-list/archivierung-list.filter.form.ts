import {
  ArchivdokumentJson,
  ArchivierungStatus,
  ArchivierungTyp,
} from "@dvb/data-access-backend";
import {NonNullableFormBuilder} from "@angular/forms";
import {DestroyRef} from "@angular/core";
import {BaseForm, FilterCombinationMethod, FilterMethod, onTextInput, TableModel} from "@dvb/util-angular";

export interface ArchivFilterForm {
  typ: ArchivierungTyp[];
  status: ArchivierungStatus[];
  betroffenesKind: string;
  datumArchivierung: string;
  datumDownload: string;
}

type Config = {
  fb: NonNullableFormBuilder;
  destroyRef: DestroyRef;
  paginatedTable: TableModel<ArchivdokumentJson>;
};

function makeForm(config: Config) {
  const fb = config.fb;
  const group = fb.group({
    betroffenesKind: fb.control<string>(''),
    typ: fb.control<ArchivierungTyp[]>([]),
    status: fb.control<ArchivierungStatus[]>([]),
    datumArchivierung: fb.control<string>(''),
    datumDownload: fb.control<string>('')
  });

  onTextInput(group.controls.betroffenesKind, config.destroyRef).subscribe({
    next: betroffenesKind => {
      config.paginatedTable.onFilterChanged({
        filterValue: betroffenesKind ?? '',
        filterMethod: FilterMethod.SUBSTRING,
        filterCombinationMethod: FilterCombinationMethod.AND,
        filterName: 'betroffenesKind',
      })
    }
  });

  onTextInput(group.controls.typ, config.destroyRef).subscribe({
    next: typ => {
      config.paginatedTable.onFilterChanged({
        filterValue: typ.map(t => t.toString()).join(' ') ?? '',
        filterMethod: FilterMethod.ENUM,
        filterCombinationMethod: FilterCombinationMethod.OR,
        filterName: 'archivierungTyp',
      })
    }
  });

  onTextInput(group.controls.status, config.destroyRef).subscribe({
    next: status => {
      config.paginatedTable.onFilterChanged({
        filterValue: status.map(s => s.toString()).join(' ') ?? '',
        filterMethod: FilterMethod.ENUM,
        filterCombinationMethod: FilterCombinationMethod.OR,
        filterName: 'archivierungStatus',
      })
    }
  });

  onTextInput(group.controls.datumArchivierung, config.destroyRef).subscribe({
    next: datum => {
      config.paginatedTable.onFilterChanged({
        filterValue: datum ?? '',
        filterMethod: FilterMethod.DATE,
        filterCombinationMethod: FilterCombinationMethod.SINGLE,
        filterName: 'timestampArchiviert',
      })
    }
  });

  onTextInput(group.controls.datumDownload, config.destroyRef).subscribe({
    next: datum => {
      config.paginatedTable.onFilterChanged({
        filterValue: datum ?? '',
        filterMethod: FilterMethod.DATE,
        filterCombinationMethod: FilterCombinationMethod.SINGLE,
        filterName: 'timestampDownloaded',
      })
    }
  });

  return group;
}

export class ArchivierungListFilterForm extends BaseForm<ReturnType<typeof makeForm>, ArchivFilterForm> {
  constructor(config: Config) {
    super(() => makeForm(config));
  }

  override valueOnValid(): ArchivFilterForm {
    return this.group.getRawValue();
  }
}
