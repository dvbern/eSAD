import {Location} from '@angular/common';
import {ChangeDetectionStrategy, Component, computed, effect, inject, input, output, signal, untracked} from '@angular/core';
import {NonNullableFormBuilder, ReactiveFormsModule} from '@angular/forms';
import {Router} from '@angular/router';
import {
  BenutzerOptionJson,
  EntityAktivStatus,
  FileDataJson,
  LabelJson,
  NotizJson,
  NotizSaveJson,
  NotizStatus,
  NotizTyp,
} from '@dvb/data-access-backend';
import {BackendEntityId, requireValidBackendEntityId} from '@dvb/data-access-types';
import {
  BadgeComponent,
  CardComponent,
  DashboardComponent,
  FormControlComponent,
  FormErrorMessageComponent,
  FormLayoutComponent,
  HeadingDirective,
  IconComponent,
  NgSelectDirective,
  PageHeaderEntityInfoComponent,
  SaveCancelButtonsComponent,
  TextareaDirective,
  TextInputDirective,
} from '@dvb/ui-components';
import {DetailMode, DirtyMarker, Loader, TableModel} from '@dvb/util-angular';
import {compareFn, hasNotizEditBerechtigung, isNotNullish, Nullish, requireValue} from '@dvb/util-shared';
import {TranslocoPipe, TranslocoService} from '@jsverse/transloco';
import {NgbModal, NgbNav, NgbNavItem} from '@ng-bootstrap/ng-bootstrap';
import {NgOptionComponent, NgSelectComponent} from '@ng-select/ng-select';
import {BenutzerOptionsStore} from '../admin-shared/benutzer-options.store';
import {LabelsStore} from '../admin-shared/labels.store';
import {BenutzerStore} from '../benutzer/benutzer/benutzer.store';
import {globalConstants} from '../constants/global-constants';
import {Path} from '../core/path';
import {ConfirmService} from '../dialogs/confirm/confirm.service';
import {TenantSettingsStore} from '../tenant/tenant-settings.store';
import {TranslatedStringPipe} from '../translated-strings/view/translated-string.pipe';
import {FileDropComponent} from '../upload/file-drop';
import {MobileUploadComponent} from '../upload/mobile-upload/mobile-upload.component';
import {UploadTableComponent} from '../upload/upload-table';
import {FilePreviewComponent} from './file-preview/file-preview.component';
import {NotizenMobileUploadModalComponent} from './modal/notizen-mobile-upload-modal.component';
import {NotizenFileParams} from './notizen.file.params';
import {NotizenForm} from './notizen.form';
import {NotizenSaveParams} from './notizen.save.params';
import {NotizenStore} from './notizen.store';

@Component({
  selector: 'feat-notizen',
  changeDetection: ChangeDetectionStrategy.OnPush,
  imports: [
    TranslocoPipe,
    CardComponent,
    HeadingDirective,
    FormLayoutComponent,
    ReactiveFormsModule,
    FormControlComponent,
    TextareaDirective,
    FormErrorMessageComponent,
    TextInputDirective,
    NgOptionComponent,
    NgSelectComponent,
    NgSelectDirective,
    TranslatedStringPipe,
    UploadTableComponent,
    DashboardComponent,
    FileDropComponent,
    PageHeaderEntityInfoComponent,
    SaveCancelButtonsComponent,
    BadgeComponent,
    IconComponent,
    NgbNav,
    NgbNavItem,
    MobileUploadComponent,
    FilePreviewComponent,
  ],
  templateUrl: './notizen.component.html',
  styleUrl: './notizen.component.scss',
})
export class NotizenComponent implements DirtyMarker {
  protected readonly Path = Path;
  protected readonly DetailMode = DetailMode;
  protected readonly EntityAktivStatus = EntityAktivStatus;
  protected readonly compareFn = compareFn<LabelJson>;
  protected readonly compareFnBenutzer = compareFn<BenutzerOptionJson>;

  readonly store = inject(NotizenStore);
  readonly benutzerOptionsStore = inject(BenutzerOptionsStore);
  readonly benutzerStore = inject(BenutzerStore);
  readonly labelsStore = inject(LabelsStore);
  readonly tenantSettingsStore = inject(TenantSettingsStore);
  readonly #ngModal = inject(NgbModal);

  readonly confirmService = inject(ConfirmService);
  readonly #transloco = inject(TranslocoService);
  readonly location = inject(Location);
  readonly router = inject(Router);

  kindId = input<BackendEntityId>();
  initialNotiz = input<NotizSaveJson>();
  existingNotiz = input<NotizJson | Nullish>();
  notizenFiles = input.required<FileDataJson[]>();
  loader = input.required<Loader>();
  notizTyp = input<NotizTyp | undefined>();

  mode = computed(() => {
    if (isNotNullish(this.existingNotiz()) && isNotNullish(this.existingNotiz()?.id)) {
      return 'edit';
    }
    return 'create';
  });

  saveNotiz = output<NotizenSaveParams>();
  saveNotizSilent = output<NotizenSaveParams>();
  removeNotizFile = output<NotizenFileParams>();
  mobileUpload = output<string>();

  fileUploads: File[] | undefined;

  // Keep this list in sync with the accepted types in backend as we do not sync config to client yet
  readonly acceptedFiletypes = globalConstants.acceptedFileTypes;

  entityInfo = computed(() => this.existingNotiz()?.entityInfo);
  inaktivLabels = computed(() => this.existingNotiz()?.data?.labels?.filter(label => !label.data.aktiv) ?? []);

  readonly NotizTyp = NotizTyp;

  notizStatus = Object.values(NotizStatus);

  canEditNotiz = this.benutzerStore.createHasRequiredRoleSig(hasNotizEditBerechtigung());
  readonly #fileIdSelectedForPreview = signal<string | null>(null);

  qrData = '';

  private setQr(notizId: string) {
    let baseUrl = this.router.url;

    if (baseUrl.toLowerCase().includes('fragebogen') || baseUrl.toLowerCase().includes('impfausweis')) {
      this.qrData = `${baseUrl}/${Path.UPLOAD}`;
      return;
    }

    if (this.mode() === 'create') {
      baseUrl = baseUrl.replace(/\/create$/, '');
      this.qrData = `${baseUrl}/${notizId}/${Path.UPLOAD}`;
      return;
    }

    this.qrData = `${baseUrl}/${Path.UPLOAD}`;
  }

  uploadTable = new TableModel<FileDataJson>(
    { rawListSignal: this.notizenFiles },
    { frontendOrBackend: 'frontend', pageOrScrollOrNothing: 'scroll' },
  );

  form = new NotizenForm({
    fb: inject(NonNullableFormBuilder),
    currentBenutzer: this.benutzerOptionsStore.getOptionForBenutzer(this.benutzerStore.benutzer()?.id),
  });

  anlassLink = computed(() => {
    const notiz = this.existingNotiz();
    return `${notiz?.data.anlassLink}/${Path.ANLASS_PLANUNG}`;
  });

  constructor() {
    effect(() => {
      const selectedEntity = this.existingNotiz()?.data;
      const initialNotiz = this.initialNotiz();
      const mode = DetailMode.EDIT;
      untracked(() => {
        const resetTo = selectedEntity ?? initialNotiz;
        if (resetTo) {
          // We need to create a copy as to not filter away the disabled labels everywhere.
          const copy = { ...resetTo };
          // Take out the disabled labels as to not show up in the control
          copy.labels = copy.labels?.filter(label => label.data.aktiv);
          this.form.reset(copy, mode);
        }
      });
    });

    effect(() => {
      const id = this.existingNotiz()?.id;
      if (id) {
        this.setQr(id);
      }
    });
  }

  async submitIfValid(): Promise<void> {
    if (await this.form.validate()) {
      const payload = this.form.valueOnValid();
      // Add the disabled labels back into the payload to satisfy backend validations.
      payload.labels = [...payload.labels, ...this.inaktivLabels()];
      this.saveNotiz.emit({
        notiz: requireValue(payload),
        fileUploads: this.fileUploads,
      });
      this.fileUploads = undefined;
    }
  }

  async submitIfValidSilent(afterSave?: (notizenId: string) => void): Promise<void> {
    if (await this.form.validate()) {
      const payload = this.form.valueOnValid();
      // Add the disabled labels back into the payload to satisfy backend validations.
      payload.labels = [...payload.labels, ...this.inaktivLabels()];
      this.saveNotizSilent.emit({
        notiz: requireValue(payload),
        fileUploads: this.fileUploads,
        afterSave,
      });
      this.fileUploads = undefined;

      this.form.group.markAsPristine();
      this.form.group.markAsUntouched();
    }
  }

  previewFile = computed<FileDataJson | null | undefined>(() => {
    const files = this.notizenFiles();
    if (files.length === 0) {
      return null;
    }
    const id = this.#fileIdSelectedForPreview();
    return files.find(f => f.id === id) ?? files.at(0);
  });

  public isDirty(): boolean {
    return this.form.isDirty() || this.fileUploads !== undefined;
  }

  public onFilesSelected($event: File[]): void {
    this.fileUploads = $event;
  }

  public onFileDelete($event: FileDataJson): void {
    const notizId = requireValidBackendEntityId(this.existingNotiz()?.id);
    this.confirmService
      .confirm({
        question: this.#transloco.translate('ui.upload.table.action.delete.confirm.question', {
          filename: $event.calculatedFileName,
        }),
        consequence: this.#transloco.translate('ui.upload.table.action.delete.confirm.consequence'),
        decision: this.#transloco.translate('ui.upload.table.action.delete.decision'),
      })
      .subscribe(() => {
        this.removeNotizFile.emit({
          notizId: notizId,
          fileId: $event.id,
        });
      });
  }

  public onFileView($event: FileDataJson): void {
    const notizId = requireValidBackendEntityId(this.existingNotiz()?.id);
    this.store.downloadNotizFile({
      notizId: notizId,
      fileId: $event.id,
    });
  }
  public onFilePreview($event: FileDataJson): void {
    this.#fileIdSelectedForPreview.set($event.id);
  }

  public onFileDirectDownload($event: FileDataJson): void {
    const notizId = requireValidBackendEntityId(this.existingNotiz()?.id);
    this.store.downloadNotizFile({
      notizId: notizId,
      fileId: $event.id,
      downloadDirectly: true,
    });
  }

  public onCancelClicked(): void {
    this.location.back();
  }

  public async onMobileClick(): Promise<void> {
    // 1. Validate before starting the async flow to prevent hanging
    if (!(await this.form.validate())) {
      return;
    }

    try {
      // 2. Wrap the save in a promise that handles the callback
      const notizId = await new Promise<string>((resolve, reject) => {
        this.submitIfValidSilent((id: string) => {
          if (id) {
            resolve(id);
          } else {
            reject(new Error('No ID returned from save'));
          }
        });
      });

      this.setQr(notizId);

      const ngbModalRef = this.#ngModal.open(NotizenMobileUploadModalComponent, { size: 'm' });
      ngbModalRef.componentInstance.notizTyp.set(this.notizTyp());
      ngbModalRef.componentInstance.qrCodeData.set(this.qrData);

      ngbModalRef.result
        .catch(() => {
          /* empty since we always just trigger a reload*/
        })
        .finally(() => {
          this.mobileUpload.emit(notizId);
        });
    } catch (error) {
      console.error('Failed to prepare mobile upload:', error);
    }
  }
}
