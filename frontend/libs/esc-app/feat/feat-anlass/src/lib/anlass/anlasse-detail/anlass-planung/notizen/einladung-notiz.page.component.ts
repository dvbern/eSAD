import {Location} from '@angular/common'
import {ChangeDetectionStrategy, Component, computed, inject, ViewChild} from '@angular/core';
import {ReactiveFormsModule} from '@angular/forms';
import {LabelJson, NotizJson, NotizSaveJson, NotizStatus, NotizTyp} from '@dvb/data-access-backend';
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
import {compareByEntityInfoTimestampErstellt, isNotNullish, isNullish, requireValue} from '@dvb/util-shared';
import {TranslocoPipe, TranslocoService} from '@jsverse/transloco';
import {AnlassStore} from '../../../anlass.store';
import {anlassTitleOptional} from '../../../anlass.util';
import {AnlassKinderStore} from '../../anlass-kinder.store';

@Component({
  selector: 'feat-anlass.einladung-notiz.page',
  templateUrl: './einladung-notiz.page.component.html',
  styleUrl: './einladung-notiz.page.component.scss',
  changeDetection: ChangeDetectionStrategy.OnPush,
  imports: [
    TranslocoPipe,
    PageComponent,
    ReactiveFormsModule,
    NotizenComponent,
  ],
})
export class EinladungNotizPageComponent implements DirtyMarker {
  @ViewChild(NotizenComponent) notizenComponent!: NotizenComponent;

  readonly anlassStore = inject(AnlassStore);
  readonly anlassKinderStore = inject(AnlassKinderStore);
  readonly benutzerStore = inject(BenutzerStore);
  readonly #benutzerOptionsStore = inject(BenutzerOptionsStore);
  readonly #transloco = inject(TranslocoService);
  readonly labelStore = inject(LabelsStore);
  readonly #notificationService = inject(AppNotificationStore);

  readonly location = inject(Location);

  protected readonly Path = Path;
  protected readonly requireValue = requireValue;

  readonly notizTyp = computed(() => {
    const state = this.location.getState() as { notizTyp: NotizTyp };
    return state.notizTyp;
  });

  kindId = computed<BackendEntityId | undefined>(() => this.anlassKinderStore.selectedEinladung()?.kind.id);

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
    const mode = this.currentIsNew() ? 'create' : 'edit';
    return `einladung.${this.typeKey()}.page.${mode}.title`;
  });

  defaultNotizTitleSig = computed(() => {
    return this.#transloco.translate(`einladung.${this.typeKey()}.notiz.title`,
      {anlassname: anlassTitleOptional(this.anlassStore.selectedEntity()?.data, this.#transloco)});
  });

  defaultNotizLabelSig = computed(() => {
    const defaultLabel = this.getDefaultLabel(this.notizTyp());
    return isNotNullish(defaultLabel) ? [defaultLabel] : [];
  });

  private getDefaultLabel(notizTyp: NotizTyp): LabelJson | undefined {
    switch (notizTyp) {
      case NotizTyp.IMPFAUSWEIS:
        return this.labelStore.impfausweisAutoLabel();
      case NotizTyp.FRAGEBOGEN:
        return this.labelStore.fragebogenAutoLabel();
      case NotizTyp.ANMELDUNG_BESTAETIGUNG:
        return this.labelStore.anmeldungBestaetigungAutoLabel();
      default:
        return undefined;
    }
  }

  defaultNotizSig = computed(() => {
      const titel = this.defaultNotizTitleSig();
      const labels = this.defaultNotizLabelSig();
      const currentUser = this.#benutzerOptionsStore.getOptionForBenutzer(this.benutzerStore.benutzer()?.id);
      if (isNullish(currentUser)) {
        return undefined;
      }
      const defaultNotiz: NotizSaveJson = {
        titel: titel,
        bemerkungen: undefined,
        status: NotizStatus.ABGESCHLOSSEN,
        typ: this.notizTyp(),
        verantwortlicher: requireValue(currentUser),
        labels: labels,
        files: [],
        version: globalConstants.entityPrePersistVersion,
      };
      return defaultNotiz;
    },
  );

  public isDirty(): boolean {
    return this.notizenComponent.isDirty();
  }

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
    this.location.back();
  }
}
