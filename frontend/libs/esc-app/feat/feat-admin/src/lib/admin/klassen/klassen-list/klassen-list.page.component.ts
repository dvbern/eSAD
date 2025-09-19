import { CommonModule } from '@angular/common';
import { ChangeDetectionStrategy, Component, computed, DestroyRef, inject, OnInit } from '@angular/core';
import { KlasseJsonEnhanced, KlassenStore } from '@dvb/feat-shared';
import { BenutzerStore } from '@dvb/feat-shared';
import { Path } from '@dvb/feat-shared';
import { PageComponent } from '@dvb/feat-shared';
import {
  PageHeaderAddButtonComponent,
  PageHeaderButtonsComponent,
  PageHeaderListSummaryComponent,
  SummaryListItem,
} from '@dvb/ui-components';
import { ensureProps, hasListEditBerechtigung, isValidEnumValue } from '@dvb/util-shared';
import { TranslocoPipe, TranslocoService } from '@jsverse/transloco';
import { KlassenListComponent } from './klassen-list.component';
import { FilterSessionStorage, TableModel } from '@dvb/util-angular';
import { NonNullableFormBuilder } from '@angular/forms';
import { KlassenListFilter, KlassenListFilterForm } from './klassen-list-filter.form';
import { EntityAktivStatus } from '@dvb/data-access-backend';

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
export class KlassenListPageComponent extends FilterSessionStorage<KlassenListFilter> implements OnInit {
  protected readonly Path = Path;
  readonly klassenStore = inject(KlassenStore);
  readonly #benutzerStore = inject(BenutzerStore);
  readonly #transloco = inject(TranslocoService);

  canEditList = this.#benutzerStore.createHasRequiredRoleSig(hasListEditBerechtigung());

  readonly SESSIONSTORAGE_KEY = 'klassenFilter';

  itemTable = new TableModel<KlasseJsonEnhanced>(
    {
      rawListSignal: this.klassenStore.entities,
      lazyLoadOnSortOrFilter: () => this.setSessionStorageFilter(this.tableFilterForm.valueOnValid()),
    },
    { frontendOrBackend: 'frontend', pageOrScrollOrNothing: 'scroll' },
  );
  readonly tableFilterForm = new KlassenListFilterForm({
    fb: inject(NonNullableFormBuilder),
    destroyRef: inject(DestroyRef),
    paginatedTable: this.itemTable,
  });

  summaryItems = computed(() => {
    const items: SummaryListItem[] = [];
    items.push({
      name: this.#transloco.translate('klasse.list.summary.anzahl.total'),
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
        schule: parsed.schule ?? '',
        bezeichnung: parsed.bezeichnung ?? '',
        klassenstufe: parsed.klassenstufe ?? '',
        klasseSchulart: parsed.klasseSchulart ?? '',
        lehrperson: parsed.lehrperson ?? '',
        klasseAktivStatus: isValidEnumValue(EntityAktivStatus, parsed.klasseAktivStatus)
          ? parsed.klasseAktivStatus
          : undefined,
      });
    } catch (error) {
      console.warn('Failed to parse session storage filter:', error);
      return undefined;
    }
  }
}
