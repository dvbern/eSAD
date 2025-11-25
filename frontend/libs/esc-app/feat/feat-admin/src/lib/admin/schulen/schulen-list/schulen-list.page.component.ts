
import { ChangeDetectionStrategy, Component, computed, DestroyRef, inject, OnInit } from '@angular/core';
import { NonNullableFormBuilder } from '@angular/forms';
import { EntityAktivStatus, EscadaKlasseStufe, SchuleJson } from '@dvb/data-access-backend';
import { BenutzerStore, PageComponent, Path, SchulenStore } from '@dvb/feat-shared';
import {
  PageHeaderAddButtonComponent,
  PageHeaderButtonsComponent,
  PageHeaderListSummaryComponent,
  SummaryListItem,
} from '@dvb/ui-components';
import { FilterSessionStorage, TableModel } from '@dvb/util-angular';
import { ensureProps, hasListEditBerechtigung, isValidEnumValue } from '@dvb/util-shared';
import { TranslocoPipe, TranslocoService } from '@jsverse/transloco';
import { SchulenListComponent } from './list/schulen-list.component';
import { KlassenListFilter, SchulenListFilterForm } from './schulen-list-filter.form';

@Component({
  selector: 'feat-schulen-list-page',
  imports: [
    PageComponent,
    SchulenListComponent,
    TranslocoPipe,
    PageHeaderButtonsComponent,
    PageHeaderAddButtonComponent,
    PageHeaderListSummaryComponent
],
  templateUrl: './schulen-list.page.component.html',
  styleUrl: './schulen-list.page.component.scss',
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class SchulenListPageComponent extends FilterSessionStorage<KlassenListFilter> implements OnInit {
  readonly schulenStore = inject(SchulenStore);
  readonly benutzerStore = inject(BenutzerStore);
  protected readonly Path = Path;
  readonly #transloco = inject(TranslocoService);

  canEditList = this.benutzerStore.createHasRequiredRoleSig(hasListEditBerechtigung());

  readonly SESSIONSTORAGE_KEY = 'schulenFilter';

  itemTable = new TableModel<SchuleJson>(
    {
      rawListSignal: this.schulenStore.entities,
      lazyLoadOnSortOrFilter: () => this.setSessionStorageFilter(this.tableFilterForm.valueOnValid()),
    },
    { frontendOrBackend: 'frontend', pageOrScrollOrNothing: 'scroll' },
  );
  readonly tableFilterForm = new SchulenListFilterForm({
    fb: inject(NonNullableFormBuilder),
    destroyRef: inject(DestroyRef),
    paginatedTable: this.itemTable,
  });

  summaryItems = computed(() => {
    const items: SummaryListItem[] = [];
    items.push({
      name: this.#transloco.translate('schule.list.page.title'),
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

  mapSessionStorageStringToFilter(sessionStorageFilter: string): KlassenListFilter | undefined {
    try {
      const parsed = JSON.parse(sessionStorageFilter) as KlassenListFilter;

      // Reconstruct the filter with proper data types and validation
      return ensureProps<KlassenListFilter>({
        klassenstufen: Array.isArray(parsed.klassenstufen)
          ? parsed.klassenstufen.filter(klassenStufe => Object.values(EscadaKlasseStufe).includes(klassenStufe))
          : [],
        schulhaus: parsed.schulhaus ?? '',
        schulstandort: parsed.schulstandort ?? '',
        schulkreis: parsed.schulkreis ?? '',
        zustaendigArzt: parsed.zustaendigArzt ?? '',
        zustaendigMPA: parsed.zustaendigMPA ?? '',
        schuleAktivStatus: isValidEnumValue(EntityAktivStatus, parsed.schuleAktivStatus)
          ? parsed.schuleAktivStatus
          : undefined,
      });
    } catch (error) {
      console.warn('Failed to parse session storage filter:', error);
      return undefined;
    }
  }
}
