import {DatePipe} from '@angular/common';
import {ChangeDetectionStrategy, Component, computed, DestroyRef, effect, inject, model} from '@angular/core';
import {FormControl, FormRecord, FormsModule, NonNullableFormBuilder, ReactiveFormsModule} from '@angular/forms';
import {RouterLink} from '@angular/router';
import {
  AnlassStatus,
  EinladungJson,
  EinladungSaveJson,
  EinladungStatus,
  Geschlecht,
  Klassenstufe,
  NaechsteStation,
  NotizJson,
  NotizTyp,
} from '@dvb/data-access-backend';
import {
  BackendEntityId,
  toEinladungBackendEntityIdWithClass,
} from '@dvb/data-access-types';
import {BenutzerStore, NavigationService, Path, TenantSettingsStore} from '@dvb/feat-shared';
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
import {EinladungAbmeldenJson} from './modal/einladung-abmelden.form';

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
  readonly #fb = inject(NonNullableFormBuilder);
  readonly tenantSettingsStore = inject(TenantSettingsStore);

  protected readonly geschlechter = Object.values(Geschlecht);
  protected readonly einladungStatusValues = Object.values(this.tenantSettingsStore.availableEinladungStatus());
  protected readonly klassenStufeValues = Object.values(Klassenstufe);
  protected readonly naechsteStationValues = Object.values(NaechsteStation);
  protected readonly getUseCaseGechlecht = getUseCaseGechlecht;
  protected readonly Path = Path;
  protected readonly NotizTyp = NotizTyp;

  canDownloadDossierExport = this.#benutzerStore.createHasRequiredRoleSig(hasDownloadDossierBerechtigung());

  isAnlassStatusBereit = computed(() => this.anlassStore.selectedEntity()?.data.anlassStatus === AnlassStatus.BEREIT);
  showNachkontrolle = computed(() => !this.anlassStore.isAbgeschlossen() && this.tenantSettingsStore.nachkontrolleEnabled());

  itemTable = new TableModel<EinladungJsonEnhanced>(
    {rawListSignal: this.store.einladungen},
    {frontendOrBackend: 'frontend', pageOrScrollOrNothing: 'scroll'});

  allEinladungenMarkedForUntersuchung = model<boolean>(false);

  canEditNotiz = this.#benutzerStore.createHasRequiredRoleSig(hasAllNotizenEditBerechtigung());

  anlassFlags = Object.values(AnlassFlags);
  protected readonly formatTimeWithDateIfNotToday = formatTimeWithDateIfNotToday;

  readonly tableFilterForm = new EinladungenListeFilterForm({
    fb: this.#fb,
    destroyRef: inject(DestroyRef),
    paginatedTable: this.itemTable,
  });

  einladungStatusForm: FormRecord<FormControl<EinladungStatus>> = this.#fb.record({});

  constructor() {
    effect(() => {
      const disabled = !this.anlassStore.canUserEditAnlassInCurrentState();
      this.einladungStatusForm = this.#fb.record({});
      this.store.einladungen().forEach((einladung) => {
        this.einladungStatusForm.addControl(einladung.id,
          this.#fb.control({ value: einladung.data.einladungStatus, disabled }));
      });
    });
  }

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
    if ($event === EinladungStatus.TEILNAHME_ABGESAGT) {
      await this.addAbmeldungsgrund(item);
    } else {
      this.store.updateEinladungStatus({einladungId: item.id, status: $event});
    }
  }

  async addAbmeldungsgrund(einladung: EinladungJson) {
    if (this.tenantSettingsStore.hasAbmeldungsEinladungsStatus()) {
      const ngbModalRef = this.#ngModal.open(EinladungAbmeldenComponent, {size: 'lg'});
      ngbModalRef.componentInstance.einladungData.set(einladung);
      ngbModalRef.componentInstance.saveForm.subscribe(
        (form: EinladungAbmeldenJson) => {
          const requestObject: EinladungSaveJson = {
            ...einladung.data,
            einladungStatus: EinladungStatus.TEILNAHME_ABGESAGT,
            bemerkungAbmeldung: form.bemerkungAbmeldung,
            artAbmeldung: form.artAbmeldung,
            grundAbmeldung: form.grundAbmeldung,
          };
          this.store.einladungAbmelden({
            einladungId: toEinladungBackendEntityIdWithClass(einladung.id),
            einladungSaveJson: requestObject,
            onSuccess: () => {
              ngbModalRef.close({result: 'success'});
            },
          });
        },
      );

      const result = await firstValueFrom(ngbModalRef.closed, {defaultValue: undefined});

      if (!result) {
        const previousStatus = this.store.einladungById(einladung.id)?.data?.einladungStatus;
        this.einladungStatusForm.controls[einladung.id].reset(previousStatus);
      }
    } else{
      console.info('This tenant does not have an Einladungsstatus for abmeldung, so no popup is shown for this case.');
    }
  }

  /**
   * Because we need the dropdown to update when declining the AbmeldungsGrund popup, we need the list to consider that
   * an element change, hence we integrate the einladungStatus as part of the unique signature.
   */
  customTrackBy(index: number, item: EinladungJsonEnhanced) {
    return `${item.id}-${item.data.einladungStatus}`;
  }

  gotoEinladungNotiz(einladung: EinladungJson, notizTyp: NotizTyp): void {
    this.navigationService.navigateToEinladungNotiz(einladung.data.anlassId, einladung.id, notizTyp);
  }

  downloadKinderdossier(einladung: EinladungJson): void {
    this.store.downloadKinderdossierReport({kindId: einladung.kind.id, anlassId: einladung.data.anlassId});
  }

  hasNotizFile(notizen: NotizJson[], typ: NotizTyp): boolean {
    return notizen.some(notiz => notiz.data.typ === typ && notiz.data.files && notiz.data.files.length > 0);
  }
}
