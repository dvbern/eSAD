import {CommonModule} from '@angular/common';
import {ChangeDetectionStrategy, Component, computed, effect, inject, input, output, signal} from '@angular/core';
import {NonNullableFormBuilder, ReactiveFormsModule} from '@angular/forms';
import {
  AufgabeJson,
  AufgabeStatus,
  BenutzerOptionJson,
  FileDataJson,
  KindJson,
  LabelJson,
} from '@dvb/data-access-backend';
import {requireValidBackendEntityId} from '@dvb/data-access-types';
import { AufgabenStore } from '../../admin-shared/aufgaben.store';
import { BenutzerOptionsStore } from '../../admin-shared/benutzer-options.store';
import { LabelsStore } from '../../admin-shared/labels.store';
import { BenutzerStore } from '../../benutzer/benutzer/benutzer.store';
import { NavigationService } from '../../core/navigation.service';
import { Path } from '../../core/path';
import { ConfirmService } from '../../dialogs/confirm/confirm.service';
import { DeploymentStore } from '../../deployment/deployment.store';
import { KindSearchComponent } from '../../kind-search/kind-search.component';
import { TranslatedStringPipe } from '../../translated-strings/view/translated-string.pipe';
import { FileDropComponent } from '../../upload/file-drop/file-drop.component';
import { UploadTableComponent } from '../../upload/upload-table/upload-table.component';
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
import {DetailMode, DirtyMarker, TableModel} from '@dvb/util-angular';
import {compareFn, hasAufgabenEditBerechtigungen, isNotNullish} from '@dvb/util-shared';
import {TranslocoPipe, TranslocoService} from '@jsverse/transloco';
import {NgbNav, NgbNavItem} from '@ng-bootstrap/ng-bootstrap';
import {NgOptionComponent, NgSelectComponent} from '@ng-select/ng-select';
import {AufgabeDetailsForm} from './aufgabe-details.form';

@Component({
  selector: 'feat-shared-aufgabe-details',
  changeDetection: ChangeDetectionStrategy.OnPush,
  imports: [
    CommonModule,
    TranslocoPipe,
    CardComponent,
    DashboardComponent,
    FileDropComponent,
    FormControlComponent,
    FormErrorMessageComponent,
    FormLayoutComponent,
    HeadingDirective,
    NgOptionComponent,
    NgSelectComponent,
    NgSelectDirective,
    ReactiveFormsModule,
    SaveCancelButtonsComponent,
    TextInputDirective,
    TextareaDirective,
    TranslatedStringPipe,
    UploadTableComponent,
    PageHeaderEntityInfoComponent,
    KindSearchComponent,
    BadgeComponent,
    IconComponent,
    NgbNavItem,
    NgbNav,
  ],
  templateUrl: './aufgabe-details.component.html',
  styles: `
    :host {
      display: block;
    }
  `,
})
export class AufgabeDetailsComponent implements DirtyMarker {

  readonly defaultKind = input<KindJson>();

  readonly saveAction = output();
  readonly cancelAction = output();

  readonly store = inject(AufgabenStore);
  readonly benutzerOptionsStore = inject(BenutzerOptionsStore);
  readonly labelsStore = inject(LabelsStore);
  readonly benutzerStore = inject(BenutzerStore);

  readonly confirmService = inject(ConfirmService);
  readonly transloco = inject(TranslocoService);
  readonly navigationService = inject(NavigationService);

  form = new AufgabeDetailsForm({
    fb: inject(NonNullableFormBuilder),
    deploymentStore: inject(DeploymentStore),
  });
  protected readonly Path = Path;
  protected readonly DetailMode = DetailMode;

  aufgabeStatus = Object.values(AufgabeStatus);

  canEditAufgabe = this.benutzerStore.createHasRequiredRoleSig(hasAufgabenEditBerechtigungen());

  protected readonly compareBenutzer = compareFn<BenutzerOptionJson>;
  protected readonly compareLabel = compareFn<LabelJson>;

  fileUploads: File[] | undefined;
  // keep in sync with backend setting as we do not sync config to client yet
  readonly acceptedFiletypes = signal(
    '.pdf, .doc, .docx, .xls, .xlsx, .ppt, .pptx, .eml, .msg, .mbox, .jpg, .jpeg, .png, .heic, .webp');

  entityInfo = computed(() => this.store.selectedEntity()?.entityInfo);
  aufgabenFiles = computed(() => this.store.selectedEntity()?.data.files ?? []);
  inaktivLabels = computed(() =>
    this.store.selectedEntity()?.data?.labels?.filter(label => !label.data.aktiv) ?? []);

  uploadTable = new TableModel<FileDataJson>(
    { rawListSignal: this.aufgabenFiles },
    { frontendOrBackend: 'frontend', pageOrScrollOrNothing: 'scroll' });

  constructor() {
    effect(() => {
      const user = this.benutzerOptionsStore.getOptionForBenutzer(this.benutzerStore.benutzer()?.id);
      if (user && !this.store.selectedEntity()) {
        this.form.setDefaultBenutzer(user);
      }
    });
    effect(() => {
      const selectedAufgabe = this.store.selectedEntity();
      if (selectedAufgabe) {
        // We need to create a copy as to not filter away the disabled labels everywhere.
        const copy = { ...selectedAufgabe.data };
        // Take out the disabled labels as to not show up in the control
        copy.labels = copy.labels?.filter(label => label.data.aktiv);
        this.form.reset(copy, DetailMode.EDIT);
      }
    });
    effect(() => {
      const kind = this.defaultKind();
      if (kind && !this.store.selectedEntity()) {
        this.form.setDefaultKind(kind);
      }
    });
  }

  public isDirty(): boolean {
    return this.form.isDirty() || this.fileUploads !== undefined;
  }

  async submitIfValid(): Promise<void> {
    if (await this.form.validate()) {
      const aufgabeSaveJson = this.form.valueOnValid();
      aufgabeSaveJson.labels = [...aufgabeSaveJson.labels, ...this.inaktivLabels()];
      if (!this.store.selectedEntity()) {
        this.store.createAufgabe({
          aufgabeSaveJson,
          onSuccess: (aufgabe: AufgabeJson) => {
            if (isNotNullish(this.fileUploads) && this.fileUploads.length > 0) {
              this.store.uploadAufgabeFiles({
                aufgabeId: aufgabe.id,
                fileUploads: this.fileUploads,
                onSuccess: () => this.afterSuccessfulSaveAndUpload(),
              });
            } else {
              this.afterSuccessfulSaveAndUpload();
            }
          },
        });
      } else {
        this.store.updateAufgabe({
          aufgabeSaveJson,
          onSuccess: (aufgabe: AufgabeJson) => {
            if (isNotNullish(this.fileUploads) && this.fileUploads.length > 0) {
              this.store.uploadAufgabeFiles({
                aufgabeId: aufgabe.id,
                fileUploads: this.fileUploads,
                onSuccess: () => this.afterSuccessfulSaveAndUpload(),
              });
            } else {
              this.afterSuccessfulSaveAndUpload();
            }
          },
        });
      }
    }
  }

  public onFilesSelected($event: File[]): void {
    this.fileUploads = $event;
  }

  public onFileDelete($event: FileDataJson): void {
    const aufgabeId = requireValidBackendEntityId(this.store.selectedEntity()?.id);
    this.confirmService.confirm({
      question: this.transloco.translate('ui.upload.table.action.delete.confirm.question',
        { filename: $event.calculatedFileName }),
      consequence: this.transloco.translate('ui.upload.table.action.delete.confirm.consequence'),
      decision: this.transloco.translate('ui.upload.table.action.delete.decision'),
    }).subscribe(() => {
      this.store.removeAufgabeFile({
        aufgabeId: aufgabeId,
        fileId: $event.id,
      });
    });
  }

  public onFileView($event: FileDataJson): void {
    const aufgabeId = requireValidBackendEntityId(this.store.selectedEntity()?.id);
    this.store.downloadAufgabeFile({
      aufgabeId: aufgabeId,
      fileId: $event.id,
    });
  }

  public onFileDirectDownload($event: FileDataJson): void {
    const aufgabeId = requireValidBackendEntityId(this.store.selectedEntity()?.id);
    this.store.downloadAufgabeFile({
      aufgabeId: aufgabeId,
      fileId: $event.id,
      downloadDirectly: true,
    });
  }

  private afterSuccessfulSaveAndUpload(): void {
    this.form.reset();
    this.fileUploads = undefined;
    this.saveAction.emit();
  }
}
