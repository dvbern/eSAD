import {DatePipe} from '@angular/common';
import {ChangeDetectionStrategy, Component, computed, inject, input, output} from '@angular/core';
import {ReactiveFormsModule} from '@angular/forms';
import {BenutzerOptionJson, LabelJson, NotizStatus} from '@dvb/data-access-backend';
import {BackendEntityId} from '@dvb/data-access-types';
import {
  BadgeComponent,
  CardComponent,
  CollapsibleMenuComponent,
  ColWidthComponent,
  EmptyTableMessageComponent,
  IconComponent,
  NgSelectDirective,
  SortableComponent,
  TableComponent,
  TableFilterComponent,
  TextInputDirective,
} from '@dvb/ui-components';
import {TableModel} from '@dvb/util-angular';
import {compareFn, hasNotizEditBerechtigung, isNotNullish} from '@dvb/util-shared';
import {TranslocoPipe, TranslocoService} from '@jsverse/transloco';
import {NgbDropdownItem} from '@ng-bootstrap/ng-bootstrap';
import {NgOptionComponent, NgSelectComponent} from '@ng-select/ng-select';
import {BenutzerOptionsStore} from '../../admin-shared/benutzer-options.store';
import {LabelsStore} from '../../admin-shared/labels.store';
import {BenutzerStore} from '../../benutzer/benutzer/benutzer.store';
import {Path} from '../../core/path';
import {ConfirmService} from '../../dialogs/confirm/confirm.service';
import {TranslatedStringPipe} from '../../translated-strings/view/translated-string.pipe';
import {NotizJsonEnhanced} from '../notiz-json-enhanced';
import {NotizenListFilterForm} from './notizen-list-filter.form';

@Component({
  selector: 'feat-notiz-list',
  imports: [
    TranslocoPipe,
    BadgeComponent,
    CardComponent,
    CollapsibleMenuComponent,
    DatePipe,
    EmptyTableMessageComponent,
    NgOptionComponent,
    NgSelectComponent,
    NgSelectDirective,
    NgbDropdownItem,
    ReactiveFormsModule,
    SortableComponent,
    TableComponent,
    TableFilterComponent,
    TextInputDirective,
    TranslatedStringPipe,
    ColWidthComponent,
    IconComponent,
  ],
  templateUrl: './notiz-list.component.html',
  styleUrl: './notiz-list.component.scss',
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class NotizListComponent {
  readonly labelStore = inject(LabelsStore);
  readonly benutzerStore = inject(BenutzerStore);
  readonly benutzerOptionsStore = inject(BenutzerOptionsStore);
  readonly #transloco = inject(TranslocoService);
  readonly confirmService = inject(ConfirmService);

  itemTable = input.required<TableModel<NotizJsonEnhanced>>();
  tableFilterForm = input.required<NotizenListFilterForm>();
  loading = input.required<boolean>();
  notizen = input.required<NotizJsonEnhanced[]>();
  deleteLoading = input.required<boolean>();
  showKind = input.required<boolean>();

  readonly removeNotiz = output<BackendEntityId>();
  readonly goToNotiz = output<BackendEntityId>();

  readonly notizStatus = Object.values(NotizStatus);
  protected readonly Path = Path;
  protected readonly compareFnLabel = compareFn<LabelJson>;
  protected readonly compareBenutzerFn = compareFn<BenutzerOptionJson>;

  // we need to create a new computed property to avoid errors that occur because the input is not set during
  // construction
  notizenList = computed(() => {
    if (isNotNullish(this.notizen) && isNotNullish(this.notizen())) {
      return this.notizen();
    }
    return [];
  });
  canEditNotiz = this.benutzerStore.createHasRequiredRoleSig(hasNotizEditBerechtigung());

  checkRemoveItem(notizId: BackendEntityId): void {
    this.confirmService.confirm({
        question: this.#transloco.translate('notizen.list.delete.question'),
        consequence: this.#transloco.translate('notizen.list.delete.consequence'),
        decision: this.#transloco.translate('notizen.list.delete.decision'),
    }).subscribe(() => {
        this.removeNotiz.emit(notizId);
      });
  }
}
