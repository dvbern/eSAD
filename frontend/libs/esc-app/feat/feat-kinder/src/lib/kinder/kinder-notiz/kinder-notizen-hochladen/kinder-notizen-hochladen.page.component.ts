import { ChangeDetectionStrategy, Component, computed, inject } from '@angular/core';
import { ReactiveFormsModule } from '@angular/forms';
import {
  BenutzerStore,
  globalConstants,
  NotizenFileParams,
  NotizenMobileUploadComponent,
  NotizenSaveParams,
  PageComponent,
  Path,
} from '@dvb/feat-shared';
import { TranslocoPipe, TranslocoService } from '@jsverse/transloco';
import { DossierStore } from '../../dossier.store';
import { KinderNotizStore } from '../kinder-notiz/kinder-notiz.store';
import { BackendEntityId } from '@dvb/data-access-types';
import { hasAllNotizenEditBerechtigung, isNotNullish } from '@dvb/util-shared';
import { AppNotificationStore } from '@dvb/util-angular';
import { Location } from '@angular/common';
import { NotizTyp } from '@dvb/data-access-backend';

@Component({
  selector: 'feat-kinder-notizen-hochladen.page',
  standalone: true,
  templateUrl: './kinder-notizen-hochladen.page.component.html',
  styleUrl: './kinder-notizen-hochladen.page.component.scss',
  changeDetection: ChangeDetectionStrategy.OnPush,
  imports: [
    TranslocoPipe,
    PageComponent,
    ReactiveFormsModule,
    NotizenMobileUploadComponent,
  ],
})
export class KinderNotizenHochladenPageComponent {

  readonly kinderNotizStore = inject(KinderNotizStore);
  readonly dossierStore = inject(DossierStore);
  readonly #benutzerStore = inject(BenutzerStore);

  readonly #transloco = inject(TranslocoService);
  readonly #notificationService = inject(AppNotificationStore);
  readonly location = inject(Location);

  protected readonly Path = Path;
  protected readonly notizTyp = NotizTyp;

  pageTitle = `notizen.hochladen.page.title`;

  kindId = computed<BackendEntityId | undefined>(() => this.dossierStore.selectedDossier()?.kind.id);

  canEditAllNotizen = this.#benutzerStore.createHasRequiredRoleSig(hasAllNotizenEditBerechtigung());

  onSaveNotiz(payload: NotizenSaveParams) {
    const kindIdOrUndefined = this.kindId();
    if (!kindIdOrUndefined) {
      console.warn('no kind id can not save notiz');
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
        onSuccess: (_) => {
          this.afterSuccessfulSaveAndUpload();
        },
      });
    } else {
      this.kinderNotizStore.updateNotiz({
        notiz: payload.notiz,
        fileUploads: payload.fileUploads ?? [],
        onSuccess: (_) => {
          this.afterSuccessfulSaveAndUpload();
        },
      });
    }
  }

  onRemoveNotizFile(params: NotizenFileParams) {
    this.kinderNotizStore.removeNotizFile(params);
  }

  private afterSuccessfulSaveAndUpload(): void {
    this.#notificationService.addSuccessNotification(this.#transloco.translate('notizen.action.mobile.upload.success'));
  }

}
