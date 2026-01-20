import {Location} from '@angular/common';
import {ChangeDetectionStrategy, Component, computed, inject, input, output, ViewChild} from '@angular/core';
import {NonNullableFormBuilder, ReactiveFormsModule} from '@angular/forms';
import {ActivatedRoute} from '@angular/router';
import {FileDataJson, NotizJson} from '@dvb/data-access-backend';
import {requireValidBackendEntityId} from '@dvb/data-access-types';
import {
  CardComponent,
  FormControlComponent,
  FormErrorMessageComponent,
  FormLayoutComponent,
  HeadingDirective,
  SaveCancelButtonsComponent,
} from '@dvb/ui-components';
import {DetailMode, Loader, TableModel} from '@dvb/util-angular';
import {hasNotizEditBerechtigung, Nullish, requireValue} from '@dvb/util-shared';
import {TranslocoPipe, TranslocoService} from '@jsverse/transloco';
import {BenutzerOptionsStore} from '../../admin-shared/benutzer-options.store';
import {BenutzerStore} from '../../benutzer/benutzer/benutzer.store';
import {globalConstants} from '../../constants/global-constants';
import {ConfirmService} from '../../dialogs/confirm/confirm.service';
import {FileDropComponent} from '../../upload/file-drop/file-drop.component';
import {UploadTableComponent} from '../../upload/upload-table/upload-table.component';
import {NotizenFileParams} from '../notizen.file.params';
import {NotizenForm} from '../notizen.form';
import {NotizenSaveParams} from '../notizen.save.params';
import {NotizenStore} from '../notizen.store';

@Component({
  selector: 'feat-notizen-mobile-upload',
  imports: [
    CardComponent,
    FormLayoutComponent,
    HeadingDirective,
    ReactiveFormsModule,
    TranslocoPipe,
    FormControlComponent,
    FileDropComponent,
    FormErrorMessageComponent,
    SaveCancelButtonsComponent,
    UploadTableComponent,
  ],
  templateUrl: './notizen-mobile-upload.component.html',
  styles: ':host { display:block;}',
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class NotizenMobileUploadComponent {
  @ViewChild(FileDropComponent) fileDrop!: FileDropComponent;

  protected readonly DetailMode = DetailMode;

  readonly store = inject(NotizenStore);
  readonly benutzerOptionsStore = inject(BenutzerOptionsStore);
  readonly benutzerStore = inject(BenutzerStore);

  readonly confirmService = inject(ConfirmService);
  readonly #transloco = inject(TranslocoService);
  readonly location = inject(Location);
  readonly route = inject(ActivatedRoute);

  existingNotiz = input.required<NotizJson | Nullish>();
  notizenFiles = input.required<FileDataJson[]>();
  loader = input.required<Loader>();

  saveNotiz = output<NotizenSaveParams>();
  removeNotizFile = output<NotizenFileParams>();

  notizTitel = computed(() => this.existingNotiz()?.data.titel);
  fileUploads: File[] | undefined;

  readonly acceptedFiletypes = globalConstants.acceptedFileTypes;

  canEditNotiz = this.benutzerStore.createHasRequiredRoleSig(hasNotizEditBerechtigung());

  uploadTable = new TableModel<FileDataJson>(
    {rawListSignal: this.notizenFiles},
    {frontendOrBackend: 'frontend', pageOrScrollOrNothing: 'scroll'},
  );

  form = new NotizenForm({
    fb: inject(NonNullableFormBuilder),
    currentBenutzer: this.benutzerOptionsStore.getOptionForBenutzer(this.benutzerStore.benutzer()?.id),
  });

  async submitIfValid(): Promise<void> {

    this.saveNotiz.emit({
      notiz: requireValue(this.existingNotiz()).data,
      fileUploads: this.fileUploads,
    });
    this.fileUploads = undefined;
    this.fileDrop?.clear();

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
}
