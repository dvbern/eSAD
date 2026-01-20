import {CommonModule} from '@angular/common';
import {ChangeDetectionStrategy, Component, computed, DestroyRef, inject, OnInit} from '@angular/core';
import {NonNullableFormBuilder, ReactiveFormsModule} from '@angular/forms';
import {EntityAktivStatus, Geschlecht, Klassenstufe, SearchSummaryJson} from '@dvb/data-access-backend';
import {isISODateString} from '@dvb/data-access-types';
import {
  BenutzerStore,
  ConfirmService,
  PageComponent,
  Path,
  SchulenOptionsStore,
  TenantSettingsStore,
} from '@dvb/feat-shared';
import {
  ButtonComponent,
  ButtonsRightContainerComponent,
  CardComponent,
  CollapsibleMenuComponent,
  ColWidthComponent,
  EmptyTableMessageComponent,
  InputWithIconComponent,
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
import {AppNotificationStore, FilterSessionStorage, SortDTO, TableModel} from '@dvb/util-angular';
import {
  ensurePropsFlat,
  hasKindEditBerechtigung,
  isNotNullish,
  isValidEnumValue,
  toValidEnumList,
} from '@dvb/util-shared';
import {TranslocoPipe, TranslocoService} from '@jsverse/transloco';
import {NgbDropdownItem, NgbModal} from '@ng-bootstrap/ng-bootstrap';
import {NgOptionComponent, NgSelectComponent} from '@ng-select/ng-select';
import {firstValueFrom} from 'rxjs';

import {KinderSummaryStore} from '../kinder-summary.store';
import {KinderFilterComponent} from './kinder-filter/kinder-filter.component';
import {ErweiterteFilterJson} from './kinder-filter/kinder-filter.form';
import {KinderImportComponent} from './kinder-import/kinder-import.component';
import {KinderImportJsonEnhanced, KinderImportStore} from './kinder-import/kinder-import.store';
import {KinderFilterForm, KinderListFilterForm} from './kinder-list-filter.form';

@Component({
  selector: 'feat-kinder-list.page',
  imports: [
    CommonModule,
    PageComponent,
    TranslocoPipe,
    TableComponent,
    SortableComponent,
    ReactiveFormsModule,
    TableFilterComponent,
    TextInputDirective,
    CollapsibleMenuComponent,
    PageHeaderAddButtonComponent,
    PageHeaderButtonsComponent,
    PageHeaderListSummaryComponent,
    NgSelectComponent,
    NgSelectDirective,
    NgOptionComponent,
    PageComponent,
    CardComponent,
    EmptyTableMessageComponent,
    TableLinkComponent,
    ColWidthComponent,
    NgbDropdownItem,
    InputWithIconComponent,
    ButtonComponent,
    ButtonsRightContainerComponent,
  ],
  templateUrl: './kinder-list.page.component.html',
  styleUrl: './kinder-list.page.component.scss',
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class KinderListPageComponent extends FilterSessionStorage<KinderFilterForm> implements OnInit {
  readonly kinderSummaryStore = inject(KinderSummaryStore);
  readonly kinderImportStore = inject(KinderImportStore);
  readonly #transloco = inject(TranslocoService);
  readonly confirmService = inject(ConfirmService);
  readonly benutzerStore = inject(BenutzerStore);
  readonly #schulenOptionsStore = inject(SchulenOptionsStore);
  readonly #appNotificationStore = inject(AppNotificationStore);
  readonly tenantSettingsStore = inject(TenantSettingsStore);

  readonly Path = Path;
  readonly klassenstufen = Object.values(Klassenstufe);
  readonly geschlechter = Object.values(Geschlecht);

  readonly #ngModal = inject(NgbModal);

  readonly SESSIONSTORAGE_KEY = 'kinderFilter';

  itemTable = new TableModel<SearchSummaryJson>(
    {
      rawListSignal: this.kinderSummaryStore.kinderListWithSchuleMarkedInactive,
      backendFilteredSize: this.kinderSummaryStore.backendFilteredListSize,
      backendLastLoadedPageSignal: this.kinderSummaryStore.currentLoadedPage,
      lazyLoadOnSortOrFilter: displayConfig => {
        console.debug('changed sort or filter');
        this.kinderSummaryStore.loadPaginatedKinderList({config: displayConfig, replaceList: true});
        this.setSessionStorageFilter(
          this.tableFilterForm.valueOnValid(),
        );
      },
      lazyLoadOnPaginateOrScroll: displayConfig => {
        console.debug('changed page');
        this.kinderSummaryStore.loadPaginatedKinderList({config: displayConfig, replaceList: false});
      },
    },
    {
      frontendOrBackend: 'backend',
      pageOrScrollOrNothing: 'scroll',
    },
  );

  readonly tableFilterForm = new KinderListFilterForm({
    fb: inject(NonNullableFormBuilder),
    destroyRef: inject(DestroyRef),
    paginatedTable: this.itemTable,
  });

  canEditKind = this.benutzerStore.createHasRequiredRoleSig(hasKindEditBerechtigung());
  schuleOptions = computed(() => this.#schulenOptionsStore.entitiesMarkedInactive());

  summaryItems = computed(() => {
    const items: SummaryListItem[] = [
      {
        name: this.#transloco.translate('kinder.list.page.title'),
        totalCount: this.kinderSummaryStore.totalKinderCount() ?? 0,
        filteredCount: this.kinderSummaryStore.backendFilteredListSize(),
      },
    ];
    return items;
  });

  constructor() {
    super();
    // prepare initial filter
    const initialSortConfig: SortDTO<SearchSummaryJson> = {
      colSorts: [
        {colName: 'name', sortDirection: 'ASC'},
        {colName: 'vorname', sortDirection: 'ASC'},
      ],
    };
    this.itemTable.onSort(initialSortConfig);
  }

  ngOnInit(): void {
    const filter = this.getSessionStorageFilter();
    if (filter) {
      this.tableFilterForm.reset(filter);
    }
  }

  public changeKindAktivStatus(item: SearchSummaryJson): void {
    const decideText = this.determineTextBasedOnStatus(item.kindAktivStatus);

    this.confirmService
      .confirm({
        question: this.#transloco.translate('kinder.list.' + decideText + '.question'),
        consequence: this.#transloco.translate('kinder.list.' + decideText + '.consequence'),
        decision: this.#transloco.translate('global.' + decideText),
      })
      .subscribe(() => {
        const desiredStatus =
          item.kindAktivStatus == EntityAktivStatus.AKTIV ? EntityAktivStatus.INAKTIV : EntityAktivStatus.AKTIV;

        this.kinderSummaryStore.changeKindAktivStatus({
          kindId: item.id,
          saveJson: {
            entityAktivStatus: desiredStatus,
          },
        });
      });
  }

  public determineTextBasedOnStatus(status: EntityAktivStatus): string {
    return status === EntityAktivStatus.AKTIV ? 'deactivate' : 'activate';
  }

  async openFilter(): Promise<void> {
    const ngbModalRef = this.#ngModal.open(KinderFilterComponent, {size: 'xl'});
    ngbModalRef.componentInstance.filterData.set(this.kinderSummaryStore.erweiterteFilterValues());
    const result: ErweiterteFilterJson | undefined = await firstValueFrom(ngbModalRef.closed, {
      defaultValue: undefined,
    });
    if (result) {
      this.itemTable.resetPaginationToFirstPage();
      this.kinderSummaryStore.setErweiterteFilter(result);
      this.kinderSummaryStore.loadPaginatedKinderList({config: this.itemTable.displayConfig(), replaceList: true});
    }
  }

  async openImport(): Promise<void> {
    const ngbModalRef = this.#ngModal.open(KinderImportComponent, {size: 'l'});

    const result: KinderImportJsonEnhanced = await firstValueFrom(ngbModalRef.closed, {
      defaultValue: undefined,
    });
    if (isNotNullish(result)) {
      if (result.standardImport) {
        this.kinderImportStore.importKinderStandard({
          kinderImportJson: result,
          onSuccess: () => this.showImportStartedNotification(),
        });
      } else {
        this.kinderImportStore.importKinderKlassen({
          kinderImportJson: result,
          onSuccess: () => this.showImportStartedNotification(),
        });
      }
    }
  }

  private showImportStartedNotification(): void {
    this.#appNotificationStore.addNotification({
      alertType: 'success',
      id: crypto.randomUUID(),
      summary: this.#transloco.translate('kinder.list.import.started'),
      autohide: true,
    });
  }

  mapSessionStorageStringToFilter(sessionStorageFilter: string): KinderFilterForm | undefined {
    try {
      const parsed = JSON.parse(sessionStorageFilter) as KinderFilterForm;

      // Reconstruct the filter with proper data types and validation
      return ensurePropsFlat({
        schule: Array.isArray(parsed.schule) ? parsed.schule : [],
        klasse: parsed.klasse ?? '',
        klassenstufe: toValidEnumList(Klassenstufe, parsed.klassenstufe),
        geschlecht: isValidEnumValue(Geschlecht, parsed.geschlecht) ? parsed.geschlecht : '',
        name: parsed.name ?? '',
        vorname: parsed.vorname ?? '',
        geburtsdatum: isISODateString(parsed.geburtsdatum) ? parsed.geburtsdatum : null,
        esadId: parsed.esadId ?? '',
      })
    } catch (error) {
      console.warn('Failed to parse session storage filter:', error);
      return undefined;
    }
  }
}
