import {ChangeDetectionStrategy, Component, computed, inject} from '@angular/core';
import {ReactiveFormsModule} from '@angular/forms';
import {NotizJson, NotizTyp} from '@dvb/data-access-backend';
import {
  globalConstants,
  NotizenFileParams,
  NotizenMobileUploadComponent,
  NotizenSaveParams,
  PageComponent,
  Path,
} from '@dvb/feat-shared';
import {AppNotificationStore} from '@dvb/util-angular';
import {compareByEntityInfoTimestampErstellt, isNotNullish, isNullish, requireValue} from '@dvb/util-shared';
import {TranslocoPipe, TranslocoService} from '@jsverse/transloco';
import {AnlassStore} from '../../../../anlass.store';
import {AnlassKinderStore} from '../../../anlass-kinder.store';

@Component({
  selector: 'feat-einladung-notiz-hochladen.page',
  standalone: true,
  templateUrl: './einladung-notiz-hochladen.page.component.html',
  styleUrl: './einladung-notiz-hochladen.page.component.scss',
  changeDetection: ChangeDetectionStrategy.OnPush,
  imports: [
    TranslocoPipe,
    PageComponent,
    ReactiveFormsModule,
    NotizenMobileUploadComponent
  ]
})
export class EinladungNotizHochladenPageComponent {

  readonly anlassStore = inject(AnlassStore);
  readonly anlassKinderStore = inject(AnlassKinderStore);
  readonly #notificationService = inject(AppNotificationStore);
  readonly #transloco = inject(TranslocoService);

  protected readonly Path = Path;

  readonly notizTyp = computed(() => {
    const currentNotizTyp = this.anlassKinderStore.currentNotizTyp();
    if (isNullish(currentNotizTyp)) {
      console.error('currentNotizTyp must be set in anlassKinderStore')
    }
    return requireValue(currentNotizTyp);
  });

  currentNotiz = computed<NotizJson | undefined>(() => {
    return this.anlassKinderStore.selectedEinladung()?.data?.notizen.find(n => n.data.typ === this.notizTyp());
  });

  currentIsNew = computed(() => !this.currentNotiz());
  notizenFiles = computed(() => this.currentNotiz()?.data?.files.sort(compareByEntityInfoTimestampErstellt));

  typeKey = computed(() => {
    switch (this.notizTyp()) {
      case NotizTyp.IMPFAUSWEIS:
        return 'impfausweis';
      case NotizTyp.FRAGEBOGEN:
        return 'fragebogen';
      case NotizTyp.ANMELDUNG_BESTAETIGUNG:
        return 'anmeldungBestaetigung';
      default:
        return '';
    }
  });

  pageTitle = computed(() => {
    return `einladung.${ this.typeKey() }.hochladen.page.title`;
  });

  onSaveNotiz(payload: NotizenSaveParams) {
    if (isNotNullish(payload.fileUploads) && payload.fileUploads.length > globalConstants.notizen.uploadFilesMax) {
      this.#notificationService.addDangerNotification(this.#transloco.translate('ui.validation-error.max-files'));
      return;
    }

    if (this.currentIsNew()) {
      this.anlassKinderStore.createNotizWithFiles({
        notiz: payload.notiz,
        notizTyp: this.notizTyp(),
        fileUploads: payload.fileUploads ?? [],
        onSuccess: (_) => {
          this.afterSuccessfulSaveAndUpload();
        },
      });
    } else {
      this.anlassKinderStore.updateNotizWithFiles({
        notizId: requireValue(this.currentNotiz()?.id),
        notiz: payload.notiz,
        notizTyp: this.notizTyp(),
        fileUploads: payload.fileUploads ?? [],
        onSuccess: (_) => {
          this.afterSuccessfulSaveAndUpload();
        },
      });
    }
  }

  onRemoveNotizFile(params: NotizenFileParams) {
    this.anlassKinderStore.removeNotizFile({...params, notizTyp: this.notizTyp()});
  }

  private afterSuccessfulSaveAndUpload(): void {
    this.#notificationService.addSuccessNotification(this.#transloco.translate('notizen.action.mobile.upload.success'));
  }

}
