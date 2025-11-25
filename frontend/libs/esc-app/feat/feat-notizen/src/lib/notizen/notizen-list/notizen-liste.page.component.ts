import {ChangeDetectionStrategy, Component, computed, DestroyRef, inject} from '@angular/core';
import {NonNullableFormBuilder} from '@angular/forms';
import {ActivatedRoute, Router} from '@angular/router';
import {BackendEntityId} from '@dvb/data-access-types';
import {NotizenListFilterForm, NotizJsonEnhanced, NotizListComponent, PageComponent, Path} from '@dvb/feat-shared';
import {PageHeaderButtonsComponent, PageHeaderListSummaryComponent, SummaryListItem} from '@dvb/ui-components';
import {TableModel} from '@dvb/util-angular';
import {TranslocoPipe, TranslocoService} from '@jsverse/transloco';
import {NotizenListStore} from "../notizen-list.store";

@Component({
  selector: 'feat-notizen-list.page',
  templateUrl: './notizen-liste.page.component.html',
  styles: ':host { display:block;}',
  changeDetection: ChangeDetectionStrategy.OnPush,
  imports: [
    PageComponent,
    TranslocoPipe,
    NotizListComponent,
    PageHeaderButtonsComponent,
    PageHeaderListSummaryComponent,
  ],
})
export class NotizenListePageComponent {

  readonly notizenListStore = inject(NotizenListStore);
  readonly #router = inject(Router);
  readonly #route = inject(ActivatedRoute);
  readonly #transloco = inject(TranslocoService);

  protected readonly Path = Path;

  notizenTable = new TableModel<NotizJsonEnhanced>({rawListSignal: this.notizenListStore.entities},
    {frontendOrBackend: 'frontend', pageOrScrollOrNothing: 'scroll'});
  readonly tableFilterForm = new NotizenListFilterForm({
    fb: inject(NonNullableFormBuilder),
    destroyRef: inject(DestroyRef),
    paginatedTable: this.notizenTable,
  });

  summaryItems = computed((): SummaryListItem[] => [
    {
      name: this.#transloco.translate('kind.dossier.notizen', {}),
      totalCount: this.notizenTable.rawListSize(),
      filteredCount: this.notizenTable.filteredListSize(),
    },
  ]);

  async goToNotiz(notizId: BackendEntityId) {
    await this.#router.navigate([notizId], {relativeTo: this.#route});
  }

  async removeNotiz(notizId: BackendEntityId) {
    this.notizenListStore.removeNotiz({
      notizId: notizId,
    });
  }
}
