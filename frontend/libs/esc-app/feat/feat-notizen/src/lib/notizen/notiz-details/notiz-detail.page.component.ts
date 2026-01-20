import {ChangeDetectionStrategy, Component, computed, inject, viewChild} from '@angular/core';
import {ReactiveFormsModule} from '@angular/forms';
import {ActivatedRoute, Router} from '@angular/router';
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
  readonly notizenComponent = viewChild(NotizenComponent);

  readonly notizenListStore = inject(NotizenListStore);
  readonly dossierStore = inject(DossierStore);

  readonly #transloco = inject(TranslocoService);
  readonly #router = inject(Router);
  readonly #activatedRoute = inject(ActivatedRoute);
  readonly #notificationService = inject(AppNotificationStore);

  protected readonly Path = Path;
  protected readonly requireValue = requireValue;

  kind = computed(() => this.notizenListStore.selectedEntity()?.kind);

  isDirty(): boolean {
    return this.notizenComponent()?.isDirty() || this.notizenListStore.saveLoader.isLoading();
  }

  onSaveNotiz(payload: NotizenSaveParams) {
    this.saveNotiz(payload, () => {
      this.afterSuccessfulSaveAndUpload();
    });
  }

  onSaveNotizSilent(payload: NotizenSaveParams) {
    this.saveNotiz(payload, (updatedId: string) => {
      payload.afterSave?.(updatedId);
    });
  }

  saveNotiz(payload: NotizenSaveParams, onSuccess: (resultId: string) => void): void {
    if (isNotNullish(payload.fileUploads) && payload.fileUploads.length > globalConstants.notizen.uploadFilesMax) {
      this.#notificationService.addDangerNotification(this.#transloco.translate('ui.validation-error.max-files'));
      return;
    }
    this.notizenListStore.updateNotiz({
      notiz: payload.notiz,
      fileUploads: payload.fileUploads ?? [],
      onSuccess: (result) => onSuccess(result.id),
    });
  }

  onRemoveNotizFile(params: NotizenFileParams) {
    this.notizenListStore.removeNotizFile(params);
  }

  private afterSuccessfulSaveAndUpload() {
    this.notizenComponent()?.form.reset();
    this.#router.navigate(
      [Path.RELATIVE_ONE_UP],
      {relativeTo: this.#activatedRoute});
  }
}
