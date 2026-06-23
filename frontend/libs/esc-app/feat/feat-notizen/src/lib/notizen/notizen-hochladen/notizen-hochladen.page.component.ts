import { ChangeDetectionStrategy, Component, computed, inject } from '@angular/core';

import { TranslocoPipe, TranslocoService } from '@jsverse/transloco';
import {
  BenutzerStore,
  globalConstants,
  NotizenFileParams,
  NotizenMobileUploadComponent,
  NotizenSaveParams,
  PageComponent,
  Path,
} from '@dvb/feat-shared';
import { ReactiveFormsModule } from '@angular/forms';
import { NotizDetailStore } from '../notiz-detail.store';
import { DossierStore } from '@dvb/feat-kinder';
import { AppNotificationStore } from '@dvb/util-angular';
import { hasAllNotizenEditBerechtigung, isNotNullish } from '@dvb/util-shared';
import { Location } from '@angular/common';
import { NotizTyp } from '@dvb/data-access-backend';

@Component({
  selector: 'feat-notizen-hochladen.page',
  standalone: true,
  templateUrl: './notizen-hochladen.page.component.html',
  styleUrl: './notizen-hochladen.page.component.scss',
  changeDetection: ChangeDetectionStrategy.OnPush,
  imports: [
    TranslocoPipe,
    PageComponent,
    ReactiveFormsModule,
    NotizenMobileUploadComponent,
  ],
})
export class NotizenHochladenPageComponent {

  readonly notizDetailStore = inject(NotizDetailStore);
  readonly dossierStore = inject(DossierStore);
  readonly #benutzerStore = inject(BenutzerStore);

  readonly #transloco = inject(TranslocoService);
  readonly #notificationService = inject(AppNotificationStore);
  readonly location = inject(Location);

  protected readonly Path = Path;
  protected readonly notizTyp = NotizTyp;

  pageTitle = `notizen.hochladen.page.title`;

  kind = computed(() => this.notizDetailStore.kind());

  canEditAllNotizen = this.#benutzerStore.createHasRequiredRoleSig(hasAllNotizenEditBerechtigung());

  onSaveNotiz(payload: NotizenSaveParams) {
    if (isNotNullish(payload.fileUploads) && payload.fileUploads.length > globalConstants.notizen.uploadFilesMax) {
      this.#notificationService.addDangerNotification(this.#transloco.translate('ui.validation-error.max-files'));
      return;
    }
    this.notizDetailStore.updateNotiz({
      notiz: payload.notiz,
      fileUploads: payload.fileUploads ?? [],
      onSuccess: (_) => {
        this.afterSuccessfulSaveAndUpload();
      },
    });
  }

  onRemoveNotizFile(params: NotizenFileParams) {
    this.notizDetailStore.removeNotizFile(params);
  }

  private afterSuccessfulSaveAndUpload() {
    this.#notificationService.addSuccessNotification(this.#transloco.translate('notizen.action.mobile.upload.success'));
  }
}
