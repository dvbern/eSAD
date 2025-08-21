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

import { ChangeDetectionStrategy, Component, computed, inject, signal } from '@angular/core';
import { ArztJson } from '@dvb/data-access-backend';
import { BackendEntityId } from '@dvb/data-access-types';
import { AerzteStore } from '@dvb/feat-shared';
import { BenutzerStore } from '@dvb/feat-shared';
import { Path } from '@dvb/feat-shared';
import { ConfirmService } from '@dvb/feat-shared';
import { PageComponent } from '@dvb/feat-shared';
import {
  CardComponent,
  IconOnlyButtonComponent,
  PageHeaderAddButtonComponent,
  PageHeaderButtonsComponent,
  PageHeaderListSummaryComponent,
  SummaryListItem,
} from '@dvb/ui-components';
import { AppNotificationStore } from '@dvb/util-angular';
import { hasAerztePageEditBerechtigung } from '@dvb/util-shared';
import { TranslocoPipe, TranslocoService } from '@jsverse/transloco';
import { AerzteListComponent } from './aerzte-list.component';

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
export class AerzteListPageComponent {
  protected readonly Path = Path;
  readonly #transloco = inject(TranslocoService);
  readonly #aerzteStore = inject(AerzteStore);
  readonly #benutzerStore = inject(BenutzerStore);
  readonly #confirmService = inject(ConfirmService);
  readonly #appNotificationStore = inject(AppNotificationStore);

  arztList = this.#aerzteStore.entities;
  canEditList = this.#benutzerStore.createHasRequiredRoleSig(hasAerztePageEditBerechtigung());

  filteredArztIds = signal<BackendEntityId[]>([]);

  summaryItems = computed((): SummaryListItem[] => [
    {
      name: this.#transloco.translate('aerzte.list.summary', {}),
      totalCount: this.arztList().length,
      filteredCount: this.filteredArztIds().length,
    },
  ]);

  public exportFilteredAerzte() {
    this.#aerzteStore.exportAerzteAsDoc({
      aerzteIds: this.filteredArztIds(),
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
        this.#aerzteStore.removeArzt(item.id);
      });
  }
}
