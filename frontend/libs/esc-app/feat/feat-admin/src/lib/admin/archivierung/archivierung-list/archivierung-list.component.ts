import {ChangeDetectionStrategy, Component, computed, DestroyRef, inject, OnInit} from "@angular/core";
import {FilterSessionStorage, TableModel} from "@dvb/util-angular";
import {
  ArchivdokumentJson,
  ArchivierungStatus,
  ArchivierungTyp,
} from "@dvb/data-access-backend";
import {ArchivierungStore} from "../archivierung.store";
import {
  CollapsibleMenuComponent,
  EmptyTableMessageComponent, NgSelectDirective,
  SortableComponent,
  TableComponent, TableFilterComponent, TextInputDirective
} from "@dvb/ui-components";
import {TranslocoPipe, TranslocoService} from "@jsverse/transloco";
import {DatePipe} from "@angular/common";
import {ArchivFilterForm, ArchivierungListFilterForm} from "./archivierung-list.filter.form";
import {NonNullableFormBuilder, ReactiveFormsModule} from "@angular/forms";
import {NgOptionComponent, NgSelectComponent} from "@ng-select/ng-select";
import {ensureProps, requireValue, toValidEnumList} from "@dvb/util-shared";
import {NgbDropdownItem} from "@ng-bootstrap/ng-bootstrap";
import {ConfirmService} from "@dvb/feat-shared";

@Component({
  selector: 'feat-archivierung-list',
  standalone: true,
  imports: [
    TableComponent,
    SortableComponent,
    TranslocoPipe,
    DatePipe,
    CollapsibleMenuComponent,
    EmptyTableMessageComponent,
    TableFilterComponent,
    NgSelectDirective,
    NgSelectComponent,
    ReactiveFormsModule,
    TextInputDirective,
    NgOptionComponent,
    NgbDropdownItem,
  ],
  templateUrl: './archivierung-list.component.html',
  styleUrl: './archivierung-list.component.scss',
  changeDetection: ChangeDetectionStrategy.OnPush
})
export class ArchivierungListComponent extends FilterSessionStorage<ArchivFilterForm> implements OnInit {
  readonly store = inject(ArchivierungStore);

  readonly #confirmService = inject(ConfirmService);
  readonly #transloco = inject(TranslocoService);

  readonly archivTypen = Object.values(ArchivierungTyp);
  readonly archivStatus = Object.values(ArchivierungStatus);

  readonly SESSIONSTORAGE_KEY = this.store.isArchivModusOffen() ? 'archivOffenFilter' : 'archivGeschlossenFilter';

  displayArchivDokumente = computed(() => this.store.isArchivModusOffen() ?
    this.store.offeneArchivDokumente() :
    this.store.abgeschlosseneArchivDokumente());

  itemTable = new TableModel<ArchivdokumentJson>(
    {
      rawListSignal: this.displayArchivDokumente,
      lazyLoadOnSortOrFilter: () => this.setSessionStorageFilter(this.tableFilterForm.valueOnValid()),
    },
    {frontendOrBackend: 'frontend', pageOrScrollOrNothing: 'scroll'},
  );

  readonly tableFilterForm = new ArchivierungListFilterForm({
    fb: inject(NonNullableFormBuilder),
    destroyRef: inject(DestroyRef),
    paginatedTable: this.itemTable,
  });

  downloadText(item: ArchivdokumentJson): string {
    if (this.store.isArchivModusOffen() && item.archivierungTyp === ArchivierungTyp.KIND_DEAKTIVIERT) {
        return 'archiv.list.download.offen.kind.loeschen';
    }
    return 'archiv.list.download';
  }

  ngOnInit() {
    const filter = this.getSessionStorageFilter();
    if (filter) {
      this.tableFilterForm.reset(filter);
    }
  }

  mapSessionStorageStringToFilter(sessionStorageFilter: string): ArchivFilterForm | undefined {
    try {
      const parsed = JSON.parse(sessionStorageFilter) as ArchivFilterForm;

      return ensureProps<ArchivFilterForm>({
        datumArchivierung: parsed.datumArchivierung,
        datumDownload: parsed.datumDownload,
        typ: toValidEnumList(ArchivierungTyp, parsed.typ),
        status: toValidEnumList(ArchivierungStatus, parsed.status),
        betroffenesKind: parsed.betroffenesKind ?? '',
      });
    } catch (error) {
      console.warn('Failed to parse session storage filter:', error);
      return undefined;
    }
  }

  downloadArchivdokument(item: ArchivdokumentJson) {
    if (item.archivierungTyp === ArchivierungTyp.KIND_DEAKTIVIERT && item.archivierungStatus === ArchivierungStatus.OFFEN) {
      this.#confirmService.confirm({
        question: this.#transloco.translate('archiv.list.download.question'),
        consequence: this.#transloco.translate('archiv.list.download.consequence'),
        decision: this.#transloco.translate('archiv.list.download.decision'),
        confirmCheckbox: this.#transloco.translate('archiv.list.download.confirmCheckbox'),
      }).subscribe(() => {
        this.store.downloadArchivdokument({
          archivdokumentId: requireValue(item.id),
          reloadOffeneArchivDokumente: this.store.isArchivModusOffen()
        })
      });
    } else {
      this.store.downloadArchivdokument({
        archivdokumentId: requireValue(item.id),
        reloadOffeneArchivDokumente: this.store.isArchivModusOffen()
      })
    }
  }
}
