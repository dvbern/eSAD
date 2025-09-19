import { ChangeDetectionStrategy, Component, computed, DestroyRef, inject, OnInit } from '@angular/core';
import {
  AufgabeJsonEnhanced,
  AufgabeListFilterForm,
  AufgabenListComponent,
  AufgabenListFilter,
  AufgabenStore,
  BenutzerStore,
  mapSessionStorageStringToFilter as mapSessionFilterHelper,
  PageComponent,
  Path,
} from '@dvb/feat-shared';
import { PageHeaderAddButtonComponent, PageHeaderButtonsComponent, PageHeaderListSummaryComponent, SummaryListItem } from '@dvb/ui-components';
import { hasAufgabenEditBerechtigungen } from '@dvb/util-shared';
import { TranslocoPipe, TranslocoService } from '@jsverse/transloco';
import { DossierStore } from '../../dossier.store';
import { FilterSessionStorage, TableModel } from '@dvb/util-angular';
import { NonNullableFormBuilder } from '@angular/forms';

@Component({
  selector: 'feat-kinder-aufgaben.page',
  templateUrl: './kinder-aufgaben-liste.page.component.html',
  styles: ':host { display:block;}',
  changeDetection: ChangeDetectionStrategy.OnPush,
  imports: [
    AufgabenListComponent,
    PageComponent,
    TranslocoPipe,
    PageHeaderAddButtonComponent,
    PageHeaderButtonsComponent,
    PageHeaderListSummaryComponent,
  ],
})
export class KinderAufgabenListePageComponent extends FilterSessionStorage<AufgabenListFilter> implements OnInit {

  readonly store = inject(AufgabenStore);
  readonly dossierStore = inject(DossierStore);
  readonly #transloco = inject(TranslocoService);
  readonly benutzerStore = inject(BenutzerStore);

  private readonly hasRoleSig = this.benutzerStore.createHasRequiredRoleSig(hasAufgabenEditBerechtigungen());
  canEditAufgabe = computed(() => this.hasRoleSig() && this.dossierStore.isKindAktiv());

  readonly SESSIONSTORAGE_KEY = 'aufgabenFilter';
  protected readonly Path = Path;

  mapSessionStorageStringToFilter(sessionStorageFilter: string): AufgabenListFilter | undefined {
    return mapSessionFilterHelper(sessionStorageFilter);
  }

  itemTable = new TableModel<AufgabeJsonEnhanced>(
    {
      rawListSignal: this.store.entities,
      lazyLoadOnSortOrFilter: () => this.setSessionStorageFilter(this.tableFilterForm.valueOnValid()),
    },
    { frontendOrBackend: 'frontend', pageOrScrollOrNothing: 'scroll' },
  );

  readonly tableFilterForm = new AufgabeListFilterForm({
    fb: inject(NonNullableFormBuilder),
    destroyRef: inject(DestroyRef),
    paginatedTable: this.itemTable,
  });

  summaryItems = computed(() => {
    const items: SummaryListItem[] = [];
    items.push({
      name: this.#transloco.translate('aufgaben.list.page.title'),
      totalCount: this.itemTable.rawListSize(),
      filteredCount: this.itemTable.filteredListSize(),
    });
    return items;
  });

  ngOnInit() {
    const filter = this.getSessionStorageFilter();
    if (filter) {
        this.tableFilterForm.reset(filter);
      }
  }
}
