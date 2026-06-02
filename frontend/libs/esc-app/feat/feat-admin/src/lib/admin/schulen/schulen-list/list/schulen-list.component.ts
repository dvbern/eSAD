
import {ChangeDetectionStrategy, Component, inject, input} from '@angular/core';
import {ReactiveFormsModule} from '@angular/forms';
import {EntityAktivStatus, EscadaKlasseStufe, SchuleJson} from '@dvb/data-access-backend';
import {KlassenStore, SchulenStore} from '@dvb/feat-shared';
import {BenutzerStore} from '@dvb/feat-shared';
import {ConfirmService} from '@dvb/feat-shared';
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
import {TableModel} from '@dvb/util-angular';
import {hasSchuleEditBerechtigung, isActiveInExternalSystem} from '@dvb/util-shared';
import {TranslocoPipe, TranslocoService} from '@jsverse/transloco';
import {NgbDropdownItem} from '@ng-bootstrap/ng-bootstrap';
import {NgOptionComponent, NgSelectComponent} from '@ng-select/ng-select';
import {SchulenListFilterForm} from '../schulen-list-filter.form';

@Component({
  selector: 'feat-schulen-list',
  imports: [
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
    NgOptionComponent,
    NgSelectComponent,
    NgSelectDirective,
    TableLinkComponent,
    ColWidthComponent
],
  templateUrl: './schulen-list.component.html',
  styleUrl: './schulen-list.component.scss',
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class SchulenListComponent {

  readonly store = inject(SchulenStore);
  readonly klassenStore = inject(KlassenStore);
  readonly benutzerStore = inject(BenutzerStore);
  readonly confirmService = inject(ConfirmService);
  readonly #transloco = inject(TranslocoService);

  canEditList = this.benutzerStore.createHasRequiredRoleSig(hasSchuleEditBerechtigung());

  itemTable = input.required<TableModel<SchuleJson>>();
  tableFilterForm = input.required<SchulenListFilterForm>();
  loading = input.required<boolean>();

  readonly klassenStufenOptions = Object.values(EscadaKlasseStufe);
  readonly schuleAktivStati = Object.values(EntityAktivStatus);

  isExternalSchuleAndActive(schule: SchuleJson): boolean {
    return isActiveInExternalSystem(schule);
  }

  public getTranslatedKlassenstufen(schule: SchuleJson): string {
    return this.klassenStore.getKlassenstufeTranslatedList(schule.klassenstufen);
  }

  public deleteTitleTextForSchule(schule: SchuleJson): string {
    if (this.isExternalSchuleAndActive(schule)) {
      return 'schule.list.delete.impossible.tooltip';
    }
    return '';
  }

  public changeSchuleAktivStatus(schule: SchuleJson): void {
    const decideText = this.determineTextBasedOnStatus(schule.data.schuleAktivStatus);

    this.confirmService.confirm({
      question: this.#transloco.translate('schule.list.' + decideText + '.question', {name: schule.data.schulhaus}),
      consequence: this.#transloco.translate('schule.list.' + decideText + '.consequence'),
      decision: this.#transloco.translate('global.' + decideText),
    }).subscribe(() => {
      const desiredStatus = schule.data.schuleAktivStatus == EntityAktivStatus.AKTIV ?
          EntityAktivStatus.INAKTIV :
          EntityAktivStatus.AKTIV;

        this.store.changeSchuleAktivStatus({
          schuleId: schule.id,
          saveJson: {
            entityAktivStatus: desiredStatus,
          },
        });
      },
    );
  }

  public determineTextBasedOnStatus(status: EntityAktivStatus): string {
    return status === EntityAktivStatus.AKTIV ? 'deactivate' : 'activate';
  }

  removeSchule(schule: SchuleJson): void {
    this.confirmService.confirm({
      question: this.#transloco.translate('schule.list.delete.question', {name: schule.data.schulhaus}),
      consequence: this.#transloco.translate('schule.list.delete.consequence'),
      decision: this.#transloco.translate('schule.list.delete.decision'),
    }).subscribe(() => {
      this.store.removeSchule(schule.id);
    });
  }
}
