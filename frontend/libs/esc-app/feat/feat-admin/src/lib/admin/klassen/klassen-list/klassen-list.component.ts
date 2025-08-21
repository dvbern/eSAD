import { CommonModule } from '@angular/common';
import { ChangeDetectionStrategy, Component, DestroyRef, inject, OnInit } from '@angular/core';
import { NonNullableFormBuilder, ReactiveFormsModule } from '@angular/forms';
import { EntityAktivStatus, KlasseJson } from '@dvb/data-access-backend';
import { BenutzerStore, ConfirmService, KlasseJsonEnhanced, KlassenStore } from '@dvb/feat-shared';
import {
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
import { FilterSessionStorage, TableModel } from '@dvb/util-angular';
import { ensureProps, hasListEditBerechtigung, isNullish, isValidEnumValue } from '@dvb/util-shared';
import { TranslocoPipe, TranslocoService } from '@jsverse/transloco';
import { NgbDropdownItem } from '@ng-bootstrap/ng-bootstrap';
import { NgOptionComponent, NgSelectComponent } from '@ng-select/ng-select';
import { KlassenListFilter, KlassenListFilterForm } from './klassen-list-filter.form';

@Component({
  selector: 'feat-klassen-list',
  imports: [
    CommonModule,
    TableComponent,
    SortableComponent,
    TableFilterComponent,
    ReactiveFormsModule,
    TranslocoPipe,
    TextInputDirective,
    CollapsibleMenuComponent,
    NgbDropdownItem,
    CardComponent,
    EmptyTableMessageComponent,
    TableLinkComponent,
    ColWidthComponent,
    NgOptionComponent,
    NgSelectComponent,
    NgSelectDirective,
  ],
  templateUrl: './klassen-list.component.html',
  styleUrl: './klassen-list.component.scss',
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class KlassenListComponent extends FilterSessionStorage<KlassenListFilter> implements OnInit {
  readonly klassenStore = inject(KlassenStore);
  readonly benutzerStore = inject(BenutzerStore);
  readonly confirmService = inject(ConfirmService);
  readonly #transloco = inject(TranslocoService);

  canEditList = this.benutzerStore.createHasRequiredRoleSig(hasListEditBerechtigung());

  readonly SESSIONSTORAGE_KEY = 'klassenFilter';

  itemTable = new TableModel<KlasseJsonEnhanced>(
    {
      rawListSignal: this.klassenStore.entities,
      lazyLoadOnSortOrFilter: () => this.setSessionStorageFilter(this.tableFilterForm.valueOnValid()),
    },
    { frontendOrBackend: 'frontend', pageOrScrollOrNothing: 'scroll' },
  );
  readonly tableFilterForm = new KlassenListFilterForm({
    fb: inject(NonNullableFormBuilder),
    destroyRef: inject(DestroyRef),
    paginatedTable: this.itemTable,
  });
  readonly klasseAktivStati = Object.values(EntityAktivStatus);

  ngOnInit() {
    const filter = this.getSessionStorageFilter();
    if (filter) {
      this.tableFilterForm.reset(filter);
    }
  }

  public deleteTitleTextForKlasse(klasse: KlasseJson): string {
    if (this.isExternalKlasseAndActive(klasse)) {
      return 'klasse.list.delete.impossible.tooltip';
    }
    return '';
  }

  public isExternalKlasseAndActive(klasse: KlasseJson): boolean {
    if (isNullish(klasse.externalImportInfoJson)) {
      return false;
    }
    return klasse.externalImportInfoJson.aktivInExternalSystem;
  }

  public changeKlasseAktivStatus(klasse: KlasseJson): void {
    const decideText = this.determineTextBasedOnStatus(klasse.data.klasseAktivStatus);

    this.confirmService
      .confirm({
        question: this.#transloco.translate('klasse.list.' + decideText + '.question', {
          name: klasse.data.bezeichnung,
        }),
        consequence: this.#transloco.translate('klasse.list.' + decideText + '.consequence'),
        decision: this.#transloco.translate('global.' + decideText),
      })
      .subscribe(() => {
        const desiredStatus =
          klasse.data.klasseAktivStatus == EntityAktivStatus.AKTIV
            ? EntityAktivStatus.INAKTIV
            : EntityAktivStatus.AKTIV;

        this.klassenStore.changeKlasseAktivStatus({
          klasseId: klasse.id,
          saveJson: {
            entityAktivStatus: desiredStatus,
          },
        });
      });
  }

  public determineTextBasedOnStatus(status: EntityAktivStatus): string {
    return status === EntityAktivStatus.AKTIV ? 'deactivate' : 'activate';
  }

  public removeKlasse(klasse: KlasseJson): void {
    this.confirmService
      .confirm({
        question: this.#transloco.translate('klasse.list.delete.question', { name: klasse.data.bezeichnung }),
        consequence: this.#transloco.translate('klasse.list.delete.consequence'),
        decision: this.#transloco.translate('klasse.list.delete.decision'),
      })
      .subscribe(() => {
        this.klassenStore.removeKlasse(klasse.id);
      });
  }

  mapSessionStorageStringToFilter(sessionStorageFilter: string): KlassenListFilter | undefined {
    try {
      const parsed = JSON.parse(sessionStorageFilter) as KlassenListFilter;

      // Reconstruct the filter with proper data types and validation
      return ensureProps<KlassenListFilter>({
        schule: parsed.schule ?? '',
        bezeichnung: parsed.bezeichnung ?? '',
        klassenstufe: parsed.klassenstufe ?? '',
        klasseSchulart: parsed.klasseSchulart ?? '',
        lehrperson: parsed.lehrperson ?? '',
        klasseAktivStatus: isValidEnumValue(EntityAktivStatus, parsed.klasseAktivStatus)
          ? parsed.klasseAktivStatus
          : undefined,
      });
    } catch (error) {
      console.warn('Failed to parse session storage filter:', error);
      return undefined;
    }
  }
}
