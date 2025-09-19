import {CommonModule} from '@angular/common';
import {
  ChangeDetectionStrategy,
  Component,
  inject,
  input,
  OnInit,
} from '@angular/core';
import {ReactiveFormsModule} from '@angular/forms';
import { AufgabeStatus, BenutzerOptionJson, LabelJson } from '@dvb/data-access-backend';
import {BackendEntityId} from '@dvb/data-access-types';
import {
  BadgeComponent,
  CardComponent,
  CollapsibleMenuComponent,
  ColWidthComponent,
  EmptyTableMessageComponent,
  NgSelectDirective,
  SortableComponent,
  TableComponent,
  TableFilterComponent,
  TableLinkComponent,
  TextInputDirective,
} from '@dvb/ui-components';
import {compareFn, hasAufgabenEditBerechtigungen} from '@dvb/util-shared';
import {TranslocoPipe, TranslocoService} from '@jsverse/transloco';
import {NgbDropdownItem} from '@ng-bootstrap/ng-bootstrap';
import {NgOptionComponent, NgSelectComponent} from '@ng-select/ng-select';
import { AufgabeJsonEnhanced, AufgabenStore } from '../../admin-shared/aufgaben.store';
import {BenutzerOptionsStore} from '../../admin-shared/benutzer-options.store';
import {LabelsStore} from '../../admin-shared/labels.store';
import {BenutzerStore} from '../../benutzer/benutzer/benutzer.store';
import {ConfirmService} from '../../dialogs/confirm/confirm.service';
import {TranslatedStringPipe} from '../../translated-strings/view/translated-string.pipe';
import { TableModel } from '@dvb/util-angular';
import { AufgabeListFilterForm } from './aufgaben-list-filter.form';

@Component({
  selector: 'feat-shared-aufgaben-list',
  imports: [
    CommonModule,
    TranslocoPipe,
    SortableComponent,
    TableComponent,
    BadgeComponent,
    TranslatedStringPipe,
    ReactiveFormsModule,
    TableFilterComponent,
    TextInputDirective,
    NgOptionComponent,
    NgSelectComponent,
    NgSelectDirective,
    CollapsibleMenuComponent,
    NgbDropdownItem,
    EmptyTableMessageComponent,
    CardComponent,
    TableLinkComponent,
    ColWidthComponent,
  ],
  templateUrl: './aufgaben-list.component.html',
  styleUrl: './aufgaben-list.component.scss',
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class AufgabenListComponent implements OnInit{
  readonly defaultEmpfaenger = input<BenutzerOptionJson>();
  itemTable = input.required<TableModel<AufgabeJsonEnhanced>>();
  tableFilterForm = input.required<AufgabeListFilterForm>();
  loading = input.required<boolean>();

  readonly store = inject(AufgabenStore);
  readonly labelsStore = inject(LabelsStore);
  readonly benutzerOptionsStore = inject(BenutzerOptionsStore);
  readonly benutzerStore = inject(BenutzerStore);
  readonly confirmService = inject(ConfirmService);
  readonly #transloco = inject(TranslocoService);

  readonly aufgabeStatus = Object.values(AufgabeStatus);

  protected readonly compareFnLabel = compareFn<LabelJson>;
  protected readonly compareBenutzerFn = compareFn<BenutzerOptionJson>;

  canEditAufgabe = this.benutzerStore.createHasRequiredRoleSig(hasAufgabenEditBerechtigungen());

  checkRemoveItem(aufgabeId: BackendEntityId): void {
    this.confirmService.confirm({
      question: this.#transloco.translate('aufgaben.list.delete.question'),
      consequence: this.#transloco.translate('aufgaben.list.delete.consequence'),
      decision: this.#transloco.translate('aufgaben.list.delete.decision'),
    }).subscribe(() => {
        this.store.removeAufgabe(aufgabeId);
      },
    );
  }

  ngOnInit() {
    const defaultEmpfaenger = this.defaultEmpfaenger();
    const form = this.tableFilterForm();

    if (defaultEmpfaenger && !form.valueOnValid().empfaenger?.length) {
      form.setEmpfaenger(defaultEmpfaenger);
    }
  }

}
