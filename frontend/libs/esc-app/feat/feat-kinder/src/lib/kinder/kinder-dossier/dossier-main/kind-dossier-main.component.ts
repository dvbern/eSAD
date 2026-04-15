import {DatePipe} from '@angular/common';
import {ChangeDetectionStrategy, Component, computed, inject} from '@angular/core';
import {FormsModule} from '@angular/forms';
import {RouterLink} from '@angular/router';
import {
  BenutzerOptionJson,
  DossierMedizinischSaveJson,
  EntityAktivStatus,
  EsadIdJson,
  Geschlecht,
  NachkontrolleSaveJson,
  PapierakteStatus,
} from '@dvb/data-access-backend';
import {randomEntityId, requireValidBackendEntityId} from '@dvb/data-access-types';
import {
  BenutzerOptionsStore,
  BenutzerStore,
  BriefvorlagenStore,
  ConfirmService,
  getAdresse,
  Path,
  TenantSettingsStore,
  TimetravelStore,
} from '@dvb/feat-shared';
import {
  ButtonComponent,
  CardComponent,
  CardHeaderComponent,
  CollapsibleMenuComponent,
  IconComponent,
  IconUseCase,
  NgSelectDirective,
  StatusTextComponent,
  TileComponent,
  TileGridComponent,
} from '@dvb/ui-components';
import {AppNotificationStore} from '@dvb/util-angular';
import {
  fromISODateString,
  hasAnlassEditBerechtigungForAbgeschlossen,
  hasDownloadDossierBerechtigung,
  hasKindEditBerechtigung,
  hasKinderMedizinischeDatenEditBerechtigung,
  hasKinderMedizinischeDatenViewBerechtigung,
  hasKindVorlagenViewBerechtigung,
  isNotNullish,
  isNullish,
} from '@dvb/util-shared';
import {TranslocoPipe, TranslocoService} from '@jsverse/transloco';
import {NgbDropdownItem, NgbModal} from '@ng-bootstrap/ng-bootstrap';
import {NgOptionComponent, NgSelectComponent} from '@ng-select/ng-select';
import {firstValueFrom} from 'rxjs';
import {DossierStore} from '../../dossier.store';
import {KindAnlaesseStore} from '../dossier-anlaesse/kind-anlaesse-store.service';
import {
  KinderBriefgenerierenModalComponent,
} from '../kinder-briefgenerieren-modal/kinder-briefgenerieren-modal.component';
import {KinderBriefgenerierenJson} from '../kinder-briefgenerieren-modal/kinder-briefgenerieren.form';
import {KinderDossierExportModalComponent} from '../kinder-dossier-export-modal/kinder-dossier-export-modal.component';
import {KinderDossierExportFormOutputType} from '../kinder-dossier-export-modal/kinder-dossier-export.form';
import {KinderEsadidModalComponent} from '../kinder-esadid-modal/kinder-esadid-modal.component';
import {KinderImmunitaetModalComponent} from '../kinder-immunitaet-modal/kinder-immunitaet-modal.component';
import {KinderImmunitaetJson} from '../kinder-immunitaet-modal/kinder-immunitaet.form';
import {KinderNachkontrolleModalComponent} from '../kinder-nachkontrolle-modal/kinder-nachkontrolle-modal.component';
import {
  KinderZusatzinformationenModalComponent,
} from '../kinder-zusatzinformationen-modal/kinder-zusatzinformationen-modal.component';
import {KinderZusatzinformationenJson} from '../kinder-zusatzinformationen-modal/kinder-zusatzinformationen.form';

@Component({
  selector: 'feat-kind-dossier-main',
  templateUrl: './kind-dossier-main.component.html',
  changeDetection: ChangeDetectionStrategy.OnPush,
  imports: [
    CardComponent,
    CardHeaderComponent,
    IconComponent,
    TileComponent,
    TileGridComponent,
    DatePipe,
    TranslocoPipe,
    FormsModule,
    CollapsibleMenuComponent,
    NgbDropdownItem,
    RouterLink,
    NgOptionComponent,
    NgSelectComponent,
    NgSelectDirective,
    StatusTextComponent,
    ButtonComponent,
  ],
})
export class KindDossierMainComponent {

  readonly dossierStore = inject(DossierStore);
  readonly benutzerOptionsStore = inject(BenutzerOptionsStore);
  readonly benutzerStore = inject(BenutzerStore);
  readonly confirmService = inject(ConfirmService);
  readonly transloco = inject(TranslocoService);
  readonly appNotificationStore = inject(AppNotificationStore);
  readonly #ngbModal = inject(NgbModal);
  readonly timetravelStore = inject(TimetravelStore);
  readonly #briefvorlagenStore = inject(BriefvorlagenStore);
  readonly kindAnlassStore = inject(KindAnlaesseStore);
  readonly tenantSettingsStore = inject(TenantSettingsStore);

  dossier = computed(() => this.dossierStore.selectedDossier());
  kind = this.dossierStore.selectedKind;
  fullName = computed(() => this.kind()?.data?.name + ' ' + this.kind()?.data?.vorname);
  geschlecht = computed(() => this.getUseCaseGechlecht(this.kind()?.data?.geschlecht));
  idNr = computed(() => this.dossier()?.kind?.esadId ?? '');
  adresse = computed(() => getAdresse(this.kind()?.data?.adresse));

  zustaendigArzt = computed(() => this.dossier()?.dataNichtMedizinisch?.zustaendigArzt);
  zustaendigMPA = computed(() => this.dossier()?.dataNichtMedizinisch?.zustaendigMPA);

  canActivateKind = this.benutzerStore.createHasRequiredRoleSig(hasKindEditBerechtigung());
  canReadMedizinischeDaten = this.benutzerStore.createHasRequiredRoleSig(hasKinderMedizinischeDatenViewBerechtigung());
  canWriteMedizinischeDaten = this.benutzerStore.createHasRequiredRoleSig(hasKinderMedizinischeDatenEditBerechtigung());

  canEditKind = computed(() =>
    this.benutzerStore.createHasRequiredRoleSig(hasKindEditBerechtigung())() && this.dossierStore.isKindAktiv()
  );
  canDownloadDossierExport = this.benutzerStore.createHasRequiredRoleSig(hasDownloadDossierBerechtigung());

  canEditAfterAbgeschlossen = this.benutzerStore.createHasRequiredRoleSig(hasAnlassEditBerechtigungForAbgeschlossen());

  canViewBriefvorlagen = this.benutzerStore.createHasRequiredRoleSig(hasKindVorlagenViewBerechtigung());

  canChangeIdNr = computed(() => {
    if (this.kindAnlassStore.isKindInAnyAbgeschlossenAnlass()) {
      return this.canEditKind() && this.canEditAfterAbgeschlossen();
    }
    return this.canEditKind();
  });

  papierakteStatusOptions = Object.values(PapierakteStatus);
  papierakteStatus = computed(() => this.dossier()?.dataNichtMedizinisch?.papierakteStatus);

  protected readonly Path = Path;
  protected readonly fromISODateString = fromISODateString;

  compareBenutzerFn(item: BenutzerOptionJson, selected: BenutzerOptionJson): boolean {
    return item.id === selected.id;
  }

  private getUseCaseGechlecht(geschlecht: Geschlecht | undefined): IconUseCase {
    if (isNullish(geschlecht)) {
      return 'divers';
    }

    switch (geschlecht) {
      case 'MAENNLICH':
        return 'male';
      case 'WEIBLICH':
        return 'female';
      case 'DIVERS':
        return 'divers';
      case 'UNBEKANNT':
        return 'placeholder_seethrough';
    }
  }

  protected activateKind(): void {
    const kind = this.dossierStore.selectedDossier()?.kind;
    if (isNotNullish(kind)) {
      this.dossierStore.activateKind({
        kindId: kind.id,
        saveJson: {
          entityAktivStatus: EntityAktivStatus.AKTIV,
        },
        onSuccess: () => {
          this.appNotificationStore.addNotification({
            alertType: 'success',
            id: 'kindActivated' + randomEntityId(),
            summary: this.transloco.translate('kind.dossier.activateKind.success', {
              name: kind.data.name,
              vorname: kind.data.vorname
            }),
            autohide: true,
          });
        }
      });
    }
  }

  protected generateIdForKind(): void {
    const kind = this.kind();
    if (isNotNullish(this.dossierStore.selectedDossier) && isNotNullish(kind)) {
      // Generate ID for Kind if the Schule is Present
      if (isNotNullish(kind.data.schule)) {
        const name = this.kind()?.data?.name + ' ' + this.kind()?.data?.vorname;
        this.confirmService.confirm({
          question: this.transloco.translate('kind.dossier.id.generieren.question', {fullname: name}),
          consequence: this.transloco.translate('kind.dossier.id.generieren.consequence'),
          decision: this.transloco.translate('kind.dossier.id.generieren.decision'),
        }).subscribe(() => {
          this.dossierStore.generateEsadIdForKind(kind.id);
        });

      } else {
        // If there is no Schule, the ID will be generated with 000 as Schulhausnummer
        this.confirmService.confirm({
          question: this.transloco.translate('kind.dossier.id.generieren.keine.schule.question'),
          consequence: this.transloco.translate('kind.dossier.id.generieren.keine.schule.consequence'),
          decision: this.transloco.translate('kind.dossier.id.generieren.keine.schule.decision'),
        }).subscribe(() => {
          this.dossierStore.generateEsadIdForKind(kind.id);
        });
      }
    } else {
      this.appNotificationStore.addNotification({
        alertType: 'warning',
        id: 'noSchulhausNummerForSchule' + randomEntityId(),
        summary: this.transloco.translate('ui.validation-error.kindOrDossierMissingWhenTryingToGenerateId'),
        autohide: true,
      });
    }
  }

  async setIdForKind(): Promise<void> {
    const kind = this.kind();
    if (isNotNullish(kind) && this.canEditKind()) {
      const ngbModalRef = this.#ngbModal.open(KinderEsadidModalComponent, {size: 'lg'});
      const result: EsadIdJson | undefined = await firstValueFrom(ngbModalRef.closed,
        {defaultValue: undefined});
      if (result) {
        this.dossierStore.setEsadIdForKind({
          kindId: kind.id,
          esadId: result,
        });
      }
    }
  }

  onArztChange(arzt: BenutzerOptionJson): void {
    const dossier = this.dossier();
    if (isNotNullish(arzt) && isNotNullish(dossier)) {
      this.dossierStore.updateDossierVerantwortlichArzt({
        dossier: dossier,
        arztOption: arzt
      });
    }
  }

  onMPAChange(mpa: BenutzerOptionJson): void {
    const dossier = this.dossier();
    if (isNotNullish(mpa) && isNotNullish(dossier)) {
      this.dossierStore.updateDossierVerantwortlichMpa({
        dossier: dossier,
        mpaOption: mpa
      });
    }
  }

  async editZusatzinformationen(): Promise<void> {
    const dossier = this.dossier();
    if (dossier && this.canEditKind() && dossier.dataMedizinisch) {
      const ngbModalRef = this.#ngbModal.open(KinderZusatzinformationenModalComponent, {size: 'lg'});
      ngbModalRef.componentInstance.dossierData.set(dossier.dataMedizinisch);
      const result: KinderZusatzinformationenJson | undefined = await firstValueFrom(ngbModalRef.closed,
        {defaultValue: undefined});
      if (result) {
        const requestObject: DossierMedizinischSaveJson = {
          ...dossier.dataMedizinisch,
          kinderschutzfall: result.kinderschutzfall,
          keineAuskunftAnDrittpersonen: result.keineAuskunftAnDrittpersonen,
          sansPapier: result.sansPapier,
          andereZusatzinformation: result.andereZusatzinformation,
          andereZusatzInformationText: result.andereZusatzInformationText,

        };

        this.dossierStore.updateDossierMedizinisch({
          dossier: requestObject,
          onSuccess: () => this.appNotificationStore.addSuccessNotification(this.transloco.translate(
            'kind.dossier.zusatzinformationen.sucess')),
        });
      }
    }
  }

  async editImmunisierungen(): Promise<void> {
    const dossier = this.dossier();
    if (dossier && this.canEditKind() && dossier.dataMedizinisch) {
      const ngbModalRef = this.#ngbModal.open(KinderImmunitaetModalComponent, {size: 'lg'});
      ngbModalRef.componentInstance.dossierData.set(dossier.dataMedizinisch);
      const result: KinderImmunitaetJson | undefined = await firstValueFrom(ngbModalRef.closed,
        {defaultValue: undefined});
      if (result) {
        const requestObject: DossierMedizinischSaveJson = {
          ...dossier.dataMedizinisch,
          immunitaetMasern: result.immunitaetMasern,
          immunitaetVarizellen: result.immunitaetVarizellen,
        };

        this.dossierStore.updateDossierMedizinisch({
          dossier: requestObject,
          onSuccess: () => this.appNotificationStore.addSuccessNotification(this.transloco.translate(
            'kind.dossier.immunisierung.sucess')),
        });
      }
    }
  }

  async editNachkontrolle(): Promise<void> {
    const dossier = this.dossier();
    if (dossier && this.canEditKind() && dossier.dataMedizinisch) {
      const ngbModalRef = this.#ngbModal.open(KinderNachkontrolleModalComponent, {size: 'lg'});
      ngbModalRef.componentInstance.dossierData.set(dossier.dataMedizinisch);
      const result: NachkontrolleSaveJson | undefined =
        await firstValueFrom(ngbModalRef.closed, {defaultValue: undefined});
      if (result) {
        const requestObject: DossierMedizinischSaveJson = {
          ...dossier.dataMedizinisch,
          nachkontrolle: {
            ...dossier.dataMedizinisch.nachkontrolle,
            data: result,
          },
        };
        this.dossierStore.updateDossierMedizinisch({
          dossier: requestObject,
          onSuccess: () => this.appNotificationStore.addSuccessNotification(this.transloco.translate(
            'kind.dossier.nachkontrolle.success')),
        });
      }
    }
  }

  async briefGenerieren(): Promise<void> {
    const kind = this.kind();
    if (isNotNullish(kind)) {
      const ngbModalRef = this.#ngbModal.open(KinderBriefgenerierenModalComponent, {size: 'lg'});
      ngbModalRef.componentInstance.kindData.set(kind);
      ngbModalRef.componentInstance.anlassList.set(this.kindAnlassStore.entities());
      const result: KinderBriefgenerierenJson | undefined = await firstValueFrom(ngbModalRef.closed,
        {defaultValue: undefined});
      if (result) {
        this.#briefvorlagenStore.downloadBrief({
          briefvorlageId: requireValidBackendEntityId(result.vorlage?.id),
          kindId: kind.id,
          einladungId: result.einladung?.id,
          arztId: result.arztId,
          onSuccess: () =>
            this.appNotificationStore.addSuccessNotification(this.transloco.translate(
              'kind.dossier.briefgenerieren.success')),
        });
      }
    }
  }

  onPapierakteStatusChanged(papierakteStatus: PapierakteStatus): void {
    const dossier = this.dossier();
    if (isNotNullish(dossier)) {
      this.dossierStore.updateDossierPapierakteStatus({
        dossier: dossier,
        status: papierakteStatus,
      });
    }
  }

  async exportDossier(): Promise<void> {
    const dossier = this.dossier();
    if (dossier) {
      const ngbModalRef = this.#ngbModal.open(KinderDossierExportModalComponent, {size: 'lg'});
      const result: KinderDossierExportFormOutputType | undefined =
        await firstValueFrom(ngbModalRef.closed, {defaultValue: undefined});
      if (result) {
        this.dossierStore.downloadKinderdossierReport({
          kindId: dossier.kind.id,
          variation: result.kinderdossierPrintVariant,
        });
      }
    }
  }

  protected readonly EntityAktivStatus = EntityAktivStatus;
}
