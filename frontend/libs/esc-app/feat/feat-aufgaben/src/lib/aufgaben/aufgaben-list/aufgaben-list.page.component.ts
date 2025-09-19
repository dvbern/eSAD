import {CommonModule} from '@angular/common';
import { ChangeDetectionStrategy, Component, computed, DestroyRef, inject, OnInit } from '@angular/core';
import { NonNullableFormBuilder, ReactiveFormsModule } from '@angular/forms';
import {
  AufgabeJsonEnhanced,
  AufgabeListFilterForm,
  AufgabenListFilter,
  AufgabenStore,
  BenutzerOptionsStore,
  mapSessionStorageStringToFilter as mapSessionFilterHelper,
} from '@dvb/feat-shared';
import {BenutzerStore} from '@dvb/feat-shared';
import {Path} from '@dvb/feat-shared';
import {PageComponent} from '@dvb/feat-shared';
import {AufgabenListComponent} from '@dvb/feat-shared';
import {PageHeaderAddButtonComponent, PageHeaderButtonsComponent, PageHeaderListSummaryComponent, SummaryListItem } from '@dvb/ui-components';
import { hasAufgabenEditBerechtigungen } from '@dvb/util-shared';
import { TranslocoPipe, TranslocoService } from '@jsverse/transloco';
import { FilterSessionStorage, TableModel } from '@dvb/util-angular';

@Component({
  selector: 'feat-aufgaben',
  changeDetection: ChangeDetectionStrategy.OnPush,
  imports: [
    CommonModule,
    PageComponent,
    TranslocoPipe,
    ReactiveFormsModule,
    PageHeaderAddButtonComponent,
    AufgabenListComponent,
    PageHeaderButtonsComponent,
    PageHeaderListSummaryComponent,
  ],
  templateUrl: './aufgaben-list.page.component.html',
  styleUrl: './aufgaben-list.page.component.scss',
})
export class AufgabenListPageComponent extends FilterSessionStorage<AufgabenListFilter> implements OnInit {

  readonly store = inject(AufgabenStore);
  readonly benutzerStore = inject(BenutzerStore);
  readonly benutzerOptionsStore = inject(BenutzerOptionsStore);
  readonly #transloco = inject(TranslocoService);

  canEditAufgabe = this.benutzerStore.createHasRequiredRoleSig(hasAufgabenEditBerechtigungen());

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
