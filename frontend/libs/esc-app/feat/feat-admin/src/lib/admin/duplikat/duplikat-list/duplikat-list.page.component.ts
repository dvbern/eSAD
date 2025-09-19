import {CommonModule, DatePipe} from '@angular/common';
import {ChangeDetectionStrategy, Component, computed, DestroyRef, inject, OnInit} from '@angular/core';
import {FormsModule, NonNullableFormBuilder, ReactiveFormsModule} from '@angular/forms';
import {DuplikatJson, DuplikatKindJson, DuplikatStatus} from '@dvb/data-access-backend';
import {
  BenutzerStore,
  ConfirmService,
  NavigationService,
  PageComponent,
  Path,
  TenantSettingsStore,
  TranslatedStringPipe,
} from '@dvb/feat-shared';
import {
  ButtonComponent,
  CardComponent,
  CollapsibleMenuComponent,
  ColWidthComponent,
  EmptyTableMessageComponent,
  NgSelectDirective,
  PageHeaderButtonsComponent,
  PageHeaderListSummaryComponent,
  SortableComponent,
  SummaryListItem,
  TableComponent,
  TableFilterComponent,
} from '@dvb/ui-components';
import {TableModel} from '@dvb/util-angular';
import {isNullish} from '@dvb/util-shared';
import {TranslocoPipe, TranslocoService} from '@jsverse/transloco';
import {NgbDropdownItem, NgbModal} from '@ng-bootstrap/ng-bootstrap';
import {NgOptionComponent, NgSelectComponent} from '@ng-select/ng-select';
import {firstValueFrom} from 'rxjs';
import {DuplikatMergeComponent} from '../duplikat-merge/duplikat-merge.component';
import {DuplikatMergeJson} from '../duplikat-merge/duplikat-merge.form';
import {DuplikatStore} from '../duplikat.store';
import {DuplikatListFilterForm} from './duplikat-list-filter.form';

@Component({
  selector: 'feat-duplikat-list.page',
  providers: [TranslatedStringPipe, DatePipe],
  imports: [
    CommonModule,
    PageComponent,
    FormsModule,
    SortableComponent,
    TableComponent,
    TranslocoPipe,
    ReactiveFormsModule,
    CollapsibleMenuComponent,
    PageHeaderButtonsComponent,
    CardComponent,
    EmptyTableMessageComponent,
    ColWidthComponent,
    ButtonComponent,
    TableFilterComponent,
    NgOptionComponent,
    NgSelectComponent,
    NgSelectDirective,
    NgbDropdownItem,
    PageHeaderListSummaryComponent,
  ],
  templateUrl: './duplikat-list.page.component.html',
  styleUrl: './duplikat-list.page.component.scss',
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class DuplikatListPageComponent implements OnInit {
  readonly duplikatStore = inject(DuplikatStore);

  readonly benutzerStore = inject(BenutzerStore);
  readonly tenantSettingsStore = inject(TenantSettingsStore);
  readonly confirmService = inject(ConfirmService);

  readonly navigationService = inject(NavigationService);
  readonly #transloco = inject(TranslocoService);
  readonly #datePipe = inject(DatePipe);
  readonly #ngModal = inject(NgbModal);
  readonly duplikatStatusList = Object.values(DuplikatStatus);

  readonly Path = Path;

  protected readonly DuplikatStatus = DuplikatStatus;

  itemTable = new TableModel<DuplikatJson>(
    { rawListSignal: this.duplikatStore.entities },
    {frontendOrBackend: 'frontend', pageOrScrollOrNothing: 'scroll'});

  readonly tableFilterForm = new DuplikatListFilterForm({
    fb: inject(NonNullableFormBuilder),
    destroyRef: inject(DestroyRef),
    paginatedTable: this.itemTable,
  });

  summaryItems = computed((): SummaryListItem[] => [
    {
      name: this.#transloco.translate('duplikat.list.summary', {}),
      totalCount: this.itemTable.rawListSize(),
      filteredCount: this.itemTable.filteredListSize(),
    },
  ]);

  ngOnInit(): void {
    // filter for Dubletten im Status offen only
    this.tableFilterForm.group.controls.duplikatStatus.patchValue(DuplikatStatus.OFFEN);
  }

  reloadDuplikate(): void {
    this.duplikatStore.reloadDuplikate();
  }

  getKindLabel(kind: DuplikatKindJson): string {
    if (isNullish(kind.name) || isNullish(kind.vorname)) {
      return '';
    }
    const gebDatString = this.#datePipe.transform(kind.geburtsdatum, 'dd.MM.yyyy');
    const geschlechtString = this.#transloco.translate('Geschlecht.' + kind.geschlecht);
    return kind.name + ' ' + kind.vorname + ', ' + gebDatString + ' (' + geschlechtString + ')';
  }

  markDuplikatAsFalsePositive(duplikat: DuplikatJson): void {
    this.confirmService.confirm({
        question: this.#transloco.translate('duplikat.mark.false.positive.question'),
        consequence: this.#transloco.translate('duplikat.mark.false.positive.consequence'),
        decision: this.#transloco.translate('duplikat.mark.false.positive.decision'),
    }).subscribe(() => {
        this.duplikatStore.markDuplikatAsFalsePositive(duplikat);
      },
    );
  }

  async openMergeDialog(einladung: DuplikatJson) {
    const ngbModalRef = this.#ngModal.open(DuplikatMergeComponent, { size: 'lg' });
    ngbModalRef.componentInstance.duplikatData.set(einladung);
    const result: DuplikatMergeJson = await firstValueFrom(ngbModalRef.closed, { defaultValue: undefined });
    if (result) {
      this.duplikatStore.merge(result);
    }
  }
}
