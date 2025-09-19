import {ChangeDetectionStrategy, ChangeDetectorRef, Component, inject, input, output} from '@angular/core';
import {FormFullWidthDirective, IconOnlyButtonComponent} from '@dvb/ui-components';
import {AppNotificationStore} from '@dvb/util-angular';
import {TranslocoPipe, TranslocoService} from '@jsverse/transloco';
import {FileSystemFileEntry, NgxFileDropEntry, NgxFileDropModule} from 'ngx-file-drop';

@Component({
  selector: 'feat-file-drop',
  imports: [
    NgxFileDropModule,
    IconOnlyButtonComponent,
    TranslocoPipe,
  ],
  templateUrl: './file-drop.component.html',
  styleUrl: './file-drop.component.scss',
  changeDetection: ChangeDetectionStrategy.OnPush,
  // render the component in full width
  hostDirectives: [FormFullWidthDirective],
})
export class FileDropComponent {

  readonly acceptedFiletypes = input.required<string>();
  readonly readonly = input<boolean>(false);
  readonly multiple = input<boolean>(true);

  readonly #appNotificationStore = inject(AppNotificationStore);
  readonly #transloco = inject(TranslocoService);
  readonly cdRef = inject(ChangeDetectorRef);

  readonly filesToUpload = output<File[]>();
  public selectedFiles: File[] = []; // list of 'File' objects to emit to parent component
  public files: NgxFileDropEntry[] = [];

  public dropped(inputFiles: NgxFileDropEntry[]): void {
    if (!this.multiple() && this.files.length >= 1) {
      this.#appNotificationStore.addDangerNotification(
        this.#transloco.translate('ui.upload.action.upload.error.multipleFiles'),
      );
      return;
    }
    for (let i = 0; i < inputFiles.length; i++) {
      const file = inputFiles[i];
      if (file.fileEntry.isFile) {
        const droppedFile = file.fileEntry as FileSystemFileEntry;
        droppedFile.file((f: File) => {
          // the mime type is not checked by ngx-filedrop, we have to check it here
          const matchingMimeType = FileDropComponent.validateFileType(f, this.acceptedFiletypes());
          if (matchingMimeType) {
            this.files.push(file);
            this.selectedFiles.push(f);
            this.cdRef.detectChanges();

            // once all are processed, emit the files to the parent component
            if (i === inputFiles.length - 1) {
              this.filesToUpload.emit(this.selectedFiles);
            }
          } else {
            this.#appNotificationStore.addNotification({
              alertType: 'warning',
              id: crypto.randomUUID(),
              summary: this.#transloco.translate('ui.upload.action.upload.error.invalidFileType',
                {filename: f.name, mimeType: f.type}),
              autohide: true,
            });
          }
        });
      }
    }

  }

  static validateFileType(file: File, acceptedFiletypes: string): boolean {
    const acceptedTypes = acceptedFiletypes.split(',').map(type => type.trim());
    // Check MIME type
    if (acceptedTypes.includes(file.type)) {
      return true;
    }

    // Check file extension
    const fileExtension = '.' + file.name.split('.').pop()?.toLowerCase()?.trim();
    return acceptedTypes.includes(fileExtension);
  }

  public removeFileFromUpload(item: NgxFileDropEntry): void {
    if (this.files) {
      const indexToRemove = this.files.indexOf(item);
      if (indexToRemove > -1) {
        this.files.splice(indexToRemove, 1);
        const fileToRemove = item.fileEntry as FileSystemFileEntry;
        fileToRemove.file((f: File) => {
          // async remove file from selectedFiles
          this.selectedFiles = this.selectedFiles.filter(file => file.name !== f.name);
          // emit the updated files to the parent component
          this.filesToUpload.emit(this.selectedFiles);
        });
      }
    }
  }
}
