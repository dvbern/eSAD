import {ChangeDetectionStrategy, Component, computed, inject, viewChild} from '@angular/core';
import {ReactiveFormsModule} from '@angular/forms';
import {DossierStore} from '@dvb/feat-kinder';
import {
  globalConstants,
  NotizenComponent,
  NotizenFileParams,
  NotizenSaveParams,
  PageComponent,
  Path,
} from '@dvb/feat-shared';
import {AppNotificationStore, DirtyMarker} from '@dvb/util-angular';
import {isNotNullish, requireValue} from '@dvb/util-shared';
import {TranslocoPipe, TranslocoService} from '@jsverse/transloco';
import {NotizDetailStore} from '../notiz-detail.store';
import {NotizenListStore} from '../notizen-list.store';

@Component({
  selector: 'feat-notiz-detail.page',
  templateUrl: './notiz-detail.page.component.html',
  changeDetection: ChangeDetectionStrategy.OnPush,
  imports: [
    TranslocoPipe,
    PageComponent,
    ReactiveFormsModule,
    NotizenComponent,
  ],
})
export class NotizDetailPageComponent implements DirtyMarker {
  readonly notizenComponent = viewChild.required(NotizenComponent);

  readonly notizDetailStore = inject(NotizDetailStore);
  readonly notizenListStore = inject(NotizenListStore);
  readonly dossierStore = inject(DossierStore);

  readonly #transloco = inject(TranslocoService);
  readonly #notificationService = inject(AppNotificationStore);

  protected readonly Path = Path;
  protected readonly requireValue = requireValue;

  kind = computed(() => this.notizDetailStore.kind());

  isDirty(): boolean {
    return this.notizenComponent().isDirty() || this.notizDetailStore.saveLoader.isLoading();
  }

  onSaveNotiz(payload: NotizenSaveParams) {
    this.saveNotiz(payload, () => {
      this.afterSuccessfulSaveAndUpload();
    });
  }

  onSaveNotizSilent(payload: NotizenSaveParams) {
    this.saveNotiz(payload, (updatedId: string) => {
      this.notizenComponent().resetAfterSuccessfulSave();
      payload.afterSave?.(updatedId);
    });
  }

  saveNotiz(payload: NotizenSaveParams, onSuccess: (resultId: string) => void): void {
    if (isNotNullish(payload.fileUploads) && payload.fileUploads.length > globalConstants.notizen.uploadFilesMax) {
      this.#notificationService.addDangerNotification(this.#transloco.translate('ui.validation-error.max-files'));
      return;
    }
    this.notizDetailStore.updateNotiz({
      notiz: payload.notiz,
      fileUploads: payload.fileUploads ?? [],
      onSuccess: (result) => onSuccess(result.id),
    });
  }

  onRemoveNotizFile(params: NotizenFileParams) {
    this.notizDetailStore.removeNotizFile(params);
  }

  private afterSuccessfulSaveAndUpload() {
    this.notizenComponent().resetAfterSuccessfulSave();
    this.#notificationService.addSuccessNotification(this.#transloco.translate('global.data.saved'));
  }

  public onMobileUpload(_: string) {
    this.notizenListStore.loadNotizen();
  }
}
