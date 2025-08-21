import {DatePipe} from '@angular/common';
import {ChangeDetectionStrategy, Component, computed, DestroyRef, inject, model} from '@angular/core';
import {FormsModule, NonNullableFormBuilder, ReactiveFormsModule} from '@angular/forms';
import {RouterLink} from '@angular/router';
import {
  AnlassStatus,
  EinladungJson,
  EinladungSaveJson,
  EinladungStatus,
  Geschlecht,
  Klassenstufe,
  NaechsteStation,
} from '@dvb/data-access-backend';
import {BackendEntityId} from '@dvb/data-access-types';
import {BenutzerStore, NavigationService, Path} from '@dvb/feat-shared';
import {
  BadgeComponent,
  CardComponent,
  CardHeaderComponent,
  CheckboxDirective,
  CollapsibleMenuComponent,
  ColWidthComponent,
  EmptyTableMessageComponent,
  IconComponent,
  IconOnlyButtonComponent,
  NgSelectDirective,
  SortableComponent,
  SummaryValuesComponent,
  TableComponent,
  TableControlComponent,
  TableFilterComponent,
  TableLinkComponent,
  TextInputDirective,
} from '@dvb/ui-components';
import {TableModel} from '@dvb/util-angular';
import {
  formatTimeWithDateIfNotToday,
  hasAllNotizenEditBerechtigung,
  hasDownloadDossierBerechtigung,
  isNullish,
} from '@dvb/util-shared';
import {TranslocoPipe} from '@jsverse/transloco';
import {NgbDropdownItem, NgbModal} from '@ng-bootstrap/ng-bootstrap';
import {NgOptionComponent, NgSelectComponent} from '@ng-select/ng-select';
import {firstValueFrom} from 'rxjs';
import {AnlassStore} from '../../../anlass.store';
import {getUseCaseGechlecht} from '../../anlass-bearbeiten/kinder/kinder.util';
import {AnlassKinderStore, EinladungJsonEnhanced} from '../../anlass-kinder.store';
import {EinladungenListeFilterForm} from './einladungen-liste-filter.form';
import {EinladungAbmeldenComponent} from './modal/einladung-abmelden.component';

export enum AnlassFlags {
  NACHKONTROLLESEHEN = 'NachkontrolleSehen',
  NACHKONTROLLEHOEREN = 'NachkontrolleHoeren',
  NACHKONTROLLEGROESSEGEWICHT = 'NachkontrolleGroesseGewicht',
  NACHKONTROLLESPRACHE = 'NachkontrolleSprache',
  NACHKONTROLLEMOTORIK = 'NachkontrolleMotorik',
  NACHKONTROLLEANDERES = 'NachkontrolleAnderes',
}

@Component({
  selector: 'feat-anlass-planung-einladungen-liste',
  templateUrl: './einladungen-liste.component.html',
  changeDetection: ChangeDetectionStrategy.OnPush,
  imports: [
    FormsModule,
    SortableComponent,
    TableComponent,
    TranslocoPipe,
    DatePipe,
    IconComponent,
    CardComponent,
    TableFilterComponent,
    TextInputDirective,
    NgOptionComponent,
    NgSelectComponent,
    NgSelectDirective,
    ReactiveFormsModule,
    IconOnlyButtonComponent,
    CardHeaderComponent,
    CollapsibleMenuComponent,
    NgbDropdownItem,
    RouterLink,
    CheckboxDirective,
    TableControlComponent,
    EmptyTableMessageComponent,
    BadgeComponent,
    TableLinkComponent,
    ColWidthComponent,
    SummaryValuesComponent,
  ],
})
export class EinladungenListeComponent {

  readonly store = inject(AnlassKinderStore);
  readonly anlassStore = inject(AnlassStore);
  readonly #ngModal = inject(NgbModal);
  readonly navigationService = inject(NavigationService);
  readonly #benutzerStore = inject(BenutzerStore);

  protected readonly geschlechter = Object.values(Geschlecht);
  protected readonly einladungStatusValues = Object.values(EinladungStatus);
  protected readonly klassenStufeValues = Object.values(Klassenstufe);
  protected readonly naechsteStationValues = Object.values(NaechsteStation);
  protected readonly getUseCaseGechlecht = getUseCaseGechlecht;
  protected readonly Path = Path;
  canDownloadDossierExport = this.#benutzerStore.createHasRequiredRoleSig(hasDownloadDossierBerechtigung());

  isAnlassStatusBereit = computed(() => this.anlassStore.selectedEntity()?.data.anlassStatus === AnlassStatus.BEREIT);

  itemTable = new TableModel<EinladungJsonEnhanced>(
    {rawListSignal: this.store.einladungen},
    {frontendOrBackend: 'frontend', pageOrScrollOrNothing: 'scroll'});

  allEinladungenMarkedForUntersuchung = model<boolean>(false);

  canEditNotiz = this.#benutzerStore.createHasRequiredRoleSig(hasAllNotizenEditBerechtigung());

  anlassFlags = Object.values(AnlassFlags);
  protected readonly formatTimeWithDateIfNotToday = formatTimeWithDateIfNotToday;

  readonly tableFilterForm = new EinladungenListeFilterForm({
    fb: inject(NonNullableFormBuilder),
    destroyRef: inject(DestroyRef),
    paginatedTable: this.itemTable,
  });

  public isMarkedForUntersuchung(id: BackendEntityId): boolean {
    return this.store.isEinladungMarkedForUntersuchung(id);
  }

  public toggleMarkedForUntersuchung(id: BackendEntityId): void {
    if (this.store.isEinladungMarkedForUntersuchung(id)) {
      this.store.unmarkEinladungForUntersuchung(id);
    } else {
      this.store.markEinladungForUntersuchung(id);
    }
  }

  public toggleAllMarkedForUntersuchung(): void {
    if (this.allEinladungenMarkedForUntersuchung()) {
      this.store.markAllEinladungenForUntersuchung((this.itemTable.filteredSortedList().map(
          item => item.id)
      ));
    } else {
      this.store.unmarkAllEinladungenForUntersuchung();
    }
  }

  async einladungsstatusChanged(item: EinladungJson, $event: EinladungStatus) {
    this.store.updateEinladungStatus({einladungId: item.id, status: $event});
    if ($event === EinladungStatus.TEILNAHME_ABGESAGT) {
      await this.addAbmeldungsgrund(item);
    }
  }

  async addAbmeldungsgrund(einladung: EinladungJson) {
    const ngbModalRef = this.#ngModal.open(EinladungAbmeldenComponent, {size: 'lg'});
    ngbModalRef.componentInstance.einladungData.set(einladung);
    const result = await firstValueFrom(ngbModalRef.closed, {defaultValue: undefined});
    if (result) {
      const requestObject: EinladungSaveJson = {
        ...einladung.data,
        einladungStatus: EinladungStatus.TEILNAHME_ABGESAGT,
        bemerkungAbmeldung: result.bemerkungAbmeldung,
        artAbmeldung: result.artAbmeldung,
        grundAbmeldung: result.grundAbmeldung,
      };
      this.store.einladungAbmelden({
        einladungId: einladung.id,
        einladungSaveJson: requestObject,
      });
    } else {
      // Only reset if we haven't filled out a reason
      if (isNullish(einladung.data.grundAbmeldung)) {
        this.store.updateEinladungStatus({einladungId: einladung.id, status: EinladungStatus.TEILNAHME_BESTAETIGT});
      }
    }
  }

  /**
   * Because we need the dropdown to update when declining the AbmeldungsGrund popup, we need the list to consider that
   * an element change, hence we integrate the einladungStatus as part of the unique signature.
   */
  customTrackBy(index: number, item: EinladungJsonEnhanced) {
    return `${item.id}-${item.data.einladungStatus}`;
  }

  gotoImpfausweis(einladung: EinladungJson): void {
    this.navigationService.navigateToEinladungImpfausweis(einladung.data.anlassId, einladung.id);
  }

  gotoFragebogen(einladung: EinladungJson): void {
    this.navigationService.navigateToEinladungFragebogen(einladung.data.anlassId, einladung.id);
  }

  downloadKinderdossier(einladung: EinladungJson): void {
    this.store.downloadKinderdossierReport({kindId: einladung.kind.id, anlassId: einladung.data.anlassId});
  }
}
