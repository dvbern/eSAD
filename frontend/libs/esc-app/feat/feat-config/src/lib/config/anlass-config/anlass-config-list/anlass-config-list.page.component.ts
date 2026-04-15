import {ChangeDetectionStrategy, Component, computed, DestroyRef, inject, OnInit} from '@angular/core';
import {NonNullableFormBuilder} from '@angular/forms';
import {AnlassConfigJson, AnlassTyp} from '@dvb/data-access-backend';
import {BenutzerStore, PageComponent, Path, TenantSettingsStore} from '@dvb/feat-shared';
import {
  IconOnlyButtonComponent,
  PageHeaderAddButtonComponent,
  PageHeaderButtonsComponent,
  PageHeaderListSummaryComponent,
  SummaryListItem,
} from '@dvb/ui-components';
import {FilterSessionStorage, TableModel} from '@dvb/util-angular';
import {ensureProps, isNotNullish, toValidEnumList} from '@dvb/util-shared';
import {TranslocoPipe, TranslocoService} from '@jsverse/transloco';
import {AnlassConfigStore} from '../anlass-config.store';
import {AnlassConfigFilterForm, AnlassConfigListFilterForm} from './anlass-config-list-filter.form';
import {AnlassConfigListComponent} from './list/anlass-config-list.component';
import {AnlassConfigHelperService} from "../anlass-config-helper.service";

@Component({
  selector: 'feat-anlass-config-list-page',
  imports: [
    PageComponent,
    TranslocoPipe,
    PageHeaderAddButtonComponent,
    PageHeaderButtonsComponent,
    PageHeaderListSummaryComponent,
    AnlassConfigListComponent,
    IconOnlyButtonComponent,
  ],
  templateUrl: './anlass-config-list.page.component.html',
  styleUrl: './anlass-config-list.page.component.scss',
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class AnlassConfigListPageComponent extends FilterSessionStorage<AnlassConfigFilterForm> implements OnInit {
  protected readonly Path = Path;
  readonly store = inject(AnlassConfigStore);
  readonly tenantSettingsStore = inject(TenantSettingsStore);
  readonly benutzerStore = inject(BenutzerStore);
  readonly #translocoService = inject(TranslocoService);
  readonly anlassConfigHelperService = inject(AnlassConfigHelperService);

  pageTitle = computed(() => {
    const mandant = isNotNullish(this.tenantSettingsStore.settings())
      ? this.tenantSettingsStore.settings()?.tenant
      : '?';

    return this.#translocoService.translate('anlassconfig.list.page.title', { mandant });
  });

  readonly SESSIONSTORAGE_KEY = 'anlassConfigFilter';

  itemTable = new TableModel<AnlassConfigJson>(
    {
      rawListSignal: this.store.entities,
      lazyLoadOnSortOrFilter: () => this.setSessionStorageFilter(this.tableFilterForm.valueOnValid()),
    },
    { frontendOrBackend: 'frontend', pageOrScrollOrNothing: 'scroll' },
  );

  readonly tableFilterForm = new AnlassConfigListFilterForm({
    fb: inject(NonNullableFormBuilder),
    destroyRef: inject(DestroyRef),
    paginatedTable: this.itemTable,
  });

  summaryItems = computed(() => {
    const items: SummaryListItem[] = [];
    items.push({
      name: this.pageTitle(),
      totalCount: this.itemTable.rawListSize(),
      filteredCount: this.itemTable.filteredListSize(),
    });
    return items;
  });

  ngOnInit(): void {
    const filter = this.getSessionStorageFilter();
    if (filter) {
      this.tableFilterForm.reset(filter);
    }
  }

  mapSessionStorageStringToFilter(sessionStorageFilter: string): AnlassConfigFilterForm | undefined {
    try {
      const parsed = JSON.parse(sessionStorageFilter) as AnlassConfigFilterForm;

      return ensureProps<AnlassConfigFilterForm>({
        anlassTyp: toValidEnumList(AnlassTyp, parsed.anlassTyp),
        bezeichnung: parsed.bezeichnung,
        datumVon: parsed.datumVon ?? null,
        datumBis: parsed.datumBis ?? null,
        anlassConfigStatus: parsed.anlassConfigStatus,
        anlassConfigVisible: parsed.anlassConfigVisible,
      });
    } catch (error) {
      console.warn('Failed to parse session storage filter:', error);
      return undefined;
    }
  }

  async downloadAllSqlSkripts(): Promise<void> {
    this.store.downloadAllSqlSkripts();
  }
}
