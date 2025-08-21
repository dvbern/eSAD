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

import {ChangeDetectionStrategy, Component, DestroyRef, effect, inject, input, OnInit, output} from '@angular/core';
import {FormsModule, NonNullableFormBuilder, ReactiveFormsModule} from '@angular/forms';
import {ArztJson, Fachrichtung, HpvTeilnahme} from '@dvb/data-access-backend';
import {BackendEntityId} from '@dvb/data-access-types';
import {
  CollapsibleMenuComponent,
  EmptyTableMessageComponent,
  NgSelectDirective,
  SortableComponent,
  TableComponent,
  TableFilterComponent,
  TableLinkComponent,
  TextInputDirective,
} from '@dvb/ui-components';
import {FilterSessionStorage, TableModel} from '@dvb/util-angular';
import {ensureProps, isValidEnumValue, toValidEnumList} from '@dvb/util-shared';
import {TranslocoPipe} from '@jsverse/transloco';
import {NgbDropdownItem} from '@ng-bootstrap/ng-bootstrap';
import {NgOptionComponent, NgSelectComponent} from '@ng-select/ng-select';
import {AerzteListFilterForm, ArztFilterForm} from './aerzte-list-filter.form';

@Component({
  selector: 'feat-aerzte-list',
  templateUrl: 'aerzte-list.component.html',
  standalone: true,
  imports: [
    TableComponent,
    EmptyTableMessageComponent,
    TranslocoPipe,
    SortableComponent,
    FormsModule,
    TableFilterComponent,
    TextInputDirective,
    ReactiveFormsModule,
    NgSelectComponent,
    NgSelectDirective,
    NgOptionComponent,
    TableLinkComponent,
    CollapsibleMenuComponent,
    NgbDropdownItem,
  ],
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class AerzteListComponent extends FilterSessionStorage<ArztFilterForm> implements OnInit {
  tableData = input.required<ArztJson[]>();
  isLoading = input(false);
  isDeleteLoading = input(false);
  canEditList = input(false);
  removeArzt = output<ArztJson>();
  filteredArztIds = output<BackendEntityId[]>();

  readonly fachrichtungen = Object.values(Fachrichtung);
  readonly hpvTeilnamen = Object.values(HpvTeilnahme);

  readonly SESSIONSTORAGE_KEY = 'aerzteFilter';

  itemTable = new TableModel<ArztJson>(
    {
      rawListSignal: this.tableData,
      lazyLoadOnSortOrFilter: () => this.setSessionStorageFilter(this.tableFilterForm.valueOnValid()),
    },
    {frontendOrBackend: 'frontend', pageOrScrollOrNothing: 'scroll'},
  );

  constructor() {
    super();
    effect(() => {
      this.filteredArztIds.emit(this.itemTable.filteredSortedList().map(({id}) => id));
    });
  }

  ngOnInit() {
    const filter = this.getSessionStorageFilter();
    if (filter) {
      this.tableFilterForm.reset(filter);
    }
  }

  readonly tableFilterForm = new AerzteListFilterForm({
    fb: inject(NonNullableFormBuilder),
    destroyRef: inject(DestroyRef),
    paginatedTable: this.itemTable,
  });

  mapSessionStorageStringToFilter(sessionStorageFilter: string): ArztFilterForm | undefined {
    try {
      const parsed = JSON.parse(sessionStorageFilter) as ArztFilterForm;

      // Reconstruct the filter with proper data types and validation
      return ensureProps<ArztFilterForm>({
        fachrichtung: toValidEnumList(Fachrichtung, parsed.fachrichtung),
        hpvTeilnahme: isValidEnumValue(HpvTeilnahme,parsed.hpvTeilnahme) ? parsed.hpvTeilnahme : undefined,
        name: parsed.name ?? '',
        vorname: parsed.vorname ?? '',
      });
    } catch (error) {
      console.warn('Failed to parse session storage filter:', error);
      return undefined;
    }
  }
}
