
import {ChangeDetectionStrategy, Component, computed, inject} from '@angular/core';
import {ReactiveFormsModule} from '@angular/forms';
import {KontaktPersonSchuleJson} from '@dvb/data-access-backend';
import {KontaktPersonSchuleOptionsStore, KontaktPersonSchuleStore, SchulenStore} from '@dvb/feat-shared';
import {BenutzerStore} from '@dvb/feat-shared';
import {Path} from '@dvb/feat-shared';
import {ConfirmService} from '@dvb/feat-shared';
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
import {hasSchuleEditBerechtigung, joinNotBlank, requireValue} from '@dvb/util-shared';
import {TranslocoPipe, TranslocoService} from '@jsverse/transloco';
import {NgbDropdownItem, NgbModal} from '@ng-bootstrap/ng-bootstrap';
import {firstValueFrom} from 'rxjs';
import {
  SchulenDetailKontaktpersonenAddComponent,
} from '../schulen-detail-kontaktpersonen-add/schulen-detail-kontaktpersonen-add.component';

@Component({
  selector: 'feat-schulen-detail-kontaktpersonen-list',
  imports: [
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
    ButtonComponent
],
  templateUrl: './schulen-detail-kontaktpersonen-list.component.html',
  styles: ':host { display:block;}',
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class SchulenDetailKontaktpersonenListComponent {
  readonly #schulenStore = inject(SchulenStore);
  readonly store = inject(KontaktPersonSchuleStore);
  readonly benutzerStore = inject(BenutzerStore);
  readonly confirmService = inject(ConfirmService);
  readonly #transloco = inject(TranslocoService);
  readonly kontaktPersonSchuleStore = inject(KontaktPersonSchuleStore);
  readonly kontaktPersonSchuleOptionsStore = inject(KontaktPersonSchuleOptionsStore);
  readonly #ngModal = inject(NgbModal);
  protected readonly Path = Path;

  canEditList = this.benutzerStore.createHasRequiredRoleSig(hasSchuleEditBerechtigung());

  itemTable = new TableModel<KontaktPersonSchuleJson>(
    { rawListSignal: this.store.entities },
    { frontendOrBackend: 'frontend', pageOrScrollOrNothing: 'scroll' });
  summaryItems = computed(() => {
    const items: SummaryListItem[] = [
      {
        name: this.#transloco.translate('kontaktperson.list.summary.kontaktpersonen'),
        totalCount: this.itemTable.rawListSize(),
        filteredCount: this.itemTable.filteredListSize(),
      },
    ];
    return items;
  });

  deleteKontaktpersonSchule(kontaktPerson: KontaktPersonSchuleJson): void {
    this.store.setSelectedEntityId(kontaktPerson.id);
    this.confirmService.confirm({
      question: this.#transloco.translate('schule.kontaktperson.list.delete.question',
        { name: joinNotBlank([kontaktPerson.data.name, kontaktPerson.data.vorname]) }),
      consequence: this.#transloco.translate('schule.kontaktperson.list.delete.consequence'),
      decision: this.#transloco.translate('schule.kontaktperson.list.delete.decision'),
    }).subscribe(() => {
      this.store.deleteKontaktPerson({
        schuleId: requireValue(this.#schulenStore.selectedEntity()?.id),
        kontaktPersonId: kontaktPerson.id,
      })
    });
  }

  removeKontaktPersonSchuleFromSchule(kontaktPerson: KontaktPersonSchuleJson): void {
    this.store.setSelectedEntityId(kontaktPerson.id);
    this.confirmService.confirm({
      question: this.#transloco.translate('schule.kontaktperson.list.remove-connection.question',
        { name: joinNotBlank([kontaktPerson.data.name, kontaktPerson.data.vorname]) }),
      consequence: this.#transloco.translate('schule.kontaktperson.list.remove-connection.consequence'),
      decision: this.#transloco.translate('schule.kontaktperson.list.remove-connection.decision'),
    }).subscribe(() => {
      this.store.removeKontaktPersonFromSchule({
        schuleId: requireValue(this.#schulenStore.selectedEntity()?.id),
        kontaktPersonId: kontaktPerson.id,
        // Die Kontaktpersonen neu einlesen, welche zum Hinzufuegen verfuegbar sind
        onSuccess: () => this.kontaktPersonSchuleOptionsStore.loadKontaktPersonenExcludeSchule({
          schuleID: requireValue(this.#schulenStore.selectedEntity()?.id),
        }),
      });
    });
  }

  async addKontaktPersonSchuleToSchule(): Promise<void> {
    const ngbModalRef = this.#ngModal.open(SchulenDetailKontaktpersonenAddComponent, { size: 'lg' });
    ngbModalRef.componentInstance.kontaktpersonen.set(this.kontaktPersonSchuleOptionsStore.entities());
    const result = await firstValueFrom(ngbModalRef.closed, { defaultValue: undefined });
    if (result) {
      this.kontaktPersonSchuleStore.addKontaktPersonToSchule({
        schuleId: requireValue(this.#schulenStore.selectedEntity()?.id),
        kontaktPersonId: result.id,
        // Die Kontaktpersonen neu einlesen, welche zum Hinzufuegen verfuegbar sind
        onSuccess: () => this.kontaktPersonSchuleOptionsStore.loadKontaktPersonenExcludeSchule({
          schuleID: requireValue(this.#schulenStore.selectedEntity()?.id),
        }),
      });
    }
  }
}
