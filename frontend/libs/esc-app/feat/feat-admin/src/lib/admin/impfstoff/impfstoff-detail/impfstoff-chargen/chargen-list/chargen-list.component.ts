import {CommonModule} from '@angular/common';
import {ChangeDetectionStrategy, Component, computed, DestroyRef, inject} from '@angular/core';
import {NonNullableFormBuilder, ReactiveFormsModule} from '@angular/forms';
import {ImpfstoffChargeJson} from '@dvb/data-access-backend';
import {ImpfstoffeChargenStore} from '@dvb/feat-shared';
import {BenutzerStore} from '@dvb/feat-shared';
import {Path} from '@dvb/feat-shared';
import {ConfirmService} from '@dvb/feat-shared';
import {
  ButtonsRightContainerComponent,
  CardComponent,
  CollapsibleMenuComponent,
  ColWidthComponent,
  EmptyTableMessageComponent,
  HeadingDirective,
  NgSelectDirective,
  PageHeaderAddButtonComponent,
  PageHeaderListSummaryComponent,
  SortableComponent,
  SummaryListItem,
  TableComponent,
  TableFilterComponent,
  TextInputDirective,
} from '@dvb/ui-components';
import {TableModel} from '@dvb/util-angular';
import {hasImpfstoffEditBerechtigung, requireValue} from '@dvb/util-shared';
import {TranslocoPipe, TranslocoService} from '@jsverse/transloco';
import {NgbDropdownItem} from '@ng-bootstrap/ng-bootstrap';
import {NgOptionComponent, NgSelectComponent} from '@ng-select/ng-select';
import {ChargenStore} from '../chargen.store';
import {ChargeListFilterForm} from './chargen-list-filter.form';

@Component({
  selector: 'feat-lehrpersonen-list',
  standalone: true,
  imports: [
    CommonModule,
    TranslocoPipe,
    HeadingDirective,
    ReactiveFormsModule,
    SortableComponent,
    TableComponent,
    PageHeaderAddButtonComponent,
    CollapsibleMenuComponent,
    NgbDropdownItem,
    PageHeaderListSummaryComponent,
    ButtonsRightContainerComponent,
    CardComponent,
    EmptyTableMessageComponent,
    ColWidthComponent,
    NgOptionComponent,
    NgSelectComponent,
    NgSelectDirective,
    TableFilterComponent,
    TextInputDirective,
  ],
  templateUrl: './chargen-list.component.html',
  styles: ':host { display:block;}',
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class ChargenListComponent {
  readonly store = inject(ChargenStore);
  readonly #impfstoffStore = inject(ImpfstoffeChargenStore);
  readonly benutzerStore = inject(BenutzerStore);
  readonly confirmService = inject(ConfirmService);
  readonly #transloco = inject(TranslocoService);
  protected readonly Path = Path;

  canEditList = this.benutzerStore.createHasRequiredRoleSig(hasImpfstoffEditBerechtigung());

  itemTable = new TableModel<ImpfstoffChargeJson>({rawListSignal: this.store.entities},
    {frontendOrBackend: 'frontend', pageOrScrollOrNothing: 'scroll'});
  summaryItems = computed(() => {
    const items: SummaryListItem[] = [
      {
        name: this.#transloco.translate('charge.list.summary.chargen'),
        totalCount: this.itemTable.rawListSize(),
        filteredCount: this.itemTable.filteredListSize(),
      },
    ];
    return items;
  });

  readonly tableFilterForm = new ChargeListFilterForm({
    fb: inject(NonNullableFormBuilder),
    destroyRef: inject(DestroyRef),
    paginatedTable: this.itemTable,
  });

  removeCharge(charge: ImpfstoffChargeJson) {
    this.confirmService.confirm({
      question: this.#transloco.translate('charge.list.delete.question', {name: charge.data.name}),
      consequence: this.#transloco.translate('charge.list.delete.consequence'),
      decision: this.#transloco.translate('charge.list.delete.decision'),
    }).subscribe(() => {
      this.store.removeImpfstoffCharge({
        impfstoffID: requireValue(this.#impfstoffStore.selectedEntity()?.id),
        chargeID: charge.id,
      });
    });
  }

  public toggleStatus(item: ImpfstoffChargeJson): void {
    this.store.toggleActiveOnCharge(item);
  }
}
