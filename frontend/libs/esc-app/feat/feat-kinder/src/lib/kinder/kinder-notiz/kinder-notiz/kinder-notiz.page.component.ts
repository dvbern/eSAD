import {ChangeDetectionStrategy, Component, computed, inject, ViewChild} from '@angular/core';
import {ReactiveFormsModule} from '@angular/forms';
import {ActivatedRoute, Router} from '@angular/router';
import {LabelJson, NotizSaveJson, NotizStatus} from '@dvb/data-access-backend';
import {BackendEntityId} from '@dvb/data-access-types';
import {BenutzerOptionsStore, LabelsStore} from '@dvb/feat-shared';
import {BenutzerStore} from '@dvb/feat-shared';
import {Path} from '@dvb/feat-shared';
import {NotizenComponent, NotizenFileParams, NotizenSaveParams} from '@dvb/feat-shared';
import {PageComponent} from '@dvb/feat-shared';
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
  @ViewChild(NotizenComponent) notizenComponent!: NotizenComponent;

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

  pageTitle = computed(() => {
    if (this.kinderNotizStore.selectedIsNew()) {
      return 'notizen.detail.create.titel';
    } else {
      return 'notizen.detail.edit.titel';
    }
  });

  isDirty(): boolean {
    return this.notizenComponent.isDirty() || this.kinderNotizStore.saveLoader.isLoading();
  }

  kindId = computed<BackendEntityId | undefined>(() => this.dossierStore.selectedDossier()?.kind.id);

  canEditAllNotizen = this.#benutzerStore.createHasRequiredRoleSig(hasAllNotizenEditBerechtigung());

  defaultNotizSig = computed(() => {
      if (!this.canEditAllNotizen()) {
        // Es duerfen nur Notizen mit dem Label Dispensationsgesuch erstellt werden
        const label: LabelJson | undefined = this.#labelStore.dispensationsgesuchAutoLabel();
        const labels: LabelJson[] = label ? [label] : [];
        const currentUser = this.#benutzerOptionsStore.getOptionForBenutzer(this.#benutzerStore.benutzer()?.id);
        const defaultNotiz: NotizSaveJson = {
          titel: this.#transloco.translate('notiz.dispensationsgesuch.title'),
          bemerkungen: undefined,
          status: NotizStatus.ABGESCHLOSSEN,
          verantwortlicher: requireValue(currentUser),
          labels: labels,
          files: [],
        };
        return defaultNotiz;
      }
      return undefined;
    },
  );

  onSaveNotiz(payload: NotizenSaveParams) {
    const kindIdOrUndefined = this.kindId();
    if (!kindIdOrUndefined) {
      return;
    }
    if (isNotNullish(payload.fileUploads) && payload.fileUploads.length > 4) {
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
    this.notizenComponent.form.reset();
    this.#router.navigate(
      [Path.RELATIVE_TWO_UP],
      {relativeTo: this.#activatedRoute});
  }
}
