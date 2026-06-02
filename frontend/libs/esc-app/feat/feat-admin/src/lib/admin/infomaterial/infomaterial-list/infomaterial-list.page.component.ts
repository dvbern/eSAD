
import {ChangeDetectionStrategy, Component, computed, DestroyRef, inject} from '@angular/core';
import {NonNullableFormBuilder, ReactiveFormsModule} from '@angular/forms';
import {InfomaterialJson} from '@dvb/data-access-backend';
import {
  BenutzerStore,
  ConfirmService,
  InfomaterialStore,
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
import {TableModel} from '@dvb/util-angular';
import {hasInfomaterialEditBerechtigung} from '@dvb/util-shared';
import {TranslocoPipe, TranslocoService} from '@jsverse/transloco';
import {NgbDropdownItem} from '@ng-bootstrap/ng-bootstrap';
import {NgOptionComponent, NgSelectComponent} from '@ng-select/ng-select';
import {InfomaterialListFilterForm} from './infomaterial-list-filter.form';

@Component({
  selector: 'feat-infomaterial-list.page',
  providers: [TranslatedStringPipe],
  imports: [
    PageComponent,
    TableComponent,
    SortableComponent,
    TableFilterComponent,
    ReactiveFormsModule,
    TranslocoPipe,
    TextInputDirective,
    PageHeaderAddButtonComponent,
    PageHeaderButtonsComponent,
    NgSelectComponent,
    NgSelectDirective,
    NgOptionComponent,
    CollapsibleMenuComponent,
    NgbDropdownItem,
    PageHeaderListSummaryComponent,
    CardComponent,
    EmptyTableMessageComponent,
    TableLinkComponent,
    ColWidthComponent
],
  templateUrl: './infomaterial-list.page.component.html',
  styleUrl: './infomaterial-list.page.component.scss',
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class InfomaterialListPageComponent {
  readonly infomaterialStore = inject(InfomaterialStore);
  readonly benutzerStore = inject(BenutzerStore);
  readonly tenantSettingsStore = inject(TenantSettingsStore);
  readonly confirmService = inject(ConfirmService);
  readonly #transloco = inject(TranslocoService);
  readonly #translatedStringPipe = inject(TranslatedStringPipe);

  readonly Path = Path;

  itemTable = new TableModel<InfomaterialJson>(
    {rawListSignal: this.infomaterialStore.entities},
    {frontendOrBackend: 'frontend', pageOrScrollOrNothing: 'scroll'},
  );
  readonly tableFilterForm = new InfomaterialListFilterForm({
    fb: inject(NonNullableFormBuilder),
    destroyRef: inject(DestroyRef),
    paginatedTable: this.itemTable,
  });

  canEditList = this.benutzerStore.createHasRequiredRoleSig(hasInfomaterialEditBerechtigung());
  summaryItems = computed(() => {
    const items: SummaryListItem[] = [
      {
        name: this.#transloco.translate('infomaterial.list.summary.infomaterial'),
        totalCount: this.itemTable.rawListSize(),
        filteredCount: this.itemTable.filteredListSize(),
      },
    ];
    return items;
  });

  public removeItem(item: InfomaterialJson): void {
    const infomaterialName = this.#translatedStringPipe.transform(item.data.name);
    this.confirmService
      .confirm({
        question: this.#transloco.translate('infomaterial.list.delete.question', {name: infomaterialName}),
        consequence: this.#transloco.translate('infomaterial.list.delete.consequence'),
        decision: this.#transloco.translate('infomaterial.list.delete.decision'),
      })
      .subscribe(() => {
        this.infomaterialStore.removeInfomaterial(item.id);
      });
  }

  public toggleStatus(item: InfomaterialJson): void {
    this.infomaterialStore.toggleActiveOnInfomaterial(item);
  }
}
