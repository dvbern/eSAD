
import { ChangeDetectionStrategy, Component, computed, DestroyRef, inject, OnInit } from '@angular/core';
import { FormsModule, NonNullableFormBuilder, ReactiveFormsModule } from '@angular/forms';
import { LabelJson, LabelTyp } from '@dvb/data-access-backend';
import {
  BenutzerStore,
  BooleanPipe,
  ConfirmService,
  LabelsStore,
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
import { FilterSessionStorage, TableModel } from '@dvb/util-angular';
import { ensureProps, hasListEditBerechtigung, isValidEnumValue } from '@dvb/util-shared';
import { TranslocoPipe, TranslocoService } from '@jsverse/transloco';
import { NgbDropdownItem } from '@ng-bootstrap/ng-bootstrap';
import { NgOptionComponent, NgSelectComponent } from '@ng-select/ng-select';

import { LabelFilterForm, LabelListFilterForm } from './label-list-filter.form';

@Component({
  selector: 'feat-label-list.page',
  providers: [TranslatedStringPipe],
  imports: [
    PageComponent,
    FormsModule,
    SortableComponent,
    TableComponent,
    TranslocoPipe,
    TableFilterComponent,
    ReactiveFormsModule,
    TextInputDirective,
    CollapsibleMenuComponent,
    PageHeaderButtonsComponent,
    PageHeaderAddButtonComponent,
    NgSelectDirective,
    NgOptionComponent,
    NgSelectComponent,
    NgbDropdownItem,
    PageHeaderListSummaryComponent,
    CardComponent,
    EmptyTableMessageComponent,
    TableLinkComponent,
    ColWidthComponent,
    BooleanPipe
],
  templateUrl: './label-list.page.component.html',
  styleUrl: './label-list.page.component.scss',
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class LabelListPageComponent extends FilterSessionStorage<LabelFilterForm> implements OnInit {
  readonly labelsStore = inject(LabelsStore);
  readonly benutzerStore = inject(BenutzerStore);
  readonly tenantSettingsStore = inject(TenantSettingsStore);
  readonly confirmService = inject(ConfirmService);
  readonly #transloco = inject(TranslocoService);
  readonly #translatedStringPipe = inject(TranslatedStringPipe);

  readonly Path = Path;
  readonly labelTypen = Object.values(LabelTyp);

  canEditLabel = this.benutzerStore.createHasRequiredRoleSig(hasListEditBerechtigung());

  readonly SESSIONSTORAGE_KEY = 'labelFilter';

  itemTable = new TableModel<LabelJson>(
    {
      rawListSignal: this.labelsStore.entities,
      lazyLoadOnSortOrFilter: () => this.setSessionStorageFilter(this.tableFilterForm.valueOnValid()),
    },
    { frontendOrBackend: 'frontend', pageOrScrollOrNothing: 'scroll' },
  );
  readonly tableFilterForm = new LabelListFilterForm({
    fb: inject(NonNullableFormBuilder),
    destroyRef: inject(DestroyRef),
    paginatedTable: this.itemTable,
  });

  summaryItems = computed(() => {
    const items: SummaryListItem[] = [
      {
        name: this.#transloco.translate('label.list.summary.label'),
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

  mapSessionStorageStringToFilter(sessionStorageFilter: string): LabelFilterForm | undefined {
    try {
      const parsed = JSON.parse(sessionStorageFilter) as LabelFilterForm;

      // Reconstruct the filter with proper data types and validation
      return ensureProps<LabelFilterForm>({
        nameDe: parsed.nameDe ?? '',
        nameFr: parsed.nameFr ?? '',
        typ: isValidEnumValue(LabelTyp, parsed.typ) ? parsed.typ : null,
        autoLabel: parsed.autoLabel,
        aktiv: parsed.aktiv,
      });
    } catch (error) {
      console.warn('Failed to parse session storage filter:', error);
      return undefined;
    }
  }

  public removeItem(item: LabelJson): void {
    const labelName = this.#translatedStringPipe.transform(item.data.name);
    this.confirmService
      .confirm({
        question: this.#transloco.translate('label.list.delete.question', { name: labelName }),
        consequence: this.#transloco.translate('label.list.delete.consequence'),
        decision: this.#transloco.translate('label.list.delete.decision'),
      })
      .subscribe(() => {
        this.labelsStore.removeLabel(item.id);
      });
  }

  public toggleStatus(item: LabelJson): void {
    this.labelsStore.toggleActiveOnLabel(item);
  }
}
