import {DatePipe} from '@angular/common';
import {ChangeDetectionStrategy, Component, computed, input, output} from '@angular/core';
import {FileDataJson} from '@dvb/data-access-backend';
import {
  EmptyTableMessageComponent,
  FileUploadButtonComponent,
  IconOnlyButtonComponent,
  SortableComponent,
  SpinnerComponent,
  TableComponent,
} from '@dvb/ui-components';

import {TableModel} from '@dvb/util-angular';
import {TranslocoPipe} from '@jsverse/transloco';

import {UploadTableConfig, uploadTableDefaults} from './upload-table-config';

@Component({
    selector: 'feat-upload-table',
    templateUrl: './upload-table.component.html',
    styleUrl: './upload-table.component.scss',
    changeDetection: ChangeDetectionStrategy.OnPush,
    imports: [
        TranslocoPipe,
        TableComponent,
        SortableComponent,
        FileUploadButtonComponent,
        SpinnerComponent,
        DatePipe,
        IconOnlyButtonComponent,
        EmptyTableMessageComponent,
    ]
})
export class UploadTableComponent {
  readonly fileUploaded = output<File>();
  readonly fileView = output<FileDataJson>();
  readonly fileDirectDownload = output<FileDataJson>();
  readonly fileRemove = output<FileDataJson>();
  readonly filePreview = output<FileDataJson>();

  readonly activePreviewFileId = input<string | null>(null);

  readonly uploadTable = input.required<TableModel<FileDataJson>>();
  readonly loading = input<boolean>(false);
  readonly readonly = input<boolean>(false);
  readonly accepted = input.required<string>();
  readonly working = input<boolean>(false);
  // eslint-disable-next-line @angular-eslint/no-input-rename
  readonly partialConfig = input<Partial<UploadTableConfig>>({}, {alias: 'config'});

  readonly tableConfig = computed<UploadTableConfig>(() => Object.assign(uploadTableDefaults, this.partialConfig()));
  readonly showTyp = computed<boolean>(() => this.tableConfig().columnBelegUploadTyp);
  readonly showActions = computed<boolean>(() => this.tableConfig().showViewAction || this.tableConfig().showRemoveAction || this.tableConfig().showDirectDownloadAction);
  readonly showSize = computed<boolean>(() => this.tableConfig().columnSize);
  readonly showHeader = computed<boolean>(() => !this.tableConfig().disableHeader && (this.uploadTable()
    .rawListSize() > 1));
  readonly showUploadButton = computed<boolean>(
    () =>
      !(
        this.tableConfig().disableUploadButton ||
        this.readonly() ||
        (this.tableConfig().singleFileStore && (this.uploadTable().displayedList()?.length ?? 0) > 0)
      ),
  );

  onFileUploaded($event: File): void {
    this.fileUploaded.emit($event);
  }
}
