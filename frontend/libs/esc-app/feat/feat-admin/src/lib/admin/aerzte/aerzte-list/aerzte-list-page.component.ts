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

import {ChangeDetectionStrategy, Component, computed, inject, signal, DestroyRef, OnInit, effect} from '@angular/core';
import {ArztIdListJson, ArztJson, ExportMode, Fachrichtung, HpvTeilnahme} from '@dvb/data-access-backend';
import { BackendEntityId } from '@dvb/data-access-types';
import { AerzteStore, BenutzerStore, ConfirmService, PageComponent, Path } from '@dvb/feat-shared';
import {
  CardComponent,
  IconOnlyButtonComponent,
  PageHeaderAddButtonComponent,
  PageHeaderButtonsComponent,
  PageHeaderListSummaryComponent,
  SummaryListItem,
} from '@dvb/ui-components';
import { AppNotificationStore, FilterSessionStorage, TableModel } from '@dvb/util-angular';
import {
  ensureProps,
  hasAerztePageEditBerechtigung,
  isValidEnumValue,
  toValidEnumList,
} from '@dvb/util-shared';
import { TranslocoPipe, TranslocoService } from '@jsverse/transloco';
import { AerzteListComponent } from './aerzte-list.component';
import { NonNullableFormBuilder } from '@angular/forms';
import { AerzteListFilterForm, ArztFilterForm } from './aerzte-list-filter.form';

@Component({
  selector: 'feat-aerzte-list.page',
  templateUrl: 'aerzte-list-page.component.html',
  standalone: true,
  changeDetection: ChangeDetectionStrategy.OnPush,
  imports: [
    PageComponent,
    TranslocoPipe,
    PageHeaderListSummaryComponent,
    AerzteListComponent,
    CardComponent,
    PageHeaderAddButtonComponent,
    IconOnlyButtonComponent,
    PageHeaderButtonsComponent,
  ],
})
export class AerzteListPageComponent extends FilterSessionStorage<ArztFilterForm> implements OnInit {
  protected readonly Path = Path;
  readonly #transloco = inject(TranslocoService);
  readonly aerzteStore = inject(AerzteStore);
  readonly #benutzerStore = inject(BenutzerStore);
  readonly #confirmService = inject(ConfirmService);
  readonly #appNotificationStore = inject(AppNotificationStore);

  arztList = this.aerzteStore.entities;
  canEditList = this.#benutzerStore.createHasRequiredRoleSig(hasAerztePageEditBerechtigung());

  filteredArztIds = signal<BackendEntityId[]>([]);

  readonly SESSIONSTORAGE_KEY = 'aerzteFilter';

  itemTable = new TableModel<ArztJson>(
    {
      rawListSignal: this.aerzteStore.entities,
      lazyLoadOnSortOrFilter: () => this.setSessionStorageFilter(this.tableFilterForm.valueOnValid()),
    },
    { frontendOrBackend: 'frontend', pageOrScrollOrNothing: 'scroll' },
  );

  readonly tableFilterForm = new AerzteListFilterForm({
    fb: inject(NonNullableFormBuilder),
    destroyRef: inject(DestroyRef),
    paginatedTable: this.itemTable,
  });

  summaryItems = computed((): SummaryListItem[] => [
    {
      name: this.#transloco.translate('aerzte.list.summary', {}),
      totalCount: this.arztList().length,
      filteredCount: this.filteredArztIds().length,
    },
  ]);

  ExportMode =ExportMode;

  constructor() {
    super();
    effect(() => {
      const filterIds = this.itemTable.filteredSortedList().map(({id}) => id);
      this.filteredArztIds.set(filterIds);
    });
  }

  public exportFilteredAerzte(mode: ExportMode) {
    this.aerzteStore.exportAerzteAsDoc({
      mode,
      arztIdListJson: ensureProps<ArztIdListJson>({
        arztIdList: this.filteredArztIds()
      }),
      onSuccess: () => {
        this.#appNotificationStore.addSuccessNotification(
          this.#transloco.translate('aerzte.list.page.export.success')
        );
      },
    });
  }

  public removeArzt(item: ArztJson): void {
    this.#confirmService
      .confirm({
        question: this.#transloco.translate('aerzte.list.remove.question', { name: item.data.name }),
        consequence: this.#transloco.translate('aerzte.list.remove.consequence'),
        decision: this.#transloco.translate('aerzte.list.remove.decision'),
      })
      .subscribe(() => {
        this.aerzteStore.removeArzt(item.id);
      });
  }

  ngOnInit() {
    const filter = this.getSessionStorageFilter();
    if (filter) {
      this.tableFilterForm.reset(filter);
    }
  }

  mapSessionStorageStringToFilter(sessionStorageFilter: string): ArztFilterForm | undefined {
    try {
      const parsed = JSON.parse(sessionStorageFilter) as ArztFilterForm;

      return ensureProps<ArztFilterForm>({
        fachrichtung: toValidEnumList(Fachrichtung, parsed.fachrichtung),
        name: parsed.name ?? '',
        vorname: parsed.vorname ?? '',
        hpvTeilnahme: isValidEnumValue(HpvTeilnahme, parsed.hpvTeilnahme) ? parsed.hpvTeilnahme : undefined,
      });
    } catch (error) {
      console.warn('Failed to parse session storage filter:', error);
      return undefined;
    }
  }
}
