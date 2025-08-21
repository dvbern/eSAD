import {DatePipe} from '@angular/common';
import {ChangeDetectionStrategy, Component, computed, DestroyRef, inject, input, OnInit, output} from '@angular/core';
import {NonNullableFormBuilder, ReactiveFormsModule} from '@angular/forms';
import {LabelJson, NotizStatus} from '@dvb/data-access-backend';
import {BackendEntityId, isISODateString} from '@dvb/data-access-types';
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
  TextInputDirective,
} from '@dvb/ui-components';
import {FilterSessionStorage, TableModel} from '@dvb/util-angular';
import {compareFn, ensurePropsFlat, hasNotizEditBerechtigung, isNotNullish, toValidEnumList} from '@dvb/util-shared';
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
import {NotizenListFilterForm, NotizFilterForm} from './notizen-list-filter.form';

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
  ],
  templateUrl: './notiz-list.component.html',
  styleUrl: './notiz-list.component.scss',
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class NotizListComponent extends FilterSessionStorage<NotizFilterForm> implements OnInit {
  readonly labelStore = inject(LabelsStore);
  readonly benutzerStore = inject(BenutzerStore);
  readonly benutzerOptionsStore = inject(BenutzerOptionsStore);
  readonly #transloco = inject(TranslocoService);
  readonly confirmService = inject(ConfirmService);

  loading = input.required<boolean>();
  notizen = input.required<NotizJsonEnhanced[]>();
  deleteLoading = input.required<boolean>();
  readonly removeNotiz = output<BackendEntityId>();
  readonly goToNotiz = output<BackendEntityId>();

  readonly notizStatus = Object.values(NotizStatus);
  protected readonly Path = Path;
  protected readonly compareFnLabel = compareFn<LabelJson>;

  // we need to create a new computed property to avoid errors that occur because the input is not set during
  // construction
  notizenList = computed(() => {
    if (isNotNullish(this.notizen) && isNotNullish(this.notizen())) {
      return this.notizen();
    }
    return [];
  });
  canEditNotiz = this.benutzerStore.createHasRequiredRoleSig(hasNotizEditBerechtigung());

  readonly SESSIONSTORAGE_KEY = 'notizenFilter';

  notizenTable = new TableModel<NotizJsonEnhanced>(
    {
      rawListSignal: this.notizenList,
      lazyLoadOnSortOrFilter: () => this.setSessionStorageFilter(this.tableFilterForm.valueOnValid()),
    },
    {frontendOrBackend: 'frontend', pageOrScrollOrNothing: 'scroll'},
  );
  readonly tableFilterForm = new NotizenListFilterForm({
    fb: inject(NonNullableFormBuilder),
    destroyRef: inject(DestroyRef),
    paginatedTable: this.notizenTable,
  });

  checkRemoveItem(notizId: BackendEntityId): void {
    this.confirmService.confirm({
      question: this.#transloco.translate('notizen.list.delete.question'),
      consequence: this.#transloco.translate('notizen.list.delete.consequence'),
      decision: this.#transloco.translate('notizen.list.delete.decision'),
    }).subscribe(() => {
      this.removeNotiz.emit(notizId);
    })
  }

  ngOnInit() {
    const filter = this.getSessionStorageFilter();
    if (filter) {
      this.tableFilterForm.reset(filter);
    }
  }

  mapSessionStorageStringToFilter(sessionStorageFilter: string): NotizFilterForm | undefined {
    try {
      const parsed = JSON.parse(sessionStorageFilter) as NotizFilterForm;

      // Reconstruct the filter with proper data types and validation
      return ensurePropsFlat<NotizFilterForm>({
        erstelltAm: isISODateString(parsed.erstelltAm) ? parsed.erstelltAm : null,
        titel: parsed.titel ?? '',
        label: Array.isArray(parsed.label) ? parsed.label : [],
        status: toValidEnumList(NotizStatus, parsed.status),
        verantwortlichePerson: Array.isArray(parsed.verantwortlichePerson) ? parsed.verantwortlichePerson : [],
      });
    } catch (error) {
      console.warn('Failed to parse session storage filter:', error);
      return undefined;
    }
  }
}
