import {CommonModule} from '@angular/common';
import {ChangeDetectionStrategy, Component, computed, inject} from '@angular/core';
import {toSignal} from '@angular/core/rxjs-interop';
import {ReactiveFormsModule} from '@angular/forms';
import {KontaktPersonKindJson} from '@dvb/data-access-backend';
import {BenutzerStore, BooleanPipe, ConfirmService, getAdresse, Path} from '@dvb/feat-shared';
import {
  ButtonComponent,
  ButtonsRightContainerComponent,
  CardComponent,
  CollapsibleMenuComponent,
  ColWidthComponent,
  EmptyTableMessageComponent,
  HeadingDirective,
  PageHeaderAddButtonComponent,
  PageHeaderListSummaryComponent,
  SortableComponent,
  SummaryListItem,
  TableComponent,
  TableLinkComponent,
} from '@dvb/ui-components';
import {TableModel} from '@dvb/util-angular';
import {hasListEditBerechtigung, requireValue} from '@dvb/util-shared';
import {TranslocoPipe, TranslocoService} from '@jsverse/transloco';
import {NgbDropdownItem, NgbModal} from '@ng-bootstrap/ng-bootstrap';
import {firstValueFrom} from 'rxjs';
import {DossierStore} from '../../../dossier.store';
import {KontaktPersonKindOptionsStore} from '../kontakt-person-kind-options.store';
import {KontaktPersonKindStore} from '../kontakt-person-kind.store';
import {KontaktPersonKindAddComponent} from '../kontakt-personen-kind-add/kontakt-person-kind-add.component';

@Component({
  selector: 'feat-kontakt-personen-kind-list',
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
    TableLinkComponent,
    ColWidthComponent,
    ButtonComponent,
    BooleanPipe
  ],
  templateUrl: './kontakt-personen-kind-list.component.html',
  styleUrl: './kontakt-personen-kind-list.component.scss',
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class KontaktPersonenKindListComponent {
  readonly dossierStore = inject(DossierStore);
  readonly store = inject(KontaktPersonKindStore);
  readonly benutzerStore = inject(BenutzerStore);
  readonly confirmService = inject(ConfirmService);
  readonly #transloco = inject(TranslocoService);
  readonly #kontaktPersonKindOptionsStore = inject(KontaktPersonKindOptionsStore);

  readonly #ngModal = inject(NgbModal);

  protected readonly Path = Path;
  protected readonly getAdresse = getAdresse;

  canEditList = this.benutzerStore.createHasRequiredRoleSig(hasListEditBerechtigung());

  itemTable = new TableModel<KontaktPersonKindJson>(
    {rawListSignal: this.store.entities},
    {frontendOrBackend: 'frontend', pageOrScrollOrNothing: 'scroll'});
  kontaktpersonenTranslated = toSignal(this.#transloco.selectTranslate('kontaktperson.list.summary.kontaktpersonen'));
  summaryItems = computed(() => {
    const items: SummaryListItem[] = [
      {
        name: this.kontaktpersonenTranslated(),
        totalCount: this.itemTable.rawListSize(),
        filteredCount: this.itemTable.filteredListSize(),
      },
    ];
    return items;
  });

  isEscadaItem(kontaktPerson: KontaktPersonKindJson): boolean {
    return !!kontaktPerson.externalImportInfoJson?.aktivInExternalSystem;
  }

  deleteKontaktPersonKind(kontaktPerson: KontaktPersonKindJson) {
    this.store.setSelectedEntityId(kontaktPerson.id);
    this.confirmService.confirm({
      question: this.#transloco.translate('kind.kontaktperson.list.delete.question',
        {name: kontaktPerson.data.name + ' ' + kontaktPerson.data.vorname}),
      consequence: this.#transloco.translate('kind.kontaktperson.list.delete.consequence'),
      decision: this.#transloco.translate('kind.kontaktperson.list.delete.decision'),
    }).subscribe(() => {
      this.store.deleteKontaktPerson({
        kindID: requireValue(this.dossierStore.selectedKind()?.id),
        kontaktPersonId: kontaktPerson.id,
      })
    });
  }

  removeKontaktPersonKindFromKind(kontaktPerson: KontaktPersonKindJson) {
    this.store.setSelectedEntityId(kontaktPerson.id);
    this.confirmService.confirm({
      question: this.#transloco.translate('kind.kontaktperson.list.remove-connection.question',
        {name: kontaktPerson.data.name + ' ' + kontaktPerson.data.vorname}),
      consequence: this.#transloco.translate('kind.kontaktperson.list.remove-connection.consequence'),
      decision: this.#transloco.translate('kind.kontaktperson.list.remove-connection.decision'),
    }).subscribe(() => {
      this.store.removeKontaktPersonFromKind({
        kindID: requireValue(this.dossierStore.kindId()),
        kontaktPersonId: kontaktPerson.id,
        // Die Kontaktpersonen neu einlesen, welche zum Hinzufuegen verfuegbar sind
        onSuccess: () => this.#kontaktPersonKindOptionsStore.loadKontaktPersonenByKindOptions({
          kindID: requireValue(this.dossierStore.kindId()),
        }),
      });
    });
  }

  async addKontaktPersonKindToKind(): Promise<void> {
    const ngbModalRef = this.#ngModal.open(KontaktPersonKindAddComponent, {size: 'lg'});
    ngbModalRef.componentInstance.kontaktPersonen.set(this.#kontaktPersonKindOptionsStore.entities());
    const result = await firstValueFrom(ngbModalRef.closed, {defaultValue: undefined});
    if (result) {
      this.store.addKontaktPersonToKind({
        kindID: requireValue(this.dossierStore.kindId()),
        kontaktPersonId: result.id,
        // Die Kontaktpersonen neu einlesen, welche zum Hinzufuegen verfuegbar sind
        onSuccess: () => this.#kontaktPersonKindOptionsStore.loadKontaktPersonenByKindOptions({
          kindID: requireValue(this.dossierStore.kindId()),
        }),
      });
    }
  }
}
