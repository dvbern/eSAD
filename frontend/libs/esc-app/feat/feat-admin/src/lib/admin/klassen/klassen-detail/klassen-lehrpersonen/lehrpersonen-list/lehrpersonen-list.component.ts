import {CommonModule} from '@angular/common';
import {ChangeDetectionStrategy, Component, computed, inject} from '@angular/core';
import {ReactiveFormsModule} from '@angular/forms';
import {LehrpersonJson} from '@dvb/data-access-backend';
import {KlassenStore} from '@dvb/feat-shared';
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
import {hasListEditBerechtigung, requireValue} from '@dvb/util-shared';
import {TranslocoPipe, TranslocoService} from '@jsverse/transloco';
import {NgbDropdownItem, NgbModal} from '@ng-bootstrap/ng-bootstrap';
import {firstValueFrom} from 'rxjs';
import {LehrpersonenAddComponent} from '../lehrpersonen-add/lehrpersonen-add.component';
import {LehrpersonenOptionStore} from '../lehrpersonen-option.store';
import {LehrpersonenStore} from '../lehrpersonen.store';

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
    TableLinkComponent,
    ColWidthComponent,
    ButtonComponent,
  ],
  templateUrl: './lehrpersonen-list.component.html',
  styleUrl: './lehrpersonen-list.component.scss',
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class LehrpersonenListComponent {
  readonly #klassenStore = inject(KlassenStore);
  readonly store = inject(LehrpersonenStore);
  readonly benutzerStore = inject(BenutzerStore);
  readonly confirmService = inject(ConfirmService);
  readonly #transloco = inject(TranslocoService);
  readonly #lehrpersonenOptionStore = inject(LehrpersonenOptionStore);
  protected readonly Path = Path;

  readonly #ngModal = inject(NgbModal);

  canEditList = this.benutzerStore.createHasRequiredRoleSig(hasListEditBerechtigung());

  itemTable = new TableModel<LehrpersonJson>(
    {rawListSignal: this.store.entities},
    {frontendOrBackend: 'frontend', pageOrScrollOrNothing: 'scroll'});
  summaryItems = computed(() => {
    const items: SummaryListItem[] = [
      {
        name: this.#transloco.translate('lehrpersonen.list.summary.lehrpersonen'),
        totalCount: this.itemTable.rawListSize(),
        filteredCount: this.itemTable.filteredListSize(),
      },
    ];
    return items;
  });

  deleteLehrperson(lehrperson: LehrpersonJson) {
    this.confirmService.confirm({
      question: this.#transloco.translate('lehrpersonen.list.delete.question',
        {name: lehrperson.data.name + ' ' + lehrperson.data.vorname}),
      consequence: this.#transloco.translate('lehrpersonen.list.delete.consequence'),
      decision: this.#transloco.translate('lehrpersonen.list.delete.decision'),
    }).subscribe(() => {
      this.store.deleteLehrperson({
        klasseID: requireValue(this.#klassenStore.selectedEntity()?.id),
        lehrpersonID: lehrperson.id,
      })
    });
  }

  removeLehrpersonFromKlasse(lehrperson: LehrpersonJson) {
    this.store.setSelectedEntityId(lehrperson.id);
    this.confirmService.confirm({
      question: this.#transloco.translate('lehrpersonen.list.remove-connection.question',
        { name: lehrperson.data.name + ' ' + lehrperson.data.vorname }),
      consequence: this.#transloco.translate('lehrpersonen.list.remove-connection.consequence'),
      decision: this.#transloco.translate('lehrpersonen.list.remove-connection.decision'),
    }).subscribe(() => {
      this.store.removeLehrpersonFromKlasse({
        klasseID: requireValue(this.#klassenStore.selectedEntity()?.id),
        lehrpersonID: lehrperson.id,
        // Die Lehrperson neu einlesen, welche zum Hinzufuegen verfuegbar sind
        onSuccess: () => this.#lehrpersonenOptionStore.loadLehrpersonenOptions({
          klasseID: requireValue(this.#klassenStore.selectedEntity()?.id),
        }),
      });
    });
  }

  async addLehrpersonToKlasse(): Promise<void> {
    const ngbModalRef = this.#ngModal.open(LehrpersonenAddComponent, { size: 'lg' });
    ngbModalRef.componentInstance.lehrpersonen.set(this.#lehrpersonenOptionStore.entities());
    const result = await firstValueFrom(ngbModalRef.closed, { defaultValue: undefined });
    if (result) {
      this.store.addLehrpersonToKlasse({
        klasseID: requireValue(this.#klassenStore.selectedEntity()?.id),
        lehrpersonID: result.id,
        // Die Lehrperson neu einlesen, welche zum Hinzufuegen verfuegbar sind
        onSuccess: () => this.#lehrpersonenOptionStore.loadLehrpersonenOptions({
          klasseID: requireValue(this.#klassenStore.selectedEntity()?.id),
        }),
      });
    }
  }
}
