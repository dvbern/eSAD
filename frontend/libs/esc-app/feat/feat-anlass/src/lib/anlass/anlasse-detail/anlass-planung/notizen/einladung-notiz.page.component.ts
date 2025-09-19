import {Location} from '@angular/common'
import {ChangeDetectionStrategy, Component, computed, inject, input, ViewChild} from '@angular/core';
import {ReactiveFormsModule} from '@angular/forms';
import {NotizJson, NotizSaveJson, NotizStatus} from '@dvb/data-access-backend';
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
import {isNotNullish, isNullish, requireValue} from '@dvb/util-shared';
import {TranslocoPipe, TranslocoService} from '@jsverse/transloco';
import {AnlassStore} from '../../../anlass.store';
import {anlassTitleOptional} from '../../../anlass.util';
import {AnlassKinderStore} from '../../anlass-kinder.store';

export type EinladungNotizTyp = 'impfausweis' | 'fragebogen';

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

  readonly notizTyp = input.required<EinladungNotizTyp>();

  kindId = computed<BackendEntityId | undefined>(() => this.anlassKinderStore.selectedEinladung()?.kind.id);
  currentNotiz = computed<NotizJson | undefined>(() => {
    if (this.notizTyp() === 'impfausweis') {
      return this.anlassKinderStore.selectedEinladung()?.data?.impfausweis;
    } else {
      return this.anlassKinderStore.selectedEinladung()?.data?.fragebogen;
    }
  });
  currentIsNew = computed(() => !this.currentNotiz());
  notizenFiles = computed(() => this.currentNotiz()
    ?.data
    ?.files
    .sort((a, b) => b.entityInfo.timestampErstellt.localeCompare(a.entityInfo.timestampErstellt)));

  pageTitle = computed(() => {
    if (this.notizTyp() === 'impfausweis') {
      if (this.currentIsNew()) {
        return 'einladung.impfausweis.page.create.title';
      } else {
        return 'einladung.impfausweis.page.edit.title';
      }
    } else {
      if (this.currentIsNew()) {
        return 'einladung.fragebogen.page.create.title';
      } else {
        return 'einladung.fragebogen.page.edit.title';
      }
    }
  });

  defaultNotizTitleSig = computed(() => {
    const titelKey = this.notizTyp() === 'impfausweis'
      ? 'einladung.impfausweis.notiz.title'
      : 'einladung.fragebogen.notiz.title';
    return this.#transloco.translate(titelKey,
      {anlassname: anlassTitleOptional(this.anlassStore.selectedEntity()?.data, this.#transloco)});
  });

  defaultNotizLabelSig = computed(() => {
    const defaultLabel = this.notizTyp() === 'impfausweis'
      ? this.labelStore.impfausweisAutoLabel()
      : this.labelStore.fragebogenAutoLabel();
    return isNotNullish(defaultLabel) ? [defaultLabel] : [];
  });

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
    if (isNotNullish(payload.fileUploads) && payload.fileUploads.length > 4) {
      this.#notificationService.addDangerNotification(this.#transloco.translate('ui.validation-error.max-files'));
      return;
    }

    const kindId: BackendEntityId = requireValue(this.kindId());
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
        kindId: kindId,
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
