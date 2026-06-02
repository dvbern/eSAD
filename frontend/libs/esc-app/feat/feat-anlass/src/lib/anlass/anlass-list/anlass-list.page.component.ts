import {CommonModule} from '@angular/common';
import {ChangeDetectionStrategy, Component, computed, DestroyRef, inject, OnInit} from '@angular/core';
import {NonNullableFormBuilder, ReactiveFormsModule} from '@angular/forms';
import {RouterLink} from '@angular/router';
import {AnlassJson, AnlassStatus} from '@dvb/data-access-backend';
import {BackendEntityId, isValidBackendEntityId} from '@dvb/data-access-types';
import {
  AnlassJsonEnhanced,
  BenutzerOptionsStore,
  BenutzerStore,
  ConfirmService,
  PageComponent,
  Path
} from '@dvb/feat-shared';
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
  TableComponent,
  TableFilterComponent,
  TableLinkComponent,
  TextInputDirective,
} from '@dvb/ui-components';
import {FilterSessionStorage, TableModel} from '@dvb/util-angular';
import {
  hasAnlassCreateOrEditForNotAbgeschlossenBerechtigung,
  hasAnlassViewBerechtigung,
  toISODateTimeString,
  toValidEnumList,
  truncateToDate,
} from '@dvb/util-shared';
import {TranslocoPipe, TranslocoService} from '@jsverse/transloco';
import {NgbDropdownItem} from '@ng-bootstrap/ng-bootstrap';
import {NgOptionComponent, NgSelectComponent} from '@ng-select/ng-select';
import {AnlassStore} from '../anlass.store';
import {anlassTitle} from '../anlass.util';
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
    {frontendOrBackend: 'frontend', pageOrScrollOrNothing: 'scroll'},
  );
  readonly tableFilterForm = new AnlassListFilterForm({
    fb: inject(NonNullableFormBuilder),
    destroyRef: inject(DestroyRef),
    paginatedTable: this.itemTable,
  });

  summaryItems = computed(() => {
    const allItems = this.anlassStore.entities();
    const filteredItems = this.itemTable.filteredSortedList();

    const countByStatus = (list: any[], status: AnlassStatus) =>
      list.filter(item => item.data.anlassStatus === status).length;

    return [
      {
        name: this.#transloco.translate('anlass.list.summary.anzahl.total'),
        totalCount: allItems.length,
        filteredCount: filteredItems.length,
      },
      {
        name: this.#transloco.translate('anlass.list.summary.anzahl.planung'),
        totalCount: countByStatus(allItems, AnlassStatus.IN_PLANUNG),
        filteredCount: countByStatus(filteredItems, AnlassStatus.IN_PLANUNG),
      },
      {
        name: this.#transloco.translate('anlass.list.summary.anzahl.bereit'),
        totalCount: countByStatus(allItems, AnlassStatus.BEREIT),
        filteredCount: countByStatus(filteredItems, AnlassStatus.BEREIT),
      },
    ];
  });

  canCreateAnlass = this.benutzerStore.createHasRequiredRoleSig(hasAnlassCreateOrEditForNotAbgeschlossenBerechtigung());
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

      if (parsed.anlassStatus.length == 0) {
        parsed.anlassStatus = Object.values(AnlassStatus).filter(
          status => ![AnlassStatus.ABGESCHLOSSEN, AnlassStatus.ABGESAGT].includes(status)
        );
      }
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
