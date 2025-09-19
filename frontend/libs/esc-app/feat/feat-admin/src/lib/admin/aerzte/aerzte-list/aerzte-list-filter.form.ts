/*
 * Copyright (C) 2025 DV Bern AG, Switzerland
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

import {DestroyRef} from '@angular/core';
import {NonNullableFormBuilder} from '@angular/forms';
import {ArztJson, Fachrichtung, HpvTeilnahme} from '@dvb/data-access-backend';
import {BaseForm, FilterCombinationMethod, FilterMethod, onTextInput, TableModel} from '@dvb/util-angular';

export interface ArztFilterForm {
  fachrichtung: Fachrichtung[];
  vorname: string;
  name: string;
  hpvTeilnahme: HpvTeilnahme | undefined;
}

type Config = {
  fb: NonNullableFormBuilder;
  destroyRef: DestroyRef;
  paginatedTable: TableModel<ArztJson>;
};

function makeForm(config: Config) {
  const fb = config.fb;
  const group = fb.group({
    fachrichtung: fb.control<Fachrichtung[]>([]),
    vorname: fb.control<string>(''),
    name: fb.control<string>(''),
    adresseZusatz: fb.control<string>(''),
    strasseUndNr: fb.control<string>(''),
    plz: fb.control<string>(''),
    ort: fb.control<string>(''),
    hpvTeilnahme: fb.control<HpvTeilnahme | undefined>(undefined),
  });

  onTextInput(group.controls.fachrichtung, config.destroyRef).subscribe({
    next: fachrichtung => {
      config.paginatedTable.onFilterChanged({
        filterValue: fachrichtung.map(as => as.toString()).join(' ') ?? '',
        filterMethod: FilterMethod.ENUM,
        filterCombinationMethod: FilterCombinationMethod.OR,
        filterName: 'data.fachrichtung',
      })
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
      })
    },
    error: console.error,
  });

  onTextInput(group.controls.name, config.destroyRef).subscribe({
    next: name => {
      config.paginatedTable.onFilterChanged({
        filterValue: name ?? '',
        filterMethod: FilterMethod.SUBSTRING,
        filterCombinationMethod: FilterCombinationMethod.AND,
        filterName: 'data.name',
      })
    },
    error: console.error,
  });

  onTextInput(group.controls.adresseZusatz, config.destroyRef).subscribe({
    next: adresseZusatz => {
      config.paginatedTable.onFilterChanged({
        filterValue: adresseZusatz ?? '',
        filterMethod: FilterMethod.SUBSTRING,
        filterCombinationMethod: FilterCombinationMethod.AND,
        filterName: 'data.adresse.adresseZusatz',
      })
    },
    error: console.error,
  });

  onTextInput(group.controls.strasseUndNr, config.destroyRef).subscribe({
    next: strasseUndNr => {
      config.paginatedTable.onFilterChanged({
        filterValue: strasseUndNr ?? '',
        filterMethod: FilterMethod.SUBSTRING,
        filterCombinationMethod: FilterCombinationMethod.AND,
        filterName: 'data.adresse.strasseUndNr',
      })
    },
    error: console.error,
  });

  onTextInput(group.controls.plz, config.destroyRef).subscribe({
    next: plz => {
      config.paginatedTable.onFilterChanged({
        filterValue: plz ?? '',
        filterMethod: FilterMethod.SUBSTRING,
        filterCombinationMethod: FilterCombinationMethod.AND,
        filterName: 'data.adresse.plz',
      })
    },
    error: console.error,
  });

  onTextInput(group.controls.ort, config.destroyRef).subscribe({
    next: ort => {
      config.paginatedTable.onFilterChanged({
        filterValue: ort ?? '',
        filterMethod: FilterMethod.SUBSTRING,
        filterCombinationMethod: FilterCombinationMethod.AND,
        filterName: 'data.adresse.ort',
      })
    },
    error: console.error,
  });


  onTextInput(group.controls.hpvTeilnahme, config.destroyRef).subscribe({
    next: hpvTeilname => {
      config.paginatedTable.onFilterChanged({
        filterValue: hpvTeilname ?? '',
        filterMethod: FilterMethod.ENUM,
        filterCombinationMethod: FilterCombinationMethod.AND,
        filterName: 'data.hpvTeilnahme',
      })
    },
    error: console.error,
  });

  return group;
}

export class AerzteListFilterForm extends BaseForm<ReturnType<typeof makeForm>, ArztFilterForm> {
  constructor(config: Config) {
    super(() => makeForm(config));
  }

  override valueOnValid(): ArztFilterForm {
    return this.group.getRawValue();
  }
}
