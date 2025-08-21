import {CommonModule} from '@angular/common';
import {ChangeDetectionStrategy, Component, computed, inject} from '@angular/core';
import {EntityAktivStatus} from '@dvb/data-access-backend';
import {BenutzerStore, KlasseJsonEnhanced, KlassenStore, Path} from '@dvb/feat-shared';
import {
  ButtonsRightContainerComponent,
  CardComponent,
  ColWidthComponent,
  EmptyTableMessageComponent,
  HeadingDirective,
  PageHeaderAddButtonComponent,
  PageHeaderListSummaryComponent,
  SortableComponent,
  SummaryListItem,
  TableComponent,
  TableLinkComponent,
} from '@dvb/ui-components';
import {TableModel} from '@dvb/util-angular';
import {hasListEditBerechtigung} from '@dvb/util-shared';
import {TranslocoPipe, TranslocoService} from '@jsverse/transloco';

@Component({
  selector: 'feat-schulen-detail-klassen-list',
  imports: [
    CommonModule,
    CardComponent,
    EmptyTableMessageComponent,
    SortableComponent,
    TableComponent,
    TranslocoPipe,
    ButtonsRightContainerComponent,
    HeadingDirective,
    PageHeaderAddButtonComponent,
    PageHeaderListSummaryComponent,
    TableLinkComponent,
    ColWidthComponent,
  ],
  templateUrl: './schulen-detail-klassen-list.component.html',
  styleUrl: './schulen-detail-klassen-list.component.scss',
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class SchulenDetailKlassenListComponent {
  readonly klassenStore = inject(KlassenStore);
  readonly benutzerStore = inject(BenutzerStore);
  readonly #transloco = inject(TranslocoService);
  protected readonly Path = Path;
  protected readonly EntityAktivStatus = EntityAktivStatus;

  canEditList = this.benutzerStore.createHasRequiredRoleSig(hasListEditBerechtigung());
  itemTable = new TableModel<KlasseJsonEnhanced>(
    {rawListSignal: this.klassenStore.entities},
    {frontendOrBackend: 'frontend', pageOrScrollOrNothing: 'scroll'});

  summaryItems = computed(() => {
    const items: SummaryListItem[] = [
      {
        name: this.#transloco.translate('klasse.list.summary.klassen'),
        totalCount: this.itemTable.rawListSize(),
        filteredCount: this.itemTable.filteredListSize(),
      },
    ];
    return items;
  });
}
