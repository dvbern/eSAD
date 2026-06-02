
import { ChangeDetectionStrategy, Component, inject, input } from '@angular/core';
import { ReactiveFormsModule } from '@angular/forms';
import { EntityAktivStatus, KlasseJson } from '@dvb/data-access-backend';
import {BenutzerStore, ConfirmService, KlasseJsonEnhanced, KlassenStore} from '@dvb/feat-shared';
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
import {hasKlassenEditBerechtigung, isActiveInExternalSystem} from '@dvb/util-shared';
import { TranslocoPipe, TranslocoService } from '@jsverse/transloco';
import { NgbDropdownItem } from '@ng-bootstrap/ng-bootstrap';
import { NgOptionComponent, NgSelectComponent } from '@ng-select/ng-select';
import { TableModel } from '@dvb/util-angular';
import { KlassenListFilterForm } from './klassen-list-filter.form';

@Component({
  selector: 'feat-klassen-list',
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
    TableLinkComponent,
    ColWidthComponent,
    NgOptionComponent,
    NgSelectComponent,
    NgSelectDirective
],
  templateUrl: './klassen-list.component.html',
  styleUrl: './klassen-list.component.scss',
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class KlassenListComponent {
  readonly klassenStore = inject(KlassenStore);
  readonly benutzerStore = inject(BenutzerStore);
  readonly confirmService = inject(ConfirmService);
  readonly #transloco = inject(TranslocoService);

  canEditList = this.benutzerStore.createHasRequiredRoleSig(hasKlassenEditBerechtigung());

  itemTable = input.required<TableModel<KlasseJsonEnhanced>>();
  tableFilterForm = input.required<KlassenListFilterForm>();
  loading = input.required<boolean>();

  readonly klasseAktivStati = Object.values(EntityAktivStatus);

  public deleteTitleTextForKlasse(klasse: KlasseJsonEnhanced): string {
    if (this.isExternalKlasseAndActive(klasse)) {
      return 'klasse.list.delete.impossible.tooltip';
    }
    return '';
  }

  public isExternalKlasseAndActive(klasse: KlasseJsonEnhanced): boolean {
    return isActiveInExternalSystem(klasse);
  }

  public changeKlasseAktivStatus(klasse: KlasseJsonEnhanced): void {
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
}
