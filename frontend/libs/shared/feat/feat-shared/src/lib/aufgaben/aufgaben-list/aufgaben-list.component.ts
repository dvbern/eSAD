import {CommonModule} from '@angular/common';
import {ChangeDetectionStrategy, Component, DestroyRef, inject, input, OnInit} from '@angular/core';
import {NonNullableFormBuilder, ReactiveFormsModule} from '@angular/forms';
import {AufgabeStatus, BenutzerOptionJson, LabelJson} from '@dvb/data-access-backend';
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
  TableLinkComponent,
  TextInputDirective,
} from '@dvb/ui-components';
import {FilterSessionStorage, TableModel} from '@dvb/util-angular';
import {compareFn, ensurePropsFlat, hasAufgabenEditBerechtigungen, toValidEnumList} from '@dvb/util-shared';
import {TranslocoPipe, TranslocoService} from '@jsverse/transloco';
import {NgbDropdownItem} from '@ng-bootstrap/ng-bootstrap';
import {NgOptionComponent, NgSelectComponent} from '@ng-select/ng-select';
import {AufgabeJsonEnhanced, AufgabenStore} from '../../admin-shared/aufgaben.store';
import {BenutzerOptionsStore} from '../../admin-shared/benutzer-options.store';
import {LabelsStore} from '../../admin-shared/labels.store';
import {BenutzerStore} from '../../benutzer/benutzer/benutzer.store';
import {ConfirmService} from '../../dialogs/confirm/confirm.service';
import {TranslatedStringPipe} from '../../translated-strings/view/translated-string.pipe';
import {AufgabeListFilterForm, AufgabenListFilter} from './aufgaben-list-filter.form';

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
export class AufgabenListComponent extends FilterSessionStorage<AufgabenListFilter> implements OnInit {
  readonly defaultEmpfaenger = input<BenutzerOptionJson>();

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

  readonly SESSIONSTORAGE_KEY = 'aufgabenFilter';

  aufgabenTable = new TableModel<AufgabeJsonEnhanced>(
    {
      rawListSignal: this.store.entities,
      lazyLoadOnSortOrFilter: () => this.setSessionStorageFilter(this.tableFilterForm.valueOnValid()),
    },
    {frontendOrBackend: 'frontend', pageOrScrollOrNothing: 'scroll'},
  );
  readonly tableFilterForm = new AufgabeListFilterForm({
    fb: inject(NonNullableFormBuilder),
    destroyRef: inject(DestroyRef),
    paginatedTable: this.aufgabenTable,
  });

  ngOnInit() {
    const filter = this.getSessionStorageFilter();
    if (filter) {
      this.tableFilterForm.reset(filter);
    }

    const defaultEmpfaenger = this.defaultEmpfaenger();
    if (defaultEmpfaenger && !filter?.empfaenger.length ) {
      this.tableFilterForm.setEmpfaenger(defaultEmpfaenger);
    }
  }

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

  mapSessionStorageStringToFilter(sessionStorageFilter: string): AufgabenListFilter | undefined {
    try {
      const parsed = JSON.parse(sessionStorageFilter) as AufgabenListFilter;

      // Reconstruct the filter with proper data types and validation
      return ensurePropsFlat<AufgabenListFilter>({
        faelligBis: isISODateString(parsed.faelligBis) ? parsed.faelligBis : null,
        aufgabeStatus: toValidEnumList(AufgabeStatus, parsed.aufgabeStatus),
        label: Array.isArray(parsed.label) ? parsed.label : [],
        titel: parsed.titel ?? '',
        erstelltDurch: Array.isArray(parsed.erstelltDurch) ? parsed.erstelltDurch : [],
        empfaenger: Array.isArray(parsed.empfaenger) ? parsed.empfaenger : [],
        kind: parsed.kind ?? '',
      });
    } catch (error) {
      console.warn('Failed to parse session storage filter:', error);
      return undefined;
    }
  }

}
