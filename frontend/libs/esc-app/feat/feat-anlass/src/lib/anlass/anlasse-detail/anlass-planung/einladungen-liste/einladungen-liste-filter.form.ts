import {DestroyRef} from '@angular/core';
import {takeUntilDestroyed} from '@angular/core/rxjs-interop';
import {NonNullableFormBuilder} from '@angular/forms';
import {EinladungStatus, Geschlecht, Klassenstufe, NaechsteStation} from '@dvb/data-access-backend';
import {ISODateString, ISODateTimeString} from '@dvb/data-access-types';
import {BaseForm, FilterCombinationMethod, FilterMethod, onTextInput, TableModel} from '@dvb/util-angular';
import {distinctUntilChanged} from 'rxjs';
import {EinladungJsonEnhanced} from '../../anlass-kinder.store';
import {AnlassFlags} from './einladungen-liste.component';

type Config = {
  fb: NonNullableFormBuilder;
  destroyRef: DestroyRef;
  paginatedTable: TableModel<EinladungJsonEnhanced>;
};

function makeForm(config: Config) {
  const fb = config.fb;
  const group = fb.group({
    geschlecht: fb.control<Geschlecht | undefined>(undefined),
    name: fb.control<string>(''),
    vorname: fb.control<string>(''),
    geburtsdatum: fb.control<ISODateString | undefined>(undefined),
    sprache: fb.control<string>(''),
    idNr: fb.control<string>(''),
    klassenStufe: fb.control<Klassenstufe | undefined>(undefined),
    nachkontrolle: fb.control<AnlassFlags[]>([]),
    einladungStatus: fb.control<EinladungStatus | undefined>(undefined),
    naechsteStation: fb.control<NaechsteStation | undefined>(undefined),
    naechsteStationSeit: fb.control<ISODateTimeString | undefined>(undefined),
  });

  group.controls.geschlecht.valueChanges.pipe(distinctUntilChanged(), takeUntilDestroyed(config.destroyRef)).subscribe({
    next: geschlecht => {
      config.paginatedTable.onFilterChanged({
        filterValue: geschlecht ?? '',
        filterMethod: FilterMethod.ENUM,
        filterCombinationMethod: FilterCombinationMethod.AND,
        filterName: 'kind.data.geschlecht',
      });
    },
    error: console.error,
  });

  onTextInput(group.controls.name, config.destroyRef).subscribe({
    next: name => {
      config.paginatedTable.onFilterChanged({
        filterValue: name ?? '',
        filterMethod: FilterMethod.SUBSTRING,
        filterCombinationMethod: FilterCombinationMethod.AND,
        filterName: 'kind.data.name',
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
        filterName: 'kind.data.vorname',
      });
    },
    error: console.error,
  });

  group.controls.geburtsdatum.valueChanges.pipe(distinctUntilChanged(), takeUntilDestroyed(config.destroyRef))
    .subscribe({
      next: geburtsdatum => {
        config.paginatedTable.onFilterChanged({
          filterValue: geburtsdatum ?? '',
          filterMethod: FilterMethod.DATE,
          filterCombinationMethod: FilterCombinationMethod.AND,
          filterName: 'kind.data.geburtsdatum',
        });
      },
      error: console.error,
    });

  group.controls.einladungStatus.valueChanges.pipe(distinctUntilChanged(), takeUntilDestroyed(config.destroyRef))
    .subscribe({
      next: einladungStatus => {
        config.paginatedTable.onFilterChanged({
          filterValue: einladungStatus ?? '',
          filterMethod: FilterMethod.ENUM,
          filterCombinationMethod: FilterCombinationMethod.AND,
          filterName: 'data.einladungStatus',
        });
      },
      error: console.error,
    });

  group.controls.naechsteStation.valueChanges.pipe(distinctUntilChanged(), takeUntilDestroyed(config.destroyRef))
    .subscribe({
      next: naechsteStation => {
        config.paginatedTable.onFilterChanged({
          filterValue: naechsteStation ?? '',
          filterMethod: FilterMethod.ENUM,
          filterCombinationMethod: FilterCombinationMethod.AND,
          filterName: 'naechsteStation.naechsteStation',
        });
      },
      error: console.error,
    });

  group.controls.naechsteStationSeit.valueChanges.pipe(distinctUntilChanged(), takeUntilDestroyed(config.destroyRef))
    .subscribe({
      next: naechsteStationSeit => {
        config.paginatedTable.onFilterChanged({
          filterValue: naechsteStationSeit ?? '',
          filterMethod: FilterMethod.SUBSTRING,
          filterCombinationMethod: FilterCombinationMethod.AND,
          filterName: 'naechsteStation.naechsteStationSeit',
        });
      },
      error: console.error,
    });

  onTextInput(group.controls.sprache, config.destroyRef).subscribe({
    next: sprache => {
      config.paginatedTable.onFilterChanged({
        filterValue: sprache ?? '',
        filterMethod: FilterMethod.SUBSTRING,
        filterCombinationMethod: FilterCombinationMethod.AND,
        filterName: 'kind.data.erstsprache',
      });
    },
    error: console.error,
  });

  onTextInput(group.controls.idNr, config.destroyRef).subscribe({
    next: idNr => {
      config.paginatedTable.onFilterChanged({
        filterValue: idNr ?? '',
        filterMethod: FilterMethod.SUBSTRING,
        filterCombinationMethod: FilterCombinationMethod.AND,
        filterName: 'kind.esadId',
      });
    },
    error: console.error,
  });

  onTextInput(group.controls.nachkontrolle, config.destroyRef).subscribe({
    next: flags => {
      config.paginatedTable.onFilterChanged({
        filterValue: flags.map(as => as.toString()).join(' ') ?? '',
        filterMethod: FilterMethod.SUBSTRING,
        filterCombinationMethod: FilterCombinationMethod.OR,
        filterName: 'nachkontrolleString',
      });
    },
    error: console.error,
  });

  group.controls.klassenStufe.valueChanges.pipe(distinctUntilChanged(), takeUntilDestroyed(config.destroyRef))
    .subscribe({
      next: klassenStufe => {
        config.paginatedTable.onFilterChanged({
          filterValue: klassenStufe ?? '',
          filterMethod: FilterMethod.ENUM,
          filterCombinationMethod: FilterCombinationMethod.AND,
          filterName: 'kind.data.klassenstufe',
        });
      },
      error: console.error,
    });

  return group;
}

export class EinladungenListeFilterForm extends BaseForm<
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
