import {ChangeDetectionStrategy, Component, computed, inject, viewChild} from '@angular/core';
import {ReactiveFormsModule} from '@angular/forms';
import {ActivatedRoute, Router} from '@angular/router';
import {LabelJson, NotizStatus, NotizTyp} from '@dvb/data-access-backend';
import {BackendEntityId} from '@dvb/data-access-types';
import {
  BenutzerOptionsStore,
  BenutzerStore,
  globalConstants,
  LabelsStore,
  NotizenComponent,
  NotizenFileParams,
  NotizenSaveParams,
  PageComponent,
  Path,
} from '@dvb/feat-shared';
import {AppNotificationStore, DirtyMarker} from '@dvb/util-angular';
import {hasAllNotizenEditBerechtigung, isNotNullish, requireValue} from '@dvb/util-shared';
import {TranslocoPipe, TranslocoService} from '@jsverse/transloco';
import {DossierStore} from '../../dossier.store';
import {KinderNotizStore} from './kinder-notiz.store';

@Component({
  selector: 'feat-kinder-notiz.page',
  templateUrl: './kinder-notiz.page.component.html',
  styleUrl: './kinder-notiz.page.component.scss',
  changeDetection: ChangeDetectionStrategy.OnPush,
  imports: [
    TranslocoPipe,
    PageComponent,
    ReactiveFormsModule,
    NotizenComponent,
  ],
})
export class KinderNotizPageComponent implements DirtyMarker {
  notizenComponent = viewChild.required(NotizenComponent);

  readonly kinderNotizStore = inject(KinderNotizStore);
  readonly dossierStore = inject(DossierStore);
  readonly #labelStore = inject(LabelsStore);
  readonly #benutzerStore = inject(BenutzerStore);
  readonly #benutzerOptionsStore = inject(BenutzerOptionsStore);

  readonly #transloco = inject(TranslocoService);
  readonly #router = inject(Router);
  readonly #activatedRoute = inject(ActivatedRoute);
  readonly #notificationService = inject(AppNotificationStore);

  protected readonly Path = Path;
  protected readonly requireValue = requireValue;

  mode = computed(() => this.kinderNotizStore.selectedIsNew() ? 'create' : 'edit');

  pageTitle = computed(() => {
    return `notizen.detail.${this.mode()}.titel`;
  });

  isDirty(): boolean {
    return this.notizenComponent().isDirty() || this.kinderNotizStore.saveLoader.isLoading();
  }

  kindId = computed<BackendEntityId | undefined>(() => this.dossierStore.selectedDossier()?.kind.id);

  canEditAllNotizen = this.#benutzerStore.createHasRequiredRoleSig(hasAllNotizenEditBerechtigung());

  defaultNotizSig = computed(() => {
      if (!this.canEditAllNotizen()) {
        // Es duerfen nur Notizen mit dem Label Dispensationsgesuch erstellt werden
        const label: LabelJson | undefined = this.#labelStore.dispensationsgesuchAutoLabel();
        const labels: LabelJson[] = label ? [label] : [];
        const currentUser = this.#benutzerOptionsStore.getOptionForBenutzer(this.#benutzerStore.benutzer()?.id);

        return {
          titel: this.#transloco.translate('notiz.dispensationsgesuch.title'),
          bemerkungen: undefined,
          status: NotizStatus.ABGESCHLOSSEN,
          typ: NotizTyp.DEFAULT,
          verantwortlicher: requireValue(currentUser),
          anlass: undefined,
          labels: labels,
          files: [],
          version: globalConstants.entityPrePersistVersion,
        };
      }
      return undefined;
    },
  );

  onSaveNotiz(payload: NotizenSaveParams) {
    this.saveNotiz(payload, () => {
      this.notizenComponent().resetAfterSuccessfulSave();
      this.#notificationService.addSuccessNotification(this.#transloco.translate('global.data.saved'));
      this.#goToEditPage();
    });
  }

  onSaveNotizSilent(payload: NotizenSaveParams) {
    this.saveNotiz(payload, (resultId: string) => {
      this.notizenComponent().resetAfterSuccessfulSave();
      payload.afterSave?.(resultId);
    });
  }

  saveNotiz(payload: NotizenSaveParams, onSuccess: (resultId: string) => void): void {
    const kindIdOrUndefined = this.kindId();
    if (!kindIdOrUndefined) {
      return;
    }
    if (isNotNullish(payload.fileUploads) && payload.fileUploads.length > globalConstants.notizen.uploadFilesMax) {
      this.#notificationService.addDangerNotification(this.#transloco.translate('ui.validation-error.max-files'));
      return;
    }

    if (this.kinderNotizStore.selectedIsNew()) {
      this.kinderNotizStore.createNotiz({
        notiz: payload.notiz,
        fileUploads: payload.fileUploads ?? [],
        onSuccess: (created) => onSuccess(created.id),
      });
    } else {
      this.kinderNotizStore.updateNotiz({
        notiz: payload.notiz,
        fileUploads: payload.fileUploads ?? [],
        onSuccess: (updated) => onSuccess(updated.id),
      });
    }
  }

  onRemoveNotizFile(params: NotizenFileParams) {
    this.kinderNotizStore.removeNotizFile(params);
  }

  #goToEditPage(): void {
    this.#router.navigate([
      Path.RELATIVE_ONE_UP,
      this.kinderNotizStore.selectedEntity()?.id
    ], {
      relativeTo: this.#activatedRoute,
      replaceUrl: true,
    });
  }

  public onMobileUpload(notizId : string) : void{
    // check what the current page url is to know if we are in create mode
    const isOnCreateRoute = this.#router.url.endsWith(`/${Path.CREATE}`);
    if (isOnCreateRoute) {
      this.#router.navigate([Path.RELATIVE_ONE_UP, notizId], {
        relativeTo: this.#activatedRoute,
        replaceUrl: true,
      }).then(() => {
        this.kinderNotizStore.loadNotizenForKind(requireValue(this.dossierStore.kindId()));
      });
    } else {
      this.kinderNotizStore.loadNotizenForKind(requireValue(this.dossierStore.kindId()));
    }
  }
}
