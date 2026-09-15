import {ChangeDetectionStrategy, Component, computed, DestroyRef, inject, OnInit} from "@angular/core";
import {AppNotification, AppNotificationStore, FilterSessionStorage, TableModel} from "@dvb/util-angular";
import {ArchivdokumentJson, ArchivierungStatus, ArchivierungTyp,} from "@dvb/data-access-backend";
import {Observable, of} from 'rxjs';
import {ArchivierungStore} from "../archivierung.store";
import {
  ButtonComponent,
  CollapsibleMenuComponent,
  EmptyTableMessageComponent,
  IconWithTooltipComponent,
  NgSelectDirective,
  SortableComponent,
  TableComponent,
  TableFilterComponent,
  TextInputDirective,
} from '@dvb/ui-components';
import {TranslocoPipe, TranslocoService} from "@jsverse/transloco";
import {DatePipe} from "@angular/common";
import {ArchivFilterForm, ArchivierungListFilterForm} from "./archivierung-list.filter.form";
import {NonNullableFormBuilder, ReactiveFormsModule} from "@angular/forms";
import {NgOptionComponent, NgSelectComponent} from "@ng-select/ng-select";
import {ensureProps, requireValue, toValidEnumList} from "@dvb/util-shared";
import {NgbDropdownItem} from '@ng-bootstrap/ng-bootstrap';
import {BenutzerStore, ConfirmConfig, ConfirmOutcomeEnum, ConfirmService, DeploymentStore} from '@dvb/feat-shared';

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
    ButtonComponent,
    IconWithTooltipComponent,
  ],
  templateUrl: './archivierung-list.component.html',
  styleUrl: './archivierung-list.component.scss',
  changeDetection: ChangeDetectionStrategy.OnPush
})
export class ArchivierungListComponent extends FilterSessionStorage<ArchivFilterForm> implements OnInit {
  readonly store = inject(ArchivierungStore);
  readonly benutzerStore = inject(BenutzerStore);

  readonly #confirmService = inject(ConfirmService);
  readonly #transloco = inject(TranslocoService);
  readonly #deploymentStore = inject(DeploymentStore);
  readonly #appNotificationStore = inject(AppNotificationStore);

  readonly archivTypen = Object.values(ArchivierungTyp);
  readonly archivStatus = Object.values(ArchivierungStatus);

  readonly SESSIONSTORAGE_KEY = this.store.isArchivModusOffen() ? 'archivOffenFilter' : 'archivGeschlossenFilter';

  displayArchivDokumente = computed(() => this.store.isArchivModusOffen() ?
    this.store.offeneArchivDokumente() :
    this.store.abgeschlosseneArchivDokumente());

  bulkDownloadDisabled = computed(() => {
    return this.displayArchivDokumente().length === 0 || this.store.saveLoader.isLoading() || this.benutzerStore.hasFachrolleSupport()
  });

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
    let confirmation$ : Observable<ConfirmOutcomeEnum | undefined> = of(undefined);
    if (item.archivierungTyp === ArchivierungTyp.KIND_DEAKTIVIERT && item.archivierungStatus === ArchivierungStatus.OFFEN) {
      confirmation$ = this.#confirmService.confirm(this.#getSingleArchivDownloadDialogConfig())
    }

    confirmation$.subscribe(() => {
      this.store.downloadArchivdokument({
        archivdokumentId: requireValue(item.id),
        reloadOffeneArchivDokumente: this.store.isArchivModusOffen()
      })
    })
  }

  bulkDownloadArchivdokument() {
    if(!this.store.isArchivModusOffen()){
      return;
    }

    const alleOffenen = this.displayArchivDokumente();
    const total = alleOffenen.length;
    const max = this.#deploymentStore.archiveBulkDownloadMaxFiles();
    const isTruncated = total > max;

    // The backend takes the first `max` documents ordered by timestampArchiviert, which is the order the store
    // already holds them in - so slicing here gives exactly the batch that will be downloaded.
    const batch = isTruncated ? alleOffenen.slice(0, max) : alleOffenen;
    const deactivatedKinderCount = batch.filter(d => d.archivierungTyp === ArchivierungTyp.KIND_DEAKTIVIERT).length;

    const confirmation$ = this.#confirmService.confirm(
      this.#getBulkArchivDownloadDialogConfig(batch.length, deactivatedKinderCount, isTruncated ? {total, max} : undefined),
    );

    confirmation$.subscribe(() => {
      const generating = this.#notifyGenerating();
      this.store.bulkDownloadArchivdokument({
        onSettled: () => this.#appNotificationStore.removeNotification(generating),
      })
    })
  }

  /**
   * The zip is built synchronously and can take a while for a large batch, so tell the user something is happening.
   * Not autohiding: the notification is removed once the download has actually settled, however long that takes.
   */
  #notifyGenerating(): AppNotification {
    const notification: AppNotification = {
      id: crypto.randomUUID(),
      alertType: 'info',
      summary: this.#transloco.translate('archiv.list.bulkdownload.generating'),
      autohide: false,
    };
    this.#appNotificationStore.addNotification(notification);

    return notification;
  }

  /**
   * Only KIND_DEAKTIVIERT documents lead to a Kind being deleted; ZUSTAENDIGER_ARZT_CHANGED and DUPLIKAT_MERGE are
   * plain downloads. The batch can hold any mix of the three, so the dialog has to say which of the three cases the
   * user is actually confirming - otherwise the checkbox asks them to attest to a deletion that will not happen.
   */
  #getBulkArchivDownloadDialogConfig(
    batchCount: number,
    deactivatedKinderCount: number,
    truncation?: {total: number, max: number},
  ): ConfirmConfig {
    const variant = this.getMessageVariant(deactivatedKinderCount, batchCount);
    const params = {batchCount, kindCount: deactivatedKinderCount};
    const translationKey = (part: string) => `archiv.list.bulkdownload.${variant}.${part}`;

    const consequence = this.#transloco.translate(translationKey('consequence'), params);

    return {
      question: this.#transloco.translate(translationKey('question'), params),
      consequence: truncation
        ? `${consequence} ${this.#transloco.translate('archiv.list.bulkdownload.truncated', truncation)}`
        : consequence,
      decision: this.#transloco.translate(translationKey('decision'), params),
      confirmCheckbox: this.#transloco.translate(translationKey('confirmCheckbox'), params),
    }
  }

  /**
   * returns part of a message key based on how many kinder are in the state KIND_DEAKTIVIERT
   * @param deactivatedKinderCount
   * @param batchCount
   * @private
   */
  private getMessageVariant(deactivatedKinderCount: number, batchCount: number): 'nodeactivated' | 'alldeactivated' | 'mixeddeactivated' {
    if (deactivatedKinderCount === 0) {
      return 'nodeactivated';
    }
    if (deactivatedKinderCount === batchCount) {
      return 'alldeactivated';
    }
    return 'mixeddeactivated';
  }

  #getSingleArchivDownloadDialogConfig() : ConfirmConfig {
    return {
      question: this.#transloco.translate('archiv.list.download.question'),
      consequence: this.#transloco.translate('archiv.list.download.consequence'),
      decision: this.#transloco.translate('archiv.list.download.decision'),
      confirmCheckbox: this.#transloco.translate('archiv.list.download.confirmCheckbox'),
    }
  }
}
