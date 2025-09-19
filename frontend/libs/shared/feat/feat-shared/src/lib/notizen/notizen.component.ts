import {CommonModule, Location} from '@angular/common';
import {ChangeDetectionStrategy, Component, computed, effect, inject, input, output, untracked} from '@angular/core';
import {NonNullableFormBuilder, ReactiveFormsModule} from '@angular/forms';
import {
  BenutzerOptionJson,
  FileDataJson,
  LabelJson,
  NotizJson,
  NotizSaveJson,
  NotizStatus,
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
  NgSelectDirective,
  PageHeaderEntityInfoComponent,
  SaveCancelButtonsComponent,
  TextareaDirective,
  TextInputDirective,
} from '@dvb/ui-components';
import {DetailMode, DirtyMarker, Loader, TableModel} from '@dvb/util-angular';
import {compareFn, hasNotizEditBerechtigung, Nullish, requireValue} from '@dvb/util-shared';
import {TranslocoPipe, TranslocoService} from '@jsverse/transloco';
import {NgOptionComponent, NgSelectComponent} from '@ng-select/ng-select';
import {BenutzerOptionsStore} from '../admin-shared/benutzer-options.store';
import {LabelsStore} from '../admin-shared/labels.store';
import {BenutzerStore} from '../benutzer/benutzer/benutzer.store';
import {globalConstants} from '../constants/global-constants';
import {Path} from '../core/path';
import {ConfirmService} from '../dialogs/confirm/confirm.service';
import {TranslatedStringPipe} from '../translated-strings/view/translated-string.pipe';
import {FileDropComponent} from '../upload/file-drop/file-drop.component';
import {UploadTableComponent} from '../upload/upload-table/upload-table.component';
import {NotizenFileParams} from './notizen.file.params';
import {NotizenForm} from './notizen.form';
import {NotizenSaveParams} from './notizen.save.params';
import {NotizenStore} from './notizen.store';

@Component({
  selector: 'feat-notizen',
  changeDetection: ChangeDetectionStrategy.OnPush,
  imports: [
    CommonModule,
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
  ],
  templateUrl: './notizen.component.html',
  styles: ':host { display:block;}',
})
export class NotizenComponent implements DirtyMarker {

  protected readonly Path = Path;
  protected readonly DetailMode = DetailMode;
  protected readonly compareFn = compareFn<LabelJson>;
  protected readonly compareFnBenutzer = compareFn<BenutzerOptionJson>;

  readonly store = inject(NotizenStore);
  readonly benutzerOptionsStore = inject(BenutzerOptionsStore);
  readonly benutzerStore = inject(BenutzerStore);
  readonly labelsStore = inject(LabelsStore);

  readonly confirmService = inject(ConfirmService);
  readonly #transloco = inject(TranslocoService);
  readonly location = inject(Location);

  kindId = input<BackendEntityId>();
  initialNotiz = input<NotizSaveJson>();
  existingNotiz = input<NotizJson | Nullish>();
  notizenFiles = input.required<FileDataJson[]>();
  loader = input.required<Loader>();

  saveNotiz = output<NotizenSaveParams>();
  removeNotizFile = output<NotizenFileParams>();

  fileUploads: File[] | undefined;

  // Keep this list in sync with the accepted types in backend as we do not sync config to client yet
  readonly acceptedFiletypes = globalConstants.acceptedFileTypes;

  entityInfo = computed(() => this.existingNotiz()?.entityInfo);
  inaktivLabels = computed(() => this.existingNotiz()?.data?.labels?.filter(label => !label.data.aktiv) ?? []);

  notizStatus = Object.values(NotizStatus);

  canEditNotiz = this.benutzerStore.createHasRequiredRoleSig(hasNotizEditBerechtigung());

  uploadTable = new TableModel<FileDataJson>(
    {rawListSignal: this.notizenFiles},
    {frontendOrBackend: 'frontend', pageOrScrollOrNothing: 'scroll'});

  form = new NotizenForm({
    fb: inject(NonNullableFormBuilder),
    currentBenutzer: this.benutzerOptionsStore.getOptionForBenutzer(this.benutzerStore.benutzer()?.id),
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
          const copy = {...resetTo};
          // Take out the disabled labels as to not show up in the control
          copy.labels = copy.labels?.filter(label => label.data.aktiv);
          this.form.reset(copy, mode);
        }
      });
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

  public isDirty(): boolean {
    return this.form.isDirty() || this.fileUploads !== undefined;
  }

  public onFilesSelected($event: File[]): void {
    this.fileUploads = $event;
  }

  public onFileDelete($event: FileDataJson): void {
    const notizId = requireValidBackendEntityId(this.existingNotiz()?.id);
    this.confirmService.confirm({
      question: this.#transloco.translate('ui.upload.table.action.delete.confirm.question',
        {filename: $event.calculatedFileName}),
      consequence: this.#transloco.translate('ui.upload.table.action.delete.confirm.consequence'),
      decision: this.#transloco.translate('ui.upload.table.action.delete.decision'),
    }).subscribe(() => {
      this.removeNotizFile.emit({
        kindId: requireValue(this.kindId()),
        notizId: notizId,
        fileId: $event.id,
      });
    });
  }

  public onFileView($event: FileDataJson): void {
    const notizId = requireValidBackendEntityId(this.existingNotiz()?.id);
    this.store.downloadNotizFile({
      kindId: requireValue(this.kindId()),
      notizId: notizId,
      fileId: $event.id,
    });
  }

  public onFileDirectDownload($event: FileDataJson): void {
    const notizId = requireValidBackendEntityId(this.existingNotiz()?.id);
    this.store.downloadNotizFile({
      kindId: requireValue(this.kindId()),
      notizId: notizId,
      fileId: $event.id,
      downloadDirectly: true,
    });
  }

  public onCancelClicked(): void {
    this.location.back();
  }
}
