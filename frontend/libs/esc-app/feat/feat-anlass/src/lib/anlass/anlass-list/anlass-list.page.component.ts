import {CommonModule} from '@angular/common';
import {ChangeDetectionStrategy, Component, computed, DestroyRef, inject, OnInit} from '@angular/core';
import {NonNullableFormBuilder, ReactiveFormsModule} from '@angular/forms';
import {RouterLink} from '@angular/router';
import {AnlassJson, AnlassStatus} from '@dvb/data-access-backend';
import {BackendEntityId, isValidBackendEntityId} from '@dvb/data-access-types';
import {BenutzerOptionsStore, BenutzerStore, ConfirmService, PageComponent, Path} from '@dvb/feat-shared';
import {
  CardComponent,
  CollapsibleMenuComponent,
  ColWidthComponent,
  EmptyTableMessageComponent,
  NgSelectDirective,
  PageHeaderAddButtonComponent,
  PageHeaderButtonsComponent,
  PageHeaderListSummaryComponent,
  SortableComponent,
  SummaryListItem,
  TableComponent,
  TableFilterComponent,
  TableLinkComponent,
  TextInputDirective,
} from '@dvb/ui-components';
import {FilterSessionStorage, TableModel} from '@dvb/util-angular';
import {
  hasAnlassCreateBerechtigung,
  hasAnlassViewBerechtigung,
  toISODateTimeString,
  toValidEnumList,
  truncateToDate,
} from '@dvb/util-shared';
import {TranslocoPipe, TranslocoService} from '@jsverse/transloco';
import {NgbDropdownItem} from '@ng-bootstrap/ng-bootstrap';
import {NgOptionComponent, NgSelectComponent} from '@ng-select/ng-select';
import {AnlassStore} from '../anlass.store';
import {AnlassJsonEnhanced, anlassTitle} from '../anlass.util';
import {AnlassFilterForm, AnlassListFilterForm} from './anlass-list-filter.form';

@Component({
  selector: 'feat-anlass-list.page',
  imports: [
    CommonModule,
    PageComponent,
    TranslocoPipe,
    TableComponent,
    ReactiveFormsModule,
    CollapsibleMenuComponent,
    PageHeaderAddButtonComponent,
    NgbDropdownItem,
    SortableComponent,
    TableFilterComponent,
    NgSelectComponent,
    NgSelectDirective,
    NgOptionComponent,
    TextInputDirective,
    RouterLink,
    CardComponent,
    EmptyTableMessageComponent,
    TableLinkComponent,
    ColWidthComponent,
    PageHeaderListSummaryComponent,
    PageHeaderButtonsComponent,
  ],
  templateUrl: './anlass-list.page.component.html',
  styleUrl: './anlass-list.page.component.scss',
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class AnlassListPageComponent extends FilterSessionStorage<AnlassFilterForm> implements OnInit {
  readonly anlassStore = inject(AnlassStore);
  readonly transloco = inject(TranslocoService);
  readonly confirmService = inject(ConfirmService);
  readonly benutzerStore = inject(BenutzerStore);
  readonly benutzerOptionsStore = inject(BenutzerOptionsStore);
  readonly #transloco = inject(TranslocoService);

  readonly Path = Path;
  readonly anlassStatusList = Object.values(AnlassStatus);

  readonly SESSIONSTORAGE_KEY = 'anlassFilter';
  itemTable = new TableModel<AnlassJsonEnhanced>(
    {
      rawListSignal: this.anlassStore.entities,
      lazyLoadOnSortOrFilter: () => this.setSessionStorageFilter(this.tableFilterForm.valueOnValid()),
    },
    { frontendOrBackend: 'frontend', pageOrScrollOrNothing: 'scroll' },
  );
  readonly tableFilterForm = new AnlassListFilterForm({
    fb: inject(NonNullableFormBuilder),
    destroyRef: inject(DestroyRef),
    paginatedTable: this.itemTable,
  });

  anlassInPlanung = computed(() =>
    this.itemTable.filteredSortedList().filter(item => item.data.anlassStatus == AnlassStatus.IN_PLANUNG),
  );

  anlassInBereit = computed(() =>
    this.itemTable.filteredSortedList().filter(item => item.data.anlassStatus == AnlassStatus.BEREIT),
  );

  summaryItems = computed(() => {
    const items: SummaryListItem[] = [
      {
        name: this.#transloco.translate('anlass.list.summary.anzahl.total'),
        totalCount: this.itemTable.filteredSortedList().length,
        filteredCount: this.itemTable.filteredSortedList().length,
      },
      {
        name: this.#transloco.translate('anlass.list.summary.anzahl.planung'),
        totalCount: this.anlassInPlanung().length,
        filteredCount: this.anlassInPlanung().length,
      },
      {
        name: this.#transloco.translate('anlass.list.summary.anzahl.bereit'),
        totalCount: this.anlassInBereit().length,
        filteredCount: this.anlassInBereit().length,
      },
    ];
    return items;
  });

  canCreateAnlass = this.benutzerStore.createHasRequiredRoleSig(hasAnlassCreateBerechtigung());
  canViewAnlass = this.benutzerStore.createHasRequiredRoleSig(hasAnlassViewBerechtigung());

  ngOnInit() {
    const filter = this.getSessionStorageFilter();
    if (filter) {
      this.tableFilterForm.reset(filter);
    }
  }

  public removeItem(item: AnlassJson): void {
    this.confirmService
      .confirm({
        question: this.transloco.translate('anlass.list.delete.question', {
          name: anlassTitle(item.data, this.transloco),
        }),
        consequence: this.transloco.translate('anlass.list.delete.consequence'),
        decision: this.transloco.translate('anlass.list.delete.decision'),
      })
      .subscribe(() => {
        this.anlassStore.removeAnlass(item.id);
      });
  }

  compareBenutzerFn(item: BackendEntityId, selected: BackendEntityId): boolean {
    return item === selected;
  }

  protected readonly anlassTitle = anlassTitle;

  mapSessionStorageStringToFilter(sessionStorageFilter: string): AnlassFilterForm | undefined {
    try {
      const parsed = JSON.parse(sessionStorageFilter) as AnlassFilterForm;

      // Reconstruct the filter with proper data types and validation
      return {
        fullTitle: parsed.fullTitle ?? '',
        anlassStatus: toValidEnumList(AnlassStatus, parsed.anlassStatus),
        datum: parsed.datum ? truncateToDate(toISODateTimeString(new Date(parsed.datum))) : null,
        arzt: Array.isArray(parsed.arzt) ? parsed.arzt.filter(arztId => isValidBackendEntityId(arztId)) : [],
        mpa: Array.isArray(parsed.mpa) ? parsed.mpa.filter(mpaId => isValidBackendEntityId(mpaId)) : [],
        weiterePerson: Array.isArray(parsed.weiterePerson)
          ? parsed.weiterePerson.filter(weiterePersonId => isValidBackendEntityId(weiterePersonId))
          : [],
      };
    } catch (error) {
      console.warn('Failed to parse session storage filter:', error);
      return undefined;
    }
  }
}
