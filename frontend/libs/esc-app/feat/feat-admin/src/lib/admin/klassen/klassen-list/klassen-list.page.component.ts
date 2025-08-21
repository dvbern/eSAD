import {CommonModule} from '@angular/common';
import {ChangeDetectionStrategy, Component, computed, inject} from '@angular/core';
import {KlassenStore} from '@dvb/feat-shared';
import {BenutzerStore} from '@dvb/feat-shared';
import {Path} from '@dvb/feat-shared';
import {PageComponent} from '@dvb/feat-shared';
import {
  PageHeaderAddButtonComponent,
  PageHeaderButtonsComponent,
  PageHeaderListSummaryComponent,
  SummaryListItem,
} from '@dvb/ui-components';
import {hasListEditBerechtigung} from '@dvb/util-shared';
import {TranslocoPipe, TranslocoService} from '@jsverse/transloco';
import {KlassenListComponent} from './klassen-list.component';

@Component({
  selector: 'feat-klassen-list.page',
  imports: [
    CommonModule,
    PageComponent,
    KlassenListComponent,
    TranslocoPipe,
    PageHeaderButtonsComponent,
    PageHeaderAddButtonComponent,
    PageHeaderListSummaryComponent,
  ],
  templateUrl: './klassen-list.page.component.html',
  styleUrl: './klassen-list.page.component.scss',
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class KlassenListPageComponent {
  protected readonly Path = Path;
  readonly #benutzerStore = inject(BenutzerStore);
  readonly #transloco = inject(TranslocoService);
  readonly #klassenStore = inject(KlassenStore);

  canEditList = this.#benutzerStore.createHasRequiredRoleSig(hasListEditBerechtigung());

  summaryItems = computed(() => {
    const items: SummaryListItem[] = [
      {
        name: this.#transloco.translate('klasse.list.summary.anzahl.total'),
        totalCount: this.#klassenStore.entities().length,
        filteredCount: this.#klassenStore.entities().length,
      },
    ];
    return items;
  });
}
