import {CommonModule, DatePipe} from '@angular/common';
import {ChangeDetectionStrategy, Component, inject} from '@angular/core';
import {ReactiveFormsModule} from '@angular/forms';
import {ImpfStatus, UntersuchungImpfungInternJson, UntersuchungImpfungInternSaveJson} from '@dvb/data-access-backend';
import {
  BenutzerOptionsStore,
  BenutzerStore,
  ConfirmService,
  EventBusStore,
  ImpfstoffeChargenStore,
  Path
} from '@dvb/feat-shared';
import {
  ButtonsRightContainerComponent,
  CollapsibleMenuComponent,
  ColWidthComponent,
  EmptyTableMessageComponent,
  HeadingDirective,
  IconOnlyButtonComponent,
  PageHeaderAddButtonComponent,
  SortableComponent,
  TableComponent,
} from '@dvb/ui-components';
import {TableModel} from '@dvb/util-angular';
import {hasImpfungenEditBerechtigung, requireValue, toISODateString} from '@dvb/util-shared';
import {TranslocoPipe, TranslocoService} from '@jsverse/transloco';
import {NgbDropdownItem, NgbModal} from '@ng-bootstrap/ng-bootstrap';
import {constructNow} from 'date-fns';
import {firstValueFrom} from 'rxjs';
import {AnlassStore} from '../../../anlass.store';
import {UntersuchungStore} from '../../untersuchung.store';
import {
  ImpfungPopupMode,
  UntersuchungImpfungenInternComponent,
} from '../untersuchung-impfungen-intern/untersuchung-impfungen-intern.component';
import {UntersuchungImpfungenStore} from '../untersuchung-impfungen.store';
import {takeUntilDestroyed} from "@angular/core/rxjs-interop";

@Component({
  selector: 'feat-untersuchung-impfungen-intern-list',
  imports: [
    CommonModule,
    TableComponent,
    SortableComponent,
    IconOnlyButtonComponent,
    ReactiveFormsModule,
    TranslocoPipe,
    CollapsibleMenuComponent,
    PageHeaderAddButtonComponent,
    HeadingDirective,
    ButtonsRightContainerComponent,
    NgbDropdownItem,
    EmptyTableMessageComponent,
    ColWidthComponent,
  ],
  providers: [DatePipe],
  templateUrl: './untersuchung-impfungen-intern-list.component.html',
  styleUrl: './untersuchung-impfungen-intern-list.component.scss',
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class UntersuchungImpfungenInternListComponent {

  readonly untersuchungImpfungenStore = inject(UntersuchungImpfungenStore);
  readonly impfstoffeChargenStore = inject(ImpfstoffeChargenStore);
  readonly anlassStore = inject(AnlassStore);
  readonly untersuchungStore = inject(UntersuchungStore);
  readonly benutzerStore = inject(BenutzerStore);
  readonly #benutzerOptionsStore = inject(BenutzerOptionsStore);
  readonly #ngModal = inject(NgbModal);
  readonly #translationService = inject(TranslocoService);
  readonly #confirmService = inject(ConfirmService);
  readonly #datePipe = inject(DatePipe);
  readonly #eventBusStore = inject(EventBusStore);

  protected readonly Path = Path;

  hasImpfungenEditBerechtigung = this.benutzerStore.createHasRequiredRoleSig(hasImpfungenEditBerechtigung());

  itemTable = new TableModel<UntersuchungImpfungInternJson>(
    {rawListSignal: this.untersuchungImpfungenStore.impfungenInternList},
    {frontendOrBackend: 'frontend', pageOrScrollOrNothing: 'scroll'});

  constructor() {
    // Reload ImpfstoffChargen, falls sie geaendert haben
    this.#eventBusStore
      .on('IMPFSTOFF_CHARGEN_POTENTIALLY_CHANGED')
      .pipe(takeUntilDestroyed())
      .subscribe(_event => {
        this.impfstoffeChargenStore.loadImpfstoffeAndChargen();
      });
  }

  async createImpfung() {
    await this.openImpfung(undefined, 'create');
  }

  async editImpfung(impfung: UntersuchungImpfungInternJson) {
    await this.openImpfung(impfung, 'edit');
  }

  async impfungVerabreicht(impfung: UntersuchungImpfungInternJson) {
    await this.openImpfung(impfung, 'verabreicht');
  }

  async impfungNichtVerabreicht(impfung: UntersuchungImpfungInternJson) {
    await this.openImpfung(impfung, 'nicht-verabreicht');
  }

  private async openImpfung(impfung: UntersuchungImpfungInternJson | undefined, inputMode: ImpfungPopupMode) {
    const user = this.#benutzerOptionsStore.getOptionForBenutzer(this.benutzerStore.benutzer()?.id);
    const ngbModalRef = this.#ngModal.open(UntersuchungImpfungenInternComponent, {size: 'lg'});
    ngbModalRef.componentInstance.impfung.set(impfung?.data);
    ngbModalRef.componentInstance.inputMode.set(inputMode);
    ngbModalRef.componentInstance.currentUser.set(requireValue(user));
    ngbModalRef.componentInstance.anlassConfig.set(requireValue(this.anlassStore.selectedEntity()?.anlassConfig));
    const result: UntersuchungImpfungInternSaveJson = await firstValueFrom(ngbModalRef.closed,
      {defaultValue: undefined});
    if (result) {
      if (impfung) {
        if (inputMode === 'verabreicht') {
          result.userDurchgefuehrt = user;
          result.datumImpfung = toISODateString(constructNow(undefined));
          result.impfStatus = ImpfStatus.DURCHGEFUEHRT;
        }
        if (inputMode === 'nicht-verabreicht') {
          result.userDurchgefuehrt = undefined;
          result.datumImpfung = toISODateString(constructNow(undefined));
          result.impfStatus = ImpfStatus.NICHT_DURCHGEFUEHRT;
        }
        this.untersuchungImpfungenStore.updateImpfungIntern({
          anlassId: requireValue(this.anlassStore.selectedEntity()).id,
          einladungId: requireValue(this.untersuchungStore.currentTab()).einladungID,
          impfungId: impfung.id,
          impfung: result,
        });
      } else {
        this.untersuchungImpfungenStore.createImpfungIntern({
          anlassId: requireValue(this.anlassStore.selectedEntity()).id,
          einladungId: requireValue(this.untersuchungStore.currentTab()).einladungID,
          impfung: result,
        });
      }
    }
  }

  removeImpfung(impfung: UntersuchungImpfungInternJson) {
    this.#confirmService.confirm({
      question: this.#translationService.translate('impfungintern.action.delete.question', {
        impfstoff: impfung.data.impfstoff.data.name,
        datum: this.#datePipe.transform(impfung.data.datumImpfung, 'dd.MM.yyyy'),
      }),
      consequence: this.#translationService.translate('impfungintern.action.delete.consequence'),
      decision: this.#translationService.translate('impfungintern.action.delete.decision'),
    }).subscribe(() => {
      this.untersuchungImpfungenStore.removeImpfungIntern({
        anlassId: requireValue(this.anlassStore.selectedEntity()).id,
        einladungId: requireValue(this.untersuchungStore.currentTab()).einladungID,
        impfungId: impfung.id,
      });
    });
  }

  getImmunitaeten(item: UntersuchungImpfungInternJson): string {
    const immunitaeten = item.data.krankheitenImmunitaeten;
    if (immunitaeten.length !== 0) {
      if (immunitaeten.every(i => i.immunitaetGenuegend)) {
        return this.#translationService.translate('Immunitaet.GENUEGEND');
      }
      if (immunitaeten.some(i => i.immunitaetGenuegend)) {
        return this.#translationService.translate('Immunitaet.TEILWEISE_GENUEGEND');
      }
      if (immunitaeten.every(i => !i.immunitaetGenuegend)) {
        return this.#translationService.translate('Immunitaet.NICHT_GENUEGEND');
      }
    }
    return this.#translationService.translate('Immunitaet.null');
  }
}
