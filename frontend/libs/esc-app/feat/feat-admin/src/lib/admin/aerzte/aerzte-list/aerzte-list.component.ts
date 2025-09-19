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

import {ChangeDetectionStrategy, Component, input, output} from '@angular/core';
import {FormsModule, ReactiveFormsModule} from '@angular/forms';
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
import {TableModel} from '@dvb/util-angular';
import {TranslocoPipe} from '@jsverse/transloco';
import {NgbDropdownItem} from '@ng-bootstrap/ng-bootstrap';
import {NgOptionComponent, NgSelectComponent} from '@ng-select/ng-select';
import {AerzteListFilterForm} from './aerzte-list-filter.form';

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
export class AerzteListComponent {
  itemTable = input.required<TableModel<ArztJson>>();
  tableFilterForm = input.required<AerzteListFilterForm>();
  isLoading = input(false);
  isDeleteLoading = input(false);
  canEditList = input(false);
  removeArzt = output<ArztJson>();
  filteredArztIds = output<BackendEntityId[]>();

  readonly fachrichtungen = Object.values(Fachrichtung);
  readonly hpvTeilnamen = Object.values(HpvTeilnahme);
}
