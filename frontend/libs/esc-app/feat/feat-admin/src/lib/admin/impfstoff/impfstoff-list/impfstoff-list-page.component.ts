import {CommonModule} from '@angular/common';
import {ChangeDetectionStrategy, Component, computed, DestroyRef, inject, OnInit} from '@angular/core';
import {FormsModule, NonNullableFormBuilder, ReactiveFormsModule} from '@angular/forms';
import {EntityAktivStatus, ImpfstoffJson, Krankheit} from '@dvb/data-access-backend';
import {
  BenutzerStore,
  ConfirmService,
  ImpfstoffeChargenStore,
  PageComponent,
  Path,
  TenantSettingsStore,
  TranslatedStringPipe,
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
  SummaryListItem,
  TableComponent,
  TableFilterComponent,
  TableLinkComponent,
  TextInputDirective,
} from '@dvb/ui-components';
import {FilterSessionStorage, TableModel} from '@dvb/util-angular';
import {ensureProps, hasImpfstoffEditBerechtigung, isValidEnumValue} from '@dvb/util-shared';
import {TranslocoPipe, TranslocoService} from '@jsverse/transloco';
import {NgbDropdownItem} from '@ng-bootstrap/ng-bootstrap';
import {NgOptionComponent, NgSelectComponent} from '@ng-select/ng-select';
import {ImpfstoffFilterForm, ImpfstoffListFilterForm} from './impfstoff-list-filter.form';

@Component({
  selector: 'feat-impfstoff-list.page',
  providers: [TranslatedStringPipe],
  imports: [
    CommonModule,
    PageComponent,
    FormsModule,
    SortableComponent,
    TableComponent,
    TranslocoPipe,
    TableFilterComponent,
    ReactiveFormsModule,
    TextInputDirective,
    PageHeaderButtonsComponent,
    PageHeaderListSummaryComponent,
    PageHeaderAddButtonComponent,
    CardComponent,
    EmptyTableMessageComponent,
    CollapsibleMenuComponent,
    NgbDropdownItem,
    TableLinkComponent,
    ColWidthComponent,
    NgOptionComponent,
    NgSelectComponent,
    NgSelectDirective,
  ],
  templateUrl: './impfstoff-list-page.component.html',
  styles: ':host { display:block;}',
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class ImpfstoffListPageComponent extends FilterSessionStorage<ImpfstoffFilterForm> implements OnInit {

  readonly Path = Path;
  readonly impfstoffeChargenStore = inject(ImpfstoffeChargenStore);
  readonly tenantSettingsStore = inject(TenantSettingsStore);
  readonly confirmService = inject(ConfirmService);
  readonly #transloco = inject(TranslocoService);
  readonly benutzerStore = inject(BenutzerStore);
  readonly impfstoffAktivStati = Object.values(EntityAktivStatus);

  readonly SESSIONSTORAGE_KEY = 'impfstoffFilter';

  itemTable = new TableModel<ImpfstoffJson>(
    {
      rawListSignal: this.impfstoffeChargenStore.entities,
      lazyLoadOnSortOrFilter: () => this.setSessionStorageFilter(this.tableFilterForm.valueOnValid()),
    },
    {frontendOrBackend: 'frontend', pageOrScrollOrNothing: 'scroll'});

  readonly tableFilterForm = new ImpfstoffListFilterForm({
    fb: inject(NonNullableFormBuilder),
    destroyRef: inject(DestroyRef),
    paginatedTable: this.itemTable,
  });

  canEditImpfstoff = this.benutzerStore.createHasRequiredRoleSig(hasImpfstoffEditBerechtigung());

  summaryItems = computed(() => {
    const items: SummaryListItem[] = [
      {
        name: this.#transloco.translate('impfstoff.list.summary.label'),
        totalCount: this.itemTable.rawListSize(),
        filteredCount: this.itemTable.filteredListSize(),
      },
    ];
    return items;
  });

  ngOnInit() {
    const filter = this.getSessionStorageFilter();
    if (filter) {
      this.tableFilterForm.reset(filter);
    }
  }

  mapSessionStorageStringToFilter(sessionStorageFilter: string): ImpfstoffFilterForm | undefined {
    try {
      const parsed = JSON.parse(sessionStorageFilter) as ImpfstoffFilterForm;

      // Reconstruct the filter with proper data types and validation
      return ensureProps<ImpfstoffFilterForm>({
        name: parsed.name ?? '',
        impfstoffAktivStatus: isValidEnumValue(EntityAktivStatus, parsed.impfstoffAktivStatus) ?
          parsed.impfstoffAktivStatus :
          undefined,
      })
    } catch (error) {
      console.warn('Failed to parse session storage filter:', error);
      return undefined;
    }
  }

  translateKrankheiten = (data: Krankheit[]) => {
    const items = data.map((item) => this.#transloco.translate(`Krankheit.${item}`));
    return items.join(', ');
  };

  public changeImpfstoffAktivStatus(impfstoff: ImpfstoffJson): void {
    const decideText = this.determineTextBasedOnStatus(impfstoff.data.impfstoffAktivStatus);

    this.confirmService.confirm({
      question: this.#transloco.translate('impfstoff.list.' + decideText + '.question', {name: impfstoff.data.name}),
      consequence: this.#transloco.translate('impfstoff.list.' + decideText + '.consequence'),
      decision: this.#transloco.translate('global.' + decideText),
    }).subscribe(() => {
        const desiredStatus = impfstoff.data.impfstoffAktivStatus == EntityAktivStatus.AKTIV ?
          EntityAktivStatus.INAKTIV :
          EntityAktivStatus.AKTIV;

        this.impfstoffeChargenStore.changeImpfstoffAktivStatus({
          impfstoffId: impfstoff.id,
          saveJson: {
            entityAktivStatus: desiredStatus,
          },
        });
      },
    );
  }

  public determineTextBasedOnStatus(status: EntityAktivStatus): string {
    return status === EntityAktivStatus.AKTIV ? 'deactivate' : 'activate';
  }

  public removeItem(item: ImpfstoffJson): void {
    this.confirmService.confirm({
      question: this.#transloco.translate('impfstoff.list.delete.question', {name: item.data.name}),
      consequence: this.#transloco.translate('impfstoff.list.delete.consequence'),
      decision: this.#transloco.translate('impfstoff.list.delete.decision'),
    }).subscribe(() => {
      this.impfstoffeChargenStore.removeImpfstoff(item.id);
    });
  }
}
